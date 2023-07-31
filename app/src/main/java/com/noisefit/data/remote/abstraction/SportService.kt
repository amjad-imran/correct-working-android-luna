package com.noisefit.data.remote.abstraction

import com.google.gson.JsonObject
import com.noisefit_commans.data.model.matches.Matches
import retrofit2.http.GET
import retrofit2.http.Query

//https://allsportsapi.com/cricket-api-documentation
interface SportService {

    @GET("/sports/league")
    suspend fun currentMatches(
        @Query("met") action: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("leagueId") leagueId: Int,
        @Query("timezone") timezone: String
    ): Matches

    @GET("/sports/livescore")
    suspend fun matchInfo(
        @Query("met") action: String,
        @Query("matchId") matchId: Int,
        @Query("timezone") timezone: String,
        @Query("leagueId") leagueId: Int
    ): JsonObject


}