package com.noisefit.data.repository.abstraction

import com.google.gson.JsonObject
import com.noisefit.data.local.db.CacheResult
import com.noisefit_commans.data.model.matches.Matches
import com.noisefit_commans.data.model.matches.SportEvent
import com.noisefit.data.remote.base.Resource
import kotlinx.coroutines.flow.Flow

interface SportEventRepository {

    suspend fun currentMatches(
        action: String,
        from: String,
        to: String,
        leagueId: Int
    ): Flow<Resource<Matches>>

    suspend fun matchInfo(
        leagueId: Int,
        matchId: Int
    ): Flow<Resource<JsonObject>>

    suspend fun saveSelectedSportEvent(
        sportEventList: ArrayList<SportEvent>?
    ): Flow<CacheResult<Boolean?>>

    suspend fun getSelectedSportEvent(): Flow<CacheResult<List<SportEvent>?>>

    suspend fun getSelectedSportEvent(date: String): Flow<CacheResult<SportEvent?>>

    suspend fun getSelectedSportEvents(date: String): Flow<CacheResult<List<SportEvent>?>>

    suspend fun deleteSportEvent(eventId: String): Flow<CacheResult<Unit?>>
}
