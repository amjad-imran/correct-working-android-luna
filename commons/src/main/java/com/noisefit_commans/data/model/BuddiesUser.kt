package com.noisefit_commans.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BuddiesUser(
    @SerializedName("id")
    @Expose
    var id: Int? = null,
    @SerializedName("first_name")
    @Expose
    var firstName: String? = null,
    @SerializedName("country_code")
    @Expose
    var countryCode: String? = null,
    @SerializedName("email")
    @Expose
    var email: String? = null,
    @SerializedName("mobile")
    @Expose
    var mobile: String? = null,

    @SerializedName("image_url")
    @Expose
    var imageUrl: String? = null,

    @SerializedName("info")
    @Expose
    var info: String? = null,


    @SerializedName("isSelected") var isSelected: Boolean? = false,
    @SerializedName("status") val status: String? = null,
    @SerializedName("name") var name: String? = null,

    ) {

    fun getFullUserName(): String {
        return "$firstName"
    }

    fun getMobileNumber(): String {
        if (mobile.isNullOrEmpty()) return ""

        val countryCode = countryCode ?: ""
        return "$countryCode $mobile"
    }

}