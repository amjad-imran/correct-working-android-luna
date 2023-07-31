package com.noisefit.data.local.db.implementation

import com.noisefit.data.local.db.abstraction.SportEventDataSource
import com.noisefit.data.local.db.database.SportEventDao
import com.noisefit_commans.data.model.matches.SportEvent
import com.noisefit_commans.utils.DateFormats
import javax.inject.Inject

class SportEventDataImpl
@Inject
constructor(
    private val sportEventDao: SportEventDao
) : SportEventDataSource {
    override fun getSportEventList(): List<SportEvent>? {
        return sportEventDao.getData()
    }

    override fun getSportEventList(date: String): SportEvent? {
        return sportEventDao.getData(date)
    }

    override fun saveSportEventList(dataList: ArrayList<SportEvent>?): Boolean {
        sportEventDao.deleteEvents()

        if (!dataList.isNullOrEmpty()) {
            sportEventDao.insertAll(dataList)
        }

        return true
    }

    override fun deleteEvent(eventId: String) {
        return sportEventDao.deleteEvent(eventId)
    }

    override fun deleteExpireEvents() {
        val startTimeSync = DateFormats.startOfDayTimeStamp()
        sportEventDao.deleteExpireEvents(startTimeSync)
    }

    override fun getSportEventsList(date: String): List<SportEvent>? {
        return sportEventDao.getAllData(date)
    }


}