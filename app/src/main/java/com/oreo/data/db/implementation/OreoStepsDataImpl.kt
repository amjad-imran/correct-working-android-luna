package com.oreo.data.db.implementation

import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.data.model.OreoStepsData
import com.oreo.data.db.abstaction.OreoStepsDataSource
import com.oreo.data.db.database.OreoStepsDao
import javax.inject.Inject

class OreoStepsDataImpl
@Inject
constructor(
    private val stepsDao: OreoStepsDao
) : OreoStepsDataSource {


    override suspend fun insertOrDelete(stepsData: OreoStepsData) {

    }

    override suspend fun updateNullStepArray(stepArray: ArrayList<OreoStepsData.OreoStepDataBreakup>) {
        stepsDao.updateNullStepsArray(stepArray)
    }

    override suspend fun getTodayData(date: String): OreoStepsData? {
        rescueTables()
        return stepsDao.getTodayData(date)
    }

    //If there is inconsistency in db tables and tables have been crashing then use this method to feed the tables.
    //1. For hybrid and endure, a little trick to feed steps db table if steps array data is null
    private fun rescueTables() {
        val stepArray = ArrayList<OreoStepsData.OreoStepDataBreakup>()
        for (i in 0..23) {
            stepArray.add(OreoStepsData.OreoStepDataBreakup(0, 0, 0, 0, i))
        }
        stepsDao.updateNullStepsArray(stepArray)
    }

    override suspend fun syncInsertOrUpdate(stepsData: OreoStepsData): OreoStepsData? {

        rescueTables()


        val existingSteps = stepsDao.getTodayData(stepsData.date!!)
        if (existingSteps != null) {
            if (existingSteps.totalSteps == stepsData.totalSteps && existingSteps.totalCalories == stepsData.totalCalories
                && existingSteps.activeCalories == stepsData.activeCalories) {
                LOGS.d("syncInsertOrUpdate Same steps please ignore this call")
                return stepsDao.getTodayData(stepsData.date!!)
            }
            LOGS.d("syncInsertOrUpdate Please update}")
            val syncDate = DateFormats.convertDateTimeToTimeStamp2(
                existingSteps.date!!,
                DateFormats.getTimeFormat()
            )
            stepsDao.updateSteps(
                date = stepsData.date!!,
                totalSteps = stepsData.totalSteps,
                totalActiveTime = stepsData.totalActiveTime,
                totalCalories = stepsData.totalCalories,
                totalDistance = stepsData.totalDistance,
                hourOfTheDay = stepsData.hourOfTheDay ?: 0,
                stepArray = stepsData.stepArray,
                syncDate = syncDate,
                activeCalories = stepsData.activeCalories?:0,
                is_synced = false
            )
        } else {
            LOGS.d("syncInsertOrUpdate create new}")
            //insert
            val syncDate = DateFormats.convertDateTimeToTimeStamp2(
                stepsData.date!!,
                DateFormats.getTimeFormat()
            )
            stepsData.timeStamp = syncDate
            stepsDao.insert(stepsData)
        }

        return stepsDao.getTodayData(stepsData.date!!)
    }

    override suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<OreoStepsData>? {
        rescueTables()
        return stepsDao.getUnSyncServerData(isSync)
    }

    override suspend fun deleteOldData(timeStamp: Long): Int {
        return stepsDao.deleteOlderData(timeStamp)
    }

    override suspend fun updateServerSyncData(dataList: List<OreoStepsData>): Int {
        val ids = ArrayList<Int>()
        dataList.forEach { data ->
            ids.add(data.id)
        }
        return stepsDao.updateServerUnSyncStatus(ids, true)
    }

}
