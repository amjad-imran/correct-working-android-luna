package com.oreo.ui.home.summary.paginate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.dataConverter.DataConverter
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.common.maxWithoutZero
import com.noisefit_commans.common.minWithoutZero
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.User
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.oreo.data.dataConverter.OreoHRDataConvertor
import com.oreo.data.model.HRModel
import com.oreo.data.dataConverter.OreoStressDataConvertor
import com.oreo.data.model.AlertType
import com.oreo.data.model.ChartModel
import com.oreo.data.model.DashAlert
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.health.ODashboardActivityModel
import com.oreo.data.model.health.ODashboardReadinessModel
import com.oreo.data.model.health.ODashboardSleepModel
import com.oreo.data.model.health.SleepHourlyBreakup
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

@HiltViewModel
class SummaryDataViewModel @Inject constructor(
    val userRepository: OreoUserActivityRepository,
    val ringDataStore: RingDataStore,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val dataConverter: DataConverter,
    val oreoStressDataConvertor: OreoStressDataConvertor,
    val userActivityRepository: OreoUserActivityRepository,
    val hrDataConvertor: OreoHRDataConvertor
) : BaseViewModel() {


    var serverUserHealthData: ServerUserHealthData? = null
    var date: String? = null
    var shouldShowStressCard = false
    val healthOverviewData = MutableLiveData<ArrayList<OHealthOverview>>()
    val stateHeartRateCard = MutableLiveData<OHealthOverview.HeartRateDataModel?>()

    val stateWorkouts = MutableLiveData<List<OActivityListModal>>()


    var contributorInfo: OContributorResponseModal? = null
    val hrInfo = MutableLiveData<Event<String>>()
    var sleepScoreInfo = MutableLiveData<Event<String>>()
    var readinessScoreInfo = MutableLiveData<Event<String>>()
    var activityScoreInfo = MutableLiveData<Event<String>>()

    var user: User? = null


    fun parseHealthData(healthData: ServerUserHealthData) {

        val nap = healthData.sleep?.naps ?: ArrayList()

        viewModelScope.launch(Dispatchers.IO) {


            val userActivities = ArrayList<OHealthOverview>()

            healthData.readiness.let {
                if ((it?.readinessScore?.value ?: 0) > 0) {
                    userActivities.add(
                        OHealthOverview.Readiness(
                            ODashboardReadinessModel(
                                readinessScore = it?.readinessScore?.value,
                                status = it?.readinessScore?.text?.capitalizeWords(),
                                nudges = it?.dashNudges,
                                readinessNapScoreImpact = healthData.readiness?.readinessNapScoreImpact,
                                noOfNaps = healthData.readiness?.noOfNaps
                            )
                        )
                    )
                }
            }
            val filteredNaps = nap.filter { !it.isNextDayNap }

            val newSleepArray = dataConverter.mergeSleepData(
                healthData.sleep?.hourly_breakup, filteredNaps
            )

            healthData.sleep.let {
                if ((it?.sleepScore?.value ?: 0) > 0) {
                    userActivities.add(
                        OHealthOverview.Sleep(
                            ODashboardSleepModel(
                                sleepScore = it?.sleepScore?.value,
                                totalSleep = it?.totalSleep?.value,
                                restingHr = healthData.sleep?.restingHr?.value,
                                sleepStage = it?.hourly_breakup ?: ArrayList(),
                                status = it?.sleepScore?.text?.capitalizeWords(),
                                startTime = newSleepArray?.firstOrNull()?.start_time ?: "",
                                endTime = newSleepArray?.lastOrNull()?.end_time ?: "",
                                sleepNapScoreImpact = healthData.sleep?.sleepNapScoreImpact ?: 0,
                                noOfNaps = healthData.sleep?.noOfNaps ?: 0
                            ),
                            makeSleepArray(newSleepArray),
                            newSleepArray?.firstOrNull()?.start_time ?: "",
                            newSleepArray?.lastOrNull()?.end_time ?: ""
                        )
                    )
                }

            }
            if (filteredNaps.isNotEmpty()) {
                userActivities.add(OHealthOverview.NapDashCard(filteredNaps, healthData.date))
            }

            healthData.activity.let {
                if ((it?.activityScore?.value ?: 0) > 0) {
                    val caloriesGoal = user?.userGoals?.caloriesGoal ?: 0
                    userActivities.add(
                        OHealthOverview.Activity(
                            ODashboardActivityModel(
                                activityScore = it?.activityScore?.value,
                                activeCalories = it?.activeCalories ?: 0,
                                inactiveMinutes = it?.activityContributors?.stayActive?.value,
                                status = it?.activityScore?.level?.capitalizeWords(),
                                nudges = it?.dash_nudges
                            ), caloriesGoal
                        )
                    )
                }
            }

            if (shouldShowStressCard) {
                userActivities.add(
                    OHealthOverview.StressGraph(
                        oreoStressDataConvertor.getStressCombinedData(healthData),
                        healthData.stress?.stressValue?.value ?: 0,
                        healthData.stress?.stressValue?.lastUpdated ?: 0L,
                        getStressStatus(healthData.stress?.stressValue?.value ?: 0),
                        false
                    )
                )
            }

            healthOverviewData.postValue(userActivities)

            stateHeartRateCard.postValue(parseHrData(healthData))

            stateWorkouts.postValue(healthData.activity?.workout ?: ArrayList())

        }
    }

    private fun getStressStatus(value: Int?): String {
        return when (value) {
            0 -> ""
            in 1..34 -> "Calm"
            in 35..69 -> "Focussed"
            in 70..100 -> "Stressed"
            else -> ""
        }
    }

    private fun parseHrData(data: ServerUserHealthData): OHealthOverview.HeartRateDataModel {
        var breakupArray = data.heart?.break_up
        if (breakupArray.isNullOrEmpty()) {
            val dummyArray = ArrayList<Int>()
            for (i in 0..287) {
                dummyArray.add(0)
            }
            breakupArray = dummyArray
        }
        var lastHrValue: Pair<Int, Long>? = null//HR value,timer
        breakupArray.forEachIndexed { index2, value ->
            if (value != 0 && value != 255)
                lastHrValue = Pair(value, 0)

        }

        val excludeDataList = arrayListOf<Int>()
        breakupArray.forEach { value ->
            if (value == 255) {
                excludeDataList.add(0)
            } else excludeDataList.add(value)
        }

        val hRWithIntervalList = excludeDataList.chunked(6)
        val avgList = ArrayList<Int>()
        var overAllMinValue = Int.MAX_VALUE
        var overAllMaxValue = -1
        var hrCount = 0
//        var lastHrValue: Pair<Int, Long>? = null//HR value,timer

        val listData = ArrayList<HRModel>()
        hRWithIntervalList.forEachIndexed { index, hrList ->
            val sortedBreakUpList = hrList.sorted()

            val minValue = sortedBreakUpList.minWithoutZero()
            val maxValue = sortedBreakUpList.maxWithoutZero()

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
                    overAllMinValue = min
                }
                if (max > overAllMaxValue) {
                    overAllMaxValue = max
                }
                avgList.add(avg)
            }

            /* sortedBreakUpList.forEachIndexed { index2, value ->
                 val indexMillis = ((index * 6) + index2) * 5 * 60L * 1000L
                 lastHrValue = Pair(value, indexMillis)

             }*/
            //if any change chunk value then divide 12 by that chunk value to get below correct xlabel list
            if (index % 2 == 0) {
                hrCount += 1

            }
            listData.add(
                HRModel(
                    maxValues = max,
                    minValues = min,
                    values = sortedBreakUpList,
                    midValues = avg
                )
            )
        }

        val average = if (avgList.isEmpty()) 0.0f else avgList.average().toFloat()


        val measureState = TapMeasureState.HIDE
        return OHealthOverview.HeartRateDataModel(
            listData,
            average = average,
            "0",
            value = lastHrValue?.first.toString(),
            maxValues = breakupArray.maxWithoutZero(),
            minValues = breakupArray.minWithoutZero(),
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
            if (hour > 12) hour -= 12;
        } else {
            suffix = "am"
            if (hour == 0) hour = 12;
        }
        return "$hour $suffix"
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