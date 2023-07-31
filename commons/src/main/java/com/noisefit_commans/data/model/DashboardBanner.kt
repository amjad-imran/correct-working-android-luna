package com.noisefit_commans.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DashboardBanner(val image_url: String = "", val data_url: String? = null, val type: Int) :
    Parcelable

@Parcelize
data class Content(val title: String = "", val subtitle: String = "", val image_url: String = "") :
    Parcelable

@Parcelize
data class FriendsWalkAround(
    val title: String = "",
    val subtitle: String = "",
    val image: Int
) : Parcelable
