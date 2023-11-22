package com.oreo.data.db.implementation

import com.noisefit_commans.data.model.GoogleFitWorkoutData
import com.noisefit_commans.models.HeartRate
import com.oreo.data.db.abstaction.OreoGFitWorkoutDataSource
import com.oreo.data.db.database.OreoGFitWorkoutDao
import javax.inject.Inject


class OreoGFitWorkoutDataImpl
@Inject
constructor(
    private val oreoGFitWorkoutDao: OreoGFitWorkoutDao
) : OreoGFitWorkoutDataSource {
    override suspend fun saveWorkout(data: List<GoogleFitWorkoutData>):List<GoogleFitWorkoutData>? {
        oreoGFitWorkoutDao.insertAll(data)
        return getUnSyncWorkout()
    }

    override suspend fun getUnSyncWorkout(): List<GoogleFitWorkoutData> {
        return oreoGFitWorkoutDao.getUnSyncGoogleFitTodayData(false) ?: ArrayList()
    }


    override suspend fun updateServerSyncData(dataList: List<GoogleFitWorkoutData>): Int {
        val heartList = dataList.chunked(500)

        heartList.forEach { heartRateList ->
            val ids = ArrayList<Int>()
            heartRateList.forEach { data ->
                ids.add(data.id)
            }

            oreoGFitWorkoutDao.updateServerUnSyncStatus(ids, true)
        }



        return 1
    }


}