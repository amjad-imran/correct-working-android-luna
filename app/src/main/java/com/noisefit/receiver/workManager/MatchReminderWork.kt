package com.noisefit.receiver.workManager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noisefit.data.local.db.CacheResult
import com.noisefit_commans.data.model.matches.SportEvent
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
class MatchReminderWork
@AssistedInject
constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val localDataStore: DataStoredInterface,
    private val sportEventRepository: SportEventRepository,
    private val sessionManager: SessionManager,
    private val sportUtils: SportUtils
) : CoroutineWorker(context, workerParams) {

    private val TAG = "MatchReminderWork"

    private val workScope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext =
            Dispatchers.IO // no job added i.e + SupervisorJob()
    }

    private fun reScheduleJob() {
        ApplicationUtils.scheduleMatchWork(context)
    }

    override suspend fun doWork(): Result {
        LOGS.d("$TAG Running Sport Scheduler")
        AppLogs.sendAppLogs("Sports: $TAG doWork")
        reScheduleJob()
        if (sessionManager == null || !sessionManager.isDeviceConnected()) {
            LOGS.d("$TAG MatchReminderWork: Device is disconnected")
            return Result.success()
        }

        workScope.launch {
            getMatchInfo()
        }

        return Result.success()
    }


    private suspend fun getMatchInfo() {
        val date = DateFormats.getTodaysDateString(9)
        workScope.launch {
            sportEventRepository.getSelectedSportEvents(date).collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        if (!resource.value.isNullOrEmpty()) {
                            handleNotifications(resource.value)
                        }
                    }
                    is CacheResult.GenericError -> {
                        LOGS.d("$TAG ${resource.errorMessage}")

                    }
                }
            }
        }

    }


    private fun handleNotifications(eventList: List<SportEvent>) {
        var message = ""
        eventList.forEach { sportEvent ->
            message += "Today's ${
                DateFormats.getDateFromMillis(
                    sportEvent.timeInMilliseconds,
                    "h:mm a"
                )
            } match - ${sportEvent.title} "
        }

        message += getRandomCheersMessage()
        val appCode = ApplicationType.SPORT_EVENT.type

        val matchBetween = "Game Reminder"

        val appNotification =
            AppNotification(appCode, matchBetween, null, message)
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SendAppNotification(
                appNotification
            )
        )
        AppLogs.sendAppLogs("Sports: $TAG Notification Sent $matchBetween ")

    }

    private fun getRandomCheersMessage(): String {

        val messages = arrayListOf(
            "It's game time.",
            "The pitch is set for an epic showdown.",
            "Get your game face on.",
            "Choose a team to cheer for and enjoy!",
            "Pick your side!"
        )
        val num = (0 until messages.size).random()
        return messages[num]
    }

}