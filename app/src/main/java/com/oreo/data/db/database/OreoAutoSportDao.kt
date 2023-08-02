package com.oreo.data.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.OreoAutoSportData

//[{"autoSportDuration":340,"autoSportIntensity":0,"autoSportKcal":5,"autoSportStartTime":1690863212,"autoSportSteps":601,"autoSportType":1,"hrData":[]}]


@Dao
interface OreoAutoSportDao : BaseDao<OreoAutoSportData> {

    @Query("SELECT * FROM auto_sport where is_accepted = :isAccepted")
    fun getAllNotAcceptingData(isAccepted: Boolean): List<OreoAutoSportData>?
//
//
//    @Query("UPDATE blood_oxygen SET break_up = :breakUp,is_synced = :is_synced  WHERE date = :date")
//    fun updateViaDate(breakUp: String, date: String, is_synced: Boolean)
//
//
//    @Query("Delete FROM blood_oxygen where date = :date")
//    fun deleteTodayData(date: String)
//
//    @Query("UPDATE blood_oxygen SET is_google_fit_sync = :is_google_fit_sync WHERE id IN (:ids)")
//    fun updateGoogleFitStatus(ids: List<Int>, is_google_fit_sync: Boolean)
//
//    @Query("SELECT * FROM blood_oxygen where is_synced = :is_synced")
//    fun getServerUnSyncData(is_synced: Boolean): List<OreoBloodOxygenBreakup>?
//
//
//    @Query("UPDATE blood_oxygen SET is_synced = :is_synced WHERE id IN (:ids)")
//    fun updateServerUnSyncStatus(ids: List<Int>, is_synced: Boolean): Int

}