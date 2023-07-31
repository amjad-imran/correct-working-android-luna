package com.noisefit.ui.workout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.models.SportsModeRequestList
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    val userRepository: UserRepository,
    val lastSyncProvider: LastSyncProvider,
    var sessionManager: SessionManager,
    var watchDataStore: WatchDataStore,
    var dataUnitConverter: DataUnitConverter,
    var watchesSDK: WatchesSDK,
    val localDataStore: DataStoredInterface,
) : BaseViewModel() {
    private val _activities = MutableLiveData<List<SportsModeResponse>>()
    val activities: LiveData<List<SportsModeResponse>> = _activities

    private val _sessionStartAvailable = MutableLiveData<Boolean>()
    val sessionStartAvailable: LiveData<Boolean> = _sessionStartAvailable
    var askPermissionForPro = false
    var isAddingActivity = false
    var startDate = ""


    var currentPage = 1
    var pageLimit = 10
    var isLastPage = false
    var isActivitiesLoading: Boolean = false

    init {
        localDataStore.getDeviceFeatures()?.let { deviceFeatures ->
            if (!deviceFeatures.availableActivities.isBlank() && !deviceFeatures.availableActivities.equals(
                    "none",
                    true
                )
            ) {
                _sessionStartAvailable.postValue(true)
            }
        }
        isLastPage = false
        currentPage = 1
        isActivitiesLoading = false
    }


    fun isUserLogined(): Boolean {
        return localDataStore.getUser() != null
    }

    fun getActivities() {
        setLoading(true)

        viewModelScope.launch(Dispatchers.IO) {
            if (localDataStore.getUser() == null) {
                formatDataSet(userRepository.getOfflineActivities())
                setLoading(false)
                return@launch
            }

            uploadActivitiesToServer()

        }

    }

    fun getUnitSystem(): Units {
        return localDataStore.getUnit()
    }


    fun fetchActivityFromServer() {
        viewModelScope.launch {
            //val endDate = DateFormats.getTodaysDateString(7)
            isActivitiesLoading = true
            userRepository.getActivitiesPaging(currentPage, pageLimit).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                        isActivitiesLoading = resource.loading
                    }
                    is Resource.NetworkError -> {
                        if (currentPage == 1) {
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        fetchActivityFromServer()
                                    }

                                    override fun no() {}
                                }
                            })
                        } else {
                            sendMessage("Error Connecting to internet")
                        }
                        //isActivitiesLoading = false
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {

                            //formatDataSet(it.activities)


                            if (it.isNotEmpty()) {

                                formatDataSetPagination(it)
                                currentPage++

                                /*currentPage++
                                fetchActivityFromServer()*/
                            } else {
                                isLastPage = true
                            }


                            /*if (startDate.isEmpty()) {
                                if (it.firstActivityOn != null) {
                                    startDate = it.firstActivityOn
                                    fetchActivityFromServer()
                                } else {
                                    formatDataSet(it.activities)
                                }
                            } else {
                                formatDataSet(it.activities)
                            }*/
                        }
                        //isActivitiesLoading = false
                    }
                }
            }
        }
    }

    //TODO Optimize
    private fun formatDataSet(activities: List<SportsModeResponse>) {
        val activitySorted = activities.sortedBy {
            val dateTime = it.time
            val dateFormat = DateFormats.dateTimeFormatISO

            val time = try {
                if (dateTime.isNullOrEmpty()) {
                    0
                } else {
                    dateFormat.parse(dateTime)?.time
                }

            } catch (exp: Exception) {
                0
            }
            time ?: 0
        }.reversed()

        val activityResponse = ArrayList<SportsModeResponse>()
        val datesSet = HashSet<String>()

        activitySorted.forEach {
            it.date ?: return@forEach

            val date = DateFormats.formatDateTime(
                it.time, DateFormats.dateTimeFormatISO,
                DateFormats.dateFormat2
            )
            if (date.isEmpty()) return@forEach

            if (!datesSet.contains(date)) {
                datesSet.add(date)
                activityResponse.add(SportsModeResponse(isHeader = true, date = date))
            }
            activityResponse.add(it.apply {
                isHeader = false
            })
        }

        _activities.postValue(activityResponse)

    }

    //TODO Optimize
    private fun formatDataSetPagination(activities: List<SportsModeResponse>) {

        val activitiesTemp = if (_activities.value == null) {
            activities
        } else {
            val act = _activities.value as ArrayList
            act.addAll(activities)
            act
        }
        //_activities.postValue(activitiesTemp)

        val activityResponse = ArrayList<SportsModeResponse>()

        val datesSet = HashSet<String>()

        activitiesTemp.forEach {
            it.date ?: return@forEach

            val date = DateFormats.formatDateTime(
                it.time, DateFormats.dateTimeFormatISO,
                DateFormats.dateFormat2
            )
            if (date.isEmpty()) return@forEach

            if (!datesSet.contains(date)) {
                datesSet.add(date)
                activityResponse.add(SportsModeResponse(isHeader = true, date = date))
            }

            activityResponse.add(it.apply {
                isHeader = false
            })
        }

        _activities.postValue(activityResponse)

    }



    fun resetPaginationState() {
        currentPage = 1
        isLastPage = false
        isActivitiesLoading = false
        _activities.postValue(arrayListOf())
    }

    fun uploadActivitiesToServer() {
        if (localDataStore.getUser() == null) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val activities = userRepository.getUnSyncedActivities()

            if (activities.isEmpty()) {
                if (this@ActivityViewModel.activities.value == null) {
                    resetPaginationState()
                    fetchActivityFromServer()
                } else {
                    setLoading(false)
                }
                return@launch
            }

            val requestObject = SportsModeRequestList()
            activities.forEach {
                //it.date = DateFormats.formatDateTime(it.date,DateFormats.dateFormat3,DateFormats.dateFormat)
                // it.time = DateFormats.formatDateTime(it.time,DateFormats.dateTimeFormatGMT,DateFormats.timeWithSecond)
                it.time = DateFormats.formatDateTime(
                    it.time,
                    DateFormats.dateTimeFormatISO,
                    DateFormats.timeWithSecond
                )
            }

            requestObject.activities = activities

            userRepository.postActivities(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }
                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    uploadActivitiesToServer()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.let {
                            lastSyncProvider.removeSyncTimeStamp(LastSyncItems.RECENT_ACTIVITIES)
                            if (it.success == true) {
                                userRepository.setActivitiesSynced()
                            }
                            resetPaginationState()
                            fetchActivityFromServer()
                        }
                    }
                }
            }
        }

    }
}