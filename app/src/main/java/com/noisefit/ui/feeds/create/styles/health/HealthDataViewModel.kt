package com.noisefit.ui.feeds.create.styles.health

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.data.model.HealthOverviewData
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthDataViewModel @Inject constructor(
    private val userRepository: UserRepository,
    val localDataStore: DataStoredInterface,
    val watchesSDK: WatchesSDK
) : BaseViewModel() {

    val healthOverviewData: MutableLiveData<HealthOverviewData> =
        MutableLiveData<HealthOverviewData>()
    var deviceFeatures: DeviceFeatures? = null

    var caloriesProgressCompleted: Float = -1f
    var stepsProgressCompleted: Float = -1f
    var distanceProgressCompleted: Float = -1f


    fun getInitialOfflineData() {

        if (deviceFeatures == null) {
            deviceFeatures = getDeviceFeatureList()
        }


        viewModelScope.launch(Dispatchers.IO) {
            val userActivities = userRepository.getSummaryHealthOverview(
                deviceFeatures!!
            )
            healthOverviewData.postValue(userActivities)
        }
    }

    fun isActivityDataZero(): Boolean {
        val distanceFloat = try {
            healthOverviewData.value?.distance?.toFloat()
        } catch (exp: Exception) {
            0.0f
        }
        return healthOverviewData.value?.steps == 0 && healthOverviewData.value?.calories == 0
                && distanceFloat == 0.0f
    }


    private fun getDeviceFeatureList(): DeviceFeatures {
        val deviceFeatures = localDataStore.getDeviceFeatures()
        if (deviceFeatures != null) {
            deviceFeatures.calorieData = 0
            if (watchesSDK.isCaloriesSupported()) {
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


}