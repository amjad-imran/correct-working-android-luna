package com.noisefit.data.local.db.abstraction

import com.noisefit_commans.models.StepsData

interface StepsDataSource {

    suspend fun insertOrDelete(stepsData: StepsData)
    suspend fun updateNullStepArray(stepArray: ArrayList<StepsData.StepDataBreakup>)
    suspend fun getTodayData(date: String): StepsData?
    suspend fun syncInsertOrUpdate(stepsData: StepsData): StepsData?
    suspend fun getUnSyncServerData(endDate:Long,isSync:Boolean): List<StepsData>?
    suspend fun deleteOldData(timeStamp:Long):Int
    suspend fun updateServerSyncData(dataList: List<StepsData>, timeStamp: Long): Int
}