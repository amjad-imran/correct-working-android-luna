package com.oreo.ui.readiness

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
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
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.Contributor
import com.oreo.data.model.sleep.HealthTrend
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.Event
import com.oreo.data.model.IrregularEventsChipModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale


@HiltViewModel
class OreoReadinessViewModel
@Inject
constructor(
    val userActivityRepository: OreoUserActivityRepository,
    val ringDataStore: RingDataStore,
    val resourcesProvider: ResourcesProvider,
    private val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    private val userRepository: OreoUserActivityRepository,
) : BaseViewModel() {

    var baseTemp: Float? = null
    var lowestHr: Int? = null
    var avgHrv: Int? = null

    private val _contributorInfo = MutableLiveData<OContributorResponseModal>()
    val contributorInfo: LiveData<OContributorResponseModal> = _contributorInfo

    private var contriData: List<Contributors>? = null

    private val _selectedChips = mutableStateListOf<String>()
    val selectedChips: List<String> get() = _selectedChips

    val hrvAlertsData = MutableLiveData<Event<Boolean>>()
    var isEventSubmitted: Boolean = false

    var date: String? = null

    val todayDate = LocalDate.now().toString()

    init {
        //selectedMasterDate = DateFormats.getCurrentDateOreoFormat()
    }

    fun updateChipSelection(chipText: String, isSelected: Boolean) {
        viewModelScope.launch {
            if (isSelected) {
                if (!_selectedChips.contains(chipText)) {
                    _selectedChips.add(chipText)
                }
            } else {
                _selectedChips.remove(chipText)
            }
        }
    }

    fun submitIrregularityEvents(
        optionalMessage: String? = null,
        onSubmitSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val reasonArray = JsonArray()
            selectedChips.forEach {
                reasonArray.add(it)
            }
            val data = localDataStore.getHrvAlerts(true)

            val req = JsonObject().apply {
                this.addProperty("current_date", getCurrentDate())
                this.addProperty("current_time", getCurrentTime())
                this.addProperty("current_value", data?.data?.currentValue)
                this.addProperty("previous_value", data?.data?.lastComparedValue)
                this.add("reason", reasonArray)
                this.addProperty("type", "hrv")

                if(selectedChips.find { it.equals("others") }!=null){
                    if (!optionalMessage.isNullOrEmpty()){
                        this.addProperty("description", optionalMessage)
                    }
                }
            }

            userRepository.submitIrregularityEvents(req).collect { resource ->
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
                                        submitIrregularityEvents(
                                            optionalMessage,
                                            onSubmitSuccess
                                        )
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            onSubmitSuccess()
                            _selectedChips.clear()
                        }
                    }
                }
            }
        }
    }

    fun loadAlertsData() {
        hrvAlertsData.value = (Event(true))
    }

    fun updateAlert(displayAlert: Boolean) {
        val alerts = localDataStore.getHrvAlerts(true)
        if (alerts == null) return

        alerts.data.apply {
            isDeleted = displayAlert
        }
        localDataStore.updateHrvAlerts(alerts)
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
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


    fun getReadinessDetailsData(selectedMasterDate: String? = null) {
        return

        /*viewModelScope.launch {
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
                                        getReadinessDetailsData(selectedMasterDate)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _readinessHistoryResponse.value = (it.reversed())
                        }
                    }
                }
            }

        }*/


    }


    var dateList = ArrayList<String>()
    fun getPrefixAndSuffixList(dataList: List<OreoReadinessModel>):
            Triple<ArrayList<ChartModel>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
        //dataList.reversed()
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
            chartModel.value = it.readinessScore?.value ?: 0
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
//            DateFormats.dateFormat3(), DateFormats.singleWeekDay()
//        )
//        val currentDateFromList = dataList.first().date
//        val currentDate = DateFormats.addDateFormat3(currentDateFromList, 1)!!
//        val prefixDatesList = DateFormats.getWeekDaysBetweenDates(
//            currentDate,
//            DateFormats.addDateFormat3(currentDate, 14)!!,
//            DateFormats.dateFormat3(), DateFormats.singleWeekDay()
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


    private fun getParsedDescriptionData(contriVersion: Int): ArrayList<String> {
        val descriptionList = ArrayList<String>()
        if (contriVersion >= 2) {
            descriptionList.add(contributorInfo.value?.sleep_score ?: "")
            descriptionList.add(contributorInfo.value?.yesterdayActivity ?: "")
            descriptionList.add(contributorInfo.value?.recoveryIndex ?: "")
            descriptionList.add(contributorInfo.value?.sleep_regularity ?: "")
            descriptionList.add(contributorInfo.value?.sleepBalance ?: "")
            descriptionList.add(contributorInfo.value?.resting_hr_bottom ?: "")
            descriptionList.add(contributorInfo.value?.activityBalance ?: "")
            descriptionList.add(contributorInfo.value?.hrvBalance ?: "")
            descriptionList.add(contributorInfo.value?.temperature_readiness_bottom ?: "")
        } else {
            descriptionList.add(contributorInfo.value?.yesterdaySleepDuration ?: "")
            descriptionList.add(contributorInfo.value?.sleepBalance ?: "")
            descriptionList.add(contributorInfo.value?.yesterdayActivity ?: "")
            descriptionList.add(contributorInfo.value?.activityBalance ?: "")
            descriptionList.add(contributorInfo.value?.hrvBalance ?: "")
            descriptionList.add(contributorInfo.value?.resting_hr_bottom ?: "")
            descriptionList.add(contributorInfo.value?.recoveryIndex ?: "")
        }
        return descriptionList
    }

    fun prepareDataForDescriptionArray(
        resultData: java.util.ArrayList<Contributors>,
        contriVersion: Int
    ): ArrayList<Contributors> {
        val contList = ArrayList<Contributors>()
        val desList = getParsedDescriptionData(contriVersion)
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
                contriType = ctList.contriType,
                hasData = hasData
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

    fun getContributorsData(dayData: OreoReadinessModel?, contriVersion: Int): List<Contributors> {
        contriData = if (contriVersion >= 2) {
            getContributorsDataVersion2(dayData)
        } else {
            getContributorsDataVersion1(dayData)
        }
        return contriData ?: ArrayList()
    }

    private fun getContributorsDataVersion2(dayData: OreoReadinessModel?): List<Contributors> {
        val result = ArrayList<Contributors>()

        //Sleep score
        if (dayData?.sleepScore != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.sleepScore.status)

            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_sleep_score),
                    leftText = dayData.sleepScore.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.sleepScore.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.SLEEP_SCORE
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_sleep_score),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.SLEEP_SCORE
                )
            )
        }

        //activity score
        if (dayData?.activityScore != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.activityScore.status)

            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_activity_score),
                    leftText = dayData.activityScore.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.activityScore.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.ACTIVITY_SCORE
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_activity_score),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.ACTIVITY_SCORE
                )
            )
        }

        //Recovery Index
        if (dayData?.recoveryIndex != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.recoveryIndex.status)

            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_recovery_index),
                    leftText = dayData.recoveryIndex.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.recoveryIndex.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.RECOVERY_INDEX
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_recovery_index),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.RECOVERY_INDEX
                )
            )
        }

        //Sleep regularity
        if (dayData?.sleepRegularity != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.sleepRegularity.status)
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_sleep_regularity),
                    leftText = dayData.sleepRegularity.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.sleepRegularity.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.SLEEP_REGULARITY
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_sleep_regularity),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.SLEEP_REGULARITY
                )
            )
        }

        //Sleep balance
        if (dayData?.sleepBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.sleepBalance.status)
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_sleep_balance),
                    leftText = dayData.sleepBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.sleepBalance.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.SLEEP_BALANCE
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_sleep_balance),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.SLEEP_BALANCE
                )
            )
        }

        //Average HR
        if (dayData?.restingHrBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.restingHrBalance.status)

            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_average_hr),
                    leftText = dayData.restingHrBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.restingHrBalance.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.AVERAGE_HR
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_average_hr),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.AVERAGE_HR
                )
            )
        }

        //Activity balance
        if (dayData?.activityBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.activityBalance.status)

            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_activity_balance),
                    leftText = dayData.activityBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.activityBalance.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.ACTIVITY_BALANCE
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_activity_balance),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.ACTIVITY_BALANCE
                )
            )
        }

        //HRV Balance
        if (dayData?.hrvBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.hrvBalance.status)
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_hrv_balance),
                    leftText = dayData.hrvBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.hrvBalance.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.HRV_BALANCE
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_hrv_balance),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.HRV_BALANCE
                )
            )
        }


        //Body Temperature
        if (dayData?.tempBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.tempBalance.status)
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_skin_temperature),
                    leftText = dayData.tempBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.tempBalance.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.SKIN_TEMP
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_skin_temperature),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 0,
                    hasData = false,
                    backgroundRes = R.drawable.back_modal_new_disabled,
                    contriType = Contributor.SKIN_TEMP
                )
            )
        }
        return result
    }

    private fun getContributorsDataVersion1(dayData: OreoReadinessModel?): List<Contributors> {
        val result = ArrayList<Contributors>()

        if (dayData?.totalSleep != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.totalSleep.status)
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_sleep_duration),
                    leftText = dayData.totalSleep.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.totalSleep.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.SLEEP_DURATION
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_sleep_duration),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    hasData = false,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new,
                    contriType = Contributor.SLEEP_DURATION
                )
            )
        }
        if (dayData?.sleepBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.sleepBalance.status)
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_sleep_balance),
                    leftText = dayData.sleepBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.sleepBalance.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.SLEEP_BALANCE
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_sleep_balance),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    hasData = false,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new,
                    contriType = Contributor.SLEEP_BALANCE
                )
            )
        }

        if (dayData?.activityScore != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.activityScore.status)

            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_activity_score),
                    leftText = dayData.activityScore.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.activityScore.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.ACTIVITY_SCORE
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_activity_score),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    hasData = false,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new,
                    contriType = Contributor.ACTIVITY_SCORE
                )
            )
        }

        if (dayData?.activityBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.activityBalance.status)

            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_activity_balance),
                    leftText = dayData.activityBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.activityBalance.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.ACTIVITY_BALANCE
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_activity_balance),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    hasData = false,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new,
                    contriType = Contributor.ACTIVITY_BALANCE
                )
            )
        }

        if (dayData?.hrvBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.hrvBalance.status)
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_hrv_balance),
                    leftText = dayData.hrvBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.hrvBalance.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.HRV_BALANCE
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_hrv_balance),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    hasData = false,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new,
                    contriType = Contributor.HRV_BALANCE
                )
            )
        }

        if (dayData?.restingHrBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.restingHrBalance.status)

            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_average_hr),
                    leftText = dayData.restingHrBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.restingHrBalance.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.AVERAGE_HR
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_average_hr),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    hasData = false,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new,
                    contriType = Contributor.AVERAGE_HR
                )
            )
        }

        if (dayData?.recoveryIndex != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.recoveryIndex.status)

            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_recovery_index),
                    leftText = dayData.recoveryIndex.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.recoveryIndex.valPrcnt ?: 0,
                    backgroundRes = background,
                    contriType = Contributor.RECOVERY_INDEX
                )
            )
        } else {
            result.add(
                Contributors(
                    title = resourcesProvider.getString(R.string.text_recovery_index),
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    hasData = false,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new,
                    contriType = Contributor.RECOVERY_INDEX
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
                R.color.readiness_warning_text,
                R.color.readiness_warning_bar,
                com.noisefit_commans.R.drawable.back_modal_new_warning
            )
        } else if (status.equals("good", true)) {
            Triple(
                R.color.white,
                R.color.oreo_readiness_bar_color,
                com.noisefit_commans.R.drawable.back_modal_new
            )
        } else if (status.equals("fair", true)) {
            Triple(
                R.color.white,
                R.color.oreo_readiness_bar_color_fair,
                com.noisefit_commans.R.drawable.back_modal_new
            )
        } else if (status.equals("optimal", true)) {
            Triple(
                R.color.oreo_readiness_text_color_optimal,
                R.color.oreo_readiness_bar_color_optimal,
                com.noisefit_commans.R.drawable.back_modal_new_optimal_readiness
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
            R.color.white_12_72
        } else if (status.equals("optimal", true)) {
            R.color.steps_arc
        } else {
            R.color.white_12_72
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

    fun hasHealthData(healthTrend: HealthTrend?): Boolean {
        if (healthTrend == null) return false
        return !(healthTrend.resp?.status.isNullOrEmpty() &&
                healthTrend.rhr?.status.isNullOrEmpty() &&
                healthTrend.bloodOxy?.status.isNullOrEmpty() &&
                healthTrend.hrv?.status.isNullOrEmpty() &&
                healthTrend.skinTemp?.status.isNullOrEmpty())

    }

    fun getHealthTrendIcon(status: String?): Int {
        val drawable: Int = if (status.equals("warning", true)) {
            R.drawable.ic_health_warning
        } else if (status.equals("good", true)) {
            R.drawable.ic_health_good
        } else if (status.equals("optimal", true)) {
            R.drawable.ic_health_optimal
        } else if (status.equals("calibrating", true)) {
            R.drawable.ic_hm_check_default
        } else {
            R.drawable.ic_health_good
        }
        return drawable
    }

    fun getIrregularityEventsChips(): List<IrregularEventsChipModel>? {
        return listOf(
            IrregularEventsChipModel(
                "had_alcohol",
                resourcesProvider.getString(R.string.text_had_alcohol)
            ),
            IrregularEventsChipModel("intense_workout",
                resourcesProvider.getString(R.string.text_intense_workout)),
            IrregularEventsChipModel("feeling_feverish",
                resourcesProvider.getString(R.string.text_feeling_feverish)),
            IrregularEventsChipModel(
                "disturbed_sleep_environment",
                resourcesProvider.getString(R.string.text_disturbed_sleep_environment)
            ),
            IrregularEventsChipModel("late_night_meal",
                resourcesProvider.getString(R.string.text_late_night_meal)),
            IrregularEventsChipModel(
                "higher_caffeine_intake",
                resourcesProvider.getString(R.string.text_higher_caffeine_intake)
            ),
            IrregularEventsChipModel("others", resourcesProvider.getString(R.string.text_other)),
        )
    }
}