package com.oreo.data.db.abstaction

import com.noisefit_commans.data.model.GoogleFitDataDb
import com.noisefit_commans.models.BodyMeasurementModel
import com.noisefit_commans.models.SleepDataGoogleFit
import com.noisefit_commans.models.WorkoutGoogleFit
import com.oreo.data.model.GoogleFitDataType

interface GoogleFitDataSource {

    suspend fun saveWorkouts(workoutList: List<WorkoutGoogleFit>)
    suspend fun saveSleeps(sleepList: List<SleepDataGoogleFit>)

    fun saveBodyMeasurements(bodyMeasurement: BodyMeasurementModel)

    fun getUnSyncedData(): List<GoogleFitDataDb>

    fun markDataSynced(id: Int, type: GoogleFitDataType)

    fun deleteData(timestamp:Long)

}