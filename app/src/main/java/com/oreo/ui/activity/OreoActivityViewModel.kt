package com.oreo.ui.activity

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.averageWithoutZero
import com.noisefit_commans.common.maxWithInvalidMovementValues
import com.noisefit_commans.common.maxWithoutInvalidMovementValues
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.dataConverter.OreoDayTimeDataConvertor
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributor
import com.oreo.data.model.Contributors
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.ODayTimeActivitiesDataModel
import com.oreo.data.model.OStressActivitiesDataModel
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class OreoActivityViewModel @Inject constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore,
    val sessionManager: SessionManager,
    val resourcesProvider: ResourcesProvider,
    val dayTimeDataConvertor: OreoDayTimeDataConvertor
) : BaseViewModel() {


    var contriData: List<Contributors>? = null

    var activeMinutes: Int = 0

    private val _contributorInfo = MutableLiveData<OContributorResponseModal>()
    val contributorInfo: LiveData<OContributorResponseModal> = _contributorInfo


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


    var dateList = ArrayList<String>()
    fun getPrefixAndSuffixList(dataList: List<OreoActivityModel>): Triple<ArrayList<ChartModel>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
        val list = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            var currentDayText = ""
            if (it.date == DateFormats.getCurrentDate(DateFormats.dateFormat3())) {
                currentDayText = resourcesProvider.getString(R.string.text_today_comma)
            }

            val formattedDate = if (currentDayText.isEmpty()) {
                DateFormats.getOrdinalDate(
                    it.date,
                    DateFormats.dateFormat3(),
                    NoiseFitApplicationMain.appLanguage.languageCode
                )
            } else {
                DateFormats.getOrdinalDateToday(
                    it.date,
                    DateFormats.dateFormat3(),
                    NoiseFitApplicationMain.appLanguage.languageCode
                )
            }
            chartModel.formattedDate = "$currentDayText$formattedDate"
            chartModel.date = it.date
            chartModel.index = DateFormats.formatWeek(it.date)
            chartModel.value = it.activityScore?.value ?: 0
            list.add(chartModel)
            dateList.add(it.date)
        }

        list.reverse()
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
        descriptionList.add(contributorInfo.value?.calories_goal ?: "")
        descriptionList.add(contributorInfo.value?.trainingFrequency ?: "")
        descriptionList.add(contributorInfo.value?.trainingVolume ?: "")
        return descriptionList
    }


    fun getActivityDetailsData() {
        return

        /*viewModelScope.launch {
            userActivityRepository.getActivityHistory(
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
                                        getActivityDetailsData(selectedMasterDate)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _activityHistoryResponse.value = (it.reversed())

                            //updateSelectedDate()
                        }
                    }
                }
            }

        }*/


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
            val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(
                stayActive.value ?: 0
            )
            val leftText: String = if (hour > 0) {
                "$hour hr $minute min inactivity"
            } else
                "$minute min inactivity"

            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_stay_active),
                    leftText = leftText,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = stayActive.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.STAY_ACTIVE
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_stay_active),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_activity_bar_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.STAY_ACTIVE
                )
            )
        }

        if (moveEveryHour != null) {
            val (textColor, barColor, background) = getContributorsColors(moveEveryHour.status)

            val warnings = moveEveryHour.value ?: 0
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_move_every_hour),
                    leftText = "$warnings warning${if (warnings > 1) "s" else ""}",
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = moveEveryHour.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.MOVE_EVERY_HOUR
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_move_every_hour),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_activity_bar_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.MOVE_EVERY_HOUR
                )
            )
        }

        if (caloriesGoal != null) {
            val (textColor, barColor, background) = getContributorsColors(caloriesGoal.status)

            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_calorie_goal),
                    leftText = "${caloriesGoal.value ?: 0}%",
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = caloriesGoal.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.CALORIE_GOAL
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_calorie_goal),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_activity_bar_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.CALORIE_GOAL
                )
            )
        }

        if (trainingFrequency != null) {
            val (textColor, barColor, background) = getContributorsColors(trainingFrequency.status)
            val leftText = trainingFrequency.text

            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_training_frequency),
                    leftText = leftText,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = trainingFrequency.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.TRAINING_FREQUENCY
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_training_frequency),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_activity_bar_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.TRAINING_FREQUENCY
                )
            )
        }

        if (trainingVolume != null) {
            val (textColor, barColor, background) = getContributorsColors(trainingVolume.status)
            val leftText = trainingVolume.text
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_training_volume),
                    leftText = leftText,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = trainingVolume.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.TRAINING_VOLUME
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_training_volume),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.oreo_activity_bar_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.TRAINING_VOLUME
                )
            )
        }
        return result
    }

    /**
     * textColor, barColor, background
     */
    private fun getContributorsColors(status: String): Triple<Int, Int, Int> {
        return if (status.equals("warning", true)) {
            Triple(
                R.color.oreo_activity_text_color_warning,
                R.color.oreo_activity_bar_color_warning,
                com.noisefit_commans.R.drawable.back_modal_new_warning
            )
        } else if (status.equals("good", true)) {
            Triple(
                R.color.white,
                R.color.oreo_activity_bar_color,
                com.noisefit_commans.R.drawable.back_modal_new
            )
        } else if (status.equals("fair", true)) {
            Triple(
                R.color.white,
                R.color.oreo_activity_bar_color_fair,
                com.noisefit_commans.R.drawable.back_modal_new
            )
        } else if (status.equals("optimal", true)) {
            Triple(
                R.color.oreo_activity_text_color_optimal,
                R.color.oreo_activity_bar_color_optimal,
                com.noisefit_commans.R.drawable.back_modal_new_optimal_activity
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
            val hasData = checkIfDataExists(ctList.contriType)

            val child = Contributors(
                title = ctList.title,
                leftText = ctList.leftText,
                leftTextColor = ctList.leftTextColor,
                barColor = ctList.barColor,
                barPercent = ctList.barPercent,
                backgroundRes = ctList.backgroundRes,
                description = desList[i],
                hasData = hasData,
                contriType = ctList.contriType
            )
            contList.add(child)
        }

        return contList
    }

    private fun checkIfDataExists(contriType: Contributor): Boolean {
        if (contriData == null) return false

        val data = contriData?.first {
            it.contriType == contriType
        }
        return data?.hasData ?: true
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


    fun getCombinedMovementData(
        originalList: List<Int>?,
        includeInvalid: Boolean = false
    ): List<Int> {
        if (originalList.isNullOrEmpty()) {
            return if (includeInvalid) {
                MutableList(96) { 255 }
            } else {
                MutableList(96) { 0 }

            }
        }
        val combinedList = ArrayList<Int>()
        for (i in originalList.indices step 3) {
            val endIndex = i + 3
            if (endIndex <= originalList.size) {
                val max = if (includeInvalid) {
                    originalList.subList(i, endIndex).maxWithInvalidMovementValues()
                } else {
                    originalList.subList(i, endIndex).maxWithoutInvalidMovementValues()
                }
                combinedList.add(max)
            }
        }
        return combinedList
    }

    fun getStartTimeFromPosition(position: Int): String {
        val minutes = (95 - position) * 15
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0 //set hours to zero
        calendar[Calendar.MINUTE] = 0 // set minutes to zero
        calendar[Calendar.SECOND] = 0 //set seconds to zero

        calendar.set(Calendar.MINUTE, minutes)
        return DateFormats.convertTimestampToDate(calendar.timeInMillis, DateFormats.timeFormat12())
    }

    fun getEndTimeFromPosition(position: Int): String {
        val minutes = (95 - position) * 15
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0 //set hours to zero
        calendar[Calendar.MINUTE] = 0 // set minutes to zero
        calendar[Calendar.SECOND] = 0 //set seconds to zero

        calendar.set(Calendar.MINUTE, minutes + 15)
        return DateFormats.convertTimestampToDate(calendar.timeInMillis, DateFormats.timeFormat12())
    }

    fun formattedTime(receivedTime: String): Pair<String, String> {
        val timeValue = receivedTime.split(" ")
        val time = timeValue[0]
        val timeUnit = timeValue[1].lowercase()
        return Pair(time, timeUnit)
    }

    var stressActivityData = ArrayList<ODayTimeActivitiesDataModel>()
    fun prepareStressActivityData(dayData: ServerUserHealthData) {
        val workouts = dayData.activity?.workout
        val sleep = dayData.sleep
        val dataList = ArrayList<ODayTimeActivitiesDataModel>()
        workouts?.forEach {
            dataList.add(
                ODayTimeActivitiesDataModel(
                    type = "Workout",
                    workoutData = it,
                    dateTime = "${it.date} ${it.startTime}"
                )
            )
        }

        sleep?.sleeps?.forEach {
            dataList.add(
                ODayTimeActivitiesDataModel(
                    type = "Sleep",
                    startTime = it.startTime,
                    endTime = it.endTime,
                    dateTime = it.startTime
                )
            )
        }

        sleep?.naps?.forEach { nap ->
            if (nap.date.equals(dayData.date) && !nap.isNextDayNap) {
                dataList.add(
                    ODayTimeActivitiesDataModel(
                        type = "Nap",
                        id = nap.id,
                        startTime = nap.startTime,
                        endTime = nap.endTime,
                        dateTime = "${nap.startTime}"
                    )
                )
            }
        }
        stressActivityData.clear()
        stressActivityData.addAll(dataList.sortedBy {
            LocalDateTime.parse(it.dateTime, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
        })
    }


}