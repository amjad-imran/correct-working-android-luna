package com.oreo.data.model

data class FabModel(
    val title: String,
    val icon: Int,
    val color: Int,
    val type: FabItems,
)

enum class FabItems{
    RECORD_WORKOUT,
    ADD_WORKOUT,
    ADD_SLEEP,
    TRACK_PERIOD,
    ADD_OTHER_ACTIVITY
}