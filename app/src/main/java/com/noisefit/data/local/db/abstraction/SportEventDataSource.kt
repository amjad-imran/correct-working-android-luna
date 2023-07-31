package com.noisefit.data.local.db.abstraction

import com.noisefit_commans.data.model.matches.SportEvent

interface SportEventDataSource {
    fun getSportEventList(): List<SportEvent>?
    fun getSportEventList(date: String): SportEvent?
    fun saveSportEventList(dataList: ArrayList<SportEvent>?): Boolean
    fun deleteEvent(eventId: String)
    fun deleteExpireEvents()
    fun getSportEventsList(date: String): List<SportEvent>?
}