package com.noisefit_commans.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VoucherDetailsData(
    @SerializedName("id")
    @Expose
    val id: Int? = null,
    @SerializedName("title")
    @Expose
    val title: String? = null,
    @SerializedName("brand")
    @Expose
    val brand: String? = null,
    @SerializedName("sub_title")
    @Expose
    val subTitle: String? = null,
    @SerializedName("how_to_avail")
    @Expose
    val howToAvail: List<String>? = null,
    @SerializedName("terms_and_conditions")
    @Expose
    val termsAndConditions: List<String>? = null,
    @SerializedName("image_url")
    @Expose
    val imageUrl: String? = null,

    @SerializedName("logo_url")
    @Expose
    val logoUrl: String? = null,


    @SerializedName("points")
    @Expose
    val points: Int? = null,
    @SerializedName("redirection_link")
    @Expose
    val redirectionLink: String? = null,
    @SerializedName("coupon_code")
    @Expose
    val couponCode: String? = null,
    @SerializedName("state")
    @Expose
    val state: String? = null,
    @SerializedName("valid_till")
    @Expose
    val validTill: String? = null,

    val is_eligible: Boolean? = null,
)