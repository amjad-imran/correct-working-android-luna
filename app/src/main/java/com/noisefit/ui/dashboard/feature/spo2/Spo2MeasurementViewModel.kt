package com.noisefit.ui.dashboard.feature.spo2

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.repository.abstraction.SyncRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.BloodOxygenBreakup
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class Spo2MeasurementViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    private val syncRepository: SyncRepository,
) : BaseViewModel() {


    private var _spo2Value = MutableLiveData<Int>()
    var spo2Value = _spo2Value

    init {
        getLastBloodOxygenValue()
    }

    private fun getLastBloodOxygenValue() {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.getTodayBloodOxygen().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        if (!resource.value.isNullOrEmpty()) {
                            _spo2Value.postValue( resource.value.last().value ?: 0)
                        }
                    }
                    is CacheResult.GenericError -> {
                        sendMessage(resource.errorMessage)
                    }
                }

            }

        }
    }

    fun <T> saveOfflineDb(data: T) {
        viewModelScope.launch {
            userRepository.saveBloodOxygenData(data as List<BloodOxygenBreakup>)
                .collect { resource ->
                    when (resource) {
                        is CacheResult.Success<*> -> {
//                            summary.userActivity.boData = resource.value
//                            summary.userActivities.postValue(summary.userActivity)
                        }
                        is CacheResult.GenericError -> {
                            LOGS.d("Error in syncing Blood Oxygen data " + resource.errorMessage)

                        }
                    }
                }

        }
    }

}