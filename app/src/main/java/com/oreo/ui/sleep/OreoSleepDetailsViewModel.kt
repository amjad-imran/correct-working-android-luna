package com.oreo.ui.sleep

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
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
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributors
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.health.CommonListDataModel
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.data.model.health.SleepHourlyBreakup
import com.oreo.data.model.health.SleepMovementBreakup
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class OreoSleepDetailsViewModel
@Inject
constructor(
    val userActivityRepository: OreoUserActivityRepository,
    val ringDataStore: RingDataStore,
    val sessionManager: SessionManager,
    val resourcesProvider: ResourcesProvider
) : BaseViewModel() {


    /* private val _sleepHistoryResponse = MutableLiveData<List<OreoSleepModel>>()
     val sleepHistoryResponse: LiveData<List<OreoSleepModel>> = _sleepHistoryResponse*/

    /*private val _daySleepData = MutableLiveData<OreoSleepModel>()
    val daySleepData: LiveData<OreoSleepModel> = _daySleepData*/

    private val _contributorInfo = MutableLiveData<OContributorResponseModal>()
    val contributorInfo: LiveData<OContributorResponseModal> = _contributorInfo


    init {
        //selectedMasterDate = DateFormats.getCurrentDateOreoFormat()
    }

    fun getPrefixAndSuffixList(dataList: List<OreoSleepModel>): Triple<ArrayList<ChartModel>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
        //dataList.reversed()

        val dateList = ArrayList<String>()

        val list = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            chartModel.date = it.date
            var currentDayText = ""
            if (it.date == DateFormats.getCurrentDate(DateFormats.dateFormat3)) {
                currentDayText = "Today, "
            }
            val formattedDate = if (currentDayText.isEmpty()) {
                DateFormats.getOrdinalDate(
                    it.date,
                    DateFormats.dateFormat3
                )
            } else {
                DateFormats.getOrdinalDateToday(
                    it.date,
                    DateFormats.dateFormat3,
                )
            }
            chartModel.formattedDate = "$currentDayText$formattedDate"
            chartModel.index = DateFormats.formatWeek(it.date)
            chartModel.value = it.sleepScore?.value ?: 0
            list.add(chartModel)
            dateList.add(it.date)
        }

        list.reverse()
        val lastDateFromList = dataList.first().date
        val lastDate = DateFormats.subtractDateFormat3(lastDateFromList, 1)!!
        val suffixDatesList = DateFormats.getWeekDaysBetweenDates(
            DateFormats.subtractDateFormat3(lastDate, 14)!!, lastDate,
            DateFormats.dateFormat3, DateFormats.singleWeekDay
        )
        val currentDateFromList = dataList.last().date
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


    fun getSleepDetailsData() {
        return
        /*viewModelScope.launch {
            userActivityRepository.getSleepHistory(
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
                                        getSleepDetailsData(selectedMasterDate)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            _sleepHistoryResponse.value = (it.reversed())
                            *//* if (selectedDate == null) {
                                 it.firstOrNull()?.let { data ->
                                     selectedDate = data.date
                                 }
                             }*//*

                        }
                    }
                }
            }
        }*/


    }

    fun getInfoValueByKey(type: String) {

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
                dayData.awake?.valPrcnt ?: 0,
                SleepType.AWAKE
            )
        )
        sleepStageList.add(
            SleepStageAnalysis(
                "REM",
                dayData.remSleep?.value ?: -1,
                dayData.remSleep?.value_percentage ?: 0,
                SleepType.REM
            )
        )
        sleepStageList.add(
            SleepStageAnalysis(
                "Light",
                dayData.lightSleep?.value ?: -1,
                dayData.lightSleep?.valPrcnt ?: 0,
                SleepType.LIGHT
            )
        )
        sleepStageList.add(
            SleepStageAnalysis(
                "Deep",
                dayData.deepSleep?.value ?: -1,
                dayData.deepSleep?.value_percentage ?: 0,
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
            val leftText: String = if (hour > 0) {
                "$hour hr $minute min"
            } else
                "$minute min"
            result.add(
                Contributors(
                    title = "Sleep duration",
                    leftText = leftText,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.totalSleep?.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Sleep duration",
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
        } else {
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
                    leftText = "${dayData.restFullness?.text}",
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.restFullness?.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
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

            val leftText: String = if (hour > 0) {
                "$hour hr $minute min, ${dayData.remSleep?.value_percentage ?: 0}%"
            } else {
                "$minute min, ${dayData.remSleep?.value_percentage ?: 0}%"
            }
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
            val leftText: String = if (hour > 0) {
                "$hour hr $minute min, ${dayData.deepSleep?.value_percentage ?: 0}%"
            } else {
                "$minute min, ${dayData.deepSleep?.value_percentage ?: 0}%"
            }
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
        val color: Int = if (status.equals("warning", true)) {
            R.color.oreo_contributor_warning
        } else if (status.equals("good", true)) {
            R.color.white_12_72
        } else if (status.equals("optimal", true)) {
            R.color.steps_arc
        } else {
            R.color.white_12_72
        }
        return color
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
              DateFormats.dateTimeFormat5,
              DateFormats.time12Meridian
          )
          val endTime = DateFormats.formatDate(
              sleepBreakup?.lastOrNull()?.end_time ?: "",
              DateFormats.dateTimeFormat5,
              DateFormats.time12Meridian
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

    /*fun updateSelectedDate(selectedDate: String?): String? {
        var returnSelectedDate: String? = null

        val dayData = _sleepHistoryResponse.value?.firstOrNull() {
            it.date.equals(selectedDate, false)
        }
        if (dayData != null) {
            _daySleepData.postValue(dayData)
        } else {
            _sleepHistoryResponse.value?.lastOrNull()?.let { data ->
                LOGS.w("moveToPosition selected Date new $selectedDate")
                returnSelectedDate = data.date
                LOGS.w("moveToPosition selected Date new set $selectedDate")
                _daySleepData.postValue(data)
            }
        }
        return returnSelectedDate
    }*/

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
        descriptionList.add(contributorInfo.value?.sleep_duration ?: "")
        descriptionList.add(contributorInfo.value?.sleep_efficiency ?: "")
        descriptionList.add(contributorInfo.value?.restfulness ?: "")
        descriptionList.add(contributorInfo.value?.remSleep ?: "")
        descriptionList.add(contributorInfo.value?.deepSleep ?: "")
        descriptionList.add(contributorInfo.value?.latency ?: "")
        descriptionList.add(contributorInfo.value?.timing ?: "")
        return descriptionList


    }

    fun getDummyBreakUpDataForTimeDisplay(): ArrayList<Int> {
        val dummyList = ArrayList<Int>()
        for (i in 0..287) {
            //dummyList.add(0)
            dummyList.add((10..140).random())
        }
        return dummyList

    }

    fun getBloodOxygenNudge(oxy: CommonListDataModel?): String {
        val avg = oxy?.avg
        if (avg == null || avg == 0) return ""


        if (avg < 95) {
            return resourcesProvider.getString(R.string.text_b_o_95_less)
        }

        var count = 0
        oxy.value.forEach {
            if (it != 0 && it != 255) {
                val diff = abs(it - avg)
                if (diff >= 3) {
                    count++
                }
            }
        }

        return when (count) {
            in 0..2 -> resourcesProvider.getString(R.string.text_bo_0_2)
            in 3..5 -> resourcesProvider.getString(R.string.text_bo_3_5)
            else -> resourcesProvider.getString(R.string.text_bo_else)
        }
    }


}

