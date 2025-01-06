package com.oreo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatCompletionResponse(
    val choices: List<Choice>? = null
):Parcelable

@Parcelize
data class Choice(
    val index: Int,
    val delta: Delta? = null
):Parcelable

@Parcelize
data class Delta(
    val role: String? = null,
    val audio: Audio? = null
):Parcelable

@Parcelize
data class Audio(
    val id: String? = null,
    val data: String? = null,
    val transcript: String? = null
):Parcelable
