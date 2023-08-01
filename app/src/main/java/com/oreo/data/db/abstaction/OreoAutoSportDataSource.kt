package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup

interface OreoAutoSportDataSource {

    suspend fun getAllNotAcceptingData(): List<OreoAutoSportData>?

    suspend fun insertData(
        data: List<OreoAutoSportData>
    ): Boolean
}