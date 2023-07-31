package com.noisefit.ui.content.player

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.DeviceFeatures
import com.noisefit_commans.data.model.HealthOverviewData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentPlayerViewModel @Inject constructor(
    val localDataStore: DataStoredInterface,
    val watchesSDK: WatchesSDK,
    val userRepository: UserRepository,
    val sessionManager: SessionManager
) :
    BaseViewModel() {


    var videoUrl: String? = null
    var title: String? = null
    var id: Int? = null
    var progress: Int? = null
    var caloriesProgressCompleted: Float = -1f
    var stepsProgressCompleted: Float = -1f
    var distanceProgressCompleted: Float = -1f

    val healthOverviewData: MutableLiveData<HealthOverviewData> =
        MutableLiveData<HealthOverviewData>()

    private fun getDeviceFeatureList(): DeviceFeatures {

        val deviceFeatures = localDataStore.getDeviceFeatures()
        if (deviceFeatures != null) {
            deviceFeatures.calorieData = 0
            if (watchesSDK.isCaloriesSupported()) {
                deviceFeatures.calorieData = 1
            }
            return deviceFeatures
        }

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

    fun getHealthOverviewData() {
        viewModelScope.launch(Dispatchers.IO) {
            val userActivities = userRepository.getSummaryHealthOverview(
                getDeviceFeatureList()
            )
            healthOverviewData.postValue(userActivities)
        }
    }


}