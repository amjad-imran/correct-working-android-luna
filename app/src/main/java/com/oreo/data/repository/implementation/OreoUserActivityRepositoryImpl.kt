package com.oreo.data.repository.implementation

import com.github.mikephil.charting.data.Entry
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.noisefit.data.local.dataStored.abstraction.IOfflineApiResponseStore
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.abstraction.CACHE_CLEAR_DEFAULT
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.local.db.fromJson
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.implementation.DELETE_DB_DAYS
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import com.noisefit.luna.BuildConfig
import com.noisefit_commans.common.checkDayDifferenceMoreNMinutes
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.KeyValue
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.data.model.UserHealthData
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.BaseApiResponseData
import com.noisefit_commans.ui.checkDayDifferenceMoreOne
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.dataConverter.OreoOfflineDataMapper
import com.oreo.data.dataConverter.OreoOnlineDataMapper
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.db.implementation.OreoAutoSportDataImpl
import com.oreo.data.db.implementation.OreoBloodOxygenDataImpl
import com.oreo.data.db.implementation.OreoBodyTemperatureDataImpl
import com.oreo.data.db.implementation.OreoHeartRateDataImpl
import com.oreo.data.db.implementation.OreoNapDataImpl
import com.oreo.data.db.implementation.OreoRespiratoryDataImpl
import com.oreo.data.db.implementation.OreoSleepDataImpl
import com.oreo.data.db.implementation.OreoStepsDataImpl
import com.oreo.data.db.implementation.OreoStressDataImpl
import com.oreo.data.model.AddWorkoutResponse
import com.oreo.data.model.LearnModel
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.OHSModel
import com.oreo.data.model.OHSQuestionariesResponseModel
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.OInternalPageResponseModal
import com.oreo.data.model.OStressInternalPageResponseModal
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.model.OreoNapDetailsDataModel
import com.oreo.data.model.RingCareResponse
import com.oreo.data.model.RingWelcome
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.ServerUserHealthResponse
import com.oreo.data.model.TapMeasureState
import com.oreo.data.model.TrendsData
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import com.oreo.receiver.workManager.HealthOverviewDataType
import com.oreo.ui.DataType
import com.oreo.ui.TestUserData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate
import org.json.JSONObject


private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

class OreoUserActivityRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val gson: Gson,
    private val localDatSource: DataStoredInterface,
    private val ringDataStore: RingDataStore,
    private val heartRateDataImpl: OreoHeartRateDataImpl,
    private val hrv: OreoStressDataImpl,
    private val bloodOxygenDataImpl: OreoBloodOxygenDataImpl,
    private val respiratoryDataImpl: OreoRespiratoryDataImpl,
    private val temperatureDataImpl: OreoBodyTemperatureDataImpl,
    private val sleepDataImpl: OreoSleepDataImpl,
    private val napDataImpl: OreoNapDataImpl,
    private val stepsDataImpl: OreoStepsDataImpl,
    private val oreoAutoSportDataImpl: OreoAutoSportDataImpl,
    private val offlineDataMapper: OreoOfflineDataMapper,
    private val keyValueDataSource: KeyValueDataSource,
    private val onlineDataMapper: OreoOnlineDataMapper,
    private val userHealthDataSource: OreoUserHealthDataDataSource,
    private val lastSyncProvider: LastSyncProvider,
    private val offlineApiStore: IOfflineApiResponseStore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : OreoUserActivityRepository {


    override suspend fun getRingCareData(): Flow<Resource<BaseApiResponse<RingCareResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/luna/protean/v1/details?type=care_ring"
            remoteDataSource.getRingCareData(url)
        }
    }

    override suspend fun getWelcomeRingData(): Flow<Resource<BaseApiResponse<RingWelcome>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.BASE_URL_NEW}/luna/protean/v1/details?type=welcome_ring"
            remoteDataSource.getRingWelcomeData(url)
        }
    }

    private fun getDaysList(startDate: String?, endDate: String?): List<String> {
        val dateList = ArrayList<String>()
        var start: LocalDate = LocalDate.parse(startDate)
        val end: LocalDate = LocalDate.parse(endDate)

        while (!start.isAfter(end)) {
            dateList.add(start.toString())
            start = start.plusDays(1)
        }

        return dateList

    }

    /**
     * @param startDate endDate in format YYYY-MM-dd
     */
    override suspend fun getUserHealthData(
        startDate: String?,
        endDate: String?
    ): Flow<Resource<BaseApiResponse<ServerUserHealthResponse>>> {

        return flow {

            var resultData: List<ServerUserHealthData>? = null
            val todayDate = DateFormats.getTodaysDateString(10)
            var resultTrendsData: TrendsData? = null
            var registerDate: Int? = null
            var tempBaseLine: Float? = null

            var apiStartDate: String? = startDate
            var apiEndDate: String? = endDate

            val cacheResult = safeCacheCall(Dispatchers.IO) {


                if (startDate.equals(endDate) && endDate.equals(todayDate)) {
                    val dateList = getDaysList(DateFormats.getCurrentDateMinusDays(6), todayDate)

                    val dates = ArrayList<LocalDate>()

                    dateList.forEach {
                        val data = userHealthDataSource.getDataByDate(it)
                        if (data == null) {
                            dates.add(LocalDate.parse(it))
                        }
                    }
                    LOGS.d("dates____ ${dates}")

                    if (dates.isEmpty()) {
                        apiStartDate = todayDate
                        apiEndDate = todayDate

                    } else {
                        val minDate = dates.stream().min(LocalDate::compareTo)
                            .get()
                        apiStartDate = minDate.toString()
                        apiEndDate = todayDate
                    }
                    return@safeCacheCall null

                } else {
                    apiStartDate = startDate
                    apiEndDate = endDate
                    val datesList = getDaysList(startDate, endDate)

                    val localData = ArrayList<ServerUserHealthData>()
                    datesList.forEach {
                        userHealthDataSource.getDataByDate(it)?.userHealthData?.let { healthData ->
                            localData.add(
                                Gson().fromJson<ServerUserHealthData>(
                                    healthData
                                )
                            )
                        }
                    }

                    if (localData.size != 7) {
                        return@safeCacheCall null
                    }

                    userHealthDataSource.getTodayTrend()?.let {
                        resultTrendsData = Gson().fromJson<TrendsData>(it)
                    }
                    return@safeCacheCall localData
                }
            }

            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData = it
                        }
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }

            if (resultData != null) {
                emit(
                    Resource.Success(
                        BaseApiResponse(
                            data = ServerUserHealthResponse(
                                data = resultData!!,
                                trends = resultTrendsData,
                                registerDate = ringDataStore.getRegisterDay(),
                                tempBaseLine = ringDataStore.getTempBaseLine()
                            ),
                            message = "",
                        )
                    )
                )
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url =
                    "${BuildConfig.BASE_URL_NEW}/luna/protean/v2/dashboard"
                remoteDataSource.getUserHealthData(url, apiStartDate, apiEndDate)
            }

            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }

                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }

                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }

                    is Resource.Success -> {

                        resource.data?.data?.let { response ->

                            resultData = response.data
                            resultTrendsData = response.trends
                            registerDate = response.registerDate
                            tempBaseLine = response.tempBaseLine
                            ringDataStore.setTempBaseLine(tempBaseLine ?: 98.6f)

                            ringDataStore.setRegisterDay(registerDate ?: -1)
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {

                    resultData?.forEach {
                        val trendData = if (it.date.equals(todayDate, true)) {
                            gson.toJson(resultTrendsData)
                        } else {
                            null
                        }

                        userHealthDataSource.insertData(
                            UserHealthData(
                                userHealthData = gson.toJson(it),
                                trendData = trendData,
                                date = it.date
                            )
                        )
                    }
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    BaseApiResponse(
                                        data = ServerUserHealthResponse(
                                            data = resultData!!,
                                            trends = resultTrendsData,
                                            registerDate = registerDate,
                                            tempBaseLine = tempBaseLine
                                        ),
                                        message = "",
                                    )
                                )
                            )
                        }

                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }
    }


    /* override suspend fun getSleepHistory(date: String): Flow<Resource<BaseApiResponse<List<OreoSleepModel>>>> {

         return flow {
             val type = KeyValueDataType.SLEEP
             var resultData: List<OreoSleepModel>? = null


             val cacheResult = safeCacheCall(Dispatchers.IO) {

                 val localData =
                     keyValueDataSource.getData(date, type)
                         ?: return@safeCacheCall null

                 val lastCallTime = localData.getSafeLastSyncValue()

                 val shouldCallApi =
                     lastCallTime.checkDayDifferenceMoreOne() || lastCallTime.checkDayDifferenceMoreNMinutes(
                         CACHE_CLEAR_DEFAULT
                     )

                 if (shouldCallApi) {
                     keyValueDataSource.removeDataByType(KeyValueDataType.SLEEP)
                     return@safeCacheCall null
                 } else {

                     if (localData.value == null) {
                         return@safeCacheCall null
                     }

                     return@safeCacheCall localData.value?.let {
                         Gson().fromJson<List<OreoSleepModel>>(
                             it
                         )
                     }
                 }
             }

             cacheResult.collect { resource ->
                 when (resource) {
                     is CacheResult.Success -> {

                         resource.value?.let {
                             resultData = it
                         }
                     }

                     is CacheResult.GenericError -> {

                     }
                 }
             }

             if (resultData != null) {
                 emit(
                     Resource.Success(
                         BaseApiResponse(
                             data = resultData,
                             message = "",
                         )
                     )
                 )
                 return@flow
             }


             val serverResult = safeApiCallFlow(dispatcher) {
                 val url = "${BuildConfig.OREO_BASE_URL}/sleep/v1/get"
                 remoteDataSource.getSleepHistory(url, date)
             }

             serverResult.collect { resource ->
                 when (resource) {
                     is Resource.GenericError -> {
                         emit(Resource.GenericError(resource.message, resource.errorCode))
                     }

                     is Resource.Loading -> {
                         emit(Resource.Loading(resource.loading))
                     }

                     is Resource.NetworkError -> {
                         emit(Resource.NetworkError(resource.response, resource.code))
                     }

                     is Resource.Success -> {

                         resource.data?.data?.let { response ->
                             resultData = response
                         }
                     }
                 }
             }

             if (resultData != null) {
                 safeCacheCall(Dispatchers.IO) {
                     keyValueDataSource.insertData(
                         KeyValue(
                             key = date,
                             value = gson.toJson(resultData),
                             type = KeyValueDataType.SLEEP.name
                         )
                     )
                 }.collect { resource ->
                     when (resource) {
                         is CacheResult.Success -> {
                             emit(
                                 Resource.Success(
                                     BaseApiResponse(
                                         data = resultData,
                                         message = "",
                                     )
                                 )
                             )
                         }

                         is CacheResult.GenericError -> {
                             emit(Resource.GenericError(message = "Something went wrong", 0))
                         }
                     }
                 }
             }
         }
     }*/

    /*override suspend fun getReadinessHistory(date: String): Flow<Resource<BaseApiResponse<List<OreoReadinessModel>>>> {

        return flow {
            val type = KeyValueDataType.READINESS
            var resultData: List<OreoReadinessModel>? = null


            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData =
                    keyValueDataSource.getData(date, type)
                        ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne() || lastCallTime.checkDayDifferenceMoreNMinutes(
                        CACHE_CLEAR_DEFAULT
                    )

                if (shouldCallApi) {
                    keyValueDataSource.removeDataByType(KeyValueDataType.READINESS)
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<List<OreoReadinessModel>>(
                            it
                        )
                    }
                }
            }

            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData = it
                        }
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }

            if (resultData != null) {
                emit(
                    Resource.Success(
                        BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }


            val serverResult = safeApiCallFlow(dispatcher) {
                val url = "${BuildConfig.OREO_BASE_URL}/protean/v1/readiness/get"
                remoteDataSource.getReadinessHistory(url, date)
            }

            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }

                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }

                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }

                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    keyValueDataSource.insertData(
                        KeyValue(
                            key = date,
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.READINESS.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    BaseApiResponse(
                                        data = resultData,
                                        message = "",
                                    )
                                )
                            )
                        }

                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }
    }*/

    override suspend fun getLearnData(): Flow<Resource<BaseApiResponse<List<LearnModel>>>> {

        return flow {
            val type = KeyValueDataType.LEARN
            var resultData = ArrayList<LearnModel>()


            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData =
                    keyValueDataSource.getData("", type)
                        ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne()
                LOGS.d("FORCE_REFRESH should call api $shouldCallApi")


                if (shouldCallApi) {
                    keyValueDataSource.removeDataByKey("", KeyValueDataType.LEARN)
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<List<LearnModel>>(
                            it
                        )
                    }
                }
            }

            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData.clear()
                            resultData.addAll(it)
                        }
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }

            if (resultData.isNotEmpty()) {
                emit(
                    Resource.Success(
                        BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }


            val serverResult = safeApiCallFlow(dispatcher) {
                val url =
                    "${BuildConfig.OREO_BASE_URL}/protean/v1/learn-more"
                remoteDataSource.getLearnData(url)
            }

            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }

                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }

                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }

                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            resultData.clear()
                            resultData.addAll(response)
                        }
                    }
                }
            }

            if (resultData.isNotEmpty()) {
                safeCacheCall(Dispatchers.IO) {
                    keyValueDataSource.insertData(
                        KeyValue(
                            key = "",
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.LEARN.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    BaseApiResponse(
                                        data = resultData,
                                        message = "",
                                    )
                                )
                            )
                        }

                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }
    }


    /* override suspend fun getDashboardData(forceRefresh: Boolean): Flow<Resource<BaseApiResponse<OreoDashboardResponseModel>>> {

         return flow {
             val type = KeyValueDataType.DASHBOARD
             var resultData: OreoDashboardResponseModel? = null


             val cacheResult = safeCacheCall(Dispatchers.IO) {

                 val localData =
                     keyValueDataSource.getData("", type)
                         ?: return@safeCacheCall null

                 val lastCallTime = localData.getSafeLastSyncValue()

                 val shouldCallApi =
                     lastCallTime.checkDayDifferenceMoreOne() || forceRefresh || lastCallTime.checkDayDifferenceMoreNMinutes(
                         CACHE_CLEAR_DEFAULT
                     )
                 LOGS.d("FORCE_REFRESH should call api $shouldCallApi")


                 if (shouldCallApi) {
                     keyValueDataSource.removeDataByKey("", KeyValueDataType.DASHBOARD)
                     return@safeCacheCall null
                 } else {

                     if (localData.value == null) {
                         return@safeCacheCall null
                     }

                     return@safeCacheCall localData.value?.let {
                         Gson().fromJson<OreoDashboardResponseModel>(
                             it
                         )
                     }
                 }
             }

             cacheResult.collect { resource ->
                 when (resource) {
                     is CacheResult.Success -> {

                         resource.value?.let {
                             resultData = it
                         }
                     }

                     is CacheResult.GenericError -> {

                     }
                 }
             }

             if (resultData != null) {
                 emit(
                     Resource.Success(
                         BaseApiResponse(
                             data = resultData,
                             message = "",
                         )
                     )
                 )
                 return@flow
             }


             val serverResult = safeApiCallFlow(dispatcher) {
                 val url = "${BuildConfig.OREO_BASE_URL}/protean/v1/dashboard"
                 remoteDataSource.getDashboardData(url)
             }

             serverResult.collect { resource ->
                 when (resource) {
                     is Resource.GenericError -> {
                         emit(Resource.GenericError(resource.message, resource.errorCode))
                     }

                     is Resource.Loading -> {
                         emit(Resource.Loading(resource.loading))
                     }

                     is Resource.NetworkError -> {
                         emit(Resource.NetworkError(resource.response, resource.code))
                     }

                     is Resource.Success -> {

                         resource.data?.data?.let { response ->
                             resultData = response
                         }
                     }
                 }
             }

             if (resultData != null) {
                 safeCacheCall(Dispatchers.IO) {
                     keyValueDataSource.insertData(
                         KeyValue(
                             key = "",
                             value = gson.toJson(resultData),
                             type = KeyValueDataType.DASHBOARD.name
                         )
                     )
                 }.collect { resource ->
                     when (resource) {
                         is CacheResult.Success -> {
                             emit(
                                 Resource.Success(
                                     BaseApiResponse(
                                         data = resultData,
                                         message = "",
                                     )
                                 )
                             )
                         }

                         is CacheResult.GenericError -> {
                             emit(Resource.GenericError(message = "Something went wrong", 0))
                         }
                     }
                 }
             }
         }
     }*/


    /*  override suspend fun getActivityHistory(date: String): Flow<Resource<BaseApiResponse<List<OreoActivityModel>>>> {

          return flow {
              val type = KeyValueDataType.ACTIVITY
              var resultData: List<OreoActivityModel>? = null


              val cacheResult = safeCacheCall(Dispatchers.IO) {

                  val localData =
                      keyValueDataSource.getData(date, type)
                          ?: return@safeCacheCall null

                  val lastCallTime = localData.getSafeLastSyncValue()

                  val shouldCallApi =
                      lastCallTime.checkDayDifferenceMoreOne() || lastCallTime.checkDayDifferenceMoreNMinutes(
                          CACHE_CLEAR_DEFAULT
                      )

                  if (shouldCallApi) {
                      keyValueDataSource.removeDataByType(KeyValueDataType.ACTIVITY)
                      return@safeCacheCall null
                  } else {

                      if (localData.value == null) {
                          return@safeCacheCall null
                      }

                      return@safeCacheCall localData.value?.let {
                          Gson().fromJson<List<OreoActivityModel>>(
                              it
                          )
                      }
                  }
              }

              cacheResult.collect { resource ->
                  when (resource) {
                      is CacheResult.Success -> {

                          resource.value?.let {
                              resultData = it
                          }
                      }

                      is CacheResult.GenericError -> {

                      }
                  }
              }

              if (resultData != null) {
                  emit(
                      Resource.Success(
                          BaseApiResponse(
                              data = resultData,
                              message = "",
                          )
                      )
                  )
                  return@flow
              }


              val serverResult = safeApiCallFlow(dispatcher) {
                  val url = "${BuildConfig.OREO_BASE_URL}/activity/v1/get"
                  remoteDataSource.getActivityHistory(url, date)
              }

              serverResult.collect { resource ->
                  when (resource) {
                      is Resource.GenericError -> {
                          emit(Resource.GenericError(resource.message, resource.errorCode))
                      }

                      is Resource.Loading -> {
                          emit(Resource.Loading(resource.loading))
                      }

                      is Resource.NetworkError -> {
                          emit(Resource.NetworkError(resource.response, resource.code))
                      }

                      is Resource.Success -> {

                          resource.data?.data?.let { response ->
                              resultData = response
                          }
                      }
                  }
              }

              if (resultData != null) {
                  safeCacheCall(Dispatchers.IO) {
                      keyValueDataSource.insertData(
                          KeyValue(
                              key = date,
                              value = gson.toJson(resultData),
                              type = KeyValueDataType.ACTIVITY.name
                          )
                      )
                  }.collect { resource ->
                      when (resource) {
                          is CacheResult.Success -> {
                              emit(
                                  Resource.Success(
                                      BaseApiResponse(
                                          data = resultData,
                                          message = "",
                                      )
                                  )
                              )
                          }

                          is CacheResult.GenericError -> {
                              emit(Resource.GenericError(message = "Something went wrong", 0))
                          }
                      }
                  }
              }
          }
      }*/

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
            HealthOverviewDataType.BODY_STRESS -> {}
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
        val timeStamp = DateFormats.lastClearDataTimeStamp(DELETE_DB_DAYS)
        oreoAutoSportDataImpl.deleteOldData(timeStamp)
        return oreoAutoSportDataImpl.getAllNotAcceptingData(timeStamp)?.size ?: 0
    }

    override suspend fun getSummaryHRHealthOverview(): OHealthOverview.HeartRate? {
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
        return OHealthOverview.HeartRate(
            value = "0",
            lastTime = "0",
            candleValue = ArrayList(),
            lineData = Pair(ArrayList<Entry>(), ArrayList<Int>()),
            xLabelList = ArrayList(),
            axisMinimum = 0f,
            average = 0f,
            measureState = TapMeasureState.DEFAULT
        )
    }

    override suspend fun addWorkout(request: JsonObject): Flow<Resource<BaseApiResponseData<OActivityListModal>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/activity/v1/add_workout"
            /* keyValueDataSource.removeDataByType(KeyValueDataType.ACTIVITY)
             keyValueDataSource.removeDataByType(KeyValueDataType.DASHBOARD)*/

            //todo clear data based on dates
            remoteDataSource.addWorkout(url, request)
        }
    }

    override suspend fun addGFitWorkout(request: JsonArray): Flow<Resource<BaseApiResponseData<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/activity/v1/add_workout_apple"
            val jsonObject = JSONObject()
            jsonObject.put("workouts", request)

            val requestObject = JsonObject().apply {
                this.add("workouts", request)
            }
            remoteDataSource.addGFitWorkout(url, requestObject)
        }
    }

    override suspend fun syncGoogleFitUserData(request: JsonObject): Flow<Resource<BaseApiResponseData<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/protean/v1/sync/healthfit"
            remoteDataSource.syncGoogleFitUserData(url, request)
        }
    }

    override suspend fun getWorkoutListRecord(): Flow<Resource<BaseApiResponse<List<OWorkoutListModal>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/activity/v1/record_workout_list"
            remoteDataSource.getWorkoutList(url)
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

    override suspend fun addRecordedWorkout(request: JsonObject): Flow<Resource<BaseApiResponse<List<AddWorkoutResponse>>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.OREO_BASE_URL}/activity/v1/add_workout"
            remoteDataSource.addRecordedWorkout(url, request)
        }
    }

    override suspend fun deleteWorkoutFromServer(id: String): Flow<Resource<BaseApiResponse<Any>>> {
        //'https://stage-oreo.gonoise.com/activity/v1/delete_workout/e900fe86-f2d5-422f-be40-9d0d633caa19
        return safeApiCallFlow(dispatcher) {
            /*keyValueDataSource.removeDataByType(KeyValueDataType.ACTIVITY)
            keyValueDataSource.removeDataByType(KeyValueDataType.DASHBOARD)*/
            //todo clear data base on dates
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

        return flow {
            val type = KeyValueDataType.CONTRIBUTORS
            var resultData: OContributorResponseModal? = null


            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData =
                    keyValueDataSource.getData(contributorType, type)
                        ?: return@safeCacheCall null

                val lastCallTime = localData.getSafeLastSyncValue()

                val shouldCallApi =
                    lastCallTime.checkDayDifferenceMoreOne() || lastCallTime.checkDayDifferenceMoreNMinutes(
                        CACHE_CLEAR_DEFAULT
                    )

                if (shouldCallApi) {
                    keyValueDataSource.removeDataByType(KeyValueDataType.CONTRIBUTORS)
                    return@safeCacheCall null
                } else {

                    if (localData.value == null) {
                        return@safeCacheCall null
                    }

                    return@safeCacheCall localData.value?.let {
                        Gson().fromJson<OContributorResponseModal>(
                            it
                        )
                    }
                }
            }

            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData = it
                        }
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }

            if (resultData != null) {
                emit(
                    Resource.Success(
                        BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }


            val serverResult = safeApiCallFlow(dispatcher) {
                val url = "${BuildConfig.OREO_BASE_URL}/protean/v1/details"
                remoteDataSource.getContributorsDetails(url, contributorType)
            }

            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }

                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }

                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }

                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            resultData = response
                        }
                    }
                }
            }

            if (resultData != null) {
                safeCacheCall(Dispatchers.IO) {
                    keyValueDataSource.insertData(
                        KeyValue(
                            key = contributorType,
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.CONTRIBUTORS.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    BaseApiResponse(
                                        data = resultData,
                                        message = "",
                                    )
                                )
                            )
                        }

                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }
    }

    override suspend fun getWorkoutDetails(id: String): Flow<Resource<BaseApiResponse<OWorkoutDetailsResponseModel>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.OREO_BASE_URL}/activity/v1/workout_detail/${id}"
            remoteDataSource.getWorkoutDetails(url)
        }
    }

    private fun shouldCallBannerApi(
        serverTime: Long,
        localTime: Long
    ): Boolean {
        if (serverTime == 0L) return true
        if (localTime == 0L) return true

        return localTime < serverTime
    }

    fun getLocalHelpAndSupportData(removeData: Boolean): List<OHSModel>? {
        if (removeData) {
            offlineApiStore.setHelpAndSupportList(null)
            return ArrayList()
        }
        return offlineApiStore.getHelpAndSupportList()
    }

    override suspend fun getHSCategories(): Flow<Resource<BaseApiResponse<List<OHSModel>>>> {
        return flow {

            val serverUpdateTimeStamp =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.H_AND_SUPPORT_SERVER_UPDATE)
            val localSyncTime =
                lastSyncProvider.getSyncTimeStamp(LastSyncItems.HELP_AND_SUPPORT_LIST)

            val shouldCallApi = shouldCallBannerApi(serverUpdateTimeStamp, localSyncTime)

            val resultData = ArrayList<OHSModel>()

            val cacheResult = safeCacheCall(Dispatchers.IO) {
                getLocalHelpAndSupportData(shouldCallApi)
            }
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData.clear()
                            resultData.addAll(it)
                        }
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }


            if (resultData.isNotEmpty()) {
                emit(Resource.Success(BaseApiResponse(data = resultData, message = "")))
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url = "${BuildConfig.BASE_URL_NEW}/core/ring/help_and_support/categories"
                remoteDataSource.getHSCategories(url)
            }


            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }

                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }

                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }

                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            withContext(Dispatchers.IO) {
                                keyValueDataSource.removeDataByType(KeyValueDataType.H_AND_S)
                            }
                            lastSyncProvider.setSyncTimeStamp(LastSyncItems.HELP_AND_SUPPORT_LIST)
                            resultData.clear()
                            resultData.addAll(response)
                        }
                    }
                }
            }

            if (resultData.isNotEmpty()) {
                safeCacheCall(Dispatchers.IO) {
                    offlineApiStore.setHelpAndSupportList(resultData)
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(Resource.Success(BaseApiResponse(data = resultData, message = "")))
                        }

                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }
    }

    override suspend fun clearAllHealthData() {
        GlobalScope.launch(Dispatchers.IO) {
            userHealthDataSource.clearAllData()
        }
    }

    override suspend fun getHSQAnswer(quesId: String): Flow<Resource<BaseApiResponse<List<OHSQuestionariesResponseModel>>>> {
        return flow {
            val resultData = ArrayList<OHSQuestionariesResponseModel>()

            val cacheResult = safeCacheCall(Dispatchers.IO) {

                val localData =
                    keyValueDataSource.getData(quesId, KeyValueDataType.H_AND_S)
                        ?: return@safeCacheCall null

                if (localData.value == null) {
                    return@safeCacheCall null
                }

                return@safeCacheCall localData.value?.let {
                    Gson().fromJson<List<OHSQuestionariesResponseModel>>(
                        it
                    )
                }
            }
            cacheResult.collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {

                        resource.value?.let {
                            resultData.addAll(it)
                        }
                    }

                    is CacheResult.GenericError -> {

                    }
                }
            }


            if (resultData.isNotEmpty()) {
                emit(
                    Resource.Success(
                        BaseApiResponse(
                            data = resultData,
                            message = "",
                        )
                    )
                )
                return@flow
            }

            val serverResult = safeApiCallFlow(dispatcher) {
                val url = "${BuildConfig.BASE_URL_NEW}/core/ring/help_and_support/answers/${quesId}"
                remoteDataSource.getHSQAnswer(url)
            }


            serverResult.collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        emit(Resource.GenericError(resource.message, resource.errorCode))
                    }

                    is Resource.Loading -> {
                        emit(Resource.Loading(resource.loading))
                    }

                    is Resource.NetworkError -> {
                        emit(Resource.NetworkError(resource.response, resource.code))
                    }

                    is Resource.Success -> {

                        resource.data?.data?.let { response ->
                            resultData.clear()
                            resultData.addAll(response)
                        }
                    }
                }
            }

            if (resultData.isNotEmpty()) {
                safeCacheCall(Dispatchers.IO) {
                    keyValueDataSource.insertData(
                        KeyValue(
                            key = quesId,
                            value = gson.toJson(resultData),
                            type = KeyValueDataType.H_AND_S.name
                        )
                    )
                }.collect { resource ->
                    when (resource) {
                        is CacheResult.Success -> {
                            emit(
                                Resource.Success(
                                    BaseApiResponse(
                                        data = resultData,
                                        message = "",
                                    )
                                )
                            )
                        }

                        is CacheResult.GenericError -> {
                            emit(Resource.GenericError(message = "Something went wrong", 0))
                        }
                    }
                }
            }
        }
    }

    override suspend fun getUserNapData(napId: String): Flow<Resource<BaseApiResponse<OreoNapDetailsDataModel>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.OREO_BASE_URL}/sleep/v1/nap/$napId"
            remoteDataSource.getUserNapDetailsData(url)
        }
    }

    override suspend fun addNapServer(nap: OreoNapData): Flow<Resource<BaseApiResponse<List<OreoNapDetailsDataModel>>>> {
        val napRequest = onlineDataMapper.getNapRequest(nap)

        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.OREO_BASE_URL}/sleep/v1/nap"
            remoteDataSource.addNapServer(url, napRequest)
        }
    }

    /**
     * delete naps more than 2 days and returns response
     * Check if has naps similar to sleep start time
     */
    override suspend fun getNapsToConfirm(): List<OreoNapData>? {
        napDataImpl.deleteOldData(2)
        val naps = napDataImpl.getNaps()
        val filteredNaps = ArrayList<OreoNapData>()
        naps?.forEach {
            val sleep = sleepDataImpl.getSleepByStartTime(it.startTime ?: "")?.firstOrNull()
            if (sleep == null) {
                filteredNaps.add(it)
            }
        }
        return if (filteredNaps.isEmpty()) return null else filteredNaps
    }

    override suspend fun removeNap(id: Int): Boolean {
        napDataImpl.removeNapById(id)
        return true
    }

    //todo endpoint, response, request format will change, once define
    override suspend fun getStressInternalPagesData(
        selectDate: String,
        filterType: String
    ): Flow<Resource<BaseApiResponse<OStressInternalPageResponseModal>>> {
        return safeApiCallFlow(dispatcher) {
            val url =
                "${BuildConfig.OREO_BASE_URL}/activity/v1/activity-contributors"
            remoteDataSource.getStressInternalPageData(url, selectDate, filterType)
        }
    }

}