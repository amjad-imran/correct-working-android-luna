package com.noisefit.util.moveToServer

import com.noisefit.NoiseFitApplicationMain
import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.data.model.OreoSleepData
import javax.inject.Inject

private const val TAG = "SleepNotificationUtils"

class SleepNotificationUtils
@Inject
constructor(
    private val localDataStore: DataStoredInterface
) {

    private fun sleepingBufferTimeOver(hour: Int): Boolean {
        if (hour in 10..23) {
            return true
        }
        return false
    }

    fun handleSleepData(sleepData: SleepData?) {
        if(sleepData == null){
            LOGS.d("$TAG , empty sleep data")
            return
        }
        if (!sleepingBufferTimeOver(DateFormats.getCurrentHour())) {
            LOGS.d("$TAG , sleepingBufferTimeOver: not over ${DateFormats.getCurrentHour()}")
            return
        }
        val todayDate = DateFormats.getTodaysDateString(7)
        LOGS.d("$TAG , todayDate: $todayDate ${sleepData.date} ${sleepData.sleepScore}")
        if (sleepData.date == todayDate &&  sleepData.sleepScore != 0) {
            val lastSleepSyncDate = localDataStore.getSleepLastLocalNotification()
            LOGS.d("$TAG , lastSleepSyncDate: $lastSleepSyncDate")
            val allowSleepScore = if (lastSleepSyncDate == null) {
                true
            } else {
                lastSleepSyncDate != todayDate
            }
            LOGS.d("$TAG , allowSleepScore: $allowSleepScore ${sleepData.sleepScore}")
            if (allowSleepScore) {
                handleSleepScore(sleepData.sleepScore, todayDate)
            }
        }
    }

    fun handleSleepData(sleepData: OreoSleepData?) {
        if(sleepData == null){
            LOGS.d("$TAG , empty sleep data")
            return
        }
        if (!sleepingBufferTimeOver(DateFormats.getCurrentHour())) {
            LOGS.d("$TAG , sleepingBufferTimeOver: not over ${DateFormats.getCurrentHour()}")
            return
        }
        val todayDate = DateFormats.getTodaysDateString(7)
        LOGS.d("$TAG , todayDate: $todayDate ${sleepData.date} ${sleepData.sleepScore}")
        if (sleepData.date == todayDate &&  sleepData.sleepScore != 0) {
            val lastSleepSyncDate = localDataStore.getSleepLastLocalNotification()
            LOGS.d("$TAG , lastSleepSyncDate: $lastSleepSyncDate")
            val allowSleepScore = if (lastSleepSyncDate == null) {
                true
            } else {
                lastSleepSyncDate != todayDate
            }
            LOGS.d("$TAG , allowSleepScore: $allowSleepScore ${sleepData.sleepScore}")
            if (allowSleepScore) {
                handleSleepScore(sleepData.sleepScore, todayDate)
            }
        }
    }

    private fun saveLastSleepStatus(date: String) {
        localDataStore.setSleepLastLocalNotification(date)
    }

    private fun handleSleepScore(score: Int, date: String) {
        val userName = localDataStore.getUser()?.getOnlyFirstName()?.trim() ?: "Stranger"
        LOGS.d(TAG,"Received score $score")
        when (score) {
            in 1..59 -> {
                val title =
                    "$userName, you only slept for a few hours."
                val content =
                    "Today please take it slow and try to sleep on time tonight."
                showLocalNotification(title, content)
                saveLastSleepStatus(date)
            }
            in 60..89 -> {
                val title =
                    "Good going $userName!"
                val content = "You had a satisfactory sleep yesterday. Tap away to check your sleep score"
                showLocalNotification(title, content)
                saveLastSleepStatus(date)
            }
            in 90..100 -> {
                val title =
                    "Yay $userName!"
                val content = "You had a good sleep yesterday. Tap away to check your sleep score."
                showLocalNotification(title, content)
                saveLastSleepStatus(date)
            }
        }
    }

    /*private fun showLocalNotification(title: String, content: String) {
        LOGS.d("$TAG , title: $title")
        val context = NoiseFitApplicationMain.context!!
        val alarmMgr =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        alarmIntent.putExtra("title", title)
        alarmIntent.putExtra("content", content)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            12309,
            alarmIntent,
            PendingIntent.FLAG_MUTABLE
        )
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pendingIntent)
        alarmMgr?.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1000,

            pendingIntent
        )
    }*/

    private fun showLocalNotification(title: String, content: String) {
        LOGS.d("SleepNotificationUtils , broad: $title")
        NotificationUtil.pushNotification(
            NoiseFitApplicationMain.context!!,
            title,
            content,
            NotificationEventsClass.LOCAL_SLEEP_NOTIFICATION_KEY,
            "1"
        )
    }

    fun dummySleepData(): SleepData {
        return SleepData(sleepScore = 80, date = DateFormats.getTodaysDateString(7))
    }
}