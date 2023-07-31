package com.noisefit.ui.onboarding.onboardProfile

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.EndGame
import com.noisefit_commans.data.model.Interest
import com.noisefit_commans.data.model.LocalUserData
import com.noisefit_commans.data.model.User
import com.noisefit_commans.models.Gender
import com.noisefit_commans.models.HeightUnitSystem
import com.noisefit_commans.models.Units
import com.noisefit_commans.models.WeightUnitSystem
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.toMakeTwoDecimal
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.ITEM_HEIGHT
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.ScreenUtils
import com.noisefit_commans.utils.WheelItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt


val DefaultDate = 15
val DefaultMonth = 5
val DefaultYear = 2000

const val DefaultHeightInCm = 170
const val MinHeightInCm = 115
const val MaxHeightInCm = 200

const val DefaultHeightInInches = 67
const val MinHeightInInches = 45
const val MaxHeightInInches = 79

const val DefaultWeightInKg = 65
const val MinWeightInKg = 25
const val MaxWeightInKg = 250

const val DefaultWeightInLbs = 143
const val MinWeightInLbs = 55
const val MaxWeightInLbs = 551


const val DefaultStepsGoal = 5000
const val MinStepsGoal = 3000
const val MaxStepsGoal = 25000

const val DefaultCaloriesValue = 350
const val MinCaloriesValue = 100
const val MaxCaloriesValue = 2500

const val DefaultSleepGoal = 8

private const val DefaultHeightInGender = 1
private const val MinHeightInGender = 0
private const val MaxHeightInGender = 3

private const val MinHeightInUnit = 0
private const val MaxHeightInUnit = 1

var unitInList = ArrayList<Int>()
private var unitListHeight = 0


@HiltViewModel
class SetupProfileViewModel
@Inject constructor(
    private val userRepository: UserRepository,
    val localDataStore: DataStoredInterface,
    val ringDataStore: RingDataStore,
    private val screenUtils: ScreenUtils,
    val sessionManager: SessionManager
) : BaseViewModel() {

    private val _successMessage = MutableLiveData<Event<Boolean>>()
    var successMessage = _successMessage

    var dobDate = DefaultDate
    var dobMonth = DefaultMonth
    var dobYear = DefaultYear

    var heightUnitSystem: HeightUnitSystem = HeightUnitSystem.METRIC

    var weightUnitSystem: WeightUnitSystem = WeightUnitSystem.METRIC

    private val _interests = MutableLiveData<List<Interest>>()
    var interests = _interests


    private val _userName = MutableLiveData<String>()
    var userName = _userName

    private val _gender = MutableLiveData<Gender?>(null)
    var gender = _gender

    private val _endGame = MutableLiveData<EndGame?>()
    var endGame = _endGame

    var selectedInterests = ArrayList<Int>()

    private var heightInCm = 0
    private var heightInInches = 0
    var heightInCmList = ArrayList<Int>()
    var heightInInchList = ArrayList<Int>()
    var genderInList = ArrayList<Int>()


    private var weightInKg = 0
    private var weightInLbs = 0
    var weightInKgList = ArrayList<Int>()
    var weightInLbsList = ArrayList<Int>()
    var genderList = ArrayList<String>()
    private var genderListHeight = 0


    private var stepsGoal: Int = 0
    var stepsGoalList = ArrayList<Int>()

    private var calories: Int = 0
    var caloriesList = ArrayList<Int>()

    private var localUser: User? = null
    private var mLocalUserData: LocalUserData? = null
    var isLocalDateSet = false

    var unit = localDataStore.getUnit()
    var unitList = ArrayList<String>()

    fun getPadding(context: Context): Int {
        val rvHeight =
            context.resources.getDimension(com.noisefit_commans.R.dimen.recycler_height_spinner)
        val dpHeight = screenUtils.pxToDp(rvHeight.roundToInt(), context)
        return screenUtils.dpToPx(
            (dpHeight / 2) - (ITEM_HEIGHT / 2), context
        ).roundToInt()
    }

    init {
        heightUnitSystem =
            if (localDataStore.getUnit().name.isEmpty() || (localDataStore.getUnit().name.lowercase() == HeightUnitSystem.METRIC.name.lowercase())) {
                HeightUnitSystem.METRIC
            } else HeightUnitSystem.IMPERIAL
        weightUnitSystem =
            if (localDataStore.getUnit().name.isEmpty() || (localDataStore.getUnit().name.lowercase() == HeightUnitSystem.METRIC.name.lowercase())) {
                WeightUnitSystem.METRIC
            } else WeightUnitSystem.IMPERIAL

        //unit list creation
        unitList.add("Metric (kg)")
        unitList.add("Imperial (lbs)")
        for (i in MinHeightInUnit..MaxHeightInUnit) {
            unitInList.add(i)
        }

        //gender list creation
        genderList.add("Male")
        genderList.add("Woman")
        genderList.add("Non-binary")
        genderList.add("Prefer not to say")
        for (i in MinHeightInGender..MaxHeightInGender) {
            genderInList.add(i)
        }
        for (i in MinHeightInCm..MaxHeightInCm) {
            heightInCmList.add(i)
        }
        for (i in MinHeightInInches..MaxHeightInInches) {
            heightInInchList.add(i)
        }

        for (i in MinWeightInKg..MaxWeightInKg) {
            weightInKgList.add(i)
        }
        for (i in MinWeightInLbs..MaxWeightInLbs) {
            weightInLbsList.add(i)
        }

        for (i in MinStepsGoal..MaxStepsGoal step 1000) {
            stepsGoalList.add(i)
        }
        for (i in MinCaloriesValue..MaxCaloriesValue step 10) {
            caloriesList.add(i)
        }

        heightInCm = DefaultHeightInCm
        heightInInches = DefaultHeightInInches

        weightInKg = DefaultWeightInKg
        weightInLbs = DefaultWeightInLbs

        stepsGoal = DefaultStepsGoal

        localUser = localDataStore.getUser()


        localUser?.userInfo?.let {
            heightInCm = it.height
            weightInKg = it.weight
        }
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
        mLocalUserData = localDataStore.getLocalUserData()

        this.selectedInterests.clear()
        this.selectedInterests.addAll(mLocalUserData?.interests ?: ArrayList())

        mLocalUserData?.height?.let {
            if (mLocalUserData?.heightUnit?.lowercase()
                    .equals(HeightUnitSystem.IMPERIAL.name.lowercase())
            ) {
                heightInInches = it
            } else {
                heightInCm = it
            }
        }
        mLocalUserData?.weight?.let {
            if (mLocalUserData?.weightUnit?.lowercase()
                    .equals(WeightUnitSystem.IMPERIAL.name.lowercase())
            ) {
                weightInLbs = it
            } else {
                weightInKg = it
            }
        }
        mLocalUserData?.heightUnit?.let {
            heightUnitSystem = if ((it.lowercase() == HeightUnitSystem.IMPERIAL.name.lowercase())) {
                HeightUnitSystem.IMPERIAL
            } else HeightUnitSystem.METRIC
        }
        mLocalUserData?.weightUnit?.let {
            weightUnitSystem = if ((it.lowercase() == HeightUnitSystem.IMPERIAL.name.lowercase())) {
                WeightUnitSystem.IMPERIAL
            } else WeightUnitSystem.METRIC
        }


//        //calculate 12 years later date
//        val date = calculateDefaultDateMonthYear()
//        dobDate = date.first
//        dobMonth = date.second
//        dobYear = date.third

    }

    private fun calculateDefaultDateMonthYear(): Triple<Int, Int, Int> {
        val calendar1: Calendar = Calendar.getInstance()
        calendar1.time = Date()
        calendar1.add(Calendar.YEAR, -12)
        calendar1.add(Calendar.DAY_OF_YEAR, -1)
        val newDate: Date = calendar1.time
        val formattedDate: String =
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(newDate)
        val dateArray = formattedDate.split("-")
        val date = dateArray[0].toInt()
        val month = dateArray[1].toInt()
        val year = dateArray[2].toInt()

        return Triple(date, month, year)
    }

    fun resetData() {
        val userData = localDataStore.getUser()
        userData?.userInfo?.let {
            genderListHeight = if (it.gender == "Male") 0
            else if (it.gender == "Female") 1
            else if (it.gender == "Other") 2
            else 3
        }

        userData?.userInfo?.let {
            heightInCm = it.height
            weightInKg = it.weight
        }
    }

    fun getDob(): String {
        val month = dobMonth
        return "$dobYear-${month.toMakeTwoDecimal()}-${dobDate.toMakeTwoDecimal()}"
    }

    fun getSetupDob(): String? {
        if (dobMonth == DefaultMonth && dobYear == DefaultYear && dobDate == DefaultDate) {
            return null
        }
        val month = dobMonth
        return "$dobYear-${month.toMakeTwoDecimal()}-${dobDate.toMakeTwoDecimal()}"
    }

    fun getEndGameList(): ArrayList<EndGame> {
        val endGameList = localDataStore.getEndGameList()
        if (endGame.value != null) {
            endGameList.forEach { eGame ->
                if (eGame.id == endGame.value?.id) {
                    eGame.selectd = true
                }
            }
        }
        return endGameList
    }

    fun setUserName(name: String) {
        _userName.postValue(name)
    }

    fun setEndGame(endGame: EndGame?) {
        _endGame.postValue(endGame)
    }

    fun setEndGameValue(endGameValue: String) {
        localDataStore.setEndGameValue(endGameValue)
    }

    fun setGender(gender: Gender) {
        _gender.postValue(gender)
    }

    fun setDob(year: Int, month: Int, day: Int) {
        dobDate = day
        dobMonth = month + 1
        dobYear = year
    }

    fun getAge(): Int {
        return Period.between(
            LocalDate.of(dobYear, dobMonth, dobDate),
            LocalDate.now()
        ).years
    }

    fun setHeightUnit(heightUnitSystem: HeightUnitSystem) {
        this.heightUnitSystem = heightUnitSystem
    }

    fun setWeightUnit(weightUnitSystem: WeightUnitSystem) {
        this.weightUnitSystem = weightUnitSystem
    }

    fun initialWeightUnit() {
        weightUnitSystem = if (weightUnitSystem == WeightUnitSystem.METRIC) {
            WeightUnitSystem.METRIC
        } else {
            WeightUnitSystem.IMPERIAL
        }
    }

    fun initialHeightUnit() {
        heightUnitSystem = if (heightUnitSystem == HeightUnitSystem.METRIC) {
            HeightUnitSystem.METRIC
        } else {
            HeightUnitSystem.IMPERIAL
        }
    }

    //get gender index
    fun getGenderIndex(): Int {
        return genderInList.indexOf(genderListHeight)
    }

    //get Unit index
    fun getUnitIndex(): Int {
        return unitInList.indexOf(unitListHeight)
    }

    fun updateUnitIndex(index: Int) {
        unitListHeight = getUnit(index)
    }

    private fun getUnit(index: Int): Int {
        return unitInList[index]
    }


    ////
    // height
    ////
    fun getHeightIndex(): Int {
        return if (heightUnitSystem == HeightUnitSystem.METRIC) {
            if (heightInCm == 0) {
                heightInCm = DefaultHeightInCm
            }
            heightInCmList.indexOf(heightInCm)
        } else {
            if (heightInInches == 0) {
                heightInInches = DefaultHeightInInches
            }
            heightInInchList.indexOf(heightInInches)
        }
    }

    fun getHeightList(): ArrayList<Int> {
        return if (heightUnitSystem == HeightUnitSystem.METRIC) {
            heightInCmList
        } else {
            heightInInchList
        }

    }

    fun getDefaultWeightInKg(): Int {
        return DefaultWeightInKg
    }

    fun getDefaultHeightInCm(): Int {
        return DefaultHeightInCm
    }

    fun getHeight(): Int {
        return if (heightUnitSystem == HeightUnitSystem.METRIC) {
            heightInCm
        } else {
            heightInInches
        }
    }

    fun updateHeightIndex(index: Int) {
        if (heightUnitSystem == HeightUnitSystem.METRIC) {
            heightInCm = getHeight(index)
        } else {
            heightInInches = getHeight(index)
        }


    }

    fun updateGenderIndex(index: Int) {
        genderListHeight = getGender(index)
    }

    private fun getGender(index: Int): Int {
        return genderInList[index]

    }

    private fun getHeight(index: Int): Int {
        return if (heightUnitSystem == HeightUnitSystem.METRIC) {
            heightInCmList[index]
        } else {
            heightInInchList[index]
        }
    }

    //////
    // weight
    ////

    fun getWeight(): Int {
        return if (weightUnitSystem == WeightUnitSystem.METRIC) {
            weightInKg
        } else {
            weightInLbs
        }
    }

    fun getWeightList(): ArrayList<Int> {
        return if (weightUnitSystem == WeightUnitSystem.METRIC) {
            weightInKgList
        } else {
            weightInLbsList
        }

    }

    fun getWeightIndex(): Int {
        return if (weightUnitSystem == WeightUnitSystem.METRIC) {
            if (weightInKg == 0) {
                weightInKg = DefaultWeightInKg
            }
            weightInKgList.indexOf(weightInKg)
        } else {
            if (weightInLbs == 0) {
                weightInLbs = DefaultWeightInLbs
            }
            weightInLbsList.indexOf(weightInLbs)
        }

    }

    fun updateWeightIndex(index: Int) {
        if (weightUnitSystem == WeightUnitSystem.METRIC) {
            weightInKg = getWeight(index)
            LOGS.d("updateWeightIndex kg $weightInKg")
        } else {
            weightInLbs = getWeight(index)
            LOGS.d("updateWeightIndex lb $weightInLbs")
        }

    }

    fun getStepsDefaultGoal(): Int {
        return stepsGoal
    }

    fun setStepsDefaultGoal(defaultStepGoal: Int) {
        stepsGoal = defaultStepGoal
    }

    fun setCaloriesDefault(defaultCalories: Int) {
        calories = defaultCalories
    }

    private fun getWeight(index: Int): Int {
        return if (weightUnitSystem == WeightUnitSystem.METRIC) {
            weightInKgList[index]
        } else {
            weightInLbsList[index]
        }
    }

    ////
    // steps
    ////

    fun getStepsIndex(): Int {
        return stepsGoalList.indexOf(stepsGoal)
    }

    fun getCaloriesIndex(): Int {
        return caloriesList.indexOf(calories)
    }

    fun updateStepsIndex(index: Int) {
        stepsGoal = getSteps(index)
    }

    fun updateCaloriesIndex(index: Int) {
        calories = getCalories(index)
    }


    fun getGoalList(): ArrayList<Int> {
        return stepsGoalList
    }

    private fun getSteps(index: Int): Int {
        return stepsGoalList[index]
    }

    private fun getCalories(index: Int): Int {
        return caloriesList[index]
    }


    fun getCaloriesGoal(): Int {
        return (stepsGoal * 0.04).roundToInt()
    }

    fun getCaloriesGoal2(): Int {
        return calories
    }

    fun getDistanceGoal(): Int {
        return if (weightUnitSystem == WeightUnitSystem.METRIC) {
            stepsGoal
        } else {
            (1.609 * stepsGoal).toInt()
        }


    }

    private fun createUserUpdateRequest(): JsonObject {


//        val caloriesGoal = getCaloriesGoal()
        val caloriesGoal = getCaloriesGoal2()
        val distanceGoal = getDistanceGoal()

        val dob = getDob()


        val heightValue = if (heightUnitSystem.type == HeightUnitSystem.IMPERIAL.type) {
            DistanceUtil.convertInchToCms(
                heightInInches.toDouble().roundToInt()
            ).toDouble().roundToInt()
        } else {
            heightInCm.toDouble().roundToInt()
        }
        val weightValue = if (weightUnitSystem.type == WeightUnitSystem.IMPERIAL.type) {
            DistanceUtil.convertLbsToKg(
                weightInLbs.toDouble().roundToInt()
            ).toDouble().roundToInt()
        } else {
            weightInKg.toDouble().roundToInt()
        }

        /*val uInfo = UserInfo(weight, height, 0, dob, gender.value!!.type, 70)
        user.userInfo = uInfo
        localDataStore.saveUserInfo(user)*/

        val userObject = JsonObject().apply {
            addProperty("first_name", userName.value)
            addProperty("end_game", endGame.value?.id ?: -1)
        }


        val userGoals = JsonObject()
        userGoals.apply {
            addProperty("sleep_goals", DefaultSleepGoal)
            addProperty("step_goals", stepsGoal)
            addProperty("calories_goals", caloriesGoal)
            addProperty("distance_goals", distanceGoal)
            addProperty("unit_system", weightUnitSystem.type)
        }
        userObject.add("goal", userGoals)

        val userInfo = JsonObject()
        userInfo.apply {
            addProperty("weight", weightValue)
            addProperty("height", heightValue)
            addProperty("dob", dob)
            addProperty("gender", gender.value?.type ?: Gender.MALE.type)
            addProperty("step_length", 70)
        }
        userObject.add("info", userInfo)

        userObject.add("interest_id", JsonArray().apply {
            selectedInterests.forEach { id ->
                this.add(id)
            }
        })

        return userObject


    }

    fun updateUserProfile() {

        viewModelScope.launch {
            userRepository.updateUserProfile(createUserUpdateRequest()).collect { resource ->
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

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            localDataStore.setLocalUserData(null)
                            logProfileEvent(it)
                            localDataStore.saveUserInfo(it)
                            _successMessage.postValue(Event(true))
                        }/* ?: getConfig()*/
                    }
                }
            }
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

    private fun logProfileEvent(user: User) {
        val userInfo = user.userInfo
        val userGoals = user.userGoals
        sessionManager.logInsiderAppEvent(InsiderAppEvents.ACCOUNT_SET_UP_SUCCESS)
        val gender: String = if (userInfo?.gender?.lowercase() == Gender.MALE.name.lowercase()) {
            "Male"
        } else if (userInfo?.gender?.lowercase() == Gender.FEMALE.name.lowercase()) {
            "Female"
        } else {
            "Other"
        }
        sessionManager.addUserAttributeToInsider(true,
            HashMap<String, Any>().apply
            {
                this["name"] = user.firstName ?: ""
                this["gender"] = gender
                this["age"] = userInfo?.age ?: 0
                this["dob"] = userInfo?.dob.toString()
                this["step_goal"] = userGoals?.stepGoal ?: 0
                this["sleep_goal"] = userGoals?.sleepGoal ?: 8
                this["distance_goal"] = userGoals?.distanceGoal ?: 0
                this["calories_goal"] = userGoals?.caloriesGoal ?: 0
                this["unit_type"] = userGoals?.unitSystem ?: 0
                this["height"] = userInfo?.height ?: 0
                this["weight"] = userInfo?.weight ?: 0
                this["personality_type"] = getEndGameValue(user.endGame)
                val connectedDeviceData = localDataStore.getConnectedDevice()
                try {
                    if (connectedDeviceData != null) {
                        val arr = arrayOf(connectedDeviceData.bluetoothName)
                        this["pair_device_watchname"] =
                            connectedDeviceData.bluetoothName ?: ""

                        this["pair_device_mac_address"] =
                            connectedDeviceData.address ?: ""
                        this["pair_device_firmware_number"] = ""
                        this["paired_devices_list"] = arr
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })


    }

    fun isDevicePaired(): Boolean {
        return localDataStore.getConnectedDevice() != null
    }


    fun getLocalUserFirstName(): String {
        if (mLocalUserData != null) {
            return mLocalUserData?.name ?: ""
        }
        return localUser?.firstName ?: ""
    }

    fun getLocalUserEndGame(): Int {
        if (mLocalUserData != null) {
            return mLocalUserData?.endGame?.toIntOrNull() ?: -1
        }
        return localUser?.endGame?.toIntOrNull() ?: -1
    }

    fun getLocalUserDob(): String {
        if (mLocalUserData != null) {
            return mLocalUserData?.dob ?: ""
        }
        return localUser?.userInfo?.dob ?: ""
    }

    fun getLocalUserGender(): String {
        if (mLocalUserData != null) {
            return mLocalUserData?.gender ?: ""
        }
        return localUser?.userInfo?.gender ?: ""
    }

    fun saveUserInfoLocally() {
        val localUserData = if (mLocalUserData == null) {
            LocalUserData()
        } else {
            mLocalUserData
        }
        localUserData?.name = userName.value
        endGame.value?.let {
            localUserData?.endGame = it.id.toString()
        }
        getSetupDob()?.let {
            localUserData?.dob = it
        }
        if (getHeight() != 0) {
            localUserData?.height = getHeight()
        }
        if (getWeight() != 0) {
            localUserData?.weight = getWeight()
        }
        localUserData?.stepsGoal = stepsGoal
        gender.value?.let {
            localUserData?.gender = it.type
        }

        localUserData?.heightUnit = heightUnitSystem.type
        localUserData?.weightUnit = weightUnitSystem.type

        localUserData?.interests = selectedInterests


        localDataStore.setLocalUserData(localUserData)


        mLocalUserData = localUserData
    }

    fun getInterestList() {
        viewModelScope.launch {
            userRepository.getInterests().collect { resource ->
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
                                        getInterestList()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _interests.postValue(it)
                        }
                    }
                }
            }
        }

    }

    fun updateInterest(selectedInterests: List<Int>) {
        this.selectedInterests.clear()
        this.selectedInterests.addAll(selectedInterests)
    }

    fun getHeightDataForPicker(): ArrayList<WheelItem<String>> {
        val dataSet = ArrayList<WheelItem<String>>()
        if (heightUnitSystem == HeightUnitSystem.METRIC) {
            heightInCmList.forEach {
                dataSet.add(WheelItem("$it cm"))
            }
        } else {
            heightInInchList.forEach {
                dataSet.add(WheelItem("$it in"))
            }
        }
        return dataSet

    }

    fun getWeightDataForPicker(): ArrayList<WheelItem<String>> {
        val dataSet = ArrayList<WheelItem<String>>()
        if (weightUnitSystem == WeightUnitSystem.METRIC) {
            weightInKgList.forEach {
                dataSet.add(WheelItem("$it kg"))
            }
        } else {
            weightInLbsList.forEach {
                dataSet.add(WheelItem("$it lbs"))
            }
        }
        return dataSet

    }

    fun getGoalsDataForPicker(): ArrayList<WheelItem<String>> {
        val dataSet = ArrayList<WheelItem<String>>()
        stepsGoalList.forEach {
            dataSet.add(WheelItem("$it steps"))
        }

        return dataSet

    }

    fun getCaloriesDataForPicker(): ArrayList<WheelItem<String>> {
        val dataSet = ArrayList<WheelItem<String>>()
        caloriesList.forEach {
            dataSet.add(WheelItem("$it kcal"))
        }

        return dataSet

    }


}