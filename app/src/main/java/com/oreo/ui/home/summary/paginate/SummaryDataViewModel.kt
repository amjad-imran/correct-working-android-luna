package com.oreo.ui.home.summary.paginate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.enums.DashInfoCard
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChartModel
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.VideoInfoType
import com.oreo.data.model.health.ODashboardActivityScoreModel
import com.oreo.data.model.health.ODashboardReadinessScoreModel
import com.oreo.data.model.health.ODashboardSleepModel
import com.oreo.data.model.health.ODashboardSleepScoreModel
import com.oreo.data.model.health.OreoDashboardResponseModel
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


    val healthOverviewData = MutableLiveData<ArrayList<OHealthOverview>>()
    val viewedCardsData = MutableLiveData<ArrayList<OHealthOverview>>()


    val stateReadinessAvgCard = MutableLiveData<ODashboardReadinessScoreModel?>()
    val stateSleepAvgCard =
        MutableLiveData<Pair<ODashboardSleepScoreModel?, ODashboardActivityScoreModel?>>()
    val stateHeartRateCard = MutableLiveData<OHealthOverview.HeartRate?>()


    fun parseHealthData(data: OreoDashboardResponseModel) {

        viewModelScope.launch(Dispatchers.IO) {

            val userActivities = ArrayList<OHealthOverview>()
            val viewedCardsData = ArrayList<OHealthOverview>()

            val autoSportCount = userRepository.getSummaryAutoWorkoutCount()
            if (autoSportCount > 0) {
                userActivities.add(OHealthOverview.AutoSport(autoSportCount))
            }

            ringDataStore.setRegisterDay(data.registerDate ?: -1)

            LOGS.w("RESPONSE___ ${Gson().toJson(data)}")

            handleInfoCards(data, userActivities, viewedCardsData)


            when (getDaySlot()) {
                0 -> {
                    //sleep
                    if (data.sleep?.sleepScore != null) {
                        if (data.registerDate != 0) {
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
                        if (data.registerDate != 0) {
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
                    if (data.registerDate != 0) {
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

                    if (data.registerDate != 0) {
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

            stateSleepAvgCard.postValue(Pair(data.sleepScoreAvg, data.activityScoreAvg))
            stateReadinessAvgCard.postValue(data.readinessScoreAvg)

            this@SummaryDataViewModel.viewedCardsData.postValue(viewedCardsData)
            healthOverviewData.postValue(userActivities)

            val device = ringDataStore.getRingDevice()
            stateHeartRateCard.postValue(userRepository.getSummaryHRHealthOverview().apply {
                if (device == null) {
                    this?.measureState = TapMeasureState.NO_DEVICE
                }
            })

        }
    }

    private fun handleInfoCards(
        data: OreoDashboardResponseModel,
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