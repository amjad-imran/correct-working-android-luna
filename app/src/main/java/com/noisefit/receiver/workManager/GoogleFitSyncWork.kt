package com.noisefit.receiver.workManager

import android.annotation.SuppressLint
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture
import com.noisefit.data.dataConverter.OfflineDataMapper
import com.noisefit.data.googleFit.GoogleFitDataObservers
import com.noisefit.data.repository.abstraction.SyncRepository
import com.noisefit.session.SessionManager
import com.noisefit.watch.UserActivityHandler
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

private const val TAG = "GoogleFitSyncWork"

@HiltWorker
class GoogleFitSyncWork @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataStore: DataStoredInterface,
    private val sessionManager: SessionManager,
    private val syncRepository: SyncRepository,
    private val userActivityHandler: UserActivityHandler,
    private val googleFitDataObservers: GoogleFitDataObservers,
    private val offlineDataMapper: OfflineDataMapper,
) : ListenableWorker(context, workerParams) {

    private var mFuture: SettableFuture<Result>? = null
    private var userActivityDataActions: UserActivityDataActions? = null
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

        job = syncDataScope.launch {
            supervisorScope {

                val googleFitStepsData =
                    syncRepository.getGoogleFitUnSyncData(todayDate)

//                LOGS.d("$TAG ${googleFitStepsData}")
                val call1 = async {
                    googleFitStepsData?.stepDataGoogleFit?.let { stepsData ->
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
                val call2 = async {
                    googleFitStepsData?.heartRateList?.let { heartRateList ->
                        googleFitDataObservers.insertHeartData(
                            heartRateList, success = {
                                LOGS.d("google success heart rate")
                                job = syncDataScope.launch {
                                    syncRepository.updateGoogleFitUnSyncHeartRateStatus(
                                        heartRateList
                                    )
                                }


                            },
                            failed = {
                                LOGS.d("google failed steps")

                            })

                    }

                }

                val call3 = async {
                    googleFitStepsData?.sleepData?.let { sleepDataList ->
                        sleepDataList.forEach { sleepData ->
                            LOGS.d("google  inside sleep data")
                            val googleSleepData =
                                offlineDataMapper.convertSleepDataToGoogleFit(sleepData)
                            LOGS.d("google  inside sleep data 2")
                            googleSleepData?.let { sleepDataGoogleFit ->
                                LOGS.d("google  inside sleep data 3")
                                googleFitDataObservers.insertSleepData(
                                    sleepDataGoogleFit, success = {
                                        LOGS.d("google success sleep data")
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

                try {
                    call1.await()
                } catch (e: Exception) {

                }
                try {
                    call2.await()
                } catch (e: Exception) {

                }

                try {
                    call3.await()
                } catch (e: Exception) {

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
//                    sessionManager.setSyncCompletedState(Event(SyncDataStatus(status = EventConstants.UPDATE_STATUS_FAILED)))
                    mFuture!!.set(Result.failure())
                }
            )
        }


        return mFuture!!
    }
}