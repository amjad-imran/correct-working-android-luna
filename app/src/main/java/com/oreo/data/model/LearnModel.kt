package com.oreo.data.model

data class LearnModel(
    val title: String? = null, val content: String? = null,
    val type: String? = null, val url: String? = null, val banner: String? = null
)

sealed class StressUnderstandingOverview {

    class ImageWithText(
        val title: String,
        val image: Int,
        val description: String
    ) : StressUnderstandingOverview()

    class TextWithAdapter(
        val title: String,
        val description: String,
        var subList: ArrayList<StressUnderstandingSubList> = ArrayList(),
        var showBottomLine: Boolean
    ) : StressUnderstandingOverview()

}

data class StressUnderstandingSubList(
    val title: String,
    val image: Int
)

data class StressSplashModel(
    val title: String,
    val image: Int,
    val description: String
)