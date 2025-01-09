package com.oreo.data.db.implementation

import com.google.gson.Gson
import com.noisefit.data.model.BodyMeasurementModel
import com.noisefit_commans.data.model.GoogleFitDataDb
import com.noisefit_commans.models.SleepDataGoogleFit
import com.noisefit_commans.models.WorkoutGoogleFit
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.abstaction.GoogleFitDataSource
import com.oreo.data.db.database.OreoGFitDataDao
import javax.inject.Inject

class GoogleFitDataSourceImpl @Inject
constructor(
    private val googleFitDataDao: OreoGFitDataDao
) : GoogleFitDataSource {

    override fun saveWorkouts(workoutList: List<WorkoutGoogleFit>) {
        val filteredData = ArrayList<WorkoutGoogleFit>()

        val savedSleepData =
            googleFitDataDao.getDataByType(GoogleFitDataType.SLEEP.name.lowercase())

        val savedWorkoutData =
            googleFitDataDao.getDataByType(GoogleFitDataType.SLEEP.name.lowercase())

        filteredData.addAll(workoutList.filter {
            checkDataOverlap(it.startTime, it.endTime, savedSleepData,savedWorkoutData).not()
        })

        //Save workout
        val data = ArrayList<GoogleFitDataDb>()
        filteredData.forEach {
            if (it.startTime != 0L && it.endTime != 0L) {
                data.add(
                    GoogleFitDataDb(
                        isSynced = false,
                        type = GoogleFitDataType.WORKOUT.name.lowercase(),
                        data = Gson().toJson(it),
                        startTime = it.startTime,
                        endTime = it.endTime,
                    )
                )
            }
        }

        googleFitDataDao.insertAll(data)
    }

    override fun saveSleeps(sleepList: List<SleepDataGoogleFit>) {

        val filteredData = ArrayList<SleepDataGoogleFit>()

        val savedSleepData =
            googleFitDataDao.getDataByType(GoogleFitDataType.SLEEP.name.lowercase())

        val savedWorkoutData =
            googleFitDataDao.getDataByType(GoogleFitDataType.SLEEP.name.lowercase())

        filteredData.addAll(sleepList.filter {
            checkDataOverlap(it.startTime, it.endTime, savedSleepData,savedWorkoutData).not()
        })

        //Save workout
        val data = ArrayList<GoogleFitDataDb>()
        filteredData.forEach {
            if (it.startTime != 0L && it.endTime != 0L) {
                data.add(
                    GoogleFitDataDb(
                        isSynced = false,
                        type = GoogleFitDataType.SLEEP.name.lowercase(),
                        data = Gson().toJson(it),
                        startTime = it.startTime,
                        endTime = it.endTime,
                    )
                )
            }
        }
        googleFitDataDao.insertAll(data)
    }

    private fun checkDataOverlap(
        startTime: Long,
        endTime: Long,
        savedSleepData: List<GoogleFitDataDb>?,
        savedWorkoutData: List<GoogleFitDataDb>?
    ): Boolean {
        var dataOverlap = false
        savedSleepData?.forEach {
            val isOverlapping =
                DateFormats.checkIfTimeOverlap(startTime, endTime, it.startTime, it.endTime)
            if (dataOverlap.not() && isOverlapping) {
                dataOverlap = true
            }
            LOGS.d("DATA_OVERLAP sleep $isOverlapping")

        }
        if(dataOverlap){
            return true
        }
        savedWorkoutData?.forEach {
            val isOverlapping =
                DateFormats.checkIfTimeOverlap(startTime, endTime, it.startTime, it.endTime)
            if (dataOverlap.not() && isOverlapping) {
                dataOverlap = true
            }
            LOGS.d("DATA_OVERLAP workout $isOverlapping")

        }

        return dataOverlap
    }

    override fun saveBodyMeasurements(bodyMeasurement: BodyMeasurementModel) {

        val height = bodyMeasurement.height
        val weight = bodyMeasurement.weight
        //val bodyFat = bodyMeasurement.bodyFat


        if (height != null) {
            val savedHeight =
                googleFitDataDao.getDataByType(GoogleFitDataType.HEIGHT.name.lowercase())
                    ?.firstOrNull()

            if (savedHeight == null || savedHeight.startTime < height.timeStamp) {
                googleFitDataDao.removeDataByType(GoogleFitDataType.HEIGHT.name.lowercase())

                googleFitDataDao.insert(
                    GoogleFitDataDb(
                        isSynced = false,
                        type = GoogleFitDataType.HEIGHT.name.lowercase(),
                        data = Gson().toJson(height),
                        startTime = height.timeStamp,
                        endTime = height.timeStamp,
                    )
                )
            }
        }
        if (weight != null) {

            val savedWeight =
                googleFitDataDao.getDataByType(GoogleFitDataType.WEIGHT.name.lowercase())
                    ?.firstOrNull()

            if (savedWeight == null || savedWeight.startTime < weight.timeStamp) {
                googleFitDataDao.removeDataByType(GoogleFitDataType.WEIGHT.name.lowercase())

                googleFitDataDao.insert(
                    GoogleFitDataDb(
                        isSynced = false,
                        type = GoogleFitDataType.WEIGHT.name.lowercase(),
                        data = Gson().toJson(weight),
                        startTime = weight.timeStamp,
                        endTime = weight.timeStamp,
                    )
                )
            }
        }
    }
}

enum class GoogleFitDataType {
    WORKOUT, SLEEP, HEIGHT, WEIGHT, BODY_FAT
}