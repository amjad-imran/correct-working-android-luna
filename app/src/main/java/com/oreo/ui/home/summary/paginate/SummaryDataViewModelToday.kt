package com.oreo.ui.home.summary.paginate

import android.graphics.Color
import android.util.Log
import androidx.core.graphics.toColorInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.dataConverter.DataConverter
import com.noisefit.data.googleFit.GoogleFitDataObservers
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UpdateRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.luna.BuildConfig
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.enums.DashInfoCard
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.AlarmTimingsData
import com.noisefit_commans.data.model.NotificationGoals
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.data.model.PlannerAlarmData
import com.noisefit_commans.data.model.SleepCardDashState
import com.noisefit_commans.data.model.SleepPlannerData
import com.noisefit_commans.data.model.SleepPlannerDisplayModel
import com.noisefit_commans.data.model.User
import com.noisefit_commans.data.model.caffeine.CaffeineGraphDataModel
import com.noisefit_commans.data.model.circadian.CircadianGraphData
import com.noisefit_commans.data.model.circadian.ItemCircadianGraphData
import com.noisefit_commans.data.model.circadian.NudgeCircadianGraph
import com.noisefit_commans.data.model.customHomeScreen.CustomHomeScreenNetworkItem
import com.noisefit_commans.data.model.timeline.ItemTimelineResponseModel
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.SleepDataGoogleFit
import com.noisefit_commans.models.SleepDataGoogleFit.SleepDataBreakup
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanN
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.HAPTIC_VIBRATION
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.ScreenUtils
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.dataConverter.OreoHRDataConvertor
import com.oreo.data.dataConverter.OreoStressDataConvertor
import com.oreo.data.db.abstaction.GoogleFitDataSource
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.model.AlertType
import com.oreo.data.model.AppUpdateModel
import com.oreo.data.model.CaffeineWindowData
import com.oreo.data.model.ChartModel
import com.oreo.data.model.DashAlert
import com.oreo.data.model.FemaleHealthCardState
import com.oreo.data.model.ImpactData
import com.oreo.data.model.NotificationToggleModel
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.OreoNapDetailsDataModel
import com.oreo.data.model.OtaUpdateModel
import com.oreo.data.model.PeriodCard1
import com.oreo.data.model.PeriodCard2
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.SlideUpNapScoreDataModel
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.TimeWindow
import com.oreo.data.model.TrendsData
import com.oreo.data.model.VideoInfoType
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.model.femaleh.TempPeriodData
import com.oreo.data.model.health.ODashboardActivityModel
import com.oreo.data.model.health.ODashboardActivityScoreModel
import com.oreo.data.model.health.ODashboardReadinessModel
import com.oreo.data.model.health.ODashboardReadinessScoreModel
import com.oreo.data.model.health.ODashboardSleepModel
import com.oreo.data.model.health.ODashboardSleepScoreModel
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.data.model.health.SleepHourlyBreakup
import com.oreo.data.model.sleep.HealthTrend
import com.oreo.data.model.timeline.ItemTimelineModel
import com.oreo.data.repository.abstraction.FemaleHealthRepository
import com.oreo.data.repository.abstraction.OreoSyncRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.ui.chatGpt.SummaryStates
import com.oreo.ui.custom.HighlightState
import com.oreo.ui.customHomeScreen.CustomHomeScreenItem
import com.oreo.ui.timelineScreen.TimelineScreenDataViewmodel.Companion.ACTIVITY_KEY
import com.oreo.ui.timelineScreen.TimelineScreenDataViewmodel.Companion.CAFFEINE_INTAKE_KEY
import com.oreo.ui.timelineScreen.TimelineScreenDataViewmodel.Companion.LIGHT_EXPOSURE_KEY
import com.oreo.ui.timelineScreen.TimelineScreenDataViewmodel.Companion.MEAL_INTAKE_KEY_KEY
import com.oreo.ui.timelineScreen.TimelineScreenDataViewmodel.Companion.NAP_KEY
import com.oreo.ui.timelineScreen.TimelineScreenDataViewmodel.Companion.PERIOD_STARTED_KEY
import com.oreo.ui.timelineScreen.TimelineScreenDataViewmodel.Companion.SLEEP_KEY
import com.oreo.ui.timelineScreen.TimelineScreenDataViewmodel.Companion.SYMPTOM_KEY
import com.oreo.ui.timelineScreen.TimelineScreenDataViewmodel.Companion.WATER_CONSUMPTION_KEY
import com.oreo.ui.timelineScreen.TimelineScreenDataViewmodel.Companion.WORKOUT_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.oreo.util.DateTimeUtil
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random


@HiltViewModel
class SummaryDataViewModelToday @Inject constructor(
    val userRepository: OreoUserActivityRepository,
    val userRepositoryOld: UserRepository,
    val ringDataStore: RingDataStore,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val vibrationUtils: VibrationUtils,
    val dataConverter: DataConverter,
    val screenUtils: ScreenUtils,
    val watchDataStore: WatchDataStore,
    private val syncRepository: OreoSyncRepository,
    val oreoStressDataConvertor: OreoStressDataConvertor,
    val userActivityRepository: OreoUserActivityRepository,
    val femaleHealthRepository: FemaleHealthRepository,
    val updateRepository: UpdateRepository,
    val resourceProvider: ResourcesProvider,
    private val userHealthDataDataSource: OreoUserHealthDataDataSource,
    val hrDataConvertor: OreoHRDataConvertor,
    val googleFitDataSource: GoogleFitDataSource,
    val googleFitDataObservers: GoogleFitDataObservers,
) : BaseViewModel() {

    var date: String? = null

    val notificationUpdatedState = MutableLiveData<Event<Pair<NotificationGoal, Boolean>>>()

    val stateHeaderCard = MutableLiveData<Pair<String, String>>()//Name,Date
    val healthOverviewData = MutableLiveData<ArrayList<OHealthOverview>>()
    val viewedCardsData = MutableLiveData<ArrayList<OHealthOverview>>()
    val statePairDeviceCard = MutableLiveData<Boolean>()
    val stateDashRingBattery = MutableLiveData<Pair<Boolean, ColorFitDevice?>>()
    val stateDashAlerts = MutableLiveData<HashMap<AlertType, DashAlert>>()
    val stateGoogleFitCard = MutableLiveData<Boolean>()
    val stateGoogleFitCardDataSyncAvailable = MutableLiveData<Boolean>()
    val napsList = MutableLiveData<List<OreoNapData>>()

    val sleepAlert = MutableLiveData<SleepAlert?>()
    val showBlackListDialog = MutableLiveData<Event<Boolean>>()

    var contributorInfo: OContributorResponseModal? = null
    val hrInfo = MutableLiveData<Event<String>>()
    var sleepScoreInfo = MutableLiveData<Event<String>>()
    var readinessScoreInfo = MutableLiveData<Event<String>>()
    var activityScoreInfo = MutableLiveData<Event<String>>()
    var appUpdateInfo = MutableLiveData<AppUpdateModel?>()
    var otaUpdateInfo = MutableLiveData<OtaUpdateModel?>()

    val stateWorkouts = MutableLiveData<List<OActivityListModal>>()
    var notificationToggleModel: NotificationToggleModel? = null


    val stateReadinessAvgCard = MutableLiveData<ODashboardReadinessScoreModel?>()
    val stateSleepAvgCard =
        MutableLiveData<Pair<ODashboardSleepScoreModel?, ODashboardActivityScoreModel?>>()
    val stateHeartRateCard = MutableLiveData<OHealthOverview.HeartRateDataModel?>()
    val stateLunaAiCard = MutableLiveData<OHealthOverview.LunaAiCard?>(
        OHealthOverview.LunaAiCard(null)
    )
    val summaryStates = MutableLiveData<SummaryStates>()

    val stateStressCard = MutableLiveData<OHealthOverview.StressDashDataModel?>()

    val cycleTrackerCardBigData = MutableLiveData<OHealthOverview.CycleTrackerCardBig?>()
    val cycleTrackerCardSmallData = MutableLiveData<OHealthOverview.CycleTrackerCardSmall?>()
    val trackFemaleHealthCardData = MutableLiveData<OHealthOverview.CardTrackFemaleHealth?>()
    val gotYourPeriodData = MutableLiveData<OHealthOverview.GotYourPeriod?>()

    val findMyRingCard = MutableLiveData<Boolean?>()

    var notificationGoalsCardData: NotificationGoals? = null
    val notificationGoalsCardDataInit = MutableLiveData<Event<Boolean>>()
    val notificationGoalsCardDataUpdated = MutableLiveData<Event<Boolean>>()
    val hydrationUpdated = MutableLiveData<Event<Boolean>>()

    var user: User? = null
    var gender: String? = null
    var registerDate: Int = -1
    var shouldShowStressCard = false
    var stressBeta = false
    var enableAi = false
    var onNapAddSuccess = MutableLiveData<Event<OreoNapDetailsDataModel>>()
    var serverUserHealthData: ServerUserHealthData? = null

    private var hasDetectedWorkout = false
    private var hasDetectedNaps = false

    /**
     * Pair (hasDataLoaded,Female health data)
     */
    var femaleHealthData: Pair<Boolean, FemaleHealthUserInfoModel?> = Pair(false, null)
    var femaleHealthDataLoaded = MutableLiveData<Event<Boolean>>()

    var sleepPlannerData: Pair<Boolean, SleepPlannerData?> = Pair(false, null)
    var sleepPlannerDataLoaded = MutableLiveData<Event<Boolean>>()

    //
    var userManagedSwitchState = false
    var caffeineGraphData: CaffeineGraphDataModel? = null

    var circadianGraphData: CircadianGraphData? = null
    val stateCircadianCard = MutableLiveData<Pair<OHealthOverview.CircadianAlignment?, Boolean>>()

    var isNudgeCircadianApiCalled: Boolean = false
    var nudgeCircadianData: NudgeCircadianGraph ?= null
    var updateNudgeInMainViewModel = MutableLiveData<Event<NudgeCircadianGraph>>()

    var timeTrackerActivities: List<ItemTimelineResponseModel> ?= null
    var summaryAvailable: Boolean? = false
    //

    fun getStressWalkthroughShownStatus(): Boolean {
        return localDataStore.getStressWalkthroughShownStatus()
    }

    fun checkBeforeTime(): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        val hour24hrs: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val time1 = LocalTime.of(hour24hrs, 0)
        val time2 = LocalTime.of(21, 0)
        return time1.isBefore(time2)
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
            /*stateHeartRateCard.postValue(userRepository.getSummaryHRHealthOverview().apply {
                if (device == null) {
                    this?.measureState = TapMeasureState.NO_DEVICE
                }
            })*/


            //handleGoogleFitCard()

        }

        updateAlerts()


    }

    private fun handleSleepAlert(healthData: OreoSleepModel?) {
        viewModelScope.launch(Dispatchers.IO) {

            //time check if after 6 am
            //is alert already shown for today
            val crossedDate = ringDataStore.sleepAlertCrossedForDate()
            val isSleepAlertCrossed = if (crossedDate == null) {
                false
            } else {
                val todayDate = LocalDate.now().toString()
                crossedDate.equals(todayDate)
            }

            var sleepAlertToShow: SleepAlert? = null

            val sleepExists = checkIfSleepExists(healthData)
            val isSyncedAfter6 = checkIsSyncedAfterSix()

            if (LocalDateTime.now().hour > 6
                && isSleepAlertCrossed.not()
                && sleepExists.not()
                && isSyncedAfter6
                && sessionManager.connectStateRing.value is ConnectState.ConnectSuccess
            ) {

                val hrData = userRepository.getHrDataForToday()

                val list = hrData?.breakUp?.replace("255", "0")
                val breakupArray = Gson().fromJson<List<Int>>(list ?: "")

                if (breakupArray.isNullOrEmpty().not()) {
                    try {
                        val listTill = breakupArray.subList(0, 72)
                        val zeroList = listTill.filter { it == 0 }
                        if (zeroList.isEmpty()) {
                            //if sleep is not detected and hr is continuous
                            sleepAlertToShow = SleepAlert(
                                title = resourceProvider.getString(R.string.text_did_you_sleep_yesterday),
                                message = resourceProvider.getString(R.string.text_sleep_algo_detext),
                                addSleep = true
                            )
                        } else {
                            //if sleep is not detected and hr has break
                            sleepAlertToShow = SleepAlert(
                                title = resourceProvider.getString(R.string.text_did_you_sleep_yesterday),
                                message = resourceProvider.getString(R.string.text_wear_luna_ring),
                                addSleep = true
                            )
                        }
                    } catch (ignored: Exception) {
                    }
                }
            }

            if (ringDataStore.getRingDevice() != null
                && sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess
                && isSleepAlertCrossed.not() && sleepExists.not()
            ) {

                val isTodayHrDataEmpty = checkIfHrDataEmpty(LocalDate.now().toString())
                val yesterdayDate = LocalDate.now().minusDays(1).toString()
                val isYesterdayHrDataEmpty = checkIfHrDataEmpty(yesterdayDate)
                val pairDate = ringDataStore.getRingPairedDate()

                var shouldCheck = true
                if (pairDate != null) {
                    val pairedDate =
                        LocalDate.parse(pairDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    if (pairedDate == LocalDate.now()) {
                        shouldCheck = false
                    }
                }

                if (isTodayHrDataEmpty && shouldCheck) {
                    if (isYesterdayHrDataEmpty) {
                        sleepAlertToShow = SleepAlert(
                            title = resourceProvider.getString(R.string.text_missing_data),
                            message = resourceProvider.getString(R.string.text_sleep_charge_ring_new),
                            addSleep = false
                        )
                    } else {
                        sleepAlertToShow = SleepAlert(
                            title = resourceProvider.getString(R.string.text_did_you_sleep_yesterday),
                            message = resourceProvider.getString(R.string.text_wear_luna_ring),
                            addSleep = true
                        )
                    }
                }

                /*val lastSyncTimestamp = ringDataStore.getLastSyncTimeStamp()
                val currentTimeStamp = DateFormats.getTimeStamp()
                if (lastSyncTimestamp != null && lastSyncTimestamp != -1L) {
                    val lastSyncDays = DateFormats.getDateDiff(
                        currentTimeStamp,
                        lastSyncTimestamp
                    )
                    if (lastSyncDays > 0) {
                        val daysString = if (lastSyncDays == 1L) {
                            "1 day"
                        } else {
                            "$lastSyncDays days"
                        }
                        sleepAlertToShow = SleepAlert(
                            title = "Missing data",
                            message = "We haven’t received data in last $daysString. Remember to charge your ring and wear" +
                                    " it regularly so you don’t miss out on your personalised insights!",
                            addSleep = false
                        )

                    }
                }*/
            }

            sleepAlert.postValue(
                sleepAlertToShow
            )

        }
    }

    private suspend fun checkIfHrDataEmpty(date: String): Boolean {
        val hrData = userRepository.getHrDataByDate(date)
        val formattedData = hrData?.breakUp?.replace("255", "0")
        val hrBreakup = Gson().fromJson<List<Int>>(formattedData ?: "")
        if (hrBreakup.isNullOrEmpty()) return true
        val nonZeroList = hrBreakup.filter { it != 0 }
        return nonZeroList.isEmpty()
    }

    private fun checkIsSyncedAfterSix(): Boolean {
        val lastSync = ringDataStore.getLastSyncTimeStamp()
        if (lastSync == null || lastSync == -1L) {
            return false
        }

        val instant: Instant = Instant.ofEpochMilli(lastSync)
        val zoneId = ZoneId.systemDefault() // Use the system default time zone
        val lastSyncTime = instant.atZone(zoneId).toLocalDateTime()
        val sixAmTime = LocalDateTime.now().withHour(6).withMinute(0).withSecond(0)

        return lastSyncTime > sixAmTime
    }

    private fun checkIfSleepExists(healthData: OreoSleepModel?): Boolean {
        if (healthData == null) return false

        val hasNaps = healthData.naps.isNullOrEmpty().not()
        val hasSleep = healthData.sleeps.isNullOrEmpty().not()

        if (hasSleep || hasNaps) {
            return true
        }

        return false
    }

    fun handleGoogleFitCard() {
        viewModelScope.launch(Dispatchers.IO) {
            val showGoogleFit = ringDataStore.getDeviceFeatures()?.googleFit

            if (showGoogleFit == 1) {
                val isGoogleFitEnabled = localDataStore.isEnableGoogleFit()
                val isGoogleFitCrossed = localDataStore.isGoogleFitCrossed()

                if (isGoogleFitEnabled) {
                    stateGoogleFitCard.postValue(false)

                    if (hasDetectedWorkout) {
                        stateGoogleFitCardDataSyncAvailable.postValue(false)
                        return@launch
                    }

                    if (hasDetectedNaps) {
                        stateGoogleFitCardDataSyncAvailable.postValue(false)
                        return@launch
                    }

                    //ring disconnected check
                    if (sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                        stateGoogleFitCardDataSyncAvailable.postValue(false)
                        return@launch
                    }


                    val isGoogleFitSyncCrossed = localDataStore.isGoogleFitManageCrossed()
                    if (isGoogleFitSyncCrossed) {
                        stateGoogleFitCardDataSyncAvailable.postValue(false)
                        return@launch
                    }

                    val isDataAvailableForSync =
                        (googleFitDataSource.getUnSyncedData().isNotEmpty())

                    if (isDataAvailableForSync) {
                        stateGoogleFitCardDataSyncAvailable.postValue(true)
                    } else {
                        stateGoogleFitCardDataSyncAvailable.postValue(false)
                    }
                } else {
                    stateGoogleFitCardDataSyncAvailable.postValue(false)
                    if (isGoogleFitCrossed) {
                        stateGoogleFitCard.postValue(false)
                    } else {
                        stateGoogleFitCard.postValue(true)
                    }
                }
            } else {
                stateGoogleFitCard.postValue(false)
                stateGoogleFitCardDataSyncAvailable.postValue(false)
            }
        }
    }

    fun updateAlerts() {
        val dashAlert = HashMap<AlertType, DashAlert>()

        val btState = sessionManager.bluetoothStateDash.value
        val devicePaired = ringDataStore.getRingDevice()
        if (btState == false && devicePaired != null) {
            if (sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                dashAlert[AlertType.BLUETOOTH] =
                    DashAlert(
                        resourceProvider.getString(R.string.text_authorize_bluetooth_connectivity_for_luna),
                        false
                    )
            }
        }

        /* if (sessionManager.forceOtaResponseRing != null) {
             dashAlert[AlertType.OTA_UPDATE] =
                 DashAlert("Ring firmware update available", false)
         }*/


        stateDashAlerts.postValue(dashAlert)
    }

    fun parseHealthData(
        healthData: ServerUserHealthData,
        trendsData: TrendsData?,
        impactData: ImpactData?
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            sessionManager.canLogPeriod = false

            val userActivities = ArrayList<OHealthOverview>()
            val viewedCardsData = ArrayList<OHealthOverview>()

            var gotYourPeriodCard: OHealthOverview.GotYourPeriod? = null
            var trackFemaleHealthCard: OHealthOverview.CardTrackFemaleHealth? = null
            var cycleTrackerCardBig: OHealthOverview.CycleTrackerCardBig? = null
            var cycleTrackerCardSmall: OHealthOverview.CycleTrackerCardSmall? = null

            val isAfter12 = checkIfIsAfter12()

            femaleHealthData.let {
                val (hasDataLoaded, femaleData) = it

                if (hasDataLoaded) {
                    if (femaleData == null) {
                        if (gender.equals("female", true)) {

                            val lastShownDays =
                                localDataStore.getFMHWalkthroughRemindLaterDays()

                            if (localDataStore.getFMHWalkthroughShownStatus()
                                    .not() && lastShownDays > 7
                            ) {
                                trackFemaleHealthCard = OHealthOverview.CardTrackFemaleHealth(
                                    FemaleHealthCardState.TRACK
                                )
                            }
                        }
                    } else {
                        if (femaleData.isTrackPregnancy != true) {
                            if (femaleData.currentDay == null) {
                                trackFemaleHealthCard = OHealthOverview.CardTrackFemaleHealth(
                                    FemaleHealthCardState.LOG
                                )
                            } else {
                                if (femaleData.isOvulation || femaleData.isPeriod) {
                                    cycleTrackerCardBig = OHealthOverview.CycleTrackerCardBig(
                                        convertToPeriodBigCardModel(
                                            femaleData
                                        )
                                    )
                                } else {
                                    cycleTrackerCardSmall = OHealthOverview.CycleTrackerCardSmall(
                                        convertToPeriodSmallCardModel(
                                            femaleData
                                        )
                                    )
                                }

                                val isCardShownForToday =
                                    femaleHealthRepository.getGotPeriodClickedStatus()
                                if (femaleData.isPeriod && !femaleData.otaLog && !isCardShownForToday) {

                                    val periodCurrentDay = femaleData.currentDay
                                    val dayMessage = if (periodCurrentDay == null) {
                                        null
                                    } else {
                                        resourceProvider.getString(
                                            R.string.text_today_s_your_predicted_day,
                                            ApplicationUtils.getOrdinalWord(
                                                periodCurrentDay, resourceProvider
                                            )
                                        )
                                    }

                                    gotYourPeriodCard = OHealthOverview.GotYourPeriod(
                                        dayMessage,
                                        femaleData.currentDay
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val autoSportCount = userRepository.getSummaryAutoWorkoutCount()
            if (autoSportCount > 0) {
                userActivities.add(OHealthOverview.AutoSport(autoSportCount))
                hasDetectedWorkout = true
            } else {
                hasDetectedWorkout = false
            }


            handleInfoCards(healthData, trendsData, userActivities, viewedCardsData)


            val readinessModel = ODashboardReadinessModel(
                readinessScore = healthData.readiness?.readinessScore?.value,
                status = healthData.readiness?.readinessScore?.text?.capitalizeWords(),
                statusCode = healthData.readiness?.readinessScore?.status,
                nudges = healthData.readiness?.dashNudges,
                totalScoreImpact = healthData.readiness?.totalScoreImpact ?: 0,
                noOfNaps = healthData.sleep?.naps?.size ?: 0,
                noOfSleeps = healthData.sleep?.sleeps?.size ?: 0,
                impact = impactData?.readinessScore
            )

            val filteredNaps = healthData.sleep?.naps?.filter { !it.isNextDayNap }

            val newSleepArray = dataConverter.mergeSleepDataV2(
                healthData.sleep?.sleeps, filteredNaps
            )


            var totalSleep: Int? = null
            healthData.sleep?.sleeps?.forEach {
                if (totalSleep == null) {
                    totalSleep = 0
                }
                totalSleep = totalSleep!! + (it.totalDuration ?: 0)
            }

            filteredNaps?.forEach {
                val start = LocalDateTime.parse(
                    it.startTime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                val end = LocalDateTime.parse(
                    it.endTime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                if (totalSleep == null) {
                    totalSleep = 0
                }
                Duration.between(start, end).toSeconds().toInt().let {
                    totalSleep = totalSleep!! + it
                }
            }

            val sleepModel = ODashboardSleepModel(
                sleepScore = healthData.sleep?.sleep_score?.value,
                totalSleep = totalSleep,
                restingHr = healthData.sleep?.avg_hrv,
                sleepStage = newSleepArray ?: ArrayList(),
                status = healthData.sleep?.sleep_score?.text?.capitalizeWords(),
                statusCode = healthData.sleep?.sleep_score?.status,
                startTime = newSleepArray?.firstOrNull()?.start_time ?: "",
                endTime = newSleepArray?.lastOrNull()?.end_time ?: "",
                totalScoreImpact = healthData.sleep?.totalScoreImpact ?: 0,
                noOfNaps = healthData.sleep?.naps?.size ?: 0,
                noOfSleeps = healthData.sleep?.sleeps?.size ?: 0
            )
            val activityModal = ODashboardActivityModel(
                activityScore = healthData.activity?.activityScore?.value,
                activeCalories = healthData.activity?.activeCalories ?: 0,
                inactiveMinutes = healthData.activity?.activityContributors?.stayActive?.value,
                status = healthData.activity?.activityScore?.level?.capitalizeWords(),
                statusCode = healthData.activity?.activityScore?.status,
                nudges = healthData.activity?.dash_nudges,
                steps = healthData.activity?.steps ?: 0,
                impact = impactData?.activityScore
            )

            val nap = healthData.sleep?.naps ?: ArrayList()
            val hasSleep = sleepModel.sleepScore != null && sleepModel.sleepScore != 0


            val daySlot = getDaySlot()

            when (daySlot) {
                0 -> {
                    //sleep
                    if (healthData.sleep?.sleep_score != null) {
                        if (registerDate != 0) {
                            healthData.readiness?.let {
                                if ((readinessModel.readinessScore ?: 0) > 0) {
                                    userActivities.add(OHealthOverview.Readiness(readinessModel))
                                }
                            }
                            if (enableAi) {
                                /*userActivities.add(OHealthOverview.LunaAiCard())*/
                            }
                            if ((sleepModel.totalSleep ?: 0) > 0) {
                                userActivities.add(
                                    OHealthOverview.Sleep(
                                        sleepModel,
                                        makeSleepArray(newSleepArray),
                                        newSleepArray?.firstOrNull()?.start_time ?: "",
                                        newSleepArray?.lastOrNull()?.end_time ?: "",
                                        impact = impactData?.sleepScore
                                    )
                                )
                                if (isAfter12.not()) {
                                    healthData.sleep?.healthTrend?.let {
                                        userActivities.add(OHealthOverview.HealthMonitorCard(it))
                                    }
                                }
                            }
                        } else {
                            if (enableAi) {
                                /*userActivities.add(OHealthOverview.LunaAiCard())*/
                            }
                        }
                    } else {
                        if (enableAi) {
                            /*userActivities.add(OHealthOverview.LunaAiCard())*/
                        }
                        userActivities.add(OHealthOverview.SleepWaiting)
                    }
                    if (nap.isNotEmpty()) {
                        userActivities.add(OHealthOverview.NapDashCard(nap, healthData.date))
                    }
                    sleepPlannerData.second?.let {
                        userActivities.add(
                            OHealthOverview.SleepPlannerCard(
                                SleepPlannerDisplayModel(
                                    it,
                                    getPlannerCardState(it, hasSleep)
                                )
                            )
                        )
                    }
                }

                1 -> {

                    //sleep
                    if (healthData.sleep?.sleep_score != null) {
                        if (registerDate != 0) {
                            healthData.readiness?.let {
                                if ((readinessModel.readinessScore ?: 0) > 0) {
                                    userActivities.add(OHealthOverview.Readiness(readinessModel))
                                }
                            }
                            if (enableAi) {
                                /*userActivities.add(OHealthOverview.LunaAiCard())*/
                            }
                            if ((sleepModel.totalSleep ?: 0) > 0) {
                                userActivities.add(
                                    OHealthOverview.Sleep(
                                        sleepModel,
                                        makeSleepArray(newSleepArray),
                                        newSleepArray?.firstOrNull()?.start_time ?: "",
                                        newSleepArray?.lastOrNull()?.end_time ?: "",
                                        impact = impactData?.sleepScore
                                    )
                                )
                                if (isAfter12.not()) {
                                    healthData.sleep?.healthTrend?.let {
                                        userActivities.add(OHealthOverview.HealthMonitorCard(it))
                                    }
                                }
                            }
                        } else {
                            if (enableAi) {
                                /*userActivities.add(OHealthOverview.LunaAiCard())*/
                            }
                        }
                    } else {
                        if (enableAi) {
                            /*userActivities.add(OHealthOverview.LunaAiCard())*/
                        }
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
                    sleepPlannerData.second?.let {
                        userActivities.add(
                            OHealthOverview.SleepPlannerCard(
                                SleepPlannerDisplayModel(
                                    it,
                                    getPlannerCardState(it, hasSleep)
                                )
                            )
                        )
                    }
                }

                2 -> {
                    if (registerDate != 0) {
                        healthData.readiness?.let {
                            if ((readinessModel.readinessScore ?: 0) > 0) {
                                userActivities.add(OHealthOverview.Readiness(readinessModel))
                            }
                        }
                        if (enableAi) {
                            /*userActivities.add(OHealthOverview.LunaAiCard())*/
                        }

                        healthData.sleep?.let {
                            if ((sleepModel.totalSleep ?: 0) > 0) {
                                userActivities.add(
                                    OHealthOverview.Sleep(
                                        sleepModel,
                                        makeSleepArray(newSleepArray),
                                        newSleepArray?.firstOrNull()?.start_time ?: "",
                                        newSleepArray?.lastOrNull()?.end_time ?: "",
                                        impact = impactData?.sleepScore
                                    )
                                )
                                if (isAfter12.not()) {
                                    healthData.sleep?.healthTrend?.let {
                                        userActivities.add(OHealthOverview.HealthMonitorCard(it))
                                    }
                                }
                            }
                        }
                    } else {
                        if (enableAi) {
                            /*userActivities.add(OHealthOverview.LunaAiCard())*/
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
                    sleepPlannerData.second?.let {
                        userActivities.add(
                            OHealthOverview.SleepPlannerCard(
                                SleepPlannerDisplayModel(
                                    it,
                                    getPlannerCardState(it, hasSleep)
                                )
                            )
                        )
                    }


                }

                else -> {
                    val currentTime = DateFormats.getTimeFormat()

                    val isBefore8 = DateFormats.isTimeBefore(currentTime, "20:00")
                    if (isBefore8.not()) {
                        sleepPlannerData.second?.let {
                            userActivities.add(
                                OHealthOverview.SleepPlannerCard(
                                    SleepPlannerDisplayModel(
                                        it,
                                        getPlannerCardState(it, hasSleep)
                                    )
                                )
                            )
                        }
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

                    if (enableAi) {
                        /*userActivities.add(OHealthOverview.LunaAiCard())*/
                    }

                    if (isBefore8) {
                        sleepPlannerData.second?.let {
                            userActivities.add(
                                OHealthOverview.SleepPlannerCard(
                                    SleepPlannerDisplayModel(
                                        it,
                                        getPlannerCardState(it, hasSleep)
                                    )
                                )
                            )
                        }
                    }

                    if (registerDate != 0) {
                        if (healthData.sleep?.sleep_score != null) {
                            if ((sleepModel.totalSleep ?: 0) > 0) {
                                userActivities.add(
                                    OHealthOverview.SleepMinimal(
                                        sleepModel, makeSleepArray(newSleepArray),
                                        impact = impactData?.sleepScore
                                    )
                                )
                                if (isAfter12.not()) {
                                    healthData.sleep?.healthTrend?.let {
                                        userActivities.add(OHealthOverview.HealthMonitorCard(it))
                                    }
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
                                                ?: "",
                                            impact = impactData?.sleepScore
                                        )
                                    )
                                }
                                if (isAfter12.not()) {
                                    healthData.sleep?.healthTrend?.let {
                                        userActivities.add(OHealthOverview.HealthMonitorCard(it))
                                    }
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
            val device = ringDataStore.getRingDevice()

            if (shouldShowStressCard) {
                val combinedData = oreoStressDataConvertor.getStressCombinedData(healthData)

                if (isAfter12) {
                    healthData.sleep?.healthTrend?.let {
                        userActivities.add(OHealthOverview.HealthMonitorCard(it))
                    }
                }
                /*userActivities.add(
                    OHealthOverview.StressGraph(
                        combinedData,
                        healthData.stress?.stressValue?.value ?: 0,
                        healthData.stress?.stressValue?.lastUpdated ?: 0L,
                        getStressStatus(healthData.stress?.stressValue?.value ?: 0),
                        true,
                        stressBeta
                    )
                )*/

                stateStressCard.postValue(userRepository.getSummaryStressData().apply {
                    this?.data = combinedData
                    if (device == null) {
                        this?.measureState = TapMeasureState.NO_DEVICE
                    }
                })
            }

            stateSleepAvgCard.postValue(
                Pair(
                    trendsData?.sleepScoreAvg, trendsData?.activityScoreAvg
                )
            )
            stateReadinessAvgCard.postValue(trendsData?.readinessScoreAvg)

            this@SummaryDataViewModelToday.viewedCardsData.postValue(viewedCardsData)
            healthOverviewData.postValue(userActivities)

            stateHeartRateCard.postValue(userRepository.getSummaryHRHealthOverview().apply {
                if (device == null) {
                    this?.measureState = TapMeasureState.NO_DEVICE
                }
            })


            cycleTrackerCardBigData.postValue(cycleTrackerCardBig)
            cycleTrackerCardSmallData.postValue(cycleTrackerCardSmall)
            trackFemaleHealthCardData.postValue(trackFemaleHealthCard)
            gotYourPeriodData.postValue(gotYourPeriodCard)

            //healthMonitorCardData.postValue(healthData.sleep?.healthTrend)
            stateWorkouts.postValue(healthData.activity?.workout ?: ArrayList())
            loadNapsToConfirm()

            handleSleepAlert(healthData.sleep)

        }
        handleGoogleFitCard()
    }

    fun getUserManagedHealthData(
        healthData: ServerUserHealthData,
        trendsData: TrendsData?,
        impactData: ImpactData?,
        lunaManaged: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            sessionManager.canLogPeriod = false

            val userActivities = ArrayList<OHealthOverview>()
            val viewedCardsData = ArrayList<OHealthOverview>()

            // Handle auto-detected workouts
            val autoSportCount = userRepository.getSummaryAutoWorkoutCount()
            if (autoSportCount > 0) {
                userActivities.add(OHealthOverview.AutoSport(autoSportCount))
                hasDetectedWorkout = true
            } else {
                hasDetectedWorkout = false
            }

            handleInfoCards(healthData, trendsData, userActivities, viewedCardsData)

            var totalSleep: Int? = null
            healthData.sleep?.sleeps?.forEach {
                if (totalSleep == null) {
                    totalSleep = 0
                }
                totalSleep = totalSleep!! + (it.totalDuration ?: 0)
            }

            val filteredNaps = healthData.sleep?.naps?.filter { !it.isNextDayNap }

            val newSleepArray = dataConverter.mergeSleepDataV2(
                healthData.sleep?.sleeps, filteredNaps
            )

            filteredNaps?.forEach {
                val start = LocalDateTime.parse(
                    it.startTime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                val end = LocalDateTime.parse(
                    it.endTime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                if (totalSleep == null) {
                    totalSleep = 0
                }
                Duration.between(start, end).toSeconds().toInt().let {
                    totalSleep = totalSleep!! + it
                }
            }

            val sleepModel = ODashboardSleepModel(
                sleepScore = healthData.sleep?.sleep_score?.value,
                totalSleep = totalSleep,
                restingHr = healthData.sleep?.avg_hrv,
                sleepStage = newSleepArray ?: ArrayList(),
                status = healthData.sleep?.sleep_score?.text?.capitalizeWords(),
                statusCode = healthData.sleep?.sleep_score?.status,
                startTime = newSleepArray?.firstOrNull()?.start_time ?: "",
                endTime = newSleepArray?.lastOrNull()?.end_time ?: "",
                totalScoreImpact = healthData.sleep?.totalScoreImpact ?: 0,
                noOfNaps = healthData.sleep?.naps?.size ?: 0,
                noOfSleeps = healthData.sleep?.sleeps?.size ?: 0
            )

            val hasSleep = sleepModel.sleepScore != null && sleepModel.sleepScore != 0

            val nap = healthData.sleep?.naps ?: ArrayList()

            val daySlot = getDaySlot()
            val isAfter12 = checkIfIsAfter12()

            val priorityList = if (lunaManaged.not()) {
                val list = localDataStore.getCustomHomeScreenItemsPriorityList()
                if (list != null) {
                    getCardsPriorityFromApi(list.cards).sortedBy { it.priority }
                } else {
                    getLunaManagedPriority(hasSleep)
                }
            } else {
                getLunaManagedPriority(hasSleep)
            }

            val hasCaffeineKey = priorityList.find { it.key.equals("caffeine_intake", true) }
            if (hasCaffeineKey == null) {
                getCaffeineCardData()?.let {
                    userActivities.add(it)
                }
            }

            priorityList.forEach { item ->
                if (item.switchState.not()) return@forEach

                when (item.key) {
                    "one_tap_vitals" -> {
                        getOneTapVitalsCard(healthData)?.let { userActivities.add(it) }
                    }
                    "sleep" -> {
                        getSleepDataCard(
                            healthData,
                            impactData,
                            sleepModel,
                            daySlot,
                        )?.let { userActivities.add(it) }

                        if (!lunaManaged) {
                            getHealthMonitorData(healthData.sleep)?.let {
                                userActivities.add(it)
                            }
                            /*LOGS.d("isAfter12:  ${isAfter12.not()}")
                            if (isAfter12.not()) {
                                getHealthMonitorData(healthData.sleep)?.let {
                                    userActivities.add(it)
                                }
                            }*/
                        }

                        if (nap.isNotEmpty()) {
                            userActivities.add(
                                OHealthOverview.NapDashCard(
                                    nap, healthData.date
                                )
                            )
                        }

                    }

                    "activity" -> {
                        getActivityDataCard(
                            healthData,
                            impactData,
                        )?.let { userActivities.add(it) }
                    }

                    "readiness" -> {
                        getReadinessDataCard(
                            healthData,
                            healthData.readiness,
                            impactData,
                            daySlot
                        )?.let { userActivities.add(it) }
                    }

                    "sleep_planner" -> {
                        getSleepPlannerDataCard(hasSleep)?.let { userActivities.add(it) }
                    }

                    "heart_rate" -> {
                        getHeartRateCard()?.let {
                            userActivities.add(it)
                        }
                    }

                    "health_monitor" -> {
                        getHealthMonitorData(healthData.sleep)?.let {
                            userActivities.add(it)
                        }
                    }

                    "daily_goals" -> {
                        getDailyGoalsCard()?.let { userActivities.add(it) }
                    }

                    "luna_ai" -> {
                        getLunaAiCard()?.let { userActivities.add(it) }
                    }

                    "cycle_tracker" -> {
                        val cycleTrackerCard = getCycleTrackerCard()
                        if (cycleTrackerCard != null) {
                            userActivities.add(cycleTrackerCard)
                            sessionManager.canLogPeriod = true
                        } else {
                            sessionManager.canLogPeriod = false
                        }
                    }

                    "7_day_trends_card" -> {
                        getSevenDaysTrendsDataCard(trendsData)?.let {
                            userActivities.add(it)
                        }
                    }

                    /*"workout_history" -> {
                        getWorkoutHistoryCard(healthData.activity)?.let {
                            userActivities.add(it)
                        }
                    }*/

                    "stress" -> {
                        getStressCard(healthData)?.let {
                            userActivities.add(it)
                        }
                    }

                    "caffeine_intake" -> {
                        getCaffeineCardData()?.let {
                            userActivities.add(it)
                        }
                    }

                    "circadian_alignment" -> {
                        getCircadianAlignmentCardData()?.let {
                            userActivities.add(it)
                        }
                    }

                }
            }

            /*getWorkoutHistoryCard(healthData.activity)?.let { userActivities.add(it) }*/
            getTimelineCard()?.let { userActivities.add(it) }

            if(lunaManaged) {
                getLunaAiCard()?.let {
                    if (userActivities.size > 2) {
                        userActivities.add(2, it)
                    } else {
                        userActivities.add(it)
                    }
                }
            }

            // Add naps if any (this could also be moved to a separate function)
//            healthData.sleep?.naps?.takeIf { it.isNotEmpty() }?.let { naps ->
//                userActivities.add(OHealthOverview.NapDashCard(naps, healthData.date))
//            }

            // Post the final data
            healthOverviewData.postValue(userActivities)

            //viewedCardsData.postValue(viewedCardsData)
            //this@SummaryDataViewModelToday.viewedCardsData.postValue(viewedCardsData)

            // Handle other operations
            loadNapsToConfirm()
            handleSleepAlert(healthData.sleep)
            handleGoogleFitCard()
        }
    }

    private suspend fun getOneTapVitalsCard(healthData: ServerUserHealthData): OHealthOverview.OneTapVitals? {
        // HR
        val hrModel = userRepository.getSummaryHRHealthOverview()
        val hrValue = hrModel?.lastMeasuredValue?.takeIf { it > 0 }
        val hrLastTime: String? = try {
            if (hrModel != null && hrModel.lastMeasuredIndex >= 0) {
                val lastUpdatedTimestamp =
                    DateTimeUtil.getTodayMidnightTimestamp() + (hrModel.lastMeasuredIndex + 1) * 5 * 60 * 1000
                val currentTimeStamp = DateFormats.getTimeStamp()
                val diff = currentTimeStamp - lastUpdatedTimestamp
                when {
                    diff < 60_000 -> "just now"
                    diff < 60 * 60_000 -> "${diff / 60_000} min ago"
                    else -> "${diff / (60 * 60_000)} hr ago"
                }
            } else null
        } catch (e: Exception) { null }

        // Stress
        val stressValue = healthData.stress?.stressValue?.value
        val stressLastTime: String? = healthData.stress?.stressValue?.lastUpdated?.let { ts ->
            val diff = DateFormats.getTimeStamp() - ts
            when {
                diff < 60_000 -> "just now"
                diff < 60 * 60_000 -> "${diff / 60_000} min ago"
                else -> "${diff / (60 * 60_000)} hr ago"
            }
        }

        // SpO2 and Skin Temp from HealthTrend (typically from last sleep)
        val spo2Value = healthData.sleep?.healthTrend?.bloodOxy?.value?.toInt()
        val skinTempValue = healthData.sleep?.healthTrend?.skinTemp?.value?.toFloat()

        val features = OHealthOverview.OneTapVitalsFeatureConfig(
            showHR = true,
            showStress = true,
            showSpO2 = true,
            showSkinTemp = false
        )

        if (!features.showHR && !features.showStress && !features.showSpO2 && !features.showSkinTemp) {
            return null
        }

        return OHealthOverview.OneTapVitals(
            title = resourceProvider.getString(R.string.text_one_tap_vitals),
            hrValue = hrValue,
            hrLastTime = hrLastTime,
            stressValue = stressValue,
            stressLastTime = stressLastTime,
            spo2Value = spo2Value,
            spo2LastTime = null,
            skinTempValue = skinTempValue,
            skinTempLastTime = null,
            featureConfig = features,
            expandedType = null,
            measuring = false
        )
    }

    private fun getTimelineCard(): OHealthOverview?{
        val dataList = timeTrackerActivities?: ArrayList()
        val data = mergeHydrationEvents(dataList)

        var mealCount = data.count { item -> item.event.equals(MEAL_INTAKE_KEY_KEY) }

        data?.forEach { data ->
            data.displayTime = convertTimeFormat(data.startTime)
            when(data.event){
                SLEEP_KEY -> {
                    data.titleColor = "#A8A8ED".toColorInt()
                    data.desc = getSleepDuration(data.startDate, data.startTime, data.endDate, data.endTime)
                }

                NAP_KEY -> {
                    data.titleColor = "#A8A8ED".toColorInt()
                    data.desc = getSleepDuration(data.startDate, data.startTime, data.endDate, data.endTime)
                }

                WORKOUT_KEY -> {
                    data.titleColor = "#78C3F9".toColorInt()
                    data.value?.let {
                        data.desc = it
                        data.unit?.let { data.desc += " $it" }
                    }
                }

                WATER_CONSUMPTION_KEY -> {
                    data.titleColor = "#8EF1C3".toColorInt()
                    data.value?.let {
                        try {
                            val value = formatMlToLitersOrMl(it.toIntOrNull()?:0)
                            data.desc = value
                        }catch (exp: Exception){
                            exp.printStackTrace()
                            data.desc = it
                            data.unit?.let { data.desc += " $it" }
                        }
                    }
                }

                CAFFEINE_INTAKE_KEY -> {
                    data.titleColor = "#DCA58E".toColorInt()
                    data.value?.let {
                        data.desc = it
                        data.unit?.let { data.desc += " $it" }
                    }
                }

                MEAL_INTAKE_KEY_KEY -> {
                    data.titleColor = "#FFE3B2".toColorInt()
                    data.desc = "Meal ${mealCount--}"
                }

                LIGHT_EXPOSURE_KEY -> {
                    data.titleColor = "#FFE1CF".toColorInt()
                    data.value?.let {
                        data.desc = "${it.toInt()/60} minutes"
                        /*data.unit?.let { data.desc += " $it" }*/
                    }
                }

                PERIOD_STARTED_KEY -> {
                    data.titleColor = "#F18EBD".toColorInt()
                    data.desc = "Day 1"
                }
                SYMPTOM_KEY -> {
                    data.titleColor = "#F18EBD".toColorInt()
                    data.desc = data.value
                }

                ACTIVITY_KEY -> {
                    data.titleColor = "#FFFFFF".toColorInt()
                }

                else -> {}
            }
        }

        return OHealthOverview.TimelineDash(
            listData = data.take(3)
        )
    }

    fun formatMlToLitersOrMl(ml: Int): String {
        return if (ml >= 1000) {
            String.format("%.1f liter", ml.toFloat() / 1000)
        } else {
            "$ml ml"
        }
    }

    private val DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE
    private val TIME_FMT = DateTimeFormatter.ISO_LOCAL_TIME
    private fun startDateTime(startDate: String?, startTime: String?): LocalDateTime =
        LocalDate.parse(startDate, DATE_FMT).atTime(LocalTime.parse(startTime, TIME_FMT))


    fun mergeHydrationEvents(
        events: List<ItemTimelineResponseModel>,
        windowMinutes: Long = 30
    ): List<ItemTimelineResponseModel> {

        val periodStartedEvent = events.find { it.event.equals(SYMPTOM_KEY, ignoreCase = true) }

        val eventsWithoutPeriod = events.toMutableList()
        periodStartedEvent?.let {
            eventsWithoutPeriod.remove(it)
        }

        val hydration = eventsWithoutPeriod
            .asSequence()
            .filter { it.event.equals(WATER_CONSUMPTION_KEY, ignoreCase = true) }
            .sortedByDescending { startDateTime(it.startDate, it.startTime) }
            .toList()

        val nonHydration = eventsWithoutPeriod
            .asSequence()
            .filter { !it.event.equals(WATER_CONSUMPTION_KEY, ignoreCase = true)}
            .toList()


        val merged = mutableListOf<ItemTimelineResponseModel>()
        var i = 0

        while (i < hydration.size) {
            val first = hydration[i]
            val anchor = startDateTime(first.startDate, first.startTime)
            var j = i
            var last = first
            var total = 0.0

            while (j < hydration.size) {
                val ev = hydration[j]
                val dt = startDateTime(ev.startDate, ev.startTime)

                if (dt.isAfter(anchor.minusMinutes(windowMinutes))) {
                    total += ev.value?.toDoubleOrNull() ?: 0.0
                    last = ev
                    j++
                } else {
                    break
                }
            }


            val summedValue =
                if (total % 1.0 == 0.0) total.toLong().toString() else total.toString()


            merged.add(
                first.copy(
                    startDate = first.startDate,
                    startTime = first.startTime,
                    endDate = last.endDate,
                    endTime = last.endTime,
                    value = summedValue
                )
            )

            i = j
        }

        merged.addAll(nonHydration)
        var sortedList = merged.sortedByDescending { startDateTime(it.startDate, it.startTime) }.toMutableList()
        periodStartedEvent?.let {
            if(sortedList.isEmpty()){
                sortedList = arrayListOf(it)
            }else{
                sortedList.add(it)
            }
        }

        return sortedList
    }


    private fun convertTimeFormat(time: String?): String {
        return try{
            val originalFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val timeObj = LocalTime.parse(time, originalFormatter)
            val newFormatter = DateTimeFormatter.ofPattern("h:mm a")

            timeObj.format(newFormatter).uppercase(Locale.getDefault())
        }catch (e: Exception){
            LOGS.e("TIMELINE_convertTimeFormat_EXCEPTION : $e")
            "-"
        }
    }

    fun getSleepDuration(
        startDate: String?,
        startTime: String?,
        endDate: String?,
        endTime: String?
    ): String {
        return try {
            // Define the format for time
            val timeFormatter12Hour = DateTimeFormatter.ofPattern("h:mm a")

            // Parse the start and end dates into LocalDate objects
            val startDateObj = LocalDateTime.parse("$startDate $startTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val endDateObj = LocalDateTime.parse("$endDate $endTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            // If the end time is before the start time, adjust the end time to the next day
            val adjustedEndDateObj = if (endDateObj.isBefore(startDateObj)) {
                endDateObj.plusDays(1)
            } else {
                endDateObj
            }

            // Calculate the duration between start and end times
            val duration = Duration.between(startDateObj, adjustedEndDateObj)
            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60

            "$hours hr $minutes m"
            /*
            // Format the start and end times into 12-hour AM/PM format
            val formattedStartTime = startDateObj.format(timeFormatter12Hour)
            val formattedEndTime = adjustedEndDateObj.format(timeFormatter12Hour)

            // Return the formatted result
            val formattedTime = "$formattedStartTime - $formattedEndTime".uppercase(Locale.getDefault())
            "$hours hr $minutes m; $formattedTime"
            */
        }catch (e: Exception){
            LOGS.e("TIMELINE_GET_SLEEP_DURATION_EXCEPTION : $e")
            "-"
        }
    }

    suspend fun getCircadianAlignmentCardData(): OHealthOverview? {

//        val isOnboardingDone = localDataStore.isCircadianOnboardShown()
        val graphData = circadianGraphData
        val isOnboard = graphData?.onboarding ?: false
        return if(isOnboard){
            val isLocked = graphData?.isLockedCircularView?:false
            if (isLocked) {
                OHealthOverview.CircadianLockedOrNoSleepCard(
                    isLocked = true
                )
            }else{
                if(
                    graphData?.startTime != null && graphData.endTime != null &&
                    graphData.sleepData?.wakeTime != null && graphData.sleepData?.bedTime != null
                ){
                    var sTime: LocalTime?
                    var eTime: LocalTime?
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                    val startDateTime = LocalDateTime.parse(graphData.startTime, formatter)
                    val endDateTime = LocalDateTime.parse(graphData.endTime, formatter)

                    val startTime = LocalTime.of(startDateTime.hour, startDateTime.minute)
                    var endTime = LocalTime.of(endDateTime.hour, endDateTime.minute)

                    val endDateTimeSleep =
                        LocalDateTime.parse(graphData.sleepData?.wakeTime, formatter)

                    if (endDateTimeSleep != null) {
                        val sleepWakeTime =
                            LocalTime.of(endDateTimeSleep.hour, endDateTimeSleep.minute)
                        endTime = sleepWakeTime
                    }

                    sTime = startTime
                    eTime = endTime


                    val circData = OHealthOverview.CircadianAlignment(
                        startTime = sTime,
                        endTime = eTime,
                        timeWindow = getCircadianScrollGraphList(graphData),
                        title = nudgeCircadianData?.title,
                        description = nudgeCircadianData?.description,
                        energyGraph = getEnergyValues(graphData,true)
                    )
                    withContext(Dispatchers.Main) {
                        stateCircadianCard.value = Pair(
                            circData,
                            false
                        )
                    }
                    if(isNudgeCircadianApiCalled.not()) {
                        isNudgeCircadianApiCalled = true
                        getNudgeCircadianData()
                    }
                    circData
                }else{
                    OHealthOverview.CircadianLockedOrNoSleepCard(
                        isLocked = false
                    )
                }
            }
        }else{
            OHealthOverview.CircadianAlignmentOnboarding
        }
    }

    private fun getNudgeCircadianData() {
        viewModelScope.launch {
            val reqObj = JsonObject().apply {
                this.addProperty("type", "circadian")
            }
            userRepositoryOld.getNudgeCircadianData(reqObj).collect{resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        isNudgeCircadianApiCalled = false
                    }

                    is Resource.Loading -> {

                    }

                    is Resource.NetworkError -> {
                        isNudgeCircadianApiCalled = false
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            val data  = stateCircadianCard.value?.copy()?.first
                            nudgeCircadianData = it
                            updateNudgeInMainViewModel.postValue(Event(it))
                            if(data!=null){
                                stateCircadianCard.postValue(
                                    Pair(
                                        data.apply {
                                            this.title = it.title
                                            this.description = it.description
                                        },
                                        true
                                    )
                                )
                            }


                        }
                    }
                }
            }
        }
    }

    fun getEnergyValues(graphData: CircadianGraphData?,rotate: Boolean): List<Float> {
        if(graphData?.firstFocusPeakWindowGraph==null && graphData?.secondFocusPeakWindowGraph==null){
            return ArrayList()
        }

        val graphStart = graphData.startTime
        val graphEnd = graphData.sleepData?.wakeTime

        if(graphStart==null || graphEnd==null) return ArrayList()


        val totalMinutes = 24 * 60
        val energyValues = MutableList(totalMinutes) { 0f }

        val windows = listOfNotNull(
            graphData.firstFocusPeakWindowGraph,
            graphData.secondFocusPeakWindowGraph
        )

        for (window in windows) {
            val startMin = timeToMinutes(window.startTime)
            val endMin = timeToMinutes(window.endTime)
            val peakMin = timeToMinutes(window.peakTime)

            if (startMin != null && endMin != null && peakMin != null) {
                for (minute in startMin..endMin) {
                    val dist = (minute - peakMin).toFloat()
                    // Gaussian-like curve: highest at peak, lower at edges
                    val sigma = (endMin - startMin) / 6f // spread factor
                    val energy = exp(-0.5f * (dist / sigma).pow(2))
                    energyValues[minute % totalMinutes] += energy.toFloat()
                }
            }
        }

        val maxVal = energyValues.maxOrNull() ?: 1f
        val values =  energyValues.map { it / maxVal }
        if(rotate){
            return trimArrayByTime(values, graphStart)
        }else{
            return values
        }

    }

    fun trimArrayByTime(
        array: List<Float>,
        startTime: String
    ): List<Float> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val start = LocalDateTime.parse(startTime, formatter)

        val startIndex = start.hour * 60 + start.minute

        if (array.size != 24 * 60) {
            return ArrayList()
        }

        return array.drop(startIndex) + array.take(startIndex)
    }

    private fun timeToMinutes(time: String?): Int? {
        if (time == null) return null
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = sdf.parse(time)
            val cal = Calendar.getInstance()
            cal.time = date!!
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        } catch (e: Exception) {
            null
        }
    }

    fun getCircadianScrollGraphList(
        circadianGraphData: CircadianGraphData?,
    ): List<TimeWindow> {
        val data = ArrayList<TimeWindow>()
        circadianGraphData?.caffeineWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#A1734E".toColorInt(),
                    "#D6A176".toColorInt(),
                    "#FFFFFF".toColorInt(),
                    rowIndex = 1,
                    label = resourceProvider.getString(R.string.text_caffeine_window_open)
                )
            )
            circadianGraphData?.sleepData?.let { sleepTime->
                if (sleepTime.wakeTime == null || sleepTime.bedTime == null) return@let

                val wakeTime = LocalDateTime.parse(
                    sleepTime.wakeTime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ).plusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm"))
                val bedTime = LocalDateTime.parse(
                    sleepTime.bedTime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ).format(DateTimeFormatter.ofPattern("HH:mm"))

                data.add(
                    TimeWindow(
                        getCircadianTimeFloatValue(wakeTime),
                        getCircadianTimeFloatValue(it.startTime),
                        "#CC2E2422".toColorInt(),
                        "#CC2E2422".toColorInt(),
                        "#B2D69B92".toColorInt(),
                        rowIndex = 1,
                        label = resourceProvider.getString(R.string.text_avoid_caffeine)
                    )
                )

                val wakeTimeEnd = LocalDateTime.parse(
                    sleepTime.wakeTime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ).minusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm"))

                data.add(
                    TimeWindow(
                        getCircadianTimeFloatValue(it.endTime),
                        getCircadianTimeFloatValue(wakeTimeEnd),
                        "#CC2E2422".toColorInt(),
                        "#CC2E2422".toColorInt(),
                        "#B2D69B92".toColorInt(),
                        rowIndex = 1,
                        label = resourceProvider.getString(R.string.text_avoid_caffeine)
                    )
                )




            }


        }

        circadianGraphData?.dlmoPhaseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#9E6FC7".toColorInt(),
                    "#4C4192".toColorInt(),
                    "#EBAFFF".toColorInt(),
                    rowIndex = 0,
                    label = resourceProvider.getString(R.string.text_dim_light_phase)
                )
            )
        }

        circadianGraphData?.lightAnchoringPhaseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let

            val startTime = LocalTime.parse(it.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                .plusMinutes(1)
                .format(DateTimeFormatter.ofPattern("HH:mm"))

            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#B2E6EE".toColorInt(),
                    "#FFE0BC".toColorInt(),
                    "#99000000".toColorInt(),
                    rowIndex = 0,
                    label = resourceProvider.getString(R.string.text_natural_light)
                )
            )
        }
        circadianGraphData?.sleepData?.let {
            if (it.wakeTime == null || it.bedTime == null) return@let

            //2025-08-18 01:05:00
            val startTime = LocalDateTime.parse(
                it.wakeTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).format(DateTimeFormatter.ofPattern("HH:mm"))
            val endTime = LocalDateTime.parse(
                it.bedTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).format(DateTimeFormatter.ofPattern("HH:mm"))


            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(endTime),
                    getCircadianTimeFloatValue(startTime),
                    "#33296F".toColorInt(),
                    "#634ED5".toColorInt(),
                    "#9E91E8".toColorInt(),
                    rowIndex = 0,
                    label = resourceProvider.getString(R.string.text_sleep)
                )
            )
        }

        circadianGraphData?.melatoninPrepPhaseWindowGraph?.let {
            if (it.startTime == null || it.endTime == null) return@let
            data.add(
                TimeWindow(
                    getCircadianTimeFloatValue(it.startTime),
                    getCircadianTimeFloatValue(it.endTime),
                    "#613644".toColorInt(),
                    "#BD6FC7".toColorInt(),
                    "#FFAFF2".toColorInt(),
                    rowIndex = 0,
                    label = resourceProvider.getString(R.string.text_evening_wind_down)
                )
            )

            circadianGraphData?.lightAnchoringPhaseWindowGraph?.let { it1 ->

                data.add(
                    TimeWindow(
                        getCircadianTimeFloatValue(it1.endTime),
                        getCircadianTimeFloatValue(it.startTime),
                        "#33646464".toColorInt(),
                        "#33646464".toColorInt(),
                        "#5A5A5A".toColorInt(),
                        rowIndex = 0,
                        label = resourceProvider.getString(R.string.text_neutral_light_zone)
                    )
                )
            }


        }

        return data
    }

    private fun getCircadianTimeFloatValue(time: String?): Float{
        if (time == null) {
            return -1f
        }


        val timee = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
        return timee.hour + timee.minute / 60f
    }

    private fun getCaffeineCardData(): OHealthOverview? {
        if (caffeineGraphData == null || caffeineGraphData?.status == null) {
            return null
        }

        if (caffeineGraphData?.status == false) {
            return OHealthOverview.CaffeineWindowCalibrating(
                caffeineGraphData?.title,
                caffeineGraphData?.message
            )
        }
        val data = caffeineGraphData?.let {
            val caffeineValues = ArrayList<Int>()
            caffeineGraphData!!.caffeine_window.forEach {
                caffeineValues.add(it.dose)
            }

            OHealthOverview.CaffeineWindow(
                CaffeineWindowData(
                    wakeUpTime = it.wakeUpTime,
                    bedTime = it.bedTime,
                    caffeineStartTime = it.caffeineStartTime,
                    caffeineEndTime = it.caffeineEndTime,
                    caffeineValues = caffeineValues,
                )
            )
        }

//        val data = CaffeineWindowData(
//            wakeUpTime = "07:00:00",
//            bedTime = "23:00:00",
//            caffeineStartTime = "09:00:00",
//            caffeineEndTime = "18:00:00",
//            caffeineValues = listOf(
//                50, 45, 40, 35, 30, 25, 20, 15
//            ),
//            message = null
//        )
        val (message, maxQuantity) = getMaxQuantityAndMessage(data!!.data)

        data.data.apply {
            this.message = message
            this.maxQuantity = maxQuantity
        }

        return data
    }

    fun getMaxQuantityAndMessage(data: CaffeineWindowData): Pair<String, Int?> {

        val graphStart = LocalTime.parse(data.wakeUpTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        val graphEnd = LocalTime.parse(data.bedTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        val caffeineStart =
            LocalTime.parse(data.caffeineStartTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        val caffeineEnd =
            LocalTime.parse(data.caffeineEndTime, DateTimeFormatter.ofPattern("HH:mm:ss"))

        val messageList = listOf(
            listOf(
                resourceProvider.getString(R.string.text_avoid_caffeine_now_message5),
                resourceProvider.getString(R.string.text_avoid_caffeine_now_message6)
            ),
            listOf(
                resourceProvider.getString(R.string.text_boost_focus_and_alertness_this_is_your_optimal_window_to_enjoy_caffeine_for_peak_performance),
                resourceProvider.getString(R.string.text_maximize_energy_and_mental_clarity_this_is_your_ideal_time_for_a_caffeine_boost_that_works)
            ),
            listOf(
                resourceProvider.getString(R.string.text_avoid_caffeine_now_message3),
                resourceProvider.getString(R.string.text_avoid_caffeine_now_message4)
            ),

            )

        val todayDate = LocalDate.now()
        val now = LocalDateTime.now()

        val graphStartDate = LocalDateTime.of(todayDate, graphStart)
        val caffeineStartDate = LocalDateTime.of(todayDate, caffeineStart)
        val caffeineEndDate = LocalDateTime.of(todayDate, caffeineEnd)
        val graphEndDate = if (caffeineEnd <= graphEnd) {//same day case
            LocalDateTime.of(todayDate, graphEnd)
        } else {//Next day case
            LocalDateTime.of(todayDate.plusDays(1L), graphEnd)
        }

        val startOffset = graphStartDate.minusHours(3)

        var highlightState: HighlightState? = null
        var message: String? = null
        when {
            now in startOffset..caffeineStartDate -> {
                message = messageList[0][Random.nextInt(0, 2)]
                highlightState = HighlightState.START
            }

            now in caffeineStartDate..caffeineEndDate -> {
                message = messageList[1][Random.nextInt(0, 2)]
                highlightState = HighlightState.CAFFEINE
            }

            now in caffeineEndDate..graphEndDate -> {
                message = messageList[2][Random.nextInt(0, 2)]
                highlightState = HighlightState.END
            }

            else -> {
                message = messageList[2][Random.nextInt(0, 2)]
                highlightState = HighlightState.END
            }
        }

        if (highlightState == HighlightState.START || highlightState == HighlightState.END) {
            return Pair(message ?: "", null)
        }

        val highlightedIndex = getTimeIndex(
            caffeineStart!!,
            caffeineEnd!!,
            LocalTime.now(),
            data.caffeineValues.size
        )

        return if (highlightedIndex != -1) {
            Pair(message ?: "", data.caffeineValues[highlightedIndex])
        } else {
            Pair(message ?: "", null)
        }

    }

    fun getTimeIndex(
        startTime: LocalTime,
        endTime: LocalTime,
        currentTime: LocalTime,
        parts: Int
    ): Int {
        if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) return -1

        val totalDuration = Duration.between(startTime, endTime).toMinutes()
        val interval = totalDuration / parts

        val minutesSinceStart = Duration.between(startTime, currentTime).toMinutes()

        return (minutesSinceStart / interval).toInt().coerceAtMost(parts - 1)
    }


    private fun getWorkoutHistoryCard(activity: OreoActivityModel?): OHealthOverview? {
        return if (activity != null) {
            OHealthOverview.WorkoutHistoryCardData(
                activity.workout ?: ArrayList()
            )
        } else {
            null
        }
    }

    private fun getSevenDaysTrendsDataCard(
        trendsData: TrendsData?
    ): OHealthOverview? {
//        stateSleepAvgCard.postValue(
//            Pair(trendsData?.sleepScoreAvg, trendsData?.activityScoreAvg)
//        )
//        stateReadinessAvgCard.postValue(trendsData?.readinessScoreAvg)
        val chartModelSleep = convertIntToChartModel(trendsData?.sleepScoreAvg?.value)
        val chartModelActivity = convertIntToChartModel(trendsData?.activityScoreAvg?.value)
        val chartModelReadiness = convertIntToChartModel(trendsData?.readinessScoreAvg?.value)
        val chartModelEmpty = convertIntToChartModel(arrayListOf(0, 0, 0, 0, 0, 0, 0))
        return OHealthOverview.SevenDayTrendsCard(
            trendsData,
            chartModelSleep,
            chartModelActivity,
            chartModelReadiness,
            chartModelEmpty
        )
    }

    private fun getCycleTrackerCard(): OHealthOverview? {
        var gotYourPeriodCard: OHealthOverview.GotYourPeriod? = null
        val (hasDataLoaded, femaleData) = femaleHealthData

        if (!hasDataLoaded) return null
        var trackFemaleHealthCard: OHealthOverview.CardTrackFemaleHealth? = null


        val data = if (femaleData == null) {
            if (gender.equals("female", true)) {
                val lastShownDays = localDataStore.getFMHWalkthroughRemindLaterDays()
                if (localDataStore.getFMHWalkthroughShownStatus().not() && lastShownDays > 7) {
                    //OHealthOverview.CardTrackFemaleHealth(FemaleHealthCardState.TRACK)
                    trackFemaleHealthCard = OHealthOverview.CardTrackFemaleHealth(
                        FemaleHealthCardState.TRACK
                    )
                    null
                } else {
                    null
                }
            } else {
                null
            }
        } else {
            if (femaleData.isTrackPregnancy != true) {
                if (femaleData.currentDay == null) {
                    trackFemaleHealthCard = OHealthOverview.CardTrackFemaleHealth(
                        FemaleHealthCardState.LOG
                    )
                    null
                    //OHealthOverview.CardTrackFemaleHealth(FemaleHealthCardState.LOG)
                } else {
                    val isCardShownForToday =
                        femaleHealthRepository.getGotPeriodClickedStatus()
                    if (femaleData.isPeriod && !femaleData.otaLog && !isCardShownForToday) {

                        val periodCurrentDay = femaleData.currentDay
                        val dayMessage = if (periodCurrentDay == null) {
                            null
                        } else {
                            resourceProvider.getString(
                                R.string.text_today_s_your_predicted_day,
                                ApplicationUtils.getOrdinalWord(
                                    periodCurrentDay, resourceProvider
                                )
                            )
                        }

                        gotYourPeriodCard = OHealthOverview.GotYourPeriod(
                            dayMessage,
                            femaleData.currentDay
                        )
                    }


                    if (femaleData.isOvulation || femaleData.isPeriod) {
                        OHealthOverview.CycleTrackerCardBig(convertToPeriodBigCardModel(femaleData))
                    } else {
                        OHealthOverview.CycleTrackerCardSmall(
                            convertToPeriodSmallCardModel(
                                femaleData
                            )
                        )
                    }
                }
            } else {
                null
            }
        }
        gotYourPeriodData.postValue(gotYourPeriodCard)
        trackFemaleHealthCardData.postValue(trackFemaleHealthCard)

        return data
    }

    private fun getLunaAiCard(): OHealthOverview? {
        return if (enableAi) {
            val dailyHealthDigestState: SummaryStates = getSummaryStates(summaryAvailable)
            stateLunaAiCard.postValue(
                OHealthOverview.LunaAiCard(
                    dailyHealthDigestCardState = dailyHealthDigestState
                )
            )
            stateLunaAiCard.value
        } else {
            null
        }
    }

    private fun getSummaryStates(summaryAvailable: Boolean?): SummaryStates {
        if (summaryAvailable == true) {
            return SummaryStates.DATA_AVAILABLE
        }
        val isDeviceConnected = ringDataStore.getRingDevice() != null
        if (isDeviceConnected.not()) {
            return SummaryStates.NO_DEVICE
        } else {
            return SummaryStates.NO_DATA
        }
    }

    fun getDailyGoalsCard(): OHealthOverview? {
        return if (notificationGoalsCardData != null) {
            val prevData = notificationGoalsCardData

            val isMetric = sessionManager.isMetric()

            val hydrateGoal = prevData?.hydration_required ?: 3000
            val hydrate = prevData?.hydration ?: 0

            var hydratePercent = 0f
            if (isMetric) {
                hydratePercent = (hydrate.toFloat() / hydrateGoal.toFloat()) * 100
            } else {
                val convertedHydrate = hydrate.toFloat() * 0.033814
                val convertedHydrateGoal =
                    convertMlToOuncesRounded(hydrateGoal.toDouble())

                hydratePercent = (convertedHydrate.toFloat() / convertedHydrateGoal.toFloat()) * 100
            }

            val data = com.oreo.data.model.NotificationGoals(
                hydration = hydrate,
                hydration_required = prevData?.hydration_required,
                steps = prevData?.steps,
                steps_required = prevData?.steps_required,
                isMetric = isMetric,
                convertedHydrateGoal = convertMlToOuncesRounded(hydrateGoal.toDouble()),
                hydratePercent = hydratePercent,
                notificationToggleModel = notificationToggleModel?.copy(),
                glassImage = getGlassImage(hydratePercent.toInt())
            )

            OHealthOverview.DailyGoalsCardData(
                data
            )
        } else {
            null
        }
    }

    private fun getHealthMonitorData(sleep: OreoSleepModel?): OHealthOverview? {
        return sleep?.healthTrend?.let {
            OHealthOverview.HealthMonitorCard(it)
        }
    }

    suspend fun getHeartRateCard(): OHealthOverview? {
        val device = ringDataStore.getRingDevice()
        val data: OHealthOverview.HeartRateDataModel? = userRepository.getSummaryHRHealthOverview()

        stateHeartRateCard.postValue(data.apply {
            if (device == null) {
                this?.measureState = TapMeasureState.NO_DEVICE
            }
        })

        return if (data != null) {

            data.apply {
                this.hrCombineModel = hrDataConvertor.getHrCombinedData(
                    serverUserHealthData,
                    this
                )

                val (lastMeasuredValue, lastMeasuredIndex) = getLastMeasuredValue(this.rawData)
                this.lastMeasuredValue = lastMeasuredValue
                this.lastMeasuredIndex = lastMeasuredIndex

                getHrTrend(this.rawData, lastMeasuredIndex)?.let {
                    this.trendPercent = it
                }

                if (device == null) {
                    this.measureState = TapMeasureState.NO_DEVICE
                }


                val alerts = localDataStore.getHrAlerts()

                data.alertCount = alerts?.data?.size ?: 0
            }
        } else {
            null
        }
//        stateHeartRateCard.postValue(userRepository.getSummaryHRHealthOverview().apply {
//            if (device == null) {
//                this?.measureState = TapMeasureState.NO_DEVICE
//            }
//        })
//        return null
    }

    private fun saveHrAlert(
        percent: Int,
        lastMeasuredIndex: Int,
        lastMeasuredValue: Int,
        roundedAverage: Int
    ) {
        localDataStore.saveHrAlert(percent, lastMeasuredIndex, lastMeasuredValue, roundedAverage)
    }

    suspend fun updateHeartRateCard(): OHealthOverview? {
        val device = ringDataStore.getRingDevice()
        return stateHeartRateCard.value?.apply {
            this.hrCombineModel = hrDataConvertor.getHrCombinedData(
                serverUserHealthData,
                this
            )

            val (lastMeasuredValue, lastMeasuredIndex) = getLastMeasuredValue(this.rawData)
            this.lastMeasuredValue = lastMeasuredValue
            this.lastMeasuredIndex = lastMeasuredIndex

            getHrTrend(this.rawData, lastMeasuredIndex)?.let {
                this.trendPercent = it
            }

            if (device == null) {
                this.measureState = TapMeasureState.NO_DEVICE
            }
        }
//        stateHeartRateCard.postValue(userRepository.getSummaryHRHealthOverview().apply {
//            if (device == null) {
//                this?.measureState = TapMeasureState.NO_DEVICE
//            }
//        })
//        return null
    }

    private fun getSleepPlannerDataCard(hasSleep: Boolean): OHealthOverview? {
        return sleepPlannerData.second?.let {
            OHealthOverview.SleepPlannerCard(
                SleepPlannerDisplayModel(
                    it,
                    getPlannerCardState(it, hasSleep)
                )
            )
        }
    }

    private fun saveHrvAlert(
        prevMeasuredValue: Int,
        currentMeasuredValue: Int,
        spike: Int
    ) {
        localDataStore.saveHrvAlert(prevMeasuredValue, currentMeasuredValue, spike)
    }

    private fun getReadinessDataCard(
        healthData: ServerUserHealthData,
        readiness: OreoReadinessModel?,
        impactData: ImpactData?,
        daySlot: Int
    ): OHealthOverview? {
        val sleep = healthData.sleep

        val currentDayHRV = healthData.readiness?.hrv?.value
        val previous14DayAvg = healthData.readiness?.prev14DayAvgHRV


        if (currentDayHRV != null && previous14DayAvg != null && currentDayHRV != 0 && previous14DayAvg != 0) {
            val percentageDrop =
                ((previous14DayAvg - currentDayHRV).toFloat() / previous14DayAvg) * 100
            if ((percentageDrop.roundToInt()) > 30) {
                saveHrvAlert(
                    previous14DayAvg,
                    currentDayHRV,
                    percentageDrop.roundToInt()
                )
            } else {
                localDataStore.removeHrvAlerts()
            }
        } else {
            localDataStore.removeHrvAlerts()
        }

        val alerts = localDataStore.getHrvAlerts(true)

        val alertCount = if (alerts?.data == null || alerts.data.isDeleted == true) {
            0
        } else {
            1
        }

        if (
            /*registerDate == 0 ||*/
            (readiness?.readinessScore?.value ?: 0) <= 0 ||
            healthData.readiness == null
        ) {
            return null
        }

        val readinessModel = ODashboardReadinessModel(
            readinessScore = readiness?.readinessScore?.value,
            status = readiness?.readinessScore?.text?.capitalizeWords(),
            statusCode = readiness?.readinessScore?.status,
            nudges = readiness?.dashNudges,
            totalScoreImpact = readiness?.totalScoreImpact ?: 0,
            noOfNaps = sleep?.naps?.size ?: 0,
            noOfSleeps = sleep?.sleeps?.size ?: 0,
            impact = impactData?.readinessScore,
            alertCount = alertCount
        )

        return when(daySlot){
            0,1 -> OHealthOverview.Readiness(readinessModel)
            2 -> OHealthOverview.ReadinessMinimal(readinessModel)
            else -> null
        }
    }

    private fun getActivityDataCard(
        healthData: ServerUserHealthData,
        impactData: ImpactData?,
    ): OHealthOverview? {

        if ((healthData.activity?.activeCalories ?: 0) <= 0) {
            return null
        }

        val activityModal = ODashboardActivityModel(
            activityScore = healthData.activity?.activityScore?.value,
            activeCalories = healthData.activity?.activeCalories ?: 0,
            inactiveMinutes = healthData.activity?.activityContributors?.stayActive?.value,
            status = healthData.activity?.activityScore?.level?.capitalizeWords(),
            statusCode = healthData.activity?.activityScore?.status,
            nudges = healthData.activity?.dash_nudges,
            steps = healthData.activity?.steps ?: 0,
            impact = impactData?.activityScore
        )

        val caloriesGoal = user?.userGoals?.caloriesGoal ?: 0
        val activeCalories = healthData.activity?.activeCalories ?: 0
        return if (activeCalories in 1..49) {
            OHealthOverview.ActivityMinimal(
                activityModal, caloriesGoal
            )
        }else if(activeCalories >= 50) {
            OHealthOverview.Activity(
                activityModal, caloriesGoal
            )
        }
        else null

    }

    private fun getSleepDataCard(
        healthData: ServerUserHealthData,
        impactData: ImpactData?,
        sleepModel: ODashboardSleepModel,
        daySlot: Int,
    ): OHealthOverview? {
        val newSleepArray = dataConverter.mergeSleepDataV2(
            healthData.sleep?.sleeps,
            healthData.sleep?.naps?.filter { !it.isNextDayNap }
        )

        when(daySlot){
            0 -> {
                if (healthData.sleep?.sleep_score != null) {
                    if ((sleepModel.totalSleep ?: 0) > 0) {
                        return OHealthOverview.Sleep(
                            sleepModel,
                            makeSleepArray(newSleepArray),
                            newSleepArray?.firstOrNull()?.start_time ?: "",
                            newSleepArray?.lastOrNull()?.end_time ?: "",
                            impact = impactData?.sleepScore
                        )
                    }
                } else {
                    return OHealthOverview.SleepWaiting
                }
            }
            1 -> {
                healthData.sleep?.let {
                    if ((sleepModel.totalSleep ?: 0) > 0) {
                        return OHealthOverview.Sleep(
                            sleepModel,
                            makeSleepArray(newSleepArray),
                            newSleepArray?.firstOrNull()?.start_time ?: "",
                            newSleepArray?.lastOrNull()?.end_time ?: "",
                            impact = impactData?.sleepScore
                        )
                    }
                }
            }
            else -> {

                if (healthData.sleep?.sleep_score != null) {
                    if ((sleepModel.totalSleep ?: 0) > 0) {
                        return OHealthOverview.SleepMinimal(
                            sleepModel, makeSleepArray(newSleepArray),
                            impact = impactData?.sleepScore
                        )
                    }
                }
                /*else {
                    if ((sleepModel.totalSleep ?: 0) > 0) {
                        healthData.sleep?.let {
                            return OHealthOverview.Sleep(
                                sleepModel,
                                makeSleepArray(healthData.sleep?.hourly_breakup),
                                healthData.sleep?.hourly_breakup?.firstOrNull()?.start_time
                                    ?: "",
                                healthData.sleep?.hourly_breakup?.lastOrNull()?.end_time
                                    ?: "",
                                impact = impactData?.sleepScore
                            )
                        }
                    }
                }*/

            }
        }

        return null
    }

    suspend fun updateStressCard(): OHealthOverview? {
        val device = ringDataStore.getRingDevice()
        val combinedData = serverUserHealthData?.let {
            oreoStressDataConvertor.getStressCombinedData(
                it
            )
        }

        val stressData = userRepository.getSummaryStressData().apply {
            this?.data = combinedData
            if (device == null) {
                this?.measureState = TapMeasureState.NO_DEVICE
            }
        }

        stateStressCard.value?.apply {
            this.data = combinedData
            if (device == null) {
                this.measureState = TapMeasureState.NO_DEVICE
            }
        }

        val lastMeasuredValue = getLastMeasuredValue(stressData?.listData)
        val stressStatus = getStressStatus(lastMeasuredValue.first)
        val stressTrend = getStressTrend(stressData?.listData, lastMeasuredValue.second)

        return OHealthOverview.StressCard(
            stateStressCard.value,
            lastMeasuredValue,
            stressStatus,
            stressTrend,
            resourceProvider,
            getGeneration(ringDataStore.getRingDevice()?.ringInfo?.serialNoRaw),
            ringDataStore.getRingDevice() != null
        )
    }

    private suspend fun getStressCard(
        healthData: ServerUserHealthData
    ): OHealthOverview? {
        var stressCard: OHealthOverview.StressCard? = null
        val combinedData = oreoStressDataConvertor.getStressCombinedData(healthData)

        val device = ringDataStore.getRingDevice()

        val stressData = userRepository.getSummaryStressData().apply {
            this?.data = combinedData
            if (device == null) {
                this?.measureState = TapMeasureState.NO_DEVICE
            }
        }

        stateStressCard.postValue(stressData)

        val lastMeasuredValue = getLastMeasuredValue(stateStressCard.value?.listData)
        val stressStatus = getStressStatus(lastMeasuredValue.first)
        val stressTrend = getStressTrend(stateStressCard.value?.listData, lastMeasuredValue.second)

        /*if (stressData != null && stressTrend != null) {*/
        stressCard = OHealthOverview.StressCard(
            stressData,
            lastMeasuredValue,
            stressStatus,
            stressTrend,
            resourceProvider,
            getGeneration(ringDataStore.getRingDevice()?.ringInfo?.serialNoRaw),
            ringDataStore.getRingDevice() != null
        )
        /*}*/

        return stressCard
    }

    private fun getGeneration(serialNoRaw: String?): Int {
        if (serialNoRaw == null) return 1

        return try {
            serialNoRaw.substring(1, 2).toInt()
        } catch (exp: Exception) {
            exp.printStackTrace()
            1
        }
    }

    private fun getCardsPriorityFromApi(cards: List<CustomHomeScreenNetworkItem>): List<CustomHomeScreenItem> {
        val list = ArrayList<CustomHomeScreenItem>()
        val map = getItemsMap()
        for (item in cards) {
            val mainItem = map[item.type]
            if (mainItem != null) {
                mainItem.priority = item.priority
                mainItem.switchState = item.switchState
                list.add(mainItem)
            }
        }
        return list
    }

    /*private fun getLunaManagedPriority(hasSleep: Boolean): List<CustomHomeScreenItem> {
        val itemsMap = getItemsMap()
        val priorityList = mutableListOf<CustomHomeScreenItem>()
        val isAfter12 = checkIfIsAfter12()
        val daySlot = getDaySlot() // 0=morning, 1=afternoon, 2=evening, else=default
        val currentTime = DateFormats.getTimeFormat()
        val isBefore8 = DateFormats.isTimeBefore(currentTime, "20:00")

        priorityList.add(itemsMap["caffeine_intake"]!!.copy(priority = priorityList.size))
        when (daySlot) {
            0 -> { // Morning (focus on sleep and readiness)
                priorityList.apply {
                    if (hasSleep) {
                        add(itemsMap["readiness"]!!.copy(priority = priorityList.size))
                        add(itemsMap["luna_ai"]!!.copy(priority = priorityList.size))
                        add(itemsMap["sleep"]!!.copy(priority = priorityList.size))
                        add(itemsMap["circadian_alignment"]!!.copy(priority = priorityList.size))
                        if (isAfter12.not()) {
                            add(itemsMap["health_monitor"]!!.copy(priority = priorityList.size))
                        }
                    } else {
                        add(itemsMap["luna_ai"]!!.copy(priority = priorityList.size))
                        add(itemsMap["sleep"]!!.copy(priority = priorityList.size))
                        add(itemsMap["circadian_alignment"]!!.copy(priority = priorityList.size))
                    }

                    add(itemsMap["sleep_planner"]!!.copy(priority = priorityList.size))
                }
            }

            1 -> { // Afternoon (focus on activity)
                priorityList.apply {
                    if (hasSleep) {
                        add(itemsMap["readiness"]!!.copy(priority = priorityList.size))
                        add(itemsMap["luna_ai"]!!.copy(priority = priorityList.size))
                        add(itemsMap["sleep"]!!.copy(priority = priorityList.size))
                        if (isAfter12.not()) {
                            add(itemsMap["health_monitor"]!!.copy(priority = priorityList.size))
                        }
                    } else {
                        add(itemsMap["luna_ai"]!!.copy(priority = priorityList.size))
                        add(itemsMap["sleep"]!!.copy(priority = priorityList.size))
                    }

                    add(itemsMap["activity"]!!.copy(priority = priorityList.size))
                    add(itemsMap["circadian_alignment"]!!.copy(priority = priorityList.size))
                    add(itemsMap["sleep_planner"]!!.copy(priority = priorityList.size))
                }
            }

            2 -> { // Evening (balanced)
                priorityList.apply {
                    if (hasSleep) {
                        add(itemsMap["readiness"]!!.copy(priority = priorityList.size))
                        add(itemsMap["luna_ai"]!!.copy(priority = priorityList.size))
                        add(itemsMap["sleep"]!!.copy(priority = priorityList.size))

                        if (isAfter12.not()) {
                            add(itemsMap["health_monitor"]!!.copy(priority = priorityList.size))
                        }
                    } else {
                        add(itemsMap["luna_ai"]!!.copy(priority = priorityList.size))
                        add(itemsMap["sleep"]!!.copy(priority = priorityList.size))
                    }

                    add(itemsMap["activity"]!!.copy(priority = priorityList.size))
                    add(itemsMap["circadian_alignment"]!!.copy(priority = priorityList.size))
                    add(itemsMap["sleep_planner"]!!.copy(priority = priorityList.size))
                }
            }

            else -> { // Default/Night
                priorityList.apply {
                    if (isBefore8.not()) {
                        add(itemsMap["sleep_planner"]!!.copy(priority = priorityList.size))
                    }
                    add(itemsMap["activity"]!!.copy(priority = priorityList.size))

                    if (enableAi) {
                        add(itemsMap["luna_ai"]!!.copy(priority = priorityList.size))
                    }
                    if (isBefore8) {
                        add(itemsMap["sleep_planner"]!!.copy(priority = priorityList.size))
                    }
                    if (registerDate != 0) {
                        add(itemsMap["sleep"]!!.copy(priority = priorityList.size))
                        add(itemsMap["readiness"]!!.copy(priority = priorityList.size))
                        add(itemsMap["circadian_alignment"]!!.copy(priority = priorityList.size))
                    }
                    if (!isAfter12) {
                        add(itemsMap["health_monitor"]!!.copy(priority = priorityList.size))
                    }
                }
            }
        }

        priorityList.apply {
            if (isAfter12) {
                add(itemsMap["health_monitor"]!!.copy(priority = priorityList.size))
            }
            add(itemsMap["daily_goals"]!!.copy(priority = priorityList.size))
            add(itemsMap["stress"]!!.copy(priority = priorityList.size))
            add(itemsMap["heart_rate"]!!.copy(priority = priorityList.size))
            add(itemsMap["cycle_tracker"]!!.copy(priority = priorityList.size))
            add(itemsMap["7_day_trends_card"]!!.copy(priority = priorityList.size))
            add(itemsMap["workout_history"]!!.copy(priority = priorityList.size))
        }

        return priorityList
    }*/

    private fun getLunaManagedPriority(hasSleep: Boolean): List<CustomHomeScreenItem> {
        val priorityList = mutableListOf<CustomHomeScreenItem>()

        val itemsMap = getItemsMap()
        val daySlot = getDaySlot()
        priorityList.apply {
            when(daySlot){
                0 ->{
                    add(itemsMap["readiness"]!!.copy(priority = priorityList.size))
                    add(itemsMap["one_tap_vitals"]!!.copy(priority = priorityList.size))
                    add(itemsMap["sleep"]!!.copy(priority = priorityList.size))
                    add(itemsMap["health_monitor"]!!.copy(priority = priorityList.size))
                    add(itemsMap["circadian_alignment"]!!.copy(priority = priorityList.size))
                    add(itemsMap["activity"]!!.copy(priority = priorityList.size))
                    add(itemsMap["heart_rate"]!!.copy(priority = priorityList.size))
                    add(itemsMap["stress"]!!.copy(priority = priorityList.size))
                    add(itemsMap["daily_goals"]!!.copy(priority = priorityList.size))
                    add(itemsMap["cycle_tracker"]!!.copy(priority = priorityList.size))
                    add(itemsMap["caffeine_intake"]!!.copy(priority = priorityList.size))
                    add(itemsMap["sleep_planner"]!!.copy(priority = priorityList.size))
                }

                1 ->{
                    add(itemsMap["circadian_alignment"]!!.copy(priority = priorityList.size))
                    add(itemsMap["one_tap_vitals"]!!.copy(priority = priorityList.size))
                    add(itemsMap["activity"]!!.copy(priority = priorityList.size))
                    add(itemsMap["heart_rate"]!!.copy(priority = priorityList.size))
                    add(itemsMap["stress"]!!.copy(priority = priorityList.size))
                    add(itemsMap["daily_goals"]!!.copy(priority = priorityList.size))
                    add(itemsMap["readiness"]!!.copy(priority = priorityList.size))
                    add(itemsMap["sleep"]!!.copy(priority = priorityList.size))
                    add(itemsMap["health_monitor"]!!.copy(priority = priorityList.size))
                    add(itemsMap["cycle_tracker"]!!.copy(priority = priorityList.size))
                    add(itemsMap["caffeine_intake"]!!.copy(priority = priorityList.size))
                    add(itemsMap["sleep_planner"]!!.copy(priority = priorityList.size))
                }

                else ->{
                    add(itemsMap["circadian_alignment"]!!.copy(priority = priorityList.size))
                    add(itemsMap["sleep_planner"]!!.copy(priority = priorityList.size))
                    add(itemsMap["one_tap_vitals"]!!.copy(priority = priorityList.size))
                    add(itemsMap["activity"]!!.copy(priority = priorityList.size))
                    add(itemsMap["heart_rate"]!!.copy(priority = priorityList.size))
                    add(itemsMap["stress"]!!.copy(priority = priorityList.size))
                    add(itemsMap["daily_goals"]!!.copy(priority = priorityList.size))
                    add(itemsMap["readiness"]!!.copy(priority = priorityList.size))
                    add(itemsMap["sleep"]!!.copy(priority = priorityList.size))
                    add(itemsMap["health_monitor"]!!.copy(priority = priorityList.size))
                    add(itemsMap["cycle_tracker"]!!.copy(priority = priorityList.size))
                    add(itemsMap["caffeine_intake"]!!.copy(priority = priorityList.size))
                }
            }

//            add(itemsMap["workout_history"]!!.copy(priority = priorityList.size))
        }
        return priorityList
    }

    private fun getItemsMap(): Map<String, CustomHomeScreenItem> =
        HashMap<String, CustomHomeScreenItem>().apply {
            this["one_tap_vitals"] = CustomHomeScreenItem(
                R.drawable.icon_heart_monitor,
                "one_tap_vitals",
                resourceProvider.getString(R.string.text_one_tap_vitals),
                true,
                0
            )
            this["sleep"] = CustomHomeScreenItem(
                R.drawable.icon_sleep,
                "sleep",
                resourceProvider.getString(R.string.text_sleep),
                true,
                1
            )

            this["activity"] = CustomHomeScreenItem(
                R.drawable.icon_activity,
                "activity",
                resourceProvider.getString(R.string.text_activity_o),
                true,
                2
            )
            this["readiness"] = CustomHomeScreenItem(
                R.drawable.icon_readiness,
                "readiness",
                resourceProvider.getString(R.string.text_readiness),
                true,
                3
            )
            this["sleep_planner"] = CustomHomeScreenItem(
                R.drawable.icon_sleep_planner,
                "sleep_planner",
                resourceProvider.getString(R.string.text_sleep_planner),
                true,
                4
            )
            this["heart_rate"] = CustomHomeScreenItem(
                R.drawable.icon_heart_rate,
                "heart_rate",
                resourceProvider.getString(R.string.text_heart_rate),
                true,
                5
            )
            this["stress"] = CustomHomeScreenItem(
                R.drawable.icon_flexibility_training,
                "stress",
                resourceProvider.getString(R.string.text_stress),
                true,
                6
            )

            this["daily_goals"] = CustomHomeScreenItem(
                R.drawable.icon_daily_goals,
                "daily_goals",
                resourceProvider.getString(R.string.text_daily_goals),
                true,
                7
            )
            this["luna_ai"] = CustomHomeScreenItem(
                R.drawable.icon_luna_ai,
                "luna_ai",
                resourceProvider.getString(R.string.text_luna_ai),
                true,
                8
            )
            this["cycle_tracker"] = CustomHomeScreenItem(
                R.drawable.icon_cycle_tracker,
                "cycle_tracker",
                resourceProvider.getString(R.string.text_cycle_tracker),
                true,
                9
            )
            this["7_day_trends_card"] = CustomHomeScreenItem(
                R.drawable.icon_7_day_trends_card,
                "7_day_trends_card",
                resourceProvider.getString(R.string.text_7_day_trends_cards),
                true,
                10
            )
            this["workout_history"] = CustomHomeScreenItem(
                R.drawable.icon_flexibility_training,
                "workout_history",
                resourceProvider.getString(R.string.text_workout_history),
                true,
                11
            )

            this["health_monitor"] = CustomHomeScreenItem(
                R.drawable.icon_heart_monitor,
                "health_monitor",
                resourceProvider.getString(R.string.text_heart_monitor),
                true,
                12
            )

            this["caffeine_intake"] = CustomHomeScreenItem(
                R.drawable.icon_heart_monitor,
                "caffeine_intake",
                resourceProvider.getString(R.string.text_heart_monitor),
                true,
                13
            )

            this["circadian_alignment"] = CustomHomeScreenItem(
                R.drawable.icon_heart_monitor,
                "circadian_alignment",
                resourceProvider.getString(R.string.text_circadian_alignment),
                true,
                14
            )

        }

    private fun checkIfIsAfter12(): Boolean {
        return LocalDateTime.now().hour >= 12
    }

    private fun calculateDaysLeft(dateString: String): Long {
        val targetDate = LocalDate.parse(dateString)
        val today = LocalDate.now()
        return ChronoUnit.DAYS.between(today, targetDate)
    }

    private fun convertToPeriodBigCardModel(data: FemaleHealthUserInfoModel): PeriodCard2 {
        val tempVariance = calculateTempVariance(data.temp)

        if (data.isPeriod) {
            val isPeriodLate = data.confirmPeriodDate != null

            return PeriodCard2(
                title = if (isPeriodLate) resourceProvider.getString(R.string.text_period_late_for)
                else if (data.otaLog) resourceProvider.getString(R.string.text_period)
                    .capitalizeWords()
                else resourceProvider.getString(R.string.text_predicted_period),
                subTitle = if (isPeriodLate) resourceProvider.getString(
                    R.string.text_value_day_,
                    data.confirmPeriodDate?.day ?: 0
                )
                else if (data.otaLog) resourceProvider.getString(
                    R.string.text_day_value,
                    data.currentDay ?: 0
                )
                else resourceProvider.getString(R.string.text_day_value, data.currentDay ?: 0),

                nudge = data.nudges?.firstOrNull()?.message ?: "",
                currentCycleDay = data.currentDay ?: 0,
                totalCycleDay = data.cycleLength ?: 0,
                temperatureVariation = tempVariance,
                predictionDate = data.nextPeriodDate ?: "",
                days = data.currentDay ?: 0,
                predictionString = resourceProvider.getString(R.string.text_period_date),
                background = R.drawable.back_card_period_big
            )

        } else {
            return PeriodCard2(
                title = resourceProvider.getString(R.string.text_ovulation).capitalizeWords(),
                subTitle = resourceProvider.getString(
                    R.string.text_day_value,
                    data.currentDay ?: 0
                ),
                nudge = data.nudges?.firstOrNull()?.message ?: "",
                currentCycleDay = data.currentDay ?: 0,
                totalCycleDay = data.cycleLength ?: 0,
                temperatureVariation = tempVariance,
                predictionDate = data.ovulationDate ?: "",
                days = data.currentDay ?: 0,
                predictionString = resourceProvider.getString(R.string.text_ovulation_date),
                background = R.drawable.back_card_ovulation_big
            )
        }
    }

    fun hasHealthData(healthTrend: HealthTrend?): Boolean {
        if (healthTrend == null) return false
        return !(healthTrend.resp?.status.isNullOrEmpty() &&
                healthTrend.rhr?.status.isNullOrEmpty() &&
                healthTrend.bloodOxy?.status.isNullOrEmpty() &&
                healthTrend.hrv?.status.isNullOrEmpty() &&
                healthTrend.skinTemp?.status.isNullOrEmpty())

    }

    private fun calculateTempVariance(list: List<TempPeriodData>?): Float? {
        if (list == null) {
            return null
        }
        return list.getOrNull(0)?.temperature ?: null
    }

    private fun convertToPeriodSmallCardModel(data: FemaleHealthUserInfoModel): PeriodCard1 {

        val daysUntilOvulation = if (data.ovulationDate != null) {
            calculateDaysLeft(data.ovulationDate)
        } else {
            null
        }
        val daysUntilNextPeriod = calculateDaysLeft(data.nextPeriodDate!!)

        if (daysUntilOvulation != null && (daysUntilOvulation < daysUntilNextPeriod && daysUntilOvulation > 0)) {
            val predictedOvulation = LocalDate.parse(data.nextPeriodDate).minusDays(13)
            return PeriodCard1(
                title = resourceProvider.getString(R.string.text_ovulation_in),
                days = daysUntilOvulation.toInt(),
                nudge = data.nudges?.firstOrNull()?.message ?: "",
                currentCycleDay = data.currentDay ?: 0,
                totalCycleDay = data.cycleLength ?: 0,
                bottomText = resourceProvider.getString(R.string.text_ovulation_date),
                predictionDate = data.ovulationDate
                    ?: "",/*predictedOvulation.format(DateTimeFormatter.ofPattern("dd MMM")),*/
                background = R.drawable.back_card_ovulation_small
            )
        } else {
            val isPeriodLate = data.confirmPeriodDate != null

            return PeriodCard1(
                title = if (isPeriodLate) resourceProvider.getString(R.string.text_period_late_for)
                else resourceProvider.getString(R.string.text_period_in),
                days = if (isPeriodLate) data.confirmPeriodDate?.day
                    ?: 0 else daysUntilNextPeriod.toInt(),
                nudge = data.nudges?.firstOrNull()?.message ?: "",
                currentCycleDay = data.currentDay ?: 0,
                totalCycleDay = data.cycleLength ?: 0,
                bottomText = resourceProvider.getString(R.string.text_period_date),
                predictionDate = data.nextPeriodDate ?: "",
                background = R.drawable.back_card_period_small
            )
        }

    }

    /**
     * Returns value and color
     */
    fun getStressStatus(value: Int?): Pair<String, Int> {
        return when (value) {
            0 -> Pair("", Color.parseColor("#FFFFFF"))
            in 1..34 -> Pair(
                resourceProvider.getString(R.string.text_relaxed),
                Color.parseColor("#3FE8B5")
            )

            in 35..69 -> Pair(
                resourceProvider.getString(R.string.text_focussed),
                Color.parseColor("#FFED91")
            )

            in 70..100 -> Pair(
                resourceProvider.getString(R.string.text_stressed),
                Color.parseColor("#FFAD60")
            )

            else -> Pair("", Color.parseColor("#FFFFFF"))
        }
    }

    fun loadNapsToConfirm() {
        viewModelScope.launch(Dispatchers.IO) {
            val naps = userActivityRepository.getNapsToConfirm()
            napsList.postValue(naps ?: ArrayList())

            hasDetectedNaps = !naps.isNullOrEmpty()

            handleGoogleFitCard()
        }
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

            /*if (registerDays == 0) {*/
                trendsData?.welcome?.welcome?.let {
                    userActivities.add(OHealthOverview.InfoRingWelcome(it))
                }
            /*}*/

            val cardClickState = localDataStore.getDashCardClickState()

            trendsData?.welcome?.care?.let {
                /*if (registerDays > 0) {
                    viewedCardsData.add(OHealthOverview.InfoRingCare(it))
                } else {*/
                    if (cardClickState[DashInfoCard.CARE] == false) {
                        userActivities.add(OHealthOverview.InfoRingCare(it))
                    } else {
                        viewedCardsData.add(OHealthOverview.InfoRingCare(it))
                    }
                /*}*/

            }

            /*trendsData?.welcome?.sleep_media?.let {
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
            }*/

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

        return if (DateFormats.isTimeBetween(currentTime, "00:00", "09:59")) {
            0
        } else if (DateFormats.isTimeBetween(currentTime, "10:00", "19:59")) {
            1
        } else {
            2
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

    fun measureStress(status: Boolean) {
        stateStressCard.value?.measureState = TapMeasureState.MEASURING
        stateStressCard.postValue(stateStressCard.value)


        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetManualMeasurement(
                ManualMeasureType.STRESS, status
            )
        )

    }

    fun updateManualValue(type: ManualMeasureType) {

        if (type == ManualMeasureType.STRESS) {
            val manualMeasurement = ringDataStore.getManualMeasurementValueStress()
            if (manualMeasurement != null) {

                if (manualMeasurement.isError) {
                    stateStressCard.value?.measureState = TapMeasureState.ERROR
                } else {
                    if (manualMeasurement.isMeasuring) {
                        stateStressCard.value?.measureState = TapMeasureState.MEASURING
                    } else {
                        stateStressCard.value?.measureState = TapMeasureState.LAST_MEASURED
                        stateStressCard.value?.lastTime =
                            resourceProvider.getString(R.string.text_just_now)
                    }
                    stateStressCard.value?.value = manualMeasurement.value
                }
                stateStressCard.postValue(stateStressCard.value)
            }
        } else if (type == ManualMeasureType.HEART_RATE) {
            val manualMeasurement = ringDataStore.getManualMeasurementValue()
            if (manualMeasurement != null) {

                if (manualMeasurement.isError) {
                    stateHeartRateCard.value?.measureState = TapMeasureState.ERROR
                } else {
                    if (manualMeasurement.isMeasuring) {
                        stateHeartRateCard.value?.measureState = TapMeasureState.MEASURING
                    } else {
                        stateHeartRateCard.value?.measureState = TapMeasureState.LAST_MEASURED
                        stateHeartRateCard.value?.lastTime =
                            resourceProvider.getString(R.string.text_just_now)
                    }
                    stateHeartRateCard.value?.value = manualMeasurement.value.toString()
                }
                stateHeartRateCard.postValue(stateHeartRateCard.value)
            }
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
        //LOGS.d("postUpdateOtaDataOffline ${Gson().toJson(firmwareObj)}")
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
                            if (it.isBlacklistedRing == true) {
                                withContext(Dispatchers.Main) {
                                    sessionManager.forceDisconnect.value = (Event(true))
                                    sessionManager.setConnectStateRing(ConnectState.UnPaired())
                                }

                                showBlackListDialog.postValue(Event(true))

                            } else {
                                updateRepository.saveNewOtaVersion(it.firmwareVersion, pair?.first)

                                postUpdateOtaDataOffline()
                            }
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

                            val enableGoogleFit = localDataStore.isEnableGoogleFit()
                            val syncSleep = localDataStore.getStatusGoogleFitKey("sleep")

                            if (enableGoogleFit && syncSleep) {
                                val zoneOffset =
                                    ZoneId.systemDefault().rules.getOffset(LocalDateTime.now())

                                val startTimeStamp = LocalDateTime.parse(
                                    nap.startTime,
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                ).toEpochSecond(zoneOffset)
                                val endTimeStamp = LocalDateTime.parse(
                                    nap.endTime,
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                ).toEpochSecond(zoneOffset)

                                googleFitDataObservers.insertSleepData(
                                    SleepDataGoogleFit(
                                        startTime = startTimeStamp * 1000,
                                        endTime = endTimeStamp * 1000,
                                        sleepArray = arrayListOf(
                                            SleepDataBreakup(
                                                startTime = startTimeStamp * 1000,
                                                endTime = endTimeStamp * 1000,
                                                sleepType = "light"
                                            )
                                        )
                                    ),
                                    success = {}, failed = {}
                                )
                            }

                            removeNapById(nap)
                            it.firstOrNull()?.let { napData ->
                                if (napData.date != null) {
                                    userHealthDataDataSource.clearDataByDates(listOf(napData.date!!))
                                    delay(100)
                                }
                                onNapAddSuccess.postValue(Event(napData))
                            }
                            handleGoogleFitCard()
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
            scoreImpact = nap.scoreImpact
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

    fun isChatSplashShown(): Boolean {
//        return false
        return localDataStore.isAiChatSplashShown()
    }

    fun getPeriodData() {
        viewModelScope.launch(Dispatchers.IO) {
            if (date == null) return@launch
            gender = localDataStore.getUser()?.userInfo?.gender
            if (gender.equals("female", true).not()) return@launch

            if (localDataStore.getFemaleHealthStatus().not()) return@launch

            femaleHealthRepository.getFemaleHealthUserInfo(date!!).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        //setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getPeriodData()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            femaleHealthData = Pair(true, it)
                            femaleHealthDataLoaded.postValue(Event(true))
                        }
                    }
                }
            }
        }

    }

    fun onGotPeriodClicked(status: Boolean, currentDay: Int) {
        viewModelScope.launch {
            /*if (status.not()) {
                femaleHealthRepository.saveGotPeriodClicked()
                gotYourPeriodData.postValue(null)
                return@launch
            }*/

            val requestObject = JsonObject().apply {
                this.addProperty("date", DateFormats.getTodaysDateString(10))
                this.addProperty("confirm", status)
                this.addProperty("day", currentDay)
            }

            femaleHealthRepository.setPeriodConfirm(requestObject).collect { resource ->
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
                                        onGotPeriodClicked(status, currentDay)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            femaleHealthRepository.saveGotPeriodClicked()
                            gotYourPeriodData.postValue(null)
                            getPeriodData()
                        }
                    }
                }
            }
        }


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

    fun removeSleepAlert() {
        viewModelScope.launch(Dispatchers.IO) {
            ringDataStore.removeSleepAlert(LocalDate.now().toString())
            sleepAlert.postValue(null)
        }
    }

    fun hideFindMyRingPermCard() {
        viewModelScope.launch(Dispatchers.IO) {
            localDataStore.hideFindMyRingLocationCard()
            findMyRingCard.postValue(false)

        }
    }

    private fun checkIfAlarmSetForToday(alarms: PlannerAlarmData?): AlarmTimingsData? {
        if (alarms == null) {
            return null
        }

        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> alarms.tue
            Calendar.TUESDAY -> alarms.wed
            Calendar.WEDNESDAY -> alarms.thu
            Calendar.THURSDAY -> alarms.fri
            Calendar.FRIDAY -> alarms.sat
            Calendar.SATURDAY -> alarms.sun
            Calendar.SUNDAY -> alarms.mon
            else -> null
        }
    }

    fun getSleepPlanerDetails() {
        viewModelScope.launch {

            userActivityRepository.getSleepPlannerDetails().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        //sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        // setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {

                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            sleepPlannerData = Pair(true, it)
                            sleepPlannerDataLoaded.postValue(Event(true))
                        }
                    }
                }
            }
        }
    }

    private fun getPlannerCardState(
        sleepPlannerData: SleepPlannerData,
        sleepExists: Boolean
    ): SleepCardDashState {
        return if (showBreathingExercise(
                sleepPlannerData.planner?.bed_time,
                sleepPlannerData.planner?.wake_time,
                sleepExists,
            )
        ) {
            SleepCardDashState.BreathingExercise
        } else {
            val currentTime = LocalTime.now()
            val nextAlarm = checkIfAlarmSetForToday(sleepPlannerData.alarms)
            if (currentTime.hour >= 14 && nextAlarm == null) {
                SleepCardDashState.SetAlarm
            } else {
                val appOpenCount = localDataStore.getAppOpenCount().second
                if (appOpenCount > 1) {
                    if (nextAlarm != null) {
                        SleepCardDashState.AlarmSet(nextAlarm)
                    } else {
                        SleepCardDashState.SetAlarm
                    }
                } else {
                    SleepCardDashState.None
                }
            }
        }
    }

    private fun showBreathingExercise(
        bedTime: String?,
        wakeTime: String?,
        sleepExists: Boolean
    ): Boolean {
        if (bedTime.isNullOrEmpty() || wakeTime.isNullOrEmpty()) {
            return false
        }

        val dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val currentTime = LocalTime.now()
        //val currentTime = LocalTime.of(0,2)

        try {
            val parsedBedTime = LocalTime.parse(bedTime, dateFormatter)
            val parsedWakeTime = LocalTime.parse(wakeTime, dateFormatter)

            if (parsedBedTime.isAfter(parsedWakeTime)) {
                if (currentTime.isBefore(parsedWakeTime)) {
                    val minutesToWakeTime = ChronoUnit.MINUTES.between(currentTime, parsedWakeTime)
                    return minutesToWakeTime >= 0 && sleepExists.not()
                }

                if (currentTime.isBefore(parsedBedTime)) {
                    val minutesToBedTime = ChronoUnit.MINUTES.between(currentTime, parsedBedTime)
                    return minutesToBedTime <= 60
                }
            } else {
                var minutesToBedTime = ChronoUnit.MINUTES.between(currentTime, parsedBedTime)
                if (minutesToBedTime < 0) {
                    minutesToBedTime = 1440 + minutesToBedTime
                }

                if (minutesToBedTime in 0..60) {
                    return true
                }

                if (!sleepExists && currentTime.isAfter(LocalTime.MIDNIGHT)) {
                    return currentTime.isBefore(parsedWakeTime)
                }
            }

            return false
        } catch (e: IllegalArgumentException) {
            return false
        } catch (e: DateTimeParseException) {
            return false
        }
    }

    /**
     * get last measured value from the list and seconds
     */
    fun getLastMeasuredValue(data: List<Int>?): Pair<Int, Int> {
        if (data.isNullOrEmpty()) return Pair(0, 0)

        val lastIndex = data.indexOfLast { it != 0 && it != 255 }
        if (lastIndex != -1) {
            return Pair(data[lastIndex], lastIndex)
        }
        return Pair(0, 0)
    }

    fun getStressTrend(listData: List<Int>?, lastMeasuredIndex: Int): Int? {

        if (listData.isNullOrEmpty()) return null

        if (lastMeasuredIndex > 1) {
            val lastMeasuredValue = listData[lastMeasuredIndex]
            val secondLastMeasuredValue = listData[lastMeasuredIndex - 1]
            //val thirdLastMeasuredValue = listData[lastMeasuredIndex - 2]

            if (lastMeasuredValue == 0 || secondLastMeasuredValue == 0 ||
                lastMeasuredValue == 255 || secondLastMeasuredValue == 255
            ) {
                return null
            }

            val sum = lastMeasuredValue + secondLastMeasuredValue /*+ thirdLastMeasuredValue*/

            val average = sum.toFloat() / 2
            val roundedAverage = Math.round(average)

            val percentInc = (lastMeasuredValue - roundedAverage).toFloat() / roundedAverage * 100
            val roundedPercentInc = Math.round(percentInc)

            return roundedPercentInc
        }
        return null
    }

    fun getHrTrend(listData: List<Int>?, lastMeasuredIndex: Int): Int? {

        /*saveHrAlert(90,90, 120, 70)
        saveHrAlert(90,100, 120, 70)*/

        if (listData.isNullOrEmpty()) return null

        if (lastMeasuredIndex > 5) {
            val lastMeasuredValue = listData[lastMeasuredIndex]
            val second = listData[lastMeasuredIndex - 1]
            val third = listData[lastMeasuredIndex - 2]
            val fourth = listData[lastMeasuredIndex - 3]
            val fifth = listData[lastMeasuredIndex - 4]
            val sixth = listData[lastMeasuredIndex - 5]

            if (second == 0 || third == 0 || fourth == 0 || fifth == 0 || sixth == 0 || lastMeasuredValue == 0) {
                return null
            }
            if (second == 255 || third == 255 || fourth == 255 || fifth == 255 || sixth == 255 || lastMeasuredValue == 255) {
                return null
            }

            val sum = lastMeasuredValue + second + third + fourth + fifth + sixth

            val average = sum.toFloat() / 6
            val roundedAverage = Math.round(average)

            val percentInc = (lastMeasuredValue - roundedAverage).toFloat() / roundedAverage * 100
            val roundedPercentInc = Math.round(percentInc)

            if (roundedPercentInc > 25) {
                saveHrAlert(roundedPercentInc, lastMeasuredIndex, lastMeasuredValue, roundedAverage)
            }

            return roundedPercentInc
        }
        return null
    }
    //30,30,30,30,30, 60

    fun decreaseHydration() {
        updateHydration(false)
    }

    private fun updateHydration(increase: Boolean) {
        val glassSize = 250

        if (ApplicationUtils.isInternetConnected().not()) {
            sendMessage(resourceProvider.getString(R.string.text_no_internet_connection))
            return
        }

        viewModelScope.launch {
            val lastValue = notificationGoalsCardData?.hydration ?: 0

            if (lastValue == 0 && increase.not()) {
                return@launch
            }

            vibrationUtils.vibrate(HAPTIC_VIBRATION)
            var updatedValue =
                if (increase) (lastValue + glassSize) else (lastValue - glassSize)

            val reqObj = JsonObject().apply {


                if (updatedValue < 0) {
                    updatedValue = 0
                }
                this.addProperty("hydration_amount", updatedValue)
                this.addProperty("date", LocalDate.now().toString())
            }

            notificationGoalsCardData?.hydration = updatedValue

            notificationGoalsCardDataUpdated.postValue(Event(true))

            userRepository.updateHydration(reqObj)
                .collect { resource ->
                    when (resource) {

                        /*is Resource.GenericError -> {
                            sendMessage(resource.message)
                        }

                        is Resource.Loading -> {
                            setLoading(resource.loading)
                        }*/

                        /*is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                this.uiComponentType as UIComponentType.RetryApiDialog
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                    object : BinaryActionCallback {
                                        override fun yes() {
                                            updateHydration(increase)
                                        }

                                        override fun no() {

                                        }
                                    }
                            })
                        }*/

                        is Resource.Success -> {
                            resource.data?.data?.let {
                                /*notificationGoalsCardData.postValue(
                                    notificationGoalsCardData.value?.copy(
                                        hydration = updatedValue
                                    )
                                )*/
                            }
                        }

                        else -> {}
                    }
                }
        }
    }

    fun increaseHydration() {
        updateHydration(true)
    }

    private fun getNotificationGoals() {
        viewModelScope.launch {
            userRepository.getNotificationGoals()
                .collect { resource ->
                    when (resource) {

                        is Resource.Success -> {
                            resource.data?.data?.let {
                                notificationGoalsCardData = (it)
                                notificationGoalsCardDataInit.postValue(Event(true))
                            }
                        }

                        else -> {}
                    }
                }
        }

    }

    fun getNotificationToggle() {
        viewModelScope.launch {
            userRepositoryOld.getNotificationToggle().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            notificationToggleModel = it
                            localDataStore.setShouldShowSleepNotification(
                                it.sleep_notification ?: false
                            )
                            getNotificationGoals()
                        }
                    }

                    else -> {}
                }
            }
        }

    }


    fun updateNotificationToggle(notificationGoal: NotificationGoal) {
        viewModelScope.launch {

            vibrationUtils.vibrate(HAPTIC_VIBRATION)

            val master = notificationToggleModel?.hydrate_notification ?: false == true ||
                    notificationToggleModel?.steps_notification ?: false == true ||
                    notificationToggleModel?.female_health ?: false == true ||
                    notificationToggleModel?.sleep_notification ?: false == true

            val request = JsonObject().apply {
                this.addProperty("master_notification", master)
                this.addProperty(
                    "hydrate_notification",
                    notificationToggleModel?.hydrate_notification ?: false
                )
                this.addProperty(
                    "steps_notification",
                    notificationToggleModel?.steps_notification ?: false
                )
                this.addProperty(
                    "sleep_notification",
                    notificationToggleModel?.sleep_notification ?: false
                )
                this.addProperty(
                    "female_health_notification",
                    notificationToggleModel?.female_health ?: false
                )
            }
            userRepositoryOld.updateNotificationToggle(request)
                .collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {
                            //sendMessage(resource.message)
                        }

                        is Resource.Loading -> {
                            //setLoading(resource.loading)
                        }

                        is Resource.NetworkError -> {
                            /*setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                    object : BinaryActionCallback {
                                        override fun yes() {
                                            updateNotificationToggle(notificationGoal)
                                        }

                                        override fun no() {}
                                    }
                            })*/
                        }

                        is Resource.Success -> {
                            resource.data?.data?.let {

                                if (notificationToggleModel?.hydrate_notification == true ||
                                    notificationToggleModel?.steps_notification == true ||
                                    notificationToggleModel?.female_health == true ||
                                    notificationToggleModel?.sleep_notification == true
                                ) {
                                    notificationToggleModel?.master_notification = true
                                }

                                /*if (notificationGoalsCardData != null) {
                                    notificationGoalsCardData.postValue(notificationGoalsCardData.value)
                                }*/

                                when (notificationGoal) {
                                    NotificationGoal.HYDRATE -> {
                                        notificationUpdatedState.postValue(
                                            Event(
                                                Pair(
                                                    NotificationGoal.HYDRATE,
                                                    notificationToggleModel?.hydrate_notification
                                                        ?: false
                                                )
                                            )
                                        )
                                    }

                                    NotificationGoal.STEPS -> {
                                        notificationUpdatedState.postValue(
                                            Event(
                                                Pair(
                                                    NotificationGoal.STEPS,
                                                    notificationToggleModel?.steps_notification
                                                        ?: false
                                                )
                                            )
                                        )
                                    }
                                }

                            }
                        }
                    }
                }
        }
    }

    fun getGlassImage(percent: Int): Int {
        return when (percent) {
            in 0..10 -> R.drawable.ic_glass_0
            in 11..20 -> R.drawable.ic_glass_20
            in 21..30 -> R.drawable.ic_glass_30
            in 31..40 -> R.drawable.ic_glass_40
            in 41..50 -> R.drawable.ic_glass_50
            in 51..60 -> R.drawable.ic_glass_60
            in 61..70 -> R.drawable.ic_glass_70
            in 71..80 -> R.drawable.ic_glass_80
            in 81..90 -> R.drawable.ic_glass_90
            in 91..99 -> R.drawable.ic_glass_99
            in 100..100 -> R.drawable.ic_glass_100
            in 101..Int.MAX_VALUE -> R.drawable.ic_glass_100
            else -> R.drawable.ic_glass_0
        }
    }

    fun convertMlToOuncesRounded(milliliters: Double): Int {
        val ounces = milliliters / 29.5735
        val roundedOunces = (ounces / 10).roundToInt() * 10
        return roundedOunces
    }

    fun otaRemindLater() {
        updateRepository.otaRemindLater()
    }

    fun appRemindLater() {
        updateRepository.appRemindLater()
    }

    fun shouldShowTimezoneChangedAlert(): Boolean {
        val storedTimezone = localDataStore.getLastKnownTimezone()
        val alertDismissed = localDataStore.isTimezoneChangedAlertCardDismissed()
        return if(storedTimezone==null){
            return false
        }else{
            !alertDismissed
        }
    }

}

enum class NotificationGoal {
    HYDRATE, STEPS
}

data class SleepAlert(
    val title: String,
    val message: String,
    val addSleep: Boolean
)
