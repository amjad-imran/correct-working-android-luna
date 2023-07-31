package com.noisefit.data.local.db.implementation

import com.noisefit.data.local.db.abstraction.StepsDataSource
import com.noisefit.data.local.db.database.StepsDao
import com.noisefit_commans.models.StepsData
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject

class StepsDataImpl
@Inject
constructor(
    private val stepsDao: StepsDao
) : StepsDataSource {


    override suspend fun insertOrDelete(stepsData: StepsData) {

    }

    override suspend fun updateNullStepArray(stepArray: ArrayList<StepsData.StepDataBreakup>) {
        stepsDao.updateNullStepsArray(stepArray)
    }

    override suspend fun getTodayData(date: String): StepsData? {
        rescueTables()
        return stepsDao.getTodayData(date)
    }

    //If there is inconsistency in db tables and tables have been crashing then use this method to feed the tables.
    //1. For hybrid and endure, a little trick to feed steps db table if steps array data is null
    private fun rescueTables() {
        val stepArray = ArrayList<StepsData.StepDataBreakup>()
        for (i in 0..23) {
            stepArray.add(StepsData.StepDataBreakup(0, 0, 0, 0, i))
        }
        stepsDao.updateNullStepsArray(stepArray)
    }

    override suspend fun syncInsertOrUpdate(stepsData: StepsData): StepsData? {

        rescueTables()


        val existingSteps = stepsDao.getTodayData(stepsData.date!!)
        if (existingSteps != null) {
            if (existingSteps.totalSteps == stepsData.totalSteps && existingSteps.totalCalories == stepsData.totalCalories) {
                LOGS.d("syncInsertOrUpdate Same steps please ignore this call")
                return stepsDao.getTodayData(stepsData.date!!)
            }
            LOGS.d("syncInsertOrUpdate Please update}")
            val syncDate = DateFormats.convertDateTimeToTimeStamp(
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
                is_synced = false
            )
        } else {
            LOGS.d("syncInsertOrUpdate create new}")
            //insert
            val syncDate = DateFormats.convertDateTimeToTimeStamp(
                stepsData.date!!,
                DateFormats.getTimeFormat()
            )
            stepsData.timeStamp = syncDate
            stepsDao.insert(stepsData)
        }

        return stepsDao.getTodayData(stepsData.date!!)
    }

    override suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<StepsData>? {
        rescueTables()
        return stepsDao.getUnSyncServerData(endDate, isSync)
    }

    override suspend fun deleteOldData(timeStamp: Long): Int {
        return stepsDao.deleteOlderData(timeStamp)
    }

    override suspend fun updateServerSyncData(dataList: List<StepsData>, timeStamp: Long): Int {
        val ids = ArrayList<Int>()
        dataList.forEach { data ->
            ids.add(data.id)
        }

//        LOGS.d("deleteServerSyncData stepsIds ${Gson().toJson(ids)}")

        return stepsDao.updateServerUnSyncStatus(ids, true, timeStamp)
    }

}
