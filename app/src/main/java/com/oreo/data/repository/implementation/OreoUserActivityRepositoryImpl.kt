package com.oreo.data.repository.implementation

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.safeApiCallFlow
import com.noisefit.luna.BuildConfig
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.BaseApiResponseData
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.dataConverter.OreoOfflineDataMapper
import com.oreo.data.db.implementation.*
import com.oreo.data.model.*
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.data.model.health.OreoDashboardResponseModel
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.data.model.health.OreoSleepModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.receiver.workManager.HealthOverviewDataType
import com.oreo.ui.DataType
import com.oreo.ui.TestUserData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow


private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

class OreoUserActivityRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val gson: Gson,
    private val localDatSource: DataStoredInterface,
    private val heartRateDataImpl: OreoHeartRateDataImpl,
    private val hrv: OreoStressDataImpl,
    private val bloodOxygenDataImpl: OreoBloodOxygenDataImpl,
    private val respiratoryDataImpl: OreoRespiratoryDataImpl,
    private val temperatureDataImpl: OreoBodyTemperatureDataImpl,
    private val sleepDataImpl: OreoSleepDataImpl,
    private val stepsDataImpl: OreoStepsDataImpl,
    private val oreoAutoSportDataImpl: OreoAutoSportDataImpl,
    private val offlineDataMapper: OreoOfflineDataMapper,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : OreoUserActivityRepository {


    override suspend fun getSleepHistory(date: String): Flow<Resource<BaseApiResponse<List<OreoSleepModel>>>> {

        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/sleep/v1/get"
            remoteDataSource.getSleepHistory(url, date)
        }
    }

    override suspend fun getReadinessHistory(date: String): Flow<Resource<BaseApiResponse<List<OreoReadinessModel>>>> {

        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/protean/v1/readiness/get"
            remoteDataSource.getReadinessHistory(url, date)
        }
    }

    override suspend fun getDashboardData(): Flow<Resource<BaseApiResponse<OreoDashboardResponseModel>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/protean/v1/dashboard"
            remoteDataSource.getDashboardData(url)
        }
    }


    override suspend fun getActivityHistory(date: String): Flow<Resource<BaseApiResponse<List<OreoActivityModel>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/activity/v1/get"
            remoteDataSource.getActivityHistory(url, date)
        }
    }

    override suspend fun getHealthOverview(
        healthOverviewDataType: HealthOverviewDataType,
        healthOverviewData: ArrayList<OHealthOverview>?
    ): Pair<ArrayList<OHealthOverview>?, Int?> {
        var hOverviewData = healthOverviewData
        if (hOverviewData == null) {
            hOverviewData = ArrayList()
        }

        var index: Int? = null

        val todayDate = DateFormats.getTodaysDateString(7)
//        val user = localDatSource.getUser()

        //val healthOverviewList = ArrayList<HealthOverview>()

        when (healthOverviewDataType) {
            HealthOverviewDataType.ALL -> {

            }

            HealthOverviewDataType.STEPS -> {

//                val stepsData = stepsDataImpl.getTodayData(todayDate)
//
//                val hrOverview = offlineDataMapper.convertHealthOverviewData(
//                    stepsData,
//                    user?.userGoals,
//                    dataUnitConverter
//                )
//                hOverviewData.apply {
//                    stand = hrOverview.stand
//                    standGoal = hrOverview.standGoal
//                    calories = hrOverview.calories
//                    caloriesGoal = hrOverview.caloriesGoal
//                    distance = hrOverview.distance
//                    distanceGoal = hrOverview.distanceGoal
//                    activeMinute = hrOverview.activeMinute
//                    activeMinuteGoal = hrOverview.activeMinuteGoal
//                    stepsGoal = hrOverview.stepsGoal
//                    steps = hrOverview.steps
//                    stepsGoalProgress = hrOverview.stepsGoalProgress
//                    caloriesGoalProgress = hrOverview.caloriesGoalProgress
//                    distanceGoalProgress = hrOverview.distanceGoalProgress
//
//                }
//
//                index = hOverviewData.healthOverviewList?.indexOfFirst {
//                    it is HealthOverview.Steps
//                }
//
//                val distanceIndex = hOverviewData.healthOverviewList?.indexOfFirst {
//                    it is HealthOverview.Distance
//                }
//
//                val caloriesIndex = hOverviewData.healthOverviewList?.indexOfFirst {
//                    it is HealthOverview.Calories
//                }
//                if (index != -1) {
//                    hOverviewData.healthOverviewList!![index!!] =
//                        offlineDataMapper.convertStepsOverviewData(
//                            stepsData,
//                            user?.userGoals
//                        )
//                }
//
//                if (distanceIndex != -1) {
//                    hOverviewData.healthOverviewList!![distanceIndex!!] =
//                        offlineDataMapper.convertDistanceOverviewData(
//                            stepsData,
//                            user?.userGoals,
//                            dataUnitConverter
//                        )
//                }
//
//                if (caloriesIndex != -1) {
//                    hOverviewData.healthOverviewList!![caloriesIndex!!] =
//                        offlineDataMapper.convertCaloriesOverviewData(
//                            stepsData
//                        )
//                }

            }

            HealthOverviewDataType.STRESS -> {
//                index = hOverviewData.indexOfFirst {
//                    it is OHealthOverview.Activity
//                }
//                if (index != -1) {
//                    hOverviewData.healthOverviewList!![index!!] =
//                        offlineDataMapper.convertStressOverviewData(
//                            stressDataImpl.getTodayData(
//                                todayDate
//                            )
//                        )
//                }
            }

            HealthOverviewDataType.SLEEP -> {

                index = hOverviewData.indexOfFirst {
                    it is OHealthOverview.Sleep
                }
                if (index != -1) {
//                    hOverviewData[index] =
//                        offlineDataMapper.convertSleepOverviewData(
//                            sleepDataImpl.getTodayData(todayDate)
//                        )
                }


            }

            HealthOverviewDataType.HEART -> {

                index = hOverviewData.indexOfFirst {
                    it is OHealthOverview.HeartRate
                }
                if (index != -1) {

                    val data = offlineDataMapper.convertHeartRateOverviewData(
                        heartRateDataImpl.getTodayData(
                            todayDate
                        )
                    )
                    if (data != null) {
                        hOverviewData[index] = data
                    }

                }


            }

            HealthOverviewDataType.BLOOD -> {

//                index = hOverviewData.healthOverviewList?.indexOfFirst {
//                    it is HealthOverview.BloodOxygen
//                }
//                if (index != -1) {
//                    hOverviewData.healthOverviewList!![index!!] =
//                        offlineDataMapper.convertBloodOxygenOverviewData(
//                            bloodOxygenDataImpl.getTodayData(
//                                todayDate
//                            )
//                        )
//                }


            }

            HealthOverviewDataType.TEMPERATURE -> {

//                index = hOverviewData.healthOverviewList?.indexOfFirst {
//                    it is HealthOverview.BodyTemp
//                }
//                if (index != -1) {
//                    hOverviewData.healthOverviewList!![index!!] =
//                        offlineDataMapper.convertBodyTempOverviewData(
//                            bodyTemperatureDataImpl.getTodayData(
//                                todayDate
//                            ),
//                            dataUnitConverter,
//                            localDatSource.getBodyTempUnit()
//                        )
//                }

            }

            HealthOverviewDataType.ACTIVITY -> {}
            HealthOverviewDataType.SERVER_SYNC_SUCCESS -> {}
            HealthOverviewDataType.AUTO_WORKOUT -> {}
        }

        return Pair(hOverviewData, index)
    }

    override suspend fun getTestData(): List<TestUserData> {
        val response = ArrayList<TestUserData>()

        val hrData = heartRateDataImpl.getUnSyncServerData(1L, false)
        val hrvData = hrv.getUnSyncServerData(1L, false)
        val boData = bloodOxygenDataImpl.getUnSyncServerData(1L, false)
        val respData = respiratoryDataImpl.getUnSyncServerData(1L, false)
        val tempData = temperatureDataImpl.getUnSyncServerData(1L, false)
        val sleepData = sleepDataImpl.getUnSyncServerData(1L, false)
        val activityData = stepsDataImpl.getUnSyncServerData(1L, false)

        activityData?.forEach {
            response.add(TestUserData(DataType.ACTIVITY, null, it.date ?: ""))
        }
        sleepData?.forEach {
            response.add(TestUserData(DataType.SLEEP, null, it.date ?: ""))
        }
        hrData?.forEach {
            response.add(TestUserData(DataType.HEART_RATE, null, it.date ?: ""))
        }
        hrvData?.forEach {
            response.add(TestUserData(DataType.HRV, null, it.date ?: ""))
        }
        boData?.forEach {
            response.add(TestUserData(DataType.BLOOD_OXYGEN, null, it.date ?: ""))
        }
        respData?.forEach {
            response.add(TestUserData(DataType.RESPIRATORY, null, it.date ?: ""))
        }
        tempData?.forEach {
            response.add(TestUserData(DataType.TEMP, null, it.date ?: ""))
        }
        return response
    }

    override suspend fun getTestDataListByType(data: TestUserData): List<Any> {
        val response = ArrayList<Any>()


        when (data.type) {
            DataType.HEART_RATE -> {
                val hrData = heartRateDataImpl.getUnSyncServerData(1L, false)?.filter {
                    it.date == data.data
                }

                hrData?.forEach {
                    val breakup = Gson().fromJson<List<Int>>(it.breakUp ?: "")
                    breakup?.forEachIndexed { index, breakupData ->
                        response.add(
                            TestUserData(
                                DataType.HEART_RATE,
                                time = positionToTime(index),
                                "$breakupData bpm"
                            )
                        )
                    }
                }
            }

            DataType.HRV -> {
                val hrData = hrv.getUnSyncServerData(1L, false)?.filter {
                    it.date == data.data
                }

                hrData?.forEach {
                    val breakup = Gson().fromJson<List<Int>>(it.breakUp ?: "")
                    breakup?.forEachIndexed { index, breakupData ->
                        response.add(
                            TestUserData(
                                DataType.HRV,
                                time = positionToTime(index),
                                "$breakupData ms"
                            )
                        )
                    }
                }
            }

            DataType.BLOOD_OXYGEN -> {
                val hrData = bloodOxygenDataImpl.getUnSyncServerData(1L, false)?.filter {
                    it.date == data.data
                }

                hrData?.forEach {
                    val breakup = Gson().fromJson<List<Int>>(it.breakUp ?: "")
                    breakup?.forEachIndexed { index, breakupData ->
                        response.add(
                            TestUserData(
                                DataType.BLOOD_OXYGEN,
                                time = positionToTime(index, 15),
                                breakupData.toString()
                            )
                        )
                    }
                }

            }

            DataType.RESPIRATORY -> {
                val hrData = respiratoryDataImpl.getUnSyncServerData(1L, false)?.filter {
                    it.date == data.data
                }

                hrData?.forEach {
                    val breakup = Gson().fromJson<List<Int>>(it.breakUp ?: "")
                    breakup?.forEachIndexed { index, breakupData ->
                        response.add(
                            TestUserData(
                                DataType.BLOOD_OXYGEN,
                                time = positionToTime(index),
                                "$breakupData"
                            )
                        )
                    }
                }
            }

            DataType.TEMP -> {
                val hrData = temperatureDataImpl.getUnSyncServerData(1L, false)?.filter {
                    it.date == data.data
                }

                hrData?.forEach {
                    val breakup = Gson().fromJson<List<Float>>(it.breakUp ?: "")
                    breakup?.forEachIndexed { index, breakupData ->
                        response.add(
                            TestUserData(
                                DataType.TEMP,
                                time = positionToTime(index),
                                "$breakupData °F"
                            )
                        )
                    }
                }
            }

            DataType.SLEEP -> {
                val sleepData = sleepDataImpl.getUnSyncServerData(1L, false)?.filter {
                    it.date == data.data
                }?.firstOrNull()

                if (sleepData != null) {
                    response.add(sleepData)
                }
            }

            DataType.ACTIVITY -> {
                val stepsData = stepsDataImpl.getUnSyncServerData(1L, false)?.filter {
                    it.date == data.data
                }?.firstOrNull()

                if (stepsData != null) {
                    response.add(stepsData)
                }

            }

        }

        return response

    }

    /**
     * return time in 12 hour format
     */
    fun positionToTime(pos: Int, multiplier: Int = 5): String {
        val totalMinutes = (pos + 1) * multiplier

        val hours: Int = totalMinutes / 60
        val minutes: Int = totalMinutes % 60

        return String.format("%d:%02d", hours, minutes)

    }

    override suspend fun getSummaryAutoWorkoutCount(): Int {
        return oreoAutoSportDataImpl.getAllNotAcceptingData()?.size ?: 0
    }

    override suspend fun getSummaryHRHealthOverview(): OHealthOverview? {
        try {
            val todayDate = DateFormats.getTodaysDateString(10)
            return offlineDataMapper.convertHeartRateOverviewData(
                heartRateDataImpl.getTodayData(
                    todayDate
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override suspend fun addWorkout(request: JsonObject): Flow<Resource<BaseApiResponseData<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/activity/v1/add_workout"
            remoteDataSource.addWorkout(url, request)
        }
    }

    override suspend fun getWorkoutList(): Flow<Resource<BaseApiResponse<List<OWorkoutListModal>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/activity/v1/workout_list"
            remoteDataSource.getWorkoutList(url)
        }
    }

    override suspend fun getRecentWorkoutList(isToday: Boolean): Flow<Resource<BaseApiResponse<List<OActivityListModal>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/activity/v1/recent/workout"
            remoteDataSource.getRecentWorkoutList(url, isToday)
        }
    }

    override suspend fun getInternalPagesData(
        selectDate: String,
        dayType: String,
        contriType: String
    ): Flow<Resource<BaseApiResponse<OInternalPageResponseModal>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.OREO_BASE_URL}/sleep/v1/sleep-contributors"
            remoteDataSource.getInternalPagesData(url, selectDate, dayType, contriType)
        }
    }

    override suspend fun getActivityInternalPagesData(
        selectDate: String,
        dayType: String,
        contriType: String
    ): Flow<Resource<BaseApiResponse<OInternalPageResponseModal>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.OREO_BASE_URL}/activity/v1/activity-contributors"
            remoteDataSource.getActivityInternalPagesData(url, selectDate, dayType, contriType)
        }
    }

    override suspend fun getReadinessInternalPagesData(
        selectDate: String,
        dayType: String,
        contriType: String
    ): Flow<Resource<BaseApiResponse<OInternalPageResponseModal>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.OREO_BASE_URL}/sleep/v1/readiness-contributors"
            remoteDataSource.getReadinessInternalPagesData(url, selectDate, dayType, contriType)
        }
    }

    override suspend fun deleteWorkoutFromServer(id: String): Flow<Resource<BaseApiResponse<Any>>> {
        //'https://stage-oreo.gonoise.com/activity/v1/delete_workout/e900fe86-f2d5-422f-be40-9d0d633caa19
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/activity/v1/delete_workout/$id"
            remoteDataSource.deleteWorkout(url)
        }
    }

    override suspend fun getAllActivityList(
        page: Int,
        pageLimit: Int
    ): Flow<Resource<BaseApiResponse<List<OActivityListModal>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/activity/v1/user_workout"
            remoteDataSource.getAllActivityList(url)
        }
    }

    override suspend fun getContributorDetailsInfo(contributorType: String): Flow<Resource<BaseApiResponse<OContributorResponseModal>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/protean/v1/details"
            remoteDataSource.getContributorsDetails(url, contributorType)
        }
    }

    override suspend fun getWorkoutDetails(id: String): Flow<Resource<BaseApiResponse<OWorkoutDetailsResponseModel>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/activity/v1/workout_detail/${id}"
            remoteDataSource.getWorkoutDetails(url)
        }
    }

    override suspend fun getHSCategories(): Flow<Resource<BaseApiResponse<List<OHSModel>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/protean/v1/help_and_support/categories"
            remoteDataSource.getHSCategories(url)
        }
    }

}