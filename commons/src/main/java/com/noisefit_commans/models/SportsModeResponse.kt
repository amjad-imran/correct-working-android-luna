package com.noisefit_commans.models

import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.constants.SportActivityName
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

private const val MILES_VALUE = 1.61f

@Parcelize
@Entity(tableName = "activities", indices = [Index(value = ["date", "endTime"], unique = true)])
class SportsModeResponse(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @Transient var isHeader: Boolean = false,
    @ColumnInfo(name = "is_synced") var isSynced: Boolean = false,
    @ColumnInfo(name = "heart_rate_available") @SerializedName("heart_rate_available") var heartRateAvailable: Int = 0,
    @ColumnInfo(name = "type") @SerializedName("type") var type: String? = null,
    @ColumnInfo(name = "activity_type") @SerializedName("activity_type") var activityType: String? = null,
    @ColumnInfo(name = "calories") @SerializedName("calories") var calories: Long? = null,
    @ColumnInfo(name = "distance") @SerializedName("distance") var distance: Long? = null,
    @ColumnInfo(name = "steps") @SerializedName("steps") var steps: Int? = null,
    @ColumnInfo(name = "duration") @SerializedName("duration") var duration: Long? = null,
    @ColumnInfo(name = "aerobic_minutes") @SerializedName("aerobic_minutes") var aerobic: Int? = null,
    @ColumnInfo(name = "anaerobic_minutes") @SerializedName("anaerobic_minutes") var anaerobic: Int? = null,
    @ColumnInfo(name = "fat_burn_minutes") @SerializedName("fat_burn_minutes") var fatBurn: Int? = null,
    @ColumnInfo(name = "extreme_min") @SerializedName("extreme_min") var extremeMin: Int? = null,
    @ColumnInfo(name = "warm_up_minutes") @SerializedName("warm_up_minutes") var warmUp: Int? = null,
    @ColumnInfo(name = "heart_rate_average") @SerializedName("heart_rate_average") var heartRateAvg: Int? = null,
    @ColumnInfo(name = "heart_rate_maximum") @SerializedName("heart_rate_maximum") var heartRateMax: Int? = null,
    @ColumnInfo(name = "heart_rate_current") @SerializedName("heart_rate_current") var heartRateCurrent: Int? = null,
    @ColumnInfo(name = "time") @SerializedName("time") var time: String? = null,
    @ColumnInfo(name = "date") @SerializedName("date") var date: String? = null,
    @ColumnInfo(name = "endTime") @SerializedName("endTime") var endTime: String? = null,
    @ColumnInfo(name = "pace") @SerializedName("pace") var pace: Float? = null,
    @ColumnInfo(name = "speed") @SerializedName("speed") var speed: Float? = null,
    @ColumnInfo(name = "cadence") @SerializedName("cadence") var cadence: Int? = null,
    @ColumnInfo(name = "heart_rate_data") @SerializedName("heart_rate_data") var heartRateData: IntArray? = null,
    @ColumnInfo(name = "calorie_data") @SerializedName("calorie_data") var calorieData: IntArray? = null,
    @ColumnInfo(name = "gps_data") @SerializedName("gps_data") var gpsData: String? = null,
    @ColumnInfo(name = "avg_step_frequency") @SerializedName("avg_step_frequency") var avgStepFrequency: Int? = 0,
    @ColumnInfo(name = "max_step_frequency") @SerializedName("max_step_frequency") var maxStepFrequency: Int? = 0,
    @ColumnInfo(name = "avg_step_stride") @SerializedName("avg_step_stride") var avgStepStride: Int? = 0,
    @ColumnInfo(name = "max_step_stride") @SerializedName("max_step_stride") var maxStepStride: Int? = 0,
    @ColumnInfo(name = "averageSWOLF") @SerializedName("averageSWOLF") var avgSWOLF: Int? = 0,
    @ColumnInfo(name = "gps_cord") @SerializedName("gps_cord") var gpsCoordinate: String? = null,
    @ColumnInfo(name = "totalStrokesNumber") @SerializedName("totalStrokesNumber") var totalStrokes: Int? = 0,
    @ColumnInfo(name = "isHrZoneInSeconds") @SerializedName("isHrZoneInSeconds") var hrZoneInSeconds: Int? = 0,

    @ColumnInfo(name = "w_temp") @SerializedName("temp") var temp: Double? = 0.0,
    @ColumnInfo(name = "uvi") @SerializedName("uvi") var uvi: Double? = 0.0,
    @ColumnInfo(name = "humidity") @SerializedName("humidity") var humidity: Double? = 0.0,
    @ColumnInfo(name = "start") @SerializedName("start") var start: String? = null,
    @ColumnInfo(name = "r_end") @SerializedName("end") var end: String? = null,

    ) : Parcelable {


    @IgnoredOnParcel
    @Ignore
    var formattedData: String? = null

    @IgnoredOnParcel
    @Ignore
    var formattedDataUnit: String? = null

    fun getActivityDuration(): String {
        if (duration == null) return ""
        val hrs = (duration!! / 3600)
        val mins = (duration!! % 3600 / 60)
        val secs = duration!! % 60

        // Output like "1:01" or "4:03:59" or "123:03:59"
        var timeFormat = " sec"
        var ret = ""
        if (hrs > 0) {
            timeFormat = " hr"
            ret += "" + hrs + ":" + if (mins < 10) "0" else ""
        }
        if (mins > 0) {
            if (hrs <= 0) {
                timeFormat = " min"
            }
            ret += "" + mins + ":" + if (secs < 10) "0" else ""
        }
        ret += "" + secs + timeFormat
        return ret
    }

    fun getActivityDurationFormat2(): String {
        if (duration == null) return ""
        val hrs = (duration!! / 3600)
        val mins = (duration!! % 3600 / 60)
        val secs = duration!! % 60

        // Output like "00:00:00"
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }

    /**
     * Output 2h 30m 10s
     */
    fun getActivityDurationFormat3(): String {
        if (duration == null) return ""
        val hrs = (duration!! / 3600)
        val mins = (duration!! % 3600 / 60)
        val secs = duration!! % 60


        if(hrs==0L){
            return String.format("%02dm %02ds", mins, secs)
        }else{
            return String.format("%02dh %02dm %02ds", hrs, mins, secs)
        }
    }


//    fun getHrZonesProgress(duration: Long?, zoneMinutes: Long?): Long {
//        if (duration == null || duration == 0L) {
//            return 0
//        }
//        if (zoneMinutes == null || zoneMinutes == 0L) {
//            return 0
//        }
//        val dur = duration /60
//        val progress = (zoneMinutes / (dur) )*100
//        println(dur)
////        ((stepsData.totalCalories.toFloat() / userGoals.caloriesGoal) * 100).roundToInt()
//        return progress
//    }

    fun getFormattedDistanceAndUnit(unit: Units): String {
        if (distance == null) return ""
        return if (unit == Units.IMPERIAL) {
            "${getTruncateDistance(distance!! / 1000f / MILES_VALUE)} Miles"
        } else {
            "${getTruncateDistance(distance!! / 1000f)} km"
        }
    }


    private fun getTruncateDistance(distance: Float): String {
        /*if(device && (device.type != Constants.DEVICES_TYPE.noise_ultra && device.type != Constants.DEVICES_TYPE.colorfit_nav_plus && device.type != Constants.DEVICES_TYPE.colorfit_pulse)){
            return distance.toFixed(2);
        }*/
        return String.format("%.2f", distance)
    }

    fun getFormattedActivityName(): String {
        val activityName = type ?: activityType ?: return ""
        val actNameTemp = activityName.replace("_", " ")
        return actNameTemp.capitalizeWords()
    }

    fun isStandingActivity(): Boolean {
        val activityName = type ?: activityType ?: return false
        val standingActivities = arrayListOf<String>(
            SportActivityName.YOGA,
            SportActivityName.SKIPPING,
            SportActivityName.ROWING_MACHINE,
            SportActivityName.ROWER,
            SportActivityName.WORKOUT,
            SportActivityName.FREE_WORKOUT,
            SportActivityName.STRENGTH_TRAINING,
            SportActivityName.WARM_UP_EXERCISE,
            SportActivityName.STRETCHING,
            SportActivityName.JUDO,
            SportActivityName.KARATE,
            SportActivityName.BOXING,
            SportActivityName.KENDO,
            SportActivityName.DANCE,
            SportActivityName.FREE_EXERCISE,
            SportActivityName.WEIGHT_TRAINING,
            SportActivityName.CURLING,
        )
        standingActivities.forEach {
            if (it.equals(activityName, true)) {
                return true
            }
        }
        return false
    }


    fun getHeartRateMaxValue(): Int {
        return heartRateMax ?: 0
    }

    fun getHeartRateAvgValue(): Int {
        return heartRateAvg ?: 0
    }


}

data class SportsModeRequestList(
    @SerializedName("activities") var activities: List<SportsModeResponse>? = null,
    @SerializedName("response_type") var responseType: String = ""
)