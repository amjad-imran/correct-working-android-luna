package com.noisefit.data.dataConverter

import android.location.Location
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.local.db.fromJson
import com.noisefit_commans.common.ceilRound
import com.noisefit_commans.data.db.LocationModel
import com.noisefit_commans.data.db.abstraction.LocationDataSource
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.data.model.RecordedWorkoutData
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.AddWorkoutResponse
import com.oreo.data.model.health.Nap
import com.oreo.data.model.health.SleepHourlyBreakup
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class DataConverter
@Inject constructor(
    val keyValueDataSource: KeyValueDataSource,
    val ringDataStore: RingDataStore,
    val locationDataSource: LocationDataSource
) {

    fun mergeSleepData(
        sleepArray: List<SleepHourlyBreakup>?, naps: List<Nap>?
    ): List<SleepHourlyBreakup>? {
        if (naps.isNullOrEmpty()) return sleepArray
        if (sleepArray.isNullOrEmpty()) return null


        val dates = ArrayList<String>()

        naps.forEach {
            dates.add(it.startTime)
        }

        val sleepArrayFirst = sleepArray?.firstOrNull()

        sleepArrayFirst?.start_time?.let { dates.add(it) }

        val sortedDates = DateFormats.sortDates(dates)
        val newSleepArray = ArrayList<SleepHourlyBreakup>()
        var lastDateTime: String? = null
        sortedDates.forEach {
            val dateTime = it.toString("yyyy-MM-dd HH:mm:ss")

            if (sleepArrayFirst?.start_time != null && dateTime.equals(sleepArrayFirst.start_time)) {
                if (lastDateTime != null) {
                    val duration = DateFormats.getDifferenceInMinutes(
                        lastDateTime, sleepArrayFirst.start_time
                    ) * 60
                    newSleepArray.add(
                        SleepHourlyBreakup(
                            start_time = lastDateTime ?: "",
                            end_time = sleepArrayFirst.start_time,
                            duration = duration,
                            sleep_type = "awake",
                            date = ""
                        )
                    )
                }

                newSleepArray.addAll(sleepArray)
                lastDateTime = sleepArray.lastOrNull()?.end_time
            } else {
                val breakup = getSleepDataBreakup(dateTime, naps)

                breakup?.let {
                    if (lastDateTime != null) {
                        val duration = DateFormats.getDifferenceInMinutes(
                            lastDateTime, it.start_time
                        ) * 60
                        newSleepArray.add(
                            SleepHourlyBreakup(
                                start_time = lastDateTime ?: "",
                                end_time = it.start_time ?: "",
                                duration = duration,
                                sleep_type = "awake",
                                date = ""
                            )
                        )
                    }
                    newSleepArray.add(it)
                    lastDateTime = it.end_time
                }
            }

        }
        return newSleepArray
    }

    fun getSleepDataBreakup(dateTime: String, naps: List<Nap>): SleepHourlyBreakup? {

        val nap = naps.firstOrNull {
            it.startTime.equals(dateTime)
        } ?: return null


        return SleepHourlyBreakup(
            start_time = nap.startTime,
            end_time = nap.endTime,
            duration = (nap.duration ?: 0) * 60,
            sleep_type = "deep",
            date = ""
        )
    }


    suspend fun createRecordedWorkoutArray(workouts: List<RecordedWorkoutData>): JsonArray? {
        val jsonArray = JsonArray()
        val offlineList =
            keyValueDataSource.getData("", KeyValueDataType.RECORD_WORKOUT)?.value ?: return null

        val workoutsList = Gson().fromJson<List<OWorkoutListModal>>(
            offlineList
        )
        if (workoutsList.isEmpty()) return null

        val toDeleteList = ringDataStore.getRecordDeleteList()


        workouts.forEach { workout ->

            val (workoutTypeString, dataType, dataPriority) = getWorkoutData(
                workout.type,
                workoutsList
            )
            if (workoutTypeString != null && workout.duration != 0 && !toDeleteList.contains(workout.startTime)) {

                /* val date = DateFormats.convertTimestampToDate(
                     workout.startTime,
                     DateFormats.dateFormat3
                 )*/

                val startTime =
                    DateFormats.convertTimestampToDate(workout.startTime, DateFormats.timeFormat)
                val endTime =
                    DateFormats.convertTimestampToDate(workout.endTime, DateFormats.timeFormat)


                val locationData =
                    locationDataSource.getLocations(workout.startTime, workout.endTime).sortedBy {
                        it.timeStamp
                    }


                jsonArray.add(JsonObject(
                ).apply {
                    this.addProperty("distance", workout.distance)
                    this.addProperty("cadence", workout.cadence)
                    this.addProperty("recovery_time", workout.recoveryTime)
                    this.addProperty("duration", workout.duration)
                    this.addProperty("duration_seconds", workout.durationSeconds)
                    this.addProperty("calories", workout.calories)
                    this.addProperty("activity_type", workoutTypeString)
                    this.addProperty("start_time", startTime)
                    this.addProperty("end_time", endTime)

                    this.addProperty("data_type", dataType)
                    this.addProperty("data_priority", dataPriority)

                    val locationArray = JsonArray()
                    var temp: Double? = null
                    var weatherStatus: Int? = null

                    locationData.forEach { location ->
                        locationArray.add(JsonObject().apply {
                            this.addProperty("lat", location.lat)
                            this.addProperty("long", location.longitude)
                            this.addProperty("timestamp", location.timeStamp / 1000)
                        })
                        if (temp == null) {
                            temp = location.temperature
                            weatherStatus = location.weatherStatus
                        }
                    }

                    val gpsDistanceInMeters = getGpsDistance(locationData)

                    if (locationArray.isEmpty.not()) {
                        this.add("location", locationArray)
                        this.addProperty("gps_distance", gpsDistanceInMeters)
                    }
                    if (temp != null) {
                        this.add("weather", JsonObject().apply {
                            this.addProperty("temp", temp?.roundToInt())
                            this.addProperty("status", weatherStatus)
                        })
                    }

                    val intensityArray = JsonArray()

                    val intArray = Gson().fromJson<List<Int>>(workout.intensityList ?: "")

                    intArray.forEach {
                        intensityArray.add(it)
                    }

                    val intensity = intArray.average().ceilRound()

                    this.addProperty(
                        "intensity", getIntensity(intensity)
                    )


                    this.add("intensity_value", intensityArray)
                    val hrArray = JsonArray()
                    Gson().fromJson<List<Int>>(workout.hrData ?: "").forEach {
                        hrArray.add(it)
                    }
                    this.add("hr_value", hrArray)
                    this.addProperty("steps", workout.steps)
                    this.addProperty("type", "userworkout")
                    this.addProperty("date", workout.date)
                })
            }
        }
        return jsonArray
    }

    private fun getGpsDistance(locationData: List<LocationModel>): Long {
        if (locationData.size <= 1) return 0

        var distance = 0L
        for (pos in 1 until locationData.size) {
            val location1 = locationData[pos - 1]
            val location2 = locationData[pos]

            if (location1.lat != null && location1.longitude != null && location2.lat != null && location2.longitude != null) {
                distance += calculateDistance(
                    location1.lat!!, location1.longitude!!, location2.lat!!, location2.longitude!!
                )
            }
        }
        return distance
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Long {

        val loc1 = Location("start")
        loc1.latitude = lat1
        loc1.longitude = lon1

        val loc2 = Location("end")
        loc2.latitude = lat2
        loc2.longitude = lon2

        return loc1.distanceTo(loc2).roundToLong()
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    private fun getWorkoutType(type: Int?, workoutsList: List<OWorkoutListModal>): String? {
        if (type == null) return null

        val workout = workoutsList.find {
            it.ringId == type
        }
        return workout?.activityType
    }


    /**
     * return Triple -> <Activity type, data type, data priority>
     */
    private fun getWorkoutData(
        type: Int?, workoutsList: List<OWorkoutListModal>
    ): Triple<String?, String?, String?> {
        if (type == null) return Triple(null, null, null)

        val workout = workoutsList.find {
            it.ringId == type
        }
        return if (workout == null) {
            Triple(null, null, null)
        } else {
            Triple(workout.activityType, workout.dataType, workout.dataPriority)
        }
    }

    private fun getIntensity(intensity: Int): String {
        return when (intensity) {
            0 -> {
                "Easy"
            }

            1 -> {
                "Moderate"
            }

            else -> {
                "Hard"
            }
        }
    }

    fun getWorkoutId(workouts: List<AddWorkoutResponse>, timeStamp: Long): String? {

        if (timeStamp == 0L) return null
        if (workouts.isEmpty()) return null
        val time = DateFormats.convertTimestampToDate(timeStamp, DateFormats.timeFormat)
        val date = DateFormats.convertTimestampToDate(timeStamp, DateFormats.dateFormat3)
        return workouts.find {
            it.start_time.equals(time, true) && it.date.equals(date, true)
        }?.workoutId
    }

    suspend fun getSportModeResponseArray(workouts: List<RecordedWorkoutData>): List<SportsModeResponse> {
        val list = ArrayList<SportsModeResponse>()

        val offlineList = keyValueDataSource.getData("", KeyValueDataType.RECORD_WORKOUT)?.value

        val workoutsList = if (offlineList == null) {
            ArrayList()
        } else {
            Gson().fromJson<List<OWorkoutListModal>>(
                offlineList
            )
        }

        workouts.forEach {
            val workoutTypeString = getWorkoutType(it.type, workoutsList)

            val startTime =
                DateFormats.convertTimestampToDate(it.startTime, DateFormats.dateTimeFormat6)

            val sportObj = SportsModeResponse(
                date = it.date,
                distance = 0,
                duration = (it.duration ?: 0).toLong() * 60,
                calories = it.calories?.toLong() ?: 0L,
                heartRateCurrent = 0,
                steps = it.steps ?: 0,
                type = workoutTypeString,
                time = startTime
            )
            list.add(sportObj)
        }

        return list
    }

}