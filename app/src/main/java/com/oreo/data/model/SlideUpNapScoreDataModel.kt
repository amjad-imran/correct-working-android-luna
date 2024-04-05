package com.oreo.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SlideUpNapScoreDataModel(
    var napId: String? = null,
    var title: String? = null,
    var description: String? = null,
    var oldSleepScore: Int? = 0,
    var newSleepScore: Int? = 0,
    var oldReadinessScore: Int? = 0,
    var newReadinessScore: Int? = 0,
    @SerializedName("score_impact")
    var scoreImpact: String? = null//positive/negative
) : Parcelable

enum class ScoreImpact {
    POSITIVE, NEGATIVE
}