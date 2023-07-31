package com.noisefit_commans.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.common.upToNDecimal
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class ChallengeListingResponse(
    @SerializedName("new") val new: List<com.noisefit_commans.data.response.ChallengeModel>?,
    @SerializedName("joined") val joined: List<com.noisefit_commans.data.response.ChallengeModel>?,
    @SerializedName("completed") val completed: List<com.noisefit_commans.data.response.ChallengeModel>?,
    @SerializedName("currentTime") val currentTime: String = ""
)

@Parcelize
data class ChallengeModel(
    var user_rank: Int? = 0,
    val challenge_id: Int,
    val image_url: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val description: String? = null,
    val detail: String? = null,
    val type: String? = null,
    val shareText: String? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    var currentTime: String? = null,
    val status: String? = null,
    val goal: Double? = 0.0,
    val condition: String? = null,
    var display_images: List<String> = ArrayList(),
    var participants: Long? = 0,
    val is_joined: String? = null,
    var disqualified_msg: String? = null,
    var disqualification_msg: String? = null,
    val progress: Float? = null,
    val current_average: Int? = null,
    val required_average: Int? = null,
    var is_achieved: String? = null,
    var completed_msg: String? = null,
    var awaited_msg: String? = null,
    val progress_avg: Float? = null,
    val rewards: List<com.noisefit_commans.data.response.Rewards>? = null,
    val leaderboard: List<com.noisefit_commans.data.response.LeaderBoard>? = null,
    var history: List<com.noisefit_commans.data.response.ChallengeHistory>? = null

) : Parcelable {
    fun isChallengeJoined(): Boolean {
        if (is_joined?.equals("1") == true) {
            return true
        }
        return false
    }

    fun getFormattedProgress(): String {
        return if (type.equals("distance", true)) {
            "$progress"
        } else {
            "${progress?.toInt()}"
        }

    }

    fun isDisqualified(): Boolean {
        if (!disqualified_msg.isNullOrEmpty()) {
            return true
        }
        return false
    }

    fun getStartEndData(): Pair<Pair<String, String>, Pair<String, String>> {
        //2022-09-20 09:36:06

        val sdfSource = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val sdfLocal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getDefault()
        }


        val date1: Date = sdfSource.parse(start_date)
        val date2: Date = sdfSource.parse(end_date)

        val calenderStart = Calendar.getInstance()
        calenderStart.time = sdfLocal.parse(sdfLocal.format(date1))
        calenderStart.set(Calendar.HOUR_OF_DAY, 0)
        calenderStart.set(Calendar.MINUTE, 0)
        calenderStart.set(Calendar.SECOND, 0)
        calenderStart.set(Calendar.MILLISECOND, 0)

        val calenderEnd = Calendar.getInstance()
        calenderEnd.time = sdfLocal.parse(sdfLocal.format(date2))
        calenderEnd.set(Calendar.HOUR_OF_DAY, 0)
        calenderEnd.set(Calendar.MINUTE, 0)
        calenderEnd.set(Calendar.SECOND, 0)
        calenderEnd.set(Calendar.MILLISECOND, 0)

        val startDate = Pair(
            SimpleDateFormat("dd").format(calenderStart.time),
            SimpleDateFormat("MMM yy").format(calenderStart.time).replace(" ", "'")
        )
        val endDate = Pair(
            SimpleDateFormat("dd").format(calenderEnd.time),
            SimpleDateFormat("MMM yy").format(calenderEnd.time).replace(" ", "'")
        )
        return Pair(startDate, endDate)
    }


    /**
     * Return in format "x Days/Day"
     * if day = 0 then today
     * if day = 1 then tomorrow
     */
    fun getStartsInData(): Pair<String, String> {
        val sdfSource = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val sdfLocal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getDefault()
        }

        val date1: Date = sdfSource.parse(start_date) as Date
        val current: Date = sdfSource.parse(currentTime) as Date

        val calenderStart = Calendar.getInstance()
        calenderStart.time = sdfLocal.parse(sdfLocal.format(date1))
        calenderStart.set(Calendar.HOUR_OF_DAY, 0)
        calenderStart.set(Calendar.MINUTE, 0)
        calenderStart.set(Calendar.SECOND, 0)
        calenderStart.set(Calendar.MILLISECOND, 0)

        val calenderCurrent = Calendar.getInstance()
        calenderCurrent.time = sdfLocal.parse(sdfLocal.format(current))
        calenderCurrent.set(Calendar.HOUR_OF_DAY, 0)
        calenderCurrent.set(Calendar.MINUTE, 0)
        calenderCurrent.set(Calendar.SECOND, 0)
        calenderCurrent.set(Calendar.MILLISECOND, 0)


        val difference =
            (TimeUnit.MILLISECONDS.toDays(calenderStart.timeInMillis - calenderCurrent.timeInMillis)).toInt()
        var differenceTxt = difference.toString()
        var daysText = ""
        when (difference) {
            0 -> {
                return Pair("0", "")
            }
            1 -> {
                differenceTxt = "Tomorrow"
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

    /**
     * Return in format "x Days/Day"
     * if day = 0 then today
     * if day = 1 then tomorrow
     */
    fun getEndsInData(): Pair<String, String> {
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

    fun getHistoryDays(): List<String> {
        val sdfSource = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val sdfLocal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getDefault()
        }


        val date1: Date = sdfSource.parse(start_date) as Date
        val current: Date = sdfSource.parse(end_date) as Date


        val calenderStart = Calendar.getInstance()
        calenderStart.time = sdfLocal.parse(sdfLocal.format(date1))
        calenderStart.set(Calendar.HOUR_OF_DAY, 0)
        calenderStart.set(Calendar.MINUTE, 0)
        calenderStart.set(Calendar.SECOND, 0)
        calenderStart.set(Calendar.MILLISECOND, 0)

        val calenderCurrent = Calendar.getInstance()
        calenderCurrent.time = sdfLocal.parse(sdfLocal.format(current))
        calenderCurrent.set(Calendar.HOUR_OF_DAY, 0)
        calenderCurrent.set(Calendar.MINUTE, 0)
        calenderCurrent.set(Calendar.SECOND, 0)
        calenderCurrent.set(Calendar.MILLISECOND, 0)

        val tempCal = calenderStart
        val returnResult = ArrayList<String>()
        while (tempCal <= calenderCurrent) {
            val sdfSourceRet = SimpleDateFormat("yyyy-MM-dd", DateFormats.defaultLocale)
            returnResult.add(sdfSourceRet.format(tempCal.timeInMillis))
            tempCal.add(Calendar.DAY_OF_MONTH, 1)
        }


        return returnResult
    }

    //TODO upto 2 decimal
    fun getTotalProgress(): String {
        if (history.isNullOrEmpty()) {
            return "0"
        }

        var totalCount = 0f
        history?.forEach { data ->
            totalCount += try {
                data.progress ?: 0f
            } catch (exp: Exception) {
                0f
            }
        }
        return totalCount.upToNDecimal(2).replace(".00", "")
    }

    fun getChallengeDisplayStatus(): String {
        return if (status.isNullOrEmpty()) {
            ""
        } else {
            if (status.equals("ongoing", true)) {
                "Started"
            } else if (status.equals("upcoming", true)) {
                "Exclusive"
            } else {
                status.capitalizeWords()
            }
        }
    }
}

@Parcelize
data class Rewards(
    val id: Int? = null,
    val rank: Int? = null,
    val reward_url: String? = null,
    val message: String? = null,
    val reward_title: String? = null
) : Parcelable

@Parcelize
data class LeaderBoard(
    val progress: Double? = null,
    val user_id: Int? = null,
    val user_rank: Int? = null,
    val first_name: String? = null,
    val image_url: String? = null
) : Parcelable

@Parcelize
data class ChallengeHistory(
    val date: String? = null,
    val progress: Float? = null
) : Parcelable