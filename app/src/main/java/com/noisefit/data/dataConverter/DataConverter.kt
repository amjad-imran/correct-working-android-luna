package com.noisefit.data.dataConverter

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.local.db.fromJson
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.RecordedWorkoutData
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.OWorkoutListModal
import javax.inject.Inject


class DataConverter
@Inject
constructor(
    val keyValueDataSource: KeyValueDataSource,
    val ringDataStore: RingDataStore
) {

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

            val workoutTypeString = getWorkoutType(workout.type, workoutsList)
            if (workoutTypeString != null && workout.duration != 0 && !toDeleteList.contains(workout.startTime)) {

                val date = DateFormats.convertTimestampToDate(
                    workout.startTime,
                    DateFormats.dateFormat3
                )
                val startTime =
                    DateFormats.convertTimestampToDate(workout.startTime, DateFormats.timeFormat)
                val endTime =
                    DateFormats.convertTimestampToDate(workout.endTime, DateFormats.timeFormat)

                jsonArray.add(
                    JsonObject(
                    ).apply {
                        this.addProperty("duration", workout.duration)
                        this.addProperty("calories", workout.calories)
                        this.addProperty("activity_type", workoutTypeString)
                        this.addProperty("start_time", startTime)
                        this.addProperty("end_time", endTime)
                        this.addProperty("intensity", getIntensity(0))//todo change as per logic

                        val intensityArray = JsonArray()
                        Gson().fromJson<List<Int>>(workout.intensityList ?: "").forEach {
                            intensityArray.add(it)
                        }
                        this.add("intensity_value", intensityArray)
                        val hrArray = JsonArray()
                        Gson().fromJson<List<Int>>(workout.hrData ?: "").forEach {
                            hrArray.add(it)
                        }
                        this.add("hr_value", hrArray)
                        this.addProperty("steps", workout.steps)
                        this.addProperty("type", "userworkout")
                        this.addProperty("date", date)
                    })
            }
        }
        return jsonArray
    }

    private fun getWorkoutType(type: Int?, workoutsList: List<OWorkoutListModal>): String? {
        if (type == null) return null

        val workout = workoutsList.find {
            it.ringId == type
        }
        return workout?.activityType
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

}