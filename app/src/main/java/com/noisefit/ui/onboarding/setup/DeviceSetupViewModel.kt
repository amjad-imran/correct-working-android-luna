package com.noisefit.ui.onboarding.setup

import com.noisefit.data.repository.abstraction.UpdateRepository
import com.noisefit.session.SessionManager
import com.noisefit.ui.onboarding.onboardProfile.DefaultDate
import com.noisefit.ui.onboarding.onboardProfile.DefaultHeightInCm
import com.noisefit.ui.onboarding.onboardProfile.DefaultMonth
import com.noisefit.ui.onboarding.onboardProfile.DefaultStepsGoal
import com.noisefit.ui.onboarding.onboardProfile.DefaultWeightInKg
import com.noisefit.ui.onboarding.onboardProfile.DefaultYear
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.User
import com.noisefit_commans.models.Gender
import com.noisefit_commans.models.HeightUnitSystem
import com.noisefit_commans.models.UnitSystem
import com.noisefit_commans.models.Units
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.models.UserInfo
import com.noisefit_commans.models.WeightUnitSystem
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.toMakeTwoDecimal
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class DeviceSetupViewModel @Inject constructor(
    val ringDataStore: RingDataStore,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val updateRepository: UpdateRepository
) : BaseViewModel() {


    var setupStarted: Boolean = false
    var user: User? = null

    var dobDate = DefaultDate
    var dobMonth = DefaultMonth
    var dobYear = DefaultYear

    private var stepsGoal: Int = 0
    private var calories: Int = 0
    var weightUnitSystem: WeightUnitSystem = WeightUnitSystem.METRIC
    var heightUnitSystem: HeightUnitSystem = HeightUnitSystem.METRIC
    private var localUser: User? = null
    private var unitListHeight = 0
    private var genderListHeight = 0


    init {
        heightUnitSystem =
            if (localDataStore.getUnit().name.isEmpty() || (localDataStore.getUnit().name.lowercase() == HeightUnitSystem.METRIC.name.lowercase())) {
                HeightUnitSystem.METRIC
            } else HeightUnitSystem.IMPERIAL
        weightUnitSystem =
            if (localDataStore.getUnit().name.isEmpty() || (localDataStore.getUnit().name.lowercase() == HeightUnitSystem.METRIC.name.lowercase())) {
                WeightUnitSystem.METRIC
            } else WeightUnitSystem.IMPERIAL

        stepsGoal = DefaultStepsGoal

        localUser = localDataStore.getUser()

        localUser?.userInfo?.gender?.let {
            genderListHeight = when (it.lowercase()) {
                "male" -> 0
                "female" -> 1
                "other", "others" -> 2
                else -> 3
            }
        }
        localUser?.userGoals?.let {
            if (it.stepGoal != 0) {
                stepsGoal = it.stepGoal
            } else {
                stepsGoal = DefaultStepsGoal
            }

        }

        localUser?.userGoals.let {
            unitListHeight = if (it.toString().lowercase() == Units.METRIC.name.lowercase()) 0
            else 1
        }

    }


    fun setUpUserDetails() {
        user = localDataStore.getUser()

        user?.apply {
            userGoals = getUserGoal1()
            userInfo = getUserInfo()
            localDataStore.saveUserInfo(this)
        }


    }

    fun getUserInfo(): UserInfo {
        val user = localDataStore.getUser()
        var gender = user?.userInfo?.gender
        if (gender.isNullOrEmpty()) {
            gender = Gender.MALE.type
        }
        var weight = user?.userInfo?.weight
        if (weight == null || weight == 0) {
            weight = getDefaultWeightInKg()
        }


        var height = user?.userInfo?.height
        if (height == null || height == 0) {
            height = getDefaultHeightInCm()
        }

        var age = user?.userInfo?.age
        if (age == null || age == 0) {
            age = 18
        }

        var dob = user?.userInfo?.dob
        if (dob.isNullOrEmpty()) {
            dob = getDob()
        }

        val stepLength = 66
        return UserInfo(weight, height, age, dob, gender, stepLength)

    }

    fun getUserGoal1(): UserGoals {
        val user = localDataStore.getUser()
        var stepGoal = user?.userGoals?.stepGoal

        if (stepGoal == null || stepGoal == 0) {
            stepGoal = getStepsDefaultGoal()
        }

        var caloriesGoal = user?.userGoals?.caloriesGoal
        if (caloriesGoal == null || caloriesGoal == 0) {
            caloriesGoal = getCaloriesGoal()
        }
        var distanceGoal = user?.userGoals?.distanceGoal

        if (distanceGoal == null || distanceGoal == 0) {
            distanceGoal = getDistanceGoal()
        }

        var unitSystem = user?.userGoals?.unitSystemLuna
        if (unitSystem.isNullOrEmpty()) {
            unitSystem = UnitSystem.METRIC.type
        }

        val sleepGoal = 8

        return UserGoals(stepGoal = stepGoal,
            caloriesGoal = caloriesGoal,
            hydrationGoals = user?.userGoals?.hydrationGoals?:3000,
            distanceGoal = distanceGoal,
            sleepGoal = sleepGoal,
            unitSystemLuna = unitSystem)
    }

    private fun getDefaultWeightInKg(): Int {
        return DefaultWeightInKg
    }

    private fun getDefaultHeightInCm(): Int {
        return DefaultHeightInCm
    }

    private fun getDob(): String {
        val month = dobMonth
        return "$dobYear-${month.toMakeTwoDecimal()}-${dobDate.toMakeTwoDecimal()}"
    }

    private fun getStepsDefaultGoal(): Int {
        return stepsGoal
    }

    private fun getCaloriesGoal(): Int {
        return (stepsGoal * 0.04).roundToInt()
    }

    private fun getDistanceGoal(): Int {
        return if (weightUnitSystem == WeightUnitSystem.METRIC) {
            stepsGoal
        } else {
            (1.609 * stepsGoal).toInt()
        }
    }


}