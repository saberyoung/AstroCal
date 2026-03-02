package com.yangsheng.astrocal.ai.providers

import com.yangsheng.astrocal.ai.AiMessage
import com.yangsheng.astrocal.ai.AiProvider
import com.yangsheng.astrocal.ai.AiMessage.Role
import java.time.Instant
import kotlin.math.*

object LocalRulesProvider : AiProvider {

    override suspend fun complete(messages: List<AiMessage>): String {
        val text = messages.lastOrNull { it.role == Role.USER }?.content.orEmpty().trim()
        if (text.isEmpty()) return help()

        val lower = text.lowercase()

        // Friendly natural-language shortcuts
        if (lower.contains("degree") && (lower.contains("hms") || lower.contains("dms"))) {
            // try to extract 2 numbers like "30 40"
            val nums = Regex("""-?\d+(\.\d+)?""").findAll(lower).map { it.value.toDouble() }.toList()
            if (nums.isNotEmpty()) {
                val raDeg = nums[0]
                val decDeg = nums.getOrNull(1)
                val ra = degToHms(raDeg)
                val dec = decDeg?.let { degToDms(it) }
                return buildString {
                    appendLine("RA (deg) $raDeg  →  HMS: $ra")
                    if (dec != null) appendLine("Dec (deg) ${decDeg}  →  DMS: $dec")
                }.trim()
            }
        }

        // Command style
        val parts = text.split(Regex("""\s+""")).filter { it.isNotBlank() }
        val cmd = parts.firstOrNull()?.lowercase() ?: return help()

        return when (cmd) {
            "help", "?" -> help()

            "deg2hms" -> {
                val x = parts.getOrNull(1)?.toDoubleOrNull() ?: return "Usage: deg2hms <deg>"
                degToHms(x)
            }

            "deg2dms" -> {
                val x = parts.getOrNull(1)?.toDoubleOrNull() ?: return "Usage: deg2dms <deg>"
                degToDms(x)
            }

            "hms2deg" -> {
                val x = parts.getOrNull(1) ?: return "Usage: hms2deg <HH:MM:SS[.sss]>"
                val deg = hmsToDeg(x)
                "HMS $x  →  deg: ${fmt6(deg)}"
            }

            "dms2deg" -> {
                val x = parts.getOrNull(1) ?: return "Usage: dms2deg <±DD:MM:SS[.sss]>"
                val deg = dmsToDeg(x)
                "DMS $x  →  deg: ${fmt6(deg)}"
            }

            "sep", "separation", "angularsep", "angsep" -> {
                // sep ra1 dec1 ra2 dec2 (degrees)
                val ra1 = parts.getOrNull(1)?.toDoubleOrNull()
                val dec1 = parts.getOrNull(2)?.toDoubleOrNull()
                val ra2 = parts.getOrNull(3)?.toDoubleOrNull()
                val dec2 = parts.getOrNull(4)?.toDoubleOrNull()
                if (ra1 == null || dec1 == null || ra2 == null || dec2 == null) {
                    return "Usage: sep <ra1_deg> <dec1_deg> <ra2_deg> <dec2_deg>"
                }
                val deg = angularSeparationDeg(ra1, dec1, ra2, dec2)
                "Separation: ${fmt6(deg)} deg  (${fmt3(deg * 3600.0)} arcsec)"
            }

            "jd" -> {
                if (parts.getOrNull(1)?.lowercase() == "now") {
                    val jd = instantToJulianDate(Instant.now())
                    "JD now: ${fmt6(jd)}"
                } else {
                    "Usage: jd now"
                }
            }

            "mjd" -> {
                if (parts.getOrNull(1)?.lowercase() == "now") {
                    val jd = instantToJulianDate(Instant.now())
                    val mjd = jd - 2400000.5
                    "MJD now: ${fmt6(mjd)}"
                } else {
                    "Usage: mjd now"
                }
            }

            else -> {
                // fallback: try parse "30 40" as ra/dec deg
                val nums = Regex("""-?\d+(\.\d+)?""").findAll(text).map { it.value.toDouble() }.toList()
                if (nums.size >= 1) {
                    val ra = degToHms(nums[0])
                    val dec = nums.getOrNull(1)?.let { degToDms(it) }
                    buildString {
                        appendLine("RA HMS: $ra")
                        if (dec != null) appendLine("Dec DMS: $dec")
                        appendLine("")
                        append(help())
                    }.trim()
                } else {
                    help()
                }
            }
        }
    }

    private fun help(): String = """
Local rules commands:
- deg2hms <deg>
- deg2dms <deg>
- hms2deg <HH:MM:SS[.sss]>
- dms2deg <±DD:MM:SS[.sss]>
- sep <ra1_deg> <dec1_deg> <ra2_deg> <dec2_deg>
- jd now
- mjd now

Examples:
- deg2hms 30.5
- deg2dms -12.345
- hms2deg 12:34:56
- dms2deg -12:34:56
- sep 150 2 151 2.2
""".trim()

    private fun degToHms(deg: Double): String {
        val hours = (deg / 15.0)
        val sign = if (hours < 0) "-" else ""
        val h = abs(hours)
        val hh = floor(h).toInt()
        val mmFull = (h - hh) * 60.0
        val mm = floor(mmFull).toInt()
        val ss = (mmFull - mm) * 60.0
        return "$sign${hh.toString().padStart(2, '0')}:${mm.toString().padStart(2, '0')}:${fmt3(ss).padStart(6, '0')}"
    }

    private fun degToDms(deg: Double): String {
        val sign = if (deg < 0) "-" else "+"
        val d = abs(deg)
        val dd = floor(d).toInt()
        val mmFull = (d - dd) * 60.0
        val mm = floor(mmFull).toInt()
        val ss = (mmFull - mm) * 60.0
        return "$sign${dd.toString().padStart(2, '0')}:${mm.toString().padStart(2, '0')}:${fmt3(ss).padStart(6, '0')}"
    }

    private fun hmsToDeg(hms: String): Double {
        val p = hms.trim().split(":")
        require(p.size >= 2) { "Invalid HMS." }
        val hh = p[0].toDouble()
        val mm = p[1].toDouble()
        val ss = p.getOrNull(2)?.toDouble() ?: 0.0
        val hours = hh + mm / 60.0 + ss / 3600.0
        return hours * 15.0
    }

    private fun dmsToDeg(dms: String): Double {
        val s = dms.trim()
        val sign = if (s.startsWith("-")) -1 else 1
        val t = s.removePrefix("+").removePrefix("-")
        val p = t.split(":")
        require(p.size >= 2) { "Invalid DMS." }
        val dd = p[0].toDouble()
        val mm = p[1].toDouble()
        val ss = p.getOrNull(2)?.toDouble() ?: 0.0
        val deg = dd + mm / 60.0 + ss / 3600.0
        return sign * deg
    }

    private fun angularSeparationDeg(ra1: Double, dec1: Double, ra2: Double, dec2: Double): Double {
        val r1 = Math.toRadians(ra1)
        val d1 = Math.toRadians(dec1)
        val r2 = Math.toRadians(ra2)
        val d2 = Math.toRadians(dec2)

        val sinD1 = sin(d1)
        val sinD2 = sin(d2)
        val cosD1 = cos(d1)
        val cosD2 = cos(d2)
        val cosDR = cos(r1 - r2)

        val cosSep = sinD1 * sinD2 + cosD1 * cosD2 * cosDR
        val sep = acos(cosSep.coerceIn(-1.0, 1.0))
        return Math.toDegrees(sep)
    }

    // JD conversion (UTC) — enough for utility use
    private fun instantToJulianDate(inst: Instant): Double {
        val unixSeconds = inst.epochSecond.toDouble() + inst.nano.toDouble() * 1e-9
        return unixSeconds / 86400.0 + 2440587.5
    }

    private fun fmt3(x: Double): String = String.format("%.3f", x)
    private fun fmt6(x: Double): String = String.format("%.6f", x)
}