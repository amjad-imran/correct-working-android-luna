package com.noisefit_commans.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BuddiesUserNew(
    @SerializedName("user_id")
    @Expose
    val id: Int? = null,
    @SerializedName("first_name")
    @Expose
    val firstName: String? = null,
    @SerializedName("last_name")
    @Expose
    val lastName: String? = null,
    @SerializedName("mobile")
    @Expose
    val mobile: String? = null,
    @SerializedName("image_url")
    @Expose
    val imageUrl: String? = null,
    @SerializedName("interests")
    @Expose
    val interests: ArrayList<String>? = null,
    @SerializedName("status")
    @Expose
    var status: Int = 0,

    ) {

    fun getFullUserName(): String {
//        if (lastName.isNullOrEmpty()) {
//            return firstName ?: ""
//        }
        return "$firstName"
    }


}