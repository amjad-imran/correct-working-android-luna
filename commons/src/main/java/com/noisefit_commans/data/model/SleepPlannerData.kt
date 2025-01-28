package com.noisefit_commans.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SleepPlannerData(
    val planner: PlannerData? = null,
    var alarms: PlannerAlarmData? = null,
    val goal: String? = null,
) : Parcelable

@Parcelize
data class PlannerData(
    val bed_time: String? = null,//24 hours format
    val wake_time: String? = null,//24 hours format
    val debt: Long? = null,//in seconds
    val duration: Long? = null,//in seconds
    val min_duration: Long? = null,//in seconds
    val nudge: String? = null,//in seconds
) : Parcelable

@Parcelize
data class PlannerAlarmData(
    var mon: AlarmTimingsData? = null,
    var tue: AlarmTimingsData? = null,
    var wed: AlarmTimingsData? = null,
    var thu: AlarmTimingsData? = null,
    var fri: AlarmTimingsData? = null,
    var sat: AlarmTimingsData? = null,
    var sun: AlarmTimingsData? = null,
) : Parcelable {

    /**
     * id as per Calendar.MONDAY
     */
    fun getNonNullAlarms(): List<Pair<Int, AlarmTimingsData>> {
        val result = ArrayList<Pair<Int, AlarmTimingsData>>()

        if (sun != null) {
            result.add(Pair(1, sun!!))
        }
        if (mon != null) {
            result.add(Pair(2, mon!!))
        }
        if (tue != null) {
            result.add(Pair(3, tue!!))
        }
        if (wed != null) {
            result.add(Pair(4, wed!!))
        }
        if (thu != null) {
            result.add(Pair(5, thu!!))
        }
        if (fri != null) {
            result.add(Pair(6, fri!!))
        }
        if (sat != null) {
            result.add(Pair(7, sat!!))
        }

        return result
    }

}

@Parcelize
data class AlarmTimingsData(
    val bed_time: String? = null,
    val wake_time: String? = null,
    val audio: Int? = null,
) : Parcelable


enum class SleepCardDashState {
    SET_ALARM,
    BREATHING_EXERCISE,
    NONE
}
