package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.models.StepsData
import com.noisefit_commans.data.model.OreoStepsData

@Dao
interface OreoStepsDao : BaseDao<OreoStepsData> {

    @Query("SELECT * FROM steps_data where date = :date ")
    fun getTodayData(date: String): OreoStepsData?

    @Query("Update steps_data set total_active_time = :totalActiveTime," +
            " total_steps = :totalSteps," +
            "total_distance = :totalDistance, " +
            "total_active_time = :totalActiveTime, " +
            "hour_of_the_day = :hourOfTheDay," +
            "total_calories = :totalCalories," +
            "active_calories = :activeCalories," +
            "step_array = :stepArray,sync_date = :syncDate,is_synced = :is_synced where date = :date")
    fun updateSteps(
        date: String,
        totalSteps: Int,
        totalCalories: Int,
        activeCalories: Int,
        totalDistance: Int,
        totalActiveTime: Int,
        hourOfTheDay: Int,
        stepArray: ArrayList<OreoStepsData.OreoStepDataBreakup>?,
        syncDate: Long,
        is_synced: Boolean
    )


    @Query("Update steps_data set step_array = :stepArray where step_array = \"null\" ")
    fun updateNullStepsArray(
        stepArray: ArrayList<OreoStepsData.OreoStepDataBreakup>?,
    )

    //1643826600000
    //1643826600729

    @Query("SELECT id,is_synced,reset_data,total_steps,total_calories,total_distance,active_calories,total_active_time," +
            "date,hour_of_the_day,step_array,sync_date FROM steps_data where total_steps > 0 and is_synced = :isSync")
    fun getUnSyncServerData(isSync: Boolean): List<OreoStepsData>?

//Between :startDate  And :endDate

    @Query("DELETE from steps_data where sync_date <= :timeStamp")
    fun deleteOlderData(timeStamp: Long): Int


    @Query("UPDATE steps_data SET is_synced = :is_synced WHERE id IN (:ids)")
    fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean): Int
}