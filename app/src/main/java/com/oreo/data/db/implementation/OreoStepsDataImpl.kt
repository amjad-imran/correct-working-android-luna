package com.oreo.data.db.implementation

import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.data.model.OreoStepsData
import com.noisefit_commans.data.model.OreoStepsData.OreoStepDataBreakup
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
                && existingSteps.activeCalories == stepsData.activeCalories
            ) {
                LOGS.d("syncInsertOrUpdate Same steps please ignore this call")
                return existingSteps//stepsDao.getTodayData(stepsData.date!!)
            }
            LOGS.d("syncInsertOrUpdate Please update}")
            val syncDate = DateFormats.convertDateTimeToTimeStamp2(
                existingSteps.date!!,
                DateFormats.getTimeFormat()
            )

            val mergedData = getMergedData(existingSteps, stepsData)
            val newStepsTotal = if (stepsData.totalSteps >= existingSteps.totalSteps) {
                stepsData.totalSteps
            } else {
                existingSteps.totalSteps + stepsData.totalSteps
            }

            val newCaloriesTotal = if (stepsData.totalCalories >= existingSteps.totalCalories) {
                stepsData.totalCalories
            } else {
                existingSteps.totalCalories + stepsData.totalCalories
            }

            val newActiveCalories =
                if ((stepsData.activeCalories ?: 0) >= (existingSteps.activeCalories ?: 0)) {
                    stepsData.activeCalories
                } else {
                    (existingSteps.activeCalories ?: 0) + (stepsData.activeCalories ?: 0)
                }

            val newDistanceTotal = if (stepsData.totalDistance >= existingSteps.totalDistance) {
                stepsData.totalDistance
            } else {
                existingSteps.totalDistance + stepsData.totalDistance
            }

            stepsDao.updateSteps(
                date = stepsData.date!!,
                totalSteps = newStepsTotal,
                totalActiveTime = stepsData.totalActiveTime,
                totalCalories = newCaloriesTotal,
                totalDistance = newDistanceTotal,
                hourOfTheDay = stepsData.hourOfTheDay ?: 0,
                stepArray = stepsData.stepArray,
                syncDate = syncDate,
                activeCalories = newActiveCalories ?: 0,
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

    private fun getMergedData(
        existingStepsData: OreoStepsData,
        newStepsData: OreoStepsData
    ): List<OreoStepDataBreakup> {
        val mergedList = ArrayList<OreoStepDataBreakup>()
        val existingList = existingStepsData.stepArray ?: ArrayList()
        val newList = newStepsData.stepArray ?: ArrayList()

        newList.forEach { newData ->
            val existingData = existingList.find { it.hourOfTheDay == newData.hourOfTheDay }
            if (existingData != null) {

                val mergedStep = existingData.steps + newData.steps
                val mergedCalories = existingData.calories + newData.calories
                val mergedDistance = existingData.distance + newData.distance

                mergedList.add(newData.apply {
                    steps = mergedStep
                    calories = mergedCalories
                    distance = mergedDistance
                })
            } else {
                mergedList.add(newData)
            }
        }
        return mergedList
    }

    override suspend fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<OreoStepsData>? {
        rescueTables()
        return stepsDao.getUnSyncServerData(isSync)
    }

    override suspend fun deleteOldData(days: Int): Int {
        return stepsDao.deleteOlderData(days)
    }

    override suspend fun updateServerSyncData(dataList: List<OreoStepsData>): Int {
        val ids = ArrayList<Int>()
        dataList.forEach { data ->
            ids.add(data.id)
        }
        return stepsDao.updateServerUnSyncStatus(ids, true)
    }

}
