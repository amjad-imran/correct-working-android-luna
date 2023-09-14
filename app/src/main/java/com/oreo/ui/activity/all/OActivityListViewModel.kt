package com.oreo.ui.activity.all

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.OActivityListModal
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OActivityListViewModel
@Inject
constructor(
    val watchDataStore: WatchDataStore,
    val sessionManager: SessionManager,
    var dataUnitConverter: DataUnitConverter,
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore,
    private val userRepository: OreoUserActivityRepository,
) : BaseViewModel() {

    private val _activities = MutableLiveData<List<OActivityListModal>>()
    val activities: LiveData<List<OActivityListModal>> = _activities

    private val _sessionStartAvailable = MutableLiveData<Boolean>()
    val sessionStartAvailable: LiveData<Boolean> = _sessionStartAvailable
    var askPermissionForPro = false
    var isAddingActivity = false
    var startDate = ""


    var currentPage = 1
    var pageLimit = 10
    var isLastPage = false
    var isActivitiesLoading: Boolean = false

    fun fetchActivityFromServer() {
        viewModelScope.launch {
            isActivitiesLoading = true
            userRepository.getAllActivityList(currentPage, pageLimit).collect { resource ->
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
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                    object : BinaryActionCallback {
                                        override fun yes() {
                                            fetchActivityFromServer()
                                        }

                                        override fun no() {}
                                    }
                            })
                        } else {
                            sendMessage("Error Connecting to internet")
                        }
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            if (it.isNotEmpty()) {

                                formatDataSetPagination(it)
                                currentPage++

                                /*currentPage++
                                fetchActivityFromServer()*/
                            } else {
                                isLastPage = true
                            }

                        }
                    }
                }
            }
        }


    }


//    private fun formatDataSet(activities: List<OActivityListModal>) {
//        val activitySorted = activities.sortedBy {
//            val dateTime = it.startTime
//            val dateFormat = DateFormats.dateTimeFormatISO
//
//            val time = try {
//                if (dateTime.isNullOrEmpty()) {
//                    0
//                } else {
//                    dateFormat.parse(dateTime)?.time
//                }
//
//            } catch (exp: Exception) {
//                0
//            }
//            time ?: 0
//        }.reversed()
//
//        val activityResponse = ArrayList<OActivityListModal>()
//        val datesSet = HashSet<String>()
//
//        activitySorted.forEach {
//            it.createdDate ?: return@forEach
//
//            val date = DateFormats.formatDateTime(
//                it.startTime, DateFormats.dateTimeFormatISO,
//                DateFormats.dateFormat2
//            )
//            if (date.isEmpty()) return@forEach
//
//            if (!datesSet.contains(date)) {
//                datesSet.add(date)
//                activityResponse.add(OActivityListModal(isHeader = true, createdDate = date))
//            }
//            activityResponse.add(it.apply {
//                isHeader = false
//            })
//        }
//
//        _activities.postValue(activityResponse)
//
//    }

    //TODO Optimize
    private fun formatDataSetPagination(activities: List<OActivityListModal>) {

        val activitiesTemp = if (_activities.value == null) {
            activities
        } else {
            val act = _activities.value as ArrayList
            act.addAll(activities)
            act
        }
        //_activities.postValue(activitiesTemp)

        val activityResponse = ArrayList<OActivityListModal>()

        val datesSet = HashSet<String>()

        activitiesTemp.forEach {
            it.date ?: return@forEach

            val date = DateFormats.formatDateTime(
                it.date, DateFormats.dateFormat3,
                DateFormats.dateFormat6
            )
            val compDate = DateFormats.formatDateTime(
                DateFormats.getCurrentDateOreoFormat(), DateFormats.dateFormat3,
                DateFormats.dateFormat6
            )
            if (date.isEmpty()) return@forEach

            if (!datesSet.contains(date)) {
                datesSet.add(date)
                var isTodayShown=false
                isTodayShown = !datesSet.contains(compDate)

                activityResponse.add(OActivityListModal(isHeader = true, date = date, isTodayEmptyView = isTodayShown))
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

}