package com.noisefit_commans.data.model.matches.score

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.data.model.matches.Comments


data class MatchesResultScore(
    @SerializedName("event_key")
    @Expose
    var eventKey: String? = null,
    @SerializedName("event_date_start")
    @Expose
    var eventDateStart: String? = null,
    @SerializedName("event_date_stop")
    @Expose
    var eventDateStop: String? = null,
    @SerializedName("event_time")
    @Expose
    var eventTime: String? = null,
    @SerializedName("event_home_team")
    @Expose
    var eventHomeTeam: String? = null,
    @SerializedName("home_team_key")
    @Expose
    var homeTeamKey: String? = null,
    @SerializedName("event_away_team")
    @Expose
    var eventAwayTeam: String? = null,
    @SerializedName("away_team_key")
    @Expose
    var awayTeamKey: String? = null,
    @SerializedName("event_service_home")
    @Expose
    var eventServiceHome: String? = null,
    @SerializedName("event_service_away")
    @Expose
    var eventServiceAway: String? = null,
    @SerializedName("event_home_final_result")
    @Expose
    var eventHomeFinalResult: String? = null,
    @SerializedName("event_away_final_result")
    @Expose
    var eventAwayFinalResult: String? = null,
    @SerializedName("event_home_rr")
    @Expose
    var eventHomeRr: String? = null,
    @SerializedName("event_away_rr")
    @Expose
    var eventAwayRr: String? = null,
    @SerializedName("event_status")
    @Expose
    var eventStatus: String? = null,
    @SerializedName("event_status_info")
    @Expose
    var eventStatusInfo: String? = null,
    @SerializedName("country_name")
    @Expose
    var countryName: String? = null,
    @SerializedName("league_name")
    @Expose
    var leagueName: String? = null,
    @SerializedName("league_key")
    @Expose
    var leagueKey: String? = null,
    @SerializedName("league_round")
    @Expose
    var leagueRound: String? = null,
    @SerializedName("league_season")
    @Expose
    var leagueSeason: String? = null,
    @SerializedName("event_live")
    @Expose
    var eventLive: String? = null,
    @SerializedName("event_type")
    @Expose
    var eventType: String? = null,
    @SerializedName("event_toss")
    @Expose
    var eventToss: String? = null,
    @SerializedName("event_man_of_match")
    @Expose
    var eventManOfMatch: String? = null,
    @SerializedName("event_stadium")
    @Expose
    var eventStadium: String? = null,
    @SerializedName("event_home_team_logo")
    @Expose
    var eventHomeTeamLogo: String? = null,
    @SerializedName("event_away_team_logo")
    @Expose
    var eventAwayTeamLogo: String? = null,
    @SerializedName("batterPresentAtCrease")
    @Expose
    var batterPresentAtCrease: String = "",
    @SerializedName("overs")
    @Expose
    var overs: String = "0.0",
    @SerializedName("is_home_team_batting")
    @Expose
    var isHomeTeamBatting: Boolean = false,
    @SerializedName("inning")
    @Expose
    var inning: Int = 1,
    @SerializedName("comments")
    @Expose
    var comment: Comments? = null
)