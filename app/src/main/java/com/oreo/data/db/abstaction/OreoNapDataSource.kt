package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.data.model.OreoSleepData

interface OreoNapDataSource {

    suspend fun insertData(
        data: List<OreoNapData>
    ): Boolean

}