package com.oreo.ui.sleep2.internal

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
import com.oreo.data.model.OSleepInternalTrendsDataModel
import com.oreo.data.model.OSleepTrendsDataModel
import com.oreo.data.model.TrendsData
import com.oreo.data.model.TrendsValues
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SleepInternalDetailsViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {

    lateinit var selectedLaunchMode: SleepInternalLaunchState
    val trendsData = HashMap<LocalDate, TrendsValues>()

    var startDate: String = ""
    var endDate: String = ""

    private val _selectedPeriod =
        MutableLiveData<InternalSelectedPeriod>(InternalSelectedPeriod.DAY)
    val selectedPeriod: LiveData<InternalSelectedPeriod> = _selectedPeriod

    fun setSelectedPeriod(selectedPeriod: InternalSelectedPeriod) {
        _selectedPeriod.postValue(selectedPeriod)
    }

    private val _titleUpdate =
        MutableLiveData<Pair<String, Int>>()
    val titleUpdate: LiveData<Pair<String, Int>> = _titleUpdate

    private val _trendsInternalData = MutableLiveData<OSleepInternalTrendsDataModel>()
    val trendsInternalData: LiveData<OSleepInternalTrendsDataModel>
        get() = _trendsInternalData

    val trendsDataLoaded = MutableLiveData<Event<Boolean>>()

    fun getTrendsInternalDetailsData() {
        viewModelScope.launch(Dispatchers.IO) {
            userActivityRepository.getSleepInternalTrendsPagesData(
                startDate, endDate, selectedLaunchMode.key.lowercase()
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
                            trendsDataLoaded.postValue(Event(true))

                            //_trendsInternalData.postValue(it)
                        }
                    }
                }
            }
        }
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
                resourcesProvider.getString(R.string.text_sleep_time),
                R.drawable.ic_sleep_time
            )

            SleepInternalLaunchState.HOUR_VS_NEED -> Pair(
                resourcesProvider.getString(R.string.text_hour_vs_need),
                R.drawable.ic_sleep_snooz
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
                resourcesProvider.getString(R.string.text_rem_sleep),
                R.drawable.ic_sleep_rem_sp
            )

            SleepInternalLaunchState.DEEP_SLEEP -> Pair(
                resourcesProvider.getString(R.string.text_deep_sleep),
                R.drawable.ic_sleep_deep_sp
            )

            SleepInternalLaunchState.LATENCY -> Pair(
                resourcesProvider.getString(R.string.text_latency),
                R.drawable.ic_sleep_latency
            )

            SleepInternalLaunchState.RESTFULNESS -> Pair(
                resourcesProvider.getString(R.string.text_restfulness),
                R.drawable.ic_sleep_restfulness
            )

            SleepInternalLaunchState.SLEEP_DURATION -> Pair(
                resourcesProvider.getString(R.string.text_sleep_duration),
                R.drawable.ic_clock_off_sleep
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

    fun updateTrendsName(trendsName: String?) {
        when (trendsName?.lowercase()?.replace(" ", "_")) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP.name.lowercase() -> selectedLaunchMode =
                SleepInternalLaunchState.RESTORATIVE_SLEEP

            SleepInternalLaunchState.SLEEP_PERFORMANCE.name.lowercase() -> selectedLaunchMode =
                SleepInternalLaunchState.SLEEP_PERFORMANCE

            SleepInternalLaunchState.DEEP_SLEEP.name.lowercase() -> selectedLaunchMode =
                SleepInternalLaunchState.DEEP_SLEEP

            SleepInternalLaunchState.EFFICIENCY.name.lowercase() -> selectedLaunchMode =
                SleepInternalLaunchState.EFFICIENCY

            SleepInternalLaunchState.LATENCY.name.lowercase() -> selectedLaunchMode =
                SleepInternalLaunchState.LATENCY

            SleepInternalLaunchState.RESTFULNESS.name.lowercase() -> selectedLaunchMode =
                SleepInternalLaunchState.RESTFULNESS

            SleepInternalLaunchState.REM_SLEEP.name.lowercase() ->
                selectedLaunchMode = SleepInternalLaunchState.REM_SLEEP

            SleepInternalLaunchState.SLEEP_TIME.name.lowercase() ->
                selectedLaunchMode = SleepInternalLaunchState.SLEEP_TIME

            SleepInternalLaunchState.SLEEP_DURATION.name.lowercase() ->
                selectedLaunchMode = SleepInternalLaunchState.SLEEP_DURATION

        }
    }

    fun trendsListDummyData(): OSleepInternalTrendsDataModel {
        /*val tempListData = ArrayList<TrendsValues>()
        tempListData.add(
            TrendsValues(date = "Monday 23 July, 2024", value = 20, nudge = "Nudge 1")
        )
        tempListData.add(
            TrendsValues(date = "Tuesday 24 July, 2024", value = 30, nudge = "Nudge 2")
        )
        tempListData.add(
            TrendsValues(date = "Wednesday 25 July, 2024", value = 10, nudge = "Nudge 3")
        )
        tempListData.add(
            TrendsValues(date = "Thursday 26 July, 2024", value = 5, nudge = "Nudge 4")
        )
        tempListData.add(
            TrendsValues(date = "Friday 27 July, 2024", value = 15, nudge = "Nudge 5")
        )
        tempListData.add(
            TrendsValues(date = "Saturday 28 July, 2024", value = 25, nudge = "Nudge 6")
        )
        tempListData.add(
            TrendsValues(
                date = "Sunday 29 July, 2024",
                value = 100,
                value2 = 200,
                nudge = "Nudge 7"
            )
        )*/
        val trendsData = OSleepInternalTrendsDataModel()
        trendsData.data = findLastSixMonthDatesList()
        return trendsData

    }


    fun getPostFixAbr(trendType: SleepInternalLaunchState): String {
        var abr = ""
        if (trendType == SleepInternalLaunchState.SLEEP_PERFORMANCE) {
            abr = "% - average"
        } else if (trendType == SleepInternalLaunchState.RESTFULNESS)
            abr = "times - average"

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
                textColor = R.color.edit_text_color
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
            SleepInternalLaunchState.SLEEP_PERFORMANCE, SleepInternalLaunchState.HOUR_VS_NEED, SleepInternalLaunchState.RESTORATIVE_SLEEP -> R.drawable.ic_trend_up
            SleepInternalLaunchState.EFFICIENCY, SleepInternalLaunchState.RESTFULNESS, SleepInternalLaunchState.LATENCY, SleepInternalLaunchState.SLEEP_DURATION -> R.drawable.ic_hm_tick
            else -> 0
        }
        return icon

    }

    fun parsePageData(pos: Int): OSleepTrendsDataModel {
        val childData = OSleepTrendsDataModel()
        val data = trendsInternalData.value?.data?.get(pos)
        childData.trendType = selectedLaunchMode
        childData.dayDate = data?.date
        childData.dspValue = data?.value1
        childData.dspValue2 = data?.value2
        return childData
    }

    private fun findLastSixMonthDatesList(): List<TrendsValues> {
        val currentDate = LocalDate.now()
        val sixMonthsAgo = currentDate.minusMonths(6)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val datesList = mutableListOf<String>()
        var date = sixMonthsAgo

        while (!date.isAfter(currentDate)) {
            datesList.add(date.format(formatter))
            date = date.plusDays(1)
        }

        val graphData = mutableListOf<TrendsValues>()
        println("List of Dates from 6 Months Ago to Current Date: ${datesList.size}")
        var i = 0
        for (formattedDate in datesList) {
            i++
            val ch = TrendsValues()
            ch.date = formattedDate
            ch.value1 = 3600 + i
            ch.value2 = 200
        }

        return graphData.takeLast(7)
    }


}

enum class InternalSelectedPeriod {
    DAY, WEEK, MONTH
}