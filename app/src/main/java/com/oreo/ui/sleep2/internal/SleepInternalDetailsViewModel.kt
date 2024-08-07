package com.oreo.ui.sleep2.internal

import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.TrendAverage
import com.oreo.data.model.TrendDailyData
import com.oreo.data.model.TrendsGraphData
import com.oreo.data.model.TrendsValues
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class SleepInternalDetailsViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {

    var isDeviationSelected = true

    lateinit var selectedLaunchMode: SleepInternalLaunchState
    val trendsData = HashMap<LocalDate, TrendsValues>()

    val reloadFragment = MutableLiveData<Event<SleepInternalLaunchState>>()

    var startDate: String = ""
    var endDate: String = ""


    var selectedDate: LocalDate = LocalDate.now()
    private var selectedPosition = 0

    private val _selectedPeriod = MutableLiveData<InternalSelectedPeriod>()
    val selectedPeriod: LiveData<InternalSelectedPeriod> = _selectedPeriod


    var dayAvg: TrendAverage? = null
    var weekAvg: TrendAverage? = null
    var monthAvg: TrendAverage? = null

    val fragments = MutableLiveData<List<Fragment>?>()
    var currentStartDate: LocalDate? = null
    val lastLoadedDataDate: LocalDate? = null


    fun setSelectedPeriod(selectedPeriod: InternalSelectedPeriod) {
        fragments.value = null
        _selectedPeriod.value = selectedPeriod
    }

    private val _titleUpdate = MutableLiveData<Pair<String, Int>>()
    val titleUpdate: LiveData<Pair<String, Int>> = _titleUpdate

    init {
        val datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        endDate = LocalDate.now().format(datePattern)
        startDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
            .format(datePattern)
    }


    fun getTrendsDailyData(sDate: LocalDate, eDate: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            userActivityRepository.getDailyTrendsData(
                sDate.toString(), eDate.toString(), selectedLaunchMode.key.lowercase()
            )
                .collect { resource ->
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
                                            getTrendsInternalDetailsData()
                                        }

                                        override fun no() {

                                        }
                                    }
                            })
                        }

                        is Resource.Success -> {
                            resource.data?.data?.let {

                                var oldData = fragments.value

                                if (oldData == null) {
                                    oldData = ArrayList()
                                }

                                val dataToDisplay = ArrayList<TrendDailyData>()
                                it.data?.forEach {
                                    dataToDisplay.add(
                                        it
                                    )

                                    val trendData = TrendsGraphData(
                                        dataType2 = dataToDisplay
                                    )

                                    (oldData as ArrayList).add(
                                        0, SleepSingleLineGradientChartFragment.newInstance(
                                            trendData
                                        )
                                    )
                                }

                                fragments.postValue(oldData)


                                /*it.data?.forEach {
                                    trendsData[LocalDate.parse(it.date)] = it
                                }

                                loadAllFragments()*/


                            }
                        }
                    }
                }
        }

    }

    fun getTrendsInternalDetailsData() {
        viewModelScope.launch(Dispatchers.IO) {
            if (isHealthMonitorTrend()) {
                userActivityRepository.getSleepHealthMonitorTrendsPagesData(
                    startDate, endDate, selectedLaunchMode.key.lowercase()
                )
            } else {
                userActivityRepository.getSleepInternalTrendsPagesData(
                    startDate, endDate, selectedLaunchMode.key.lowercase()
                )
            }
                .collect { resource ->
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
                                            getTrendsInternalDetailsData()
                                        }

                                        override fun no() {

                                        }
                                    }
                            })
                        }

                        is Resource.Success -> {
                            resource.data?.data?.let {

                                it.data?.forEach {
                                    trendsData[LocalDate.parse(it.date)] = it
                                }

                                loadAllFragments()

                                if (dayAvg == null) {
                                    dayAvg = it.dayAvg
                                }
                                if (weekAvg == null) {
                                    weekAvg = it.weekAvg
                                }
                                if (monthAvg == null) {
                                    monthAvg = it.monthAvg
                                }
                            }
                        }
                    }
                }
        }
    }


    fun isHealthMonitorTrend(): Boolean {
        val healthTrends = arrayListOf(
            SleepInternalLaunchState.RESPIRATORY_RATE,
            SleepInternalLaunchState.BLOOD_OXYGEN,
            SleepInternalLaunchState.HRV,
            SleepInternalLaunchState.RESTING_HEART_RATE,
            SleepInternalLaunchState.SKIN_TEMPERATURE
        )
        return selectedLaunchMode in healthTrends
    }

    /**
     * @return Pair of start date and end date
     */
    private fun getDatesToLoad(): Pair<LocalDate, LocalDate> {

        val (start, end) = when (selectedPeriod.value) {
            InternalSelectedPeriod.DAY, null -> {
                return if (currentStartDate == null) {
                    Pair(
                        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                        LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                    )
                } else {
                    Pair(
                        currentStartDate!!.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)),
                        currentStartDate!!.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY))
                    )
                }
            }

            InternalSelectedPeriod.WEEK -> {
                return if (currentStartDate == null) {
                    Pair(
                        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            .minusWeeks(5),
                        LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                    )
                } else {
                    Pair(
                        currentStartDate!!.with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
                            .minusWeeks(5),
                        currentStartDate!!.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY))
                    )
                }
            }

            InternalSelectedPeriod.MONTH -> {
                if (currentStartDate == null) {
                    val start =
                        LocalDate.now().minusMonths(5).with(TemporalAdjusters.firstDayOfMonth())
                    val end = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())
                    Pair(start, end)
                } else {
                    val start =
                        currentStartDate!!.minusMonths(5).with(TemporalAdjusters.firstDayOfMonth())
                    val end = currentStartDate!!.with(TemporalAdjusters.lastDayOfMonth())
                    Pair(start, end)
                }
            }

            InternalSelectedPeriod.DAILY -> {
                if (currentStartDate == null) {
                    currentStartDate = LocalDate.now()
                    Pair(currentStartDate, currentStartDate)
                } else {
                    currentStartDate = currentStartDate!!.minusDays(1)
                    Pair(currentStartDate, currentStartDate)
                }
            }
        }

        return Pair(start, end)
    }

    private fun loadTempDeviationFrag() {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.now()

        var current = start
        val dataToDisplay = ArrayList<TrendsValues>()
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        while (current <= end) {
            val data = trendsData[current]
            dataToDisplay.add(
                TrendsValues(
                    date = current.format(dateFormat),
                    value1 = data?.value1,
                    value2 = data?.value2
                )
            )
            current = current.plusDays(1)
        }

        val trendData = TrendsGraphData(
            data = dataToDisplay
        )
        fragments.postValue(
            arrayListOf(
                SleepTempDeviationChartFragment.newInstance(
                    trendData
                )
            )
        )

    }

    private fun loadAllFragments() {

        if (isDeviationSelected && selectedLaunchMode == SleepInternalLaunchState.SKIN_TEMPERATURE) {
            loadTempDeviationFrag()
            return
        }

        val initDates = getDatesToLoad()
        var start = initDates.first
        var end = initDates.second

        val userStartDate = LocalDate.parse(startDate)
        val fragmentsToShow = ArrayList<Fragment>()
        var positionCount = 0
        while (end >= userStartDate) {
            currentStartDate = start
            val dataToDisplay = ArrayList<TrendsValues>()

            var current = start
            val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            while (current <= end) {
                val data = trendsData[current]
                dataToDisplay.add(
                    TrendsValues(
                        date = current.format(dateFormat),
                        value1 = data?.value1,
                        value2 = data?.value2
                    )
                )
                if (current == selectedDate) {
                    selectedPosition = positionCount
                }
                current = current.plusDays(1)
            }

            val trendData = TrendsGraphData(
                data = dataToDisplay
            )

            getFragmentToAdd(trendData).let {
                fragmentsToShow.add(it)
            }

            val initDatesNew = getDatesToLoad()
            start = initDatesNew.first
            end = initDatesNew.second
            positionCount++
        }

        fragments.postValue(fragmentsToShow)
    }


    private fun getFragmentToAdd(trendData: TrendsGraphData): Fragment {
        trendData.contributorType = selectedLaunchMode

        when (selectedPeriod.value) {
            InternalSelectedPeriod.DAY, null -> {
                return when (selectedLaunchMode) {
                    SleepInternalLaunchState.REM_SLEEP, SleepInternalLaunchState.DEEP_SLEEP,
                    SleepInternalLaunchState.RESPIRATORY_RATE, SleepInternalLaunchState.BLOOD_OXYGEN,
                    SleepInternalLaunchState.LATENCY,
                    SleepInternalLaunchState.RESTFULNESS,
                    SleepInternalLaunchState.SLEEP_PERFORMANCE -> SleepBarChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.SLEEP_DURATION,
                    SleepInternalLaunchState.HRV,
                    SleepInternalLaunchState.RESTING_HEART_RATE,
                    SleepInternalLaunchState.SKIN_TEMPERATURE,
                    SleepInternalLaunchState.EFFICIENCY -> SleepSingleLineGradientChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.RESTORATIVE_SLEEP -> SleepMultiBarChartFragment.newInstance(
                        trendData
                    )

                    SleepInternalLaunchState.HOUR_VS_NEED -> SleepMultiLineChartFragment.newInstance(
                        trendData
                    )


                    //pending from product
                    SleepInternalLaunchState.SLEEP_TIME -> SleepSingleLineChartFragment.newInstance(
                        trendData
                    )

                    else -> SleepBarChartFragment.newInstance(trendData)
                }
            }

            InternalSelectedPeriod.WEEK -> {
                return when (selectedLaunchMode) {
                    SleepInternalLaunchState.HOUR_VS_NEED, SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                        SleepMultiLineChart2Fragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.WEEK
                        })
                    }

                    SleepInternalLaunchState.SLEEP_TIME -> {
                        SleepSleepTImeChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.WEEK
                        })
                    }

                    SleepInternalLaunchState.RESPIRATORY_RATE, SleepInternalLaunchState.RESTING_HEART_RATE,
                    SleepInternalLaunchState.BLOOD_OXYGEN,
                    SleepInternalLaunchState.SKIN_TEMPERATURE,
                    SleepInternalLaunchState.HRV -> {
                        SleepSingleLineChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.WEEK
                        })
                    }

                    else -> {
                        SleepSingleLineChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.WEEK
                        })
                    }
                }
            }

            InternalSelectedPeriod.MONTH -> {
                return when (selectedLaunchMode) {
                    SleepInternalLaunchState.HOUR_VS_NEED, SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                        SleepMultiLineChart2Fragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.MONTH
                        })
                    }

                    SleepInternalLaunchState.SLEEP_TIME -> {
                        SleepSleepTImeChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.MONTH
                        })
                    }

                    SleepInternalLaunchState.RESPIRATORY_RATE, SleepInternalLaunchState.RESTING_HEART_RATE,
                    SleepInternalLaunchState.BLOOD_OXYGEN,
                    SleepInternalLaunchState.SKIN_TEMPERATURE,
                    SleepInternalLaunchState.HRV -> {
                        SleepSingleLineChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.MONTH
                        })
                    }

                    else -> {
                        SleepSingleLineChartFragment.newInstance(trendData.apply {
                            this.selectedPeriod = InternalSelectedPeriod.MONTH
                        })
                    }
                }
            }

            InternalSelectedPeriod.DAILY -> {
                return SleepSingleLineGradientChartFragment.newInstance(
                    trendData
                )
            }
        }
    }

    /*
            * 0-up
            * 1-warning
            * 2-red alert
            * */
    fun getHighlightBackType(type: Int): Pair<Int, Int> {
        val background: Int
        val textColor: Int
        when (type) {
            0 -> {
                background = R.drawable.back_hm_optimal
                textColor = Color.parseColor("#29cc74")
            }

            1 -> {
                background = R.drawable.back_hm_fair
                textColor = Color.parseColor("#ffffff")
            }

            else -> {
                background = R.drawable.back_hm_warning
                textColor = Color.parseColor("#ff7c94")
            }
        }
        return Pair(background, textColor)
    }

    fun returnTrendsArrow(type: SleepInternalLaunchState): Int {
        val icon: Int = when (type) {
            SleepInternalLaunchState.SLEEP_PERFORMANCE, SleepInternalLaunchState.HOUR_VS_NEED, SleepInternalLaunchState.RESTORATIVE_SLEEP -> R.drawable.ic_trend_up
            SleepInternalLaunchState.EFFICIENCY, SleepInternalLaunchState.RESTFULNESS, SleepInternalLaunchState.LATENCY, SleepInternalLaunchState.SLEEP_DURATION -> R.drawable.ic_hm_tick
            else -> 0
        }
        return icon

    }

    private fun getTitle(): Pair<String, Int> {
        return when (selectedLaunchMode) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                Pair(
                    resourcesProvider.getString(R.string.text_restorative_sleep),
                    R.drawable.ic_sleep_restroactive_sp
                )
            }

            SleepInternalLaunchState.SLEEP_TIME -> Pair(
                resourcesProvider.getString(R.string.text_sleep_time), R.drawable.ic_sleep_time
            )

            SleepInternalLaunchState.HOUR_VS_NEED -> Pair(
                resourcesProvider.getString(R.string.text_hour_vs_need), R.drawable.ic_sleep_snooz
            )

            SleepInternalLaunchState.SLEEP_PERFORMANCE -> Pair(
                resourcesProvider.getString(R.string.text_sleep_performance),
                R.drawable.ic_sleep_performance
            )

            SleepInternalLaunchState.EFFICIENCY -> Pair(
                resourcesProvider.getString(R.string.text_efficiency),
                R.drawable.ic_sleep_efficiency
            )

            SleepInternalLaunchState.REM_SLEEP -> Pair(
                resourcesProvider.getString(R.string.text_rem_sleep), R.drawable.ic_sleep_rem_sp
            )

            SleepInternalLaunchState.DEEP_SLEEP -> Pair(
                resourcesProvider.getString(R.string.text_deep_sleep), R.drawable.ic_sleep_deep_sp
            )

            SleepInternalLaunchState.LATENCY -> Pair(
                resourcesProvider.getString(R.string.text_latency), R.drawable.ic_sleep_latency
            )

            SleepInternalLaunchState.RESTFULNESS -> Pair(
                resourcesProvider.getString(R.string.text_restfulness),
                R.drawable.ic_sleep_restfulness
            )

            SleepInternalLaunchState.SLEEP_DURATION -> Pair(
                resourcesProvider.getString(R.string.text_sleep_duration),
                R.drawable.ic_clock_off_sleep
            )

            SleepInternalLaunchState.TIMING -> Pair(
                resourcesProvider.getString(R.string.text_timing), R.drawable.ic_clock_off_sleep
            )

            SleepInternalLaunchState.RESPIRATORY_RATE -> {
                Pair(
                    resourcesProvider.getString(R.string.text_respiratory_rate),
                    R.drawable.ic_respiratory_rate
                )
            }

            SleepInternalLaunchState.RESTING_HEART_RATE -> Pair(
                resourcesProvider.getString(R.string.text_resting_heart_rate),
                R.drawable.ic_resting_hr
            )

            SleepInternalLaunchState.HRV -> Pair(
                resourcesProvider.getString(R.string.text_hrv),
                R.drawable.ic_hrv
            )

            SleepInternalLaunchState.SKIN_TEMPERATURE -> Pair(
                resourcesProvider.getString(R.string.text_skin_temperature),
                R.drawable.ic_skin_tempreature
            )

            SleepInternalLaunchState.BLOOD_OXYGEN -> Pair(
                resourcesProvider.getString(R.string.text_blood_oxygen),
                R.drawable.ic_blood_oxygen
            )
        }
    }

    fun updateTitle() {
        _titleUpdate.postValue(getTitle())
    }

    fun getLearnMoreData(): ArrayList<LearnMoreDataModel> {
        val dataList = ArrayList<LearnMoreDataModel>()
        dataList.add(
            LearnMoreDataModel(
                title = "General heart rate terms",
                msg = "2 min read",
                img = R.drawable.img_hr_article_1,
                type = 1
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = "Normal heart rate for my age",
                msg = "2 min read",
                img = R.drawable.img_hr_article_2,
                type = 2
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = "What are heart rate zones ?",
                msg = "2 min read",
                img = R.drawable.img_hr_article_3,
                type = 3
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = "Heart rate during sleep",
                msg = "2 min read",
                img = R.drawable.img_hr_article_4,
                type = 4
            )
        )
        return dataList
    }

    fun reloadData() {
        currentStartDate = null
        fragments.value = null

        if(trendsData.isEmpty()){
            getTrendsInternalDetailsData()
        }else{
            loadAllFragments()
        }
    }

    fun getUnit(): String {
        return when (selectedLaunchMode) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> ""
            SleepInternalLaunchState.SLEEP_PERFORMANCE -> "%"
            SleepInternalLaunchState.HOUR_VS_NEED -> ""
            SleepInternalLaunchState.SLEEP_TIME -> ""
            SleepInternalLaunchState.TIMING -> "min"
            SleepInternalLaunchState.EFFICIENCY -> "%"
            SleepInternalLaunchState.REM_SLEEP -> "min"
            SleepInternalLaunchState.DEEP_SLEEP -> "min"
            SleepInternalLaunchState.SLEEP_DURATION -> ""
            SleepInternalLaunchState.LATENCY -> "min"
            SleepInternalLaunchState.RESTFULNESS -> "times"
            SleepInternalLaunchState.RESPIRATORY_RATE -> "rpm"
            SleepInternalLaunchState.RESTING_HEART_RATE -> "bpm"
            SleepInternalLaunchState.HRV -> "ms"
            SleepInternalLaunchState.SKIN_TEMPERATURE -> "°F"
            SleepInternalLaunchState.BLOOD_OXYGEN -> "%"
        }
    }

    fun getTopState(): TrendsTopState {
        return when (selectedLaunchMode) {
            SleepInternalLaunchState.SLEEP_DURATION, SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                TrendsTopState.SINGLE_DATE
            }

            SleepInternalLaunchState.HOUR_VS_NEED -> {
                TrendsTopState.DOUBLE_DATE
            }

            else -> {
                TrendsTopState.SINGLE
            }
        }
    }

    fun getTopDisplayDate(): String {
        val todayDate = LocalDate.now()
        return when (selectedPeriod.value) {
            InternalSelectedPeriod.DAY, null -> {
                val dayFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy")
                todayDate.format(dayFormat)
            }

            InternalSelectedPeriod.WEEK -> {
                val weekFormatStart = DateTimeFormatter.ofPattern("dd MMMM")
                val weekFormatEnd = DateTimeFormatter.ofPattern("dd MMMM, yyyy")
                val weekStart = todayDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val weekEnd = todayDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

                "${weekStart.format(weekFormatStart)} - ${weekEnd.format(weekFormatEnd)}"
            }

            InternalSelectedPeriod.MONTH -> {
                val dayFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
                todayDate.format(dayFormat)
            }

            InternalSelectedPeriod.DAILY -> {
                val dayFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy")
                todayDate.format(dayFormat)
            }
        }

    }

    fun getSelectedPosition(): Int {
        return if (selectedPeriod.value == InternalSelectedPeriod.DAILY) {
            fragments.value?.size ?: 0
        } else {
            selectedPosition
        }
    }

    fun loadDailyPrevDayData() {
        val userStartDate = LocalDate.parse(startDate)
        val prevDate = selectedDate.minusDays(1)
        if (prevDate < userStartDate) {
            return
        }

        getTrendsDailyData(prevDate, prevDate)

        //Call API with prevDate
    }

    fun loadDailyNextDayData() {
        val todayDate = LocalDate.now()
        val nextDate = selectedDate.plusDays(1)
        if (nextDate > todayDate) {
            return
        }
        //Call API with nextDate


    }


}

enum class InternalSelectedPeriod {
    DAILY, DAY, WEEK, MONTH
}

enum class TrendsTopState {
    SINGLE, SINGLE_DATE, DOUBLE_DATE
}