package com.noisefit_commans.data.response

data class GraphHighlightResponse(
    val daily: Daily,
    val weekly: Weekly,
    val monthly: Monthly,
    val max: Max
)

data class Max(
    val calories_msg: String,
    val distance_msg: String,
    val msg: String,
    val steps: Long? = 0,
    val distance: Long? = 0,
    val calories: Long? = 0,
    val date: String? = null
)

data class Daily(
    val calories_msg: String,
    val distance_msg: String,
    val msg: String,
    val today: Values,
    val yesterday: Values
)

data class Values(
    val steps: Long? = 0,
    val distance: Long? = 0,
    var calories: Long? = 0,
    val label: String
)

data class Weekly(
    val distance_msg: String,
    val calories_msg: String,
    val msg: String,
    val current: Values,
    val last: Values
)

data class Monthly(
    val calories_msg: String,
    val distance_msg: String,
    val msg: String,
    val current: Values,
    val last: Values
)