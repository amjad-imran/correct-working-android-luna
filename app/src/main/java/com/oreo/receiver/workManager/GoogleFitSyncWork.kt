package com.oreo.receiver.workManager

import android.annotation.SuppressLint
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.noisefit.data.dataConverter.OfflineDataMapper
import com.noisefit.data.googleFit.GoogleFitDataObservers
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.abstaction.GoogleFitDataSource
import com.oreo.data.repository.abstraction.OreoSyncRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.CoroutineContext

private const val TAG = "GoogleFitSyncWork"

@HiltWorker
class GoogleFitSyncWork
@AssistedInject
constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataStore: DataStoredInterface,
    private val syncRepository: OreoSyncRepository,
    private val googleFitDataObservers: GoogleFitDataObservers,
    private val offlineDataMapper: OfflineDataMapper,
    private val sessionManager: SessionManager,
    private val userActivityRepository: OreoUserActivityRepository,
    private val googleFitDataSource: GoogleFitDataSource
) : ListenableWorker(context, workerParams) {

    private var mFuture: SettableFuture<Result>? = null

    private val syncDataScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            Dispatchers.IO // no job added i.e + SupervisorJob()
    }

    lateinit var job: Job

    private fun returnSuccess(success: () -> Unit) {
        return success.invoke()
    }


    private suspend fun getSyncData(success: () -> Unit, failed: () -> Unit) {
        val todayDate = DateFormats.getTodaysDateString(10)

        /*var shouldUserObjectSync = false
        if (localDataStore.getGFitUserDataLastSyncTime().checkTimeDifferenceMoreThanN(24)) {
            shouldUserObjectSync = true
        }*/

        LOGS.d("$TAG inside")

        /**
         * Save sleep to google fit
         */
        job = syncDataScope.launch(Dispatchers.IO) {
            supervisorScope {
                val syncSteps =
                    localDataStore.getStatusGoogleFitKey("steps")
                val callSaveSteps = async {
                    if (syncSteps) {
                        val googleFitStepData =
                            syncRepository.getGoogleFitUnSyncDataSteps(todayDate)

                        googleFitStepData?.let { stepsData ->
                            try {
                                googleFitDataObservers.insertStepData(
                                    stepsData, success = {
                                        LOGS.d("$TAG google success steps")
                                        job = syncDataScope.launch {

                                            syncRepository.updateGoogleFitUnSyncStepsStatus(
                                                todayDate,
                                                stepsData
                                            )
                                        }


                                    },
                                    failed = {
                                        LOGS.d("$TAG google failed steps")

                                    })
                            } catch (e: Exception) {
                                LOGS.d("$TAG ${e.message}")
                                e.printStackTrace()
                            }
                        }

                    }
                }


                val syncSleep =
                    localDataStore.getStatusGoogleFitKey("sleep")
                val callSaveSleep = async {
                    if (syncSleep) {
                        val googleFitSleepData =
                            syncRepository.getGoogleFitSleepUnSyncData(todayDate)
                        LOGS.d("$TAG ${googleFitSleepData?.size}")

                        LOGS.d("$TAG GET Sleep")

                        googleFitSleepData?.let { sleepDataList ->
                            sleepDataList.forEach { sleepData ->
                                LOGS.d("$TAG google  inside sleep data")
                                val googleSleepData =
                                    offlineDataMapper.convertSleepDataToGoogleFit(sleepData)
                                LOGS.d("$TAG google  inside sleep data 2")
                                LOGS.d("GOOGLE_SLEEP_DATA ${Gson().toJson(googleSleepData)}")

                                googleSleepData?.let { sleepDataGoogleFit ->
                                    LOGS.d("$TAG google  inside sleep data 3")
                                    googleFitDataObservers.insertSleepData(
                                        sleepDataGoogleFit, success = {
                                            LOGS.d("$TAG google success sleep data")

                                        },
                                        failed = {
                                            LOGS.d("google failed sleep")

                                        })
                                }

                            }


                        }

                    }

                }


                /* val callGetWorkout = async {
                     LOGS.d("$TAG GET Workout")

                     googleFitDataObservers.getWorkoutFromSession(
                         success = {

                             LOGS.d("$TAG workout session ${it.size}")
                             syncDataScope.launch {
                                 syncRepository.saveAndGetGFitWorkout(
                                     offlineDataMapper.convertWorkoutGoogleFit(
                                         it
                                     )
                                 ).collect { resource ->
                                     when (resource) {
                                         is CacheResult.Success -> {
                                             LOGS.d("$TAG workout session offline ${resource.value?.size}")
                                             if (!resource.value.isNullOrEmpty()) {

                                                 syncDataScope.launch {
                                                     userActivityRepository.addGFitWorkout(
                                                         offlineDataMapper.convertGFWorkoutIntoJsonArray(
                                                             resource.value
                                                         )
                                                     ).collect { resource1 ->
                                                         when (resource1) {
                                                             is Resource.GenericError -> {
                                                                 LOGS.d("$TAG workout session api error")
                                                             }

                                                             is Resource.Loading -> {

                                                             }

                                                             is Resource.NetworkError -> {

                                                             }

                                                             is Resource.Success -> {
                                                                 LOGS.d("$TAG workout session api success")
                                                                 syncRepository.updateGFitSyncWorkout(
                                                                     resource.value
                                                                 ).collect { resource2 ->
                                                                     when (resource2) {
                                                                         is CacheResult.Success -> {
                                                                             sessionManager.reloadTodayData.postValue(
                                                                                 Event(true)
                                                                             )
                                                                             LOGS.d("$TAG workout session gFit success")
                                                                         }

                                                                         is CacheResult.GenericError -> {
                                                                             LOGS.d("$TAG workout session gFit error")
                                                                         }
                                                                     }
                                                                 }
                                                             }
                                                         }
                                                     }
                                                 }
                                             }


                                             LOGS.d(
                                                 TAG,
                                                 "OreoSyncDataWork: steps ${resource.value}"
                                             )

                                         }

                                         is CacheResult.GenericError -> {

                                             LOGS.e(TAG, "OreoSyncDataWork: Error $it")

                                         }
                                     }
                                 }
                             }

                             LOGS.d("$TAG ${it.size}")
                         },
                         failed = {
                             LOGS.d("$TAG workout failed")
                         }
                     )
                 }*/

                val callGetBodyMeasurements = async(Dispatchers.IO) {
                    LOGS.d("$TAG GET Body measurements")
                    val syncBodyMeasurements =
                        localDataStore.getStatusGoogleFitKey("body_measurement")
                    if (syncBodyMeasurements) {
                        googleFitDataObservers.getHeightWeight(
                            success = {
                                LOGS.d("$TAG HEIGHT - ${it.height} Weight - ${it.weight} Body Fat - ${it.bodyFat}")

                                syncDataScope.launch(Dispatchers.IO) {
                                    googleFitDataSource.saveBodyMeasurements(it)
                                }

                            },
                            failed = {
                                LOGS.d("$TAG height weight failed")
                            }
                        )
                    }

                }


                val callGetWorkoutSessions = async(Dispatchers.IO) {
                    LOGS.d("$TAG GET Activity")
                    val syncWorkout =
                        localDataStore.getStatusGoogleFitKey("workout")
                    if (syncWorkout) {
                        googleFitDataObservers.importHealthSessions(
                            success = { workoutList ->

                                //LOGS.d("$TAG Workout import session ${workoutList.size}")

                                LOGS.d(TAG, "workout import data - ${Gson().toJson(workoutList)}")

                                syncDataScope.launch(Dispatchers.IO) {
                                    googleFitDataSource.saveWorkouts(workoutList)
                                    LOGS.d(TAG, "workout saved")
                                }
                            },
                            failed = {
                                LOGS.d("$TAG GET Sleep failed")
                            }
                        )
                    }
                }

                val callGetSleepSessions = async(Dispatchers.IO) {
                    LOGS.d("$TAG GET Activity")

                    if (syncSleep) {
                        googleFitDataObservers.importSleepSessions(
                            success = { sleepList ->

                                //LOGS.d("$TAG Workout import session ${workoutList.size}")

                                LOGS.d(TAG, "Sleep import data - ${Gson().toJson(sleepList)}")

                                syncDataScope.launch(Dispatchers.IO) {
                                    googleFitDataSource.saveSleeps(sleepList)
                                    LOGS.d(TAG, "Sleep saved")
                                }


                            },
                            failed = {
                                LOGS.d("$TAG GET Sleep failed")
                            }
                        )
                    }
                }

                /* val callGetSleep = async {
                     LOGS.d("$TAG GET Sleep")

                     googleFitDataObservers.importSleepData(
                         success = { sleepList ->

                             LOGS.d("$TAG Sleep import session ${sleepList.size}")

                             LOGS.d(TAG,"Sleep data - ${Gson().toJson(sleepList)}")
                         },
                         failed = {
                             LOGS.d("$TAG GET Sleep failed")
                         }
                     )
                 }*/

                try {
                    callSaveSteps.await()
                    callSaveSleep.await()
                    callGetBodyMeasurements.await()
                    callGetWorkoutSessions.await()
                    callGetSleepSessions.await()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (::job.isInitialized) {
                    job.cancel()
                    LOGS.d("SyncDataWork: Google fit Sync complete")
                }
            }
        }

        sessionManager.setGoogleFitSync()
        returnSuccess(success)
    }


    @SuppressLint("RestrictedApi")
    override fun startWork(): ListenableFuture<Result> {
        mFuture = SettableFuture.create()
        job = syncDataScope.launch {
            getSyncData(
                success = {
                    LOGS.d("SyncDataWork: GoogleFit")
                    mFuture!!.set(Result.success())
                },
                failed = {
                    mFuture!!.set(Result.failure())
                }
            )
        }


        return mFuture!!
    }
}