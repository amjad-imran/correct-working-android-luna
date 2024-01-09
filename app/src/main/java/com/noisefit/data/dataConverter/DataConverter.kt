package com.noisefit.data.dataConverter

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit_commans.common.*
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.RecordedWorkoutData
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.ActivityConvertUtils
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject


class DataConverter
@Inject
constructor() {

    fun createRecordedWorkoutArray(workouts: List<RecordedWorkoutData>): JsonArray {
        val jsonArray = JsonArray()
        workouts.forEach { workout ->

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
                this.addProperty("activity_type", workout.type)
                this.addProperty("start_time", startTime)
                this.addProperty("end_time", endTime)
                this.addProperty("intensity", getIntensity(0))//todo change as per logic

                val intensityArray = JsonArray()
                Gson().fromJson<List<Int>>(workout.intensityList ?: "")?.forEach {
                    intensityArray.add(it)
                }
                this.add("intensity_value", intensityArray)
                val hrArray = JsonArray()
                Gson().fromJson<List<Int>>(workout.hrData ?: "")?.forEach {
                    hrArray.add(it)
                }
                this.add("hr_value", hrArray)
                this.addProperty("steps", workout.steps)
                this.addProperty("type", "userworkout")
                this.addProperty("date", date)
            })

        }
        return jsonArray
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