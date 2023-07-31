package com.noisefit.ui.workout.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

@HiltViewModel
class ActivityStartViewModel @Inject constructor(
    val userRepository: UserRepository,
    val lastSyncProvider: LastSyncProvider,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {

    private val _activities = MutableLiveData<List<SportsModeResponse>>()
    var mode: SportsModeList.SportsMode? = null
    var sportsModeResponse: SportsModeResponse? = null
    var sportsModeRequest: SportsModeRequest? = null
    val activities: LiveData<List<SportsModeResponse>> = _activities
    var ongoing = false
    var distanceCovered = 0
    var isAddingActivity = false
    var activityStatus: String? = ""

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
            val startDate = "12/9/2021"
            val endDate = DateFormats.getTodaysDateString(7)
            userRepository.getActivities(startDate, endDate).collect { resource ->
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
                                    fetchActivityFromServer()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            formatDataSet(it.activities)
                        }
                    }
                }
            }
        }
    }

     fun enableGps(mode: String?): Boolean {
         LOGS.d("enableGps","$mode")
        if (mode?.lowercase() == "biking" ||
            mode?.lowercase() == "outdoor_cycling"||
            mode?.lowercase() == "outdoor_running") {
            return true
        }

        return false
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
            val date = it.date ?: return@forEach

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


//    fun addActivity(sportsModeResponses: List<SportsModeResponse>?) {
//        viewModelScope.launch(Dispatchers.IO) {
//            isAddingActivity = true
//            userRepository.saveActivity(sportsModeResponses)
//            isAddingActivity = false
//        }
//    }

    fun uploadActivitiesToServer() {
        if (localDataStore.getUser() == null) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val activities = userRepository.getUnSyncedActivities()

            if (activities.isEmpty()) {
                fetchActivityFromServer()
                return@launch
            }

            val requestObject = SportsModeRequestList()
            activities.forEach {
                it.date = DateFormats.formatDateTime(
                    it.date,
                    DateFormats.dateFormat3,
                    DateFormats.dateFormat
                )
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
                            if (it.success == true) {
                                userRepository.setActivitiesSynced()
                            }
                            fetchActivityFromServer()
                            lastSyncProvider.removeSyncTimeStamp(LastSyncItems.RECENT_ACTIVITIES)
                        }
                    }
                }
            }
        }

    }

}