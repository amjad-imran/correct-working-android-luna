package com.noisefit_commans.data.model.matches

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MatchResult(
    var isHeader: Boolean = false,
    var isSelected: Boolean = false,
    val date: String? = null,
    @SerializedName("event_key")
    @Expose
    val eventKey: String? = null,
    @SerializedName("event_date_start")
    @Expose
    val eventDateStart: String? = null,
    @SerializedName("event_date_stop")
    @Expose
    val eventDateStop: String? = null,
    @SerializedName("event_time")
    @Expose
    val eventTime: String? = null,
    @SerializedName("event_home_team")
    @Expose
    val eventHomeTeam: String? = null,
    @SerializedName("home_team_key")
    @Expose
    val homeTeamKey: String? = null,
    @SerializedName("event_away_team")
    @Expose
    val eventAwayTeam: String? = null,
    @SerializedName("away_team_key")
    @Expose
    val awayTeamKey: String? = null,
    @SerializedName("event_service_home")
    @Expose
    val eventServiceHome: String? = null,
    @SerializedName("event_service_away")
    @Expose
    val eventServiceAway: String? = null,
    @SerializedName("event_home_final_result")
    @Expose
    val eventHomeFinalResult: String? = null,
    @SerializedName("event_away_final_result")
    @Expose
    val eventAwayFinalResult: String? = null,
    @SerializedName("event_home_rr")
    @Expose
    val eventHomeRr: String? = null,
    @SerializedName("event_away_rr")
    @Expose
    val eventAwayRr: String? = null,
    @SerializedName("event_status")
    @Expose
    val eventStatus: String? = null,
    @SerializedName("event_status_info")
    @Expose
    val eventStatusInfo: String? = null,
    @SerializedName("country_name")
    @Expose
    val countryName: String? = null,
    @SerializedName("league_name")
    @Expose
    val leagueName: String? = null,
    @SerializedName("league_key")
    @Expose
    val leagueKey: String? = null,
    @SerializedName("league_round")
    @Expose
    val leagueRound: String? = null,
    @SerializedName("league_season")
    @Expose
    val leagueSeason: String? = null,
    @SerializedName("event_live")
    @Expose
    val eventLive: String? = null,
    @SerializedName("event_type")
    @Expose
    val eventType: String? = null,
    @SerializedName("event_toss")
    @Expose
    val eventToss: String? = null,
    @SerializedName("event_man_of_match")
    @Expose
    val eventManOfMatch: String? = null,
    @SerializedName("event_stadium")
    @Expose
    var eventStadium: String? = null,
    @SerializedName("event_home_team_logo")
    @Expose
    val eventHomeTeamLogo: String? = null,
    @SerializedName("event_away_team_logo")
    @Expose
    val eventAwayTeamLogo: String? = null,
//    @SerializedName("comments")
//    @Expose
//    val comment: Comments? = null,
//    @SerializedName("comments")
//    @Expose
//    val comments: List<Comments>? = null
)