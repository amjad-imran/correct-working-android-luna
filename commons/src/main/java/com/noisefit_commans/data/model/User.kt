package com.noisefit_commans.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.models.UserInfo
import com.noisefit_commans.models.UserLocation
import com.noisefit_commans.utils.StringUtils.capitalizeWords

data class User(
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
    @SerializedName("notifications_enabled")
    @Expose
    var notificationsEnabled: Int? = null,
    @SerializedName("notifications_enabled_luna")
    @Expose
    var notificationsEnabledLuna: Int? = null,
    @SerializedName("is_verified")
    @Expose
    var isVerified: Int? = null,
    @SerializedName("end_game")
    @Expose
    var endGame: String? = null,
    @SerializedName("user_info")
    @Expose
    var userInfo: UserInfo? = null,
    @SerializedName("location")
    @Expose
    var location: UserLocation? = null,
    @SerializedName("user_goals")
    @Expose
    var userGoals: UserGoals? = null,
    @SerializedName("isSelected")
    var isSelected: Boolean? = false,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("name")
    var name: String? = null,
    var interests: List<Interest>? = null,

    ) {

    fun getFullUserName(): String {
        return "$firstName"
    }

    fun getCity(): String {
        return location?.city ?: "_"
    }

    fun getMobileNumber(): String {
        if (mobile.isNullOrEmpty()) return ""

        val countryCode = countryCode ?: ""
        return "$countryCode $mobile"
    }

    fun getUserInterestToDisplay(default: String): String {
        if (interests.isNullOrEmpty()) {
            return default
        }
        return if (interests?.size == 1) {
            interests?.first()?.name?.capitalizeWords() ?: default
        } else {
            val firstInterest = interests?.first()?.name?.capitalizeWords() ?: ""
            "$firstInterest and ${(interests?.size ?: 2) - 1} more"
        }
    }

    fun getOnlyFirstName(): String {
        val userName = firstName
        if (!userName.isNullOrEmpty()) {
            val names = userName.split(" ")
            return names.first()
        }
        return ""
    }

    fun isNumberAvailable(): Boolean {
        return mobile.isNullOrEmpty()
    }


}

data class Token(
    val access_token: String? = null,
    val refresh_token: String? = null
)