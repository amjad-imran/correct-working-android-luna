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
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.fromJson
import com.noisefit.data.remote.base.Resource
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanN
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.repository.abstraction.OreoSyncRepository
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
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
    private val userActivityRepository: OreoUserActivityRepository
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
        val todayDate = DateFormats.getTodaysDateString(7)

        /*var shouldUserObjectSync = false
        if (localDataStore.getGFitUserDataLastSyncTime().checkTimeDifferenceMoreThanN(24)) {
            shouldUserObjectSync = true
        }*/

        LOGS.d("$TAG inside")
        job = syncDataScope.launch {
            supervisorScope {

                val googleFitSleepData =
                    syncRepository.getGoogleFitSleepUnSyncData(todayDate)
                LOGS.d("$TAG ${googleFitSleepData?.size}")
                val call1 = async {
                    googleFitSleepData?.let { sleepDataList ->
                        sleepDataList.forEach { sleepData ->
                            LOGS.d("$TAG google  inside sleep data")
                            val googleSleepData =
                                offlineDataMapper.convertSleepDataToGoogleFit(sleepData)
                            LOGS.d("$TAG google  inside sleep data 2")
                            LOGS.d("GOOGLE_SLEEP_DATA ${Gson().toJson(googleSleepData)}")
                            AppLogs.sendAppLogs("Sleep Google Fit Parsed -> ${Gson().toJson(googleSleepData)}")
                            googleSleepData?.let { sleepDataGoogleFit ->
                                LOGS.d("$TAG google  inside sleep data 3")
                                googleFitDataObservers.insertSleepData(
                                    sleepDataGoogleFit, success = {
                                        LOGS.d("$TAG google success sleep data")
                                        AppLogs.sendAppLogs("Sleep Google Fit Sync success")
                                        job = syncDataScope.launch {
                                            syncRepository.updateGoogleFitUnSyncSleepStatus(
                                                sleepData
                                            )
                                        }
                                    },
                                    failed = {
                                        LOGS.d("google failed sleep")

                                    })
                            }

                        }


                    }

                }


               /* if (shouldUserObjectSync) {
                    LOGS.d("$TAG height weight start get")
                    val call2 = async {
                        googleFitDataObservers.getHeightWeight(
                            success = {
                                LOGS.d("$TAG ${it.first}")
                                LOGS.d("$TAG ${it.second}")
                                syncDataScope.launch {
                                    userActivityRepository.syncGoogleFitUserData(
                                        offlineDataMapper.convertGFUserDataIntoJsonObject(
                                            it
                                        )
                                    ).collect { resource1 ->
                                        when (resource1) {
                                            is Resource.GenericError -> {
                                                LOGS.d("$TAG height weight api error")
                                            }

                                            is Resource.Loading -> {

                                            }

                                            is Resource.NetworkError -> {

                                            }

                                            is Resource.Success -> {
                                                LOGS.d("$TAG height weight api success")
                                                localDataStore.setGFitUserDataLastSyncTime()
                                            }
                                        }
                                    }
                                }

                            },
                            failed = {
                                LOGS.d("$TAG height weight failed")
                            }
                        )
                    }
                    call2.await()
                }*/

                val call3 = async {
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
                }

                try {
                    call1.await()

                    call3.await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (::job.isInitialized) {
                    job.cancel()
                    LOGS.d("SyncDataWork: Google fit Sync complete")
                }
            }
        }



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