package com.oreo.ui.googlefit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.noisefit.data.dataConverter.OfflineDataMapper
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.AuthenticationRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit.ui.onboarding.onboardProfile.DefaultSleepGoal
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.GoogleFitDataDb
import com.noisefit_commans.data.model.User
import com.noisefit_commans.models.BodyMeasurementGoogleFit
import com.noisefit_commans.models.BodyMeasurementValue
import com.noisefit_commans.models.SleepDataGoogleFit
import com.noisefit_commans.models.WorkoutGoogleFit
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.abstaction.GoogleFitDataSource
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.model.GoogleFitDataDisplayModel
import com.oreo.data.model.GoogleFitDataType
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.roundToInt

private const val TAG = "GoogleFitDataViewModel"

@HiltViewModel
class GoogleFitDataViewModel
@Inject
constructor(
    private val googleFitDataSource: GoogleFitDataSource,
    private val userActivityRepository: OreoUserActivityRepository,
    private val userRepository: UserRepository,
    private val offlineDataMapper: OfflineDataMapper,
    private val localDataSource: DataStoredInterface,
    val userHealthDataDataSource: OreoUserHealthDataDataSource,
    val sessionManager: SessionManager,
    private val authenticationRepository: AuthenticationRepository,
) : BaseViewModel() {
    val success = ArrayList<GoogleFitDataDisplayModel>()
    val fail = ArrayList<GoogleFitDataDisplayModel>()
    var unSyncedDataList = MutableLiveData<List<GoogleFitDataDisplayModel>>()
        private set

    var dataSyncingComplete = MutableLiveData<Event<Boolean>>()
        private set

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)

            //remove old data
            googleFitDataSource.deleteData()

            //compare overlapping data

            val data = googleFitDataSource.getUnSyncedData()
            val filteredData = ArrayList<GoogleFitDataDb>()

            val userDataTimeStamps = getUserWorkoutAndSleepTimestamps()

            data.forEach { localData ->
                val overlap =
                    checkDataOverlap(localData.startTime, localData.endTime, userDataTimeStamps)
                if (overlap.not()) {
                    filteredData.add(localData)
                } else {
                    googleFitDataSource.markDataSynced(
                        localData.id,
                        convertGoogleFitType(localData.type)
                    )
                }
            }
            //load date
            unSyncedDataList.postValue(convertData(filteredData))
            setLoading(false)
        }

    }

    private fun convertGoogleFitType(type: String?): GoogleFitDataType {
        return when (type) {
            "sleep" -> GoogleFitDataType.SLEEP
            "workout" -> GoogleFitDataType.WORKOUT
            "height" -> GoogleFitDataType.BODY_MEASUREMENTS
            "weight" -> GoogleFitDataType.BODY_MEASUREMENTS
            else -> GoogleFitDataType.SLEEP
        }
    }

    private fun checkDataOverlap(
        startTime: Long,
        endTime: Long,
        compareData: List<Pair<Long, Long>>,
    ): Boolean {
        var dataOverlap = false
        compareData.forEach {
            val isOverlapping =
                DateFormats.checkIfTimeOverlap(startTime, endTime, it.first, it.second)
            if (dataOverlap.not() && isOverlapping) {
                dataOverlap = true
            }
            LOGS.d("DATA_OVERLAP $isOverlapping   -$startTime - $endTime  | ${it.first} - ${it.second}")

        }
        return dataOverlap
    }

    private suspend fun getUserWorkoutAndSleepTimestamps(): List<Pair<Long, Long>> {

        val timestamps = ArrayList<Pair<Long, Long>>()

        val zoneOffset = ZoneId.systemDefault().rules.getOffset(LocalDateTime.now())

        (1..3).forEach {
            val date = DateFormats.getCurrentDateMinusDays(it - 1)

            val userData = userHealthDataDataSource.getDataByDate(date)

            val parsedData = Gson().fromJson<ServerUserHealthData>(
                userData?.userHealthData ?: ""
            )

            parsedData.activity?.workout?.forEach {
                val startTime = "${it.date} ${it.startTime}"
                val endTime = "${it.date} ${it.endTime}"

                val start = LocalDateTime.parse(
                    startTime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ).toEpochSecond(zoneOffset)

                LOGS.d("dsfjhskdjfhf $start    -$startTime - $endTime")

                timestamps.add(


                    Pair(
                        LocalDateTime.parse(
                            startTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        ).toEpochSecond(zoneOffset),
                        LocalDateTime.parse(
                            endTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        )
                            .toEpochSecond(zoneOffset),
                    )
                )
            }


            parsedData.sleep?.sleeps?.forEach {
                timestamps.add(
                    Pair(
                        LocalDateTime.parse(
                            it.startTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        ).toEpochSecond(zoneOffset),
                        LocalDateTime.parse(
                            it.endTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        )
                            .toEpochSecond(zoneOffset),
                    )
                )
            }

            parsedData.sleep?.naps?.forEach {
                timestamps.add(
                    Pair(
                        LocalDateTime.parse(
                            it.startTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        ).toEpochSecond(zoneOffset),
                        LocalDateTime.parse(
                            it.endTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        )
                            .toEpochSecond(zoneOffset),
                    )
                )
            }
        }

        return timestamps
    }


    private fun convertData(data: List<GoogleFitDataDb>): List<GoogleFitDataDisplayModel> {

        val result = ArrayList<GoogleFitDataDisplayModel>()


        val userInfo = localDataSource.getUser()?.userInfo

        val measurementObj = BodyMeasurementGoogleFit(
            userTimeStamp = localDataSource.getAppBodyMeasurementsTimeStamp(),
            userHeight = (userInfo?.height ?: 0),
            userWeight = (userInfo?.weight ?: 0)
        )

        data.forEach {

            //workout, sleep, height, weight, body_fat

            val type = if (it.type.equals("workout", true)) {
                GoogleFitDataType.WORKOUT
            } else if (it.type.equals("sleep", true)) {
                val duration = it.endTime - it.startTime
                LOGS.d("dfkhdf $duration")
                if (duration <= (3 * 60 * 60)) {
                    GoogleFitDataType.NAP
                } else {
                    GoogleFitDataType.SLEEP
                }
            } else if (it.type.equals(
                    com.oreo.data.db.implementation.GoogleFitDataType.HEIGHT.name.lowercase(),
                    true
                )
            ) {
                measurementObj.gFitHeight = Gson().fromJson<BodyMeasurementValue>(it.data ?: "")
                null
            } else if (it.type.equals(
                    com.oreo.data.db.implementation.GoogleFitDataType.WEIGHT.name.lowercase(),
                    true
                )
            ) {
                measurementObj.gFitWeight = Gson().fromJson<BodyMeasurementValue>(it.data ?: "")
                null
            } else {
                null
            }

            if (type != null) {
                /*val data = if(type==GoogleFitDataType.WORKOUT){
                    Gson().fromJson<WorkoutGoogleFit>(it.data?:"")
                }else{
                    Gson().fromJson<SleepDataGoogleFit>(it.data?:"")
                }*/
                result.add(
                    GoogleFitDataDisplayModel(
                        id = it.id,
                        type = type,
                        startTime = it.startTime,
                        endTime = it.endTime,
                        duration = it.endTime - it.startTime,
                        rawData = it.data ?: "",
                    )
                )
            }
        }

        if (measurementObj.gFitHeight != null || measurementObj.gFitWeight != null) {
            result.add(
                GoogleFitDataDisplayModel(
                    id = 0,
                    type = GoogleFitDataType.BODY_MEASUREMENTS,
                    startTime = 0,
                    endTime = 0,
                    duration = 0,
                    rawData = Gson().toJson(measurementObj),
                )
            )
        }

        return result
    }

    fun getSyncedMessage(): String {
        var hasSleep = false
        var hasWorkout = false
        var hasNap = false
        var hasBodyMeasurement = false

        success.forEach {
            when (it.type) {
                GoogleFitDataType.SLEEP -> {
                    hasSleep = true
                }

                GoogleFitDataType.NAP -> {
                    hasNap = true
                }

                GoogleFitDataType.WORKOUT -> {
                    hasWorkout = true
                }

                GoogleFitDataType.BODY_MEASUREMENTS -> {
                    hasBodyMeasurement = true
                }
            }
        }


        return "Great! Your have successfully synced {message pending}"

    }


    fun sendDataToServer(selectedItems: List<GoogleFitDataDisplayModel>) {
        setLoading(true)
        viewModelScope.launch {

            success.clear()
            fail.clear()

            LOGS.d("sendDataToServer API start")
            selectedItems.map { googleFitDataDisplayModel ->
                LOGS.d("sendDataToServer API hit ")

                when (googleFitDataDisplayModel.type) {
                    GoogleFitDataType.NAP -> {

                        val data = offlineDataMapper.convertGFManualNap(googleFitDataDisplayModel)
                        userActivityRepository.addManualNap(
                            data.first, data.second
                        ).collect { resource ->
                            when (resource) {
                                is Resource.GenericError -> {
                                    fail.add(googleFitDataDisplayModel)
                                    LOGS.d("sendDataToServer API GenericError")
                                }

                                is Resource.Loading -> {
                                    setLoading(resource.loading)

                                }

                                is Resource.NetworkError -> {
                                    fail.add(googleFitDataDisplayModel)
                                    LOGS.d("sendDataToServer API NetworkError ")
                                }

                                is Resource.Success -> {
                                    resource.data?.data?.let {
                                        LOGS.d("sendDataToServer API success $it")
                                        success.add(googleFitDataDisplayModel)
                                    }
                                }
                            }
                        }
                    }

                    GoogleFitDataType.SLEEP -> {
                        val data = offlineDataMapper.convertGFManualSleep(
                            Gson().fromJson<SleepDataGoogleFit>(
                                googleFitDataDisplayModel.rawData ?: ""
                            )
                        )
                        userActivityRepository.addManualSleep(data.first, data.second)
                            .collect { resource1 ->
                                when (resource1) {
                                    is Resource.GenericError -> {
                                        fail.add(googleFitDataDisplayModel)
                                        LOGS.d("$TAG workout session api error")
                                    }

                                    is Resource.Loading -> {

                                    }

                                    is Resource.NetworkError -> {
                                        fail.add(googleFitDataDisplayModel)
                                    }

                                    is Resource.Success -> {
                                        success.add(googleFitDataDisplayModel)
                                        LOGS.d("$TAG workout session api success")

                                    }
                                }

                            }
                    }

                    GoogleFitDataType.WORKOUT -> {
                        userActivityRepository.addGFitWorkout(
                            offlineDataMapper.convert1GFWorkoutIntoJsonArray(
                                Gson().fromJson<WorkoutGoogleFit>(
                                    googleFitDataDisplayModel.rawData ?: ""
                                )
                            )
                        ).collect { resource1 ->
                            when (resource1) {
                                is Resource.GenericError -> {
                                    fail.add(googleFitDataDisplayModel)
                                    LOGS.d("$TAG workout session api error")
                                }

                                is Resource.Loading -> {

                                }

                                is Resource.NetworkError -> {
                                    fail.add(googleFitDataDisplayModel)
                                }

                                is Resource.Success -> {
                                    success.add(googleFitDataDisplayModel)
                                    LOGS.d("$TAG workout session api success")

                                }
                            }

                        }
                    }

                    GoogleFitDataType.BODY_MEASUREMENTS -> {

                        val parsedData = Gson().fromJson<BodyMeasurementGoogleFit>(
                            googleFitDataDisplayModel.rawData ?: ""
                        )
                        val height = parsedData.gFitHeight?.value?.roundToInt()
                        val weight = parsedData.gFitWeight?.value?.roundToInt()

                        val request =
                            createUserUpdateRequest(localDataSource.getUser(), height, weight)
                                ?: return@launch

                        userRepository.updateUserProfile(request).collect { resource ->
                            when (resource) {
                                is Resource.GenericError -> {
                                    fail.add(googleFitDataDisplayModel)
                                }

                                is Resource.Loading -> {

                                }

                                is Resource.NetworkError -> {
                                    fail.add(googleFitDataDisplayModel)
                                }

                                is Resource.Success -> {
                                    resource.data?.data?.let {
                                        authenticationRepository.saveUserInfo(it)
                                        success.add(googleFitDataDisplayModel)
                                    }
                                }
                            }
                        }
                    }
                }


                LOGS.d("sendDataToServer API response ")
            }

            viewModelScope.launch(Dispatchers.IO) {
                val dates = HashSet<String>()
                success.forEach {
                    googleFitDataSource.markDataSynced(it.id, it.type)

                    val instant = Instant.ofEpochSecond(it.startTime)
                    val start = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                    dates.add(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                }

                userHealthDataDataSource.clearDataByDates(dates.toList())
                sessionManager.reloadTodayData.postValue(
                    Event(true)
                )
                sessionManager.forceSyncData.postValue(Event(true))

                kotlinx.coroutines.delay(1000L)
                setLoading(false)
                dataSyncingComplete.postValue(Event(true))
            }
            LOGS.d("sendDataToServer API response end")
        }
    }

    private fun createUserUpdateRequest(user: User?, height: Int?, weight: Int?): JsonObject? {
        if (user == null) return null

        val userObject = JsonObject().apply {
            addProperty("first_name", user.firstName)
            addProperty("image_url", user.imageUrl)
            addProperty("notifications_enabled_luna", user.notificationsEnabledLuna)
        }

        var userInfo: JsonObject? = null
        try {
            userInfo = JsonObject()

            userInfo.apply {
                addProperty("weight", weight ?: user.userInfo?.weight)
                addProperty("height", height ?: user.userInfo?.height)
                addProperty("dob", user.userInfo?.dob)
                addProperty("gender", user.userInfo?.gender)
                addProperty("step_length", 70)
            }
            userObject.add("info", userInfo)

            val userGoals = JsonObject()
            userGoals.apply {
                addProperty("sleep_goals", user.userGoals?.sleepGoal ?: DefaultSleepGoal)
                addProperty("step_goals", user.userGoals?.stepGoal)
                addProperty("calories_goals", user.userGoals?.caloriesGoal)
                addProperty("distance_goals", user.userGoals?.distanceGoal)
                addProperty("unit_system", user.userGoals?.unitSystem)
                addProperty("unit_system_luna", user.userGoals?.unitSystemLuna)
            }

            userObject.add("goal", userGoals)

        } catch (exp: Exception) {
            LOGS.d("User Info null")
            userInfo = null
        }
        return userObject

    }


}
