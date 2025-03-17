package com.noisefit_commans.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.ColorfitData
import kotlinx.parcelize.Parcelize


@Entity(
    tableName = "stress_data", indices = [Index(value = ["date"], unique = true)]
)
data class OreoStressDataBreakup(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "is_google_fit_sync") var isGoogleFitSynced: Boolean = false,
    @ColumnInfo(name = "break_up") @SerializedName("break_up") var breakUp: String? = null,
    @SerializedName("date") var date: String? = null
) : ColorfitData()


@Entity(
    tableName = "body_temperature", indices = [Index(value = ["date"], unique = true)]
)
data class OreoBodyTemperatureBreakup(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "is_google_fit_sync") var isGoogleFitSynced: Boolean = false,
    @ColumnInfo(name = "break_up") @SerializedName("break_up") var breakUp: String? = null,
    @SerializedName("date") var date: String? = null
) : ColorfitData()

@Entity(
    tableName = "blood_oxygen", indices = [Index(value = ["date"], unique = true)]
)
data class OreoBloodOxygenBreakup(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "is_google_fit_sync") var isGoogleFitSynced: Boolean = false,
    @ColumnInfo(name = "break_up") @SerializedName("break_up") var breakUp: String? = null,
    @SerializedName("date") var date: String? = null
) : ColorfitData()


@Entity(
    tableName = "auto_sport", indices = [Index(value = ["startTime"], unique = true)]
)
@Parcelize
data class OreoAutoSportData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "is_accepted") var isAccepted: Boolean = false,
    @ColumnInfo(name = "duration") var duration: Int = 0,
    @ColumnInfo(name = "intensity") @SerializedName("intensity") var intensity: Int? = null,
    @ColumnInfo(name = "calories") @SerializedName("calories") var calories: Int = 0,
    @ColumnInfo(name = "startTime") @SerializedName("startTime") var startTime: Long = 0,
    @ColumnInfo(name = "steps") @SerializedName("steps") var steps: Int = 0,
    @ColumnInfo(name = "type") @SerializedName("type") var type: String? = null,
    @ColumnInfo(name = "hr") @SerializedName("hr") var hrData: String? = null,
    @SerializedName("date") var date: String? = null
) : ColorfitData(), Parcelable


@Entity(
    tableName = "recorded_workout", indices = [Index(value = ["startTime"], unique = true)]
)
@Parcelize
data class RecordedWorkoutData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "is_accepted") var isAccepted: Boolean = false,
    @ColumnInfo(name = "duration") var duration: Int? = null,
    @ColumnInfo(name = "duration_seconds") var durationSeconds: Long? = null,
    @ColumnInfo(name = "intensity") @SerializedName("intensity") var intensity: Int? = null,
    @ColumnInfo(name = "calories") @SerializedName("calories") var calories: Int? = null,
    @ColumnInfo(name = "startTime") @SerializedName("startTime") var startTime: Long = 0,
    @ColumnInfo(name = "endTime") @SerializedName("endTime") var endTime: Long = 0,
    @ColumnInfo(name = "steps") @SerializedName("steps") var steps: Int? = null,
    @ColumnInfo(name = "type") @SerializedName("type") var type: Int? = null,
    @ColumnInfo(name = "hr") @SerializedName("hr") var hrData: String? = null,
    @ColumnInfo(name = "cadence") @SerializedName("cadence") var cadence: Long? = null,
    @ColumnInfo(name = "distance") @SerializedName("distance") var distance: Long? = null,
    @ColumnInfo(name = "recovery_time") @SerializedName("recovery_time") var recoveryTime: Long? = null,
    @ColumnInfo(name = "intensity_list") @SerializedName("intensity_list") var intensityList: String? = null,
    @SerializedName("date") var date: String? = null
) : ColorfitData(), Parcelable


@Entity(
    tableName = "google_fit_workout", indices = [Index(value = ["startTime"], unique = true)]
)
@Parcelize
data class GoogleFitWorkoutData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "identifier") var identifier: String? = null,
    @ColumnInfo(name = "appPackageName") var appPackageName: String? = null,
    @ColumnInfo(name = "activity") var activity: String? = null,
    @ColumnInfo(name = "startTime") var startTime: Long? = null,
    @ColumnInfo(name = "endTime") var endTime: Long? = null,
    @ColumnInfo(name = "distance") var distance: Float? = null,
    @ColumnInfo(name = "duration") var duration: Long? = null,
    @ColumnInfo(name = "calories") var calories: Float? = null,
    @ColumnInfo(name = "heartRate") var heartRate: Int? = null,
    @ColumnInfo(name = "steps") var steps: Int? = null,
    @ColumnInfo(name = "type") var type: String? = null,
) : ColorfitData(), Parcelable


@Entity(
    tableName = "google_fit_data",
    indices = [Index(value = ["endTime", "startTime", "type"], unique = true)]
)
@Parcelize
data class GoogleFitDataDb(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "type") var type: String? = null,//workout, sleep, height, weight, body_fat
    @ColumnInfo(name = "data") var data: String? = null,
    @ColumnInfo(name = "startTime") var startTime: Long,
    @ColumnInfo(name = "endTime") var endTime: Long,
) : ColorfitData(), Parcelable


@Entity(
    tableName = "day_time_movement", indices = [Index(value = ["date"], unique = true)]
)
data class DayTimeMovementBreakup(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "is_google_fit_sync") var isGoogleFitSynced: Boolean = false,
    @ColumnInfo(name = "break_up") @SerializedName("break_up") var breakUp: String? = null,
    @SerializedName("date") var date: String? = null
) : ColorfitData()


@Entity(
    tableName = "user_health_data", indices = [Index(value = ["date"], unique = true)]
)
data class UserHealthData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "userHealthData") @SerializedName("userHealthData") var userHealthData: String? = null,
    @ColumnInfo(name = "trendData") @SerializedName("trendData") var trendData: String? = null,
    @ColumnInfo(name = "impact") @SerializedName("impact") var impact: String? = null,
    @SerializedName("date") var date: String? = null
) : ColorfitData()

@Entity(tableName = "blood_pressure")
data class OreoBloodPressureData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @SerializedName("systolic_blood_pressure") var systolicBloodPressure: Int = 0,
    @SerializedName("diastolic_blood_pressure") var diastolicBloodPressure: Int = 0
) : ColorfitData()

@Entity(
    tableName = "heart_rate", indices = [Index(value = ["date"], unique = true)]
)
data class OreoHeartRate(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "is_google_fit_sync") var isGoogleFitSynced: Boolean = false,
    @ColumnInfo(name = "break_up") @SerializedName("break_up") var breakUp: String? = null,
    @SerializedName("date") var date: String? = null
) : ColorfitData()


@Entity(
    tableName = "respiratory", indices = [Index(value = ["date"], unique = true)]
)
data class OreoRespiratoryData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "is_google_fit_sync") var isGoogleFitSynced: Boolean = false,
    @ColumnInfo(name = "break_up") @SerializedName("break_up") var breakUp: String? = null,
    @SerializedName("date") var date: String? = null
) : ColorfitData()


@Entity(
    tableName = "nap_data", indices = [Index(value = ["start_time", "end_time"], unique = true)]
)
data class OreoNapData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "is_google_fit_sync") var isGoogleFitSynced: Boolean = false,
    @ColumnInfo(name = "start_time") var startTime: String? = null,
    @ColumnInfo(name = "end_time") var endTime: String? = null,
    @ColumnInfo(name = "duration") var duration: Int = 0,
    @ColumnInfo(name = "date") var date: String? = null
)

@Entity(
    tableName = "body_stress", indices = [Index(value = ["date"], unique = true)]
)
data class OreoBodyStressData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "is_google_fit_sync") var isGoogleFitSynced: Boolean = false,
    @ColumnInfo(name = "break_up") @SerializedName("break_up") var breakUp: String? = null,
    @SerializedName("date") var date: String? = null
) : ColorfitData()

@Entity(
    tableName = "sleep_data", indices = [Index(value = ["startTime", "endTime"], unique = true)]
)
data class OreoSleepData(
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
    @SerializedName("time_in_bed") var timeInBedTime: Int = 0,
    @SerializedName("sleep_latency") var sleepLatency: Int = 0,
    @SerializedName("sleep_efficiency") var sleepEfficiency: Int = 0,
    @SerializedName("total_deep") var deep: Int = 0,
    @SerializedName("total_light") var light: Int = 0,
    @SerializedName("total_sober") var sober: Int = 0,
    @SerializedName("total_awake") var awake: Int = 0,
    @SerializedName("rem_count") var remCount: Int = 0,
    @SerializedName("breath_quality") var breathQuality: Int = 0,
    @SerializedName("sleep_score") var sleepScore: Int = 0,
    @SerializedName("start_timestamp") var startTimeStamp: Long? = null,
    @SerializedName("end_timestamp") var endTimeStamp: Long? = null,
    @ColumnInfo(name = "readiness_score") @SerializedName("readiness_score") var readinessScore: Int? = 0,
    @ColumnInfo(name = "sync_date") @SerializedName("timeStamp") var timeStamp: Long? = null,
    @ColumnInfo(name = "sleep_array") @SerializedName("sleep_array") var sleepArray: ArrayList<OreoSleepDataBreakup>? = null,
    @ColumnInfo(name = "night_time_movement") @SerializedName("night_time_movement") var nightTimeMovement: ArrayList<OreoSleepMovementDataBreakup>? = null
) : ColorfitData() {

    class OreoSleepDataBreakup(
        @SerializedName("start_time") var startTime: String? = null,
        @SerializedName("end_time") var endTime: String? = null,
        @SerializedName("hour_of_the_day") var hourOfTheDay: Int? = null,
        @SerializedName("sleep_type") var sleepType: String,
        @SerializedName("date") var date: String? = null,
        @SerializedName("start_date") var startDate: String? = null,
        @SerializedName("end_date") var endDate: String? = null,
        @SerializedName("duration") var duration: Int = 0
    ) {
        override fun toString(): String {
            return "OreoSleepDataBreakup(startTime=$startTime, endTime=$endTime, hourOfTheDay=$hourOfTheDay, sleepType='$sleepType', date=$date, startDate=$startDate, endDate=$endDate, duration=$duration)"
        }
    }

    class OreoSleepMovementDataBreakup(
        @SerializedName("start_time") var startTime: String? = null,
        @SerializedName("end_time") var endTime: String? = null,
        @SerializedName("movement_type") var movementType: String,
        @SerializedName("duration") var duration: Int = 0

    ) {
        override fun toString(): String {
            return "OreoSleepMovementDataBreakup(startTime=$startTime, endTime=$endTime, movementType='$movementType', duration=$duration)"
        }
    }
}

@Entity(tableName = "google_fit")
data class OreoGoogleFitData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "steps") @SerializedName("steps") var steps: Int = 0,
    @ColumnInfo(name = "date") @SerializedName("date") var date: String = "",
    @ColumnInfo(name = "steps_last_sync") @SerializedName("steps_last_sync") var stepsLastSync: Long = 0L,
)

@Entity(tableName = "steps_data")
data class OreoStepsData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "reset_data") var resetData: Boolean = false,
    @ColumnInfo(name = "total_steps") @SerializedName("total_steps") var totalSteps: Int = 0,
    @ColumnInfo(name = "active_calories") @SerializedName("active_calories") var activeCalories: Int? = null,
    @ColumnInfo(name = "total_calories") @SerializedName("total_calories") var totalCalories: Int = 0,
    @ColumnInfo(name = "total_distance") @SerializedName("total_distance") var totalDistance: Int = 0,
    @ColumnInfo(name = "total_active_time") @SerializedName("total_active_time") var totalActiveTime: Int = 0,
    @ColumnInfo(name = "date") @SerializedName("date") var date: String? = null,
    @ColumnInfo(name = "sync_date") @SerializedName("timeStamp") var timeStamp: Long? = null,
    @ColumnInfo(name = "hour_of_the_day") @SerializedName("hour_of_the_day") var hourOfTheDay: Int? = 0
) : ColorfitData() {

    @ColumnInfo(name = "step_array")
    var stepArray: ArrayList<OreoStepDataBreakup>? = null

    class OreoStepDataBreakup(
        @SerializedName("steps") var steps: Int = 0,
        @SerializedName("active_calories") var activeCalories: Int = 0,
        @SerializedName("calories") var calories: Int = 0,
        @SerializedName("distance") var distance: Int = 0,
        @SerializedName("active_time") var activeTime: Int = 0,
        @SerializedName("hour_of_the_day") var hourOfTheDay: Int? = null
    ) : ColorfitData()
}