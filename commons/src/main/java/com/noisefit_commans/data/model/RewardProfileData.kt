package com.noisefit_commans.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RewardProfileData(
    @SerializedName("points")
    @Expose
    val points: Int? = null,
    @SerializedName("voucher_count")
    @Expose
    val voucherCount: Int? = null,
    @SerializedName("task_list")
    @Expose
    val taskList: List<TaskList>? = null,
    @SerializedName("coupon_list")
    @Expose
    val couponList: List<CouponList>? = null,

    )

data class CouponList(
    @SerializedName("id")
    @Expose
    val id: Int? = null,
    @SerializedName("title")
    @Expose
    val title: String? = null,
    @SerializedName("points")
    @Expose
    val points: Int? = null,
    @SerializedName("image_url")
    @Expose
    val imageUrl: String? = null,
    @SerializedName("is_eligible")
    @Expose
    val isEligible: Boolean,
    @SerializedName("brand")
    @Expose
    val brand: String?=null,

    )


data class RewardAboutData(
    @SerializedName("title")
    @Expose
    val title: String? = null,
    @SerializedName("description")
    @Expose
    val subtitle: String? = null,
    @SerializedName("sub_category")
    @Expose
    val rewardAboutSubCategoryList: ArrayList<RewardAboutSubCategoryList>? = null
)

data class RewardAboutSubCategoryList(
    @SerializedName("title")
    @Expose
    val title: String? = null,
    @SerializedName("description")
    @Expose
    val subtitle: String? = null,
    @SerializedName("image_url")
    @Expose
    val url: String? = null
)