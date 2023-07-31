package com.noisefit_commans.data.response


data class StreakDetailsResponse(

    val curr_streak: CurrentStreak? = null,
    val previous_streaks: List<StreakDate>? = null,
    val longestStreakCount: Int? = 0,
    val current_date: String? = null,//YYYY-MM-DD
    val current_time: String? = null,//HH:mm:ss
    val msg: String? = null
)

data class StreakDate(
    val start_date: String? = null,//YYYY-MM-DD
    val end_date: String? = null//YYYY-MM-DD
)

data class CurrentStreak(
    val curr_streak_length: Int? = 0,
    val daily_target: Int? = 0,
    val current_multiplier: Int? = 0,
    val next_multiplier: Int? = 0,
    val levelUpIn: Int? = 0,
    val start_date: String? = null,//YYYY-MM-DD
    val milestone_dates: List<String>? = null//YYYY-MM-DD
)



