package com.noisefit.ui.trophies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.HealthOverviewData
import com.noisefit_commans.data.model.trophies.DailyItem
import com.noisefit_commans.data.model.trophies.Distance
import com.noisefit_commans.data.model.trophies.Steps
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.StringUtils.convertDoubleStringToInt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrophiesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val localDataStore: DataStoredInterface
) : BaseViewModel() {

    var buddyMobileNumber: String? = null
    private val _steps = MutableLiveData<Steps>()
    val steps: LiveData<Steps> = _steps

    private val _buddyRemoved = MutableLiveData<Event<Boolean>>()
    val buddyRemoved: LiveData<Event<Boolean>> = _buddyRemoved


    private val _distance = MutableLiveData<Distance>()
    val distance: LiveData<Distance> = _distance

    val badgeCollected: MutableLiveData<Event<Boolean>> = MutableLiveData()

    val userActivities: MutableLiveData<HealthOverviewData> = MutableLiveData<HealthOverviewData>()
    var unit = Units.METRIC

    private var trophiesToCollect = ArrayList<Pair<DailyItem, TrophiesType>>()
    val collectTrophy = MutableLiveData<Event<Pair<DailyItem, TrophiesType>>>()


    fun getNextTrophy() {
        if (trophiesToCollect.isEmpty()) return

        val trophyData = trophiesToCollect.first()
        collectTrophy.postValue(Event(trophyData))
        trophiesToCollect.removeFirstOrNull()
    }

    fun getUnitValue(): Units {
        unit = localDataStore.getUnit()
        return unit
    }


    fun getTrophiesData(collectTrophy: Boolean) {
        unit = localDataStore.getUnit()
        viewModelScope.launch {
            val date = DateFormats.getTodaysDateString(7)
            val unitSystem = if (unit == Units.METRIC) {
                "metric"
            } else {
                "imperial"
            }
            userRepository.getTrophiesData(date, unitSystem).collect { resource ->
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
                                    getTrophiesData(collectTrophy)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { response ->

                            _steps.value = (response.steps)
                            _distance.value = (response.distance)

                            if (buddyMobileNumber.isNullOrEmpty() && collectTrophy) {
                                getTrophiesToCollect(response.steps, response.distance)
                            }

                        }
                    }
                }
            }
        }
    }

    private fun getTrophiesToCollect(steps: Steps, distance: Distance) {
        trophiesToCollect.clear()
        steps.daily?.forEach {
            if (it.isStepsAchieved == 1 && it.isStepsCollect == 0) {
                trophiesToCollect.add(Pair(it, TrophiesType.STEPS))
            }
        }
        steps.lifetime?.forEach {
            if (it.isStepsAchieved == 1 && it.isStepsCollect == 0) {
                trophiesToCollect.add(Pair(it, TrophiesType.STEPS))
            }
        }
        distance.daily?.forEach {
            if (it.isDistanceAchieved == 1 && it.isDistanceCollect == 0) {
                trophiesToCollect.add(Pair(it, TrophiesType.DISTANCE))
            }
        }
        distance.lifetime?.forEach {
            if (it.isDistanceAchieved == 1 && it.isDistanceCollect == 0) {
                trophiesToCollect.add(Pair(it, TrophiesType.DISTANCE))
            }
        }
        getNextTrophy()
    }

    fun getBuddyTrophiesData(mobileNumber: String) {
        unit = localDataStore.getUnit()
        viewModelScope.launch {

            val requestObject = JsonObject().apply {
                addProperty("info", mobileNumber)
            }

            userRepository.getBuddiesTrophiesData(requestObject).collect { resource ->
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
                                    getBuddyTrophiesData(mobileNumber)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            _steps.value = (response.steps)
                            _distance.value = (response.distance)
                        }
                    }
                }
            }
        }
    }


    fun getDistanceGoal(goal: Int): String {
        return when (getUnitValue()) {
            Units.METRIC -> "${
                DistanceUtil.getDistanceFromMetres(
                    goal,
                    Units.METRIC
                ).convertDoubleStringToInt()
            } kms"
            Units.IMPERIAL -> "${
                DistanceUtil.getDistanceFromMetres(
                    goal,
                    Units.IMPERIAL
                ).convertDoubleStringToInt()
            } miles"
            else -> "$goal"
        }
    }


    fun markBadgeCollected(id: Int) {
        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                this.addProperty("id", id)
            }

            userRepository.collectBadge(requestObject).collect { resource ->
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
                                    markBadgeCollected(id)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            badgeCollected.postValue(Event(true))
                            localDataStore.setAwardCount(localDataStore.getAwardCount() + 1)
                            getTrophiesData(false)
                        }
                    }
                }
            }
        }
    }


    fun getUserMobileNumber(): String? {
        return localDataStore.getUser()?.mobile
    }

}

enum class TrophiesInterval {
    STEPS, DISTANCE
}