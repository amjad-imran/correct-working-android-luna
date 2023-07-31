package com.noisefit.data.local.db.implementation

import androidx.room.Transaction
import com.noisefit.data.local.db.abstraction.BodyTemperatureDataSource
import com.noisefit.data.local.db.database.BodyTemperatureDao
import com.noisefit_commans.models.BodyTemperatureBreakup
import javax.inject.Inject

class BodyTemperatureDataImpl
@Inject
constructor(
    private val bodyTemperatureDao: BodyTemperatureDao
) : BodyTemperatureDataSource {
    @Transaction
    override suspend fun insertData(data: List<BodyTemperatureBreakup>): Boolean {
        if (data.isNullOrEmpty() || data[0].date == null) {
            return false
        }
//        if (!data.isNullOrEmpty() && data[0].resetData) {
//            //delete data
//        }
        bodyTemperatureDao.insertAll(data)
        return true
    }

    override suspend fun getTodayData(date: String): List<BodyTemperatureBreakup>? {
        return bodyTemperatureDao.getTodayData(date)
    }



    override suspend fun getUnSyncServerData(endDate:Long,isSync:Boolean
    ): List<BodyTemperatureBreakup>? {
        return bodyTemperatureDao.getUnSyncServerData(endDate,isSync)
    }
    override suspend fun deleteOldData(timeStamp: Long): Int {
        return bodyTemperatureDao.deleteOlderData(timeStamp)
    }

    override suspend fun updateServerSyncData(
        dataList: List<BodyTemperatureBreakup>,
        timeStamp: Long
    ): Int {


        val chunkList = dataList.chunked(500)

        chunkList.forEach { chunk ->
            val ids = ArrayList<Int>()
            chunk.forEach { data ->
                ids.add(data.id)
            }
            //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
            bodyTemperatureDao.updateServerUnSyncStatus(ids, true, timeStamp)
        }


       return 1
    }

    override suspend fun checkHalfSyncData() {

        val halfSyncList = bodyTemperatureDao.checkHalfSyncData(false)
        val chunkList = halfSyncList?.chunked(500)

        chunkList?.forEach { dataList ->
            val dateList = ArrayList<String>()
            dataList.forEach { data ->
                dateList.add(data.date!!)
            }
            //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
            bodyTemperatureDao.updateHalfSyncData(false, dateList)
        }


    }
}
