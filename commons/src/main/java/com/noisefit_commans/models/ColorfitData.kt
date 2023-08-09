package com.noisefit_commans.models

import android.location.Address
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.LOGS
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.math.BigInteger
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

open class ColorfitData {
    open fun serialize(): String = Gson().toJson(this)
}

//data class WatchFaces(
//    @SerializedName("watch_face_type") var watchFaceType: String,
//    @SerializedName("current_face_id") var currentFaceId: String,
//    @SerializedName("watch_faces") var watchFaces: List<WatchFace>? = null
//) : ColorfitData() {
//
//    /*data class WatchFace(
//        @SerializedName("id") var id: Int,
//        @SerializedName("face_id") var faceId: String,
//        @SerializedName("face_type") var faceType: String? = null,
//        @SerializedName("is_editable") var isEditable: Boolean = false,
//        @SerializedName("is_custom") var isCustom: Boolean = false,
//        @SerializedName("image_type") var imageType: String = "in_built",
//        @SerializedName("image_url") var imageUrl: String? = null,
//        @SerializedName("file_url") var fileUrl: String? = null,
//        @SerializedName("file_name") var zipName: String? = null,
//        @SerializedName("is_download_image") var isDownloadImage: Boolean = true,
//        @SerializedName("local_image_path") var localImagePath: String? = null,
//        @SerializedName("local_file_path") var localFilePath: String? = null
//    ) : ColorfitData()*/
//
//}


data class WatchFacesCustomHybrid(
    @SerializedName("background_color") var backgroundColor: String?,
    @SerializedName("local_image_path") var localImagePath: String?,
    @SerializedName("watch_faces_coor") var watchFacesSizeCoor: List<WatchFaceSizeCoordinate>? = null,
    @SerializedName("watch_face_data") var watchFaceData: WatchFaceDataForNavPlus? = null
) : ColorfitData() {

    data class WatchFaceSizeCoordinate(
        @SerializedName("coordinate_x") var coordinateX: Int = 0,
        @SerializedName("color") var color: String,
        @SerializedName("point_x") var pointX: Int = 0,
        @SerializedName("point_y") var pointY: Int = 0,
        @SerializedName("type_text") var typeTxt: Int = 0,
        @SerializedName("coordinate_y") var coordinateY: Int = 0
    ) : ColorfitData()

    data class WatchFaceDataForNavPlus(
        @SerializedName("source_id") var sourceId: String = "",
        @SerializedName("r") var r: Int = 0,
        @SerializedName("b") var b: Int = 0,
        @SerializedName("g") var g: Int = 0,
        @SerializedName("local_image_path") var localImagePath: String = ""
    ) : ColorfitData()

}

@Parcelize
data class SedentaryData(
    @SerializedName("status") var status: Boolean = false,
    @SerializedName("interval") var interval: Int = 0,
    @SerializedName("start_hour") var startHour: Int = 0,
    @SerializedName("start_minute") var startMinute: Int = 0,
    @SerializedName("end_hour") var endHour: Int = 0,
    @SerializedName("end_minute") var endMinute: Int = 0,
    @SerializedName("repeat_count") var repeat: Int = 0,
    @SerializedName("weeks") var weeks: IntArray? = null,
    @SerializedName("repeat_days") var repeatDays: List<Boolean>? = null,
) : ColorfitData(), Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SedentaryData

        if (status != other.status) return false
        if (interval != other.interval) return false
        if (startHour != other.startHour) return false
        if (startMinute != other.startMinute) return false
        if (endHour != other.endHour) return false
        if (endMinute != other.endMinute) return false
        if (repeat != other.repeat) return false
        if (weeks != null) {
            if (other.weeks == null) return false
            if (!weeks.contentEquals(other.weeks)) return false
        } else if (other.weeks != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + interval
        result = 31 * result + startHour
        result = 31 * result + startMinute
        result = 31 * result + endHour
        result = 31 * result + endMinute
        result = 31 * result + repeat
        result = 31 * result + (weeks?.contentHashCode() ?: 0)
        return result
    }

    fun getIntFromBoolean(list: List<Boolean>?): Int {
        val cycleBuffer = StringBuffer()
        list?.forEach { item ->

            cycleBuffer.append(
                when (item) {
                    true -> "1"
                    else -> "0"
                }
            )
        }
        return Integer.parseInt(cycleBuffer.toString(), 2)
    }
}

@Parcelize
data class Spo2Data(
    @SerializedName("status") var status: Boolean = false,
    @SerializedName("interval") var interval: Int = 0,
    @SerializedName("start_hour") var startHour: Int = 0,
    @SerializedName("start_minute") var startMinute: Int = 0,
    @SerializedName("end_hour") var endHour: Int = 0,
    @SerializedName("end_minute") var endMinute: Int = 0,
    @SerializedName("repeat_count") var repeat: Int = 0,
    @SerializedName("weeks") var weeks: IntArray? = null,
    @SerializedName("repeat_days") var repeatDays: List<Boolean>? = null,
) : ColorfitData(), Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SedentaryData

        if (status != other.status) return false
        if (interval != other.interval) return false
        if (startHour != other.startHour) return false
        if (startMinute != other.startMinute) return false
        if (endHour != other.endHour) return false
        if (endMinute != other.endMinute) return false
        if (repeat != other.repeat) return false
        if (weeks != null) {
            if (other.weeks == null) return false
            if (!weeks.contentEquals(other.weeks)) return false
        } else if (other.weeks != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + interval
        result = 31 * result + startHour
        result = 31 * result + startMinute
        result = 31 * result + endHour
        result = 31 * result + endMinute
        result = 31 * result + repeat
        result = 31 * result + (weeks?.contentHashCode() ?: 0)
        return result
    }

    fun getIntFromBoolean(list: List<Boolean>?): Int {
        LOGS.d("SADDDDDDAASDA ${list?.size}")
        val cycleBuffer = StringBuffer()
        list?.forEach { item ->

            cycleBuffer.append(
                when (item) {
                    true -> "1"
                    else -> "0"
                }
            )
        }
        return Integer.parseInt(cycleBuffer.toString(), 2)
    }
}

@Parcelize
data class WalkReminderData(
    @SerializedName("status") var status: Boolean = false,
    @SerializedName("goal_steps") var goalSteps: Int = 0,
    @SerializedName("start_hour") var startHour: Int = 0,
    @SerializedName("start_minute") var startMinute: Int = 0,
    @SerializedName("end_hour") var endHour: Int = 0,
    @SerializedName("end_minute") var endMinute: Int = 0,
    @SerializedName("repeat_count") var repeat: Int = 0,
    @SerializedName("weeks") var weeks: IntArray? = null
) : ColorfitData(), Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WalkReminderData

        if (status != other.status) return false
        if (goalSteps != other.goalSteps) return false
        if (startHour != other.startHour) return false
        if (startMinute != other.startMinute) return false
        if (endHour != other.endHour) return false
        if (endMinute != other.endMinute) return false
        if (repeat != other.repeat) return false
        if (!weeks.contentEquals(other.weeks)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + goalSteps
        result = 31 * result + startHour
        result = 31 * result + startMinute
        result = 31 * result + endHour
        result = 31 * result + endMinute
        result = 31 * result + repeat
        result = 31 * result + weeks.contentHashCode()
        return result
    }
}

data class StartDayOfWeek(@SerializedName("start_day") var startDay: String = "Sunday") :
    ColorfitData()

data class AppNotification(
    @SerializedName("app_type") var appType: String,
    @SerializedName("name") var name: String? = null,
    @SerializedName("number") var number: String? = null,
    @SerializedName("message") var message: String
) : ColorfitData()

data class SwitchSetting(
    @SerializedName("status") var status: Boolean = false,
    @SerializedName("setting_type") var settingType: String? = null,
    @SerializedName("walk_status") var walk_status: Boolean = false,
    @SerializedName("run_status") var run_status: Boolean = false,
    @SerializedName("cycle_status") var cycle_status: Boolean = false
) : ColorfitData()

data class Language(@SerializedName("language") var language: String? = null) : ColorfitData()

data class IncomingCall(
    @SerializedName("name") var name: String? = null,
    @SerializedName("number") var number: String? = null,
    @SerializedName("status") var status: Boolean
) : ColorfitData()

data class AutoSleep(
    @SerializedName("start_hour") val startHour: Int = 0,
    @SerializedName("start_minute") val startMinute: Int = 0,
    @SerializedName("end_hour") val endHour: Int = 0,
    @SerializedName("end_minute") val endMinute: Int = 0,
    @SerializedName("repeat_count") val repeat: Int = 0,
    @SerializedName("week") val weeks: IntArray,
    @SerializedName("status") var status: Boolean
) : ColorfitData() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AutoSleep

        if (startHour != other.startHour) return false
        if (startMinute != other.startMinute) return false
        if (endHour != other.endHour) return false
        if (endMinute != other.endMinute) return false
        if (repeat != other.repeat) return false
        if (!weeks.contentEquals(other.weeks)) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startHour
        result = 31 * result + startMinute
        result = 31 * result + endHour
        result = 31 * result + endMinute
        result = 31 * result + repeat
        result = 31 * result + weeks.contentHashCode()
        result = 31 * result + status.hashCode()
        return result
    }


    fun getRepeatValue(weeks: IntArray?): Int {
        val cycleBuffer = StringBuffer()
        weeks?.forEach { item ->
            cycleBuffer.append(
                when (item) {
                    1 -> "1"
                    0 -> "0"
                    else -> "0"
                }
            )
        }
        return Integer.parseInt(cycleBuffer.toString(), 2)
    }
}

data class SMS(
    @SerializedName("name") var name: String? = null,
    @SerializedName("number") var number: String? = null,
    @SerializedName("content") var content: String
) : ColorfitData()

data class CustomReplyData(@SerializedName("custom_replies") var customReplies: ArrayList<CustomReply>) :
    ColorfitData() {
    @Parcelize
    data class CustomReply(
        @SerializedName("content") var content: String? = null,
        @SerializedName("index") var index: Int = 0,
        @SerializedName("crc") var crc: Int = 0
    ) : Parcelable
}

data class BloodOxygen(
    @SerializedName("date") var date: String? = null,
    @SerializedName("blood_oxygen") var bloodOxygen: Int? = 0,
    @SerializedName("last_value") var lastValue: Int? = 0,
) : ColorfitData() {

    var bloodOxygenArray: List<BloodOxygenBreakup>? = null

    fun getAverage(): Int {

        if (bloodOxygenArray.isNullOrEmpty()) return 0
        return try {
            var sum = 0
            bloodOxygenArray!!.forEach { data ->
                sum += data.value ?: 0
            }
            sum / bloodOxygenArray!!.size
        } catch (exp: Exception) {
            0
        }
    }
}


data class BodyTemperature(
    @SerializedName("date") var date: String? = null,
    @SerializedName("body_temperature") var bodyTemperature: Float = 0f,
    @SerializedName("last_value") var lastValue: Float = 0f
) : ColorfitData() {

    var bodyTemperatureBreakup: List<BodyTemperatureBreakup>? = null

    fun getAverage(): Float {

        if (bodyTemperatureBreakup.isNullOrEmpty()) return 0f
        return try {
            var sum = 0f
            bodyTemperatureBreakup!!.forEach { data ->
                sum += data.value ?: 0f
            }
            sum / bodyTemperatureBreakup!!.size
        } catch (exp: Exception) {
            0f
        }
    }
}


data class TimeFormat(@SerializedName("time_format") var timeFormat: String?) : ColorfitData()

data class DeviceUnits(
    @SerializedName("unit_system") var unitSystem: String?
) : ColorfitData()

data class WeatherData(
    @SerializedName("country") var country: String? = "",
    @SerializedName("city") var city: String? = "",
    @SerializedName("unit") var unit: String?,
    @SerializedName("wind_speed") var windSpeed: Double,

    @SerializedName("temp") var temp: Double,
    @SerializedName("temp_min") var tempMin: Double,
    @SerializedName("temp_max") var tempMax: Double,
    @SerializedName("pressure") var pressure: Double,
    @SerializedName("humidity") var humidity: Double,
    @SerializedName("weather_type") var weatherType: String?,
    @SerializedName("weather_id") var weatherId: Int?,

    @SerializedName("dt") var dt: Long = 0,
    @SerializedName("sunrise") var sunrise: Long = 0,
    @SerializedName("sunset") var sunset: Long = 0,
    @SerializedName("uvi") var uvi: Double = 0.0
) : ColorfitData()

data class WeatherDataHourly(
    @SerializedName("country") var country: String? = "",
    @SerializedName("city") var city: String? = "",
    @SerializedName("unit") var unit: String?,
    @SerializedName("wind_speed") var windSpeed: Double,

    @SerializedName("temp") var temp: Double,
    @SerializedName("pressure") var pressure: Double,
    @SerializedName("humidity") var humidity: Double,
    @SerializedName("weather_type") var weatherType: String?,
    @SerializedName("weather_id") var weatherId: Int?,

    @SerializedName("dt") var dt: Long = 0,
    @SerializedName("sunrise") var sunrise: Long = 0,
    @SerializedName("sunset") var sunset: Long = 0,
    @SerializedName("uvi") var uvi: Double = 0.0
) : ColorfitData()

data class UserInfo(
    @SerializedName("weight") var weight: Int,
    @SerializedName("height") var height: Int,
    @SerializedName("age") var age: Int = 0,
    @SerializedName("dob") var dob: String,
    @SerializedName("gender") var gender: String,
    @SerializedName("step_length") var stepLength: Int = 0
) : ColorfitData() {

    fun getFormattedDob(): String {
        return DateFormats.formatBirthDateDisplay(dob)
    }

    fun getWeight(): String {
        return weight.toString()
    }

    fun setWeight(weight: String) {
        this.weight = if (weight.isNullOrEmpty()) 0 else weight.toInt()
    }

    fun getHeight(): String {
        return height.toString()
    }

    fun setHeight(height: String) {
        this.height = if (height.isNullOrEmpty()) 0 else height.toInt()
    }

    fun getStepLength(): String {
        return stepLength.toString()
    }

    fun setStepLength(stepLength: String) {
        this.stepLength = if (stepLength.isNullOrEmpty()) 0 else stepLength.toInt()
    }


    fun getFormattedGender(): String {
        val tempGender: String = if (gender.lowercase() == Gender.MALE.type.lowercase())
            "Man"
        else if (gender.lowercase() == Gender.FEMALE.type.lowercase())
            "Woman"
        else if (gender.lowercase() == Gender.OTHER.type.lowercase())
            "Non-binary"
        else
            "Prefer not to say"
        return tempGender.replaceFirstChar { if (it.isLowerCase()) it.titlecase(DateFormats.defaultLocale) else it.toString() }
    }

    fun getFormattedStepLength(unitSystem: String?): String {
        return when (unitSystem) {
            "metric" -> "$stepLength cm"
            "imperial" -> "${
                DistanceUtil.convertCmsToInch(stepLength)
            } inches"

            else -> "$stepLength"
        }
    }

    fun getFormattedWeight(unitSystem: String?): String {
        return when (unitSystem?.lowercase()) {
            "metric" -> "$weight Kg"
            "imperial" -> "${DistanceUtil.convertKgToLbs(weight).toDouble().roundToInt()} lbs"
            else -> "$weight"
        }
    }

    fun getFormattedHeight(unitSystem: String?): String {
        return when {
            unitSystem?.lowercase().equals("metric") -> "$height cms"
            unitSystem?.lowercase().equals("imperial") -> "${
                DistanceUtil.convertCmsToInch(height).toDouble().roundToInt()
            } inches"

            else -> "$height"
        }
    }
}

data class WristLiftGesture(
    @SerializedName("status") val status: Boolean,
    @SerializedName("start_hour") val startHour: Int = 0,
    @SerializedName("start_minute") val startMinute: Int = 0,
    @SerializedName("end_hour") val endHour: Int = 0,
    @SerializedName("end_minute") val endMinute: Int = 0,
    @SerializedName("display_duration") val displayDuration: Int = 0
) : ColorfitData()


data class Location(
    @SerializedName("lat") val lat: Double = 0.0,
    @SerializedName("lng") val lng: Double = 0.0,
    @SerializedName("address") val address: Address? = null,
    @SerializedName("addressString") val addressString: String,
) : ColorfitData()

data class UserLocation(
    @SerializedName("state_id") var stateId: Int? = null,
    @SerializedName("state") var state: String? = null,
    @SerializedName("city_id") var cityId: Int? = null,
    @SerializedName("city") var city: String? = null,
    @SerializedName("stateChanged") var stateChanged: Boolean = false,
) : ColorfitData() {


}


data class UserGoals(
    @SerializedName("step_goals") var stepGoal: Int = 0,
    @SerializedName("calories_goals") var caloriesGoal: Int = 0,
    @SerializedName("distance_goals") var distanceGoal: Int = 0,
    @SerializedName("sleep_goals") var sleepGoal: Int = 0,
    @SerializedName("standing_hr") var standingHr: Int = 12,
    @SerializedName("duration_min") var durationInMin: Int = 30,
    @SerializedName("unit_system") var unitSystem: String = "metric"
) : ColorfitData() {


    fun getUnit(): Units {
        return when {
            unitSystem.equals("metric", true) -> Units.METRIC
            unitSystem.equals("imperial", true) -> Units.IMPERIAL
            else -> Units.METRIC
        }
    }


}


data class WatchUpdateStatus(
    @SerializedName("status") val status: UpdateStatus? = null,
    @SerializedName("message") val message: String? = "",
    @SerializedName("wStatus") val wStatus: String? = "",
    @SerializedName("progress_percentage") val percentagePercentage: Int? = null
) : ColorfitData() {

    fun getProgressPercentage(): String {
        return if (percentagePercentage == null) "0%" else "$percentagePercentage%"
    }
}

enum class UpdateStatus {
    STARTED, PROGRESS, COMPLETED, ERROR, RETRY, BATTERY_LOW, BUSY
}

data class AppNotificationsSettings(
    @SerializedName("sms") val sms: Boolean = false,
    @SerializedName("call") val phone: Boolean = false
) : ColorfitData()

data class DeviceDataUpdate(@SerializedName("setter_operation_status") val setterOperationStatus: Boolean) :
    ColorfitData()

data class BatteryData(
    @SerializedName("battery_percentage") var percentage: Int? = 0,
    @SerializedName("fully_charge") var isFullyCharged: Boolean? = false,
    @SerializedName("is_charging") var isCharging: Boolean = false
) :
    ColorfitData()


data class DoNotDisturb(
    @SerializedName("status") var status: Boolean = false,
    @SerializedName("start_hour") var startHour: Int = 0,
    @SerializedName("start_minute") var startMinute: Int = 0,
    @SerializedName("end_hour") var endHour: Int = 0,
    @SerializedName("end_minute") var endMinute: Int = 0
) : ColorfitData()

data class MenstrualData(
    @SerializedName("status") var status: Boolean,
    @SerializedName("last_menstrual_date") var lastMenstrualDate: String = "",
    @SerializedName("menstrual_length") var menstrualLength: Int = 2,
    @SerializedName("menstrual_cycle_length") var menstrualCycleLength: Int = 25,
    @SerializedName("ovulation_interval") var ovulationInterval: Int = 0,
    @SerializedName("ovulation_before") var ovulationBefore: Int = 0,
    @SerializedName("ovulation_after") var ovulationAfter: Int = 0,
    @SerializedName("menstrual_reminder") var menstrualReminder: MenstrualReminder? = null
) : ColorfitData() {
    data class MenstrualReminder(
        @SerializedName("remind_start_day_before") var remindStartDayBefore: Int = 0,
        @SerializedName("remind_ovulation_day_before") var remindOvulationDayBefore: Int = 0,
        @SerializedName("reminder_time") var reminderTime: String? = null
    ) : ColorfitData()
}

data class ColorfitError(
    @SerializedName("type") var type: String,
    @SerializedName("message") var message: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("code") var code: String
) : ColorfitData()

open class AlarmsList(@SerializedName("alarms") var alarms: List<Alarm>? = null) : ColorfitData() {

    class Alarm(
        @SerializedName("id") var id: Int = 0,
        @SerializedName("alarm_type") var alarmType: String? = null,
        @SerializedName("repeat_mode") var repeatMode: String? = null,
        @SerializedName("repeat_days") var repeatDays: List<Boolean>? = null,
        @SerializedName("hour") var hour: Int = 0,
        @SerializedName("minute") var minute: Int = 0,
        @SerializedName("snooze_duration") var snoozeDuration: Int = 10,
        @SerializedName("comment") var comment: String? = null,
        @SerializedName("label") var status: Boolean = true,
        @SerializedName("alarmAction") var alarmAction: AlarmAction? = null
    ) : ColorfitData(), Serializable

    companion object {
        fun getBooleanFromInt(cycle: Int): List<Boolean> {
            val booleanList = ArrayList<Boolean>()
            val list = Integer.toBinaryString(cycle)
            list.toCharArray().forEach { char ->
                booleanList.add(
                    when (char.toString()) {
                        "1" -> true
                        else -> false
                    }
                )
            }
            return booleanList
        }

        fun getIntFromBoolean(list: List<Boolean>?): Int {
            val cycleBuffer = StringBuffer()
            list?.forEach { item ->
                cycleBuffer.append(
                    when (item) {
                        true -> "1"
                        else -> "0"
                    }
                )
            }
            return Integer.parseInt(cycleBuffer.toString(), 2)
        }

        fun binaryStrToBooleanArray(my_byte: Int): List<Boolean> {
            val result = ArrayList<Boolean>()
            result.addAll(listOf(false, false, false, false, false, false, false, false))
            val binStr = ByteArray(1)
            binStr[0] = my_byte.toByte()
            val byteStr: String = binary(binStr, 2)
            for (i in byteStr.length - 1 downTo 0) {
                result[i + 8 - byteStr.length] = Integer.valueOf(byteStr.substring(i, i + 1)) == 1
            }

            val subList = ArrayList(result.subList(1, result.size))
            subList.reverse()
            subList.add(0, result[0])
            return subList
        }

        fun binaryStrToBooleanArrayHybrid(my_byte: Int): List<Boolean> {
            val result = ArrayList<Boolean>()
            result.addAll(listOf(false, false, false, false, false, false, false))
            val binStr = ByteArray(1)
            binStr[0] = my_byte.toByte()
            val byteStr: String = binary(binStr, 2)
            for (i in byteStr.length - 1 downTo 0) {
                result[i + 7 - byteStr.length] = Integer.valueOf(byteStr.substring(i, i + 1)) == 1
            }

            result.reverse()
            return result
        }

        fun binaryStrToBooleanArrayVision(my_byte: Int): List<Boolean> {
            val result = ArrayList<Boolean>()
            result.addAll(listOf(false, false, false, false, false, false, false))
            val binStr = ByteArray(1)
            binStr[0] = my_byte.toByte()
            val byteStr: String = binary(binStr, 2)
            for (i in byteStr.length - 1 downTo 0) {
                result[i + 7 - byteStr.length] = Integer.valueOf(byteStr.substring(i, i + 1)) == 1
            }

            return result
        }

        fun booleanArrayToBinaryString(booleanList: List<Boolean>?): Int {
            var result = 0
            for (i in 7 downTo 0) {
                booleanList?.let { boolList ->
                    if (boolList[i]) {
                        result += 2.0.pow((7 - i).toDouble()).roundToInt()
                    }
                }

            }
            return result
        }

        private fun binary(bytes: ByteArray?, radix: Int): String {
            return BigInteger(1, bytes).toString(radix)
        }

    }
}

data class SportsModeRequest(
    @SerializedName("status") var status: String? = null,
    @SerializedName("calories") var calories: Int = 0,
    @SerializedName("distance") var distance: Int = 0,
    @SerializedName("duration") var duration: Int = 0,
    @SerializedName("year") var year: Int = 0,
    @SerializedName("month") var month: Int = 0,
    @SerializedName("day") var day: Int = 0,
    @SerializedName("hour") var hour: Int = 0,
    @SerializedName("minute") var minute: Int = 0,
    @SerializedName("activity_type") var activityType: String? = null
) : ColorfitData()

class WatchFaceLayout : ColorfitData() {

    @SerializedName("time_position")
    var timePosition: String? = null

    @SerializedName("time_top_content")
    var timeTopContent: String? = null

    @SerializedName("time_bottom_content")
    var timeBottomContent: String? = null

    @SerializedName("text_color")
    var textColor: Int? = null
}

data class AppTokens(
    @SerializedName("user_token") var userToken: String?,
    @SerializedName("device_external_id") var deviceId: String?
)


enum class DeviceLanguage(val type: String) { ENGLISH("english"), CHINESE("chinese"), HINDI("hindi") }
enum class UnitSystem(val type: String) { METRIC("Metric"), IMPERIAL("Imperial") }
enum class HeightUnitSystem(val type: String) { METRIC("Metric"), IMPERIAL("Imperial") }
enum class WeightUnitSystem(val type: String) { METRIC("Metric"), IMPERIAL("Imperial") }
enum class TimeFormats(val type: String) { HOURS_12("12 hours"), HOURS_24("24 hours") }
enum class Gender(val type: String) { MALE("Male"), FEMALE("Female"), OTHER("Other"), NotToSay("noToSay") }
enum class TaskEnums(val type: String) {
    WATCH_PAIR("watch_pairing"), PROFILE("profile_completion"), STEPS(
        "1st_x_steps"
    ),
    DISTANCE("1st_x_distance"), CALORIES("1st_x_calories"),
    CUSTOM_WATCHFACE("1st_custom_watch-face"), CHALLENGE_PARTICIPATION("1st_challenge_participation"),
    FRIEND_ADDED("1st_friend_added"), WORKOUT("1st_workout"), SHARE("1st_workout_share"),NPL_150("npl_150")
}

enum class RoundEndOptions(val type: String) {
    WEIGHT_LOSS(
        "Weight Loss"
    ),
    GAIN_MUSCLE("Gain Muscle"), INCREASE_PRODUCTIVITY("Increase Productivity"),
    BETTER_SLEEP("Better Sleep")
}

enum class DurationRange(val type: String) {
    PREVIOUS_WEEK("Previous Week"), MONTHLY("Monthly"), TODAY(
        "Today"
    ),
    YESTERDAY("Yesterday"), THIS_WEEK("This Week")
}

enum class SleepType(val type: String) {
    DEEP("deep"), LIGHT("light"), SOBER("sober"), AWAKE("awake"), REM(
        "rem"
    )
}

enum class SleepMovementType {
    LOW, MEDIUM, INTENSE, NO_MOVEMENT;

    companion object {
        fun getValueFromString(value: String?): SleepMovementType {
            return try {
                if (value.isNullOrEmpty()) return NO_MOVEMENT
                SleepMovementType.valueOf(value.uppercase(DateFormats.defaultLocale))
            } catch (ex: Exception) {
                NO_MOVEMENT
            }
        }
    }
}

enum class AlarmType(val type: String) {
    WAKE("wake"), SLEEP("sleep"), EXERCISE("exercise"), MEDICINE("medicine"),
    MEETING("meeting"), CUSTOM("custom")
}

@Parcelize
data class HandWashing(
    @SerializedName("start_hour") var startHour: Int = 0,
    @SerializedName("start_minute") var startMinute: Int = 0,
    @SerializedName("end_hour") var endHour: Int = 0,
    @SerializedName("end_minute") var endMinute: Int = 0,
    @SerializedName("frequency") var frequency: Int = 0,
    @SerializedName("duration") var duration: Int = 0,
    @SerializedName("start_wash") var startWash: Boolean = false
) : ColorfitData(), Parcelable

@Parcelize
data class HeartRateAlert(
    @SerializedName("status") var status: Boolean = true,
    @SerializedName("min_hr") var min_hr: Int = 0,
    @SerializedName("max_hr") var max_hr: Int = 0
) : ColorfitData(), Parcelable

@Parcelize
data class Reminder(
    @SerializedName("id") var id: Int = 0,
    @SerializedName("repeat_mode") var repeatMode: String? = null,
    @SerializedName("repeat_days") var repeatDays: List<Boolean>? = null,
    @SerializedName("hour") var hour: Int = 0,
    @SerializedName("minute") var minute: Int = 0,
    @SerializedName("label") var label: String? = null,
    @SerializedName("year") var year: Int = 0,
    @SerializedName("month") var month: Int = 0,
    @SerializedName("day") var day: Int = 0
) : ColorfitData(), Parcelable


open class ReminderList(@SerializedName("reminders") var reminders: List<Reminder>? = null) :
    ColorfitData() {

    @Parcelize
    data class Reminder(
        @SerializedName("id") var id: Int = 0,
        @SerializedName("repeat_mode") var repeatMode: String? = null,
        @SerializedName("repeat_days") var repeatDays: List<Boolean>? = null,
        @SerializedName("hour") var hour: Int = 0,
        @SerializedName("minute") var minute: Int = 0,
        @SerializedName("label") var label: String? = null,
        @SerializedName("year") var year: Int = 0,
        @SerializedName("month") var month: Int = 0,
        @SerializedName("day") var day: Int = 0
    ) : ColorfitData(), Parcelable

}

data class ManualMeasurement(
    @SerializedName("isMeasuring") var isMeasuring: Boolean = false,
    @SerializedName("isError") var isError: Boolean = false,
    @SerializedName("value") var value: Int = 0,
    @SerializedName("manualMeasureType") var manualMeasureType: ManualMeasureType,
    @SerializedName("timeStamp") var timeStamp: Long = 0
) : ColorfitData() {

}

data class StressData(
    @SerializedName("date") var date: String? = null,
    @SerializedName("value") var value: Int? = 0,
    @SerializedName("last_value") var lastValue: Int? = 0
) : ColorfitData() {
    var stressArray: List<StressDataBreakup>? = null

    fun getAverage(): Int {

        if (stressArray.isNullOrEmpty()) return 0
        return try {
            var sum = 0
            stressArray!!.forEach { data ->
                sum += data.value ?: 0
            }
            sum / stressArray!!.size
        } catch (exp: Exception) {
            0
        }
    }
}

data class EnabledAppsForNotifications(@SerializedName("enabledAppsForNotifications") val enabledAppsForNotifications: ArrayList<EnabledApp>? = null) :
    ColorfitData() {

    data class EnabledApp(
        @SerializedName("bundleName") val bundleName: String? = null,
        @SerializedName("type") val type: String
    ) : ColorfitData()

}

@Parcelize
class HeartRateInterval(
    @SerializedName("status") var status: Boolean = true,
    @SerializedName("interval") var interval: Int = 15,
    @SerializedName("start_time") var startTime: String = "10:00",
    @SerializedName("end_time") var endTime: String = "18:00",
    @SerializedName("status2") var status2: Boolean = false
) : ColorfitData(), Parcelable {

    fun getStartTime(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = DateFormats.timeFormat.parse(startTime)!!
        return calendar
    }

    fun getEndTime(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = DateFormats.timeFormat.parse(endTime)!!
        return calendar
    }
}

open class SportsModeList(@SerializedName("sports_modes") var sportsModes: List<SportsMode>? = null) :
    ColorfitData() {

    @Parcelize
    data class SportsMode(
        @SerializedName("index") var index: Int? = 0,
        @SerializedName("name") var name: String? = null,
        @SerializedName("type") var type: Int? = null,
        @SerializedName("value") var value: Boolean,
        @SerializedName("text") var text: String = "Activity not performed",
        @SerializedName("remove") var remove: Boolean = true
    ) : ColorfitData(), Parcelable {

    }

}

open class WorldClockList(@SerializedName("worldClocks") var worldClocks: ArrayList<WClock>) :
    ColorfitData() {

    @Parcelize
    data class WClock(
        @SerializedName("timeZone") var timeZone: Int = 0,
        @SerializedName("content") var content: String = ""
    ) : ColorfitData(), Parcelable
}

open class WorldClocksPushData(
    @SerializedName("worldClocks") var worldClocks: ArrayList<WorldClockList.WClock>,
    @SerializedName("localTimeZone") var localTimeZone: Int
) : ColorfitData()

open class StockInfoList(@SerializedName("stockInfoList") var stockInfoList: ArrayList<Stock>) :
    ColorfitData() {

    @Parcelize
    data class Stock(
        @SerializedName("timestamp") var timestamp: Int = 0,
        @SerializedName("halted") var halted: Int = 0,
        @SerializedName("delayMintue") var delayMintue: Int = 0,
        @SerializedName("symbol") var symbol: String = "",
        @SerializedName("market") var market: String = "",
        @SerializedName("name") var name: String = "",
        @SerializedName("latestPrice") var latestPrice: Float?,
        @SerializedName("change") var change: Float?,
        @SerializedName("changePercent") var changePercent: Float?,
        @SerializedName("previousClose") var previousClose: Float?
    ) : ColorfitData(), Parcelable
}

data class StockSymbol(
    @SerializedName("symbol") var symbol: String = "",
    @SerializedName("name") var name: String = "",
    @SerializedName("exchangeSymbol") var exchangeSymbol: String = "",
    @SerializedName("exchange") var exchange: String = "",
    @SerializedName("country") var country: String = "",
    @SerializedName("countryCode") var countryCode: String = "",
    @SerializedName("order") var order: Int = 0,
    @SerializedName("isWidget") var isWidget: Boolean
) : ColorfitData()

open class StockSymbolList(@SerializedName("stockSymbolList") var stockSymbolList: ArrayList<StockSymbol>) :
    ColorfitData()

data class HeartRateHistory(
    @SerializedName("heart_rate_list") var heartRateList: List<HeartRate>? = null,
    @SerializedName("heart_rate") var heartRate: HeartRate? = null,
    @SerializedName("last_value") var lastValue: String? = null
) : ColorfitData() {
    fun getAverageHeartRate(): Int {

        if (heartRateList.isNullOrEmpty()) return 0
        return try {
            var heartRateSum = 0
            heartRateList!!.forEachIndexed { i, e ->
                e.time?.let { time ->
                    heartRateSum += e.averageHeartRate
                }
            }
            heartRateSum / heartRateList!!.size
        } catch (exp: Exception) {
            0
        }
    }
}


class GPSDataResponse(
    @SerializedName("latitude") var latitude: Double = 0.0,
    @SerializedName("longitude") var longitude: Double = 0.0
) : ColorfitData()


//class SportsModeList(
//    @SerializedName("activities") var activities : List<SportsModeResponse>? = null,
//    @SerializedName("response_type") var responseType : String? = null) : ColorfitData() {
//    constructor(list: MutableList<SportsModeResponse>) : this() {
//
//    }
//}

class SportsModeListGPS(
    @SerializedName("activities") var activities: List<SportsModeResponse>? = null,
    @SerializedName("response_type") var responseType: String? = null,
    @SerializedName("gps_data") var gpsData: List<List<GPSDataResponse>>? = null
) : ColorfitData()

enum class Units {
    METRIC, IMPERIAL;

    companion object {
        fun getValueFromString(value: String?): Units {
            return try {
                if (value.isNullOrEmpty()) return METRIC

                valueOf(value.uppercase(DateFormats.defaultLocale))
            } catch (ex: Exception) {
                METRIC
            }
        }
    }
}

@Parcelize
data class SOSContact(
    @SerializedName("sosSwitch") var sosSwitch: Boolean = false,
    @SerializedName("contactList") var contactList: ArrayList<Contact> = ArrayList()
) : ColorfitData(), Parcelable
@Parcelize
data class Contact(
    @SerializedName("id") var id: String? = null, // contact number is id
    @SerializedName("name") var name: String? = null,
    @SerializedName("selected") var selected: Boolean = false,
    @SerializedName("photoUri") var photoUri: String? = null,
    @SerializedName("number") var number: ArrayList<String> = ArrayList()
) : ColorfitData(), Parcelable


data class WatchPassword(
    @SerializedName("password") var password: String? = null,
    @SerializedName("status") var status: Boolean = false
) : ColorfitData()

data class VibrationIntensity(
    @SerializedName("intensity") var vibrationIntensityEnum: VibrationIntensityEnum = VibrationIntensityEnum.Weak,
) : ColorfitData()

data class BloodOxygenStressData(
    @SerializedName("blood_oxygen") var bloodOxygenBreakup: BloodOxygenBreakup? = null,
    @SerializedName("stress") var stressDataBreakup: StressDataBreakup? = null
) : ColorfitData()

enum class VibrationIntensityEnum(val intensity: String) {
    Weak("Weak"),
    Medium("Medium"),
    Strong("Strong")
}


@Parcelize
data class SleepReminder(
    @SerializedName("status") var status: Boolean = false,
    @SerializedName("hour") var hour: Int=0,
    @SerializedName("minute") var minute: Int=0,
    @SerializedName("second") var second: Int=0,
    @SerializedName("millisecond") var millisecond: Int=0

) : Parcelable

