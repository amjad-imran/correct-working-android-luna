package com.oreo.data.model.timeline.habits

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HabitsByDateResponse(
    @SerializedName("options") var options: ArrayList<Options> = arrayListOf()
) : Parcelable {

    @Parcelize
    data class Options(
        @SerializedName("time_tracker_option_id") var timeTrackerOptionId: Int? = null,
        @SerializedName("options") var options: String? = null,
        @SerializedName("type") var type: String? = null,
        @SerializedName("workout_type") var workoutType: String? = null,
        @SerializedName("typeLabel") var typeLabel: String? = null,
        @SerializedName("created_at") var createdAt: String? = null,
        @SerializedName("is_completed") var isCompleted: Boolean = false,
        @SerializedName("is_cancelled") var isCancelled: Boolean = false,

        // for app
        val state: State = State.Normal,
        var canBeLogged: Boolean = true,
    ) : Parcelable{
        enum class State { Normal, Skipping }
    }

}
