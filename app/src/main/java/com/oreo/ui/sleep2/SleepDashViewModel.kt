package com.oreo.ui.sleep2

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.SleepMovementType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.OHMDataModel
import com.oreo.data.model.health.SleepHourlyBreakup
import com.oreo.data.model.health.SleepMovementBreakup
import com.oreo.data.model.sleep.HealthTrend
import com.oreo.data.model.sleep.MultiSleep
import com.oreo.data.model.sleep.SleepDay
import com.oreo.data.model.sleep.SleepLearnMoreDataModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.ui.custom.sleep.SleepTimeModel
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    private val userActivityRepository: OreoUserActivityRepository,
    private val resourcesProvider: ResourcesProvider,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    var addSleepCtaVisibility = MutableLiveData<Boolean>()


    val FAB_ANIM_TIME = 500L
    var selectedDate: MutableLiveData<LocalDate> = MutableLiveData(LocalDate.now())
    var calendarStartDate = MutableLiveData<Event<LocalDate>>()

    var notifyDateChange = MutableLiveData<Event<LocalDate>>()
    var trendsData = MutableLiveData<SleepTrendsData>()
    val selectedMultiSleep = MutableLiveData<MultiSleep?>()

    val currentWeekDates = ArrayList<LocalDate>()

    private val _contributorInfo = MutableLiveData<OContributorResponseModal>()
    val contributorInfo: LiveData<OContributorResponseModal> = _contributorInfo

    /**
     * for multi api call handling during calendar start it
     */
    private var lastApiCallWeek: Pair<String, String>? = null

    init {
        getContributorInfo()
    }


    /**
     * HashMap (Date,DayData)
     */
    val sleepData = HashMap<LocalDate, SleepDay>()

    private val _sleepDayData = MutableLiveData<SleepDay?>()
    val sleepDayData: LiveData<SleepDay?> get() = _sleepDayData

    private var weekDataGetJob: Job? = null

    fun getSleepData(startDate: String, endDate: String) {
        setLoading(false)
        weekDataGetJob?.cancel()

        weekDataGetJob = viewModelScope.launch {

            val todayDate = LocalDate.now()
            val weekEnd = LocalDate.parse(endDate)
            var calculatedEndDate = endDate

            if (weekEnd > todayDate) {
                calculatedEndDate = todayDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }


            if (lastApiCallWeek != null && lastApiCallWeek?.first.equals(startDate) && lastApiCallWeek?.second.equals(
                    calculatedEndDate
                )
            ) {
                return@launch
            }

            userActivityRepository.getUserHealthSleepData(
                startDate, calculatedEndDate
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

                            lastApiCallWeek = Pair(startDate, calculatedEndDate)

                            setStartDate(res.registerDate ?: -1)

                            res.result?.forEach {
                                val date = LocalDate.parse(it.date)
                                sleepData[date] = it
                            }

                            _sleepDayData.postValue(sleepData[selectedDate.value])


                            currentWeekDates.clear()
                            var weekStart = LocalDate.parse(startDate)
                            currentWeekDates.add(weekStart)


                            while (weekStart < weekEnd) {
                                notifyDateChange.value = Event(weekStart)
                                weekStart = weekStart.plusDays(1)
                                currentWeekDates.add(weekStart)
                            }

                            trendsData.postValue(generateTrendsData())
                        }
                    }
                }
            }
        }
    }

    /**
     * Today condition check
     * Device paired check
     */
    fun handleAddWorkoutVisibility() {
        viewModelScope.launch(Dispatchers.IO) {
            if (sessionManager.connectedDeviceRing.value == null) {
                addSleepCtaVisibility.postValue(false)
                return@launch
            }
            if (selectedDate.value == LocalDate.now()) {
                addSleepCtaVisibility.postValue(true)
            } else {
                addSleepCtaVisibility.postValue(false)

            }
        }
    }


    fun getHealthTrendIcon(status: String?): Int {
        val drawable: Int = if (status.equals("warning", true)) {
            R.drawable.ic_health_warning
        } else if (status.equals("good", true)) {
            R.drawable.ic_health_good
        } else if (status.equals("optimal", true)) {
            R.drawable.ic_health_optimal
        } else {
            R.drawable.ic_health_good
        }
        return drawable
    }

    fun getHealthTrendState(status: String?): Int {
        val drawable: Int = if (status.equals("warning", true)) {
            2
        } else if (status.equals("good", true)) {
            1
        } else if (status.equals("optimal", true)) {
            0
        } else if (status.equals("calibrating", true)) {
            3
        } else {
            1
        }
        return drawable
    }

    fun getContributorInfo() {
        viewModelScope.launch {
            userActivityRepository.getContributorDetailsInfo(
                "sleep"
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
                                        getContributorInfo()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _contributorInfo.postValue(it)
                        }
                    }
                }
            }
        }


    }

    /**
     * OS-2386
     */
    private fun generateTrendsData(): SleepTrendsData {

        val sleepPerformance = ArrayList<Int?>()
        val sleepPerformanceIcon = ArrayList<Int>()

        val hourVsNeed = ArrayList<Pair<Int?, Int?>>()
        val hourVsNeedIcon = ArrayList<Int>()

        val restorative = ArrayList<Pair<Int?, Int?>>()
        val restorativeIcon = ArrayList<Int>()

        var selectedPosition = -1

        currentWeekDates.forEachIndexed { index, localDate ->
            val dayData = sleepData[localDate]

            if (dayData != null) {
                sleepPerformance.add(dayData.sleepPerformance)
                if (dayData.sleepPerformance == null) {
                    sleepPerformanceIcon.add(R.drawable.ic_trend_state_default)
                    hourVsNeedIcon.add(R.drawable.ic_trend_state_default)
                } else if (dayData.sleepPerformance!! > 85) {
                    sleepPerformanceIcon.add(R.drawable.ic_trend_state_green)
                    hourVsNeedIcon.add(R.drawable.ic_trend_state_green)
                } else {
                    sleepPerformanceIcon.add(R.drawable.ic_trend_state_red)
                    hourVsNeedIcon.add(R.drawable.ic_trend_state_red)
                }

                val sleepDuration: Int? =
                    ((dayData.sleepDuration?.value ?: 0) / 60).takeIf { it != 0 }
                val sleepNeeded: Int? =
                    ((dayData.sleepNeed ?: 0) / 60).takeIf { it != 0 }

                hourVsNeed.add(Pair(sleepDuration, sleepNeeded))


                val rem: Int? = ((dayData.remSleep?.value ?: 0) / 60).takeIf { it != 0 }
                val deep: Int? = ((dayData.deepSleep?.value ?: 0) / 60).takeIf { it != 0 }
                restorative.add(Pair(rem, deep))

                if ((rem == null && deep == null) || (dayData.timeInBed ?: 0) == 0) {
                    restorativeIcon.add(R.drawable.ic_trend_state_default)
                } else {

                    val resSleep = (rem ?: 0) + (deep ?: 0)
                    if (resSleep <= 180) {
                        restorativeIcon.add(R.drawable.ic_trend_state_red)
                    } else {
                        val total = dayData.timeInBed ?: 0
                        val percent = (resSleep.toFloat() / total.toFloat()) * 100
                        val isInIdealRange = percent in 40f..50f

                        if (isInIdealRange) {
                            restorativeIcon.add(R.drawable.ic_trend_state_green)
                        } else {
                            restorativeIcon.add(R.drawable.ic_trend_state_red)
                        }
                    }

                }

            } else {
                sleepPerformance.add(null)
                sleepPerformanceIcon.add(R.drawable.ic_trend_state_default)

                hourVsNeed.add(Pair(null, null))
                hourVsNeedIcon.add(R.drawable.ic_trend_state_default)

                restorative.add(Pair(null, null))
                restorativeIcon.add(R.drawable.ic_trend_state_default)
            }

            if (localDate == selectedDate.value) {
                selectedPosition = index
            }

        }

        val (sleepTime, sleepTimeIcons) = generateSleepTimeData(currentWeekDates)
        return SleepTrendsData(
            sleepPerformance = sleepPerformance,
            sleepPerformanceIcon = sleepPerformanceIcon,
            hourVsNeed = hourVsNeed,
            hourVsNeedIcon = hourVsNeedIcon,
            restorative = restorative,
            restorativeIcon = restorativeIcon,
            sleepTime = sleepTime,
            sleepTimeIcons = sleepTimeIcons,
            selectedPosition = (selectedPosition + 1)
        )
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

    private fun generateSleepTimeData(notifyDates: ArrayList<LocalDate>): Pair<List<SleepTimeModel>, List<Int>> {
        //Get min start time based on day start time
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val sleepTimeIcons = ArrayList<Int>()
        var minValue: Long? = null
        notifyDates.forEach {
            val dayData = sleepData[it]
            if (dayData?.masterSleepStart != null) {
                val currentDay = LocalDate.parse(dayData.date).atStartOfDay()

                val sleepStartTime =
                    LocalDateTime.parse(dayData.masterSleepStart, dateTimeFormatter)

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
        notifyDates.forEach {
            val dayData = sleepData[it]

            if (dayData?.masterSleepStart != null) {
                val currentDay = LocalDate.parse(dayData.date).atStartOfDay()
                val sleepStartTime =
                    LocalDateTime.parse(dayData.masterSleepStart, dateTimeFormatter)
                val sleepEndTime = LocalDateTime.parse(dayData.masterSleepEnd, dateTimeFormatter)

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

            if (dayData?.masterSleepStart == null || dayData.prev14DayBed == null || dayData.prev14DayAwake == null) {
                sleepTimeIcons.add(R.drawable.ic_trend_state_default)
            } else {
                val isInIdealRange =
                    (dayData.prev14DayAwake!! <= 60 && dayData.prev14DayBed!! <= 60)

                if (isInIdealRange) {
                    sleepTimeIcons.add(R.drawable.ic_trend_state_green)
                } else {
                    sleepTimeIcons.add(R.drawable.ic_trend_state_red)
                }
            }

        }
        return Pair(result, sleepTimeIcons)
    }

    fun updateSelectedDate(date: LocalDate) {
        val old = selectedDate.value

        selectedDate.value = date
        notifyDateChange.value = Event(old)
        trendsData.postValue(generateTrendsData())
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

    fun generateSleepContributorData(
        data: SleepDay?,
        showAll: Boolean = false
    ): List<OHMDataModel> {
        val listData = ArrayList<OHMDataModel>()

        if (data?.sleepScore?.value == null) {
            listData.add(OHMDataModel(SleepContributor.REM_SLEEP,getSleepContributorDisplayName(SleepContributor.REM_SLEEP)))
            listData.add(OHMDataModel(SleepContributor.DEEP_SLEEP,getSleepContributorDisplayName(SleepContributor.DEEP_SLEEP)))
            listData.add(OHMDataModel(SleepContributor.EFFICIENCY,getSleepContributorDisplayName(SleepContributor.EFFICIENCY)))
            if (showAll) {
                listData.add(OHMDataModel(SleepContributor.SLEEP_DURATION,getSleepContributorDisplayName(SleepContributor.SLEEP_DURATION)))
                listData.add(OHMDataModel(SleepContributor.LATENCY,getSleepContributorDisplayName(SleepContributor.LATENCY)))
                listData.add(OHMDataModel(SleepContributor.RESTFULNESS,getSleepContributorDisplayName(SleepContributor.RESTFULNESS)))
                listData.add(OHMDataModel(SleepContributor.TIMING,getSleepContributorDisplayName(SleepContributor.TIMING)))
            }
            return listData
        }


        listData.add(
            OHMDataModel(
                SleepContributor.REM_SLEEP, getSleepContributorDisplayName(SleepContributor.REM_SLEEP),valueTime = data.remSleep?.value,
                status = data.remSleep?.status, text = data.remSleep?.text
            )
        )

        listData.add(
            OHMDataModel(
                SleepContributor.DEEP_SLEEP,  getSleepContributorDisplayName(SleepContributor.DEEP_SLEEP),valueTime = data.deepSleep?.value,
                status = data.deepSleep?.status, text = data.deepSleep?.text
            )
        )
        listData.add(
            OHMDataModel(
                SleepContributor.EFFICIENCY,
                getSleepContributorDisplayName(SleepContributor.EFFICIENCY),
                value = if (data.efficiency?.value == null) null else "${data.efficiency?.value}",
                unit = "%",
                status = data.efficiency?.status,
                text = data.efficiency?.text
            )
        )

        if (showAll) {
            listData.add(
                OHMDataModel(
                    SleepContributor.SLEEP_DURATION,
                    getSleepContributorDisplayName(SleepContributor.SLEEP_DURATION),
                    valueTime = data.sleepDuration?.value,
                    status = data.sleepDuration?.status, text = data.sleepDuration?.text
                )
            )

            listData.add(
                OHMDataModel(
                    SleepContributor.LATENCY,
                    getSleepContributorDisplayName(SleepContributor.LATENCY),
                    value = if (data.latency?.value == null) null else "${data.latency?.value}",
                    unit = "min",
                    status = data.latency?.status,
                    text = data.latency?.text
                )
            )

            listData.add(
                OHMDataModel(
                    SleepContributor.RESTFULNESS,
                    getSleepContributorDisplayName(SleepContributor.RESTFULNESS),
                    value = if (data.restfulness?.value == null) null else "${data.restfulness?.value}",
                    unit = "times",
                    status = data.restfulness?.status,
                    text = data.restfulness?.text
                )
            )

            listData.add(
                OHMDataModel(
                    SleepContributor.TIMING,
                    getSleepContributorDisplayName(SleepContributor.TIMING),
                    value = if (data.timing?.value.isNullOrEmpty()) null else "${data.timing?.value}",
                    unit = "",
                    status = data.timing?.status,
                    text = data.timing?.text
                )
            )

        }
        return listData
    }

    fun getSelectedDateData(): SleepDay? {
        return sleepData[selectedDate.value]
    }

    fun generateMultiSleepData(sleepChild: List<MultiSleep>?): List<MultiSleepDisplay>? {
        if (sleepChild.isNullOrEmpty()) return null

        val sleeps = ArrayList<MultiSleepDisplay>()

        val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm")

        sleepChild.forEachIndexed { index, multiSleep ->
            val start = LocalDateTime.parse(multiSleep.start_time, dateTimeFormat)
            val end = LocalDateTime.parse(multiSleep.end_time, dateTimeFormat)

            val duration = Duration.between(start, end)

            val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(
                duration.toMinutes().toInt()
            )

            val durationText = if (hour == 0) {
                "${minute}min"
            } else {
                "${hour}hr ${minute}min"
            }

            sleeps.add(
                MultiSleepDisplay(
                    sleepStart = start.format(timeFormatter),
                    sleepTime = durationText,
                    score = multiSleep.sleep_impact ?: 0,
                    scoreImpact = multiSleep.sleep_impact ?: 0
                )
            )
        }
        return sleeps
    }

    fun updateSelectedMultiSleep(position: Int) {
        val dayData = sleepData[selectedDate.value]
        dayData?.sleepChild?.getOrNull(position).let {
            selectedMultiSleep.postValue(it)
        }
    }

    fun setStartDate(registerDate: Int) {
        if (registerDate == -1) return
        if (calendarStartDate.value?.peekContent() == null) {
            calendarStartDate.postValue(Event(LocalDate.now().minusDays(registerDate.toLong())))
        }
    }

    fun getStatusColors(status: String): Int {
        val color: Int = if (status.equals("warning", true)) {
            R.color.sleep_warning
        } else if (status.equals("good", true)) {
            R.color.sleep_good
        } else if (status.equals("optimal", true)) {
            R.color.sleep_optimal
        } else if (status.equals("fair", true)) {
            R.color.color_fair
        } else {
            R.color.white_12_72
        }
        return color
    }

    fun getScoreBackground(status: String): Int {
        val background: Int = if (status.equals("warning", true)) {
            R.drawable.score_back_pay_attention
        } else if (status.equals("good", true)) {
            R.drawable.score_back_good
        } else if (status.equals("optimal", true)) {
            R.drawable.score_back_optimal
        } else if (status.equals("fair", true)) {
            R.drawable.score_back_fair
        } else {
            0
        }
        return background
    }

    fun getLaunchState(type: SleepContributor): SleepInternalLaunchState {
        return when (type) {
            SleepContributor.SLEEP_DURATION -> SleepInternalLaunchState.SLEEP_DURATION
            SleepContributor.REM_SLEEP -> SleepInternalLaunchState.REM_SLEEP
            SleepContributor.DEEP_SLEEP -> SleepInternalLaunchState.DEEP_SLEEP
            SleepContributor.EFFICIENCY -> SleepInternalLaunchState.EFFICIENCY
            SleepContributor.LATENCY -> SleepInternalLaunchState.LATENCY
            SleepContributor.RESTFULNESS -> SleepInternalLaunchState.RESTFULNESS
            SleepContributor.TIMING -> SleepInternalLaunchState.TIMING
            SleepContributor.RESPIRATORY_RATE -> SleepInternalLaunchState.RESPIRATORY_RATE
            SleepContributor.RESTING_HEART_RATE -> SleepInternalLaunchState.RESTING_HEART_RATE
            SleepContributor.HRV -> SleepInternalLaunchState.HRV
            SleepContributor.SKIN_TEMPERATURE -> SleepInternalLaunchState.SKIN_TEMPERATURE
            SleepContributor.BLOOD_OXYGEN -> SleepInternalLaunchState.BLOOD_OXYGEN
        }
    }

    fun hasHealthData(healthTrend: HealthTrend?): Boolean {
        if (healthTrend == null) return false
        return !(healthTrend.resp?.status.isNullOrEmpty() &&
                healthTrend.rhr?.status.isNullOrEmpty() &&
                healthTrend.bloodOxy?.status.isNullOrEmpty() &&
                healthTrend.hrv?.status.isNullOrEmpty() &&
                healthTrend.skinTemp?.status.isNullOrEmpty())

    }

    fun getInfoLearnMore(): SleepLearnMoreDataModel {
        return SleepLearnMoreDataModel(
            toolbarTitle = resourcesProvider.getString(R.string.text_sleep_duration),
            title = resourcesProvider.getString(R.string.text_the_importance_of_sleep_duration),
            content = resourcesProvider.getString(R.string.text_sleep_learn_more_about),
            img = R.drawable.image_learn_duration,
            internalImg = R.drawable.image_learn_duration_int
        )
    }

    fun getSleepContributorDisplayName(contributor: SleepContributor): String {
        return when (contributor) {
            SleepContributor.SLEEP_DURATION -> resourcesProvider.getString(R.string.text_sleep_duration)
            SleepContributor.REM_SLEEP -> resourcesProvider.getString(R.string.text_rem_sleep)
            SleepContributor.DEEP_SLEEP -> resourcesProvider.getString(R.string.text_deep_sleep)
            SleepContributor.EFFICIENCY -> resourcesProvider.getString(R.string.text_efficiency)
            SleepContributor.LATENCY -> resourcesProvider.getString(R.string.text_latency)
            SleepContributor.RESTFULNESS -> resourcesProvider.getString(R.string.text_restfulness)
            SleepContributor.TIMING -> resourcesProvider.getString(R.string.text_circadian_mid_point)
            SleepContributor.RESPIRATORY_RATE -> resourcesProvider.getString(R.string.text_respiratory_rate)
            SleepContributor.RESTING_HEART_RATE -> resourcesProvider.getString(R.string.text_resting_heart_rate)
            SleepContributor.HRV -> resourcesProvider.getString(R.string.text_hrv)
            SleepContributor.SKIN_TEMPERATURE -> resourcesProvider.getString(R.string.text_skin_temperature)
            SleepContributor.BLOOD_OXYGEN -> resourcesProvider.getString(R.string.text_blood_oxygen)
        }
    }


}

enum class SleepContributor(val icon: Int) {
    SLEEP_DURATION(R.drawable.ic_sleep_duration),
    REM_SLEEP(R.drawable.ic_sleep_rem),
    DEEP_SLEEP(R.drawable.ic_sleep_deep),
    EFFICIENCY(R.drawable.ic_sleep_efficiency),
    LATENCY(R.drawable.ic_sleep_latency),
    RESTFULNESS(R.drawable.ic_sleep_restfulness),
    TIMING(R.drawable.ic_sleep_timing),
    RESPIRATORY_RATE(R.drawable.ic_respiratory_rate),
    RESTING_HEART_RATE(R.drawable.ic_resting_hr),
    HRV(R.drawable.ic_hrv),
    SKIN_TEMPERATURE(R.drawable.ic_skin_tempreature),
    BLOOD_OXYGEN(R.drawable.ic_blood_oxygen)
}

data class SleepTrendsData(
    val sleepPerformance: List<Int?>,
    val sleepPerformanceIcon: List<Int>,
    val hourVsNeed: List<Pair<Int?, Int?>>,
    val hourVsNeedIcon: List<Int>,
    val restorative: List<Pair<Int?, Int?>>,
    val restorativeIcon: List<Int>,
    val sleepTime: List<SleepTimeModel>,
    val sleepTimeIcons: List<Int>,
    val selectedPosition: Int
)