package com.oreo.ui.sleep.scoredetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.google.gson.Gson
import com.noisefit.data.remote.base.Resource
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.BreakUp
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Comparison
import com.oreo.data.model.OInternalPageResponseModal
import com.oreo.data.model.OTestInternalPageResponseModal
import com.oreo.data.model.ResultData
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OSCDViewModel @Inject constructor(
    val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {


    var topDateLastScrollPosition = 0
    var topGraphLastScrollPosition = 15

    var dayType: String? = null
    var viewType: String? = null
    var itemType: String? = null
    var itemClickType: String? = null
    var selectedDate: String? = null
    var defaultDayPos: Int = 0
    var isProgressEqual: Boolean = false
    var trendDifferenceProgress: Int = 0

    var isTodayGreater: Boolean = false


    val trendDiff = MutableLiveData<Int>()
    private val _internalDetailsData = MutableLiveData<OInternalPageResponseModal>()
    val internalDetailsData: LiveData<OInternalPageResponseModal>
        get() = _internalDetailsData
    private val _comparisonData = MutableLiveData<Comparison>()
    val comparisonData: LiveData<Comparison>
        get() = _comparisonData

    private val _internalActDetailsData = MutableLiveData<OTestInternalPageResponseModal>()

    /*val internalActDetailsData: LiveData<OTestInternalPageResponseModal>
        get() = _internalActDetailsData
*/
    private val _topLevelData = MutableLiveData<ResultData>()
    val topLevelData: LiveData<ResultData> = _topLevelData

    /* private val _topActLevelData = MutableLiveData<ResultData>()
     val topActLevelData: LiveData<ResultData> = _topActLevelData*/

    fun setTrendData(value: Int) {
        trendDifferenceProgress = value
        trendDiff.postValue(trendDifferenceProgress)
    }

    fun getInternalDetailsData() {
        viewModelScope.launch {
            userActivityRepository.getInternalPagesData(
                selectedDate!!, dayType.toString().lowercase(), returnContributorType()
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
                                        getInternalDetailsData()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _internalDetailsData.postValue(it)

                            it.result?.firstOrNull()?.let { data ->
                                _topLevelData.postValue(data)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun returnContributorType(): String {
        val contributorType: String =
            if (itemClickType.toString().lowercase() == ViewItemClickType.STEPS.name.lowercase())
                "total_steps"
            else if (itemClickType.toString()
                    .lowercase() == ViewItemClickType.DISTANCE.name.lowercase()
            ) {
                "total_distance"
            } else if (itemClickType.toString()
                    .lowercase() == ViewItemClickType.TOTAL_CALORIES_BURNED.name.lowercase()
            ) {
                "total_calories"
            } else if (itemClickType.toString()
                    .lowercase() == ViewItemClickType.HR_VARIABILITY.name.lowercase()
            ) {
                "hrv"
            } else if (itemClickType.toString()
                    .lowercase() == ViewItemClickType.BODY_TEMPERATURE.name.lowercase()
            ) {
                "temperature"
            } else if (itemClickType.toString()
                    .lowercase() == ViewItemClickType.RESPIRATORY_RATE.name.lowercase()
            ) {
                "respiration"
            } else
                itemClickType.toString()

        return contributorType.lowercase()
    }

    fun getActivityInternalDetailsData() {

        viewModelScope.launch {
            userActivityRepository.getActivityInternalPagesData(
                selectedDate!!, dayType.toString().lowercase(), returnContributorType()
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
                                        getActivityInternalDetailsData()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
//                            _internalActDetailsData.postValue(it)
                            _internalDetailsData.postValue(it)

                            it.result?.firstOrNull()?.let { data ->
//                                _topActLevelData.postValue(data)
                                _topLevelData.postValue(data)
                            }

                            /*val comparison = Comparison()
                            val breakUpList = ArrayList<BreakUp>()
                            for (i in 0 until 23) {
                                val child = BreakUp()
                                child.calories = 10.plus(i.times(5))
                                child.avgCalories = 25.plus(i.times(2))
                                child.hourOfDay = 16000
                                breakUpList.add(child)
                            }
                            comparison.today = 44
                            comparison.average = 66
                            comparison.breakup = breakUpList
                            _comparisonData.postValue(comparison)*/
                        }
                    }
                }
            }
        }
    }

    fun getReadinessInternalDetailsData() {

        viewModelScope.launch {
            userActivityRepository.getReadinessInternalPagesData(
                selectedDate!!, dayType.toString().lowercase(), returnContributorType()
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
                                        getReadinessInternalDetailsData()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
//                            _internalActDetailsData.postValue(it)
                            _internalDetailsData.postValue(it)

                            it.result?.firstOrNull()?.let { data ->
//                                _topActLevelData.postValue(data)
                                _topLevelData.postValue(data)
                            }
                        }
                    }
                }
            }
        }
    }

    fun getPrefixAndSuffixList(
        dataList: ArrayList<ResultData>,
        dayType: String?
    ): Triple<Pair<ArrayList<ChartModel>, Int>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
        dataList.reversed()
        val list = java.util.ArrayList<ChartModel>()

        dataList.forEach {


            val chartModel = ChartModel()
            chartModel.date = it.date
            if (dayType?.lowercase() == "day")
                chartModel.index = DateFormats.shortFormatWeek(it.date)
            else if (dayType?.lowercase() == "month")
                chartModel.index = DateFormats.getMonth(it.date.toInt() - 1)
            else
                chartModel.index = it.date

            LOGS.d("dsakjdsalkjlkdsa $itemClickType")
            when (itemClickType) {
                ViewItemClickType.TOTAL_SLEEP.name,
                ViewItemClickType.TIME_IN_BED.name -> {
                    val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                        it.data.toInt()
                    )
                    chartModel.value = hour
                }

                else -> {
                    chartModel.value = it.data.toInt()
                }
            }

            list.add(chartModel)
        }

        val suffix = java.util.ArrayList<ChartModel>()
        for (i in 1..14) {
            val chartModel = ChartModel()
            chartModel.index = ""
            chartModel.value = 0
            chartModel.date = ""
            suffix.add(chartModel)
        }

        val prefix = java.util.ArrayList<ChartModel>()
        val chartModel = ChartModel()
        chartModel.index = ""
        chartModel.value = 0
        chartModel.date = ""
        prefix.add(chartModel)

        return Triple(Pair(list, 0), suffix, prefix)
    }

    fun updateSelectedDate(date: String) {

        val dayData = _internalDetailsData.value?.result?.firstOrNull() {
            it.date.equals(date, false)
        }
        LOGS.d("sdjsahjhsadkjhdasjkhk ${date} ${Gson().toJson(dayData)}")
        if (dayData != null) {
            _topLevelData.postValue(dayData!!)
        }

    }

    fun getGraphEntriesData(
        color1: Int,
        color2: Int
    ): Triple<List<Entry>, List<Entry>, (Pair<List<Int>, List<Int>>)> {
        //data1
        val data1 = java.util.ArrayList<Entry>()
        val data2 = java.util.ArrayList<Entry>()
        val colorList1 = java.util.ArrayList<Int>()
        val colorList2 = java.util.ArrayList<Int>()
//        _comparisonData.value?.breakup?.forEach {
//            data1.add(Entry(it.calories!!.toFloat(),(Math.random() * 10 + 2).toFloat()))
//            data2.add(Entry(it.avgCalories!!.toFloat(),(Math.random() * 10 + 3).toFloat()))
//            colorList1.add(color1)
//            colorList2.add(color2)
//        }
        for (i in 0..23) {
            data1.add(Entry(i.toFloat(), (Math.random() * 10 + 2).toFloat()))
            data2.add(Entry(i.toFloat() + 1, (Math.random() * 10 + 3).toFloat()))
            colorList1.add(color1)
            colorList2.add(color2)
        }

        return Triple(data1, data2, Pair(colorList1, colorList2))

    }


}
