package com.yangsheng.astrocal.ai.providers

import com.yangsheng.astrocal.ai.AiMessage
import com.yangsheng.astrocal.ai.AiProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class OpenAiProvider(
    private val apiKey: String,
    private val model: String
) : AiProvider {

    private val client = OkHttpClient()

    override suspend fun complete(messages: List<AiMessage>): String {
        val arr = JSONArray()
        for (m in messages) {
            val role = when (m.role) {
                AiMessage.Role.SYSTEM -> "system"
                AiMessage.Role.USER -> "user"
                AiMessage.Role.ASSISTANT -> "assistant"
            }
            arr.put(JSONObject().put("role", role).put("content", m.content))
        }

        val bodyJson = JSONObject()
            .put("model", model)
            .put("messages", arr)

        val req = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val resp = client.newCall(req).execute()
        val text = resp.body?.string().orEmpty()
        if (!resp.isSuccessful) {
            throw RuntimeException("OpenAI error ${resp.code}: $text")
        }

        val json = JSONObject(text)
        val choices = json.getJSONArray("choices")
        val msg = choices.getJSONObject(0).getJSONObject("message")
        return msg.optString("content").trim().ifEmpty { "(empty)" }
    }
}