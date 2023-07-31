package com.noisefit_commans.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ContactUser(
    @SerializedName("id")
    @Expose
    var id: Int? = null,
    @SerializedName("first_name")
    @Expose
    var firstName: String? = null,
    @SerializedName("country_code")
    @Expose
    var countryCode: String? = null,
    @SerializedName("mobile")
    @Expose
    var mobile: String? = null,

    @SerializedName("image_url")
    @Expose
    var imageUrl: String? = null,
    @SerializedName("interests")
    @Expose
    var interests: ArrayList<String>? = arrayListOf(),
)