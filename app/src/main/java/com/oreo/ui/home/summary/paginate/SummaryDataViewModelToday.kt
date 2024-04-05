package com.oreo.ui.home.summary.paginate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.noisefit.data.dataConverter.DataConverter
import com.noisefit.data.local.db.CacheResult
import com.oreo.data.model.AppUpdateModel
import com.oreo.data.model.OtaUpdateModel
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UpdateRepository
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.enums.DashInfoCard
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.data.model.User
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanN
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.ScreenUtils
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.dataConverter.OreoStressDataConvertor
import com.oreo.data.model.AlertType
import com.oreo.data.model.ChartModel
import com.oreo.data.model.DashAlert
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.OreoNapDetailsDataModel
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.SlideUpNapScoreDataModel
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.TrendsData
import com.oreo.data.model.VideoInfoType
import com.oreo.data.model.health.ODashboardActivityModel
import com.oreo.data.model.health.ODashboardActivityScoreModel
import com.oreo.data.model.health.ODashboardReadinessModel
import com.oreo.data.model.health.ODashboardReadinessScoreModel
import com.oreo.data.model.health.ODashboardSleepModel
import com.oreo.data.model.health.ODashboardSleepScoreModel
import com.oreo.data.model.health.SleepHourlyBreakup
import com.oreo.data.repository.abstraction.OreoSyncRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SummaryDataViewModelToday @Inject constructor(
    val userRepository: OreoUserActivityRepository,
    val ringDataStore: RingDataStore,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val dataConverter: DataConverter,
    val screenUtils: ScreenUtils,
    val watchDataStore: WatchDataStore,
    private val syncRepository: OreoSyncRepository,
    val oreoStressDataConvertor: OreoStressDataConvertor,
    val userActivityRepository: OreoUserActivityRepository,
    val updateRepository: UpdateRepository,
    private val userHealthDataDataSource: OreoUserHealthDataDataSource
) : BaseViewModel() {


    var date: String? = null

    val stateHeaderCard = MutableLiveData<Pair<String, String>>()//Name,Date
    val healthOverviewData = MutableLiveData<ArrayList<OHealthOverview>>()
    val viewedCardsData = MutableLiveData<ArrayList<OHealthOverview>>()
    val statePairDeviceCard = MutableLiveData<Boolean>()
    val stateDashRingBattery = MutableLiveData<Pair<Boolean, ColorFitDevice?>>()
    val stateDashAlerts = MutableLiveData<HashMap<AlertType, DashAlert>>()
    val stateGoogleFitCard = MutableLiveData<Boolean>()
    val napsList = MutableLiveData<List<OreoNapData>>()


    var contributorInfo: OContributorResponseModal? = null
    val hrInfo = MutableLiveData<Event<String>>()
    var sleepScoreInfo = MutableLiveData<Event<String>>()
    var readinessScoreInfo = MutableLiveData<Event<String>>()
    var activityScoreInfo = MutableLiveData<Event<String>>()
    var appUpdateInfo = MutableLiveData<AppUpdateModel?>()
    var otaUpdateInfo = MutableLiveData<OtaUpdateModel?>()

    val stateWorkouts = MutableLiveData<List<OActivityListModal>>()


    val stateReadinessAvgCard = MutableLiveData<ODashboardReadinessScoreModel?>()
    val stateSleepAvgCard =
        MutableLiveData<Pair<ODashboardSleepScoreModel?, ODashboardActivityScoreModel?>>()
    val stateHeartRateCard = MutableLiveData<OHealthOverview.HeartRate?>()

    var user: User? = null
    var registerDate: Int = -1
    var onNapAddSuccess = MutableLiveData<Event<OreoNapDetailsDataModel>>()


    fun setRingBatteryInfoState() {
        stateDashRingBattery.postValue(Pair(false, null))
        viewModelScope.launch(Dispatchers.IO) {
            localDataStore.setBatteryAlertShown()
        }
    }


    fun initTodayData() {

        viewModelScope.launch(Dispatchers.IO) {
            user = localDataStore.getUser()/*stateHeaderCard.postValue(
                Pair(
                    getGreetingMessageValue(),
                    DateFormats.getCurrentDate(DateFormats.dateTimeFormatWithWeekWithoutYear)
                )
            )*/
            val device = getDeviceConnected()
            statePairDeviceCard.postValue(device == null)
            stateHeartRateCard.postValue(userRepository.getSummaryHRHealthOverview().apply {
                if (device == null) {
                    this?.measureState = TapMeasureState.NO_DEVICE
                }
            })



            handleGoogleFitCard()

        }

        updateAlerts()
    }

    private fun handleGoogleFitCard() {
        val showGoogleFit = ringDataStore.getDeviceFeatures()?.googleFit

        if (showGoogleFit == 1) {
            val isGoogleFitEnabled = localDataStore.isEnableGoogleFit()
            val isGoogleFitCrossed = ringDataStore.isGoogleFitCrossed()

            if (isGoogleFitEnabled) {
                stateGoogleFitCard.postValue(false)
            } else {
                if (isGoogleFitCrossed) {
                    stateGoogleFitCard.postValue(false)
                } else {
                    stateGoogleFitCard.postValue(true)
                }
            }
        } else {
            stateGoogleFitCard.postValue(false)
        }
    }

    fun getGreetingMessageValue(): String {
        return "${getGreetingMessage()}, ${
            user?.getOnlyFirstName()?.trim()?.ifEmpty { "Stranger" }
        }"
    }

    private fun getGreetingMessage(): String {
        val currentTime = DateFormats.getTimeFormat()
        LOGS.d("TIME_TEST", "currentTime $currentTime")
        if (DateFormats.isTimeBetween(currentTime, "04:00", "11:59")) {
            return "Good morning"
        } else if (DateFormats.isTimeBetween(currentTime, "12:00", "16:59")) {
            return "Good afternoon"
        } else if (DateFormats.isTimeBetween(currentTime, "17:00", "20:59")) {
            return "Good evening"
        } else if (DateFormats.isTimeBetween(
                currentTime, "21:00", "23:59"
            ) || DateFormats.isTimeBetween(currentTime, "00:00", "03:59")
        ) {
            return "Hi"
        }

        return "Hi"
    }


    fun updateAlerts() {
        val dashAlert = HashMap<AlertType, DashAlert>()

        val btState = sessionManager.bluetoothStateDash.value
        val devicePaired = ringDataStore.getRingDevice()
        if (btState == false && devicePaired != null) {
            if (sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                dashAlert[AlertType.BLUETOOTH] =
                    DashAlert("Authorize Bluetooth connectivity for Luna", false)
            }
        }

        /* if (sessionManager.forceOtaResponseRing != null) {
             dashAlert[AlertType.OTA_UPDATE] =
                 DashAlert("Ring firmware update available", false)
         }*/


        stateDashAlerts.postValue(dashAlert)
    }


    fun parseHealthData(healthData: ServerUserHealthData, trendsData: TrendsData?) {

        viewModelScope.launch(Dispatchers.IO) {

            val userActivities = ArrayList<OHealthOverview>()
            val viewedCardsData = ArrayList<OHealthOverview>()

            val autoSportCount = userRepository.getSummaryAutoWorkoutCount()
            if (autoSportCount > 0) {
                userActivities.add(OHealthOverview.AutoSport(autoSportCount))
            }


            handleInfoCards(healthData, trendsData, userActivities, viewedCardsData)

            val readinessModel = ODashboardReadinessModel(
                readinessScore = healthData.readiness?.readinessScore?.value,
                status = healthData.readiness?.readinessScore?.text?.capitalizeWords(),
                nudges = healthData.readiness?.dashNudges,
                readinessNapScoreImpact = healthData.readiness?.readinessNapScoreImpact,
                noOfNaps = healthData.readiness?.noOfNaps
            )


            val newSleepArray = dataConverter.mergeSleepData(
                healthData.sleep?.hourly_breakup, healthData.sleep?.naps
            )


            val sleepModel = ODashboardSleepModel(
                sleepScore = healthData.sleep?.sleepScore?.value,
                totalSleep = healthData.sleep?.totalSleep?.value,
                restingHr = healthData.sleep?.restingHr?.value,
                sleepStage = healthData.sleep?.hourly_breakup ?: ArrayList(),
                status = healthData.sleep?.sleepScore?.text?.capitalizeWords(),
                startTime = newSleepArray?.firstOrNull()?.start_time ?: "",
                endTime = newSleepArray?.lastOrNull()?.end_time ?: "",
                sleepNapScoreImpact = healthData.sleep?.sleepNapScoreImpact ?: 0,
                noOfNaps = healthData.sleep?.noOfNaps ?: 0
            )
            val activityModal = ODashboardActivityModel(
                activityScore = healthData.activity?.activityScore?.value,
                activeCalories = healthData.activity?.activeCalories ?: 0,
                inactiveMinutes = healthData.activity?.activityContributors?.stayActive?.value,
                status = healthData.activity?.activityScore?.level?.capitalizeWords(),
                nudges = healthData.activity?.dash_nudges
            )

            val nap = healthData.sleep?.naps ?: ArrayList()


            val daySlot = getDaySlot()
            LOGS.d("TIME_TEST", "daySlot $daySlot")

            when (daySlot) {
                0 -> {
                    //sleep
                    if (healthData.sleep?.sleepScore != null) {
                        if (registerDate != 0) {
                            healthData.readiness?.let {
                                if ((readinessModel.readinessScore ?: 0) > 0) {
                                    userActivities.add(OHealthOverview.Readiness(readinessModel))
                                }
                            }
                            if ((sleepModel.totalSleep ?: 0) > 0) {
                                userActivities.add(
                                    OHealthOverview.Sleep(
                                        sleepModel,
                                        makeSleepArray(newSleepArray),
                                        newSleepArray?.firstOrNull()?.start_time ?: "",
                                        newSleepArray?.lastOrNull()?.end_time ?: ""
                                    )
                                )
                            }
                        }
                    } else {
                        userActivities.add(OHealthOverview.SleepWaiting)
                    }
                    if (nap.isNotEmpty()) {
                        userActivities.add(OHealthOverview.NapDashCard(nap, healthData.date))
                    }
                }

                1 -> {

                    //sleep
                    if (healthData.sleep?.sleepScore != null) {
                        if (registerDate != 0) {
                            healthData.readiness?.let {
                                if ((readinessModel.readinessScore ?: 0) > 0) {
                                    userActivities.add(OHealthOverview.Readiness(readinessModel))
                                }
                            }
                            if ((sleepModel.totalSleep ?: 0) > 0) {
                                userActivities.add(
                                    OHealthOverview.Sleep(
                                        sleepModel,
                                        makeSleepArray(newSleepArray),
                                        newSleepArray?.firstOrNull()?.start_time ?: "",
                                        newSleepArray?.lastOrNull()?.end_time ?: ""
                                    )
                                )
                            }
                        }
                    } else {
                        userActivities.add(OHealthOverview.SleepWaiting)
                    }
                    if (nap.isNotEmpty()) {
                        userActivities.add(OHealthOverview.NapDashCard(nap, healthData.date))
                    }


                    //Activity
                    if ((healthData.activity?.activeCalories ?: 0) > 0) {
                        val activeCalories = healthData.activity?.activeCalories ?: 0
                        if (activeCalories in 1..49) {
                            val caloriesGoal = user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.ActivityMinimal(
                                    activityModal, caloriesGoal
                                )
                            )
                        } else if (activeCalories >= 50) {
                            val caloriesGoal = user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.Activity(
                                    activityModal, caloriesGoal
                                )
                            )
                        } else {
                        }
                    }
                }

                2 -> {
                    if (registerDate != 0) {
                        healthData.readiness?.let {
                            if ((readinessModel.readinessScore ?: 0) > 0) {
                                userActivities.add(OHealthOverview.Readiness(readinessModel))
                            }
                        }

                        healthData.sleep?.let {
                            if ((sleepModel.totalSleep ?: 0) > 0) {
                                userActivities.add(
                                    OHealthOverview.Sleep(
                                        sleepModel,
                                        makeSleepArray(newSleepArray),
                                        newSleepArray?.firstOrNull()?.start_time ?: "",
                                        newSleepArray?.lastOrNull()?.end_time ?: ""
                                    )
                                )
                            }
                        }
                    }
                    if (nap.isNotEmpty()) {
                        userActivities.add(OHealthOverview.NapDashCard(nap, healthData.date))
                    }

                    if ((healthData.activity?.activeCalories ?: 0) > 0) {

                        val activeCalories = healthData.activity?.activeCalories ?: 0
                        if (activeCalories in 0..49) {
                            val caloriesGoal = user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.ActivityMinimal(
                                    activityModal, caloriesGoal
                                )
                            )
                        } else {
                            val caloriesGoal = user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.Activity(
                                    activityModal, caloriesGoal
                                )
                            )
                        }
                    }


                }

                else -> {
                    if ((healthData.activity?.activeCalories ?: 0) > 0) {

                        val activeCalories = healthData.activity?.activeCalories ?: 0
                        if (activeCalories in 0..49) {
                            val caloriesGoal = user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.ActivityMinimal(
                                    activityModal, caloriesGoal
                                )
                            )
                        } else {
                            val caloriesGoal = user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.Activity(
                                    activityModal, caloriesGoal
                                )
                            )
                        }
                    }

                    if (registerDate != 0) {
                        if (healthData.sleep?.sleepScore != null) {
                            if ((sleepModel.totalSleep ?: 0) > 0) {
                                userActivities.add(
                                    OHealthOverview.SleepMinimal(
                                        sleepModel, makeSleepArray(newSleepArray)
                                    )
                                )
                            }
                            if (nap.isNotEmpty()) {
                                userActivities.add(
                                    OHealthOverview.NapDashCard(
                                        nap, healthData.date
                                    )
                                )
                            }

                            if ((readinessModel.readinessScore ?: 0) > 0) {

                                healthData.readiness?.let {
                                    userActivities.add(
                                        OHealthOverview.ReadinessMinimal(
                                            readinessModel
                                        )
                                    )
                                }
                            }

                        } else {
                            if ((sleepModel.totalSleep ?: 0) > 0) {
                                healthData.sleep?.let {
                                    userActivities.add(
                                        OHealthOverview.Sleep(
                                            sleepModel,
                                            makeSleepArray(healthData.sleep?.hourly_breakup),
                                            healthData.sleep?.hourly_breakup?.firstOrNull()?.start_time
                                                ?: "",
                                            healthData.sleep?.hourly_breakup?.lastOrNull()?.end_time
                                                ?: ""
                                        )
                                    )
                                }
                            }
                            if (nap.isNotEmpty()) {
                                userActivities.add(
                                    OHealthOverview.NapDashCard(
                                        nap, healthData.date
                                    )
                                )
                            }
                            if ((readinessModel.readinessScore ?: 0) > 0) {
                                healthData.readiness?.let {
                                    userActivities.add(OHealthOverview.Readiness(readinessModel))
                                }
                            }
                        }
                    }
                }
            }
            val combinedData = oreoStressDataConvertor.getStressCombinedData(healthData)
            userActivities.add(
                OHealthOverview.StressGraph(
                    combinedData,
                    healthData.stress?.stressValue?.value ?: 0,
                    healthData.stress?.stressValue?.lastUpdated ?: 0L,
                    getStressStatus(healthData.stress?.stressValue?.value ?: 0),
                    true
                )
            )


            stateSleepAvgCard.postValue(
                Pair(
                    trendsData?.sleepScoreAvg, trendsData?.activityScoreAvg
                )
            )
            stateReadinessAvgCard.postValue(trendsData?.readinessScoreAvg)

            this@SummaryDataViewModelToday.viewedCardsData.postValue(viewedCardsData)
            healthOverviewData.postValue(userActivities)

            val device = ringDataStore.getRingDevice()
            stateHeartRateCard.postValue(userRepository.getSummaryHRHealthOverview().apply {
                if (device == null) {
                    this?.measureState = TapMeasureState.NO_DEVICE
                }
            })

            stateWorkouts.postValue(healthData.activity?.workout ?: ArrayList())
            loadNapsToConfirm()

        }
    }

    fun getStressStatus(value: Int?): String {
        return when (value) {
            0 -> ""
            in 1..34 -> "Calm"
            in 35..69 -> "Focussed"
            in 70..100 -> "Stressed"
            else -> ""
        }
    }

    fun loadNapsToConfirm() {
        viewModelScope.launch(Dispatchers.IO) {
            val naps = userActivityRepository.getNapsToConfirm()
            napsList.postValue(naps ?: ArrayList())
        }
    }


    private fun handleHrFormat(time: Int): String {

        if (time == 1 || time == 24) {
            return "12 am"
        }


        var hour = time
        var suffix = ""
        if (hour > 11) {
            suffix = "pm"
            if (hour > 12) hour -= 12;
        } else {
            suffix = "am"
            if (hour == 0) hour = 12;
        }
        return "$hour $suffix"
    }


    private fun handleInfoCards(
        data: ServerUserHealthData,
        trendsData: TrendsData?,
        userActivities: ArrayList<OHealthOverview>,
        viewedCardsData: ArrayList<OHealthOverview>,
    ) {
        val registerDays = (registerDate ?: 0)
        if (registerDays == -1) return

        if (registerDays < 7) {

            if (registerDays == 0) {
                trendsData?.welcome?.welcome?.let {
                    userActivities.add(OHealthOverview.InfoRingWelcome(it))
                }
            }

            val cardClickState = localDataStore.getDashCardClickState()

            trendsData?.welcome?.care?.let {
                if (registerDays > 0) {
                    viewedCardsData.add(OHealthOverview.InfoRingCare(it))
                } else {
                    if (cardClickState[DashInfoCard.CARE] == false) {
                        userActivities.add(OHealthOverview.InfoRingCare(it))
                    } else {
                        viewedCardsData.add(OHealthOverview.InfoRingCare(it))
                    }
                }

            }

            trendsData?.welcome?.sleep_media?.let {
                if (cardClickState[DashInfoCard.SLEEP] == false) {
                    userActivities.add(OHealthOverview.InfoVideo(VideoInfoType.SLEEP, it))
                } else {
                    viewedCardsData.add(OHealthOverview.InfoVideo(VideoInfoType.SLEEP, it))
                }
            }

            trendsData?.welcome?.activity_media?.let {
                if (cardClickState[DashInfoCard.ACTIVITY] == false) {
                    userActivities.add(OHealthOverview.InfoVideo(VideoInfoType.ACTIVITY, it))
                } else {
                    viewedCardsData.add(OHealthOverview.InfoVideo(VideoInfoType.ACTIVITY, it))
                }
            }

            trendsData?.welcome?.readiness_media?.let {
                if (cardClickState[DashInfoCard.READINESS] == false) {
                    userActivities.add(OHealthOverview.InfoVideo(VideoInfoType.READINESS, it))
                } else {
                    viewedCardsData.add(OHealthOverview.InfoVideo(VideoInfoType.READINESS, it))
                }
            }


        }
    }


    /**
     * Return day slots
     * 1->00:00 - 08:00
     * 2->08:00 - 12:000
     * 3->12:00 - 24:00
     */
    private fun getDaySlot(): Int {
        val currentTime = DateFormats.getTimeFormat()

        LOGS.d("TIME_TEST", "currentTime getDaySLot $currentTime")

        return if (DateFormats.isTimeBetween(currentTime, "00:00", "03:59")) {
            0
        } else if (DateFormats.isTimeBetween(currentTime, "04:00", "07:59")) {
            1
        } else if (DateFormats.isTimeBetween(currentTime, "08:00", "11:59")) {
            2
        } else {
            3
        }
    }

    private fun makeSleepArray(data: List<SleepHourlyBreakup>?): ArrayList<SleepData.SleepDataBreakup> {
        val sleepArray: ArrayList<SleepData.SleepDataBreakup> = ArrayList()

        if (data.isNullOrEmpty()) {
            return sleepArray
        }

        var duration = 0

        data.forEachIndexed { index, data1 ->
            val type = data1.sleep_type


            if (type?.lowercase() == "awake") {
                if (duration != 0) {

                    sleepArray.add(
                        SleepData.SleepDataBreakup(
                            startTime = data1.start_time,
                            endTime = data1.end_time,
                            sleepType = "DEEP",
                            duration = duration
                        )
                    )
                    duration = 0
                }
                sleepArray.add(
                    SleepData.SleepDataBreakup(
                        startTime = data1.start_time,
                        endTime = data1.end_time,
                        sleepType = "AWAKE",
                        duration = data1.duration ?: 0
                    )
                )
            } else {
                duration += (data1.duration?.toInt()) ?: 0
            }

            if (index == data.size - 1 && duration != 0) {

                sleepArray.add(
                    SleepData.SleepDataBreakup(
                        startTime = data1.start_time,
                        endTime = data1.end_time,
                        sleepType = "DEEP",
                        duration = duration
                    )
                )
                duration = 0
            }


        }


        return sleepArray
    }


    fun convertIntToChartModel(data: List<Int>?): ArrayList<ChartModel> {
        val list = ArrayList<ChartModel>()
        val chartModel1 = ChartModel()
        chartModel1.date = ""
        chartModel1.index = ""
        chartModel1.value = 0
        list.add(chartModel1)
        data?.forEach {
            val chartModel = ChartModel()
            var value = it
            if (value < 0) {
                value = 0
            }
            chartModel.value = value//(10..100).random()
            chartModel.date = ""
            chartModel.index = ""
            list.add(chartModel)
        }

        return list
    }

    fun isDeviceConnected(): Boolean {
        if (getDeviceConnected() == null) {
            return false
        }

        if (sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
            return true
        }
        return false
    }

    fun getDeviceConnected(): ColorFitDevice? {
        return ringDataStore.getRingDevice()
    }

    fun measureHr(status: Boolean) {
        stateHeartRateCard.value?.measureState = TapMeasureState.MEASURING
        stateHeartRateCard.postValue(stateHeartRateCard.value)


        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetManualMeasurement(
                ManualMeasureType.HEART_RATE, status
            )
        )

    }

    fun updateManualValue() {
        val manualMeasurement = ringDataStore.getManualMeasurementValue()
        if (manualMeasurement != null && manualMeasurement.manualMeasureType == ManualMeasureType.HEART_RATE) {


            if (manualMeasurement.isError) {
                stateHeartRateCard.value?.measureState = TapMeasureState.ERROR
            } else {
                if (manualMeasurement.isMeasuring) {
                    stateHeartRateCard.value?.measureState = TapMeasureState.MEASURING
                } else {
                    stateHeartRateCard.value?.measureState = TapMeasureState.LAST_MEASURED
                    stateHeartRateCard.value?.lastTime = "Last measured just now"
                }
                stateHeartRateCard.value?.value = manualMeasurement.value.toString()
            }
            stateHeartRateCard.postValue(stateHeartRateCard.value)
        }
    }


    /**
     *hr,sleep,activity,readiness
     */
    fun getContributorInfo(callerName: String) {
        if (contributorInfo != null) {
            when (callerName) {
                "hr" -> hrInfo.postValue(Event(contributorInfo!!.hr_graph))
                "sleep" -> sleepScoreInfo.postValue(Event(contributorInfo!!.sleep_score))
                "activity" -> activityScoreInfo.postValue(Event(contributorInfo!!.activity_score))
                "readiness" -> readinessScoreInfo.postValue(Event(contributorInfo!!.readiness_score))
            }
            return
        }

        viewModelScope.launch {
            userActivityRepository.getContributorDetailsInfo(
                "dashboard"
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
                                        getContributorInfo(callerName)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            contributorInfo = it
                            when (callerName) {
                                "hr" -> hrInfo.postValue(Event(contributorInfo!!.hr_graph))
                                "sleep" -> sleepScoreInfo.postValue(Event(contributorInfo!!.sleep_score))
                                "activity" -> activityScoreInfo.postValue(Event(contributorInfo!!.activity_score))
                                "readiness" -> readinessScoreInfo.postValue(Event(contributorInfo!!.readiness_score))
                            }
                        }
                    }
                }
            }
        }


    }


    fun checkForNewAppVersion() {
        viewModelScope.launch(Dispatchers.IO) {

            val callApi = postOfflineAppUpdateData()
            if (callApi.not()) return@launch

            checkAppVersionServer()

        }


    }

    fun checkForNewOtaVersion() {
        viewModelScope.launch(Dispatchers.IO) {
            val device = ringDataStore.getRingDevice()
            if (device == null) {
                otaUpdateInfo.postValue(null)
                return@launch
            }

            val callApi = postUpdateOtaDataOffline()

            if (callApi.not()) return@launch

            sessionManager.postFirmwareDetailsOnDash = true
            sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)


        }


    }


    fun postOfflineAppUpdateData(): Boolean {
        val appUpdateObj = localDataStore.getNewAppVersion()
        if (appUpdateObj?.first != null) {
            val lastSaveTimeStamp = appUpdateObj.third

            val isMoreThan2 = lastSaveTimeStamp.checkTimeDifferenceMoreThanN(2)
            if (isMoreThan2) {
                localDataStore.cleaNewAppVersion()
                return true
            }

            if (appUpdateObj.second != BuildConfig.VERSION_CODE) {
                localDataStore.cleaNewAppVersion()
                return true
            }

            val remindDate = localDataStore.getAppRemindDate()

            if (remindDate == null) {
                val obj = Gson().fromJson<AppUpdateModel>(appUpdateObj.first)
                appUpdateInfo.postValue(obj)
            } else if (!remindDate.equals(DateFormats.getCurrentDate())) {
                val obj = Gson().fromJson<AppUpdateModel>(appUpdateObj.first)
                appUpdateInfo.postValue(obj)
            }
            return false
        } else {
            val lastCheckTimestamp = localDataStore.getAppVersionCheckTimeStamp()
            return if (lastCheckTimestamp == 0L) {
                true
            } else {
                lastCheckTimestamp.checkTimeDifferenceMoreThanN(2)
            }
        }
    }

    private fun postUpdateOtaDataOffline(): Boolean {
        val firmwareObj = ringDataStore.getNewOtaVersion()
        LOGS.d("postUpdateOtaDataOffline ${Gson().toJson(firmwareObj)}")
        if (firmwareObj?.first != null) {

            val lastSaveTimeStamp = firmwareObj.third

            val isMoreThan2 = lastSaveTimeStamp.checkTimeDifferenceMoreThanN(2)
            if (isMoreThan2) {
                ringDataStore.cleaNewOtaVersion()
                return true
            }

            val remindDate = ringDataStore.getOtaRemindDate()

            if (remindDate == null) {
                val obj = Gson().fromJson<OtaUpdateModel>(firmwareObj.first)
                otaUpdateInfo.postValue(obj)
            } else if (!remindDate.equals(DateFormats.getCurrentDate())) {
                val obj = Gson().fromJson<OtaUpdateModel>(firmwareObj.first)
                otaUpdateInfo.postValue(obj)
            }
            return false
        } else {
            val lastCheckTimestamp = ringDataStore.getOtaVersionCheckTimeStamp()
            LOGS.d(
                "" + " ${lastCheckTimestamp}"
            )
            return if (lastCheckTimestamp == 0L) {
                true
            } else {
                lastCheckTimestamp.checkTimeDifferenceMoreThanN(2)
            }
        }
    }


    fun checkAppVersionServer() {
        viewModelScope.launch(Dispatchers.IO) {

            val request = getAppVersionRequest()
            updateRepository.checkAppVersionV2(request).collect { resource ->
                when (resource) {

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            updateRepository.saveNewAppVersion(
                                it.appVersion, BuildConfig.VERSION_CODE
                            )
                            postOfflineAppUpdateData()
                        }
                    }

                    else -> {

                    }
                }
            }
        }
    }

    /**
     * WatchInfoGlobals.firmwareVersionNumberRing,
     *                                     WatchInfoGlobals.firmwareDeviceIdRing
     */
    fun checkOtaVersionServer(pair: Pair<Int, Int>) {
        viewModelScope.launch(Dispatchers.IO) {

            val request = getOtaVersionRequest(pair)
            updateRepository.checkAppVersionV2(request).collect { resource ->
                when (resource) {

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            updateRepository.saveNewOtaVersion(it.firmwareVersion, pair?.first)

                            postUpdateOtaDataOffline()
                        }
                    }

                    else -> {

                    }
                }
            }
        }


    }


    private fun getAppVersionRequest(): JsonObject {
        return JsonObject().apply {
            addProperty("platform", "android")
            addProperty("app_version", BuildConfig.VERSION_CODE)
        }
    }

    private fun getOtaVersionRequest(pair: Pair<Int, Int>): JsonObject {
        val ringDevice = ringDataStore.getRingDevice()
        val deviceType = ringDevice?.deviceType

        return JsonObject().apply {
            addProperty(
                "version", pair.first
            )
            addProperty(
                "firmware_id", pair.second
            )
            addProperty("mac", ringDevice?.address)
            addProperty("device_type", deviceType)
            addProperty(
                "isOTARequired", sessionManager.needDfuUpdate.value?.peekContent() ?: false
            )
            addProperty("platform", "android")
        }
    }


    fun markWorkoutSyncedAll() {
        viewModelScope.launch {
            syncRepository.markWorkoutSyncedAll().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }
        }
    }

    fun removeAutoWorkoutCard() {
        val index = healthOverviewData.value?.indexOfFirst {
            it is OHealthOverview.AutoSport
        }
        if (index != null && index != -1) {
            healthOverviewData.value?.removeAt(index)
            healthOverviewData.postValue(healthOverviewData.value)
        }
    }

    fun confirmNap(nap: OreoNapData) {
        viewModelScope.launch(Dispatchers.IO) {
            userActivityRepository.addNapServer(
                nap
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
                                        confirmNap(nap)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            removeNapById(nap)
                            it.firstOrNull()?.let { napData ->
                                if (napData.date != null) {
                                    userHealthDataDataSource.clearDataByDates(listOf(napData.date!!))
                                    delay(100)
                                }
                                onNapAddSuccess.postValue(Event(napData))
                            }
                        }
                    }
                }
            }
        }
    }

    fun removeNapById(nap: OreoNapData) {
        viewModelScope.launch(Dispatchers.IO) {
            userActivityRepository.removeNap(nap.id)
            loadNapsToConfirm()
        }
    }

    fun getNapSlideUpObj(nap: OreoNapDetailsDataModel): SlideUpNapScoreDataModel {
        return SlideUpNapScoreDataModel(
            napId = nap.id,
            title = nap.title,
            description = nap.subtitle,
            oldSleepScore = nap.prevSleepScore,
            newSleepScore = nap.sleepScore,
            oldReadinessScore = nap.prevReadinessScore,
            newReadinessScore = nap.readinessScore,
        )
    }

    fun handleBatteryAlert() {
        viewModelScope.launch(Dispatchers.IO) {
            val device = ringDataStore.getRingDevice()
            if (device == null) {
                stateDashRingBattery.postValue(Pair(false, null))
                return@launch
            }
            val batteryPercentage = watchDataStore.getBatteryPercentRing()
            if (batteryPercentage < 20) {
                if (sessionManager.isRingCharging.value == false) {
                    stateDashRingBattery.postValue(Pair(true, device))
                } else {
                    stateDashRingBattery.postValue(Pair(false, null))
                }
            } else {
                stateDashRingBattery.postValue(Pair(false, null))
            }
        }

    }


}