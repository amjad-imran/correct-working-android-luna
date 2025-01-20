package com.noisefit_commans.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class SleepDataGoogleFit(
    var startTime: Long,
    var endTime: Long,
    var sleepArray: ArrayList<SleepDataBreakup>? = null
) {

    data class SleepDataBreakup(
        var startTime: Long,
        var endTime: Long,
        var sleepType: String
    )
}

enum class ManualMeasureType() {
    HEART_RATE,
    BLOOD_OXYGEN,
    STRESS,
    BODY_TEMPERATURE
}

data class SportsDataGoogleFit(
    val startTime: Long,
    val endTime: Long,
    val distance: Float,
    val duration: Int,
    val calories: Float,
    val heartRate: Float,
    val steps: Int,
    val type: String
)

data class StepDataGoogleFit(
    val startTime: Long,
    val endTime: Long,
    val steps: Int,
    val totalSteps: Int
)


data class WorkoutGoogleFit(
    var name: String? = null,
    var identifier: String? = null,
    var appPackageName: String? = null,
    var activity: String? = null,
    var startTime: Long,
    var endTime: Long,
    var distance: Float? = null,
    var duration: Long? = null,
    var calories: Float? = null,
    var heartRate: Int? = null,
    var steps: Int? = null,
    var type: String? = null
)

data class BodyMeasurementGoogleFit(
    val userTimeStamp: Long,
    val userHeight: Int,
    val userWeight: Int,
    var gFitHeight: BodyMeasurementValue? = null,
    var gFitWeight: BodyMeasurementValue? = null,
)

data class BodyMeasurementModel(
    val height: BodyMeasurementValue? = null,
    val weight: BodyMeasurementValue? = null,
    val bodyFat: BodyMeasurementValue? = null,
)

data class BodyMeasurementValue(
    val timeStamp: Long,
    val value: Int,
)

@Parcelize
data class GoogleFitDataLastSync(
    var steps: Int = 0,
    var date: String = "",
    var stepsLastSync: Long = 0L,
) : Parcelable


//{"type":"HEART RATE","count":62,"min_count":0,"max_count":0,"unit":"bpm","time":"2021-05-17T05:31:06.718Z","resting_hr":0}
//data class HeartDataGoogleFit(
//    val time: Long,
//    val count: Float,
//    val min_count: Int,
//    val max_count: Int,
//    val resting_hr: Int
//
//
//)

