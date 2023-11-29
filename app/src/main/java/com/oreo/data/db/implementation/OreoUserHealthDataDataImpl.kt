package com.oreo.data.db.implementation

import androidx.room.Transaction
import com.google.gson.Gson
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.model.DayTimeMovementBreakup
import com.noisefit_commans.data.model.UserHealthData
import com.oreo.data.db.abstaction.OreoDayTimeMovementDataSource
import com.oreo.data.db.abstaction.OreoUserHealthDataDataSource
import com.oreo.data.db.database.OreoDayTimeMovementDao
import com.oreo.data.db.database.OreoRespiratoryDao
import com.oreo.data.db.database.OreoUserHealthDataDao
import javax.inject.Inject


class OreoUserHealthDataDataImpl
@Inject
constructor(
    private val userHealthDao: OreoUserHealthDataDao
) : OreoUserHealthDataDataSource {


    @Transaction
    override suspend fun insertData(data: UserHealthData): Boolean {
        if (data.date == null) {
            return false
        }

        val prevData = getDataByDate(data.date!!)
        if (prevData == null) {
            userHealthDao.insert(data)
        } else {
            userHealthDao.updateViaDate(
                data.userHealthData,
                data.todayOtherData,
                data.date!!
            )
        }
        return true
    }

    override suspend fun getDataByDate(date: String): UserHealthData? {
        return userHealthDao.getByDate(date)

    }

    override suspend fun clearAllData() {
        userHealthDao.clearAllData()
    }

    override suspend fun clearDataByDates(dates: List<String>) {
        dates.forEach {
            userHealthDao.clearByDate(it)
        }
    }
}
