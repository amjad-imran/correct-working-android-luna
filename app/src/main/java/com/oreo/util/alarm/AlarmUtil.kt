package com.oreo.util.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.noisefit.luna.R
import com.noisefit_commans.utils.LOGS
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class AlarmUtil @Inject constructor(
    private val context: Context,
) {

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleWeeklyAlarm(
        dayOfWeek: Int,
        hour: Int,
        minute: Int,
        alarmTone: Int,
        bedTime: LocalTime
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("alarmTone", alarmTone)
        intent.putExtra("time",   String.format(locale = Locale.US, "%02d:%02d", hour, minute))



        // Set the alarm time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Ensure the alarm is set for the future
            if (before(Calendar.getInstance())) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        intent.putExtra("millis",calendar.timeInMillis)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            dayOfWeek, // Unique request code for each day
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = PendingIntent.getActivity(
            context,
            dayOfWeek,
            Intent(context, AlarmActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(calendar.timeInMillis, showIntent),
            pendingIntent
        )

        val calendarBedTime = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, bedTime.hour)
            set(Calendar.MINUTE, bedTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Ensure the alarm is set for the future
            if (before(Calendar.getInstance())) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }


//        alarmManager.setExactAndAllowWhileIdle(
//            AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent
//        );
        preAlarmNotificationSchedule(
            context,
            dayOfWeek * 100,
            calendarBedTime.timeInMillis,
            bedTime
        )

        // Schedule the alarm
        /*alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
        )*/
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun preAlarmNotificationSchedule(
        context: Context,
        notificationId: Int,
        millis: Long,
        bedTime: LocalTime,
    ) {
        val beforeMillis = 60 * 60 * 1000L
        val title = context.getString(R.string.text_bedtime_reminder)

        val sleepTime = bedTime.format(DateTimeFormatter.ofPattern("hh:mm a")).lowercase()
        val message =
            context.getString(R.string.text_bed_time_message, sleepTime)

        val intent = Intent(context, WindDownNotification::class.java)

        intent.putExtra("titleExtra", title)
        intent.putExtra("messageExtra", message)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val scheduleTime = millis - beforeMillis

        if (scheduleTime > Calendar.getInstance().timeInMillis) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                millis - beforeMillis,
                pendingIntent
            )
        }
    }

    private fun cancelPreAlarmNotification(notificationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WindDownNotification::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId, // Use the same request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Calendar.MONDAY
     */
    fun cancelWeeklyAlarm(dayOfWeek: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            dayOfWeek, // Use the same request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAllAlarms() {
        val days = arrayListOf(
            Calendar.MONDAY, Calendar.TUESDAY,
            Calendar.WEDNESDAY, Calendar.THURSDAY,
            Calendar.FRIDAY, Calendar.SATURDAY,
            Calendar.SUNDAY
        )
        days.forEach {
            cancelWeeklyAlarm(it)
            cancelPreAlarmNotification(it * 100)
        }
    }

    companion object {
        fun getAlarmToneByKey(key: Int): Int {
            return when (key) {
                1 -> R.raw.track_1_lofi
                2 -> R.raw.track_2_thailand
                3 -> R.raw.track_3_singapore
                4 -> R.raw.track_4_scotland
                else -> R.raw.track_1_lofi
            }
        }
    }
}