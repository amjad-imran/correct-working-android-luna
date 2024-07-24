package com.oreo.ui.sleep2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.SleepMovementType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.OHMDataModel
import com.oreo.data.model.health.SleepHourlyBreakup
import com.oreo.data.model.health.SleepMovementBreakup
import com.oreo.data.model.sleep.MultiSleep
import com.oreo.data.model.sleep.SleepDay
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.ui.custom.sleep.SleepTimeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class SleepDashViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {


    var selectedDate: MutableLiveData<LocalDate> = MutableLiveData(LocalDate.now())

    var notifyDateChange = MutableLiveData<Event<LocalDate>>()
    var trendsData = MutableLiveData<String>()
    val selectedMultiSleep = MutableLiveData<MultiSleep>()


    /**
     * HashMap (Date,DayData)
     */
    val sleepData = HashMap<LocalDate, SleepDay>()

    private val _sleepDayData = MutableLiveData<SleepDay?>()
    val sleepDayData: LiveData<SleepDay?> get() = _sleepDayData

    fun getSleepData(startDate: String, endDate: String) {
        viewModelScope.launch {
            userActivityRepository.getUserHealthSleepData(
                startDate, endDate
            ).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getSleepData(startDate, endDate)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let { res ->

                            res.forEach {
                                val date = LocalDate.parse(it.date)
                                sleepData[date] = it
                            }

                            _sleepDayData.postValue(sleepData[selectedDate.value])

                            trendsData.postValue("")


                            var notifyDate = LocalDate.parse(startDate)
                            val endDateNotify = LocalDate.parse(endDate)
                            while (notifyDate < endDateNotify) {
                                notifyDateChange.value = Event(notifyDate)
                                notifyDate = notifyDate.plusDays(1)
                            }

                        }
                    }
                }
            }
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

    fun generateSleepContributorData(
        data: SleepDay?,
        showAll: Boolean = false
    ): List<OHMDataModel> {
        val listData = ArrayList<OHMDataModel>()

        if (data?.sleepScore?.value == null) {
            listData.add(OHMDataModel(SleepContributor.SLEEP_DURATION))
            listData.add(OHMDataModel(SleepContributor.REM_SLEEP))
            listData.add(OHMDataModel(SleepContributor.DEEP_SLEEP))
            listData.add(OHMDataModel(SleepContributor.EFFICIENCY))
            if (showAll) {
                listData.add(OHMDataModel(SleepContributor.LATENCY))
                listData.add(OHMDataModel(SleepContributor.RESTFULNESS))
                listData.add(OHMDataModel(SleepContributor.TIMING))
            }

            return listData
        }



        listData.add(
            OHMDataModel(
                SleepContributor.SLEEP_DURATION, valueTime = data.sleepDuration?.value,
                status = data.sleepDuration?.status, text = data.sleepDuration?.text
            )
        )

        listData.add(
            OHMDataModel(
                SleepContributor.REM_SLEEP, valueTime = data.remSleep?.value,
                status = data.remSleep?.status, text = data.remSleep?.text
            )
        )

        listData.add(
            OHMDataModel(
                SleepContributor.DEEP_SLEEP, valueTime = data.deepSleep?.value,
                status = data.deepSleep?.status, text = data.deepSleep?.text
            )
        )

        listData.add(
            OHMDataModel(
                SleepContributor.EFFICIENCY, value = "${data.efficiency?.value}", unit = "%",
                status = data.efficiency?.status, text = data.efficiency?.text
            )
        )

        if (showAll) {
            listData.add(
                OHMDataModel(
                    SleepContributor.LATENCY, valueTime = data.latency?.value,
                    status = data.latency?.status, text = data.latency?.text
                )
            )

            listData.add(
                OHMDataModel(
                    SleepContributor.RESTFULNESS,
                    value = "${data.restfulness?.value}",
                    unit = "times",
                    status = data.restfulness?.status,
                    text = data.restfulness?.text
                )
            )

            listData.add(
                OHMDataModel(
                    SleepContributor.TIMING, value = "${data.timing?.value}", unit = "",
                    status = data.timing?.status, text = data.timing?.text
                )
            )

        }
        return listData
    }

    fun getSelectedDateData(): SleepDay? {
        return sleepData[selectedDate.value]
    }

    fun generateMultiSleepData(sleepChild: List<MultiSleep>?): List<String>? {
        if (sleepChild.isNullOrEmpty()) return null

        val sleeps = ArrayList<String>()

        sleepChild.forEach {
            sleeps.add("")
        }
        return sleeps
    }


}

enum class SleepContributor(val displayName: String, val icon: Int) {
    SLEEP_DURATION("Sleep duration", R.drawable.ic_sleep_duration),
    REM_SLEEP("REM sleep", R.drawable.ic_sleep_rem),
    DEEP_SLEEP("Deep sleep", R.drawable.ic_sleep_deep),
    EFFICIENCY("Efficiency", R.drawable.ic_sleep_efficiency),
    LATENCY("Latency", R.drawable.ic_sleep_latency),
    RESTFULNESS("Restfulness", R.drawable.ic_sleep_restfulness),
    TIMING("Timing", R.drawable.ic_sleep_timing)
}