package com.noisefit.data.repository

import com.facebook.bolts.Task.Companion.delay
import com.google.gson.JsonObject
import com.noisefit_commans.data.model.BuddiesUser
import com.noisefit_commans.data.model.Buddy
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.BaseApiResponse
import com.noisefit_commans.data.response.MessageResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeBuddiesRepository : BuddiesRepository {

    private val buddiesList = mutableListOf<Buddy>()

    override suspend fun getTopBuddy(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<Buddy>>>> {
        TODO("Not yet implemented")
    }

    override suspend fun friendList(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<Buddy>>>> {
        return flow {
            delay(10)
            emit(Resource.Success(
                com.noisefit_commans.data.response.BaseApiResponse(
                    data = buddiesList,
                    message = ""
                )
            ))
        }
    }

    override suspend fun buddyAdd(id: String): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>> {
        TODO("Not yet implemented")
    }

    override suspend fun buddyInvite(id: String): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>> {
        TODO("Not yet implemented")
    }

    override suspend fun buddyRequestAction(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String>>> {
        TODO("Not yet implemented")
    }

    override suspend fun buddyRequests(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<Buddy>>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getNoiseFitContacts(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<List<com.noisefit_commans.data.model.BuddiesUser>?>>> {
        TODO("Not yet implemented")
    }

    override suspend fun addNoiseFitContacts(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<Any>>> {
        TODO("Not yet implemented")
    }

    override suspend fun nudgeUser(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String>>> {
        TODO("Not yet implemented")
    }
}