package com.noisefit.oreo

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.common.checkDayDifferenceMoreNMinutes
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.data.model.health.OreoDashboardResponseModel
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import javax.inject.Inject


const val HEALTH_DATA_PAGINATION_DAYS = 7

@HiltViewModel
class OreoMainViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val ringDataStore: RingDataStore,
    val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {


    var checkBluetooth = MutableLiveData<Event<Boolean>>()

    //For API
    var selectedMasterDate: String? = null


    val userHealthData = HashMap<String, ServerUserHealthData?>()
    val userDataAdded = MutableLiveData<Event<Boolean>>()

    var bottomNavigation = MutableLiveData<Event<BottomNavOption>>()
    fun navigateTo(option: BottomNavOption) {
        bottomNavigation.postValue(Event(option))
    }

    var mEndDate: String? = null
    var mStartDate: String? = null

    //Currently highlighted date
    var selectedDate: String? = null

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
        selectedMasterDate = DateFormats.getCurrentDateOreoFormat()

        mEndDate = DateFormats.getCurrentDateOreoFormat()
        mStartDate = DateFormats.getCurrentDateMinusDays(6)
        selectedDate = DateFormats.getCurrentDateOreoFormat()

        getUserHealthData(mStartDate, mEndDate)
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
        viewModelScope.launch {
            userActivityRepository.getUserHealthData(
                startDate,
                endDate
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

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            it.data.forEach { data ->
                                userHealthData[data.date] = data
                            }
                            _dashboard.value = getDaysList()

                            val sleepList = getSleepDataList()
                            _sleepHistoryResponse.value = (sleepList)

                            val readinessList = getReadinessDataList()
                            _readinessHistoryResponse.value = (readinessList)

                            val activityList = getActivityDataList()
                            _activityHistoryResponse.value = (activityList)
                        }
                    }
                }
            }
        }


    }

    fun shouldLoadMoreData(): Boolean {
        if (sleepHistoryResponse.value.isNullOrEmpty()) return false

        if (sleepHistoryResponse.value!!.size < 2) return false

        if ((sleepHistoryResponse.value!![1]).date.equals(selectedDate)) {

            val (newStartDate, newEndDate) = getPreviousPaginationDates(mStartDate!!)
            mStartDate = newStartDate

            getUserHealthData(newStartDate, newEndDate)

            return true
        }

        val isYesterdayDate = selectedDate.equals(DateFormats.getYesterdayDate())

        if (isYesterdayDate) return false

        if ((sleepHistoryResponse.value!![sleepHistoryResponse.value!!.size - 2]).date.equals(
                selectedDate
            )
        ) {
            val (newStartDate, newEndDate) = getNextPaginationDates(mEndDate!!)
            mEndDate = newEndDate
            getUserHealthData(newStartDate, newEndDate)
            return true
        }


        return false
    }

    fun getPreviousPaginationDates(date: String): Pair<String, String> {
        val start: LocalDate = LocalDate.parse(date)

        val startDate = start.minusDays(7).toString("yyyy-MM-dd")
        val endDate = start.minusDays(1).toString("yyyy-MM-dd")

        return Pair(startDate, endDate)
    }

    fun getDatesMinus(date: String): String {
        val end: LocalDate = LocalDate.parse(date)
        return end.minusDays(7).toString("yyyy-MM-dd")
    }

    fun getNextPaginationDates(date: String): Pair<String, String> {
        val start: LocalDate = LocalDate.parse(date)

        val startDate = start.plusDays(1).toString("yyyy-MM-dd")
        val endDate = start.plusDays(7).toString("yyyy-MM-dd")

        return Pair(startDate, endDate)
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

    fun getDashBoardData(date: String): ServerUserHealthData? {
        return userHealthData[date]
    }

    fun getTodayDate(): String {
        return DateFormats.getTodaysDateString(10)
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


}