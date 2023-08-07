package com.oreo.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.averageWithoutZero
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributors
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OreoActivityViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore
) : BaseViewModel() {


    private val _activityHistoryResponse = MutableLiveData<List<OreoActivityModel>>()
    val activityHistoryResponse: LiveData<List<OreoActivityModel>> = _activityHistoryResponse

    private val _dayActivityData = MutableLiveData<OreoActivityModel>()
    val dayActivityData: LiveData<OreoActivityModel> = _dayActivityData


    private val _contributorInfo = MutableLiveData<OContributorResponseModal>()
    private val contributorInfo: LiveData<OContributorResponseModal> = _contributorInfo


    fun getContributorInfo() {
        viewModelScope.launch {
            userActivityRepository.getContributorDetailsInfo(
                "activity"
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

    fun getDatesArray(activityList: List<OreoActivityModel>?): List<String> {
        if (activityList.isNullOrEmpty()) return ArrayList()
        val datesArray = ArrayList<String>()
        activityList.forEach {
            datesArray.add(it.date)
        }
        return datesArray
    }

    fun updateSelectedDate(date: String) {
        val dayData = _activityHistoryResponse.value?.firstOrNull() {
            it.date.equals(date, false)
        }
        if (dayData != null) {
            LOGS.d("handleMovementViewshandleMovementViews-dayActivityData 2")
            _dayActivityData.postValue(dayData!!)
        }
    }

    var dateList = ArrayList<String>()
    fun getPrefixAndSuffixList(dataList: List<OreoActivityModel>): Triple<ArrayList<ChartModel>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
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
            chartModel.value = it.activityScore?.value ?: 0
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
            chartModel.date = ""
            chartModel.value = 0
            suffix.add(chartModel)
        }

        suffix.reverse()


        val prefix = java.util.ArrayList<ChartModel>()
        prefixDatesList.forEach {
            val chartModel = ChartModel()
            chartModel.index = it
            chartModel.date = ""
            chartModel.value = 0
            prefix.add(chartModel)
        }
        prefix.reverse()


        return Triple(list, suffix, prefix)

    }

    private fun getParsedDescriptionData(): ArrayList<String> {
        val descriptionList = ArrayList<String>()
        descriptionList.add(contributorInfo.value?.stayActive ?: "")
        descriptionList.add(contributorInfo.value?.moveEveryHour ?: "")
        descriptionList.add(contributorInfo.value?.caloriesGoal ?: "")
        descriptionList.add(contributorInfo.value?.trainingFrequency ?: "")
        descriptionList.add(contributorInfo.value?.trainingVolume ?: "")
        return descriptionList
    }






    fun getActivityDetailsData(date: String? = null) {
        viewModelScope.launch {
            userActivityRepository.getActivityHistory(
                date ?: DateFormats.getCurrentDateOreoFormat()
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
                                        getActivityDetailsData(date)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _activityHistoryResponse.postValue(it.reversed())

                            it.firstOrNull()?.let { data ->
                                LOGS.d("handleMovementViewshandleMovementViews-dayActivityData 1")
                                _dayActivityData.postValue(data)
                            }
                        }
                    }
                }
            }

        }


    }


    fun getContributorsData(dayData: OreoActivityModel): List<Contributors> {
        val result = ArrayList<Contributors>()

        val actContributors = dayData.activityContributors
        val stayActive = actContributors?.stayActive
        val moveEveryHour = actContributors?.moveEveryHour
        val caloriesGoal = actContributors?.calorieGoal
        val trainingFrequency = actContributors?.trainingFrequency
        val trainingVolume = actContributors?.trainingVolume
        if (stayActive != null) {
            val (textColor, barColor, background) = getContributorsColors(stayActive.status)
            val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                stayActive.value ?: 0
            )
            val leftText = "$hour hr inactivity"
            result.add(
                Contributors(
                    title = "Stay active",
                    leftText = leftText,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = stayActive.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Stay active",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_activity_bar_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (moveEveryHour != null) {
            val (textColor, barColor, background) = getContributorsColors(moveEveryHour.status)

            val warnings = moveEveryHour.value ?: 0
            result.add(
                Contributors(
                    title = "Move every hour",
                    leftText = "$warnings warning${if (warnings > 1) "s" else ""}",
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = moveEveryHour.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Move every hour",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_activity_bar_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (caloriesGoal != null) {
            val (textColor, barColor, background) = getContributorsColors(caloriesGoal.status)

            result.add(
                Contributors(
                    title = "Calorie goal",
                    leftText = "${caloriesGoal.value ?: 0}%",
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = caloriesGoal.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Calorie goal",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_activity_bar_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (trainingFrequency != null) {
            val (textColor, barColor, background) = getContributorsColors(trainingFrequency.status)
            val leftText = trainingFrequency.text

            result.add(
                Contributors(
                    title = "Training frequency",
                    leftText = leftText,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = trainingFrequency.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Training frequency",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_activity_bar_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (trainingVolume != null) {
            val (textColor, barColor, background) = getContributorsColors(trainingVolume.status)
            val leftText = trainingVolume.text
            result.add(
                Contributors(
                    title = "Training volume",
                    leftText = leftText,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = trainingVolume.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Training volume",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_activity_bar_color,
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
                R.color.oreo_activity_bar_color,
                com.noisefit_commans.R.drawable.back_modal_new
            )
        }
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

    fun getAvgValue(it: List<OreoActivityModel>): Int {
        var avgValue = 0
        val itemList: ArrayList<Int> = ArrayList()
        if (it.isNotEmpty()) {
            it.forEach {
                if ((it.activityScore?.value ?: 0) > 0) {
                    itemList.add(it.activityScore?.value ?: 0)
                }
            }
            avgValue = itemList.averageWithoutZero()
        }
        return avgValue

    }

    fun getStatusColors(status: String?): Int {
        return if (status.equals("warning", true)) {
            R.color.oreo_contributor_warning
        } else {
            R.color.steps_arc
        }
    }


}