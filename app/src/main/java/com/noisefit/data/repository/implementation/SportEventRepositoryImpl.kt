package com.noisefit.data.repository.implementation

import com.google.gson.JsonObject
import com.noisefit.data.local.db.CacheResult
import com.noisefit.data.local.db.implementation.SportEventDataImpl
import com.noisefit_commans.data.model.matches.Matches
import com.noisefit_commans.data.model.matches.SportEvent
import com.noisefit.data.remote.abstraction.SportService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.SportEventRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.data.safeCacheCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

private const val timeZone = "Asia/Kolkata"

class SportEventRepositoryImpl(
    private val sportService: SportService,
    private val sportEventDataImpl: SportEventDataImpl,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SportEventRepository {


    override suspend fun currentMatches(
        action: String,
        from: String,
        to: String,
        leagueId: Int
    ): Flow<Resource<Matches>> {

        return safeApiCallFlow(dispatcher) {
            sportService.currentMatches(action, from, to, leagueId, timeZone)
        }
    }

    override suspend fun matchInfo(
        leagueId: Int,
        matchId: Int
    ): Flow<Resource<JsonObject>> {
        val met = "Livescore"
        return safeApiCallFlow(dispatcher) {
            sportService.matchInfo(met, matchId, timeZone, leagueId)
        }
    }

    override suspend fun saveSelectedSportEvent(sportEventList: ArrayList<SportEvent>?): Flow<CacheResult<Boolean?>> {
        return safeCacheCall(dispatcher) {
            sportEventDataImpl.saveSportEventList(sportEventList)
        }
    }

    override suspend fun getSelectedSportEvent(): Flow<CacheResult<List<SportEvent>?>> {
        return safeCacheCall(dispatcher) {
            sportEventDataImpl.getSportEventList()
        }
    }

    override suspend fun getSelectedSportEvent(date: String): Flow<CacheResult<SportEvent?>> {
        return safeCacheCall(dispatcher) {
            sportEventDataImpl.getSportEventList(date)
        }
    }

    override suspend fun getSelectedSportEvents(date: String): Flow<CacheResult<List<SportEvent>?>> {
        return safeCacheCall(dispatcher) {
            sportEventDataImpl.getSportEventsList(date)
        }
    }

    override suspend fun deleteSportEvent(eventId: String): Flow<CacheResult<Unit?>> {
        return safeCacheCall(dispatcher) {
            sportEventDataImpl.deleteEvent(eventId)
        }
    }
}