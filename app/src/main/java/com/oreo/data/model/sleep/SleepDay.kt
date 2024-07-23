package com.oreo.data.model.sleep

import com.google.gson.annotations.SerializedName
import com.oreo.data.model.health.CommonDataModel
import com.oreo.data.model.health.Nap
import com.oreo.data.model.health.Nudges

data class SleepDay(
    val date: String,

    @SerializedName("sleep_score")
    var sleepScore: CommonDataModel? = null,

    @SerializedName("sleep_duration")
    var sleepDuration: CommonDataModel? = null,


    val naps: List<Nap>? = null,
    val nudges: List<Nudges>?,


    )
