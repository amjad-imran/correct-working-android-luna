package com.noisefit.util

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.noisefit.data.local.db.CacheResult
import com.noisefit_commans.data.model.matches.SportEvent
import com.noisefit_commans.data.model.matches.score.MatchesResultScore
import com.noisefit.data.repository.abstraction.SportEventRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
private const val NO_EVENT_FOR_TODAY = "NO_EVENT_FOR_TODAY"

class SportUtils
@Inject
constructor(
    private val localDataStore: DataStoredInterface,
    private val sessionManager: SessionManager,
    private val sportEventRepository: SportEventRepository
) {

    private val TAG = "SportUtils"

    private fun isHomeTeamPlaying(playingTeam: String, homeTeam: String?): Boolean {
        var homeTeamPlaying = false
        if (homeTeam.isNullOrEmpty()) {
            return homeTeamPlaying
        }
        if (playingTeam.contains(homeTeam, true)) {
            homeTeamPlaying = true
        }
        return homeTeamPlaying
    }

    fun parseJsonObject(resultObject: JsonElement): MatchesResultScore {
        val matchesResultScore = MatchesResultScore()

        matchesResultScore.eventKey = resultObject.asJsonObject.get("event_key")?.asString
        matchesResultScore.eventDateStart =
            resultObject.asJsonObject.get("event_date_start")?.asString
        matchesResultScore.eventDateStop =
            resultObject.asJsonObject.get("event_date_stop")?.asString
        matchesResultScore.eventTime = resultObject.asJsonObject.get("event_time")?.asString
        matchesResultScore.eventHomeTeam =
            resultObject.asJsonObject.get("event_home_team")?.asString
        matchesResultScore.homeTeamKey = resultObject.asJsonObject.get("home_team_key")?.asString
        matchesResultScore.eventAwayTeam =
            resultObject.asJsonObject.get("event_away_team")?.asString
        matchesResultScore.awayTeamKey = resultObject.asJsonObject.get("away_team_key")?.asString

        matchesResultScore.eventServiceHome =
            resultObject.asJsonObject.get("event_service_home")?.asString
        matchesResultScore.eventServiceAway =
            resultObject.asJsonObject.get("event_service_away")?.asString
        matchesResultScore.eventHomeFinalResult =
            resultObject.asJsonObject.get("event_home_final_result")?.asString
        matchesResultScore.eventAwayFinalResult =
            resultObject.asJsonObject.get("event_away_final_result")?.asString
        matchesResultScore.eventStatus = resultObject.asJsonObject.get("event_status")?.asString
        matchesResultScore.eventStatusInfo =
            resultObject.asJsonObject.get("event_status_info")?.asString
//        matchesResultScore.countryName =
//            resultObject.asJsonObject.get("country_name")?.asString ?: ""
//        matchesResultScore.leagueRound = resultObject.asJsonObject.get("league_round")?.asString

        matchesResultScore.eventLive = resultObject.asJsonObject.get("event_live")?.asString
//        matchesResultScore.eventToss = resultObject.asJsonObject.get("event_toss")?.asString
        matchesResultScore.eventStadium = resultObject.asJsonObject.get("event_stadium")?.asString
        matchesResultScore.eventType = resultObject.asJsonObject.get("event_type")?.asString


        val scorecardMap = resultObject.asJsonObject.get("scorecard").asJsonObject.entrySet()
        if (scorecardMap.isNotEmpty()) {
            val lastData = scorecardMap.last()
            LOGS.d("$TAG inning ${scorecardMap.size}")
            matchesResultScore.inning = scorecardMap.size
            matchesResultScore.isHomeTeamBatting =
                isHomeTeamPlaying(lastData.key, matchesResultScore.eventHomeTeam)
            for (scorecardObject in lastData.value.asJsonArray) {
                val status =
                    scorecardObject.asJsonObject.get("status")?.asString?.lowercase()?.trim()
                if (!status.isNullOrEmpty() && status.equals("not out", true)) {
                    val name = scorecardObject.asJsonObject.get("player")?.asString
                    if (!name.isNullOrEmpty()) {
                        var run = scorecardObject.asJsonObject.get("R")?.asString
                        //  var ball = scorecardObject.asJsonObject.get("B")?.asString
                        if (run.isNullOrEmpty()) {
                            run = "0"
                        }
//                        if (ball.isEmpty()) {
//                            ball = "0"
//                        }

                        matchesResultScore.batterPresentAtCrease += "$name($run) "
                    }

                }

            }
        }


        val commentObject = resultObject.asJsonObject.get("comments")?.asJsonObject
        if (commentObject?.has("Live") == true) {
            val liveArray = commentObject.getAsJsonArray("Live")
            if (!liveArray.isEmpty) {
                val lastEntry = liveArray.first()
                val overs =
                    lastEntry.asJsonObject.get("overs")?.asString
                if (!overs.isNullOrEmpty()) {
                    matchesResultScore.overs = overs
                }

            }
        }
        LOGS.d("$TAG  ${Gson().toJson(matchesResultScore)}")
        return matchesResultScore
    }

    private fun getUpcomingEvent() {
        val date = DateFormats.getTodaysDateString(9)
        GlobalScope.launch {
            sportEventRepository.getSelectedSportEvent(date).collect { resource ->
                when (resource) {
                    is CacheResult.Success -> {
                        if (resource.value != null) {
                            LOGS.d("$TAG ${Gson().toJson(resource.value)}")
                            sessionManager.upcomingSportEvent = resource.value
                        } else {
                            //seems like no events for today, setting an empty dummy data
                            val sportEvent = SportEvent(
                                0,
                                NO_EVENT_FOR_TODAY,
                                0,
                                date,
                                "",
                                "",
                                ""
                            )
                            sportEvent.lastCheckInDb = DateFormats.getTimeStamp()
                            sessionManager.upcomingSportEvent = sportEvent

                        }
                    }
                    is CacheResult.GenericError -> {
                        LOGS.d("$TAG ${resource.errorMessage}")

                    }
                }
            }
        }
    }

    fun checkWhetherSportLiveOrNot(): Boolean {
//        val apiCallIntervalInMilliseconds = 2/*localDataStore.getSportEvenApiCallInterval()*/ * 60000
//        val lastSyncTime = localDataStore.getLastSportApiTimeStamp()
//
//        if (kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime) > apiCallIntervalInMilliseconds) {
//            LOGS.d(TAG, "checkSportWork bingo let's throw some score to the watch")
//            return true
//        }


        val sportEventsEnabled = localDataStore.isEnableIpl()
        if (!sportEventsEnabled) {
            LOGS.d("$TAG sports event disabled. What a waste of the feature!!")
            return false
        }

        val matchInfo = sessionManager.upcomingSportEvent
        //check for upcoming event in session manager, if its null then query in db
        if (matchInfo == null) {
            getUpcomingEvent()
            AppLogs.sendAppLogs("$TAG upcomingSportEvent is null. let check in db first and ignore this call")
            LOGS.d("$TAG upcomingSportEvent is null. let check in db first and ignore this call")
            return false
        } else if (matchInfo.date != DateFormats.getTodaysDateString(9)) {
            sessionManager.upcomingSportEvent = null
            getUpcomingEvent()
            AppLogs.sendAppLogs("$TAG  reset event, invalid date found")
            LOGS.d("$TAG reset event, invalid date found")
            return false
        } else if (matchInfo.eventId == NO_EVENT_FOR_TODAY || matchInfo.eventId.isEmpty()) {
            AppLogs.sendAppLogs("$TAG might be there is no events for today or waiting for db result!!")
            LOGS.d("$TAG might be there is no events for today or waiting for db result!!")

            val currentTimeStamp = DateFormats.getTimeStamp()
            val timeAfter30Minutes = DateFormats.addMinuteToTimeStamp(matchInfo.lastCheckInDb, 30)

            if (currentTimeStamp > timeAfter30Minutes) {
                LOGS.d("$TAG fallback, incase user added a match but was not pick up by session manager")
                sessionManager.upcomingSportEvent = null
                getUpcomingEvent()
            }

            return false
        }

        val apiCallIntervalInMilliseconds = localDataStore.getSportEvenApiCallInterval() * 60000
        val lastSyncTime = localDataStore.getLastSportApiTimeStamp()

        AppLogs.sendAppLogs("$TAG check for condition")
        LOGS.d(
            TAG,
            "checkSportWork called ${abs(DateFormats.getTimeStamp() - lastSyncTime)} ${
                Gson().toJson(matchInfo)
            } ${matchInfo.time} ${DateFormats.getTimeStamp()}"
        )
        if (kotlin.math.abs(DateFormats.getTimeStamp() - lastSyncTime) > apiCallIntervalInMilliseconds &&
            (DateFormats.getTimeStamp() > matchInfo.timeInMilliseconds)
        ) {
            AppLogs.sendAppLogs("$TAG checkSportWork bingo let's throw some score to the watch")
            LOGS.d(TAG, "checkSportWork bingo let's throw some score to the watch")
            return true
        }

        return false
    }


//    private fun getOvers(matchResult: MatchesResultScore): String {
//        if (!matchResult.comment?.live.isNullOrEmpty()) {
//            val live = matchResult.comment?.live!![0]
//            return "(${live.overs} overs)"
//        }
//        return ""
//    }

    fun getFinishedMessage(matchResult: MatchesResultScore): String {
        var message = ""
//        val overs = matchResult.overs
        var homeScore = 0
        var awayScore = 0
        val homeTeam = getTeam(matchResult.homeTeamKey!!)
        val awayTeam = getTeam(matchResult.awayTeamKey!!)
        val homeScoreInText = matchResult.eventHomeFinalResult ?: "0"
        val awayScoreInText = matchResult.eventAwayFinalResult ?: "0"
        if (homeScoreInText != "0") {
            val homeScoreArray = homeScoreInText.split("/")
            if (homeScoreArray.isNotEmpty()) {
                homeScore = homeScoreArray[0].toInt()
            }
        }
        if (awayScoreInText != "0") {
            val awayScoreArray = awayScoreInText.split("/")
            if (awayScoreArray.isNotEmpty()) {
                awayScore = awayScoreArray[0].toInt()
            }
        }

        if (homeScore > awayScore) {
            message += "$homeTeam $homeScoreInText\n $awayTeam $awayScoreInText $homeTeam edges past $awayTeam and clinches the victory"
        } else {
            message += "$awayTeam $awayScoreInText\n $homeTeam $homeScoreInText $awayTeam edges past $homeTeam and clinches the victory"
        }

        return message
    }

    fun getInningBreakMessage(matchResult: MatchesResultScore): String {
        var message = ""
//        val overs = matchResult.overs
        var homeScore = 0
        var awayScore = 0
        val homeTeam = getTeam(matchResult.homeTeamKey!!)
        val awayTeam = getTeam(matchResult.awayTeamKey!!)
        val homeScoreInText = matchResult.eventHomeFinalResult ?: "0"
        val awayScoreInText = matchResult.eventAwayFinalResult ?: "0"
        if (homeScoreInText != "0" && homeScoreInText.isNotEmpty()) {
            val homeScoreArray = homeScoreInText.split("/")
            if (homeScoreArray.isNotEmpty()) {
                homeScore = homeScoreArray[0].toInt()
            }

        }
        if (awayScoreInText != "0" && awayScoreInText.isNotEmpty()) {
            val awayScoreArray = awayScoreInText.split("/")
            if (awayScoreArray.isNotEmpty()) {
                awayScore = awayScoreArray[0].toInt()
            }
        }

        if (matchResult.inning == 1) {
            message = if (matchResult.isHomeTeamBatting) {
                getInningsBreakMessage(homeScore, homeTeam, awayTeam)
            } else {
                getInningsBreakMessage(awayScore, awayTeam, homeTeam)
            }

        }

        LOGS.d("$TAG inning_break $message $homeScore $awayScore")
        return message
    }

    private fun getInningsBreakMessage(score: Int, teamA: String, teamB: String): String {

        val message = when {
            score > 180 -> {
                "Explosive batting by $teamA. $teamB needs $score runs to win."
            }
            score < 130 -> {
                "A stellar bowling display batting by $teamB, needs $score runs to win."
            }
            else -> {
                "This could go either way. $teamB, needs $score runs to win."
            }
        }
        return message
    }

    fun getMessage(matchResult: MatchesResultScore): String {
        var message = ""
        val overs = matchResult.overs
        val homeScore = matchResult.eventHomeFinalResult ?: "0"
        val awayScore = matchResult.eventAwayFinalResult ?: "0"
        val homeTeam = getTeam(matchResult.homeTeamKey!!)
        val awayTeam = getTeam(matchResult.awayTeamKey!!)


        message = if (matchResult.inning == 1) {
            if (matchResult.isHomeTeamBatting) {
                "1st innings $homeTeam $homeScore | $overs overs | ${matchResult.batterPresentAtCrease}"
            } else {
                "1st innings $awayTeam $awayScore | $overs overs | ${matchResult.batterPresentAtCrease}"
            }

        } else {
            if (matchResult.isHomeTeamBatting) {
                "2nd innings $homeTeam $homeScore | $overs overs | ${matchResult.batterPresentAtCrease} | ${matchResult.eventStatusInfo}"
            } else {
                "2nd innings $awayTeam $awayScore | $overs overs |  ${matchResult.batterPresentAtCrease} | ${matchResult.eventStatusInfo}"
            }
        }

        return message
    }

    fun getTeam(code: String): String {
        when (code.toInt()) {
            141 -> {
                //"Chennai Super Kings",
                return "Chennai"
            }
            142 -> {
                //"Kolkata Knight Riders",
                return "Kolkata"
            }
            143 -> {
                //"Delhi Capitals",
                return "Delhi"
            }
            144 -> {
                //"Mumbai Indians",
                return "Mumbai"
            }
            145 -> {
                // "Kings XI Punjab",
                return "Punjab"
            }
            146 -> {
                //"Royal Challengers Bangalore",
                return "Bangalore"
            }
            147 -> {
                //"Gujarat Titans",
                return "Gujarat"
            }
            148 -> {
                // "Lucknow Super Giants",
                return "Lucknow"
            }
            149 -> {
                // "Sunrisers Hyderabad",
                return "Hyderabad"
            }
            150 -> {
                //"Rajasthan Royals",
                return "Rajasthan"
            }

        }
        return ""
    }

}