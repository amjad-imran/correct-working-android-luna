package com.oreo.ui.heartrate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.common.maxWithoutZero
import com.noisefit_commans.common.minWithoutZero
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.HrAlert
import com.noisefit_commans.data.model.HrAlerts
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.dataConverter.OreoHRDataConvertor
import com.oreo.data.model.HRModel
import com.oreo.data.model.IrregularEventsChipModel
import com.oreo.data.model.IrregularEventsChipsListModel
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.ODayTimeActivitiesDataModel
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.TapMeasureState
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OHeartRateDataViewModel @Inject constructor(
    val userRepository: OreoUserActivityRepository,
    val hrDataConvertor: OreoHRDataConvertor,
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore,
    private val resourcesProvider: ResourcesProvider
) : BaseViewModel() {

    var date: String? = null
    val heartRateData = MutableLiveData<OHealthOverview.HeartRateDataModel?>()
    var summaryHealthData: ServerUserHealthData? = null
    var selectedChipsList = ArrayList<ArrayList<IrregularEventsChipModel>>()

    var isEventSubmitted = false

    val hrAlertsData = MutableLiveData<Event<Boolean>>()


    private val todayDate = LocalDate.now().toString()

    fun getLearnMoreData(): ArrayList<LearnMoreDataModel> {
        val dataList = ArrayList<LearnMoreDataModel>()
        dataList.add(
            LearnMoreDataModel(
                title = resourcesProvider.getString(R.string.text_general_heart_rate_terms),
                msg = resourcesProvider.getString(R.string.text__2_min_read),
                img = R.drawable.img_hr_article_1,
                type = 1
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = resourcesProvider.getString(R.string.text_normal_heart_rate_for_my_age),
                msg = resourcesProvider.getString(R.string.text__2_min_read),
                img = R.drawable.img_hr_article_2,
                type = 2
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = resourcesProvider.getString(R.string.text_what_are_heart_rate_zones),
                msg = resourcesProvider.getString(R.string.text__2_min_read),
                img = R.drawable.img_hr_article_3,
                type = 3
            )
        )
        dataList.add(
            LearnMoreDataModel(
                title = resourcesProvider.getString(R.string.text_heart_rate_during_sleep),
                msg = resourcesProvider.getString(R.string.text__2_min_read),
                img = R.drawable.img_hr_article_4,
                type = 4
            )
        )
        return dataList
    }

    fun getTodayHeartRate() {
        viewModelScope.launch(Dispatchers.IO) {
            heartRateData.postValue(userRepository.getSummaryHRHealthOverview().apply {
            })
        }
    }

    fun parseHealthData(healthData: ServerUserHealthData) {
        heartRateData.postValue(parseHrData(healthData))
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
            } else
                excludeDataList.add(value)
        }

        val hRWithIntervalList = excludeDataList.chunked(6)
        val avgList = ArrayList<Int>()
        var overAllMinValue = Int.MAX_VALUE
        var overAllMaxValue = -1
        var hrCount = 0

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
        val average = avgList.average().toFloat()


        val measureState = TapMeasureState.HIDE
        return OHealthOverview.HeartRateDataModel(
            listData = listData,
            rawData = breakupArray,
            average = average,
            lastTime = "0",
            value = lastHrValue?.first.toString(),
            maxValues = breakupArray.maxWithoutZero(),
            minValues = breakupArray.minWithoutZero(),
            measureState = measureState,
            hrCombineModel = null,
            lastMeasuredValue = 0,
            lastMeasuredIndex = 0,
            trendPercent = 0,
            ringGeneration = getGeneration(ringDataStore.getRingDevice()?.ringInfo?.serialNoRaw),

            )
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
    var activityData: ArrayList<ODayTimeActivitiesDataModel>? = null
    fun prepareActivityData(dayData: ServerUserHealthData) {
        val workouts = dayData.activity?.workout
        val sleep = dayData.sleep
        val dataList = ArrayList<ODayTimeActivitiesDataModel>()
        workouts?.forEach {
            dataList.add(
                ODayTimeActivitiesDataModel(
                    type = "Workout",
                    workoutData = it
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
                        endTime = nap.endTime
                    )
                )
            }
        }
        activityData = dataList
    }

    fun getTodayDate(): String {
        return todayDate
    }

    fun getIrregularityEventsAlerts(): HrAlerts? {
        return localDataStore.getHrAlerts()
    }

    fun convertAlertsModel(data: List<HrAlert>): List<IrregularEventsChipsListModel> {

        val dataList = ArrayList<IrregularEventsChipsListModel>()

        val eventData = listOf(
            IrregularEventsChipModel(
                "exercise_or_physical_activity",
                resourcesProvider.getString(R.string.text_exercise_or_physical_activity)
            ),
            IrregularEventsChipModel("stress_or_anxiety",
                resourcesProvider.getString(R.string.text_stress_or_anxiety)),
            IrregularEventsChipModel("feeling_feverish", resourcesProvider.getString(R.string.text_feeling_feverish)),
            IrregularEventsChipModel(
                "caffeine_or_stimulant_intake",
                resourcesProvider.getString(R.string.text_caffeine_or_stimulant_intake)
            ),
            IrregularEventsChipModel("medications", resourcesProvider.getString(R.string.text_medications)),
            IrregularEventsChipModel("others", resourcesProvider.getString(R.string.text_other)),
        )

        data.forEach {
            dataList.add(
                IrregularEventsChipsListModel(
                    eventData,
                    it
                )
            )
        }
        return dataList
    }

    fun loadAlertsData() {
        hrAlertsData.postValue(Event(true))
    }

    fun removeAlert(data: IrregularEventsChipsListModel) {
        val alerts = localDataStore.getHrAlerts()
        if (alerts == null) return

        val list = (alerts.data as ArrayList)

        val index = list.indexOfFirst { it.minutes == data.alert.minutes }
        list[index].isDeleted = true
        alerts.data = list
        localDataStore.updateHrAlerts(alerts)
    }

    fun onSubmitButtonClickedIrregularityEvents(
        data: IrregularEventsChipsListModel,
        selectedChips: List<String>,
        other: String?,
        onSubmitSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val reasonArray = JsonArray()
            selectedChips.forEach {
                reasonArray.add(it)
            }

            val req = JsonObject().apply {
                this.addProperty("current_date", getCurrentDate())
                this.addProperty("current_time", getCurrentTime())
                this.addProperty("current_value", data.alert.currentValue)
                this.addProperty("previous_value", data.alert.lastComparedValue)
                this.add("reason", reasonArray)
                this.addProperty("type", "hr")
                if (!other.isNullOrEmpty()){
                    this.addProperty("description", other)
                }
            }

            userRepository.submitIrregularityEvents(req).collect{ resource ->
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
                                        onSubmitButtonClickedIrregularityEvents(
                                            data,
                                            selectedChips,
                                            other,
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
                        }
                    }
                }
            }
        }
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

}