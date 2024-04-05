package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.data.model.OreoSleepData

interface OreoNapDataSource {

    suspend fun insertData(
        data: List<OreoNapData>
    ): Boolean

    suspend fun insertSingle(
        data: OreoNapData
    ): Boolean

    suspend fun getNaps(): List<OreoNapData>?

    suspend fun removeNapById(id: Int)

    suspend fun deleteOldData(days: Int): Int


}