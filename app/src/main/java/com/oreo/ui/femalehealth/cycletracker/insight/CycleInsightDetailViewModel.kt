package com.oreo.ui.femalehealth.cycletracker.insight

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.PeriodChartModel
import com.oreo.data.model.femaleh.PeriodLength
import com.oreo.data.model.femaleh.PeriodLengthListResponse
import com.oreo.data.repository.abstraction.FemaleHealthRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CycleInsightDetailViewModel @Inject constructor(
    val resourceProvider: ResourcesProvider,
    val femaleHealthRepository: FemaleHealthRepository
) : BaseViewModel() {


    var selectedDate: String? = null
    var launchMode: CycleInsightLaunchMode = CycleInsightLaunchMode.CYCLE_LENGTH

    val periodList = MutableLiveData<PeriodLengthListResponse>()


    fun getToolbarTitle(): String {
        return when (launchMode) {
            CycleInsightLaunchMode.CYCLE_LENGTH -> resourceProvider.getString(R.string.text_cycle_length)
            CycleInsightLaunchMode.PERIOD_DURATION -> resourceProvider.getString(R.string.text_period_duration)
        }
    }

    fun getGraphData() {
        viewModelScope.launch {
            val date = DateFormats.getTodaysDateString(10)
            if (launchMode == CycleInsightLaunchMode.CYCLE_LENGTH) {
                femaleHealthRepository.getCycleLengthData(date)
            } else {
                femaleHealthRepository.getPeriodDurationList(date)
            }.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getGraphData()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            periodList.postValue(it)
                        }
                    }
                }
            }
        }


    }

    fun getPrefixAndSuffixList(dataList: List<PeriodLength>): Triple<ArrayList<PeriodChartModel>, ArrayList<PeriodChartModel>, ArrayList<PeriodChartModel>> {

        val (normalMin, normalMax) = getNormalMinMax()

        val dateList = ArrayList<String>()

        val list = java.util.ArrayList<PeriodChartModel>()
        dataList.forEach {
            val chartModel = PeriodChartModel()
            chartModel.date = it.date

            val month = LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .format(
                    DateTimeFormatter.ofPattern(
                        "MMM",
                        Locale(NoiseFitApplicationMain.appLanguage.languageCode)
                    )
                )
            val day = LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .format(
                    DateTimeFormatter.ofPattern(
                        "dd",
                        Locale(NoiseFitApplicationMain.appLanguage.languageCode)
                    )
                )

            chartModel.month = month
            chartModel.day = day
            chartModel.value = it.length

            if (it.length > normalMax || it.length < normalMin) {
                chartModel.isNormal = false
            }
            list.add(chartModel)
            dateList.add(it.date)
        }

        //list.reverse()
        val lastDateFromList = dataList.first().date
        val lastDate = DateFormats.subtractDateFormat3(lastDateFromList, 1)!!
        val suffixDatesList = DateFormats.getWeekDaysBetweenDates(
            DateFormats.subtractDateFormat3(lastDate, 14)!!, lastDate,
            DateFormats.dateFormat3(), DateFormats.singleWeekDay()
        )
        val currentDateFromList = dataList.last().date
        val currentDate = DateFormats.addDateFormat3(currentDateFromList, 1)!!
        val prefixDatesList = DateFormats.getWeekDaysBetweenDates(
            currentDate,
            DateFormats.addDateFormat3(currentDate, 14)!!,
            DateFormats.dateFormat3(), DateFormats.singleWeekDay()
        )

        val suffix = java.util.ArrayList<PeriodChartModel>()
        suffixDatesList.forEach {
            val chartModel = PeriodChartModel()
            chartModel.month = ""
            chartModel.date = ""
            chartModel.day = ""
            chartModel.value = 0
            suffix.add(chartModel)
        }

        suffix.reverse()


        val prefix = java.util.ArrayList<PeriodChartModel>()
        prefixDatesList.forEach {
            val chartModel = PeriodChartModel()
            chartModel.month = ""
            chartModel.date = ""
            chartModel.day = ""
            chartModel.value = 0
            prefix.add(chartModel)
        }
        prefix.reverse()


        return Triple(list, suffix, prefix)

    }

    fun shouldLoadMoreData(): Boolean {
        return false
    }

    fun getNormalMinMax(): Pair<Int, Int> {
        return when (launchMode) {
            CycleInsightLaunchMode.CYCLE_LENGTH -> Pair(21, 35)
            CycleInsightLaunchMode.PERIOD_DURATION -> Pair(2, 7)
        }
    }
}