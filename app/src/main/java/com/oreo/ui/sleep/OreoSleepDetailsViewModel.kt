package com.oreo.ui.sleep

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.averageWithoutZero
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.SleepStageAnalysis
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.SleepMovementType
import com.noisefit_commans.models.SleepType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributors
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.data.model.health.SleepHourlyBreakup
import com.oreo.data.model.health.SleepMovementBreakup
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OreoSleepDetailsViewModel
@Inject
constructor(
    val userActivityRepository: OreoUserActivityRepository,
    val ringDataStore: RingDataStore,
) : BaseViewModel() {

    private val _sleepHistoryResponse = MutableLiveData<List<OreoSleepModel>>()
    val sleepHistoryResponse: LiveData<List<OreoSleepModel>> = _sleepHistoryResponse

    private val _daySleepData = MutableLiveData<OreoSleepModel>()
    val daySleepData: LiveData<OreoSleepModel> = _daySleepData

    private val _contributorInfo = MutableLiveData<OContributorResponseModal>()
    val contributorInfo: LiveData<OContributorResponseModal> = _contributorInfo



    var dateList = ArrayList<String>()
    fun getPrefixAndSuffixList(dataList: List<OreoSleepModel>): Triple<ArrayList<ChartModel>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
        dataList.reversed()


        val list = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            chartModel.date = it.date
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
            chartModel.index = DateFormats.formatWeek(it.date)
            chartModel.value = it.sleepScore?.value ?: 0
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

    fun getSleepDetailsData(date: String? = null) {
        viewModelScope.launch {
            userActivityRepository.getSleepHistory(
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
                                        getSleepDetailsData(date)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            _sleepHistoryResponse.postValue(it.reversed())
                            it.firstOrNull()?.let { data ->
                                _daySleepData.postValue(data)
                            }
                        }
                    }
                }
            }
        }


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

    fun getStepAnalysisData(dayData: OreoSleepModel): ArrayList<SleepStageAnalysis> {
        val sleepStageList = ArrayList<SleepStageAnalysis>()
        sleepStageList.add(
            SleepStageAnalysis(
                "Awake",
                dayData.awake?.value ?: -1,
                dayData.awake?.valPrcnt ?: 1,
                SleepType.AWAKE
            )
        )
        sleepStageList.add(
            SleepStageAnalysis(
                "REM",
                dayData.remSleep?.value ?: -1,
                dayData.remSleep?.valPrcnt ?: 1,
                SleepType.REM
            )
        )
        sleepStageList.add(
            SleepStageAnalysis(
                "Light",
                dayData.lightSleep?.value ?: -1,
                dayData.lightSleep?.valPrcnt ?: 1,
                SleepType.LIGHT
            )
        )
        sleepStageList.add(
            SleepStageAnalysis(
                "Deep",
                dayData.deepSleep?.value ?: -1,
                dayData.deepSleep?.valPrcnt ?: 1,
                SleepType.DEEP
            )
        )

        return sleepStageList
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

    fun getDatesArray(sleepList: List<OreoSleepModel>?): List<String> {
        if (sleepList.isNullOrEmpty()) return ArrayList()
        val datesArray = ArrayList<String>()
        sleepList.forEach {
            datesArray.add(it.date)
        }
        return datesArray
    }

    fun getContributorsData(dayData: OreoSleepModel): List<Contributors> {
        val result = ArrayList<Contributors>()

        if (dayData.totalSleep != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.totalSleep!!.status)
            val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                dayData.totalSleep?.value ?: 0
            )
            result.add(
                Contributors(
                    title = "Total sleep",
                    leftText = "$hour hr $minute min",
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.totalSleep?.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Total sleep",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_sleep_bar_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData.sleepEfficiency != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.sleepEfficiency!!.status)

            result.add(
                Contributors(
                    title = "Efficiency",
                    leftText = "${dayData.sleepEfficiency?.valPrcnt}%",
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.sleepEfficiency?.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        }
        else {
            result.add(
                Contributors(
                    title = "Efficiency",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_sleep_bar_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }
        if (dayData.restFullness != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.restFullness!!.status)

            result.add(
                Contributors(
                    title = "Restfulness",
                    leftText = "${dayData.restFullness?.valPrcnt}%",
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.restFullness?.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        }
        else {
            result.add(
                Contributors(
                    title = "Restfulness",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_sleep_bar_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData.remSleep != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.remSleep!!.status)
            val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                dayData.remSleep!!.value ?: 0
            )
            val leftText = "$hour hr $minute min, ${dayData.remSleep?.valPrcnt ?: 0}%"
            result.add(
                Contributors(
                    title = "REM sleep",
                    leftText = leftText,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.remSleep?.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "REM sleep",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_sleep_bar_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData.deepSleep != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.deepSleep!!.status)
            val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                dayData.deepSleep?.value ?: 0
            )
            val leftText = "$hour hr $minute min, ${dayData.deepSleep?.valPrcnt ?: 0}%"
            result.add(
                Contributors(
                    title = "Deep sleep",
                    leftText = leftText,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.deepSleep?.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Deep sleep",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_sleep_bar_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData.latency != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.latency!!.status)
            val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(
                dayData.latency?.value ?: 0
            )
            val leftText = if (hour == 0) {
                "$minute min"
            } else {
                "$hour hr $minute min"
            }
            result.add(
                Contributors(
                    title = "Latency",
                    leftText = leftText,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.latency?.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Latency",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_sleep_bar_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }


        if (dayData.timing != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.timing!!.status)
            result.add(
                Contributors(
                    title = "Timing",
                    leftText = dayData.timing!!.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.timing!!.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Timing",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_sleep_bar_color,
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
                R.color.oreo_sleep_bar_color,
                com.noisefit_commans.R.drawable.back_modal_new
            )
        }
    }

    fun getStatusColors(status: String): Int {
        return if (status.equals("warning", true)) {
            R.color.oreo_contributor_warning
        } else {
            R.color.steps_arc
        }
    }

    fun getHourlySleepBreakup(sleepBreakup: List<SleepHourlyBreakup>?): Pair<ArrayList<SleepData.SleepDataBreakup>, CountCardData> {
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

    fun getMovementBreakup(sleepBreakup: List<SleepMovementBreakup>?): Pair<List<OreoSleepData.OreoSleepMovementDataBreakup>,
            CountCardData> {
        val countCData = CountCardData(
            type = "Movement",
            imageSourceId = 0,
            cardSourceId = 0,
            imageBgSourceId = 0
        )
        countCData.count = "_"
        countCData.countSubText = "sub"

        val startTime = DateFormats.formatDate(
            sleepBreakup?.firstOrNull()?.start_time ?: "",
            DateFormats.dateTimeFormat5,
            DateFormats.time12Meridian
        )
        val endTime = DateFormats.formatDate(
            sleepBreakup?.lastOrNull()?.end_time ?: "",
            DateFormats.dateTimeFormat5,
            DateFormats.time12Meridian
        )

        countCData.leftValue = startTime
        countCData.rightValue = endTime

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

    fun updateSelectedDate(date: String) {
        val dayData = _sleepHistoryResponse.value?.firstOrNull() {
            it.date.equals(date, false)
        }
        if (dayData != null) {
            _daySleepData.postValue(dayData!!)
        }
    }

    fun getAvgValue(it: List<OreoSleepModel>): Int {
        var avgValue = 0
        val itemList: ArrayList<Int> = ArrayList()
        if (it.isNotEmpty()) {
            it.forEach {
                if ((it.sleepScore?.value ?: 0) > 0) {
                    itemList.add(it.sleepScore?.value ?: 0)
                }
            }
            avgValue = itemList.averageWithoutZero()
        }
        return avgValue
    }

    fun getParsedDescriptionData(): ArrayList<String> {
        val descriptionList = ArrayList<String>()
        descriptionList.add(contributorInfo.value?.totalSleep ?: "")
        descriptionList.add(contributorInfo.value?.efficiency ?: "")
        descriptionList.add(contributorInfo.value?.restfulness ?: "")
        descriptionList.add(contributorInfo.value?.remSleep ?: "")
        descriptionList.add(contributorInfo.value?.deepSleep ?: "")
        descriptionList.add(contributorInfo.value?.latency ?: "")
        descriptionList.add(contributorInfo.value?.timing ?: "")
        return descriptionList


    }


}

