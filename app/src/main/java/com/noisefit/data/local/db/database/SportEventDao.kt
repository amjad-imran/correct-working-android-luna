package com.noisefit.data.local.db.database

import androidx.room.Dao
import androidx.room.Query
import com.noisefit.data.local.db.BaseDao
import com.noisefit_commans.data.model.matches.SportEvent

@Dao
interface SportEventDao : BaseDao<SportEvent> {

    @Query("SELECT * FROM sport_event where date = :date order by timeInMilliseconds ASC Limit 1")
    fun getData(date: String): SportEvent?

    @Query("SELECT * FROM sport_event where date = :date order by timeInMilliseconds ASC")
    fun getAllData(date: String): List<SportEvent>?

    @Query("SELECT * FROM sport_event order by timeInMilliseconds ASC ")
    fun getData(): List<SportEvent>?

    @Query("DELETE FROM sport_event")
    fun deleteEvents()

    @Query("DELETE FROM sport_event where eventId = :eventId")
    fun deleteEvent(eventId: String)

    @Query("DELETE FROM sport_event where timeInMilliseconds <= :timeStamp")
    fun deleteExpireEvents(timeStamp: Long)
}