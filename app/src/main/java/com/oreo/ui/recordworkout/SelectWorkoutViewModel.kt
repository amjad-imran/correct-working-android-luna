package com.oreo.ui.recordworkout

import android.media.metrics.Event
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.KeyValue
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectWorkoutViewModel @Inject
constructor(
    private val userActivityRepository: OreoUserActivityRepository,
    private val sessionManager: SessionManager,
    private val watchDataStore: WatchDataStore,
    private val keyValueDataSource: KeyValueDataSource,
    private val ringDataStore: RingDataStore,
) : BaseViewModel() {


    private val _oWorkoutListModalResponse = MutableLiveData<List<OWorkoutListModal>>()
    val oWorkoutListModalResponse: LiveData<List<OWorkoutListModal>> = _oWorkoutListModalResponse

    val startWorkout = MutableLiveData<com.noisefit_commans.utils.Event<OWorkoutListModal>>()


    fun isDeviceConnected(): Boolean {
        return sessionManager.connectStateRing.value is ConnectState.ConnectSuccess
    }

    fun isDeviceCharging(): Boolean {
        return sessionManager.isRingCharging.value == true
    }

    fun getWorkoutList() {
        viewModelScope.launch(Dispatchers.IO) {

            userActivityRepository.getWorkoutListRecord().collect { resource ->
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
                                        getWorkoutList()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            _oWorkoutListModalResponse.postValue(it)

                            keyValueDataSource.removeDataByType(KeyValueDataType.RECORD_WORKOUT)
                            kotlinx.coroutines.delay(200L)
                            keyValueDataSource.insertData(
                                KeyValue(
                                    key = "",
                                    value = Gson().toJson(it),
                                    type = KeyValueDataType.RECORD_WORKOUT.name
                                )
                            )
                        }
                    }
                }
            }
        }


    }

    fun isBatteryLow(): Boolean {
        val batteryPercentage = watchDataStore.getBatteryPercentRing()
        return batteryPercentage <= 5
    }

    fun stopWorkout() {
        ringDataStore.deleteOngoingRecordWorkout()
    }

    fun getCurrentTimeStamp(): Long {
        return System.currentTimeMillis() / 1000
    }

    fun isWorkoutOngoing(): Boolean {
        val workout = ringDataStore.getOngoingRecordWorkout()
        return workout != null
    }


}