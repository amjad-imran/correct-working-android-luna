package com.oreo.ui.home.summary.paginate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.common.maxWithoutZero
import com.noisefit_commans.common.minWithoutZero
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
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
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.AlertType
import com.oreo.data.model.ChartModel
import com.oreo.data.model.DashAlert
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OContributorResponseModal
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
    val sessionManager: SessionManager,
    val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {


    val healthOverviewData = MutableLiveData<ArrayList<OHealthOverview>>()
    val stateHeartRateCard = MutableLiveData<OHealthOverview.HeartRate?>()

    val stateWorkouts = MutableLiveData<List<OActivityListModal>>()


    var contributorInfo: OContributorResponseModal? = null
    val hrInfo = MutableLiveData<Event<String>>()
    var sleepScoreInfo = MutableLiveData<Event<String>>()
    var readinessScoreInfo = MutableLiveData<Event<String>>()
    var activityScoreInfo = MutableLiveData<Event<String>>()

    var user: User? = null


    fun parseHealthData(healthData: ServerUserHealthData) {

        viewModelScope.launch(Dispatchers.IO) {

            val data = healthData.dashboard ?: return@launch

            val userActivities = ArrayList<OHealthOverview>()

            val autoSportCount = userRepository.getSummaryAutoWorkoutCount()
            if (autoSportCount > 0) {
                userActivities.add(OHealthOverview.AutoSport(autoSportCount))
            }

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

            healthOverviewData.postValue(userActivities)

            stateHeartRateCard.postValue(parseHrData(healthData))

            stateWorkouts.postValue(healthData.activity?.workout ?: ArrayList())

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

}