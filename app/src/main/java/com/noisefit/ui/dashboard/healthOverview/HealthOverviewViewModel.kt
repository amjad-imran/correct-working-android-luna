package com.noisefit.ui.dashboard.healthOverview

import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.data.model.User
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.dashboard.summary.Summary
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.ColorFitDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class HealthOverviewViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    val sessionManager: SessionManager,
    val watchesSDK: WatchesSDK,
    private val localDataStore: DataStoredInterface
) : BaseViewModel() {

    var summary = Summary()

    init {
        summary.user = getUser()
        summary.connectedDevice = getDeviceConnected()
        summary.deviceFeatures = getDeviceFeatureList()
        getInitialOfflineData()

    }

    private fun getDeviceFeatureList(): DeviceFeatures {
        val deviceFeatures = localDataStore.getDeviceFeatures()
        if (deviceFeatures != null) {
            deviceFeatures.calorieData = 0
            if(watchesSDK.isCaloriesSupported()){
                deviceFeatures.calorieData = 1
            }
            return deviceFeatures
        }
        //feed dummy data in device features

        return DeviceFeatures(
            stepsData = 1,
            heartRate = 1,
            sleepData = 1,
            bloodOxygen = 1,
            stressCount = 1,
            bodyTemperature = 0,
            calorieData = 0
        )
    }

    private fun getInitialOfflineData() {


        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val userActivities =
                userRepository.getAllHealthOverview(summary.deviceFeatures!!)

            withContext(Dispatchers.Main) {
                summary.healthOverviewData.value = userActivities
            }

            setLoading(false)
        }
    }


    private fun getDeviceConnected(): ColorFitDevice? {
        return localDataStore.getConnectedDevice()
    }


    fun getUser(): User? {
        val userInfo = localDataStore.getUser()
        return userInfo
    }

}