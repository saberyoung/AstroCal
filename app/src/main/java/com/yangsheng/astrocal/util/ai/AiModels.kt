package com.yangsheng.astrocal.ai

data class AiMessage(
    val role: Role,
    val content: String
) {
    enum class Role { SYSTEM, USER, ASSISTANT }
}