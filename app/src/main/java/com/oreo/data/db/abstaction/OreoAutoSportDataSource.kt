package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoAutoSportData

interface OreoAutoSportDataSource {

    suspend fun getAllNotAcceptingData(): List<OreoAutoSportData>?

    suspend fun insertData(
        data: List<OreoAutoSportData>
    ): Boolean

    suspend fun deleteAllAutoSport(): Boolean

    suspend fun deleteAutoSport(id: Int): Boolean
}