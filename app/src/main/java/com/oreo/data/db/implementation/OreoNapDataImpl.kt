package com.oreo.data.db.implementation

import androidx.room.Transaction
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.data.model.OreoSleepData
import com.oreo.data.db.abstaction.OreoNapDataSource
import com.oreo.data.db.abstaction.OreoSleepDataSource
import com.oreo.data.db.database.OreoNapDao
import com.oreo.data.db.database.OreoSleepDao
import javax.inject.Inject

class OreoNapDataImpl
@Inject
constructor(
    private val napDao: OreoNapDao
) : OreoNapDataSource {


    @Transaction
    override suspend fun insertData(data: List<OreoNapData>): Boolean {
        napDao.insertAll(data)
        return true
    }

}
