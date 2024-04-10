package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoAutoSportData

interface OreoAutoSportDataSource {

    suspend fun getAllNotAcceptingData(startTimeStamp: Long): List<OreoAutoSportData>?

    suspend fun getWorkoutByTime(timeStamp: Long): OreoAutoSportData?

    suspend fun insertData(
        data: List<OreoAutoSportData>
    ): Boolean

    suspend fun markWorkoutSyncedAll(): Boolean

    suspend fun deleteAutoSport(id: Int): Boolean
    suspend fun deleteOldData(time: Long): Boolean
    suspend fun markWorkoutSynced(id: Int): Boolean
}