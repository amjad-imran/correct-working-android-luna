package com.noisefit_commans.data.model.challenge

import android.os.Parcelable
import com.noisefit_commans.data.response.Teams
import com.noisefit_commans.models.Units
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChallengeIds(
    var id: Int?,
    var teamId: Int?,
    var unit: Units? = Units.METRIC,
    var challengeType: String? = null,
    var teamName: String? = "",
    var shareText: String? = ""
) : Parcelable

@Parcelize
data class TeamList(
    var teamList: ArrayList<Teams>? = null,
) : Parcelable