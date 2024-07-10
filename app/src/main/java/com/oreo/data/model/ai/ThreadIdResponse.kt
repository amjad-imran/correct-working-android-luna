package com.oreo.data.model.ai

import com.google.gson.annotations.SerializedName

data class ThreadIdResponse(
    @SerializedName("thread_id")
    val threadId: String? = null
)
