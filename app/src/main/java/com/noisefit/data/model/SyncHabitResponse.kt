package com.noisefit.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SyncHabitResponse(
    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("data") var data: Data? = Data(),
    @SerializedName("message") var message: String? = null,
    @SerializedName("time") var time: String? = null
) : Parcelable {

    @Parcelize
    data class Data(
        @SerializedName("status") var status: Boolean? = null
    ) : Parcelable

}