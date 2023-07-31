package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.OreoStepsData

interface OreoStepsDataSource {

    suspend fun insertOrDelete(stepsData: OreoStepsData)
    suspend fun updateNullStepArray(stepArray: ArrayList<OreoStepsData.OreoStepDataBreakup>)
    suspend fun getTodayData(date: String): OreoStepsData?
    suspend fun syncInsertOrUpdate(stepsData: OreoStepsData): OreoStepsData?
    suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<OreoStepsData>?
    suspend fun deleteOldData(timeStamp: Long): Int
    suspend fun updateServerSyncData(dataList: List<OreoStepsData>): Int
}