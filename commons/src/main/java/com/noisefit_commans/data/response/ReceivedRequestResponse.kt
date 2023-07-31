package com.noisefit_commans.data.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ReceivedRequestResponse(
    val pendingCompetitionRequests: List<Requests>?,
    val pendingFriendRequests: List<Requests>?
)


@Parcelize
data class Requests(
    val user_id: Int,
    val comp_detail_id: Int?=null,
    val first_name: String?=null,
    val image_url: String?=null,
    val title: String?=null,
    val description: String?=null,
    val interests: List<String>?=null,
    var tempStatus: String?=null
) : Parcelable