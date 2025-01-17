package com.oreo.data.db.implementation

import com.google.gson.Gson
import com.noisefit.data.local.db.fromJson
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.GoogleFitDataDb
import com.noisefit_commans.models.BodyMeasurementModel
import com.noisefit_commans.models.SleepDataGoogleFit
import com.noisefit_commans.models.WorkoutGoogleFit
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.db.abstaction.GoogleFitDataSource
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.db.database.OreoGFitDataDao
import com.oreo.data.model.ServerUserHealthData
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GoogleFitDataSourceImpl @Inject
constructor(
    private val googleFitDataDao: OreoGFitDataDao,
    private val localDataSource: DataStoredInterface,
    private val userHealthDataSource: OreoUserHealthDataDataSource,
) : GoogleFitDataSource {

    override suspend fun saveWorkouts(workoutList: List<WorkoutGoogleFit>) {
        val filteredData = ArrayList<WorkoutGoogleFit>()

        val compareTimestamps = ArrayList<Pair<Long, Long>>()

        val savedSleepGoogleFitData =
            googleFitDataDao.getDataByType(GoogleFitDataType.SLEEP.name.lowercase())?.map {
                Pair(it.startTime, it.endTime)
            } ?: ArrayList()
        compareTimestamps.addAll(savedSleepGoogleFitData)

        val savedWorkoutData =
            googleFitDataDao.getDataByType(GoogleFitDataType.SLEEP.name.lowercase())?.map {
                Pair(it.startTime, it.endTime)
            } ?: ArrayList()
        compareTimestamps.addAll(savedWorkoutData)


        compareTimestamps.addAll(getUserWorkoutAndSleepTimestamps())

        filteredData.addAll(workoutList.filter {
            checkDataOverlap(
                it.startTime,
                it.endTime,
                compareTimestamps,
            ).not()
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

    private suspend fun getUserWorkoutAndSleepTimestamps(): List<Pair<Long, Long>> {

        val timestamps = ArrayList<Pair<Long, Long>>()

        val zoneOffset = ZoneId.systemDefault().rules.getOffset(LocalDateTime.now())

        (1..3).forEach {
            val date = DateFormats.getCurrentDateMinusDays(it - 1)

            val userData = userHealthDataSource.getDataByDate(date)

            val parsedData = if (userData == null) {
                null
            } else {
                Gson().fromJson<ServerUserHealthData?>(
                    userData.userHealthData ?: ""
                )
            }

            parsedData?.activity?.workout?.forEach {
                val startTime = "${it.date} ${it.startTime}"
                val endTime = "${it.date} ${it.endTime}"

                val start = LocalDateTime.parse(
                    startTime,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ).toEpochSecond(zoneOffset)

                LOGS.d("dsfjhskdjfhf $start    -$startTime - $endTime")

                timestamps.add(


                    Pair(
                        LocalDateTime.parse(
                            startTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        ).toEpochSecond(zoneOffset),
                        LocalDateTime.parse(
                            endTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        )
                            .toEpochSecond(zoneOffset),
                    )
                )
            }


            parsedData?.sleep?.sleeps?.forEach {
                timestamps.add(
                    Pair(
                        LocalDateTime.parse(
                            it.startTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        ).toEpochSecond(zoneOffset),
                        LocalDateTime.parse(
                            it.endTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        )
                            .toEpochSecond(zoneOffset),
                    )
                )
            }

            parsedData?.sleep?.naps?.forEach {
                timestamps.add(
                    Pair(
                        LocalDateTime.parse(
                            it.startTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        ).toEpochSecond(zoneOffset),
                        LocalDateTime.parse(
                            it.endTime,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        )
                            .toEpochSecond(zoneOffset),
                    )
                )
            }
        }

        return timestamps
    }

    override suspend fun saveSleeps(sleepList: List<SleepDataGoogleFit>) {

        val filteredData = ArrayList<SleepDataGoogleFit>()

        val compareTimestamps = ArrayList<Pair<Long, Long>>()

        val savedSleepGoogleFitData =
            googleFitDataDao.getDataByType(GoogleFitDataType.SLEEP.name.lowercase())?.map {
                Pair(it.startTime, it.endTime)
            } ?: ArrayList()
        compareTimestamps.addAll(savedSleepGoogleFitData)

        val savedWorkoutData =
            googleFitDataDao.getDataByType(GoogleFitDataType.SLEEP.name.lowercase())?.map {
                Pair(it.startTime, it.endTime)
            } ?: ArrayList()
        compareTimestamps.addAll(savedWorkoutData)

        compareTimestamps.addAll(getUserWorkoutAndSleepTimestamps())


        filteredData.addAll(sleepList.filter {
            checkDataOverlap(it.startTime, it.endTime, compareTimestamps).not()
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
        compareData: List<Pair<Long, Long>>,
    ): Boolean {
        var dataOverlap = false
        compareData.forEach {
            val isOverlapping =
                DateFormats.checkIfTimeOverlap(startTime, endTime, it.first, it.second)
            if (dataOverlap.not() && isOverlapping) {
                dataOverlap = true
            }
            LOGS.d("DATA_OVERLAP $isOverlapping   -$startTime - $endTime  | ${it.first} - ${it.second}")

        }
        /*if (dataOverlap) {
            return true
        }
        savedWorkoutData?.forEach {
            val isOverlapping =
                DateFormats.checkIfTimeOverlap(startTime, endTime, it.startTime, it.endTime)
            if (dataOverlap.not() && isOverlapping) {
                dataOverlap = true
            }
            LOGS.d("DATA_OVERLAP workout $isOverlapping")

        }*/
        return dataOverlap
    }

    override fun getUnSyncedData(): List<GoogleFitDataDb> {
        return googleFitDataDao.getUnSyncedData()
    }

    override fun markDataSynced(id: Int, type: com.oreo.data.model.GoogleFitDataType) {
        if (type == com.oreo.data.model.GoogleFitDataType.BODY_MEASUREMENTS) {
            googleFitDataDao.markDataSyncedByType(GoogleFitDataType.HEIGHT.name.lowercase())
            return googleFitDataDao.markDataSyncedByType(GoogleFitDataType.WEIGHT.name.lowercase())
        } else {
            return googleFitDataDao.markDataSynced(id)
        }
    }

    override suspend fun deleteData() {
        val workoutTimestamp = DateFormats.lastClearDataTimeStamp(3) / 1000
        val sleepTimestamp = DateFormats.lastClearDataTimeStamp(2) / 1000

        googleFitDataDao.deleteDataByTypeAndTimestamp(
            GoogleFitDataType.WORKOUT.name.lowercase(),
            workoutTimestamp
        )
        googleFitDataDao.deleteDataByTypeAndTimestamp(
            GoogleFitDataType.SLEEP.name.lowercase(),
            sleepTimestamp
        )
    }

    override fun saveBodyMeasurements(bodyMeasurement: BodyMeasurementModel) {

        val height = bodyMeasurement.height
        val weight = bodyMeasurement.weight

        //val bodyFat = bodyMeasurement.bodyFat
        val userData = localDataSource.getUser()
        var appTimestamp = localDataSource.getAppBodyMeasurementsTimeStamp()
        if (appTimestamp == 0L) {
            localDataSource.saveAppBodyMeasurementsTimeStamp()
            appTimestamp = ZonedDateTime.now().toEpochSecond()
        }

        if (height != null) {
            val savedHeight =
                googleFitDataDao.getDataByType(GoogleFitDataType.HEIGHT.name.lowercase())
                    ?.firstOrNull()

            if (savedHeight == null || savedHeight.startTime < height.timeStamp) {
                googleFitDataDao.removeDataByType(GoogleFitDataType.HEIGHT.name.lowercase())

                if ((height.timeStamp > appTimestamp) &&
                    (height.value != (userData?.userInfo?.height ?: 0.0f))
                ) {
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
        }
        if (weight != null) {

            val savedWeight =
                googleFitDataDao.getDataByType(GoogleFitDataType.WEIGHT.name.lowercase())
                    ?.firstOrNull()

            if (savedWeight == null || savedWeight.startTime < weight.timeStamp) {
                googleFitDataDao.removeDataByType(GoogleFitDataType.WEIGHT.name.lowercase())

                if ((weight.timeStamp > appTimestamp) &&
                    (weight.value != (userData?.userInfo?.weight ?: 0.0f))
                ) {
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
}

enum class GoogleFitDataType {
    WORKOUT, SLEEP, HEIGHT, WEIGHT, BODY_FAT
}