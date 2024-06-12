package com.oreo.ui.femalehealth.cycletracker

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.dataConverter.FemaleHealthDataConvertor
import com.oreo.data.dataConverter.FemaleHealthGeneratorResult
import com.oreo.data.model.ChartModel
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.data.model.PeriodChartModel
import com.oreo.data.model.PeriodCycleHistory
import com.oreo.data.model.PeriodTempChartModel
import com.oreo.data.model.femaleh.FemaleTempResponse
import com.oreo.data.model.femaleh.PeriodLength
import com.oreo.data.model.femaleh.TempPeriodData
import com.oreo.data.model.femaleh.TempPrediction
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.data.repository.abstraction.FemaleHealthRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.ui.custom.Section
import com.oreo.ui.femalehealth.cycletracker.insight.CycleInsightLaunchMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.SortedMap
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class SkinTemperatureViewModel @Inject constructor(
    private val femaleHealthRepository: FemaleHealthRepository,
    val femaleHealthDataConvertor: FemaleHealthDataConvertor,
) : BaseViewModel() {

    val tempData = MutableLiveData<FemaleTempResponse>()

    private val _cycleHistoryData = MutableLiveData<List<FMHCycleHistoryDataModel>?>()
    val cycleHistoryData: LiveData<List<FMHCycleHistoryDataModel>?> get() = _cycleHistoryData

    var healthDataDateList = HashMap<LocalDate, DayState>()

    init {
        getCycleHistoryData()
    }

    fun getCycleHistoryData() {
        viewModelScope.launch {
            femaleHealthRepository.getPeriodCycleHistory().collect { resource ->
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
                                        getCycleHistoryData()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {


                            generateHealthData(it)


                        }
                    }
                }
            }
        }
    }

    private fun generateHealthData(cycleData: PeriodCycleHistory) {
        viewModelScope.launch {

            femaleHealthDataConvertor.convertHealthData(cycleData, true)
                .collect { resource ->

                    when (resource) {
                        is FemaleHealthGeneratorResult.Loading -> {
                            setLoading(resource.loading)
                        }

                        is FemaleHealthGeneratorResult.Success -> {
                            healthDataDateList.clear()
                            healthDataDateList = resource.value
                            _cycleHistoryData.postValue(cycleData.cycleHistory)
                        }
                    }
                }
        }
    }


    fun getTempData(date: String) {
        viewModelScope.launch {
            femaleHealthRepository.getFemaleHealthTempData(date).collect { resource ->
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
                                        getTempData(date)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            tempData.postValue(it)
                        }
                    }
                }
            }

        }
    }

    /**
     * returns Pair(Max value, Data)
     */
    fun getPrefixAndSuffixList(dataList: List<TempPeriodData>):
            Triple<Float,
                    Triple<ArrayList<PeriodTempChartModel>, ArrayList<PeriodTempChartModel>, ArrayList<PeriodTempChartModel>>,
                    ArrayList<Section>> {

        val dateList = ArrayList<String>()

        val list = ArrayList<PeriodTempChartModel>()
        var max = 0.0f
        dataList.forEach {

            val phase = CyclePhase.FOLLECULAR//TODO calculate on the bases of data

            val chartModel = PeriodTempChartModel(
                phase = phase
            )
            chartModel.date = it.date

            val month =
                DateFormats.formatDateTime(
                    it.date, DateFormats.dateFormat3(), DateFormats.monthOnly()
                )
            val day = DateFormats.formatDateTime(
                it.date, DateFormats.dateFormat3(), DateFormats.dateOnly()
            )

            chartModel.month = month
            chartModel.day = day
            chartModel.value = it.temperature

            list.add(chartModel)
            dateList.add(it.date)

            val currentVal = abs(it.temperature ?: 0f)
            if (currentVal > max) {
                max = currentVal
            }

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

        val suffix = java.util.ArrayList<PeriodTempChartModel>()
        suffixDatesList.forEach {
            val chartModel = PeriodTempChartModel()
            chartModel.month = ""
            chartModel.date = ""
            chartModel.day = ""
            chartModel.value = null
            suffix.add(chartModel)
        }

        suffix.reverse()

        val prefix = java.util.ArrayList<PeriodTempChartModel>()
        prefixDatesList.forEach {
            val chartModel = PeriodTempChartModel()
            chartModel.month = ""
            chartModel.date = ""
            chartModel.day = ""
            chartModel.value = null
            prefix.add(chartModel)
        }
        prefix.reverse()


        /**
         * Generate Sections
         */
        val sections = ArrayList<Section>()
        getPeriodSection(dataList.last().date, dataList.first().date)?.forEach {
            sections.add(it)
           /* LOGS.d(
                "sdfsdfsdf -> ${dataList.last().date}, ${dataList.first().date} ${
                    Gson().toJson(
                        it
                    )
                }"
            )*/
        }

        return Triple(max, Triple(list, suffix, prefix), sections)
    }

    private fun getPeriodSection(startDate: String, endDate: String): List<Section>? {

        val history = cycleHistoryData.value
        if (history.isNullOrEmpty()) return null
        val sections = ArrayList<Section>()

        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDateLocal = LocalDate.parse(startDate, pattern)
        val endDateLocal = LocalDate.parse(endDate, pattern)


        val sortedPeriodData =
            healthDataDateList.filter { (it.value is DayState.Period) && (it.key <= endDateLocal) && (it.key >= startDateLocal) }
                .toSortedMap()

        val sortedOvData =
            healthDataDateList.filter {
                (it.value is DayState.Fertile || it.value is DayState.OvulationDay) && (
                        it.key <= endDateLocal
                        ) && (it.key >= startDateLocal)
            }.toSortedMap()

        createSections(sections, sortedPeriodData, endDateLocal, 1)
        createSections(sections, sortedOvData, endDateLocal, 2)

        return sections
    }

    /**
     * 1->period, 2->Fertile
     */
    private fun createSections(
        sections: ArrayList<Section>,
        sortedPeriodData: SortedMap<LocalDate, DayState>,
        endDateLocal: LocalDate,
        type: Int
    ) {
        var lastValue: Map.Entry<LocalDate, DayState>? = null
        val tempDatesArray = ArrayList<LocalDate>()
        sortedPeriodData.forEach {
            val currentKey = it.key

            if (lastValue == null || ChronoUnit.DAYS.between(
                    lastValue!!.key,
                    currentKey
                ) != 1L
            ) {
                lastValue = null
                if (tempDatesArray.isNotEmpty()) {

                    var daysStart =
                        abs(ChronoUnit.DAYS.between(endDateLocal, tempDatesArray.first()))
                    val daysEnd =
                        abs(ChronoUnit.DAYS.between(endDateLocal, tempDatesArray.last()))

                    sections.add(
                        Section(
                            if (type == 1) "period" else "",
                            daysStart.toInt(),
                            daysEnd.toInt(),
                            if (type == 1) Color.parseColor("#80ff7fc4") else Color.parseColor("#801ec9ff"),
                            imageRes = if (type == 1) R.drawable.ic_fertile_graph else R.drawable.ic_period_graph
                        )
                    )
                }
                tempDatesArray.clear()
            }

            tempDatesArray.add(currentKey)
            lastValue = it
        }
        if (tempDatesArray.isNotEmpty()) {
            var daysStart = abs(ChronoUnit.DAYS.between(endDateLocal, tempDatesArray.first()))
            val daysEnd = abs(ChronoUnit.DAYS.between(endDateLocal, tempDatesArray.last()))
            sections.add(
                Section(
                    if (type == 1) "period" else "",
                    daysStart.toInt(),
                    daysEnd.toInt(),
                    if (type == 1) Color.parseColor("#80ff7fc4") else Color.parseColor("#801ec9ff"),
                    imageRes = if (type == 1) R.drawable.ic_fertile_graph else R.drawable.ic_period_graph
                )
            )
        }
    }


    fun shouldLoadMoreData(): Boolean {
        return false
    }

    fun getDummyTempList(): ArrayList<TempPeriodData> {
        return arrayListOf(
            TempPeriodData(
                "2024-06-04",
                2.5f
            ),
            TempPeriodData(
                "2024-06-03",
                2.5f
            ), TempPeriodData(
                "2024-06-02",
                1.0f
            ), TempPeriodData(
                "2024-06-01",
                1.5f
            ), TempPeriodData(
                "2024-05-31",
                2.0f
            ), TempPeriodData(
                "2024-05-30",
                1.5f
            ), TempPeriodData(
                "2024-05-29",
                0f
            ), TempPeriodData(
                "2024-05-28",
                -0.5f
            ), TempPeriodData(
                "2024-05-27",
                -1f
            ), TempPeriodData(
                "2024-05-26",
                -1.5f
            ), TempPeriodData(
                "2024-05-25",
                -2f
            ), TempPeriodData(
                "2024-05-24",
                -2.5f
            ), TempPeriodData(
                "2024-05-23",
                0f
            ), TempPeriodData(
                "2024-05-22",
                -0.5f
            ), TempPeriodData(
                "2024-05-21",
                -1f
            ), TempPeriodData(
                "2024-05-20",
                -1.5f
            ), TempPeriodData(
                "2024-05-19",
                -2f
            ), TempPeriodData(
                "2024-05-18",
                -2.5f
            ), TempPeriodData(
                "2024-05-17",
                0f
            ), TempPeriodData(
                "2024-05-16",
                -0.5f
            ), TempPeriodData(
                "2024-05-15",
                -1f
            ), TempPeriodData(
                "2024-05-14",
                -1.5f
            ), TempPeriodData(
                "2024-05-13",
                -2f
            ), TempPeriodData(
                "2024-05-12",
                -2.5f
            ), TempPeriodData(
                "2024-05-11",
                -2.5f
            ), TempPeriodData(
                "2024-05-10",
                -2.5f
            ), TempPeriodData(
                "2024-05-09",
                -2.5f
            ), TempPeriodData(
                "2024-05-08",
                -2.5f
            ), TempPeriodData(
                "2024-05-07",
                -2.5f
            ), TempPeriodData(
                "2024-05-06",
                -2.5f
            ), TempPeriodData(
                "2024-05-05",
                -2.5f
            )
        )
    }
}