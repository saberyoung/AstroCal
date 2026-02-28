package com.yangsheng.astrocal.util

import java.time.*
import kotlin.math.*

enum class TimeInputType { ISO_UTC, JD, MJD, GPS }

data class TimeOutputs(
    val isoUtc: String,
    val jd: Double,
    val mjd: Double,
    val gpsSeconds: Double
)

object TimeUtils {

    fun parseToInstantUtc(type: TimeInputType, raw: String): Instant {
        val s = raw.trim()
        require(s.isNotEmpty()) { "empty" }
        return when (type) {
            TimeInputType.ISO_UTC -> parseIsoUtcToInstant(s)
            TimeInputType.JD -> jdToInstantUtc(s.toDouble())
            TimeInputType.MJD -> mjdToInstantUtc(s.toDouble())
            TimeInputType.GPS -> gpsSecondsToInstantUtc(s.toDouble())
        }
    }

    fun computeAllOutputs(instantUtc: Instant): TimeOutputs {
        val jd = instantUtcToJd(instantUtc)
        val mjd = jd - 2400000.5
        val gps = instantUtcToGpsSeconds(instantUtc)
        return TimeOutputs(
            isoUtc = formatInstantIsoUtc(instantUtc),
            jd = jd,
            mjd = mjd,
            gpsSeconds = gps
        )
    }

    // ---- implementation ----

    private const val UNIX_EPOCH_JD = 2440587.5
    private const val SECONDS_PER_DAY = 86400.0

    private val GPS_EPOCH_INSTANT: Instant =
        OffsetDateTime.parse("1980-01-06T00:00:00Z").toInstant()

    private fun parseIsoUtcToInstant(raw: String): Instant {
        val s = raw.replace(" ", "T")
        return try {
            OffsetDateTime.parse(s).toInstant()
        } catch (_: Throwable) {
            val ldt = LocalDateTime.parse(s)
            ldt.toInstant(ZoneOffset.UTC)
        }
    }

    private fun formatInstantIsoUtc(ins: Instant): String {
        return OffsetDateTime.ofInstant(ins, ZoneOffset.UTC).toString()
    }

    private fun instantUtcToJd(ins: Instant): Double {
        val unixSeconds = ins.epochSecond.toDouble() + ins.nano.toDouble() * 1e-9
        return UNIX_EPOCH_JD + unixSeconds / SECONDS_PER_DAY
    }

    private fun jdToInstantUtc(jd: Double): Instant {
        val unixSeconds = (jd - UNIX_EPOCH_JD) * SECONDS_PER_DAY
        val sec = floor(unixSeconds).toLong()
        val nano = ((unixSeconds - sec) * 1e9).roundToLong().coerceIn(0L, 999_999_999L)
        return Instant.ofEpochSecond(sec, nano)
    }

    private fun mjdToInstantUtc(mjd: Double): Instant = jdToInstantUtc(mjd + 2400000.5)

    private data class LeapEntry(val effectiveUtc: Instant, val gpsMinusUtc: Int)

    // GPS-UTC = 18s since 2017-01-01 (update if new leap seconds occur)
    private val LEAP_TABLE: List<LeapEntry> = listOf(
        LeapEntry(OffsetDateTime.parse("1980-01-06T00:00:00Z").toInstant(), 0),
        LeapEntry(OffsetDateTime.parse("1981-07-01T00:00:00Z").toInstant(), 1),
        LeapEntry(OffsetDateTime.parse("1982-07-01T00:00:00Z").toInstant(), 2),
        LeapEntry(OffsetDateTime.parse("1983-07-01T00:00:00Z").toInstant(), 3),
        LeapEntry(OffsetDateTime.parse("1985-07-01T00:00:00Z").toInstant(), 4),
        LeapEntry(OffsetDateTime.parse("1988-01-01T00:00:00Z").toInstant(), 5),
        LeapEntry(OffsetDateTime.parse("1990-01-01T00:00:00Z").toInstant(), 6),
        LeapEntry(OffsetDateTime.parse("1991-01-01T00:00:00Z").toInstant(), 7),
        LeapEntry(OffsetDateTime.parse("1992-07-01T00:00:00Z").toInstant(), 8),
        LeapEntry(OffsetDateTime.parse("1993-07-01T00:00:00Z").toInstant(), 9),
        LeapEntry(OffsetDateTime.parse("1994-07-01T00:00:00Z").toInstant(), 10),
        LeapEntry(OffsetDateTime.parse("1996-01-01T00:00:00Z").toInstant(), 11),
        LeapEntry(OffsetDateTime.parse("1997-07-01T00:00:00Z").toInstant(), 12),
        LeapEntry(OffsetDateTime.parse("1999-01-01T00:00:00Z").toInstant(), 13),
        LeapEntry(OffsetDateTime.parse("2006-01-01T00:00:00Z").toInstant(), 14),
        LeapEntry(OffsetDateTime.parse("2009-01-01T00:00:00Z").toInstant(), 15),
        LeapEntry(OffsetDateTime.parse("2012-07-01T00:00:00Z").toInstant(), 16),
        LeapEntry(OffsetDateTime.parse("2015-07-01T00:00:00Z").toInstant(), 17),
        LeapEntry(OffsetDateTime.parse("2017-01-01T00:00:00Z").toInstant(), 18),
    )

    private fun gpsMinusUtcAt(utc: Instant): Int {
        var last = LEAP_TABLE.first().gpsMinusUtc
        for (e in LEAP_TABLE) {
            if (utc >= e.effectiveUtc) last = e.gpsMinusUtc else break
        }
        return last
    }

    private fun instantUtcToGpsSeconds(utc: Instant): Double {
        val offset = gpsMinusUtcAt(utc).toDouble()
        val dt = Duration.between(GPS_EPOCH_INSTANT, utc)
        val sec = dt.seconds.toDouble() + dt.nano.toDouble() * 1e-9
        return sec + offset
    }

    private fun gpsSecondsToInstantUtc(gpsSeconds: Double): Instant {
        var utcGuess = GPS_EPOCH_INSTANT.plusNanos((gpsSeconds * 1e9).roundToLong())
        repeat(5) {
            val offset = gpsMinusUtcAt(utcGuess).toDouble()
            val corrected = gpsSeconds - offset
            val newGuess = GPS_EPOCH_INSTANT.plusNanos((corrected * 1e9).roundToLong())
            if (newGuess == utcGuess) return utcGuess
            utcGuess = newGuess
        }
        return utcGuess
    }
}