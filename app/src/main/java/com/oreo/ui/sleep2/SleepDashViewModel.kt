package com.oreo.ui.sleep2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.noisefit.luna.R
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.SleepMovementType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.PeriodCycleHistory
import com.oreo.data.model.health.SleepHourlyBreakup
import com.oreo.data.model.health.SleepMovementBreakup
import com.oreo.data.model.sleep.SleepDay
import com.oreo.ui.custom.sleep.SleepTimeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    var selectedDate: MutableLiveData<LocalDate> = MutableLiveData(LocalDate.now())

    var notifyDateChange = MutableLiveData<Event<LocalDate>>()
    var trendsData = MutableLiveData<String>()


    /**
     * HashMap (Date,DayData)
     */
    val sleepData = HashMap<LocalDate, SleepDay>()

    private val _sleepDayData = MutableLiveData<SleepDay?>()
    val sleepDayData: LiveData<SleepDay?> get() = _sleepDayData

    fun getSleepData(startDate: String, endDate: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val response =
                "[ { \"date\": \"2024-07-13\" }, { \"date\": \"2024-07-12\", \"sleep_score\": { \"value\": 92, \"text\": \"Optimal\", \"status\": \"optimal\" }, \"sleep_duration\": { \"value\": 19710, \"text\": \"Less than 7 hr\", \"status\": \"warning\" }, \"rem_sleep\": { \"value\": 2460, \"text\": \"\", \"status\": \"\" }, \"deep_sleep\": { \"value\": 5790, \"text\": \"Less than 2956.5\", \"status\": \"warning\" }, \"latency\": { \"value\": 3, \"text\": \"Less than 5 min\", \"status\": \"warning\" }, \"efficiency\": { \"value\": 94, \"text\": \"Above 85%\", \"status\": \"optimal\" }, \"restfulness\": { \"value\": 3, \"text\": \"Greater than 3 times\", \"status\": \"warning\" }, \"master_sleep_start\": \"2024-07-12 00:17:00\", \"master_sleep_end\": \"2024-07-12 06:05:00\", \"timing\": { \"value\": \"\", \"text\": \"Greater than 3am\", \"status\": \"warning\" }, \"nudges\": [ {} ], \"naps\": [ {} ], \"sleep_child\": [ { \"start_time\": \"2024-07-12 00:17:00\", \"end_time\": \"2024-07-12 06:05:00\", \"night_time_movement\": [ { \"duration\": 1800, \"end_time\": \"2024-07-12 00:47:00\", \"movement_type\": \"NO_MOVEMENT\", \"start_time\": \"2024-07-12 00:17:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 00:53:00\", \"movement_type\": \"INTENSE\", \"start_time\": \"2024-07-12 00:47:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 00:59:00\", \"movement_type\": \"LOW\", \"start_time\": \"2024-07-12 00:53:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 01:05:00\", \"movement_type\": \"NO_MOVEMENT\", \"start_time\": \"2024-07-12 00:59:00\" }, { \"duration\": 720, \"end_time\": \"2024-07-12 01:17:00\", \"movement_type\": \"INTENSE\", \"start_time\": \"2024-07-12 01:05:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 01:23:00\", \"movement_type\": \"MEDIUM\", \"start_time\": \"2024-07-12 01:17:00\" }, { \"duration\": 720, \"end_time\": \"2024-07-12 01:35:00\", \"movement_type\": \"NO_MOVEMENT\", \"start_time\": \"2024-07-12 01:23:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 01:41:00\", \"movement_type\": \"LOW\", \"start_time\": \"2024-07-12 01:35:00\" }, { \"duration\": 5040, \"end_time\": \"2024-07-12 03:05:00\", \"movement_type\": \"NO_MOVEMENT\", \"start_time\": \"2024-07-12 01:41:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 03:11:00\", \"movement_type\": \"LOW\", \"start_time\": \"2024-07-12 03:05:00\" }, { \"duration\": 6480, \"end_time\": \"2024-07-12 04:59:00\", \"movement_type\": \"NO_MOVEMENT\", \"start_time\": \"2024-07-12 03:11:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 05:05:00\", \"movement_type\": \"LOW\", \"start_time\": \"2024-07-12 04:59:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 05:11:00\", \"movement_type\": \"MEDIUM\", \"start_time\": \"2024-07-12 05:05:00\" }, { \"duration\": 1080, \"end_time\": \"2024-07-12 05:29:00\", \"movement_type\": \"NO_MOVEMENT\", \"start_time\": \"2024-07-12 05:11:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 05:35:00\", \"movement_type\": \"MEDIUM\", \"start_time\": \"2024-07-12 05:29:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 05:41:00\", \"movement_type\": \"NO_MOVEMENT\", \"start_time\": \"2024-07-12 05:35:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 05:47:00\", \"movement_type\": \"MEDIUM\", \"start_time\": \"2024-07-12 05:41:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 05:53:00\", \"movement_type\": \"NO_MOVEMENT\", \"start_time\": \"2024-07-12 05:47:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 05:59:00\", \"movement_type\": \"INTENSE\", \"start_time\": \"2024-07-12 05:53:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 06:05:00\", \"movement_type\": \"LOW\", \"start_time\": \"2024-07-12 05:59:00\" } ], \"hourly\": [ { \"duration\": 180, \"end_time\": \"2024-07-12 00:20:00\", \"sleep_type\": \"awake\", \"start_time\": \"2024-07-12 00:17:00\" }, { \"duration\": 630, \"end_time\": \"2024-07-12 00:30:30\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-12 00:20:00\" }, { \"duration\": 180, \"end_time\": \"2024-07-12 00:33:30\", \"sleep_type\": \"awake\", \"start_time\": \"2024-07-12 00:30:30\" }, { \"duration\": 1020, \"end_time\": \"2024-07-12 00:50:30\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-12 00:33:30\" }, { \"duration\": 300, \"end_time\": \"2024-07-12 00:55:30\", \"sleep_type\": \"awake\", \"start_time\": \"2024-07-12 00:50:30\" }, { \"duration\": 960, \"end_time\": \"2024-07-12 01:11:30\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-12 00:55:30\" }, { \"duration\": 480, \"end_time\": \"2024-07-12 01:19:30\", \"sleep_type\": \"awake\", \"start_time\": \"2024-07-12 01:11:30\" }, { \"duration\": 540, \"end_time\": \"2024-07-12 01:28:30\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-12 01:19:30\" }, { \"duration\": 720, \"end_time\": \"2024-07-12 01:40:30\", \"sleep_type\": \"rem\", \"start_time\": \"2024-07-12 01:28:30\" }, { \"duration\": 210, \"end_time\": \"2024-07-12 01:44:00\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-12 01:40:30\" }, { \"duration\": 3330, \"end_time\": \"2024-07-12 02:39:30\", \"sleep_type\": \"deep\", \"start_time\": \"2024-07-12 01:44:00\" }, { \"duration\": 810, \"end_time\": \"2024-07-12 02:53:00\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-12 02:39:30\" }, { \"duration\": 810, \"end_time\": \"2024-07-12 03:06:30\", \"sleep_type\": \"deep\", \"start_time\": \"2024-07-12 02:53:00\" }, { \"duration\": 660, \"end_time\": \"2024-07-12 03:17:30\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-12 03:06:30\" }, { \"duration\": 360, \"end_time\": \"2024-07-12 03:23:30\", \"sleep_type\": \"rem\", \"start_time\": \"2024-07-12 03:17:30\" }, { \"duration\": 690, \"end_time\": \"2024-07-12 03:35:00\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-12 03:23:30\" }, { \"duration\": 1560, \"end_time\": \"2024-07-12 04:01:00\", \"sleep_type\": \"deep\", \"start_time\": \"2024-07-12 03:35:00\" }, { \"duration\": 900, \"end_time\": \"2024-07-12 04:16:00\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-12 04:01:00\" }, { \"duration\": 1380, \"end_time\": \"2024-07-12 04:39:00\", \"sleep_type\": \"rem\", \"start_time\": \"2024-07-12 04:16:00\" }, { \"duration\": 4500, \"end_time\": \"2024-07-12 05:54:00\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-12 04:39:00\" }, { \"duration\": 90, \"end_time\": \"2024-07-12 05:55:30\", \"sleep_type\": \"deep\", \"start_time\": \"2024-07-12 05:54:00\" }, { \"duration\": 540, \"end_time\": \"2024-07-12 06:04:30\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-12 05:55:30\" }, { \"duration\": 30, \"end_time\": \"2024-07-12 06:05:00\", \"sleep_type\": \"awake\", \"start_time\": \"2024-07-12 06:04:30\" } ] } ] }, { \"date\": \"2024-07-11\", \"sleep_score\": { \"value\": 92, \"text\": \"Optimal\", \"status\": \"optimal\" }, \"sleep_duration\": { \"value\": 14370, \"text\": \"Less than 7 hr\", \"status\": \"warning\" }, \"rem_sleep\": { \"value\": 1950, \"text\": \"\", \"status\": \"\" }, \"deep_sleep\": { \"value\": 3150, \"text\": \"Less than 2155.5\", \"status\": \"warning\" }, \"latency\": { \"value\": 7, \"text\": \"Within 5 min to 20 min\", \"status\": \"optimal\" }, \"efficiency\": { \"value\": 96, \"text\": \"Above 85%\", \"status\": \"optimal\" }, \"restfulness\": { \"value\": 2, \"text\": \"Within 0-3 times\", \"status\": \"optimal\" }, \"master_sleep_start\": \"2024-07-11 01:37:00\", \"master_sleep_end\": \"2024-07-11 05:44:00\", \"timing\": { \"value\": \"\", \"text\": \"Greater than 3am\", \"status\": \"warning\" }, \"nudges\": [ {} ], \"naps\": [ {} ], \"sleep_child\": [ { \"start_time\": \"2024-07-11 01:37:00\", \"end_time\": \"2024-07-11 05:44:00\", \"night_time_movement\": [ { \"duration\": 360, \"end_time\": \"2024-07-11 01:43:00\", \"movement_type\": \"MEDIUM\", \"start_time\": \"2024-07-11 01:37:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-11 01:49:00\", \"movement_type\": \"LOW\", \"start_time\": \"2024-07-11 01:43:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-11 01:55:00\", \"movement_type\": \"MEDIUM\", \"start_time\": \"2024-07-11 01:49:00\" }, { \"duration\": 2880, \"end_time\": \"2024-07-11 02:43:00\", \"movement_type\": \"NO_MOVEMENT\", \"start_time\": \"2024-07-11 01:55:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-11 02:49:00\", \"movement_type\": \"INTENSE\", \"start_time\": \"2024-07-11 02:43:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-11 02:55:00\", \"movement_type\": \"NO_MOVEMENT\", \"start_time\": \"2024-07-11 02:49:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-11 03:01:00\", \"movement_type\": \"INTENSE\", \"start_time\": \"2024-07-11 02:55:00\" }, { \"duration\": 6120, \"end_time\": \"2024-07-11 04:43:00\", \"movement_type\": \"NO_MOVEMENT\", \"start_time\": \"2024-07-11 03:01:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-11 04:49:00\", \"movement_type\": \"MEDIUM\", \"start_time\": \"2024-07-11 04:43:00\" }, { \"duration\": 3240, \"end_time\": \"2024-07-11 05:43:00\", \"movement_type\": \"NO_MOVEMENT\", \"start_time\": \"2024-07-11 04:49:00\" } ], \"hourly\": [ { \"duration\": 420, \"end_time\": \"2024-07-11 01:44:00\", \"sleep_type\": \"awake\", \"start_time\": \"2024-07-11 01:37:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-11 01:50:00\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-11 01:44:00\" }, { \"duration\": 570, \"end_time\": \"2024-07-11 01:59:30\", \"sleep_type\": \"rem\", \"start_time\": \"2024-07-11 01:50:00\" }, { \"duration\": 180, \"end_time\": \"2024-07-11 02:02:30\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-11 01:59:30\" }, { \"duration\": 1830, \"end_time\": \"2024-07-11 02:33:00\", \"sleep_type\": \"deep\", \"start_time\": \"2024-07-11 02:02:30\" }, { \"duration\": 1020, \"end_time\": \"2024-07-11 02:50:00\", \"sleep_type\": \"rem\", \"start_time\": \"2024-07-11 02:33:00\" }, { \"duration\": 420, \"end_time\": \"2024-07-11 02:57:00\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-11 02:50:00\" }, { \"duration\": 360, \"end_time\": \"2024-07-11 03:03:00\", \"sleep_type\": \"rem\", \"start_time\": \"2024-07-11 02:57:00\" }, { \"duration\": 450, \"end_time\": \"2024-07-11 03:10:30\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-11 03:03:00\" }, { \"duration\": 180, \"end_time\": \"2024-07-11 03:13:30\", \"sleep_type\": \"deep\", \"start_time\": \"2024-07-11 03:10:30\" }, { \"duration\": 600, \"end_time\": \"2024-07-11 03:23:30\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-11 03:13:30\" }, { \"duration\": 1140, \"end_time\": \"2024-07-11 03:42:30\", \"sleep_type\": \"deep\", \"start_time\": \"2024-07-11 03:23:30\" }, { \"duration\": 7260, \"end_time\": \"2024-07-11 05:43:30\", \"sleep_type\": \"light\", \"start_time\": \"2024-07-11 03:42:30\" }, { \"duration\": 30, \"end_time\": \"2024-07-11 05:44:00\", \"sleep_type\": \"awake\", \"start_time\": \"2024-07-11 05:43:30\" } ] } ] } ]"

            val responseParse = Gson().fromJson<List<SleepDay>>(response)

            responseParse.forEach {
                val date = LocalDate.parse(it.date)
                sleepData[date] = it
            }

            _sleepDayData.postValue(sleepData[selectedDate.value])
        }
    }

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

            if (it.first != null) {
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
            } else {
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
        return result
    }

    fun updateSelectedDate(date: LocalDate) {
        val old = selectedDate.value

        selectedDate.value = date
        notifyDateChange.value = Event(old)
    }

    fun onWeekScrolled(date: LocalDate) {
        val todayData = LocalDate.now()

        if (date > selectedDate.value) {
            val days = abs(ChronoUnit.DAYS.between(date, selectedDate.value))
            val diff = days % 7

            val newDate = if (diff == 0L) {
                date
            } else {
                date.plusDays(7 - diff)
            }

            if (newDate > todayData) {
                updateSelectedDate(todayData)
            } else {
                updateSelectedDate(newDate)
            }
        } else {
            val days = abs(ChronoUnit.DAYS.between(date, selectedDate.value))
            val diff = days % 7

            val newDate = date.plusDays(diff)

            if (newDate > todayData) {
                updateSelectedDate(todayData)
            } else {
                updateSelectedDate(newDate)
            }

        }


        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val endDate = date.plusDays(6)
        getSleepData(date.format(format), endDate.format(format))
    }

    fun getDataForDate(selectedDate: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            val dayData = sleepData[selectedDate]
            if (dayData == null) {
                //Call API here

            } else {
                _sleepDayData.postValue(dayData)
            }
        }
    }

    fun getStatusColors(status: String): Int {
        val color: Int = if (status.equals("warning", true)) {
            R.color.oreo_contributor_warning
        } else if (status.equals("good", true)) {
            R.color.white_12_72
        } else if (status.equals("optimal", true)) {
            R.color.steps_arc
        } else {
            R.color.white_12_72
        }
        return color
    }


}