package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.models.StepsData

@Dao
interface StepsDao : BaseDao<StepsData> {

    @Query("SELECT * FROM steps_data where date = :date ")
    fun getTodayData(date: String): StepsData?

    @Query("Update steps_data set total_active_time = :totalActiveTime, total_steps = :totalSteps,total_distance = :totalDistance, total_active_time = :totalActiveTime, hour_of_the_day = :hourOfTheDay,total_calories = :totalCalories,step_array = :stepArray,sync_date = :syncDate,is_synced = :is_synced where date = :date")
    fun updateSteps(
        date: String,
        totalSteps: Int,
        totalCalories: Int,
        totalDistance: Int,
        totalActiveTime: Int,
        hourOfTheDay: Int,
        stepArray: ArrayList<StepsData.StepDataBreakup>?,
        syncDate: Long,
        is_synced: Boolean
    )


    @Query("Update steps_data set step_array = :stepArray where step_array = \"null\" ")
    fun updateNullStepsArray(
        stepArray: ArrayList<StepsData.StepDataBreakup>?,
    )

    //1643826600000
    //1643826600729

    @Query("SELECT id,is_synced,reset_data,total_steps,total_calories,total_distance,total_active_time,date,hour_of_the_day,step_array FROM steps_data where total_steps > 0 and sync_date >= :endDate and sync_date > 0 and is_synced = :isSync")
    fun getUnSyncServerData(endDate: Long, isSync: Boolean): List<StepsData>?

//Between :startDate  And :endDate

    @Query("DELETE from steps_data where sync_date <= :timeStamp")
    fun deleteOlderData(timeStamp: Long): Int


    @Query("UPDATE steps_data SET is_synced = :is_synced WHERE id IN (:ids) and sync_date <= :timeStamp")
    fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean, timeStamp: Long): Int
}