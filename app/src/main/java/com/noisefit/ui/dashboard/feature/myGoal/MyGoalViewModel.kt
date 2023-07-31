package com.noisefit.ui.dashboard.feature.myGoal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit.data.local.AppStaticData
import com.noisefit.data.local.db.CacheResult
import com.noisefit_commans.data.model.DistanceKmMiMapper
import com.noisefit_commans.data.model.User
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.SyncRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.toMakeTwoDecimal
import com.noisefit.ui.onboarding.onboardProfile.*
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class MyGoalViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore,
    val sessionManager: SessionManager,
    val dataUnitConverter: DataUnitConverter,
    private val syncRepository: SyncRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    private val _editMode = MutableLiveData(false)
    private val _showDurationMinLayout = MutableLiveData(false)
    var showDurationMinLayout = _showDurationMinLayout
    var stepsGoalList = ArrayList<String>()
    var distanceGoalList = ArrayList<String>()
    var caloriesGoalList = ArrayList<String>()

    var distanceGoalMapperList = ArrayList<DistanceKmMiMapper>()
    var standHrList = ArrayList<String>()
    var durationList = ArrayList<String>()

    var userGoals: UserGoals? = null
    var userInfo: UserInfo? = null
    var user: User? = null
    var distanceUnit = ""

    init {
        distanceGoalMapperList.addAll(AppStaticData.getDefaultDistanceMapperValue())
        user = localDataStore.getUser()
        userGoals = getUserGoal1()
        userInfo = getUserInfo1()
        distanceUnit = dataUnitConverter.distanceUnit(userGoals?.getUnit())
        getStepsData()
        for (i in 1..23) {
            standHrList.add("$i hr")
        }
        for (i in 30..150 step 30) {
            durationList.add("$i min")
        }
        for (i in MinStepsGoal..MaxStepsGoal step 1000) {
            stepsGoalList.add("$i steps")
            caloriesGoalList.add("${(i * 0.04).roundToInt()} kcal")
        }

        distanceGoalMapperList.forEach { distanceKmMiMapper ->

            var distance = distanceKmMiMapper.distanceInKm
            if (Units.IMPERIAL == userGoals?.getUnit()) {
                distance = distanceKmMiMapper.distanceInMiles
            }
            val distanceWithUnit = "$distance $distanceUnit"

            distanceGoalList.add(distanceWithUnit)
        }


    }

    private fun getUserInfo1(): UserInfo {
        val user = localDataStore.getUser()
        var gender = user?.userInfo?.gender
        if (gender.isNullOrEmpty()) {
            gender = Gender.MALE.type
        }
        var weight = user?.userInfo?.weight
        if (weight == null || weight == 0) {
            weight = DefaultWeightInKg
        }


        var height = user?.userInfo?.height
        if (height == null || height == 0) {
            height = DefaultHeightInCm
        }

        var age = user?.userInfo?.age
        if (age == null || age == 0) {
            age = 18
        }

        var dob = user?.userInfo?.dob
        if (dob.isNullOrEmpty()) {
            dob =
                "$DefaultYear-${DefaultMonth.toMakeTwoDecimal()}-${DefaultDate.toMakeTwoDecimal()}"
        }

        val stepLength = 66
        return UserInfo(weight, height, age, dob, gender, stepLength)

    }

    private fun getUserGoal1(): UserGoals {
        val user = localDataStore.getUser()
        var stepGoal = user?.userGoals?.stepGoal

        if (stepGoal == null || stepGoal == 0) {
            stepGoal = DefaultStepsGoal
        }

        var caloriesGoal = user?.userGoals?.caloriesGoal
        if (caloriesGoal == null || caloriesGoal == 0) {
            caloriesGoal = (stepGoal * 0.04).roundToInt()
        }
        var distanceGoal = user?.userGoals?.distanceGoal

        if (distanceGoal == null || distanceGoal == 0) {
            distanceGoal = (1.609 * stepGoal).toInt()
        }

        var unitSystem = user?.userGoals?.unitSystem
        if (unitSystem.isNullOrEmpty()) {
            unitSystem = UnitSystem.METRIC.type
        }

        val sleepGoal = 8

        return UserGoals(
            stepGoal,
            caloriesGoal,
            distanceGoal,
            sleepGoal,
            unitSystem = unitSystem,
            standingHr = user?.userGoals?.standingHr ?: 12
        )
    }

    private fun getStepsData() {
        viewModelScope.launch(Dispatchers.IO) {
            syncRepository.getTodaySteps().collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        if (resource.value != null && resource.value.totalActiveTime > 0) {
                            _showDurationMinLayout.postValue(true)
                        } else {
                            _showDurationMinLayout.postValue(false)
                        }


                    }
                    is CacheResult.GenericError -> {
                        sendMessage(resource.errorMessage)
                    }
                }

            }

        }
    }

    fun getDistanceInMeter(selectedDistance: Int): Int {
        val unit = userGoals?.getUnit()
        distanceGoalMapperList.forEach { distanceKmMiMapper ->

            if (unit == Units.IMPERIAL) {
                val distance = distanceKmMiMapper.distanceInMiles
                if (distance == selectedDistance) {
                    return distanceKmMiMapper.milesInMeter
                }
            } else {
                val distance = distanceKmMiMapper.distanceInKm
                if (distance == selectedDistance) {
                    return distanceKmMiMapper.kmInMeter
                }
            }

        }
        return 1000
    }


    fun getDistanceFromMeter(distanceInMeter: Int): Int {
        val unit = userGoals?.getUnit()
        distanceGoalMapperList.forEach { distanceKmMiMapper ->

            if (unit == Units.IMPERIAL) {
                val milesInMeter = distanceKmMiMapper.milesInMeter
                val kmInMeter = distanceKmMiMapper.kmInMeter
                if (milesInMeter == distanceInMeter || kmInMeter == distanceInMeter) {
                    return distanceKmMiMapper.distanceInMiles
                }
            } else {
                val milesInMeter = distanceKmMiMapper.milesInMeter
                val kmInMeter = distanceKmMiMapper.kmInMeter
                if (milesInMeter == distanceInMeter || kmInMeter == distanceInMeter) {
                    return distanceKmMiMapper.distanceInKm
                }
            }
        }
        return 1
    }


    fun setUserGoal(user: User) {
        localDataStore.saveUserInfo(user)
    }

    val editMode: LiveData<Boolean>
        get() = _editMode

    fun setEditMode(mode: Boolean) {
        _editMode.value = mode
    }

    private fun createUserUpdateRequest(): JsonObject? {
        val userObject = JsonObject()
        val userGoalsObj = JsonObject()
        userGoalsObj.apply {
            addProperty("sleep_goals", DefaultSleepGoal)
            addProperty("step_goals", userGoals?.stepGoal)
            addProperty("calories_goals", userGoals?.caloriesGoal)
            addProperty("distance_goals", userGoals?.distanceGoal)
            addProperty("unit_system", userGoals?.unitSystem)
        }
        userObject.add("goal", userGoalsObj)


        return userObject

    }


    fun updateUserProfile() {
        val requestObj = createUserUpdateRequest() ?: return
        viewModelScope.launch {
            userRepository.updateUserProfile(requestObj).collect { resource ->
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
                                    updateUserProfile()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {

                        }
                    }
                }
            }
        }


    }

}