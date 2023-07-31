package com.noisefit.ui.feeds.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.RecentActivities
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class PostExternalDataViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val localDataSource: DataStoredInterface,
    private val dataUnitConverter: DataUnitConverter
) : BaseViewModel() {

    private val _workouts = MutableLiveData<List<SportsModeResponse>>()
    val workouts: LiveData<List<SportsModeResponse>>
        get() = _workouts


    private val _challenge = MutableLiveData<List<ChallengeModel>>()
    val challenge: LiveData<List<ChallengeModel>>
        get() = _challenge

    fun getWorkouts() {
        viewModelScope.launch {
            userRepository.geRecentActivities(false).collect { resource ->
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
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getWorkouts()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            handleRecentActivities(it)

                        }
                    }
                }
            }
        }

    }

    private fun handleRecentActivities(recentActivities: RecentActivities) {
        if (recentActivities.activities.isNullOrEmpty()) {
            _workouts.postValue(recentActivities.activities ?: ArrayList())
            return
        } else {
            val user = localDataSource.getUser()
            recentActivities.activities?.forEach { sportsModeResponse ->
                if (sportsModeResponse.distance != null && sportsModeResponse.distance!! > 0) {
                    val units = user?.userGoals?.getUnit() ?: Units.METRIC
                    sportsModeResponse.formattedData = dataUnitConverter.formatDistance(
                        sportsModeResponse.distance?.toInt() ?: 0, units
                    )
                    sportsModeResponse.formattedDataUnit = dataUnitConverter.distanceUnit(units)
                } else {
                    sportsModeResponse.formattedData = sportsModeResponse.calories.toString()
                    sportsModeResponse.formattedDataUnit = "kcal"
                }
            }
        }
        _workouts.postValue(recentActivities.activities ?: ArrayList())
    }

    fun getChallenges() {
        viewModelScope.launch {
            userRepository.getRecentChallenges().collect { resource ->
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
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getChallenges()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _challenge.postValue(it)
                        }
                    }
                }
            }
        }

    }


}