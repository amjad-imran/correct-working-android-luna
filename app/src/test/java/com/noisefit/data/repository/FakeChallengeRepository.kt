package com.noisefit.data.repository

import com.google.common.truth.Truth
import com.google.gson.Gson
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.data.repository.implementation.UserActivityRepositoryImpl
import com.noisefit.data.safeApiCallFlow
import com.noisefit.di.DependencyContainer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FakeChallengeRepository {
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    private val dependencyContainer: DependencyContainer = DependencyContainer()
    private val userActivityRepository: UserActivityRepository

    init {
        dependencyContainer.build()
        userActivityRepository = UserActivityRepositoryImpl(
            dependencyContainer.iOfflineApiResponseStore,
            dependencyContainer.networkService,
            dependencyContainer.lastSyncProvider,
            dependencyContainer.keyValueDataSource,
            Gson()
        )
    }

    @Test
    fun test_challengeById_success() = runBlocking {

        val serverResult = safeApiCallFlow(dispatcher) {
            userActivityRepository.getChallengeDetailByID(false, 891)
        }

        System.out.println("DEBUGdsadsadsa " )
        serverResult.collect { resource ->
            when (resource) {
                is Resource.GenericError -> {
                    val count = 6
                    Truth.assertThat(count).isEqualTo(5)
                }
                is Resource.Loading -> {

                }
                is Resource.NetworkError -> {
                    val count = 90
                    Truth.assertThat(count).isEqualTo(5)
                }
                is Resource.Success -> {

                    val count = 5
                    Truth.assertThat(count).isEqualTo(5)
                }
            }
        }

    }
}