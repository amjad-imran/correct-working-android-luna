package com.noisefit.data.model.referral

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class ReferralInfoResponse(
    @SerializedName("has_referral")
    val hasReferral: Boolean,
    @SerializedName("remaining_days")
    val remainingDays: Int? = null,
    @SerializedName("referral_title")
    val referralTitle: String? = null,
    @SerializedName("referral_text")
    val referralText: String? = null,
    @SerializedName("referral_image")
    val referralImage: String? = null,
    val banner: List<Referral>? = null
) : Parcelable

@Parcelize
data class Referral(
    val type: String,
    val title: String? = null,
    @SerializedName("sub_title")
    val subTitle: String? = null,
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
    val backgroundRes: Int
) : Parcelable