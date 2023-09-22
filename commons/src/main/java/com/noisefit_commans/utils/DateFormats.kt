package com.noisefit_commans.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import com.noisefit_commans.models.TimeFormat
import com.noisefit_commans.models.TimeFormats
import org.joda.time.DateTime
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*
import java.util.concurrent.TimeUnit


object DateFormats {
    val defaultLocale = Locale.ENGLISH

    private const val PastSyncData = 4
    private const val TAG = "DateFormats"

    @SuppressLint("ConstantLocale")
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateFormat2 = SimpleDateFormat("dd MMMM yyyy", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateFormat3 = SimpleDateFormat("yyyy-MM-dd", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateFormat4 = SimpleDateFormat("MMM yyyy", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateFormat5 = SimpleDateFormat("yyyyMMdd", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateFormat6 = SimpleDateFormat("dd MMMM, yyyy", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateFormat7 = SimpleDateFormat("dd MMM", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateTimeFormatWithoutZone = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateTimeFormat5 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateTimeFormat6 = SimpleDateFormat("yyyy-MM-dd HH:mm", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateTimeFormat2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", defaultLocale)


    @SuppressLint("ConstantLocale")
    val dateTimeFormat4 = SimpleDateFormat("h:mm a, dd MMM yyyy", defaultLocale)

    @SuppressLint("ConstantLocale")
    val monthDateWithoutYear = SimpleDateFormat("MM/dd", defaultLocale)

    @SuppressLint("ConstantLocale")
    val monthDateWithoutYear2 = SimpleDateFormat("MMM dd", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateTimeFormatWithWeekDay = SimpleDateFormat("EEEE, dd MMMM yyyy", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateTimeFormatWithWeekInit = SimpleDateFormat("EEEE, dd MMM yyyy", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateTimeFormatWithWeekWithoutYear = SimpleDateFormat("EEEE, dd MMMM", defaultLocale)
    val dateTimeFormatWithWeekWithoutYearShort = SimpleDateFormat("EEE, dd MMMM", defaultLocale)

    @SuppressLint("ConstantLocale")
    val singleWeekDay = SimpleDateFormat("EEEEE", defaultLocale)

    @SuppressLint("ConstantLocale")
    val shortWeekDay = SimpleDateFormat("EEE", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateTimeFormatISO =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateTimeFormat3 =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", defaultLocale)

    @SuppressLint("ConstantLocale")
    val timeFormat = SimpleDateFormat("HH:mm", defaultLocale)

    @SuppressLint("ConstantLocale")
    val timeFormat12 = SimpleDateFormat("hh:mm a", defaultLocale)


    @SuppressLint("ConstantLocale")
    val timeFormatSleepTime = SimpleDateFormat("yyyy-MM-dd HH:mm", defaultLocale)

    @SuppressLint("ConstantLocale")
    val timeWithSecond = SimpleDateFormat("HH:mm:ss", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateTimeFormatWfTranser = SimpleDateFormat(" dd-MM-yy HH:mm:ss", defaultLocale)

    @SuppressLint("ConstantLocale")
    val time12Meridian = SimpleDateFormat("h:mm a", defaultLocale).apply {
        timeZone = TimeZone.getDefault()
    }

    @SuppressLint("ConstantLocale")
    val timeFormat2 = SimpleDateFormat("h a", defaultLocale)

    @SuppressLint("ConstantLocale")
    val dateTime12Meridian = SimpleDateFormat("dd MMM , h:mm a", defaultLocale).apply {
        timeZone = TimeZone.getDefault()
    }


    fun getDefaultUnitFormats(context: Context): TimeFormat {
        var timeFormat = TimeFormats.HOURS_12
        if (DateFormat.is24HourFormat(context)) {
            timeFormat = TimeFormats.HOURS_24
        }
        return TimeFormat(timeFormat.type)
    }


    fun addMinutes(dateWithTime: String, duration: Int): String? {
        try {

            val d = dateTimeFormat3.parse(dateWithTime)
            LOGS.d("addMinutes $d")
            val cal = Calendar.getInstance()
            cal.time = d
            cal.add(Calendar.SECOND, duration)
            LOGS.d("addMinutes ${cal.time} $duration")
            return time12Meridian.format(cal.time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }

    fun addMinutes2(dateWithTime: String, duration: Int): String? {
        try {

            val d = dateTimeFormatISO.parse(dateWithTime)
            LOGS.d("addMinutes $d")
            val cal = Calendar.getInstance()
            cal.time = d
            cal.add(Calendar.SECOND, duration)
            LOGS.d("addMinutes ${cal.time} $duration")
            return time12Meridian.format(cal.time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }

    fun subtractDate(date: String, subtractDay: Int): String? {
        val cal = Calendar.getInstance()
        cal.time = dateFormat.parse(date)
        cal.add(Calendar.DATE, -subtractDay)
        return dateFormat.format(cal.time)
    }

    fun subtractDateFormat3(date: String, subtractDay: Int): String? {
        val cal = Calendar.getInstance()
        cal.time = dateFormat3.parse(date)
        cal.add(Calendar.DATE, -subtractDay)
        return dateFormat3.format(cal.time)
    }

    fun addDateFormat3(date: String, addDay: Int): String? {
        val cal = Calendar.getInstance()
        cal.time = dateFormat3.parse(date)
        cal.add(Calendar.DATE, addDay)
        return dateFormat3.format(cal.time)
    }


    fun subtractDate(timeStamp: Long, subtractDay: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeStamp
        cal.add(Calendar.DATE, -subtractDay)
        return cal.timeInMillis
    }

    //    fun getCurrentDateTime(): String {
//        val calendar = Calendar.getInstance()
//        return dateTimeFormatWithoutZone.format(calendar.time)
//    }


    //get current date
    fun getCurrentDate(): String {
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return df.format(c)
    }

    fun getYesterdayDate(): String {
        val c = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.time

        return dateFormat3.format(c)
    }

    fun getDate(timeFormat: SimpleDateFormat): String {
        val c = Calendar.getInstance().time
        return timeFormat.format(c)
    }

    fun getCurrentMonth(): String {
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("MM", Locale.getDefault())
        return df.format(c)
    }

    /**
     * 2023-05-29
     */
    fun getCurrentDateOreoFormat(): String {
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return df.format(c)
    }

    fun getCurrentDate(dataFormat: SimpleDateFormat): String {
        val c = Calendar.getInstance().time
        return dataFormat.format(c)
    }


    fun calculateDayDifference(date1: String, date2: String): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy", defaultLocale)
        val dateOne = sdf.parse(date1)
        val dateTwo = sdf.parse(date2)
        // To calculate the time difference of two dates
        val differenceInTime = dateOne!!.time - dateTwo!!.time;
        val differenceInDays = differenceInTime / (1000 * 3600 * 24)

        return if (differenceInDays.toInt() == 0)
            "Today"
        else
            "$differenceInDays Days ago"
    }


    fun getLogDateFormatForColorPro(): String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("yyyyMMdd", defaultLocale).format(calendar.time)
    }

    fun formatDateTime(date: Date, format: SimpleDateFormat): String {
        return try {
            format.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }


    fun formatDateTime(
        inputDate: String?,
        inputDateFormat: SimpleDateFormat,
        outputDateFormat: SimpleDateFormat
    ): String {
        try {
            if (inputDate.isNullOrEmpty()) return ""
            val date = inputDateFormat.parse(inputDate) ?: return ""
            return outputDateFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun getDefaultBirthDate(format: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -10)
        when (format) {
            1 -> {
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            2 -> {
                val dateFormat = SimpleDateFormat("dd", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            3 -> {
                val dateFormat = SimpleDateFormat("MMMM", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            4 -> {
                val dateFormat = SimpleDateFormat("yyyy", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            6 -> {
                val dateFormat = SimpleDateFormat("MM", defaultLocale)
                return dateFormat.format(calendar.time)
            }
        }
        return ""
    }

    fun getCurrentHour(): Int {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR, 1)
        //        if (hour == 0) {
//            hour = 24;
//        }
        return cal[Calendar.HOUR_OF_DAY]
    }

    /**
     * @param format 1 -> 01 January 2021
     * 2 -> 01
     * 3 -> January
     * 4 -> 2021
     */
    @SuppressLint("SimpleDateFormat")
    fun getTodaysDateString(format: Int): String {
        val calendar = Calendar.getInstance()
        when (format) {
            1 -> {
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", defaultLocale)
                return dateFormat.format(calendar.time)

            }

            2 -> {
                val dateFormat = SimpleDateFormat("dd", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            3 -> {
                val dateFormat = SimpleDateFormat("MMMM", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            4 -> {
                val dateFormat = SimpleDateFormat("yyyy", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            5 -> {
                val dateFormat = SimpleDateFormat("h:mm a, dd/MM/yyyy", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            6 -> {
                val dateFormat = SimpleDateFormat("MM", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            7 -> {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            8 -> {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd h:mm:ss a", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            9 -> {
                return dateFormat3.format(calendar.time)
            }

            10 -> {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            11 -> {
                val dateFormat = SimpleDateFormat("MMM yyyy", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            12 -> {
                val dateFormat = SimpleDateFormat("yyyy", defaultLocale)
                return dateFormat.format(calendar.time)
            }

            13 -> {
                return dateTimeFormatWithWeekDay.format(calendar.time)
            }

            14 -> {
                val dateFormat = SimpleDateFormat("EEEE, d MMMM", defaultLocale)
                return dateFormat.format(calendar.time)
            }
        }
        return ""
    }


    fun formatTimeInto24HoursValue(time: String?): String {
        return try {
            if (time.isNullOrEmpty()) return ""
            val dateFormatInput = SimpleDateFormat("HH:mm", defaultLocale)
            val dateFormatOutput = SimpleDateFormat("HH", defaultLocale)
            val date = dateFormatInput.parse(time) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun formatTimeInto24HoursValue(time: String?, dateFormatInput: SimpleDateFormat): String {
        return try {
            if (time.isNullOrEmpty()) return ""
            val dateFormatOutput = SimpleDateFormat("HH", defaultLocale)
            val date = dateFormatInput.parse(time) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun convert24HourTo12(time: String?, simpleDateFormat: SimpleDateFormat): String {
        return try {
            if (time.isNullOrEmpty()) return ""

            val dateFormatOutput = SimpleDateFormat("hh:mm a", defaultLocale)
            val date = simpleDateFormat.parse(time) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun convertTimeIntoTime(
        time: String?,
        current: SimpleDateFormat,
        requested: SimpleDateFormat
    ): String {
        return try {
            if (time.isNullOrEmpty()) return ""
            val date = current.parse(time) ?: return ""
            requested.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun convert24HourTo12(time: String?): String {
        return try {
            if (time.isNullOrEmpty()) return ""
            val dateFormatInput = SimpleDateFormat("HH:mm", defaultLocale)
            val dateFormatOutput = SimpleDateFormat("hh:mm a", defaultLocale)
            val date = dateFormatInput.parse(time) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun formatDateWithWeekDayYear(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatInput = SimpleDateFormat("yyyy-MM-dd", defaultLocale)
            val date = dateFormatInput.parse(dateInput) ?: return ""
            dateTimeFormatWithWeekDay.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun formatWeeklyDateMonthWithYear(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatInput = SimpleDateFormat("yyyy-MM-dd", defaultLocale)
            val dateFormatOutput = SimpleDateFormat("dd MMM yyyy", defaultLocale)
            val date = dateFormatInput.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun getMonth(month: Int): String {

        when (month) {
            0 -> {
                return "Jan"
            }

            1 -> {
                return "Feb"
            }

            2 -> {
                return "Mar"
            }

            3 -> {
                return "Apr"
            }

            4 -> {
                return "May"
            }

            5 -> {
                return "Jun"
            }

            6 -> {
                return "Jul"
            }

            7 -> {
                return "Aug"
            }

            8 -> {
                return "Sep"
            }

            9 -> {
                return "Oct"
            }

            10 -> {
                return "Nov"
            }

            11 -> {
                return "Dec"
            }

            else -> {
                return ""
            }
        }
    }

    fun getCompleteMonthName(month: Int): String {

        when (month) {
            0 -> {
                return "January"
            }

            1 -> {
                return "February"
            }

            2 -> {
                return "March"
            }

            3 -> {
                return "April"
            }

            4 -> {
                return "May"
            }

            5 -> {
                return "June"
            }

            6 -> {
                return "July"
            }

            7 -> {
                return "August"
            }

            8 -> {
                return "September"
            }

            9 -> {
                return "October"
            }

            10 -> {
                return "November"
            }

            11 -> {
                return "December"
            }

            else -> {
                return ""
            }
        }
    }

    fun formatMonthly(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatInput = SimpleDateFormat("yyyy-MM-dd", defaultLocale)
            val dateFormatOutput = SimpleDateFormat("dd/MM", defaultLocale)
            val date = dateFormatInput.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun formatDate(
        dateInput: String?,
        currentFormat: SimpleDateFormat,
        requestedFormat: SimpleDateFormat
    ): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val date = currentFormat.parse(dateInput) ?: return ""
            requestedFormat.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun formatWeek(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatInput = SimpleDateFormat("yyyy-MM-dd", defaultLocale)
            val dateFormatOutput = SimpleDateFormat("EEEEE", defaultLocale)
            val date = dateFormatInput.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun shortFormatWeek(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatInput = SimpleDateFormat("yyyy-MM-dd", defaultLocale)
            val dateFormatOutput = SimpleDateFormat("EEE", defaultLocale)
            val date = dateFormatInput.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun formatWeek2(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatInput = SimpleDateFormat("yyyy-MM-dd", defaultLocale)
            val dateFormatOutput = SimpleDateFormat("EEE", defaultLocale)
            val date = dateFormatInput.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun formatWeeklyDateMonth(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatInput = SimpleDateFormat("yyyy-MM-dd", defaultLocale)
            val dateFormatOutput = SimpleDateFormat("dd MMM", defaultLocale)
            val date = dateFormatInput.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun getMonthName(month: Int): String {
        return DateFormatSymbols(defaultLocale).months[month]
    }

    /**
     * @param birthDate format should be yyyy-MM-dd from server
     * @return Formatted date for displaying
     */
    fun formatBirthDateDisplay(birthDate: String?): String {
        return try {
            if (birthDate.isNullOrEmpty()) return ""
            val dateFormatInput = SimpleDateFormat("yyyy-MM-dd", defaultLocale)
            val dateFormatOutput = SimpleDateFormat("dd MMMM yyyy", defaultLocale)
            val date = dateFormatInput.parse(birthDate) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    /**
     * input format yyyy-MM-dd
     */
    fun formatActivityDate(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatInput = SimpleDateFormat("yyyy-MM-dd", defaultLocale)
            val dateFormatOutput = SimpleDateFormat("dd MMM, yyyy", defaultLocale)
            val date = dateFormatInput.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun formatActivityTime(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", defaultLocale)
            val dateFormatOutput = SimpleDateFormat("h:mm a", defaultLocale)
            val date = dateFormatInput.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun formatActivityTime2(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatOutput = SimpleDateFormat("MMM dd yyyy, h:mm a", defaultLocale)
            val date = dateTimeFormatISO.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun formatActivityTime3(dateInput: String?): String {
        return try {

            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatOutput = SimpleDateFormat("h:mm a", DateFormats.defaultLocale)
            val date = dateTimeFormat3.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            exp.printStackTrace()
            ""
        }
    }

    fun formatActivityTime8(dateInput: String?): String {
        return try {

            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatOutput = SimpleDateFormat("h:mm a", DateFormats.defaultLocale)
            val date = timeWithSecond.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            exp.printStackTrace()
            ""
        }
    }

    fun formatActivityTime6(dateInput: String?): String {
        return try {

            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatOutput = SimpleDateFormat("h:mm a", DateFormats.defaultLocale)
            val date = dateTimeFormatISO.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            exp.printStackTrace()
            ""
        }
    }

    fun formatActivityTime4(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatOutput = SimpleDateFormat("EEEE, MMMM dd", defaultLocale)
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val date = input.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun formatActivityTime7(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatOutput = SimpleDateFormat("EEE, dd/MM/yy", defaultLocale)
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val date = input.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun formatActivityTime5(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatInput = SimpleDateFormat("dd:MM:yyyy HH:mm:ss", defaultLocale)
            val dateFormatOutput = SimpleDateFormat("h:mm a", defaultLocale)
            val date = dateFormatInput.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }


    fun getAlarmDate(hour: Int, minute: Int): String {
        val inputTime = "$hour:$minute"
        val simpleDateFormat = SimpleDateFormat("H:mm", defaultLocale)
        val date = simpleDateFormat.parse(inputTime)
        return SimpleDateFormat("h:mm a", defaultLocale).format(date)
    }


    fun getReminderDate(month: Int, day: Int, year: Int): String {
        val inputDate = "$day/$month/$year"
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", defaultLocale)
        val date = simpleDateFormat.parse(inputDate)
        return SimpleDateFormat("MMM dd,yyyy", defaultLocale).format(date)
    }

    fun get24HourFrom12(hour: Int, isAm: Boolean): Int? {
        val inputDate = "$hour ${if (isAm) "AM" else "PM"}"
        val dateFormat12 = SimpleDateFormat("hh a", defaultLocale)

        return try {
            val date = dateFormat12.parse(inputDate)

            val dateFormat24 = SimpleDateFormat("HH", defaultLocale)
            dateFormat24.format(date).toIntOrNull()
        } catch (exp: Exception) {
            null
        }
    }

    /**
     * @param Hour in 24 hour format
     * @param minute in 24 hour format
     */
    fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        val simpleDateFormat = SimpleDateFormat("HH:mm", defaultLocale)
        return simpleDateFormat.format(calendar.time)
    }

    /**
     * @param Hour in 24 hour format
     * @param minute in 24 hour format
     */
    fun formatTimeWithAmPm(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        val simpleDateFormat = SimpleDateFormat("h:mm a", defaultLocale)
        return simpleDateFormat.format(calendar.time)
    }


    /**
     * @param dateString
     * @param format
     */
    fun isTodayDate(dateString: String, format: String): Boolean {
        val dateFormat = SimpleDateFormat(format, defaultLocale)
        val date: Date? = dateFormat.parse(dateString)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return if (date != null) {
            date.time == calendar.time.time
        } else {
            false
        }
    }


    fun convertDateTimeToTimeStamp(date: String, simpleDateFormat: SimpleDateFormat): Long? {

        try {
            val mDate = simpleDateFormat.parse(date)
            return mDate.time

        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null

    }

    fun convertDateTimeToTimeStampUTC(date: String, simpleDateFormat: SimpleDateFormat): String? {

        try {
            val sdfSource = simpleDateFormat.apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            val mDate = sdfSource.parse(date)
            return getRelativeTime(mDate.time)

        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null

    }

    private fun getSimpleDateFormat(format: String?): SimpleDateFormat {
        return SimpleDateFormat(format, defaultLocale)
    }

    fun getDateFromMillis(milliSeconds: Long, dateFormat: String?): String? {
        val formatter = getSimpleDateFormat(dateFormat)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }


    fun addSecondToTimeStamp(timestamp: Long, second: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.SECOND, second)
        return calendar.timeInMillis
    }

    fun addMinuteToTimeStamp(timestamp: Long, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.MINUTE, minute)
        return calendar.timeInMillis
    }

    fun getDateFormat(date: Date): String {
        return dateFormat.format(date)
    }

    fun getDateFormat(): String {
        val calendar = Calendar.getInstance()
        return dateFormat.format(calendar.time)
    }

    fun getTimeFormat(): String {
        val calendar = Calendar.getInstance()
        return timeFormat.format(calendar.time)
    }

    fun getDateFormatFromString(date: String): Date? {
        return dateFormat.parse(date)
    }

    fun getDateFormatFromString2(date: String): Date? {
        return dateFormat3.parse(date)
    }

    fun getTimeStamp(): Long {
        val calendar = Calendar.getInstance()
        return calendar.timeInMillis
    }

    fun convertDateTimeToTimeStamp(date: String, time: String): Long {
        return SimpleDateFormat("dd/MM/yyyyHH:mm", defaultLocale).parse("$date$time").time

    }

    fun convertDateTimeToTimeStamp2(date: String, time: String): Long {
        return SimpleDateFormat("yyyy-MM-ddHH:mm", defaultLocale).parse("$date$time").time

    }

    fun convertDateTimeToTimeStamp3(dateTime: String): Long {
        return dateTimeFormat5.parse(dateTime).time

    }

    fun convertDateTimeToTimeStamp(
        simpleDateFormat: SimpleDateFormat,
        date: String,
        time: String
    ): Long {
        return SimpleDateFormat("dd/MM/yyyyHH:mm", defaultLocale).parse("$date$time").time

    }

    fun convertDateToTimeStamp(date: String): Long {
        return SimpleDateFormat("dd/MM/yyyy", defaultLocale).parse("$date").time

    }

    fun convertDateTimeToTimeStampWithISO(date: String): Long {
        return dateTimeFormatISO.parse(date).time

    }

    fun startOfDayTimeStamp(): Long {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0 //set hours to zero
        cal[Calendar.MINUTE] = 0 // set minutes to zero
        cal[Calendar.SECOND] = 0 //set seconds to zero
        return cal.timeInMillis
    }

    fun addHourInMilliseconds(timeInMilliseconds: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeInMilliseconds
        cal.add(Calendar.HOUR, 4)
        return cal.timeInMillis
    }

    fun addSecondsInMilliseconds(timeInMilliseconds: Long, second: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeInMilliseconds
        cal.add(Calendar.SECOND, second)
        return cal.timeInMillis
    }

    fun addMinutesInMilliseconds(timeInMilliseconds: Long, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeInMilliseconds
        cal.add(Calendar.MINUTE, minute)
        return cal.timeInMillis
    }

    fun lastClearDataTimeStamp(): Long {
        val currentTimeStamp = getTimeStamp()
        return convertTimeStampToStartOfDay(subtractDate(currentTimeStamp, PastSyncData))
    }

    fun getNDayStartingTimeStamp(): Long {
        val currentTimeStamp = getTimeStamp()
        return convertTimeStampToStartOfDay(subtractDate(currentTimeStamp, PastSyncData))
    }


    fun checkUnSyncDaysGreaterThanNDays(lastTimestamp: Long): Long {
        val currentTimeStamp = getTimeStamp()
        if (lastTimestamp == 0L) {
            return convertTimeStampToStartOfDay(currentTimeStamp)
        }

        LOGS.d(TAG, "lastTimestamp $lastTimestamp")
        LOGS.d(TAG, "currentTimeStamp $currentTimeStamp")
        val days = getDateDiff(currentTimeStamp, lastTimestamp)
        LOGS.d(TAG, "days $days")
        if (days <= PastSyncData) {
            return convertTimeStampToStartOfDay(lastTimestamp)
        }
        //LOGS.d(TAG,"new time stamp ${convertTimeStampToStartOfDay(subtractDate(currentTimeStamp,7))}")
        return convertTimeStampToStartOfDay(subtractDate(currentTimeStamp, PastSyncData))
    }

    fun checkSleepUnSyncDaysGreaterThanNDays(lastTimestamp: Long): Long {
        val currentTimeStamp = subtractDate(getTimeStamp(), 1)
        if (lastTimestamp == 0L) {
            return convertTimeStampToPrevious12ofDay(currentTimeStamp)
        }

        LOGS.d(TAG, "lastTimestamp :: $lastTimestamp")
        LOGS.d(TAG, "currentTimeStamp :: $currentTimeStamp")
        val days = getDateDiff(currentTimeStamp, lastTimestamp)
        LOGS.d(TAG, "days :: $days")
        if (days <= PastSyncData) {
            return convertTimeStampToPrevious12ofDay(subtractDate(lastTimestamp, 1))
        }
        //LOGS.d(TAG,"new time stamp ${convertTimeStampToStartOfDay(subtractDate(currentTimeStamp,7))}")
        return convertTimeStampToPrevious12ofDay(subtractDate(currentTimeStamp, PastSyncData))
    }

    fun convertTimeStampToPrevious12ofDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal[Calendar.HOUR_OF_DAY] = 12 //set hours to zero
        cal[Calendar.MINUTE] = 0 // set minutes to zero
        cal[Calendar.SECOND] = 0 //set seconds to zero
        return cal.timeInMillis
    }

    fun convertTimeStampToStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal[Calendar.HOUR_OF_DAY] = 0 //set hours to zero
        cal[Calendar.MINUTE] = 0 // set minutes to zero
        cal[Calendar.SECOND] = 0 //set seconds to zero
        return cal.timeInMillis
    }


    fun convertTimestampToDate(timestamp: Long, formatted: SimpleDateFormat): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return formatted.format(calendar.time).toString()
    }

    fun isTimeBefore(time: String, endTime: String): Boolean {
        try {
            val date1 = timeFormat.parse(time)
            val date2 = timeFormat.parse(endTime)
            return date1.before(date2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    //11.28 04.00 11.59
    fun isTimeBetween(time: String, startTime: String, endTime: String): Boolean {
        try {
            val date1 = timeFormat.parse(time)
            val date2 = timeFormat.parse(startTime)
            val date3 = timeFormat.parse(endTime)
            LOGS.d("currentTime $date1 $date2 $date3")
            return (date1.after(date2) && date1.before(date3)) ||
                    (date1.equals(date2) || date1.equals(date3))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false

    }


    fun isTimeAfter(time: String, endTime: String): Boolean {
        try {
            val date1 = timeFormat.parse(time)
            val date2 = timeFormat.parse(endTime)
            return date1.after(date2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    fun getStartAndEndWeek(week: Int, year: Int): String {
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
        return "$startDateInStr - $endDaString ${getMonth(month - 1)}"
    }

    fun getConvertToDateFormat(
        date: String,
        currentFormat: SimpleDateFormat,
        requiredFormat: SimpleDateFormat
    ): String? {
        val dateInPreviousFormat = currentFormat.parse(date) ?: return null
        return requiredFormat.format(dateInPreviousFormat)

    }

    private fun getDateDiff(currentTimeStamp: Long, lastTimestamp: Long): Long {
        return TimeUnit.DAYS.convert(
            currentTimeStamp - lastTimestamp,
            TimeUnit.MILLISECONDS
        )
    }

    fun getDateDiff(format: SimpleDateFormat, oldDate: String?, newDate: String?): Long {
        LOGS.d("getDateDiff $oldDate")
        return try {
            TimeUnit.DAYS.convert(
                format.parse(newDate).time - format.parse(oldDate).time,
                TimeUnit.MILLISECONDS
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            0
        }
    }

    fun getDateDiffHours(format: SimpleDateFormat, oldDate: String?, newDate: String?): Long {
        LOGS.d("getDateDiff $oldDate")
        return try {
            TimeUnit.HOURS.convert(
                format.parse(newDate).time - format.parse(oldDate).time,
                TimeUnit.MILLISECONDS
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            0
        }
    }

    fun getCalendarFromDate(inputDate: String?, inputDateFormat: SimpleDateFormat): Calendar {
        val calendar = Calendar.getInstance()
        try {
            if (inputDate.isNullOrEmpty()) return calendar
            val date = inputDateFormat.parse(inputDate) ?: return calendar
            calendar.time = date
            return calendar
        } catch (e: Exception) {
            e.printStackTrace()
            return calendar
        }
    }

    fun Long.checkDayDifferenceMoreOne(): Boolean {
        val lastSyncDate = DateFormats.convertTimestampToDate(
            this,
            DateFormats.dateFormat
        )
        LOGS.d("checkDayDifferenceMoreOne $lastSyncDate")
        val difference =
            DateFormats.getDateDiff(
                DateFormats.dateFormat,
                lastSyncDate,
                DateFormats.getTodaysDateString(7)
            ).toInt()
        LOGS.d("checkDayDifferenceMoreOne $difference")
        if (difference > 0) {
            return true
        }

        return false
    }

    /**
     * @param checkDifferenceHours in hours
     */
    fun Long.checkTimeDifferenceMoreThanN(checkDifferenceHours: Int): Boolean {
        LOGS.d("checkTimeDifferenceMoreThanN $this")
        val difference = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - this)
        LOGS.d("checkTimeDifferenceMoreThanN $difference")

        //Case when date is changed by user
        if (difference < 0) {
            return true
        }

        if (difference > checkDifferenceHours) {
            return true
        }

        return false
    }

    /**
     * @param checkDifferenceDays in Days
     */
    fun Long.checkTimeDifferenceMoreThanNDays(checkDifferenceDays: Int): Boolean {
        LOGS.d("checkTimeDifferenceMoreThanNDays $this")
        val difference = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - this)
        LOGS.d("checkTimeDifferenceMoreThanNDays $difference")

        //Case when date is changed by user
        if (difference < 0) {
            return false
        }

        if (difference >= checkDifferenceDays) {
            return true
        }

        return false
    }


    fun convertTimeStampToHourOfDay(timestamp: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal[Calendar.HOUR_OF_DAY]
    }

    fun isMatchOnGoing(longTime: Long): Boolean {
        val currentTimeStamp = getTimeStamp()
        val cal = Calendar.getInstance()
        cal.timeInMillis = longTime
        cal.add(Calendar.HOUR, 3)

        LOGS.d("isMatchOnGoing $currentTimeStamp")
        if (currentTimeStamp > longTime) {
            return true
        }
        return false
    }

    fun getRelativeTime(timestamp: Long): String {
        val currentTimeStamp = Calendar.getInstance().timeInMillis
        val relativeTime = if (timestamp + 60000 > currentTimeStamp) {
            "Just Now"
        } else {
            DateUtils.getRelativeTimeSpanString(
                timestamp,
                currentTimeStamp,
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
        }
        return relativeTime

    }


    fun getDateFromString(time: String?, duration: Long?): String {
        if (duration == null)
            return ""
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val output = SimpleDateFormat("dd:MM:yyyy HH:mm:ss", Locale.getDefault())
        var dateTemp: Date? = null
        try {
            dateTemp = input.parse(time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val cal = Calendar.getInstance()
        cal.time = dateTemp
        cal.add(Calendar.SECOND, duration.toInt())
        val currentDatePlusOne = cal.time
        return output.format(currentDatePlusOne)


    }


    private fun getTime(time: Int): String {
        if (time < 10) {
            return "0$time"
        }
        return time.toString()
    }

    fun checkDifferenceInBtwInterval(
        hour1: Int,
        minute1: Int,
        hour2: Int,
        minute2: Int,
        frequencyInMinutes: Int
    ): Boolean {


        val startTime = "${getTime(hour1)}:${getTime(minute1)}"
        val endTime = "${getTime(hour2)}:${getTime(minute2)}"


        val date1: Date = DateFormats.timeFormat.parse(endTime)
        val date2: Date = DateFormats.timeFormat.parse(startTime)
        val mills = date1.time - date2.time
        LOGS.d("$TAG ${date1.time}")
        LOGS.d("$TAG ${date2.time}")
        val hours = (mills / (1000 * 60 * 60)).toInt()
        val mins = (mills / (1000 * 60)).toInt() % 60
        val totalMinutes = hours * 60 + mins
        LOGS.d("$TAG $startTime $endTime $mins ${frequencyInMinutes} ${hours} $totalMinutes")
        if (totalMinutes >= frequencyInMinutes) {
            return true
        }

        return false
    }

    /**
     * @return 0 if same, -1 is hour1 is less than hour2, 1 if hour1 more than hour2
     */
    fun compareTime(hour1: Int, minute1: Int, hour2: Int, minute2: Int): Int {
        val cal1 = Calendar.getInstance().apply {
            this.set(Calendar.HOUR_OF_DAY, hour1)
            this.set(Calendar.MINUTE, minute1)
        }
        val cal2 = Calendar.getInstance().apply {
            this.set(Calendar.HOUR_OF_DAY, hour2)
            this.set(Calendar.MINUTE, minute2)
        }
        return cal1.compareTo(cal2)
    }

    fun checkStartTimeLess(
        time: String,
        endTime: String,
        simpleDateFormat: SimpleDateFormat
    ): Boolean {

        try {
            val date1 = simpleDateFormat.parse(time)
            val date2 = simpleDateFormat.parse(endTime)
            if (date1 != null) {
                return date1.before(date2)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    fun getHistoryMonths(historyYears: Int): Array<String> {
        val list = ArrayList<String>()

        val monthDate = SimpleDateFormat("MMM-yyyy")
        val cal: Calendar = Calendar.getInstance()
        for (i in 1..(historyYears * 12)) {
            val monthName: String = monthDate.format(cal.time)
            list.add(monthName)
            cal.add(Calendar.MONTH, -1)
        }
        return list.toTypedArray()
    }

    fun getHistoryYear(historyYears: Int): Array<String> {
        val list = ArrayList<String>()

        val date = SimpleDateFormat("yyyy")
        val cal: Calendar = Calendar.getInstance()

        val hYear = historyYears - 1
        val year: String = date.format(cal.time)
        list.add(year)

        for (i in 1..hYear) {
            cal.add(Calendar.YEAR, -1)
            list.add(date.format(cal.time))
        }

        return list.toTypedArray()
    }

    fun getStartEndDateDay(selectedDate: String): Pair<String, String> {
        var startDate: String? = null
        var endDate: String? = null
        val format = "yyyy-MM-dd"
        val df = SimpleDateFormat(format)
        val date: Date = df.parse(selectedDate)
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        endDate = selectedDate
        cal.add(Calendar.DATE, -6)
        startDate = df.format(cal.time)
        return Pair(startDate, endDate)
    }

    fun getStartEndDateWeek(selectedDate: String): Pair<String, String> {
        var startDate: String? = null
        var endDate: String? = null
        val format = "yyyy-MM-dd"
        val df = SimpleDateFormat(format)
        val date: Date = df.parse(selectedDate)
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        startDate = df.format(cal.time)
        cal.add(Calendar.DATE, 6)
        endDate = df.format(cal.time)
        return Pair(startDate, endDate)
    }

    fun getStartEndDateMonth(month: String): Pair<String, String> {
        var startDate: String? = null
        var endDate: String? = null
        val format = "MMM-yyyy"
        val returnFormat = SimpleDateFormat("yyyy-MM-dd")
        val df = SimpleDateFormat(format)
        val date: Date = df.parse(month)
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startDate = returnFormat.format(cal.time)
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.DATE, -1)
        endDate = returnFormat.format(cal.time)
        return Pair(startDate, endDate)
    }

    fun getStartEndDateYear(year: String): Pair<String, String> {
        var startDate: String? = null
        var endDate: String? = null
        val format = "yyyy"
        val returnFormat = SimpleDateFormat("yyyy-MM-dd")
        val df = SimpleDateFormat(format)
        val date: Date = df.parse(year)
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_YEAR, 1)
        startDate = returnFormat.format(cal.time)
        cal.add(Calendar.YEAR, 1)
        cal.add(Calendar.DATE, -1)
        endDate = returnFormat.format(cal.time)
        return Pair(startDate, endDate)
    }

    /**
     * Returns in format MMM-yyyy
     */
    fun getFormattedMonth(date: String?): String {
        if (date.isNullOrEmpty()) {
            return ""
        }
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", DateFormats.defaultLocale)
            val dateFormatMonth = SimpleDateFormat("MMM-yyyy", DateFormats.defaultLocale)
            val date = dateFormat.parse(date)
            val cal = Calendar.getInstance()
            cal.time = date
            dateFormatMonth.format(cal.time)
        } catch (exp: Exception) {
            ""
        }
    }

    /**
     * Returns in format yyyy
     */
    fun getFormattedYear(date: String?): String {
        if (date.isNullOrEmpty()) {
            return ""
        }
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", DateFormats.defaultLocale)
            val dateFormatMonth = SimpleDateFormat("yyyy", DateFormats.defaultLocale)
            val date = dateFormat.parse(date)
            val cal = Calendar.getInstance()
            cal.time = date
            dateFormatMonth.format(cal.time)
        } catch (exp: Exception) {
            ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(defaultLocale).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isAfterToday(date: LocalDate): Boolean {
        return date > LocalDate.now()
    }


    fun isAfterToday(date: DateTime): Boolean {
        return date > DateTime.now()
    }

    fun isTimeBetween(hourOfDay: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentHourOfDay = calendar[Calendar.HOUR_OF_DAY]

        LOGS.d("isTimeBetween $hourOfDay $currentHourOfDay")
        if (hourOfDay == currentHourOfDay) {
            LOGS.d("isTimeBetween yes")
            return true
        }
        LOGS.d("isTimeBetween no")
        return false

    }

    fun getEndsInData1(currentTime: String, end_date: String): Pair<String, String> {
        val sdfSource = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getTimeZone("IST")
        }

        val sdfLocal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getDefault()
        }


        val date1: Date = sdfSource.parse(end_date) as Date
        val current: Date = sdfSource.parse(currentTime) as Date


        val calenderEnd = Calendar.getInstance()
        calenderEnd.time = sdfLocal.parse(sdfLocal.format(date1))
        calenderEnd.set(Calendar.HOUR_OF_DAY, 0)
        calenderEnd.set(Calendar.MINUTE, 0)
        calenderEnd.set(Calendar.SECOND, 0)
        calenderEnd.set(Calendar.MILLISECOND, 0)

        val calenderCurrent = Calendar.getInstance()
        calenderCurrent.time = sdfLocal.parse(sdfLocal.format(current))
        calenderCurrent.set(Calendar.HOUR_OF_DAY, 23)
        calenderCurrent.set(Calendar.MINUTE, 59)
        calenderCurrent.set(Calendar.SECOND, 59)
        calenderCurrent.set(Calendar.MILLISECOND, 59)


        val difference =
            (TimeUnit.MILLISECONDS.toDays(calenderEnd.timeInMillis - calenderCurrent.timeInMillis)).toInt()

        var differenceTxt = difference.toString()
        var daysText = ""
        when (difference) {
            1 -> {
                differenceTxt = "Tomorrow"
            }

            0 -> {
                differenceTxt = "Today"
            }

            else -> {
                daysText = if (difference <= 1) {
                    "Day"
                } else {
                    "Days"
                }
            }
        }

        return Pair(differenceTxt, daysText)
    }

    fun getEndsInData(currentTime: String, end_date: String): Pair<String, String> {
        val sdfSource = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val sdfLocal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getDefault()
        }


        val date1: Date = sdfSource.parse(end_date) as Date
        val current: Date = sdfSource.parse(currentTime) as Date


        val calenderEnd = Calendar.getInstance()
        calenderEnd.time = sdfLocal.parse(sdfLocal.format(date1))
        calenderEnd.set(Calendar.HOUR_OF_DAY, 0)
        calenderEnd.set(Calendar.MINUTE, 0)
        calenderEnd.set(Calendar.SECOND, 0)
        calenderEnd.set(Calendar.MILLISECOND, 0)

        val calenderCurrent = Calendar.getInstance()
        calenderCurrent.time = sdfLocal.parse(sdfLocal.format(current))
        calenderCurrent.set(Calendar.HOUR_OF_DAY, 0)
        calenderCurrent.set(Calendar.MINUTE, 0)
        calenderCurrent.set(Calendar.SECOND, 0)
        calenderCurrent.set(Calendar.MILLISECOND, 0)


        val difference =
            (TimeUnit.MILLISECONDS.toDays(calenderEnd.timeInMillis - calenderCurrent.timeInMillis)).toInt()

        var differenceTxt = difference.toString()
        var daysText = ""
        when (difference) {
            1 -> {
                differenceTxt = "Tomorrow"
            }

            0 -> {
                differenceTxt = "Today"
            }

            else -> {
                daysText = if (difference <= 1) {
                    "Day"
                } else {
                    "Days"
                }
            }
        }

        return Pair(differenceTxt, daysText)
    }

    fun getPreviousWeek(num: Int): ArrayList<String> {
        val c: Calendar = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        c.add(Calendar.DATE, num * 7)
        val df: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val listDate = ArrayList<String>()
        for (i in 0..6) {
            listDate.add(df.format(c.time))
            c.add(Calendar.DAY_OF_MONTH, 1)
        }
        return listDate
    }

    fun getLastDayOfMonth(): String {
        val cal = Calendar.getInstance()
        val date = cal.getActualMaximum(Calendar.DATE)
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH) + 1

        val tempDate: String = if (date in 1..9) {
            "0$date"
        } else
            date.toString()
        val tempMonth: String = if (month in 1..9) {
            "0$month"
        } else
            month.toString()
        return "$year-$tempMonth-$tempDate"
    }

    fun getFirstDayOfMonth(): String {
        val cal = Calendar.getInstance()
        val date = cal.getActualMinimum(Calendar.DATE)
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH) + 1
        val tempDate: String = if (date in 1..9) {
            "0$date"
        } else
            date.toString()
        val tempMonth: String = if (month in 1..9) {
            "0$month"
        } else
            month.toString()
        return "$year-$tempMonth-$tempDate"
    }

    fun getCurrentDateYMDFormat(): String {
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return df.format(c)
    }

    fun getWeekDaysBetweenDates(
        dateString1: String,
        dateString2: String,
        simpleDateFormat: SimpleDateFormat,
        timeDateFormat: SimpleDateFormat
    ): List<String> {
        val dateList = ArrayList<String>()

        var date1: Date? = null
        var date2: Date? = null
        try {
            date1 = simpleDateFormat.parse(dateString1)
            date2 = simpleDateFormat.parse(dateString2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        while (!cal1.after(cal2)) {
            dateList.add(timeDateFormat.format(cal1.time).toString())
            cal1.add(Calendar.DATE, 1)
        }
        return dateList
    }


    fun getPreviousDateYMDFormat(): String {
        val sDate = getCurrentDateYMDFormat()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(sDate)
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DATE, -1)
        return dateFormat.format(calendar.time)
    }

    /*
    * convert timeStamp to Month date time format
    * */
    fun convertTimeStampToDate(timeStamp: Long): String? {
        val dateFormat = "MMMM dd, yyyy 'at' h:mm a"

        val objFormatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        objFormatter.timeZone = TimeZone.getDefault()
        val objCalendar = Calendar.getInstance(TimeZone.getDefault())
        objCalendar.timeInMillis = timeStamp
        val result = objFormatter.format(objCalendar.time)
        objCalendar.clear()
        return result
    }

    /*
    * convert timeStamp to Month date time format
    * */
    fun convertTimeStampToDateOnly(timeStamp: Long): String? {
        val dateFormat = "dd MMMM, yyyy"

        val objFormatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        objFormatter.timeZone = TimeZone.getDefault()
        val objCalendar = Calendar.getInstance(TimeZone.getDefault())
        objCalendar.timeInMillis = timeStamp
        val result = objFormatter.format(objCalendar.time)
        objCalendar.clear()
        return result
    }

    fun formatServerTimeToMDYearFormat(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatOutput = SimpleDateFormat("MMMM dd, yyyy", defaultLocale)
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = input.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun formatTimeNpl(dateInput: String?): String {
        return try {
            if (dateInput.isNullOrEmpty()) return ""
            val dateFormatOutput = SimpleDateFormat("dd MMMM, yyyy", defaultLocale).apply {
                timeZone = TimeZone.getDefault()
            }
            val input =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            val date = input.parse(dateInput) ?: return ""
            dateFormatOutput.format(date)
        } catch (exp: Exception) {
            ""
        }
    }

    fun convertTimeStampIntoHrMin(time: Long): String {
        val comparedTime: String
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time * 1000
        val hours = cal[Calendar.HOUR_OF_DAY]
        val minutes = cal[Calendar.MINUTE]
        val formattedHour: String = when {
            hours > 9 -> {
                hours.toString()
            }

            else -> {
                "0${hours}"
            }
        }
        val formattedMin: String = when {
            minutes > 9 -> {
                minutes.toString()
            }

            else -> {
                "0${minutes}"
            }
        }
        comparedTime = "$formattedHour hr $formattedMin min"
        return comparedTime
    }

    /**
     * reutrns date in format yyyy-MM-dd
     */
    fun getDateFromTimeStamp(startTimeStamp: Long): String? {
        return try {
            val netDate = Date(startTimeStamp)
            dateFormat3.format(netDate)
        } catch (exp: Exception) {
            ""
        }
    }

    fun getDayElapsedMinutesFromTimeStamp(startTimeStamp: Long): Int? {
        return try {
            val netDate = Date(startTimeStamp)
            val hms = timeFormat.format(netDate)
            val splitData = hms.split(":")
            return (splitData[0].toInt() * 60 + splitData[1].toInt())
        } catch (exp: Exception) {
            0
        }
    }

    fun formatWfTransfer(timeStamp: Long): String? {
        dateTimeFormatWfTranser.timeZone = TimeZone.getDefault()
        val objCalendar = Calendar.getInstance(TimeZone.getDefault())
        objCalendar.timeInMillis = timeStamp
        val result = dateTimeFormatWfTranser.format(objCalendar.time)
        objCalendar.clear()
        return result
    }

    fun getActivityDisplayDates(startTime: String?, endTime: String?): String {
        if (startTime.isNullOrEmpty() || endTime.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", defaultLocale)
            val outputFormat = SimpleDateFormat("hh:mm a", defaultLocale)
            val start = inputFormat.parse(startTime)
            val end = inputFormat.parse(endTime)
            "${outputFormat.format(start)} - ${outputFormat.format(end)}"
        } catch (exp: Exception) {
            ""
        }
    }
}


/**
 * Input dd/MM/YYYY
 */
fun String.convertToMMDDYYYY(): String {
    return try {
        if (this.isEmpty()) return ""
        val dateFormatOutput = SimpleDateFormat("MM/dd/yyyy", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getDefault()
        }
        val input =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
        val date = input.parse(this) ?: return ""
        dateFormatOutput.format(date)
    } catch (exp: Exception) {
        ""
    }
}

/**
 * Input dd/MM/YYYY
 */
fun String.convertToYYYY_MM_DD(): String {
    return try {
        if (this.isEmpty()) return ""
        val dateFormatOutput = SimpleDateFormat("MM/dd/yyyy", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getDefault()
        }
        val input =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
        val date = input.parse(this) ?: return ""
        dateFormatOutput.format(date)
    } catch (exp: Exception) {
        ""
    }
}

fun String.to12HourFormat(): String {
    return try {
        LocalTime.parse(this, DateTimeFormatter.ofPattern("HH:mm"))
            .format(DateTimeFormatter.ofPattern("hh:mm a"))
    } catch (exp: Exception) {
        ""
    }
}