package com.noisefit.data.local.db.implementation

import androidx.room.Transaction
import com.noisefit.data.local.db.abstraction.HeartRateDataSource
import com.noisefit.data.local.db.database.HeartRateDao
import com.noisefit_commans.models.HeartRate
import javax.inject.Inject


class HeartRateDataImpl
@Inject
constructor(
    private val heartRateDao: HeartRateDao
) : HeartRateDataSource {

    @Transaction
    override suspend fun insertData(data: List<HeartRate>): Boolean {
        if (data.isNullOrEmpty() || data[0].date == null) {
            return false
        }
//        if (!data.isNullOrEmpty() && data[0].resetData) {
//            //delete data
//        }
        heartRateDao.insertAll(data)
        return true
    }


    override suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<HeartRate>? {
        return heartRateDao.getUnSyncServerData(endDate, isSync)
    }

    override suspend fun getTodayData(date: String): List<HeartRate>? {
        return heartRateDao.getTodayData(date)
    }

    override suspend fun deleteOldData(timeStamp: Long): Int {
        return heartRateDao.deleteOlderData(timeStamp)
    }

    override suspend fun updateServerSyncData(dataList: List<HeartRate>, timeStamp: Long): Int {
        val heartList = dataList.chunked(500)

        heartList.forEach { heartRateList ->
            val ids = ArrayList<Int>()
            heartRateList.forEach { data ->
                ids.add(data.id)
            }
            //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
            heartRateDao.updateServerUnSyncStatus(ids, true, timeStamp)
        }



        return 1
    }

    override suspend fun checkHalfSyncData() {


        val halfSyncList = heartRateDao.checkHalfSyncData(false)
        val heartList = halfSyncList?.chunked(500)

        heartList?.forEach { heartRateList ->
            val dateList = ArrayList<String>()
            heartRateList.forEach { data ->
                dateList.add(data.date!!)
            }
            //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
            heartRateDao.updateHalfSyncData(false, dateList)
        }



    }

    override suspend fun getHeartRateBetweenTimeStamp(
        startTimeStamp: Long,
        endTimeStamp: Long
    ): List<HeartRate>? {
       return heartRateDao.getHeartRateBetweenTimeStamp(startTimeStamp, endTimeStamp)
    }
}
