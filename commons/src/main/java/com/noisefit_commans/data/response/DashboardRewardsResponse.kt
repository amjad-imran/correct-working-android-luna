package com.noisefit_commans.data.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.utils.AppConstants
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt


data class DashboardRewardsResponse(
    val points: Long,
    @SerializedName("reward_awaiting")
    val rewardAwaiting: Boolean,
    val streak: Streaks,
    @SerializedName("prize_info")
    val prizeInfo: PrizeInfo? = null,
    @SerializedName("live_match")
    val liveMatch: LiveMatch? = null,
    @SerializedName("npl_league")
    val nplLeague: NplLeague? = null
)

data class Streaks(
    val curr_streak_length: Int? = 0,
    val previous_target_days: Int? = 0,
    val current_target_days: Int? = 0,
    val next_target_days: Int? = 0,
    val current_multiplier: Int? = 0,
    val next_multiplier: Int? = 0,
    val last_multiplier: Int? = 0,
    val msg: String? = null,
) {

    fun currentStreakProgress(): Int {
        if (curr_streak_length == 0) return 0
        if (current_target_days == 0) return 0
        return (((curr_streak_length ?: 1).toFloat() / (current_target_days
            ?: 1).toFloat()) * 100).roundToInt()
    }
}

@Parcelize
data class PrizeInfo(

    @SerializedName("required_wins")
    val rewardWins: Long? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("user_wins")
    val userWins: Long? = null,
    @SerializedName("matches_left")
    val matchesLeft: Long? = null,
    @SerializedName("eligible_count")
    val eligibleCount: Long? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("prize_name")
    val prizeName: String? = null,
    @SerializedName("message")
    val message: String? = null,
) : Parcelable

@Parcelize
data class NplLeague(

    @SerializedName("correct_ques")
    var correctQues: Int = 0,
    @SerializedName("total_ques")
    val totalQues: Int = 0,
    @SerializedName("ques_left")
    val quesLeft: Int = 0,
    @SerializedName("sponsor_image")
    val sponsorImage: String? = null,
    val title: String? = null,
    @SerializedName("reward")
    val reward: Int = 0,
    @SerializedName("message")
    val message: String? = null,
) : Parcelable


@Parcelize
data class LiveMatch(
    @SerializedName("start_at")
    var startAt: String? = null,
    @SerializedName("prediction_count")
    val predictionCount: Long? = null,
    val prediction_id: Long? = null,
    val match_id: Long? = null,
    @SerializedName("sponsor_image")
    val sponsorImage: String? = null,
    @SerializedName("user_team")
    val userTeam: Long? = null,
    @SerializedName("winning_team")
    val winningTeam: Long? = null,
    @SerializedName("match_status")
    val match_status: String? = null,
    @SerializedName("is_super_over")
    val isSuperOver: Boolean? = null,
    @SerializedName("team_a")
    val teamA: TeamInfo? = null,
    @SerializedName("team_b")
    val teamB: TeamInfo? = null,
    var elapsed_time: Long? = null,

    var userSelectedTeamId: Long? = null
) : Parcelable


@Parcelize
data class TeamInfo(
    @SerializedName("team_id")
    val teamId: Long? = null,
    @SerializedName("team_name")
    val teamName: String? = null,
    @SerializedName("short_name")
    val shortName: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("score")
    val score: TeamScore? = null,
    val meter: Double? = null
) : Parcelable {


}

@Parcelize
data class TeamScore(
    @SerializedName("run")
    val run: Long? = null,
    @SerializedName("wickets")
    val wickets: Long? = null,
    @SerializedName("over")
    val over: Double? = null,
) : Parcelable



