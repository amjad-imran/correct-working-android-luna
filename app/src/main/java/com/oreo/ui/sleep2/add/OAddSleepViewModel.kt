package com.oreo.ui.sleep2.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.googleFit.GoogleFitDataObservers
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.model.timeline.SupplementOption
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.OreoNapNetworkEntity
import com.noisefit_commans.data.model.timeline.ItemTimelineResponseModel
import com.noisefit_commans.models.SleepDataGoogleFit
import com.noisefit_commans.models.SleepDataGoogleFit.SleepDataBreakup
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.dataConverter.OreoOfflineDataMapper
import com.oreo.data.model.OAddSleep
import com.oreo.data.repository.abstraction.OreoSyncRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OAddSleepViewModel
@Inject
constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    private val keyValueDataSource: KeyValueDataSource,
    private val localDataStore: DataStoredInterface,
    private val googleFitDataObservers: GoogleFitDataObservers,
    private val oreoStepsDataImpl: OreoSyncRepository,
    private val userRepository: UserRepository,
) :
    BaseViewModel() {

    var isStartTimeSelected = false
    var isEndTimeSelected = false

    private var totalDuration = 0L
    var startTimeSleep = OAddSleep()
    var endTimeSleep = OAddSleep()

    var editDataAddActivity : ItemTimelineResponseModel ?= null
    val sleepEnvOptListData = MutableLiveData<ArrayList<SupplementOption>>()

    val selectedOptMap = HashMap<Int, Boolean>()

    private val _addSleepResponse =
        MutableLiveData<Event<Boolean>>()//todo return type will change once finalized
    val addSleepResponse = _addSleepResponse

    fun getSleepDuration(): Long {
        if (!isStartTimeSelected || !isEndTimeSelected) {
            return 0
        }

        val startTime = if (startTimeSleep.day.equals("Today", true)) {
            "${LocalDate.now()} ${
                String.format(
                    locale = Locale.US, "%02d:%02d", startTimeSleep.hour.toInt(),
                    startTimeSleep.minute.toInt()
                )
            }"
        } else {
            "${
                LocalDate.now().minusDays(1)
            } ${
                String.format(
                    locale = Locale.US,
                    "%02d:%02d",
                    startTimeSleep.hour.toInt(),
                    startTimeSleep.minute.toInt()
                )
            }"
        }

        val endTime = if (endTimeSleep.day.equals("Today", true)) {
            "${LocalDate.now()} ${
                String.format(
                    locale = Locale.US, "%02d:%02d", endTimeSleep.hour.toInt(),
                    endTimeSleep.minute.toInt()
                )
            }"
        } else {
            "${
                LocalDate.now().minusDays(1)
            } ${
                String.format(
                    locale = Locale.US, "%02d:%02d", endTimeSleep.hour.toInt(),
                    endTimeSleep.minute.toInt()
                )
            }"
        }

        totalDuration = Duration.between(
            LocalDateTime.parse(
                startTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            ), LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ).toSeconds()
        return totalDuration
    }

    fun callApiToAddSleep() {
        viewModelScope.launch {

            oreoStepsDataImpl.getTodaySteps().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        val activeCalories = resource.value?.activeCalories ?: 0

                        val jsonObject = JsonObject()


                        val startTime = if (startTimeSleep.day.equals("Today", true)) {
                            "${LocalDate.now()} ${
                                String.format(
                                    locale = Locale.US, "%02d:%02d", startTimeSleep.hour.toInt(),
                                    startTimeSleep.minute.toInt()
                                )
                            }"
                        } else {
                            "${
                                LocalDate.now().minusDays(1)
                            } ${
                                String.format(
                                    locale = Locale.US, "%02d:%02d", startTimeSleep.hour.toInt(),
                                    startTimeSleep.minute.toInt()
                                )
                            }"
                        }

                        val endTime = if (endTimeSleep.day.equals("Today", true)) {
                            "${LocalDate.now()} ${
                                String.format(
                                    locale = Locale.US, "%02d:%02d", endTimeSleep.hour.toInt(),
                                    endTimeSleep.minute.toInt()
                                )
                            }"
                        } else {
                            "${
                                LocalDate.now().minusDays(1)
                            } ${
                                String.format(
                                    locale = Locale.US, "%02d:%02d", endTimeSleep.hour.toInt(),
                                    endTimeSleep.minute.toInt()
                                )
                            }"
                        }


                        if (totalDuration <= (3 * 60 * 60)) {
                            //NAP
                            //val date = DateFormats.getCurrentDate(DateFormats.dateFormat3())

                            val date = startTime.split(" ")[0]

                            jsonObject.addProperty(
                                "date",
                                date
                            )
                            jsonObject.addProperty("end_time", "$endTime:00")
                            jsonObject.addProperty("start_time", "$startTime:00")
                            jsonObject.addProperty("duration", totalDuration / 60)

                            val jsonArray = JsonArray()
                            jsonArray.add(jsonObject)

                            addNapApi(jsonArray, date)
                        } else {
                            //Sleep

                            //val date = DateFormats.getCurrentDate(DateFormats.dateFormat3())

                            val date = endTime.split(" ")[0]

                            jsonObject.addProperty(
                                "date",
                                date
                            )
                            jsonObject.addProperty("end_time", endTime)
                            jsonObject.addProperty("start_time", startTime)
                            jsonObject.addProperty("total_duration", totalDuration)
                            jsonObject.addProperty("active_calories", activeCalories)

                            val jsonFinalObject = JsonObject()
                            jsonFinalObject.add("day_break_up", jsonObject)
                            val jsonArray = JsonArray()
                            jsonArray.add(jsonFinalObject)
                            addSleepApi(jsonArray, date)
                        }

                    }

                    is CacheResult.GenericError -> {
                        sendMessage("Something went wrong")
                    }
                }
            }


        }

    }


    private fun addSleepApi(jsonArray: JsonArray, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userActivityRepository.addManualSleep(
                jsonArray, date
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
                                        addSleepApi(jsonArray, date)
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
                                try {
                                    jsonArray.forEach {
                                        val breakup =
                                            (it as JsonObject).getAsJsonObject("day_break_up")
                                        val startTime = breakup.get("start_time").asString
                                        val endTime = breakup.get("end_time").asString
                                        addSleepToGoogleFit(startTime + ":00", endTime + ":00")
                                    }
                                } catch (ignored: Exception) {
                                }
                            }
                            withContext(Dispatchers.IO) {
                                keyValueDataSource.removeDataByType(KeyValueDataType.SLEEP_PLANNER)
                                _addSleepResponse.postValue(Event(true))
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Start and end of format ->yyyy-MM-dd HH:mm:ss
     */
    private fun addSleepToGoogleFit(startTime: String, endTime: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val zoneOffset =
                ZoneId.systemDefault().rules.getOffset(LocalDateTime.now())

            val startTimeStamp = LocalDateTime.parse(
                startTime,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ).toEpochSecond(zoneOffset)
            val endTimeStamp = LocalDateTime.parse(
                endTime,
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
    }

    private fun addNapApi(jsonArray: JsonArray, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userActivityRepository.addManualNap(
                jsonArray, date
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
                                        addNapApi(jsonArray, date)
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
                                try {
                                    jsonArray.forEach {
                                        (it as JsonObject)
                                        val startTime = it.get("start_time").asString
                                        val endTime = it.get("end_time").asString
                                        addSleepToGoogleFit(startTime, endTime)
                                    }
                                } catch (ignored: Exception) {
                                }
                            }

                            _addSleepResponse.postValue(Event(true))
                        }
                    }
                }
            }
        }
    }

    fun getSleepEnvOptionsList(){
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getTimelineOptionIdData("sleep_env").collect{ resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getSleepEnvOptionsList()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            it.options?.let {
                                val alreadySelected = HashSet<Int>()
                                editDataAddActivity?.metadata?.lunaTrackingOptionIds?.forEach {
                                    alreadySelected.add(it)
                                }
                                it.forEach {
                                    it.id?.let { key ->
                                        val isSelected = alreadySelected.contains(key)
                                        it.isChecked = isSelected
                                        selectedOptMap.put(key, isSelected)
                                    }
                                }
                                sleepEnvOptListData.postValue(it as ArrayList)
                            }
                        }
                    }
                }
            }
        }
    }

    fun submitSleepEnvOptions(editEventFun: () -> Unit){
        viewModelScope.launch {

            val sleepEnvObject = JsonObject().apply {
                editDataAddActivity?.id?.let {id ->
                    this.addProperty("track_id", id)
                }
                this.add("tags", JsonArray().apply {
                    selectedOptMap.filter { it.value }.keys.forEach {
                        this.add(it)
                    }
                })
            }

            val reqData = JsonObject().apply {
                this.add("events", JsonArray().apply {
                    this.add(sleepEnvObject)
                })
            }

            userRepository.submitLogSleepEnvOptData(reqData).collect{ resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        submitSleepEnvOptions(editEventFun)
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            editEventFun()
                        }
                    }
                }
            }

        }
    }

    fun isStartDateToday(): Boolean {
        return startTimeSleep.day.equals("Today", true)
    }


}