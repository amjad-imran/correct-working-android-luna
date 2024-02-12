package com.oreo.ui.stress

import android.graphics.Color
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.common.maxWithInvalidMovementValues
import com.noisefit_commans.common.maxWithoutInvalidMovementValues
import com.noisefit_commans.data.enums.StressType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.ScreenUtils
import com.oreo.data.dataConverter.OreoStressDataConvertor
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OStressActivitiesDataModel
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.Stress
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.ui.custom.Item
import com.oreo.ui.custom.Section
import com.oreo.ui.custom.StressCombineModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.floor
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class OStressDetailViewModel @Inject
constructor(
    val screenUtils: ScreenUtils,
    val oreoStressDataConvertor: OreoStressDataConvertor
) : BaseViewModel() {

    var lastStressValue: Int? = 0
    var isToday = false
    var defaultMeterData: Pair<Int?, Long?>? = null

    var oldDegree: Float = 0.0f
    var lastSelectedType: Int = -1
    var isSelectedMode: Boolean = false
    var dayTimeMovement: List<Int>? = null

    var date: String? = null


    fun getCombinedMovementData(
        originalList: List<Int>?,
        includeInvalid: Boolean = false
    ): List<Int> {
        if (originalList.isNullOrEmpty()) {
            return MutableList(96) { 255 }
        }
        val combinedList = ArrayList<Int>()
        for (i in originalList.indices step 3) {
            val endIndex = i + 3
            if (endIndex <= originalList.size) {
                val max = if (includeInvalid) {
                    originalList.subList(i, endIndex).maxWithInvalidMovementValues()
                } else {
                    originalList.subList(i, endIndex).maxWithoutInvalidMovementValues()
                }
                combinedList.add(max)
            }
        }
        return combinedList
    }


    /* var stressActivityData: ArrayList<OStressActivitiesDataModel>? = null
     fun prepareStressActivityData(dayData: ServerUserHealthData) {
         val workouts = dayData.activity?.workout
         val sleep = dayData.sleep
         val dataList = ArrayList<OStressActivitiesDataModel>()
         workouts?.forEach {
             dataList.add(
                 OStressActivitiesDataModel(
                     type = "Workout",
                     startTime = it.startTime,
                     endTime = it.endTime,
                     id = it.id
                 )
             )
         }
         if (sleep != null) {
             if (sleep.hourly_breakup != null)
                 dataList.add(
                     OStressActivitiesDataModel(
                         type = "Sleep",
                         startTime = sleep.hourly_breakup?.firstOrNull()?.start_time,
                         endTime = sleep.hourly_breakup?.lastOrNull()?.end_time,
                         id = ""
                     )
                 )
         }
         stressActivityData = dataList
     }*/


    fun getStressMinutes(stress: Stress?): Triple<Int, Int, Int> {
        //return Triple(32, 21, 10)
        if (stress?.breakUp.isNullOrEmpty()) return Triple(0, 0, 0)

        var calmCount = 0
        var focusedCount = 0
        var stressedCount = 0

        stress?.breakUp?.forEach { stressValue ->
            when (stressValue) {
                0 -> {}
                in 1..34 -> {
                    calmCount++
                }

                in 35..69 -> {
                    focusedCount++
                }

                else -> {
                    stressedCount++
                }
            }

        }
        return Triple(calmCount * 15, focusedCount * 15, stressedCount * 15)
    }

    fun getStressStatus(value: Int?): Pair<String, Int> {
        return when (value) {
            0 -> Pair("", R.color.white)
            in 1..34 -> Pair("Calm", R.color.stress_nap_calm)
            in 35..69 -> Pair("Focussed", R.color.stress_nap_focussed)
            in 70..100 -> Pair("Stressed", R.color.stress_nap_stressed)
            else -> Pair("", R.color.white)
        }
    }

    fun getRotationDegree(value: Int?): Float {
        if (value == null) return 0.0f
        return ((value.toFloat() / 100) * 180)
    }

    fun checkIsToday(date: String?) {
        if (date == null) {
            isToday = false
        }
        val todayDate = DateFormats.getCurrentDate(DateFormats.dateFormat3)
        isToday = todayDate.equals(date, true)
    }

    fun getStressType(value: Int?): StressType {
        if (value == null || value == 0) return StressType.NO_DATA

        return when (value) {
            0 -> StressType.NO_DATA
            in 1..34 -> StressType.CALM
            in 35..69 -> StressType.FOCUSED
            in 70..100 -> StressType.STRESSED
            else -> StressType.NO_DATA
        }

    }

    fun getTimeFromPosition(position: Int): String {
        val minutes = (96 - position) * 15
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0 //set hours to zero
        calendar[Calendar.MINUTE] = 0 // set minutes to zero
        calendar[Calendar.SECOND] = 0 //set seconds to zero

        calendar.set(Calendar.MINUTE, minutes)
        return DateFormats.convertTimestampToDate(calendar.timeInMillis, DateFormats.timeFormat12)
    }


}