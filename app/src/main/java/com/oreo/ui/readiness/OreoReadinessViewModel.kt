package com.oreo.ui.readiness

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.common.averageWithoutZero
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributors
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.TestDataModel
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.oreo.data.model.health.UnitDataModelArray
import com.noisefit_commans.utils.LOGS
import kotlin.math.roundToInt


@HiltViewModel
class OreoReadinessViewModel
@Inject
constructor(
    val userActivityRepository: OreoUserActivityRepository,
    val ringDataStore: RingDataStore,
) : BaseViewModel() {

    var selectedMasterDate: String? = null
    var selectedDate: String? = null


    private val _readinessData = MutableLiveData<TestDataModel>()
    val readinessData: LiveData<TestDataModel>
        get() = _readinessData

    private val _readinessHistoryResponse = MutableLiveData<List<OreoReadinessModel>>()
    val readinessHistoryResponse: LiveData<List<OreoReadinessModel>> = _readinessHistoryResponse

    private val _dayReadinessData = MutableLiveData<OreoReadinessModel>()
    val dayReadinessData: LiveData<OreoReadinessModel> = _dayReadinessData

    private val _contributorInfo = MutableLiveData<OContributorResponseModal>()
    private val contributorInfo: LiveData<OContributorResponseModal> = _contributorInfo
    init {
        selectedMasterDate = DateFormats.getCurrentDateOreoFormat()
    }

    fun getContributorInfo() {
        viewModelScope.launch {
            userActivityRepository.getContributorDetailsInfo(
                "readiness"
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


    fun getReadinessDetailsData(date: String? = null) {
        viewModelScope.launch {
            userActivityRepository.getReadinessHistory(
                selectedMasterDate ?: DateFormats.getCurrentDateOreoFormat()
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
                                        getReadinessDetailsData(date)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _readinessHistoryResponse.value = (it.reversed())
                            updateSelectedDate()
                        }
                    }
                }
            }

        }


    }


    var dateList = ArrayList<String>()
    fun getPrefixAndSuffixList(dataList: List<OreoReadinessModel>):
            Triple<ArrayList<ChartModel>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
        dataList.reversed()
        val list = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            var currentDayText = ""
            if (it.date == DateFormats.getCurrentDate(DateFormats.dateFormat3)) {
                currentDayText = "Today, "
            }
            val formattedDate = DateFormats.formatDate(
                it.date,
                DateFormats.dateFormat3,
                DateFormats.dateFormat7
            )
            chartModel.formattedDate = "$currentDayText $formattedDate"
            chartModel.date = it.date
            chartModel.index = DateFormats.formatWeek(it.date)
            chartModel.value = it.readinessScore?.value ?: 0
            list.add(chartModel)
            dateList.add(it.date)
        }

        list.reverse()
        val lastDateFromList = dataList.last().date
        val lastDate = DateFormats.subtractDateFormat3(lastDateFromList, 1)!!
        val suffixDatesList = DateFormats.getWeekDaysBetweenDates(
            DateFormats.subtractDateFormat3(lastDate, 14)!!, lastDate,
            DateFormats.dateFormat3, DateFormats.singleWeekDay
        )
        val currentDateFromList = dataList.first().date
        val currentDate = DateFormats.addDateFormat3(currentDateFromList, 1)!!
        val prefixDatesList = DateFormats.getWeekDaysBetweenDates(
            currentDate,
            DateFormats.addDateFormat3(currentDate, 14)!!,
            DateFormats.dateFormat3, DateFormats.singleWeekDay
        )

        val suffix = java.util.ArrayList<ChartModel>()
        suffixDatesList.forEach {
            val chartModel = ChartModel()
            chartModel.index = it
            chartModel.value = 0
            chartModel.date = ""
            suffix.add(chartModel)
        }

        suffix.reverse()


        val prefix = java.util.ArrayList<ChartModel>()
        prefixDatesList.forEach {
            val chartModel = ChartModel()
            chartModel.index = it
            chartModel.value = 0
            chartModel.date = ""
            prefix.add(chartModel)
        }
        prefix.reverse()


        return Triple(list, suffix, prefix)

    }

    fun getHeartPrefixAndSuffixList(dataList: List<Int>):
            Triple<ArrayList<ChartModel>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
        dataList.reversed()
        val list = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            chartModel.date = ""
            chartModel.index = ""
            chartModel.value = it
            list.add(chartModel)
        }

        list.reverse()
//        val lastDateFromList = dataList.last().date
//        val lastDate = DateFormats.subtractDateFormat3(lastDateFromList, 1)!!
//        val suffixDatesList = DateFormats.getWeekDaysBetweenDates(
//            DateFormats.subtractDateFormat3(lastDate, 14)!!, lastDate,
//            DateFormats.dateFormat3, DateFormats.singleWeekDay
//        )
//        val currentDateFromList = dataList.first().date
//        val currentDate = DateFormats.addDateFormat3(currentDateFromList, 1)!!
//        val prefixDatesList = DateFormats.getWeekDaysBetweenDates(
//            currentDate,
//            DateFormats.addDateFormat3(currentDate, 14)!!,
//            DateFormats.dateFormat3, DateFormats.singleWeekDay
//        )

        val suffix = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            chartModel.index = ""
            chartModel.value = 0
            chartModel.date = ""
            suffix.add(chartModel)
        }

        suffix.reverse()


        val prefix = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            chartModel.index = ""
            chartModel.value = 0
            chartModel.date = ""
            prefix.add(chartModel)
        }
        prefix.reverse()


        return Triple(list, suffix, prefix)

    }


    fun updateSelectedDate() {

        val dayData = _readinessHistoryResponse.value?.firstOrNull() {
            it.date.equals(selectedDate, false)
        }
        if (dayData != null) {
            _dayReadinessData.postValue(dayData)
        }else{
            _readinessHistoryResponse.value?.lastOrNull()?.let { data ->
                LOGS.w("moveToPosition selected Date new $selectedDate")
                selectedDate = data.date
                LOGS.w("moveToPosition selected Date new set $selectedDate")
                _dayReadinessData.postValue(data)
            }
        }
    }

    fun getBannerDummyData(): ArrayList<Nudges> {
        val listData = ArrayList<Nudges>()
        listData.add(
            Nudges(
                "You’re on the mend",
                "A poor sleep score could be caused by insufficient or extraneous sleep, or due to frequent dreams."
            )
        )
        listData.add(
            Nudges(
                "You’re on the mend",
                "A poor sleep score could be caused by insufficient or extraneous sleep, or due to frequent dreams."
            )
        )
        listData.add(
            Nudges(
                "You’re on the mend",
                "A poor sleep score could be caused by insufficient or extraneous sleep, or due to frequent dreams."
            )
        )
        return listData
    }


    private fun getParsedDescriptionData(): ArrayList<String> {
        val descriptionList = ArrayList<String>()
        descriptionList.add(contributorInfo.value?.yesterdaySleepDuration ?: "")
        descriptionList.add(contributorInfo.value?.sleepBalance ?: "")
        descriptionList.add(contributorInfo.value?.yesterdayActivity ?: "")
        descriptionList.add(contributorInfo.value?.activityBalance ?: "")
        descriptionList.add(contributorInfo.value?.hrvBalance ?: "")
        descriptionList.add(contributorInfo.value?.restingHr ?: "")
        //descriptionList.add(contributorInfo.value?.heartRate ?: "")
        descriptionList.add(contributorInfo.value?.recoveryIndex ?: "")
        return descriptionList
    }

    fun prepareDataForDescriptionArray(resultData: java.util.ArrayList<Contributors>): ArrayList<Contributors> {
        val contList = ArrayList<Contributors>()
        val desList = getParsedDescriptionData()
        for (i in resultData.indices) {
            val ctList = resultData[i]
            val child = Contributors(
                title = ctList.title,
                leftText = ctList.leftText,
                leftTextColor = ctList.leftTextColor,
                barColor = ctList.barColor,
                barPercent = ctList.barPercent,
                backgroundRes = ctList.backgroundRes,
                description = desList[i]
            )
            contList.add(child)
        }

        return contList
    }

    fun getContributorsData(dayData: OreoReadinessModel?): List<Contributors> {
        val result = ArrayList<Contributors>()

        if (dayData?.totalSleep != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.totalSleep.status)
            result.add(
                Contributors(
                    title = "Yesterday's sleep duration",
                    leftText = dayData.totalSleep.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.totalSleep.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Yesterday's sleep duration",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }
        if (dayData?.sleepBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.sleepBalance.status)
            result.add(
                Contributors(
                    title = "Sleep balance",
                    leftText = dayData.sleepBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.sleepBalance.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Sleep balance",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData?.activityScore != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.activityScore.status)

            result.add(
                Contributors(
                    title = "Yesterday's activity",
                    leftText = dayData.activityScore.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.activityScore.value ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Yesterday's activity",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData?.activityBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.activityBalance.status)

            result.add(
                Contributors(
                    title = "Activity balance",
                    leftText = dayData.activityBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.activityBalance.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Activity balance",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData?.hrvBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.hrvBalance.status)
            result.add(
                Contributors(
                    title = "HRV balance",
                    leftText = dayData.hrvBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.hrvBalance.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "HRV balance",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData?.restingHrBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.restingHrBalance.status)

            result.add(
                Contributors(
                    title = "Resting HR",
                    leftText = dayData.restingHrBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.restingHrBalance.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Resting HR",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

       /* if (dayData?.hrReserve != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.hrReserve.status)

            result.add(
                Contributors(
                    title = "Heart rate reserve",
                    leftText = dayData.hrReserve.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.hrReserve.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Heart rate reserve",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }*/

        if (dayData?.recoveryIndex != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.recoveryIndex.status)

            result.add(
                Contributors(
                    title = "Recovery index",
                    leftText = dayData.recoveryIndex.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.recoveryIndex.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Recovery index",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }




        return result
    }

    private fun getContributorsColors(status: String): Triple<Int, Int, Int> {
        return if (status.equals("warning", true)) {
            Triple(
                R.color.oreo_contributor_warning,
                R.color.oreo_contributor_warning,
                com.noisefit_commans.R.drawable.back_modal_new_warning
            )
        } else {
            Triple(
                R.color.white,
                R.color.readiness_progress_color,
                com.noisefit_commans.R.drawable.back_modal_new
            )
        }
    }

    fun getStatusColors(status: String?): Int {
        val color: Int = if (status.equals("warning", true)) {
            R.color.oreo_contributor_warning
        } else if (status.equals("good", true)) {
            R.color.distance_arc
        } else if (status.equals("optimal", true)) {
            R.color.steps_arc
        } else {
            R.color.white
        }
        return color
    }

    fun getDummyBreakUpDataForTimeDisplay(): ArrayList<Int> {
        val dummyList = ArrayList<Int>()
        for (i in 0..287) {
            dummyList.add(0)
        }
        return dummyList

    }

    fun getDummyBreakUpDataForTimeDisplayFloat(): ArrayList<Float> {
        val dummyList = ArrayList<Float>()
        for (i in 0..287) {
            dummyList.add(0f)
        }
        return dummyList

    }


}