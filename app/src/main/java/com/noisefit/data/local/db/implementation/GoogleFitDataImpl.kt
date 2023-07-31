package com.noisefit.data.local.db.implementation

import androidx.room.Transaction
import com.noisefit.data.local.db.abstraction.GoogleFitDataSource
import com.noisefit.data.local.db.database.GoogleFitDao
import com.noisefit.data.local.db.database.HeartRateDao
import com.noisefit.data.local.db.database.SleepDao
import com.noisefit.data.local.db.database.StepsDao
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.DateFormats
import javax.inject.Inject


class GoogleFitDataImpl
@Inject
constructor(
    private val googleFitDao: GoogleFitDao,
    private val stepsDao: StepsDao,
    private val heartRateDao: HeartRateDao,
    private val sleepDao: SleepDao
) : GoogleFitDataSource {


    @Transaction
    override suspend fun insertOrUpdate(data: GoogleFitData, date: String): GoogleFitData? {

        val existingSteps = googleFitDao.getTodayData(date)
        if (existingSteps?.date != null) {
            // update
            googleFitDao.updateSteps(
                date = data.date,
                steps = data.steps,
                isSynced = data.isSynced,
                stepsLastSync = data.stepsLastSync

            )
        } else {
            //insert
            googleFitDao.insert(data)
        }

//        heartRateDao.insertAll(data)
        return getTodayData(date)
    }

    override suspend fun insertOrDelete(data: GoogleFitData) {

    }

    override suspend fun getTodayData(date: String): GoogleFitData? {

        return googleFitDao.getTodayData(date)
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
    private suspend fun getStepsData(date: String): StepDataGoogleFit? {
        rescueTables()
        val steps = stepsDao.getTodayData(date)
        val lastSyncedSteps = getTodayData(date)

        if (steps == null || steps.totalSteps == 0) {
            return null
        }

        var stepsToSync = steps.totalSteps
        var startTimeSync = DateFormats.startOfDayTimeStamp()
        val endTimeSync = DateFormats.getTimeStamp()

        if (lastSyncedSteps != null) {
            if (steps.totalSteps - lastSyncedSteps.steps < 20) {
                return null
            }
            startTimeSync = lastSyncedSteps.stepsLastSync
            stepsToSync = steps.totalSteps - lastSyncedSteps.steps
        }
//1663051943583
        //1663052027325
        return StepDataGoogleFit(startTimeSync, endTimeSync, stepsToSync, steps.totalSteps)
    }


    override suspend fun getUnSyncedData(date: String): SyncGoogleFitData {
        val syncGoogleFitData = SyncGoogleFitData()

        syncGoogleFitData.stepDataGoogleFit = getStepsData(date)


        val heartRateList = heartRateDao.getUnSyncGoogleFitTodayData(false)

        if (heartRateList.isNullOrEmpty()) {
            syncGoogleFitData.heartRateList = null
        } else {
            syncGoogleFitData.heartRateList = heartRateList
        }

        val sleepList = sleepDao.getUnSyncGoogleFitTodayData(false)

        if (sleepList.isNullOrEmpty()) {
            syncGoogleFitData.sleepData = null
        } else {
            syncGoogleFitData.sleepData = sleepList
        }

//        LOGS.d("google: data to sync ${Gson().toJson(syncGoogleFitData)} ")
        return syncGoogleFitData
    }

    override suspend fun updateSyncHeartRateStatus(heartRateList: List<HeartRate>) {
        val ids = ArrayList<Int>()
        heartRateList.forEach { heartRate ->
            ids.add(heartRate.id)
        }

//        LOGS.d("google: update ${Gson().toJson(ids)} ")
        heartRateDao.updateGoogleFitStatus(ids, true)
    }

    override suspend fun updateSyncSleepStatus(sleepData: SleepData) {
        val ids = ArrayList<Int>()
        sleepData.let { heartRate ->
            ids.add(heartRate.id)
        }

        sleepDao.updateGoogleFitStatus(ids, true)
    }

    override suspend fun updateSyncStepsStatus(date: String, data: StepDataGoogleFit) {
        val lastSyncedSteps = getTodayData(date)
        if (lastSyncedSteps != null) {
            googleFitDao.updateSteps(data.totalSteps, data.endTime, true, date)
        } else {
            val googleFitData = GoogleFitData()
            googleFitData.date = date
            googleFitData.isSynced = true
            googleFitData.steps = data.totalSteps
            googleFitData.stepsLastSync = data.endTime
            googleFitDao.insert(googleFitData)
        }
    }


}
