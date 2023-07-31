package com.noisefit.data.remote

import com.google.gson.annotations.SerializedName


data class StateData(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("state_name")
    val name: String? = null,
)


data class CityData(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("city")
    val name: String? = null,
)


data class UserLocationUpdatedResponse(
    @SerializedName("isMapped")
    val isMapped: Boolean = false,
    @SerializedName("state_id")
    var stateId: Int? = null,
    @SerializedName("state_name")
    var state: String? = "",
    @SerializedName("city_id")
    var cityId: Int? = null,
    @SerializedName("city_name")
    var city: String? = null,
)