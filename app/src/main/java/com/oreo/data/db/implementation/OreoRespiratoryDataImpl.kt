package com.oreo.data.db.implementation

import androidx.room.Transaction
import com.google.gson.Gson
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.model.OreoBloodOxygenBreakup
import com.noisefit_commans.data.model.OreoRespiratoryData
import com.noisefit_commans.models.BloodOxygenBreakup
import com.oreo.data.db.abstaction.OreoBloodOxygenDataSource
import com.oreo.data.db.abstaction.OreoRespiratoryDataSource
import com.oreo.data.db.database.OreoBloodOxygenDao
import com.oreo.data.db.database.OreoRespiratoryDao
import javax.inject.Inject


class OreoRespiratoryDataImpl
@Inject
constructor(
    private val respiratoryDao: OreoRespiratoryDao
) : OreoRespiratoryDataSource {
    @Transaction
    override suspend fun insertData(data: OreoRespiratoryData): Boolean {
        if (data.date == null) {
            return false
        }

        val prevData = getTodayData(data.date!!)

        if (prevData == null) {
            respiratoryDao.insert(data)
        } else {
            val newBreakup = Gson().fromJson<List<Int>>(data.breakUp ?: "")
            val prevBreakup = Gson().fromJson<List<Int>>(prevData.breakUp ?: "")
            if (newBreakup.sum() != prevBreakup.sum()) {
                respiratoryDao.updateViaDate(data.breakUp ?: "", data.date!!, false)
            }
        }
        return true
    }


    override suspend fun getUnSyncServerData(
        endDate: Long, isSync: Boolean
    ): List<OreoRespiratoryData>? {
        return respiratoryDao.getServerUnSyncData(isSync)
    }

    override suspend fun deleteOldData(timeStamp: Long): Int {
        //return bloodOxygenDao.deleteOlderData(timeStamp)
        return 1
    }

    override suspend fun updateServerSyncData(
        dataList: List<OreoRespiratoryData>,
        timeStamp: Long
    ): Int {

        val ids = ArrayList<Int>()
        dataList.forEach { data ->
            ids.add(data.id)
        }
        return respiratoryDao.updateServerUnSyncStatus(ids, true)

    }


    override suspend fun getTodayData(date: String): OreoRespiratoryData? {
        return respiratoryDao.getTodayData(date)
    }

    override suspend fun checkHalfSyncData() {

/*
        val halfSyncList = bloodOxygenDao.checkHalfSyncData(false)
        val chunkList = halfSyncList?.chunked(500)

        chunkList?.forEach { dataList ->
            val dateList = ArrayList<String>()
            dataList.forEach { data ->
                dateList.add(data.date!!)
            }
            //  LOGS.d("updateServerSyncData ${Gson().toJson(ids)}")
            bloodOxygenDao.updateHalfSyncData(false, dateList)
        }*/
    }
}
