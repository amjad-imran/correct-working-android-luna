package com.noisefit.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class AiWorkoutResponse(
    val day_name: String? = null,//day_1, day_2
    val workouts: List<AiWorkouts>? = null,
)

@Parcelize
data class AiWorkouts(
    val session: String? = null,
    val workout: List<AiWorkout>? = null,
) : Parcelable

@Parcelize
data class AiWorkout(
    val reps: String? = null,
    val workout_name: String? = null,
    val description: String? = null,
) : Parcelable


