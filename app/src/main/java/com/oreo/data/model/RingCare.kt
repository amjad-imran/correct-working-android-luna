package com.oreo.data.model

data class RingCareResponse(
    val care: List<RingCare>,
)
data class RingCare(
    val title: String,
    val content: List<String>
)

data class RingWelcome(
    val title: String,
    val url: String,
    val content: List<String>
)
