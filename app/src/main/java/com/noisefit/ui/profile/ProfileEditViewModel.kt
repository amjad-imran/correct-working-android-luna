package com.noisefit.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.googleFit.GoogleFitDataObservers
import com.noisefit.data.local.AppStaticData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.AuthenticationRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit.ui.onboarding.onboardProfile.DefaultDate
import com.noisefit.ui.onboarding.onboardProfile.DefaultHeightInCm
import com.noisefit.ui.onboarding.onboardProfile.DefaultMonth
import com.noisefit.ui.onboarding.onboardProfile.DefaultSleepGoal
import com.noisefit.ui.onboarding.onboardProfile.DefaultWeightInKg
import com.noisefit.ui.onboarding.onboardProfile.DefaultYear
import com.noisefit.ui.onboarding.onboardProfile.MaxHeightInCm
import com.noisefit.ui.onboarding.onboardProfile.MaxHeightInInches
import com.noisefit.ui.onboarding.onboardProfile.MaxWeightInKg
import com.noisefit.ui.onboarding.onboardProfile.MaxWeightInLbs
import com.noisefit.ui.onboarding.onboardProfile.MinHeightInCm
import com.noisefit.ui.onboarding.onboardProfile.MinHeightInInches
import com.noisefit.ui.onboarding.onboardProfile.MinWeightInKg
import com.noisefit.ui.onboarding.onboardProfile.MinWeightInLbs
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.Interest
import com.noisefit_commans.data.model.User
import com.noisefit_commans.models.Gender
import com.noisefit_commans.models.Units
import com.noisefit_commans.models.UserLocation
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.toMakeTwoDecimal
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.ITEM_HEIGHT
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.ScreenUtils
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject
import kotlin.math.roundToInt

const val METRIC = "Metric (cm/kg)"
const val IMPERIAL = "Imperial (in/lbs)"

@HiltViewModel
class ProfileEditViewModel
@Inject constructor(
    var sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val userRepository: UserRepository,
    val screenUtils: ScreenUtils,
    val authenticationRepository: AuthenticationRepository,
    val googleFitDataObservers: GoogleFitDataObservers
) : BaseViewModel() {

    private val _userDetailsUpdated = MutableLiveData<Event<Boolean>>()
    val userDetailsUpdated = _userDetailsUpdated
    var dobDate = DefaultDate
    var dobMonth = DefaultMonth
    var dobYear = DefaultYear

    var heightInCm = MutableLiveData<String>()

    var heightInCmList = ArrayList<String>()
    var heightInInchList = ArrayList<String>()

    var weightInKg = MutableLiveData<String>()
    var weightInKgList = ArrayList<String>()
    var weightInLbsList = ArrayList<String>()


    var gender = MutableLiveData(Gender.NotToSay.name)
    var userName = MutableLiveData("_")
    var interests = MutableLiveData(ArrayList<Interest>())
    var phoneNumber = MutableLiveData<String?>()
    var email = MutableLiveData<String?>()
    var profileLink = MutableLiveData<String?>()
    var unit = MutableLiveData<Units>()
    var notificationSetting = MutableLiveData<Int>()
    var unitList = ArrayList<String>()
    var dobValue = MutableLiveData(false)
    var localUser: User? = null

    var tempLocation: UserLocation? = null


    init {
        unitList.add(METRIC)
        unitList.add(IMPERIAL)
        localUser = localDataStore.getUser()

        updateName(localUser?.firstName)
        setGender(localUser?.userInfo?.gender)
        phoneNumber.value = localUser?.mobile
        unit.value = localDataStore.getUnit()
        notificationSetting.value = 1


        email.value = localUser?.email
        interests.value = if (localUser?.interests != null) {
            localUser?.interests as ArrayList<Interest>
        } else {
            ArrayList()
        }

        tempLocation = localUser?.location

        for (i in MinHeightInCm..MaxHeightInCm) {
            heightInCmList.add("$i cm")
        }
        for (i in MinHeightInInches..MaxHeightInInches) {
            heightInInchList.add("$i inches")
        }

        for (i in MinWeightInKg..MaxWeightInKg) {
            weightInKgList.add("$i kg")
        }
        for (i in MinWeightInLbs..MaxWeightInLbs) {
            weightInLbsList.add("$i lbs")
        }

        setDefaultUserHeight()
        setDefaultUserWeight()

        if (!localUser?.imageUrl.isNullOrEmpty()) {
            profileLink.value = localUser?.imageUrl
        }

        try {
            if (!localUser?.userInfo?.dob.isNullOrEmpty()) {
                val userDob = localUser?.userInfo?.dob
                val dateArray = userDob!!.split("-")
                setDob(
                    dateArray[0].toInt(), dateArray[1].toInt() - 1, dateArray[2].toInt()
                )
                dobValue.value = true
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    fun getDob(): String {
        val month = dobMonth
        return "$dobYear-${month.toMakeTwoDecimal()}-${dobDate.toMakeTwoDecimal()}"
    }

    fun getAge(): Int {
        return Period.between(
            LocalDate.of(dobYear, dobMonth, dobDate), LocalDate.now()
        ).years
    }

    fun setDob(year: Int, month: Int, day: Int) {
        dobDate = day
        dobMonth = month + 1
        dobYear = year
    }


    fun convertValues() {
        if (unit.value == Units.METRIC) {

//            if(height.isNullOrEmpty()){
//                height = "63.0"
//            }
            heightInCm.value =
                DistanceUtil.convertInchToCms((heightInCm.value ?: "63.0").toDouble().roundToInt())

//            if (weight.isNullOrEmpty()){
//                weight = "132.3"
//            }
            weightInKg.value =
                DistanceUtil.convertLbsToKg((weightInKg.value ?: "132.3").toDouble().roundToInt())
        } else {
//            if(distanceGoal.isNullOrEmpty()){
//                distanceGoal = "8.0"
//            }
//            distanceGoal =
//                DistanceUtil.convertKmValueToMi((distanceGoal ?: "8.0").toDouble().roundToInt())
//            if(height.isNullOrEmpty()){
//                height = "160"
//            }
            heightInCm.value =
                DistanceUtil.convertCmsToInch((heightInCm.value ?: "160").toDouble().roundToInt())
//            if(stepLength.isNullOrEmpty()){
//                stepLength = "66"
//            }
//            stepLength = DistanceUtil.convertCmsToInch((stepLength ?: "66").toDouble().roundToInt())
//            if(weight.isNullOrEmpty()){
//                weight = "60"
//            }
            weightInKg.value =
                DistanceUtil.convertKgToLbs((weightInKg.value ?: "60").toDouble().roundToInt())
        }
    }

    private fun setDefaultUserHeight() {
        val savedHeight = localUser?.userInfo?.height

        if (savedHeight == null) {
            if (unit.value == Units.IMPERIAL) {
                heightInCm.value =
                    DistanceUtil.convertCmsToInch(DefaultHeightInCm).toDouble().roundToInt()
                        .toString()
            } else {
                heightInCm.value = DefaultHeightInCm.toString()
            }
        } else {
            if (unit.value == Units.IMPERIAL) {
                heightInCm.value =
                    DistanceUtil.convertCmsToInch(savedHeight).toDouble().roundToInt().toString()
            } else {
                heightInCm.value = savedHeight.toString()
            }
        }


    }

    private fun setDefaultUserWeight() {
        val savedWeight = localUser?.userInfo?.weight

        if (savedWeight == null) {
            if (unit.value == Units.IMPERIAL) {
                weightInKg.value =
                    DistanceUtil.convertKgToLbs(DefaultWeightInKg).toDouble().roundToInt()
                        .toString()
            } else {
                weightInKg.value = DefaultHeightInCm.toString()
            }
        } else {
            if (unit.value == Units.IMPERIAL) {
                weightInKg.value =
                    DistanceUtil.convertKgToLbs(savedWeight).toDouble().roundToInt().toString()
            } else {
                weightInKg.value = savedWeight.toString()
            }
        }
    }


    fun setWeight(weight: String) {
        weightInKg.value = weight

    }

    fun setHeight(height: String) {
        heightInCm.value = height
    }

    fun getHeight(): String {
        var unit = "cm"

        if (this.unit.value == Units.IMPERIAL) {
            unit = "inches"

        }
        return "${heightInCm.value?.toDouble()?.roundToInt()} $unit"
    }

    fun getWeight(): String {
        var unit = "kg"

        if (this.unit.value == Units.IMPERIAL) {
            unit = "lbs"

        }
        return "${weightInKg.value?.toDouble()?.roundToInt()} $unit"
    }


    fun getHeightList(): ArrayList<String> {
        if (unit.value == Units.METRIC) {
            return heightInCmList
        }
        return heightInInchList
    }

    fun getWeightList(): ArrayList<String> {
        if (unit.value == Units.METRIC) {
            return weightInKgList
        }
        return weightInLbsList
    }

    fun getGenderValue(): String {

        val tempGender: String =
            if (gender.value?.lowercase() == Gender.MALE.type.lowercase()) "Man"
            else if (gender.value?.lowercase() == Gender.FEMALE.type.lowercase()) "Woman"
            else if (gender.value?.lowercase() == Gender.OTHER.type.lowercase()) "Non-binary"
            else "Prefer not to say"
        return tempGender.replaceFirstChar { if (it.isLowerCase()) it.titlecase(DateFormats.defaultLocale) else it.toString() }
    }

    fun setGender(lGender: String?) {

        val temp: String = when (lGender?.lowercase()) {
            "male", "man" -> Gender.MALE.type
            "female", "woman" -> Gender.FEMALE.type
            "non-binary", "other" -> Gender.OTHER.type
            else -> Gender.NotToSay.type
        }
        gender.value = temp
    }

    private fun getGenderForServer(): String {

        val temp: String = when (gender.value?.lowercase()) {
            "male" -> Gender.MALE.type
            "female" -> Gender.FEMALE.type
            "non-binary", "other" -> Gender.OTHER.type
            else -> Gender.NotToSay.type
        }
        return temp
    }

    private fun getGenderForBmr(): Gender {

        val temp: Gender = when (gender.value?.lowercase()) {
            "male" -> Gender.MALE
            "female" -> Gender.FEMALE
            "non-binary", "other" -> Gender.OTHER
            else -> Gender.NotToSay
        }
        return temp
    }

    fun getSelectedUnit(): String {
        if (unit.value == Units.METRIC) {
            return METRIC
        }
        return IMPERIAL
    }

    fun getUserCity(): String {
        if (tempLocation == null) {
            return localDataStore.getUser()?.location?.city ?: "Add Location"
        } else {
            return tempLocation?.city ?: "Add Location"
        }
    }

    fun setSelectedUnit(selectedUnit: String) {
        unit.value = if (selectedUnit.equals(METRIC, false)) {
            Units.METRIC
        } else {
            Units.IMPERIAL
        }
    }

    fun updateName(name: String?) {
        if (name.isNullOrEmpty()) {
            userName.value = ""
        } else {
            userName.value = name
        }
    }


    fun getEndGameValue(id: String?): String {
        if (id.isNullOrEmpty()) {
            return "Not Set"
        }
        val endGameList = localDataStore.getEndGameList()

        endGameList.forEach {
            if (it.id == id.toInt()) {
                return it.title
            }
        }
        return "Not Set"
    }

    fun uploadUserImage(selectedImageUri: Uri) {
        viewModelScope.launch {
            userRepository.uploadUserImage(selectedImageUri).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        uploadUserImage(selectedImageUri)
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.let {
                            if (it.imageUrl != null) {
                                sendMessage("Profile image updated")
                                profileLink.value = it.imageUrl
                            }/*if (it.success) {
                                sendMessage("Profile image updated")
                            } else {

                            }*/


                        }
                    }
                }
            }
        }


    }


    fun getPadding(context: Context): Int {
        val rvHeight =
            context.resources.getDimension(com.noisefit_commans.R.dimen.recycler_height_spinner)
        val dpHeight = screenUtils.pxToDp(rvHeight.roundToInt(), context)
        return screenUtils.dpToPx(
            (dpHeight / 2) - (ITEM_HEIGHT / 2), context
        ).roundToInt()
    }


    private fun getDistanceInMeter(distanceInMeter: Int?): Int {
        if (distanceInMeter == null) {
            return 1600
        }
        AppStaticData.getDefaultDistanceMapperValue().forEach { distanceKmMiMapper ->
            if (unit.value == Units.IMPERIAL) {
                val milesInMeter = distanceKmMiMapper.milesInMeter
                val kmInMeter = distanceKmMiMapper.kmInMeter
                if (milesInMeter == distanceInMeter || kmInMeter == distanceInMeter) {
                    return distanceKmMiMapper.milesInMeter
                }
            } else {
                val milesInMeter = distanceKmMiMapper.milesInMeter
                val kmInMeter = distanceKmMiMapper.kmInMeter
                if (milesInMeter == distanceInMeter || kmInMeter == distanceInMeter) {
                    return distanceKmMiMapper.kmInMeter
                }
            }
        }
        return 1600
    }


    private fun createUserUpdateRequest(): JsonObject {
        val userObject = JsonObject().apply {
            addProperty("first_name", userName.value)
            addProperty("image_url", profileLink.value)
            addProperty("notifications_enabled_luna", notificationSetting.value)
        }


        var userInfo: JsonObject? = null
        try {
            //TODO Optimize conversion
            userInfo = JsonObject()

            val heightValue =/* if (unit.value == Units.IMPERIAL) {
                DistanceUtil.convertInchToCms(
                    heightInCm.value!!.toDouble().roundToInt()
                ).toDouble().roundToInt()
            } else {*/
                heightInCm.value?.toDouble()?.roundToInt()
            //}
            val weightValue = /*if (unit.value == Units.IMPERIAL) {
                DistanceUtil.convertLbsToKg(
                    weightInKg.value!!.toDouble().roundToInt()
                ).toDouble().roundToInt()
            } else {*/
                weightInKg.value?.toDouble()?.roundToInt()
            //}


            userInfo.apply {
                addProperty("weight", weightValue)
                addProperty("height", heightValue)
                addProperty("dob", getDob())
                addProperty("gender", getGenderForServer())
                addProperty("step_length", 70)
            }
            userObject.add("info", userInfo)/*if (interests.value != null) {
                if (interests.value!!.size > 0)
                    userObject.add("interest_id", JsonArray().apply {
                        interests.value?.forEach { id ->
                            if (id.id != null) {
                                this.add(id.id!!.toInt())
                            }
                        }
                    })
            }*/

        } catch (exp: Exception) {
            LOGS.d("User Info null")
            userInfo = null
        }

        val stepGoalNew = ApplicationUtils.bmiCalculate(
            heightInCm.value!!.toFloat(),
            weightInKg.value!!.toFloat(),
            unit.value?.name ?: Units.METRIC.name,
            unit.value?.name ?: Units.METRIC.name
        )

        val caloriesGoalNew = ApplicationUtils.bmrCalculate(
            heightInCm.value!!.toFloat(),
            weightInKg.value!!.toFloat(),
            unit.value?.name ?: Units.METRIC.name,
            unit.value?.name ?: Units.METRIC.name,
            getAge(),
            getGenderForBmr()

        )
        val stepsGoal = stepGoalNew.second.toInt()
        val caloriesGoal = caloriesGoalNew


        val userGoals = JsonObject()
        userGoals.apply {
            addProperty("sleep_goals", DefaultSleepGoal)
            addProperty("step_goals", stepsGoal)
//            addProperty("step_goals", localUser?.userGoals?.stepGoal)
            addProperty("calories_goals", caloriesGoal)
//            addProperty("calories_goals", localUser?.userGoals?.caloriesGoal)
            addProperty("distance_goals", getDistanceInMeter(localUser?.userGoals?.distanceGoal))
            addProperty("unit_system", unit.value?.name)
            addProperty("unit_system_luna", unit.value?.name)
        }
        userObject.add("goal", userGoals)


        /*tempLocation?.let {
            val userLocation = JsonObject()
            userLocation.apply {
                addProperty("city_id", it.cityId)
                addProperty("state_id", it.stateId)
            }

            userObject.add("location", userLocation)
        }*/






        return userObject

    }

    fun updateUserProfile() {
        val request = createUserUpdateRequest()


        viewModelScope.launch {
            userRepository.updateUserProfile(request).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        updateUserProfile()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            authenticationRepository.saveUserInfo(it)
                            _userDetailsUpdated.postValue(Event(true))
                        }
                    }
                }
            }
        }


    }

    fun setInterests(selectedInterests: List<Interest>) {
        interests.postValue(selectedInterests as ArrayList<Interest>)
    }

    fun getUserInterestToDisplay(interest: List<Interest>?, default: String): String {
        if (interest.isNullOrEmpty()) {
            return default
        }
        return if (interest.size == 1) {
            interest.first().name.capitalizeWords() ?: default
        } else {
            val firstInterest = interest.first().name.capitalizeWords() ?: ""
            "$firstInterest and ${(interest.size ?: 2) - 1} more"
        }
    }

    fun instantUpdateInterest(it1: java.util.ArrayList<Interest>) {
        val user = localDataStore.getUser()
        user?.interests = it1
        localDataStore.saveUserInfo(user!!)
    }

    fun isMetric(): Boolean {
        return unit.value != Units.IMPERIAL
    }

}
