package com.noisefit_commans.data.response

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.data.model.EndGame
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class ConfigResponse(
    @SerializedName("countries")
    @Expose
    var countries: @RawValue List<Country>? = null,
    @SerializedName("images")
    @Expose
    var images: List<String>? = null,
    @SerializedName("end_games")
    @Expose
    var endGames: ArrayList<EndGame>? = null
) : Parcelable

@Parcelize
data class Country(
    @SerializedName("flag")
    @Expose
    var flag: String? = null,
    @SerializedName("name")
    @Expose
    var name: String? = null,
    @SerializedName("code")
    @Expose
    var code: String? = null
) : Parcelable