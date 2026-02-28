package com.yangsheng.astrocal.util

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

data class GeoPlace(
    val name: String,
    val admin1: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String?
) {
    val label: String
        get() = listOfNotNull(name, admin1, country).joinToString(", ")
}

data class HourlyWeather(
    val timeIso: String,
    val temperature2m: Double?,
    val windSpeed10m: Double?,
    val precipProb: Int?,
    val cloudCover: Int?,
    val cloudLow: Int?,
    val cloudMid: Int?,
    val cloudHigh: Int?
)

data class WeatherForecast(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val hours: List<HourlyWeather>
)

object OpenMeteoService {

    fun searchCity(query: String, count: Int = 8, language: String = "en"): List<GeoPlace> {
        val q = URLEncoder.encode(query.trim(), "UTF-8")
        val url = URL("https://geocoding-api.open-meteo.com/v1/search?name=$q&count=$count&language=$language&format=json")
        val json = httpGetJson(url)

        val results = json.optJSONArray("results") ?: return emptyList()
        val out = ArrayList<GeoPlace>(results.length())
        for (i in 0 until results.length()) {
            val o = results.getJSONObject(i)
            out.add(
                GeoPlace(
                    name = o.optString("name", ""),
                    admin1 = o.optString("admin1", null),
                    country = o.optString("country", null),
                    latitude = o.getDouble("latitude"),
                    longitude = o.getDouble("longitude"),
                    timezone = o.optString("timezone", null)
                )
            )
        }
        return out
    }

    fun fetchHourlyForecast(
        latitude: Double,
        longitude: Double,
        timezone: String? = "auto"
    ): WeatherForecast {
        val tz = URLEncoder.encode(timezone ?: "auto", "UTF-8")

        // 你关心：云量(总/低中高) + 温度 + 风 + 降水概率
        val hourly =
            "temperature_2m,wind_speed_10m,precipitation_probability,cloud_cover,cloud_cover_low,cloud_cover_mid,cloud_cover_high"

        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=$latitude&longitude=$longitude" +
                    "&hourly=$hourly" +
                    "&timezone=$tz"
        )

        val json = httpGetJson(url)
        val usedTz = json.optString("timezone", timezone ?: "auto")

        val hourlyObj = json.getJSONObject("hourly")
        val times = hourlyObj.getJSONArray("time")

        fun arr(name: String): JSONArray? = hourlyObj.optJSONArray(name)

        val t2m = arr("temperature_2m")
        val wind = arr("wind_speed_10m")
        val pprob = arr("precipitation_probability")
        val cc = arr("cloud_cover")
        val ccl = arr("cloud_cover_low")
        val ccm = arr("cloud_cover_mid")
        val cch = arr("cloud_cover_high")

        val n = times.length()
        val hours = ArrayList<HourlyWeather>(n)
        for (i in 0 until n) {
            hours.add(
                HourlyWeather(
                    timeIso = times.getString(i),
                    temperature2m = t2m?.optDoubleOrNull(i),
                    windSpeed10m = wind?.optDoubleOrNull(i),
                    precipProb = pprob?.optIntOrNull(i),
                    cloudCover = cc?.optIntOrNull(i),
                    cloudLow = ccl?.optIntOrNull(i),
                    cloudMid = ccm?.optIntOrNull(i),
                    cloudHigh = cch?.optIntOrNull(i)
                )
            )
        }

        return WeatherForecast(
            latitude = latitude,
            longitude = longitude,
            timezone = usedTz,
            hours = hours
        )
    }

    // ---------- helpers ----------

    private fun httpGetJson(url: URL): JSONObject {
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) {
                throw RuntimeException("HTTP $code: $text")
            }
            return JSONObject(text)
        } finally {
            conn.disconnect()
        }
    }

    private fun JSONArray.optDoubleOrNull(i: Int): Double? {
        if (i < 0 || i >= length()) return null
        if (isNull(i)) return null
        return try { getDouble(i) } catch (_: Throwable) { null }
    }

    private fun JSONArray.optIntOrNull(i: Int): Int? {
        if (i < 0 || i >= length()) return null
        if (isNull(i)) return null
        return try { getInt(i) } catch (_: Throwable) { null }
    }
}