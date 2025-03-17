package com.oreo.util

import android.content.Context
import android.text.format.DateUtils
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtil {


    fun getDurationMinutes(start: LocalTime, end: LocalTime): Long {
        return if (end.isAfter(start)) {
            Duration.between(start, end).toMinutes()
        } else {
            val dayEnd = LocalTime.of(23, 59)
            val dayStart = LocalTime.of(0, 0)
            Duration.between(start, dayEnd).toMinutes() + 1 + Duration.between(dayStart, end)
                .toMinutes()
        }
    }

    fun getTodayMidnightTimestamp(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }


    fun getRelativeTime(
        timestamp: Long,
        resourcesProvider: ResourcesProvider?,
        context: Context? = null
    ): String {
        val currentTimeStamp = Calendar.getInstance().timeInMillis
        val relativeTime = if (timestamp + 60000 > currentTimeStamp) {
            if (context != null) {
                context.getString(com.noisefit_commans.R.string.text_just_now)
            } else if (resourcesProvider != null) {
                resourcesProvider.getString(com.noisefit_commans.R.string.text_just_now)
            } else {
                ""
            }
        } else {
            DateUtils.getRelativeTimeSpanString(
                timestamp,
                currentTimeStamp,
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
        }
        return relativeTime

    }

    fun getStartAndEndWeek(week: Int, year: Int, resourcesProvider: ResourcesProvider): String {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.YEAR, year)
        //first day of week
        calendar.set(Calendar.WEEK_OF_YEAR, week)

        val formatter = SimpleDateFormat("dd", Locale.getDefault()) // PST`

        val firstDay = calendar.firstDayOfWeek
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        //calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val startDate: Date = calendar.time
        val startDateInStr = formatter.format(startDate)

        calendar.add(Calendar.DATE, 6)
        val enddate: Date = calendar.time
        val endDaString = formatter.format(enddate)

        val df = SimpleDateFormat("MM", Locale.getDefault())
        val month = df.format(enddate).toInt()
        return "$startDateInStr - $endDaString ${getMonth(month - 1, resourcesProvider)}"
    }


    fun getMonth(month: Int, resourcesProvider: ResourcesProvider): String {

        when (month) {
            0 -> {
                return resourcesProvider.getString(R.string.text_jan)
            }

            1 -> {
                return resourcesProvider.getString(R.string.text_feb)
            }

            2 -> {
                return resourcesProvider.getString(R.string.text_mar)
            }

            3 -> {
                return resourcesProvider.getString(R.string.text_apr)
            }

            4 -> {
                return resourcesProvider.getString(R.string.text_may)
            }

            5 -> {
                return resourcesProvider.getString(R.string.text_jun)
            }

            6 -> {
                return resourcesProvider.getString(R.string.text_jul)
            }

            7 -> {
                return resourcesProvider.getString(R.string.text_aug)
            }

            8 -> {
                return resourcesProvider.getString(R.string.text_sep)
            }

            9 -> {
                return resourcesProvider.getString(R.string.text_oct)
            }

            10 -> {
                return resourcesProvider.getString(R.string.text_nov)
            }

            11 -> {
                return resourcesProvider.getString(R.string.text_dec)
            }

            else -> {
                return ""
            }
        }
    }

    fun getCompleteMonthName(month: Int, resourcesProvider: ResourcesProvider): String {

        when (month) {
            0 -> {
                return resourcesProvider.getString(R.string.text_january)
            }

            1 -> {
                return resourcesProvider.getString(R.string.text_february)
            }

            2 -> {
                return resourcesProvider.getString(R.string.text_march)
            }

            3 -> {
                return resourcesProvider.getString(R.string.text_april)
            }

            4 -> {
                return resourcesProvider.getString(R.string.text_may)
            }

            5 -> {
                return resourcesProvider.getString(R.string.text_june)
            }

            6 -> {
                return resourcesProvider.getString(R.string.text_july)
            }

            7 -> {
                return resourcesProvider.getString(R.string.text_august)
            }

            8 -> {
                return resourcesProvider.getString(R.string.text_september)
            }

            9 -> {
                return resourcesProvider.getString(R.string.text_october)
            }

            10 -> {
                return resourcesProvider.getString(R.string.text_november)
            }

            11 -> {
                return resourcesProvider.getString(R.string.text_december)
            }

            else -> {
                return ""
            }
        }
    }


}