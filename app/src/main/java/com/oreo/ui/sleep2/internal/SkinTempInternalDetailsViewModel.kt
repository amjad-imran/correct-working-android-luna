package com.oreo.ui.sleep2.internal

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
class SkinTempInternalDetailsViewModel
@Inject
constructor(
    private val resourcesProvider: ResourcesProvider,
    private val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {

    var startDate: String = ""
    var endDate: String = ""
    val fragments = MutableLiveData<List<Fragment>?>()
    var dayAvg: TrendAverage? = null
    var weekAvg: TrendAverage? = null
    var monthAvg: TrendAverage? = null

    var currentStartDate: LocalDate? = null
    var dataTill: LocalDate? = null
    val trendsData = HashMap<LocalDate, TrendsValues>()
    lateinit var selectedLaunchMode: SleepInternalLaunchState
    var isDeviationSelected:Boolean=true
    val reloadFragment = MutableLiveData<Event<SleepInternalLaunchState>>()

    private val _selectedPeriod = MutableLiveData<InternalSelectedPeriod>()
    val selectedPeriod: LiveData<InternalSelectedPeriod> = _selectedPeriod

    init {
        val datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        endDate = LocalDate.now().format(datePattern)
        startDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
            .format(datePattern)
    }
    fun setSelectedPeriod(selectedPeriod: InternalSelectedPeriod) {
        fragments.value = null
        _selectedPeriod.value = selectedPeriod

    }

    private val _titleUpdate =
        MutableLiveData<Pair<String, Int>>()
    val titleUpdate: LiveData<Pair<String, Int>> = _titleUpdate


    private fun getTitle(): Pair<String, Int> {
        return when (selectedLaunchMode) {
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

            else -> {
                Pair(
                    resourcesProvider.getString(R.string.text_skin_temperature),
                    R.drawable.ic_skin_tempreature
                )
            }
        }
    }

    fun loadMoreData() {
        loadNewFragment()
    }

    fun reloadData() {
        currentStartDate = null
        loadNewFragment()
    }
    fun updateTitle() {
        _titleUpdate.postValue(getTitle())
    }
    fun getTopState(): TrendsTopState {
        return if (selectedLaunchMode == SleepInternalLaunchState.SLEEP_DURATION) {
            TrendsTopState.SINGLE_DATE
        } else {
            TrendsTopState.SINGLE
        }
    }

    fun getUnit(): String {
        return if (selectedLaunchMode == SleepInternalLaunchState.SLEEP_PERFORMANCE) {
            "%"
        } else if (selectedLaunchMode == SleepInternalLaunchState.RESTFULNESS) {
            "times"
        } else {
            "min"
        }
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

//    fun updateTrendsName(trendsName: String?) {
//        when (trendsName?.lowercase()?.replace(" ", "_")) {
//            SleepInternalLaunchState.RESPIRATORY_RATE.name.lowercase() -> selectedLaunchMode =
//                SleepContributor.RESPIRATORY_RATE
//
//            SleepContributor.RESTING_HEART_RATE.name.lowercase() -> selectedLaunchMode =
//                SleepContributor.RESTING_HEART_RATE
//
//            SleepContributor.HRV.name.lowercase() -> selectedLaunchMode =
//                SleepContributor.HRV
//
//            SleepContributor.SKIN_TEMPERATURE.name.lowercase() -> selectedLaunchMode =
//                SleepContributor.SKIN_TEMPERATURE
//
//            SleepContributor.BLOOD_OXYGEN.name.lowercase() -> selectedLaunchMode =
//                SleepContributor.BLOOD_OXYGEN
//
//        }
//    }

    fun getTrendsInternalDetailsData() {
        viewModelScope.launch(Dispatchers.IO) {
            userActivityRepository.getSleepInternalTrendsPagesData(
                startDate,
                endDate,
                SleepInternalLaunchState.SLEEP_PERFORMANCE.key
                /*  selectedLaunchMode.key.lowercase()*/
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

                            it.data?.firstOrNull()?.date?.let {
                                dataTill = LocalDate.parse(it)
                            }

                            loadNewFragment()



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
                            .minusWeeks(6),
                        LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                    )
                } else {
                    Pair(
                        currentStartDate!!.with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
                            .minusWeeks(6),
                        currentStartDate!!.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY))
                    )
                }
            }

            InternalSelectedPeriod.MONTH -> {
                val start = LocalDate.now().minusMonths(6).withDayOfMonth(1)
                val end = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                Pair(start, end)
            }
        }

        return Pair(start, end)
    }

    private fun loadNewFragment() {

        val (start, end) = getDatesToLoad()
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
            current = current.plusDays(1)
        }


        val trendData = TrendsGraphData(
            data = dataToDisplay
        )

        getFragmentToAdd(trendData).let {
            var oldData = fragments.value
            if (oldData == null) {
                oldData = ArrayList()
            }
            (oldData as ArrayList).add(it)
            fragments.postValue(oldData)
        }
    }


    private fun getFragmentToAdd(trendData: TrendsGraphData): Fragment {
        trendData.contributorType = selectedLaunchMode

        when (selectedPeriod.value) {
            InternalSelectedPeriod.DAY, null -> {
                return when (selectedLaunchMode) {
                    SleepInternalLaunchState.BLOOD_OXYGEN,
                    SleepInternalLaunchState.RESPIRATORY_RATE -> {

                        SleepBarChartFragment.newInstance(
                            trendData
                        )
                    }

                    SleepInternalLaunchState.SKIN_TEMPERATURE,
                    SleepInternalLaunchState.RESTING_HEART_RATE,
                    SleepInternalLaunchState.HRV -> SleepSingleLineGradientChartFragment.newInstance(
                        trendData
                    )


                    else -> SleepBarChartFragment.newInstance(trendData)
                }
            }

            InternalSelectedPeriod.WEEK -> {
                return SleepSingleLineChartFragment.newInstance(trendData.apply {
                    this.selectedPeriod = InternalSelectedPeriod.WEEK
                })
            }

            InternalSelectedPeriod.MONTH -> {
                return SleepSingleLineChartFragment.newInstance(trendData.apply {
                    this.selectedPeriod = InternalSelectedPeriod.MONTH
                })
            }
        }


    }

    fun getPostFixAbr(trendType: SleepInternalLaunchState): String {
        val abr: String = when (trendType) {
            SleepInternalLaunchState.SKIN_TEMPERATURE -> "°F - average"
            SleepInternalLaunchState.HRV -> "ms - average"
            SleepInternalLaunchState.RESPIRATORY_RATE -> "rpm - average"
            SleepInternalLaunchState.RESTING_HEART_RATE -> "bpm - average"
            SleepInternalLaunchState.BLOOD_OXYGEN -> "% - average"
            SleepInternalLaunchState.SLEEP_DURATION -> ""
            SleepInternalLaunchState.REM_SLEEP -> ""
            SleepInternalLaunchState.DEEP_SLEEP -> ""
            SleepInternalLaunchState.EFFICIENCY -> ""
            SleepInternalLaunchState.LATENCY -> ""
            SleepInternalLaunchState.RESTFULNESS -> ""
            SleepInternalLaunchState.TIMING -> ""
            else -> {
                ""
            }
        }
        return abr

    }

    fun getHighlightBackType(type: Int): Pair<Int, Int> {
        /*
        * 0-up
        * 1-warning
        * 2-red alert
        * */
        val background: Int
        val textColor: Int
        when (type) {
            0 -> {
                background = R.drawable.back_hm_optimal
                textColor = R.color.white
            }

            1 -> {
                background = R.drawable.back_hm_fair
                textColor = R.color.color_trends_warning
            }

            else -> {
                background = R.drawable.back_hm_warning
                textColor = R.color.oreo_contributor_warning
            }
        }
        return Pair(background, textColor)
    }

    fun returnTrendsArrow(type: SleepInternalLaunchState): Int {
        val icon: Int = when (type) {
            SleepInternalLaunchState.RESPIRATORY_RATE,
            SleepInternalLaunchState.HRV,
            SleepInternalLaunchState.RESTING_HEART_RATE,
            SleepInternalLaunchState.BLOOD_OXYGEN,
            SleepInternalLaunchState.SKIN_TEMPERATURE
            -> R.drawable.ic_hm_tick

            else -> 0
        }
        return icon

    }

//    fun hmDummyData(): OHealthMonTrendsDataModel {
//        return OHealthMonTrendsDataModel(
//            trendType = selectedLaunchMode,
//            dayDate = "Monday  30 May, 2024",
//            dspValue = "40",
//            isShowOptimal = false,
//            isShowHighlight = true,
//            description = "Your resting HR seems to be higher than previous day. Allow yourself sufficient time for recovery by taking it slow."
//        )
//    }

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
        }

    }


}

