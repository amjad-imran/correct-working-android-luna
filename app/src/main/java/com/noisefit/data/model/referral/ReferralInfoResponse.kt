package com.noisefit.data.model.referral

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class ReferralInfoResponse(
    @SerializedName("has_notification")
    val hasNotification: Boolean,
    @SerializedName("has_referral")
    val hasReferral: Boolean,
    @SerializedName("remaining_days")
    val remainingDays: Int? = null,
    @SerializedName("referral_title")
    val referralTitle: String? = null,
    @SerializedName("referral_text")
    val referralText: String? = null,
    @SerializedName("campaign_id")
    val campaignId: Long? = null,
    @SerializedName("referral_image")
    val referralImage: String? = null,
    val banner: List<Referral>? = null,
    val prize:Prize?=null
) : Parcelable


@Parcelize
data class Prize(
    @SerializedName("image_url")
    val image: String? = null,
    val text: String? = null,
) : Parcelable

@Parcelize
data class Referral(
    val type: String,
    val title: String? = null,
    val name: String? = null,
    @SerializedName("sub_title")
    val subTitle: String? = null,
    val status: String? = null,//todo check
    val date: String? = null,
    val prize: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
) : Parcelable

@Parcelize
data class CardStyle1(
    val title: String? = null,
    val subtitle: String? = null,
    val prize: String? = null,
    val image: String? = null
) : Parcelable

@Parcelize
data class CardStyle2(
    val title: String? = null,
    val subTitle: String? = null,
    val backgroundRes: Int,
    val textColor: Int
) : Parcelable

@Parcelize
data class CardStyle3(
    val name: String? = null,
    val date: String? = null,
    val status: String? = null,
    val textColor: Int,
    val selectedRingRes: Int,
    val defaultRing: Int,
    val backgroundRes: Int,
    val ringConnectRes: Int,
) : Parcelable