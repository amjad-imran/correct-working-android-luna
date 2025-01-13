package com.oreo.data.db.abstaction

import com.noisefit.data.model.BodyMeasurementModel
import com.noisefit_commans.data.model.GoogleFitDataDb
import com.noisefit_commans.models.SleepDataGoogleFit
import com.noisefit_commans.models.WorkoutGoogleFit

interface GoogleFitDataSource {

    fun saveWorkouts(workoutList: List<WorkoutGoogleFit>)
    fun saveSleeps(sleepList: List<SleepDataGoogleFit>)

    fun saveBodyMeasurements(bodyMeasurement: BodyMeasurementModel)

    fun getUnSyncedData(): List<GoogleFitDataDb>

    fun markDataSynced(id:Int)

    fun deleteData(timestamp:Long)

}