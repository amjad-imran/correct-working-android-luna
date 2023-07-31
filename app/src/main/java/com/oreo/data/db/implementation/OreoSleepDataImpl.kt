package com.oreo.data.db.implementation

import androidx.room.Transaction
import com.noisefit.data.local.db.abstraction.SleepDataSource
import com.noisefit.data.local.db.database.SleepDao
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.data.model.OreoSleepData
import com.oreo.data.db.abstaction.OreoSleepDataSource
import com.oreo.data.db.database.OreoSleepDao
import javax.inject.Inject

class OreoSleepDataImpl
@Inject
constructor(
    private val sleepDao: OreoSleepDao
) : OreoSleepDataSource {
    @Transaction
    override suspend fun insertData(data: OreoSleepData): Boolean {
        if (data.startTime == null || data.endTime == null || data.total == 0 || data.sleepArray.isNullOrEmpty()) {
            return false
        }
        val sleepDataExist =
            sleepDao.checkDataExist(startTime = data.startTime!!, endTime = data.endTime!!)

        // LOGS.d("SleepDataImpl ${Gson().toJson(data)}")

        if (sleepDataExist.isNullOrEmpty()) {

            val currentTimeStamp =
                DateFormats.convertDateTimeToTimeStamp2(data.date!!, DateFormats.getTimeFormat())
            val syncDate = DateFormats.convertTimeStampToStartOfDay(currentTimeStamp)

            data.timeStamp = syncDate
            sleepDao.insert(data)
        }
        return true
    }

    override suspend fun setHealthScore(score: Int, date: String) {
        val sleeps = sleepDao.getTodayData(date)
        if (sleeps.isNullOrEmpty()) return

        sleepDao.updateHealthScore(score,date)

    }

    override suspend fun getTodayData(date: String): List<OreoSleepData>? {
        return sleepDao.getTodayData(date)
    }

    override suspend fun getServerUnSyncData(): List<OreoSleepData>? {
        return sleepDao.getServerUnSyncData(false)
    }


    //can't think of a better solution
    override suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<OreoSleepData>? {

        return sleepDao.getUnSyncServerData(endDate, isSync)

    }

    override suspend fun deleteOldData(timeStamp: Long): Int {
        return sleepDao.deleteOlderData(timeStamp)
    }

    override suspend fun updateServerSyncData(dataList: List<OreoSleepData>, timeStamp: Long): Int {
        val ids = ArrayList<Int>()
        dataList.forEach { data ->
            ids.add(data.id)
        }

        return sleepDao.updateServerUnSyncStatus(ids, true)
    }
}
