package com.noisefit.receiver.workManager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.noisefit.data.local.db.CacheResult
import com.noisefit_commans.data.model.matches.score.MatchesResultScore
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.SportEventRepository
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.SportUtils
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.enums.ApplicationType
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.AppNotification
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


@HiltWorker
class SportWork
@AssistedInject
constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataStore: DataStoredInterface,
    private val sportEventRepository: SportEventRepository,
    private val sessionManager: SessionManager,
    private val sportUtils: SportUtils
) : CoroutineWorker(context, workerParams) {

    private val TAG = "SportWork"

    private val workScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            Dispatchers.IO // no job added i.e + SupervisorJob()
    }


    override suspend fun doWork(): Result {
        LOGS.d("$TAG Running Sport Scheduler")
        AppLogs.sendAppLogs("SportWork doWork()")

        if (!ApplicationUtils.isInternetConnected()) {
            LOGS.d("SportWork: No Internet Access!!")
            return Result.success()
        }

        if (sessionManager == null || !sessionManager.isDeviceConnected()) {
            LOGS.d("$TAG SportWork: Device is disconnected")
            return Result.success()
        }

        workScope.launch {
            getMatchInfo()
        }

        return Result.success()
    }


    private suspend fun getMatchInfo() {

        val matchInfo = sessionManager.upcomingSportEvent ?: return
        sportEventRepository.matchInfo(
            745,
            matchInfo.eventId.toInt()
        )
            .collect { resource ->
                when (resource) {
                    is Resource.NetworkError -> {
                        AppLogs.sendAppLogs("$TAG Notification : Network Error")
                    }
                    is Resource.Success -> {
                        try {
                            val sportObject = resource.data?.asJsonObject
                            AppLogs.sendAppLogs("$TAG Notification :Api success")
                            if (sportObject?.has("result") == true) {
                                val resultArray = sportObject.getAsJsonArray("result")
                                for (resultObject in resultArray) {
                                    val matchesResultScore =
                                        sportUtils.parseJsonObject(resultObject)
                                    handleNotifications(matchesResultScore)

                                }

                            } else {
                                val currentTimeStamp = DateFormats.getTimeStamp()
                                val timeAfter3Hours =
                                    DateFormats.addHourInMilliseconds(matchInfo.timeInMilliseconds)

                                if (currentTimeStamp > timeAfter3Hours) {
                                    LOGS.d("$TAG inside expire match $currentTimeStamp $timeAfter3Hours")
                                    deleteFinishEvent(matchInfo.eventId)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    else -> {}
                }
            }
    }


    private fun handleNotifications(matchesResultScore: MatchesResultScore) {
        AppLogs.sendAppLogs("$TAG Parsed Notification : ${Gson().toJson(matchesResultScore)}")
        val matchBetween =
            "${sportUtils.getTeam(matchesResultScore.homeTeamKey!!)} vs " +
                    sportUtils.getTeam(matchesResultScore.awayTeamKey!!)

        val appCode = ApplicationType.SPORT_EVENT.type
        var message = ""
        when (matchesResultScore.eventStatus?.lowercase()) {
            "in progress" -> {
                message = sportUtils.getMessage(matchesResultScore)
              //  LOGS.d("$TAG ${Gson().toJson(message)}")
            }
            "innings break" -> {
                message = sportUtils.getInningBreakMessage(matchesResultScore)
               // LOGS.d("$TAG ${Gson().toJson(message)}")
            }
            "finished" -> {
                deleteFinishEvent(matchesResultScore.eventKey!!)
                message = sportUtils.getFinishedMessage(matchesResultScore)
            }
//            "strategic timeout" -> {
//
//            }
            else -> {
                message = sportUtils.getMessage(matchesResultScore)
                LOGS.d("$TAG ${Gson().toJson(message)}")
            }
        }

        val appNotification =
            AppNotification(appCode, matchBetween, null, message)
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SendAppNotification(
                appNotification
            )
        )

        AppLogs.sendAppLogs("$TAG Notification sent : $message")
    }


    private fun deleteFinishEvent(eventId: String) {
        workScope.launch {
            sportEventRepository.deleteSportEvent(eventId).collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        ApplicationUtils.stopSportScheduler(context)
                        sessionManager.upcomingSportEvent = null
                    }
                    is CacheResult.GenericError -> {


                    }
                }
            }
        }
    }


}