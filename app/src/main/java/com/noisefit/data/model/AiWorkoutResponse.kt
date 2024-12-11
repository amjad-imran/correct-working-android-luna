package com.noisefit.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class AiWorkoutResponse(
    val day:String?=null,
    val exercises:List<AiExerciseList>?=null,
)

@Parcelize
data class AiExerciseList(
    val reps:String?=null,
    val exercise_name:String?=null,
):Parcelable
