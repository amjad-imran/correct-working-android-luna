package com.oreo.data.model


import com.oreo.ui.sleep2.SleepContributor

data class OHMDataModel(
    val type: SleepContributor,
    val value: String? = null,
    val valueTime: Int? = null,//For time based contributors
    val unit: String? = null,
    val status: String? = null,
    val text: String? = null
)