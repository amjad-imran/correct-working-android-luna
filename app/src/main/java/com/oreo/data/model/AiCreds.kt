package com.oreo.data.model

data class AiCreds(
    val OPENAI_API_KEY: String? = null,
    val prompt: Prompts? = null,
)

data class Prompts(
    val audio: String? = null
)