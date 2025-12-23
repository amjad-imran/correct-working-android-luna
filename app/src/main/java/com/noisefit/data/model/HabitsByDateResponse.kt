package com.noisefit.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HabitsByDateResponse(
    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("data") var data: Data? = Data(),
    @SerializedName("message") var message: String? = null,
    @SerializedName("time") var time: String? = null
) : Parcelable {

    @Parcelize
    data class Data(
        @SerializedName("options") var options: ArrayList<Options> = arrayListOf()
    ) : Parcelable

    @Parcelize
    data class Options(
        @SerializedName("time_tracker_option_id") var timeTrackerOptionId: Int? = null,
        @SerializedName("options") var options: String? = null,
        @SerializedName("type") var type: String? = null,
        @SerializedName("typeLabel") var typeLabel: String? = null,
        @SerializedName("created_at") var createdAt: String? = null,
        @SerializedName("is_completed") var isCompleted: Boolean? = null
    ) : Parcelable

}
