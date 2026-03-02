package com.yangsheng.astrocal.ai.providers

import com.yangsheng.astrocal.ai.AiMessage
import com.yangsheng.astrocal.ai.AiProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class GeminiProvider(
    private val apiKey: String,
    private val model: String
) : AiProvider {

    private val client = OkHttpClient()

    override suspend fun complete(messages: List<AiMessage>): String {
        // Gemini expects "contents": [{role, parts:[{text}]}]
        val contents = JSONArray()
        for (m in messages) {
            val role = when (m.role) {
                AiMessage.Role.USER -> "user"
                AiMessage.Role.ASSISTANT -> "model"
                AiMessage.Role.SYSTEM -> "user" // map system to user prefix
            }
            val contentText =
                if (m.role == AiMessage.Role.SYSTEM) "System: ${m.content}" else m.content

            val parts = JSONArray().put(JSONObject().put("text", contentText))
            contents.put(JSONObject().put("role", role).put("parts", parts))
        }

        val bodyJson = JSONObject().put("contents", contents)

        val url =
            "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val req = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val resp = client.newCall(req).execute()
        val text = resp.body?.string().orEmpty()
        if (!resp.isSuccessful) {
            throw RuntimeException("Gemini error ${resp.code}: $text")
        }

        val json = JSONObject(text)
        val candidates = json.optJSONArray("candidates") ?: return "(no candidates)"
        if (candidates.length() == 0) return "(no candidates)"
        val content = candidates.getJSONObject(0).optJSONObject("content") ?: return "(no content)"
        val parts = content.optJSONArray("parts") ?: return "(no parts)"
        if (parts.length() == 0) return "(no parts)"
        return parts.getJSONObject(0).optString("text").trim().ifEmpty { "(empty)" }
    }
}