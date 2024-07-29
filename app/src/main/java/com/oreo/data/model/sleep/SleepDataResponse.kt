package com.oreo.data.model.sleep

import com.google.gson.annotations.SerializedName

data class SleepDataResponse(
    val result: List<SleepDay>? = null,
    @SerializedName("register_date")
    val registerDate: Int? = null,
)
