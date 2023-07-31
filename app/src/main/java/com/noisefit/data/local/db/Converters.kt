package com.noisefit.data.local.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.models.StepsData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.data.model.OreoStepsData

inline fun <reified T> Gson.fromJson(json: String): T =
    fromJson<T>(json, object : TypeToken<T>() {}.type)!!

class Converters {
    @TypeConverter
    fun fromStringToIntArray(value: String?): IntArray? =
        Gson().fromJson(value, object : TypeToken<IntArray?>() {}.type)

    @TypeConverter
    fun fromIntArrayToString(list: IntArray?): String = Gson().toJson(list)

    @TypeConverter
    fun fromSleepDataBreakup(value: String): ArrayList<SleepData.SleepDataBreakup> =
        Gson().fromJson<ArrayList<SleepData.SleepDataBreakup>>(value)

    @TypeConverter
    fun toSleepDataBreakup(list: ArrayList<SleepData.SleepDataBreakup>?): String =
        Gson().toJson(list)

    @TypeConverter
    fun fromStepsDataBreakup(value: String?): ArrayList<StepsData.StepDataBreakup?>? =
        value?.let { Gson().fromJson<ArrayList<StepsData.StepDataBreakup?>?>(it) }

    @TypeConverter
    fun toStepsDataBreakup(list: ArrayList<StepsData.StepDataBreakup?>?): String =
        Gson().toJson(list)

    //For oreo
    @TypeConverter
    fun toStepsDataBreakupOreo(list: ArrayList<OreoStepsData.OreoStepDataBreakup?>?): String =
        Gson().toJson(list)

    @TypeConverter
    fun fromStepsDataBreakupOreo(value: String?): ArrayList<OreoStepsData.OreoStepDataBreakup?>? =
        value?.let { Gson().fromJson<ArrayList<OreoStepsData.OreoStepDataBreakup?>?>(it) }

    @TypeConverter
    fun toSleepDataBreakupOreo(list: ArrayList<OreoSleepData.OreoSleepDataBreakup>?): String =
        Gson().toJson(list)

    @TypeConverter
    fun toSleepMovementDataBreakupOreo(list: ArrayList<OreoSleepData.OreoSleepMovementDataBreakup>?): String =
        Gson().toJson(list)

    @TypeConverter
    fun fromSleepDataBreakupOreo(value: String): ArrayList<OreoSleepData.OreoSleepDataBreakup> =
        Gson().fromJson<ArrayList<OreoSleepData.OreoSleepDataBreakup>>(value)

    @TypeConverter
    fun fromSleepMovementDataBreakupOreo(value: String): ArrayList<OreoSleepData.OreoSleepMovementDataBreakup> =
        Gson().fromJson<ArrayList<OreoSleepData.OreoSleepMovementDataBreakup>>(value)

}