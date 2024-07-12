package com.oreo.ui.sleep2

import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.SleepMovementType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.health.SleepHourlyBreakup
import com.oreo.data.model.health.SleepMovementBreakup
import com.oreo.ui.custom.sleep.SleepTimeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min

@HiltViewModel
class SleepDashViewModel @Inject constructor() : BaseViewModel() {


    fun getHourlySleepBreakup(sleepBreakup: List<SleepHourlyBreakup>?):
            Pair<ArrayList<SleepData.SleepDataBreakup>, CountCardData> {
        val countCData = CountCardData(
            type = "Sleep",
            imageSourceId = 0,
            cardSourceId = 0,
            imageBgSourceId = 0
        )
        countCData.count = "_"
        countCData.countSubText = "sub"

        val startTime = sleepBreakup?.firstOrNull()?.start_time ?: ""
        val endTime = sleepBreakup?.lastOrNull()?.end_time ?: ""



        countCData.leftValue = startTime
        countCData.rightValue = endTime

        val sleepArray = ArrayList<SleepData.SleepDataBreakup>()
        sleepBreakup?.forEach { breakup ->
            if (breakup.duration >= 60) {
                sleepArray.add(
                    SleepData.SleepDataBreakup(
                        sleepType = ApplicationUtils.getSleepType(breakup.sleep_type).type,
                        duration = breakup.duration,
                        startTime = breakup.start_time,
                        endTime = breakup.end_time
                    )
                )
            }
        }
        return Pair(sleepArray, countCData)
    }

    fun getMovementBreakup(
        sleepBreakup: List<SleepMovementBreakup>?,
        sleepStartTime: String?,
        sleepEndTime: String?
    ): Pair<List<OreoSleepData.OreoSleepMovementDataBreakup>,
            CountCardData> {
        val countCData = CountCardData(
            type = "Movement",
            imageSourceId = 0,
            cardSourceId = 0,
            imageBgSourceId = 0
        )
        countCData.count = "_"
        countCData.countSubText = "sub"

        /*  val startTime = DateFormats.formatDate(
              sleepBreakup?.firstOrNull()?.start_time ?: "",
              DateFormats.dateTimeFormat5(),
              DateFormats.time12Meridian()
          )
          val endTime = DateFormats.formatDate(
              sleepBreakup?.lastOrNull()?.end_time ?: "",
              DateFormats.dateTimeFormat5(),
              DateFormats.time12Meridian()
          )*/

        countCData.leftValue = sleepStartTime ?: ""
        countCData.rightValue = sleepEndTime ?: ""

        val sleepArray = ArrayList<OreoSleepData.OreoSleepMovementDataBreakup>()
        sleepBreakup?.forEach { breakup ->
            /*if (breakup.duration >= 60) {*/
            sleepArray.add(
                OreoSleepData.OreoSleepMovementDataBreakup(
                    movementType = SleepMovementType.getValueFromString(breakup.movement_type).name,
                    duration = breakup.duration,
                    startTime = breakup.start_time,
                    endTime = breakup.end_time
                )
            )
            //}
        }
        return Pair(sleepArray, countCData)
    }

    fun generateSleepTimeData(): List<SleepTimeModel> {

        //Sleep start and end times
        val sleepArray = arrayListOf(
            Triple("2024-06-30 22:00:00", "2024-07-01 06:00:00", "2024-07-01"),
            Triple("2024-07-01 23:00:00", "2024-07-02 07:00:00", "2024-07-02"),
            Triple(null, null, "2024-07-03"),
            Triple("2024-07-03 22:45:00", "2024-07-04 09:00:00", "2024-07-04"),
            Triple("2024-07-04 21:30:00", "2024-07-05 06:30:00", "2024-07-05"),
            Triple("2024-07-05 22:10:00", "2024-07-06 07:30:00", "2024-07-06"),
            Triple("2024-07-06 23:20:00", "2024-07-07 08:30:00", "2024-07-07")
        )

        //Get min start time based on day start time
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        var minValue: Long? = null
        sleepArray.forEach {
            if (it.first != null) {
                val currentDay = LocalDate.parse(it.third).atStartOfDay()

                val sleepStartTime = LocalDateTime.parse(it.first, dateTimeFormatter)

                val difference = Duration.between(currentDay, sleepStartTime).toMinutes()
                val newStartTime = if (difference < 0) {
                    1440 - abs(difference)
                } else {
                    1440 + difference
                }

                if (minValue == null) {
                    minValue = newStartTime
                } else if (newStartTime < minValue!!) {
                    minValue = newStartTime
                }
            }
        }


        val result = ArrayList<SleepTimeModel>()
        sleepArray.forEach {

            if(it.first!=null){
                val currentDay = LocalDate.parse(it.third).atStartOfDay()
                val sleepStartTime = LocalDateTime.parse(it.first, dateTimeFormatter)
                val sleepEndTime = LocalDateTime.parse(it.second, dateTimeFormatter)

                val difference = Duration.between(currentDay, sleepStartTime).toMinutes()
                val newStartTime = if (difference < 0) {
                    1440 - abs(difference)
                } else {
                    1440 + difference
                }

                val sleepDifference = Duration.between(sleepEndTime, sleepStartTime).toMinutes()

                val startTime = (newStartTime - (minValue ?: 0L))
                val endTime = startTime + abs(sleepDifference)
                result.add(
                    SleepTimeModel(
                        startTime = startTime,
                        endTime = endTime,
                        startTimeText = sleepStartTime.format(DateTimeFormatter.ofPattern("hh:mm")),
                        endTimeString = sleepEndTime.format(DateTimeFormatter.ofPattern("hh:mm"))
                    )
                )
            }else{
                result.add(
                    SleepTimeModel(
                        startTime = 0,
                        endTime = 0,
                        startTimeText = "",
                        endTimeString = ""
                    )
                )
            }
        }
        LOGS.d("dfgdfgdfg $result")
        return result
    }


}