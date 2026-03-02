package com.yangsheng.astrocal.ai

import com.yangsheng.astrocal.ai.AiMessage.Role
import com.yangsheng.astrocal.ai.providers.*

sealed class AiResult {
    data class Ok(val text: String) : AiResult()
    data class Error(val message: String) : AiResult()
}

object AiRouter {

    suspend fun reply(
        mode: AiMode,
        apiKey: String,
        messages: List<AiMessage>,
        // 你也可以把 model 放到 UI 里让用户选，这里先给默认值
        openAiModel: String = "gpt-4o-mini",
        geminiModel: String = "gemini-1.5-flash",
        claudeModel: String = "claude-3-5-sonnet-20240620",
    ): AiResult {

        val userText = messages.lastOrNull { it.role == Role.USER }?.content?.trim().orEmpty()

        if (mode == AiMode.OFF) {
            return AiResult.Error("AI is disabled.")
        }
        if (userText.isEmpty()) {
            return AiResult.Error("Empty input.")
        }

        return try {
            val provider = when (mode) {
                AiMode.LOCAL_RULES -> LocalRulesProvider
                AiMode.OPENAI -> {
                    if (apiKey.isBlank()) return AiResult.Error("API key is missing.")
                    OpenAiProvider(apiKey.trim(), openAiModel)
                }
                AiMode.GEMINI -> {
                    if (apiKey.isBlank()) return AiResult.Error("API key is missing.")
                    GeminiProvider(apiKey.trim(), geminiModel)
                }
                AiMode.CLAUDE -> {
                    if (apiKey.isBlank()) return AiResult.Error("API key is missing.")
                    ClaudeProvider(apiKey.trim(), claudeModel)
                }
                AiMode.OFF -> return AiResult.Error("AI is disabled.")
            }

            val text = provider.complete(messages)
            AiResult.Ok(text)
        } catch (e: Exception) {
            AiResult.Error(e.message ?: e.toString())
        }
    }
}

interface AiProvider {
    suspend fun complete(messages: List<AiMessage>): String
}