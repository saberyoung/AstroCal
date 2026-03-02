package com.yangsheng.astrocal.ai.providers

import com.yangsheng.astrocal.ai.AiMessage
import com.yangsheng.astrocal.ai.AiProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class ClaudeProvider(
    private val apiKey: String,
    private val model: String
) : AiProvider {

    private val client = OkHttpClient()

    override suspend fun complete(messages: List<AiMessage>): String {
        // Anthropic Messages API:
        // { model, max_tokens, system, messages:[{role, content:[{type:"text", text:"..."}]}] }

        val systemText = messages.firstOrNull { it.role == AiMessage.Role.SYSTEM }?.content ?: ""

        val msgArr = JSONArray()
        for (m in messages) {
            if (m.role == AiMessage.Role.SYSTEM) continue
            val role = when (m.role) {
                AiMessage.Role.USER -> "user"
                AiMessage.Role.ASSISTANT -> "assistant"
                AiMessage.Role.SYSTEM -> "user"
            }
            val content = JSONArray().put(JSONObject().put("type", "text").put("text", m.content))
            msgArr.put(JSONObject().put("role", role).put("content", content))
        }

        val bodyJson = JSONObject()
            .put("model", model)
            .put("max_tokens", 800)
            .put("system", systemText)
            .put("messages", msgArr)

        val req = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val resp = client.newCall(req).execute()
        val text = resp.body?.string().orEmpty()
        if (!resp.isSuccessful) {
            throw RuntimeException("Claude error ${resp.code}: $text")
        }

        val json = JSONObject(text)
        val content = json.optJSONArray("content") ?: return "(no content)"
        if (content.length() == 0) return "(no content)"
        return content.getJSONObject(0).optString("text").trim().ifEmpty { "(empty)" }
    }
}