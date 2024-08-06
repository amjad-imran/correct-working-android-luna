package com.noisefit.oreo

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.dataConverter.DataConverter
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit_commans.common.checkDayDifferenceMoreNMinutes
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.db.abstraction.LocationDataSource
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.RecordedWorkoutData
import com.noisefit_commans.data.model.User
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.checkDayDifferenceMoreOne
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.TrendsData
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import com.oreo.data.repository.abstraction.OreoSyncRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.ui.home.summary.PushLocalNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject


const val HEALTH_DATA_PAGINATION_DAYS = 7

@HiltViewModel
class OreoMainViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val ringDataStore: RingDataStore,
    val syncRepository: OreoSyncRepository,
    val userHealthDataDataSource: OreoUserHealthDataDataSource,
    val dataConverter: DataConverter,
    val locationDataSource: LocationDataSource,
    val userActivityRepository: OreoUserActivityRepository,
    val oreoDeviceRepository: OreoDeviceRepository,
) : BaseViewModel() {


    val FAB_ANIM_TIME = 500L

    var registerDate: Int = -1
    var temperatureBaseLine: Float? = null
    val DEFAULT_TEMPERATURE_BASELINE = 98.6f

    var checkBluetooth = MutableLiveData<Event<Boolean>>()

    var user: User? = null

    var addWorkoutCtaVisibility = MutableLiveData<Boolean>()
    var isActivityWorkAdd = false

    val userHealthData = HashMap<String, ServerUserHealthData?>()
    var trendsData: TrendsData? = null
    var stressFirstDate: String? = null
    var stressBeta: Boolean = false
    var enableAi: Boolean = false
    val dataReload = MutableLiveData<Event<List<String>>>()
    val dashTodayReload = MutableLiveData<Event<Boolean>>()

    var showChatUi = MutableLiveData<Event<String>>()

    var bottomNavigation = MutableLiveData<Event<BottomNavOption>>()
    fun navigateTo(option: BottomNavOption) {
        bottomNavigation.postValue(Event(option))
    }

    val syncTextState = MutableLiveData<String?>()//if has text show, else hide
    val syncProgressBarState = MutableLiveData<Pair<Int, Int>?>()//Pair(currentValue,total)

    var mEndDate: String? = null
    var mStartDate: String? = null

    //Currently highlighted date
    var selectedDate: String? = null
    var dateSetOn: String? = null


    val pushNotificationSleep = MutableLiveData<Event<PushLocalNotification>>()
    val pushNotificationReadiness = MutableLiveData<Event<PushLocalNotification>>()
    var isFetchRequestOnGoing = false


    //Dashboard
    private val _dashboard = MutableLiveData<List<String>>()
    val dashboard: LiveData<List<String>> = _dashboard


    //Sleep Data
    private val _sleepHistoryResponse = MutableLiveData<List<OreoSleepModel>>()
    val sleepHistoryResponse: LiveData<List<OreoSleepModel>> = _sleepHistoryResponse
    private val _daySleepData = MutableLiveData<OreoSleepModel>()
    val daySleepData: LiveData<OreoSleepModel> = _daySleepData


    //Readiness Data
    private val _readinessHistoryResponse = MutableLiveData<List<OreoReadinessModel>>()
    val readinessHistoryResponse: LiveData<List<OreoReadinessModel>> = _readinessHistoryResponse
    private val _dayReadinessData = MutableLiveData<OreoReadinessModel>()
    val dayReadinessData: LiveData<OreoReadinessModel> = _dayReadinessData


    //Activity Data
    private val _activityHistoryResponse = MutableLiveData<List<OreoActivityModel>>()
    val activityHistoryResponse: LiveData<List<OreoActivityModel>> = _activityHistoryResponse

    private val _dayActivityData = MutableLiveData<OreoActivityModel>()
    val dayActivityData: LiveData<OreoActivityModel> = _dayActivityData


    init {
        viewModelScope.launch(Dispatchers.IO) {
            user = localDataStore.getUser()
            resetMasterDates()
        }
    }

    private fun resetMasterDates() {
        LOGS.d("RESET_DATES resetMasterDates")
        mEndDate = DateFormats.getCurrentDateOreoFormat()
        mStartDate = DateFormats.getCurrentDateMinusDays(6)
        selectedDate = DateFormats.getCurrentDateOreoFormat()
        dateSetOn = DateFormats.getCurrentDateOreoFormat()
        getUserHealthData(mStartDate, mEndDate)
    }

    fun shouldResetMasterDates(): Boolean {
        LOGS.d("RESET_DATES shouldResetMasterDates")
        val todayDate = DateFormats.getCurrentDateOreoFormat()

        if (userHealthData.contains(todayDate)) return false

        //if (todayDate.equals(dateSetOn, true)) return false

        /* if (userHealthData.isEmpty()) {*/
        resetHealthCacheData()
        resetMasterDates()
        /*} else {
            shouldLoadMoreData()*/
        dateSetOn = DateFormats.getCurrentDateOreoFormat()
        /*}*/
        LOGS.d("RESET_DATES shouldResetMasterDates done")
        return true
    }

    private fun resetHealthCacheData() {
        userHealthData.clear()
        trendsData = null
        stressFirstDate = null
        stressBeta = false
        _dashboard.value = ArrayList()
        _sleepHistoryResponse.value = ArrayList()
        _readinessHistoryResponse.value = ArrayList()
        _activityHistoryResponse.value = ArrayList()
        isFetchRequestOnGoing = false

    }


    val stateConnectHelp = MutableLiveData<Boolean>()
    var isHelpWidgetShown = false
    var timer: CountDownTimer? = null

    fun startDisconnectTimer() {
        if (timer == null && !isHelpWidgetShown) {
            timer = object : CountDownTimer(60000L, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    LOGS.w("TIMER running $millisUntilFinished")
                }

                override fun onFinish() {
                    timer = null
                    if (!isHelpWidgetShown) {
                        isHelpWidgetShown = true
                        stateConnectHelp.postValue(true)
                    }
                }
            }
            timer?.start()
        }

    }

    fun onRingConnected() {
        timer?.cancel()
        timer = null
        stateConnectHelp.postValue(false)
    }

    fun getUserHealthData(startDate: String?, endDate: String?) {
        isFetchRequestOnGoing = true
        viewModelScope.launch {
            userActivityRepository.getUserHealthData(
                startDate,
                endDate
            ).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                        isFetchRequestOnGoing = false
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        isFetchRequestOnGoing = false
                        if (resource.code == 410) {
                            localDataStore.setForceUpdateRequired()
                            sessionManager.forceUpdateApp.postValue(Event(true))
                        } else {
                            setApiErrors(resource.response.apply {
                                this.uiComponentType as UIComponentType.RetryApiDialog
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                    object : BinaryActionCallback {
                                        override fun yes() {
                                            getUserHealthData(
                                                startDate,
                                                endDate
                                            )
                                        }

                                        override fun no() {

                                        }
                                    }
                            })
                        }
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            registerDate = it.registerDate ?: -1
                            stressFirstDate = it.firstStress
                            stressBeta = it.stressBeta ?: false
                            enableAi = it.enableAi ?: false
                            temperatureBaseLine = it.tempBaseLine ?: DEFAULT_TEMPERATURE_BASELINE

                            it.data.forEach { data ->
                                userHealthData[data.date] = data
                            }
                            if (it.trends != null) {
                                trendsData = it.trends
                            }

                            _dashboard.value = getDaysList()

                            val sleepList = getSleepDataList()
                            _sleepHistoryResponse.value = (sleepList)

                            val readinessList = getReadinessDataList()
                            _readinessHistoryResponse.value = (readinessList)

                            val activityList = getActivityDataList()
                            _activityHistoryResponse.value = (activityList)


                            /*val todayData = userHealthData[getTodayDate()]
                            showNotification(todayData)*/

                            val reloadDays = getDaysList(startDate, endDate)
                            dataReload.value = Event(reloadDays)

                            if (reloadDays.contains(DateFormats.getTodaysDateString(10))) {
                                dashTodayReload.value = Event(true)
                            }

                            val todayData = userHealthData[getTodayDate()]
                            todayData?.let {
                                sendSleepEvents(it)
                            }


                            isFetchRequestOnGoing = false

                        }
                    }
                }
            }
        }


    }

    private fun sendSleepEvents(data: ServerUserHealthData) {
        viewModelScope.launch(Dispatchers.IO) {

            if ((data.sleep?.sleep_score?.value ?: 0) == 0) {
                return@launch
            }
            val isSynced = localDataStore.isSleepSyncedForDate(data.date)
            if (isSynced) return@launch

            val name = localDataStore.getUser()?.firstName

            sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.sleep_recorded,
                HashMap<String, Any>().apply {
                    this[MoEngageAppEventParams.date] = data.date
                    this[MoEngageAppEventParams.first_name] = name ?: ""
                    this[MoEngageAppEventParams.sleep_score] = data.sleep?.sleep_score ?: 0
                    this[MoEngageAppEventParams.sleep_duration] = data.sleep?.totalSleep?.value ?: 0
                    this[MoEngageAppEventParams.rem_sleep_duration] =
                        data.sleep?.remSleep?.value ?: 0
                    this[MoEngageAppEventParams.bed_time] = data.sleep?.startTime ?: ""
                    this[MoEngageAppEventParams.wake_up_time] = data.sleep?.endTime ?: ""
                    this[MoEngageAppEventParams.latency] = data.sleep?.latency?.value ?: 0
                    this[MoEngageAppEventParams.avg_spo2] = data.sleep?.oxy?.avg ?: 0
                    this[MoEngageAppEventParams.deep_sleep_duration] =
                        data.sleep?.deepSleep?.value ?: 0
                })
            localDataStore.saveSleepSyncedForDate(data.date)
        }
    }

    fun shouldLoadMoreData(): Boolean {

        if (isFetchRequestOnGoing) return false

        if (sleepHistoryResponse.value.isNullOrEmpty()) return false

        if (sleepHistoryResponse.value!!.size < 2) return false

        if (selectedDate.isNullOrEmpty()) return false

        if ((sleepHistoryResponse.value!![1]).date.equals(selectedDate) || (sleepHistoryResponse.value!![0]).date.equals(
                selectedDate
            )
        ) {

            val (newStartDate, newEndDate) = getPreviousPaginationDates(mStartDate!!)
            if (newStartDate == null && newEndDate == null) return false

            mStartDate = newStartDate

            getUserHealthData(newStartDate, newEndDate)

            return true
        }

        val isYesterdayDate = selectedDate.equals(DateFormats.getYesterdayDate())

        if (isYesterdayDate) return false

        return false

        if ((sleepHistoryResponse.value!![sleepHistoryResponse.value!!.size - 2]).date.equals(
                selectedDate
            )
        ) {
            val (newStartDate, newEndDate) = getNextPaginationDates(mEndDate!!)
            if (newStartDate == null && newEndDate == null) return false
            mEndDate = newEndDate
            getUserHealthData(newStartDate, newEndDate)
            return true
        }


        return false
    }

    fun getPreviousPaginationDates(date: String): Pair<String?, String?> {
        val start: LocalDate = LocalDate.parse(date)

        val startDate = start.minusDays(7)
        val endDate = start.minusDays(1)

        val startingCalDate = LocalDate.parse("2023-08-01")
        if (startDate < startingCalDate) {
            return Pair(null, null)

        }
        return Pair(startDate.toString("yyyy-MM-dd"), endDate.toString("yyyy-MM-dd"))
    }

    fun getDatesMinus(date: String, minusDays: Int): String {
        val end: LocalDate = LocalDate.parse(date)
        return end.minusDays(minusDays).toString("yyyy-MM-dd")
    }

    fun getDatesPlus(date: String, plusDays: Int): String {
        val end: LocalDate = LocalDate.parse(date)
        return end.plusDays(plusDays).toString("yyyy-MM-dd")
    }

    fun getNextPaginationDates(date: String): Pair<String?, String?> {
        val start: LocalDate = LocalDate.parse(date)
        val todayDate = LocalDate.now()

        val startDateObj = start.plusDays(1)//.toString("yyyy-MM-dd")
        var endDateObj = start.plusDays(7)//.toString("yyyy-MM-dd")

        if (startDateObj > todayDate) {
            return Pair(null, null)
        }
        if (endDateObj > todayDate) {
            endDateObj = todayDate
        }

        return Pair(startDateObj.toString("yyyy-MM-dd"), endDateObj.toString("yyyy-MM-dd"))
    }

    private fun getSleepDataList(): List<OreoSleepModel> {
        val daysList = getDaysList()
        val result = ArrayList<OreoSleepModel>()

        daysList.forEach {
            val data = userHealthData[it]
            if (data?.sleep != null) {
                result.add(data.sleep!!)
            }
        }
        return result
    }

    private fun getReadinessDataList(): List<OreoReadinessModel> {
        val daysList = getDaysList()
        val result = ArrayList<OreoReadinessModel>()

        daysList.forEach {
            val data = userHealthData[it]
            if (data?.readiness != null) {
                result.add(data.readiness!!)
            }
        }
        return result
    }

    private fun getActivityDataList(): List<OreoActivityModel> {
        val daysList = getDaysList()
        val result = ArrayList<OreoActivityModel>()

        daysList.forEach {
            val data = userHealthData[it]
            if (data?.activity != null) {
                result.add(data.activity!!)
            }
        }
        return result
    }

    private fun getDaysList(): List<String> {
        val dateList = ArrayList<String>()
        var start: LocalDate = LocalDate.parse(mStartDate)
        val end: LocalDate = LocalDate.parse(mEndDate)

        while (!start.isAfter(end)) {
            dateList.add(start.toString())
            start = start.plusDays(1)
        }


        LOGS.w("Setting_data $mStartDate - $mEndDate")

        return dateList

    }

    private fun getDaysList(startDate: String?, endDate: String?): List<String> {
        if (startDate == null || endDate == null) return ArrayList()
        val dateList = ArrayList<String>()
        var start: LocalDate = LocalDate.parse(startDate)
        val end: LocalDate = LocalDate.parse(endDate)

        while (!start.isAfter(end)) {
            dateList.add(start.toString())
            start = start.plusDays(1)
        }
        return dateList

    }


    fun updateSelectedDate(selectedDate: String?): String? {
        var returnSelectedDate: String? = null

        val dayData = _sleepHistoryResponse.value?.firstOrNull() {
            it.date.equals(selectedDate, false)
        }
        if (dayData != null) {
            _daySleepData.postValue(dayData)
        } else {
            _sleepHistoryResponse.value?.lastOrNull()?.let { data ->
                LOGS.w("moveToPosition selected Date new $selectedDate")
                returnSelectedDate = data.date
                LOGS.w("moveToPosition selected Date new set $selectedDate")
                _daySleepData.postValue(data)
            }
        }
        return returnSelectedDate
    }

    fun updateSelectedDateReadiness(selectedDate: String?): String? {
        var returnSelectedDate: String? = null

        val dayData = _readinessHistoryResponse.value?.firstOrNull() {
            it.date.equals(selectedDate, false)
        }
        if (dayData != null) {
            _dayReadinessData.postValue(dayData)
        } else {
            _readinessHistoryResponse.value?.lastOrNull()?.let { data ->
                LOGS.w("moveToPosition selected Date new $selectedDate")
                returnSelectedDate = data.date
                LOGS.w("moveToPosition selected Date new set $selectedDate")
                _dayReadinessData.postValue(data)
            }
        }
        return returnSelectedDate
    }

    fun updateSelectedDateActivity(selectedDate: String?): String? {
        var returnSelectedDate: String? = null
        val dayData = _activityHistoryResponse.value?.firstOrNull() {
            it.date.equals(selectedDate, false)
        }
        if (dayData != null) {
            _dayActivityData.postValue(dayData)
        } else {
            _activityHistoryResponse.value?.lastOrNull()?.let { data ->
                LOGS.w("moveToPosition selected Date new $selectedDate")
                returnSelectedDate = data.date
                LOGS.w("moveToPosition selected Date new set $selectedDate")
                _dayActivityData.postValue(data)
            }
        }
        return returnSelectedDate
    }

    fun getDashBoardData(date: String): Pair<ServerUserHealthData, TrendsData?>? {
        val dayData = userHealthData[date] ?: return null
        return Pair(dayData, trendsData)
    }

    fun getStressData(date: String): ServerUserHealthData? {
        return userHealthData[date] ?: return null
    }

    fun getTodayDate(): String {
        return DateFormats.getTodaysDateString(10)
    }

    fun getDayMovementData(date: String?): ServerUserHealthData? {
        return userHealthData[date]
    }

    fun shouldSyncAutoLogs(): Boolean {
        val lastTimeStamp = ringDataStore.getAutoLogsTimeStamp()
        val logSyncInterval = localDataStore.getLogSyncInterval()

        if (logSyncInterval == 0) return false

        if (lastTimeStamp == 0L) {
            ringDataStore.saveAutoLogsTimeStamp()
            return false
        }

        return lastTimeStamp.checkDayDifferenceMoreNMinutes(logSyncInterval * 60)
    }

    fun testClearLocalHealthData() {
        viewModelScope.launch {
            userActivityRepository.clearAllHealthData()
        }
    }

    fun reloadTodaysData() {
        val shouldRefresh = shouldResetMasterDates()
        if (!shouldRefresh) {
            val todayDate = getTodayDate()
            getUserHealthData(todayDate, todayDate)
        }
    }

    private fun showNotification(response: ServerUserHealthData?) {

        if (response?.sleep == null) return

        //Sleep
        response.sleep?.let {
            if ((it.sleep_score?.value ?: 0) > 75 && (it.totalSleep?.value
                    ?: 0) >= 25200 && (it.totalSleep?.value
                    ?: 0) <= 32400
            ) {
                val timeStamp = localDataStore.getSleepNotificationTimeStamp()

                if (timeStamp == 0L || timeStamp.checkDayDifferenceMoreOne()) {
                    pushNotificationSleep.postValue(
                        Event(
                            PushLocalNotification(
                                "Good sleep last night",
                                "You got enough sleep hours today. This helps with higher recovery, cognitive & immune system function",
                                NotificationEventsClass.LOCAL_SLEEP_NOTIFICATION_KEY
                            )
                        )
                    )
                    localDataStore.setSleepNotificationTimeStamp()
                }
            }
        }

        response.readiness?.let {
            val timeStamp = localDataStore.getReadinessNotificationTimeStamp()
            if (timeStamp == 0L || timeStamp.checkDayDifferenceMoreOne()) {
                val nudge = it.dashNudges?.firstOrNull()
                if (it.readinessScore?.value != null && nudge != null) {
                    pushNotificationReadiness.postValue(
                        Event(
                            PushLocalNotification(
                                nudge.label,
                                nudge.message,
                                NotificationEventsClass.LOCAL_READINESS_NOTIFICATION_KEY
                            )
                        )
                    )
                    localDataStore.setReadinessNotificationTimeStamp()
                }
            }
        }
    }

    fun onCalendarDateSelected(selectedDate: String) {

        val todayDate = LocalDate.now()
        val startingDate = LocalDate.parse("2023-08-01")
        val selectedDateLocal = LocalDate.parse(selectedDate)


        val difference = Days.daysBetween(selectedDateLocal, todayDate).days
        LOGS.d("onCalendarDateSelected $difference")

        var plusDays = 3
        var minusDays = 3
        when (difference) {
            0 -> {
                plusDays = 0
                minusDays = 6
            }

            1 -> {
                plusDays = 1
                minusDays = 5
            }

            2 -> {
                plusDays = 2
                minusDays = 4
            }
        }

        when (Days.daysBetween(startingDate, selectedDateLocal).days) {
            0 -> {
                plusDays = 6
                minusDays = 0
            }

            1 -> {
                plusDays = 5
                minusDays = 1
            }

            2 -> {
                plusDays = 4
                minusDays = 2
            }
        }




        mEndDate = getDatesPlus(selectedDate, plusDays)
        mStartDate = getDatesMinus(selectedDate, minusDays)
        this.selectedDate = selectedDate
        dateSetOn = DateFormats.getCurrentDateOreoFormat()
    }

    fun isDeviceConnected(): Boolean {
        if (isDevicePaired() == null) {
            return false
        }

        if (sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
            return true
        }
        return false
    }

    fun isDevicePaired(): ColorFitDevice? {
        return ringDataStore.getRingDevice()
    }

    fun syncRecordedWorkoutData() {
        viewModelScope.launch(Dispatchers.IO) {

            syncRepository.getRecordedWorkouts()
                .collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            resource.value?.let {
                                syncWorkoutsToServer(resource.value)
                            }
                        }

                        is CacheResult.GenericError -> {

                        }
                    }
                }


        }
    }

    fun shouldShowStressCard(date: String): Boolean {
        if (stressFirstDate == null) return false

        return try {
            val stressDate =
                LocalDateTime.parse(stressFirstDate, DateTimeFormat.forPattern("yyyy-MM-dd"))
            val currentDate = LocalDateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd"))
            currentDate >= stressDate
        } catch (exp: Exception) {
            //formatting exception
            false
        }
    }

    fun stressDaysFromCurrent(date: String?): Int {
        if (date.isNullOrEmpty()) return -1
        if (stressFirstDate == null) return -1

        return try {
            val stressDate =
                LocalDateTime.parse(stressFirstDate, DateTimeFormat.forPattern("yyyy-MM-dd"))
            val currentDate = LocalDateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd"))

            val difference = Days.daysBetween(stressDate, currentDate)
            return difference.days
        } catch (exp: Exception) {
            -1
        }
    }

    //TODO convert to worker
    private fun syncWorkoutsToServer(workouts: List<RecordedWorkoutData>) {
        if (workouts.isEmpty()) return
        GlobalScope.launch(Dispatchers.IO) {

            val workoutsArray = dataConverter.createRecordedWorkoutArray(workouts)

            if (workoutsArray == null || workoutsArray.isEmpty) {
                val dates = HashSet<String>()
                workouts.forEach { workout ->
                    workout.date?.let { date ->
                        dates.add(date)
                    }
                }
                userHealthDataDataSource.clearDataByDates(dates.toList())
                syncRepository.removeRecordedWorkouts().collect()
                locationDataSource.deleteAll()
                ringDataStore.removeRecordDeleteList()
                return@launch
            }

            val reqObj = JsonObject()
            reqObj.add("workouts", workoutsArray)
            userActivityRepository.addRecordedWorkout(
                reqObj
            ).collect { resource ->
                when (resource) {

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            val dates = HashSet<String>()
                            workouts.forEach { workout ->
                                workout.date?.let { date ->
                                    dates.add(date)
                                }
                            }
                            userHealthDataDataSource.clearDataByDates(dates.toList())
                            syncRepository.removeRecordedWorkouts().collect()
                            locationDataSource.deleteAll()
                            ringDataStore.removeRecordDeleteList()
                            delay(200)

                            reloadTodaysData()
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    /**
     * Today condition check
     * Device paired check
     */
    fun handleAddWorkoutVisibility() {
        viewModelScope.launch(Dispatchers.IO) {
            if (sessionManager.connectedDeviceRing.value == null) {
                addWorkoutCtaVisibility.postValue(false)
                return@launch
            }
            if (selectedDate == DateFormats.getCurrentDateOreoFormat()) {
                addWorkoutCtaVisibility.postValue(true)
                isActivityWorkAdd = true
            } else {
                addWorkoutCtaVisibility.postValue(false)
                isActivityWorkAdd = false
            }
        }
    }

    fun checkOnGoingWorkout() {
        viewModelScope.launch(Dispatchers.IO) {
            val workout = ringDataStore.getOngoingRecordWorkout()
            if (workout != null) {
                sessionManager.sendUpdateQueryAction(UpdateDeviceAction.CheckOngoingWorkout())
            }
        }
    }

    fun shouldShowFemaleHealthCta(): Boolean {
        val genderCondition = !sessionManager.gender.equals("male", true)
        if (genderCondition) {
            return sessionManager.canLogPeriod
        }
        return false
    }

    fun getChatHistoryToday() {
        viewModelScope.launch {
            oreoDeviceRepository.getChatHistoryByDate(
                DateFormats.getTodaysDateString(10)
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
                                        getChatHistoryToday()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            val threadId = it.firstOrNull()?.threadId
                            showChatUi.postValue(Event(threadId ?: ""))
                        }
                    }
                }
            }
        }


    }

    fun checkForForceUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            val isForceUpdateRequired = localDataStore.getForceUpdateRequired()
            if (isForceUpdateRequired) {
                sessionManager.forceUpdateApp.postValue(Event(true))
            } else {
                sessionManager.forceUpdateApp.postValue(Event(false))
            }
        }
    }

}