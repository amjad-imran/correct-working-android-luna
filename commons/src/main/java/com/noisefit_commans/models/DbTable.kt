package com.noisefit_commans.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Entity(
    tableName = "stress_data",
    indices = [Index(value = ["value", "date", "time"], unique = true)]
)
data class StressDataBreakup(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "resetData") var resetData: Boolean = false,
    @SerializedName("value") var value: Int? = 0,
    @ColumnInfo(name = "sync_date") @SerializedName("timeStamp") var timeStamp: Long? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("time") var time: String? = null
) : ColorfitData()

@Entity(
    tableName = "body_temperature",
    indices = [Index(value = ["value", "date", "time"], unique = true)]
)
data class BodyTemperatureBreakup(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "resetData") var resetData: Boolean = false,
    @SerializedName("value") var value: Float? = 0f,
    @SerializedName("date") var date: String? = null,
    @ColumnInfo(name = "sync_date") @SerializedName("timeStamp") var timeStamp: Long? = null,
    @SerializedName("time") var time: String? = null
) : ColorfitData()

@Entity(
    tableName = "blood_oxygen",
    indices = [Index(value = ["value", "date", "time"], unique = true)]
)
data class BloodOxygenBreakup(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "resetData") var resetData: Boolean = false,
    @SerializedName("value") var value: Int? = 0,
    @ColumnInfo(name = "sync_date") @SerializedName("timeStamp") var timeStamp: Long? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("time") var time: String? = null
) : ColorfitData()

@Entity(tableName = "blood_pressure")
data class BloodPressureData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @SerializedName("systolic_blood_pressure") var systolicBloodPressure: Int = 0,
    @SerializedName("diastolic_blood_pressure") var diastolicBloodPressure: Int = 0
) : ColorfitData()

@Entity(
    tableName = "heart_rate",
    indices = [Index(value = ["averageHeartRate", "date", "time"], unique = true)]
)
data class HeartRate(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "resetData") var resetData: Boolean = false,
    @ColumnInfo(name = "is_google_fit_sync") var isGoogleFitSynced: Boolean = false,
    @SerializedName("average_heart_rate") var averageHeartRate: Int = 0,
    @SerializedName("highest_heart_rate") var highestHeartRate: Int = 0,
    @SerializedName("lowest_heart_rate") var lowestHeartRate: Int = 0,
    @SerializedName("resting_heart_rate") var restingHeartRate: Int = 0,
    @ColumnInfo(name = "sync_date") @SerializedName("timeStamp") var timeStamp: Long? = null,
    @SerializedName("time") var time: String? = null,
    @SerializedName("date") var date: String? = null
) : ColorfitData()

@Entity(
    tableName = "sleep_data",
    indices = [Index(value = ["startTime", "endTime"], unique = true)]
)
data class SleepData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "is_google_fit_sync") var isGoogleFitSynced: Boolean = false,
    @SerializedName("start_time") var startTime: String? = null,
    @SerializedName("end_time") var endTime: String? = null,
    @SerializedName("start_date") var startDate: String? = null,
    @SerializedName("end_date") var endDate: String? = null,
    @SerializedName("available_sleep_types") var availableSleepTypes: String? = "",
    @SerializedName("date") var date: String? = null,
    @SerializedName("total_duration") var total: Int = 0,
    @SerializedName("total_deep") var deep: Int = 0,
    @SerializedName("total_light") var light: Int = 0,
    @SerializedName("total_sober") var sober: Int = 0,
    @SerializedName("total_awake") var awake: Int = 0,
    @SerializedName("rem_count") var remCount: Int = 0,
    @SerializedName("breath_quality") var breathQuality: Int = 0,
    @SerializedName("sleep_score") var sleepScore: Int = 0,
    @SerializedName("start_timestamp") var startTimeStamp: Long? = null,
    @SerializedName("end_timestamp") var endTimeStamp: Long? = null,
    @ColumnInfo(name = "sync_date") @SerializedName("timeStamp") var timeStamp: Long? = null,
    @ColumnInfo(name = "sleep_array")
    @SerializedName("sleep_array")
    var sleepArray: ArrayList<SleepDataBreakup>? = null
) : ColorfitData() {

    @Parcelize
    class SleepDataBreakup(
        @SerializedName("start_time") var startTime: String? = null,
        @SerializedName("end_time") var endTime: String? = null,
        @SerializedName("hour_of_the_day") var hourOfTheDay: Int? = null,
        @SerializedName("sleep_type") var sleepType: String,
        @SerializedName("date") var date: String? = null,
        @SerializedName("start_date") var startDate: String? = null,
        @SerializedName("end_date") var endDate: String? = null,
        @SerializedName("duration") var duration: Int = 0
    ): Parcelable
}

@Entity(tableName = "google_fit")
data class GoogleFitData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "steps") @SerializedName("steps") var steps: Int = 0,
    @ColumnInfo(name = "date") @SerializedName("date") var date: String = "",
    @ColumnInfo(name = "steps_last_sync") @SerializedName("steps_last_sync") var stepsLastSync: Long = 0L,
)

@Entity(tableName = "steps_data")
data class StepsData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "reset_data") var resetData: Boolean = false,
    @ColumnInfo(name = "total_steps") @SerializedName("total_steps") var totalSteps: Int = 0,
    @ColumnInfo(name = "total_calories") @SerializedName("total_calories") var totalCalories: Int = 0,
    @ColumnInfo(name = "total_distance") @SerializedName("total_distance") var totalDistance: Int = 0,
    @ColumnInfo(name = "total_active_time") @SerializedName("total_active_time") var totalActiveTime: Int = 0,
    @ColumnInfo(name = "date") @SerializedName("date") var date: String? = null,
    @ColumnInfo(name = "sync_date") @SerializedName("timeStamp") var timeStamp: Long? = null,
    @ColumnInfo(name = "hour_of_the_day") @SerializedName("hour_of_the_day") var hourOfTheDay: Int? = 0
) : ColorfitData() {

    @ColumnInfo(name = "step_array")
    var stepArray: ArrayList<StepDataBreakup>? = null

    class StepDataBreakup(
        @SerializedName("steps") var steps: Int = 0,
        @SerializedName("calories") var calories: Int = 0,
        @SerializedName("distance") var distance: Int = 0,
        @SerializedName("active_time") var activeTime: Int = 0,
        @SerializedName("hour_of_the_day") var hourOfTheDay: Int? = null
    ) : ColorfitData()
}