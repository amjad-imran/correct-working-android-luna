package com.oreo.ui.home.summary.paginate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.common.maxWithoutZero
import com.noisefit_commans.common.minWithoutZero
import com.noisefit_commans.data.enums.DashInfoCard
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.User
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.getColor
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.AlertType
import com.oreo.data.model.ChartModel
import com.oreo.data.model.DashAlert
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.VideoInfoType
import com.oreo.data.model.health.ODashboardActivityScoreModel
import com.oreo.data.model.health.ODashboardReadinessScoreModel
import com.oreo.data.model.health.ODashboardSleepModel
import com.oreo.data.model.health.ODashboardSleepScoreModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SummaryDataViewModel @Inject
constructor(
    val userRepository: OreoUserActivityRepository,
    val ringDataStore: RingDataStore,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {


    val stateHeaderCard = MutableLiveData<Pair<String, String>>()//Name,Date
    val healthOverviewData = MutableLiveData<ArrayList<OHealthOverview>>()
    val viewedCardsData = MutableLiveData<ArrayList<OHealthOverview>>()
    val statePairDeviceCard = MutableLiveData<Boolean>()
    val stateDashRingBattery = MutableLiveData<Pair<Boolean, ColorFitDevice?>>()
    val stateDashAlerts = MutableLiveData<HashMap<AlertType, DashAlert>>()


    val stateReadinessAvgCard = MutableLiveData<ODashboardReadinessScoreModel?>()
    val stateSleepAvgCard =
        MutableLiveData<Pair<ODashboardSleepScoreModel?, ODashboardActivityScoreModel?>>()
    val stateHeartRateCard = MutableLiveData<OHealthOverview.HeartRate?>()

    var isToday = false

    var user:User?=null


    fun initTodayData() {

        viewModelScope.launch(Dispatchers.IO) {
            user = localDataStore.getUser()
            stateHeaderCard.postValue(
                Pair(
                    getGreetingMessageValue(),
                    DateFormats.getCurrentDate(DateFormats.dateTimeFormatWithWeekWithoutYear)
                )
            )
            val device = getDeviceConnected()
            statePairDeviceCard.postValue(device == null)
            stateHeartRateCard.postValue(userRepository.getSummaryHRHealthOverview().apply {
                if (device == null) {
                    this?.measureState = TapMeasureState.NO_DEVICE
                }
            })
        }

        updateAlerts()
    }

    fun getGreetingMessageValue(): String {
        return "${getGreetingMessage()}, ${
            user?.getOnlyFirstName()?.trim()?.ifEmpty { "Stranger" }
        }"
    }

    private fun getGreetingMessage(): String {
        val currentTime = DateFormats.getTimeFormat()
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

        if (sessionManager.forceOtaResponseRing != null) {
            dashAlert[AlertType.OTA_UPDATE] =
                DashAlert("Ring firmware update available", false)
        }


        stateDashAlerts.postValue(dashAlert)
    }


    fun parseHealthData(healthData: ServerUserHealthData) {

        viewModelScope.launch(Dispatchers.IO) {

            val data = healthData.dashboard ?: return@launch

            val userActivities = ArrayList<OHealthOverview>()
            val viewedCardsData = ArrayList<OHealthOverview>()

            val autoSportCount = userRepository.getSummaryAutoWorkoutCount()
            if (autoSportCount > 0) {
                userActivities.add(OHealthOverview.AutoSport(autoSportCount))
            }

            if(isToday){
                ringDataStore.setRegisterDay(healthData.registerDate ?: -1)
                handleInfoCards(healthData, userActivities, viewedCardsData)
            }




            when (getDaySlot()) {
                0 -> {
                    //sleep
                    if (data.sleep?.sleepScore != null) {
                        if (healthData.registerDate != 0) {
                            data.readiness?.let {
                                userActivities.add(OHealthOverview.Readiness(data.readiness))
                            }
                            userActivities.add(
                                OHealthOverview.Sleep(
                                    data.sleep,
                                    makeSleepArray(data.sleep),
                                    data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                    data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                                )
                            )
                        }
                    } else {
                        userActivities.add(OHealthOverview.SleepWaiting)
                    }
                }

                1 -> {

                    //sleep
                    if (data.sleep?.sleepScore != null) {
                        if (healthData.registerDate != 0) {
                            data.readiness?.let {
                                userActivities.add(OHealthOverview.Readiness(data.readiness))
                            }
                            userActivities.add(
                                OHealthOverview.Sleep(
                                    data.sleep,
                                    makeSleepArray(data.sleep),
                                    data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                    data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                                )
                            )
                        }
                    } else {
                        userActivities.add(OHealthOverview.SleepWaiting)
                    }

                    //Activity
                    if (data.activity?.activeCalories != null) {
                        val activeCalories = data.activity.activeCalories
                        if (activeCalories in 1..49) {
                            val caloriesGoal = 300//summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.ActivityMinimal(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        } else if (activeCalories >= 50) {
                            val caloriesGoal = 300//summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.Activity(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        } else {
                        }
                    }
                }

                2 -> {
                    if (healthData.registerDate != 0) {
                        data.readiness?.let {
                            userActivities.add(OHealthOverview.Readiness(data.readiness))
                        }

                        data.sleep?.let {
                            userActivities.add(
                                OHealthOverview.Sleep(
                                    data.sleep,
                                    makeSleepArray(data.sleep),
                                    data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                    data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                                )
                            )
                        }
                    }

                    data.activity?.let {

                        val activeCalories = data.activity.activeCalories ?: 0
                        if (activeCalories in 0..49) {
                            val caloriesGoal = 300// summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.ActivityMinimal(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        } else {
                            val caloriesGoal = 300// summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.Activity(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        }
                    }


                }

                else -> {
                    data.activity?.let {

                        val activeCalories = data.activity.activeCalories ?: 0
                        if (activeCalories in 0..49) {
                            val caloriesGoal = 300//summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.ActivityMinimal(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        } else {
                            val caloriesGoal = 300// summary.user?.userGoals?.caloriesGoal ?: 0
                            userActivities.add(
                                OHealthOverview.Activity(
                                    data.activity,
                                    caloriesGoal
                                )
                            )
                        }
                    }

                    if (healthData.registerDate != 0) {
                        if (data.sleep?.sleepScore != null) {
                            userActivities.add(
                                OHealthOverview.SleepMinimal(
                                    data.sleep,
                                    makeSleepArray(data.sleep)
                                )
                            )

                            data.readiness?.let {
                                userActivities.add(OHealthOverview.ReadinessMinimal(data.readiness))
                            }

                        } else {
                            data.sleep?.let {
                                userActivities.add(
                                    OHealthOverview.Sleep(
                                        data.sleep,
                                        makeSleepArray(data.sleep),
                                        data.sleep.sleepStage.firstOrNull()?.startTime ?: "",
                                        data.sleep.sleepStage.lastOrNull()?.endTime ?: ""
                                    )
                                )
                            }
                            data.readiness?.let {
                                userActivities.add(OHealthOverview.Readiness(data.readiness))
                            }
                        }
                    }
                }
            }

            if (isToday) {
                stateSleepAvgCard.postValue(Pair(healthData.sleepScoreAvg, healthData.activityScoreAvg))
                stateReadinessAvgCard.postValue(healthData.readinessScoreAvg)
            }

            this@SummaryDataViewModel.viewedCardsData.postValue(viewedCardsData)
            healthOverviewData.postValue(userActivities)

            if (isToday) {
                val device = ringDataStore.getRingDevice()
                stateHeartRateCard.postValue(userRepository.getSummaryHRHealthOverview().apply {
                    if (device == null) {
                        this?.measureState = TapMeasureState.NO_DEVICE
                    }
                })
            } else {
                stateHeartRateCard.postValue(parseHrData(healthData))
            }
        }
    }

    private fun parseHrData(data: ServerUserHealthData): OHealthOverview.HeartRate {

        var breakupArray = data.heart?.break_up
        if (breakupArray.isNullOrEmpty()) {
            val dummyArray = ArrayList<Int>()
            for (i in 0..287) {
                dummyArray.add(0)
            }
            breakupArray = dummyArray
        }
        val hRWithIntervalList = breakupArray.chunked(6)
        val lineChartList: ArrayList<Entry> = ArrayList()
        val candleChartList: ArrayList<CandleEntry> = ArrayList()
        val lineColorList: ArrayList<Int> = ArrayList()
        val xLabelList = ArrayList<String>()
        val avgList = ArrayList<Int>()
        var overAllMinValue = Int.MAX_VALUE
        var overAllMaxValue = -1
        var hrCount = 0
        var lastHrValue: Pair<Int, Long>? = null//HR value,timer


        hRWithIntervalList.forEachIndexed { index, hrList ->


            val minValue = hrList.minWithoutZero()

            val maxValue = hrList.maxWithoutZero()

            var min = minValue
            var max = maxValue

            if (min == 0 && max != 0) {
                min = max
            }

            if (max == 0 && min != 0) {
                max = min
            }

            val avg = (min + max) / 2
            if (avg != 0) {
                if (min < overAllMinValue) {
                    overAllMinValue = min;
                }
                if (max > overAllMaxValue) {
                    overAllMaxValue = max;
                }
                avgList.add(avg)

            }

            hrList.forEachIndexed { index2, value ->
                if (value != 0) {

                    val indexMillis = ((index * 6) + index2) * 5 * 60L * 1000L
                    LOGS.w("convertHeartRateOverviewData $index $indexMillis")

                    lastHrValue = Pair(value, indexMillis)
                }
            }

            //if any change chunk value then divide 12 by that chunk value to get below correct xlabel list
            if (index % 2 == 0) {
                hrCount += 1

            }


            xLabelList.add(handleHrFormat(hrCount))

            candleChartList.add(
                CandleEntry(
                    index.toFloat(),
                    max.toFloat(),
                    min.toFloat(),
                    max.toFloat(),
                    min.toFloat()
                )
            )

            if (index % 2 == 0) {
                lineColorList.add(R.color.color_error.getColor())
            } else {
                lineColorList.add(R.color.white.getColor())
            }
            lineChartList.add(
                Entry(
                    index.toFloat(),
                    avg.toFloat()
                )
            )
        }

        val average = avgList.average().toFloat()




        if (overAllMinValue == Int.MAX_VALUE) {
            overAllMinValue = 69
        }

        if (overAllMinValue != 0) {
            overAllMinValue -= 9
        }

        val measureState = TapMeasureState.HIDE


        return OHealthOverview.HeartRate(
            "",
            "",
            candleChartList,
            Pair(lineChartList, lineColorList),
            xLabelList, overAllMinValue.toFloat(), average,
            measureState
        )
    }

    private fun handleHrFormat(time: Int): String {

        if (time == 1 || time == 24) {
            return "12 am"
        }


        var hour = time
        var suffix = ""
        if (hour > 11) {
            suffix = "pm"
            if (hour > 12)
                hour -= 12;
        } else {
            suffix = "am"
            if (hour == 0)
                hour = 12;
        }
        return "$hour $suffix"
    }


    private fun handleInfoCards(
        data: ServerUserHealthData,
        userActivities: ArrayList<OHealthOverview>,
        viewedCardsData: ArrayList<OHealthOverview>,
    ) {
        val registerDays = (data.registerDate ?: 0)

        if (registerDays < 7) {

            if (registerDays == 0) {
                data.welcome?.welcome?.let {
                    userActivities.add(OHealthOverview.InfoRingWelcome(it))
                }
            }

            val cardClickState = localDataStore.getDashCardClickState()

            data.welcome?.care?.let {
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

            data.welcome?.sleep_media?.let {
                if (cardClickState[DashInfoCard.SLEEP] == false) {
                    userActivities.add(OHealthOverview.InfoVideo(VideoInfoType.SLEEP, it))
                } else {
                    viewedCardsData.add(OHealthOverview.InfoVideo(VideoInfoType.SLEEP, it))
                }
            }

            data.welcome?.activity_media?.let {
                if (cardClickState[DashInfoCard.ACTIVITY] == false) {
                    userActivities.add(OHealthOverview.InfoVideo(VideoInfoType.ACTIVITY, it))
                } else {
                    viewedCardsData.add(OHealthOverview.InfoVideo(VideoInfoType.ACTIVITY, it))
                }
            }

            data.welcome?.readiness_media?.let {
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

    private fun makeSleepArray(data: ODashboardSleepModel?): ArrayList<SleepData.SleepDataBreakup> {
        val sleepArray: ArrayList<SleepData.SleepDataBreakup> = ArrayList()

        if (data == null) {
            return sleepArray
        }

        var duration = 0

        data.sleepStage.forEachIndexed { index, data1 ->
            val type = data1.sleepType


            if (type?.lowercase() == "awake") {
                if (duration != 0) {

                    sleepArray.add(
                        SleepData.SleepDataBreakup(
                            startTime = data1.startTime,
                            endTime = data1.endTime,
                            sleepType = "DEEP",
                            duration = duration
                        )
                    )
                    duration = 0
                }
                sleepArray.add(
                    SleepData.SleepDataBreakup(
                        startTime = data1.startTime,
                        endTime = data1.endTime,
                        sleepType = "AWAKE",
                        duration = data1.duration ?: 0
                    )
                )
            } else {
                duration += (data1.duration?.toInt()) ?: 0
            }

            if (index == data.sleepStage.size - 1 && duration != 0) {

                sleepArray.add(
                    SleepData.SleepDataBreakup(
                        startTime = data1.startTime,
                        endTime = data1.endTime,
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


}