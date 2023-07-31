package com.noisefit_evolve2.util

import com.touchgui.sdk.TGSimpleSportDataListener
import com.touchgui.sdk.bean.TGSportRecord
import com.touchgui.sdk.bean.TGSyncGps
import com.touchgui.sdk.bean.TGSyncSwim
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SportDataListenerWrapper(private val callback: Callback?) : TGSimpleSportDataListener() {

    private val records: MutableList<SportRecordMerge> = ArrayList()

    override fun onStart() {
        super.onStart()
        records.clear()
    }
    override fun onSportRecord(data: TGSportRecord) {
        records.add(SportRecordMerge(data))
    }

    override fun onGpsData(data: TGSyncGps) {
        val record = getSportRecord(data.date)
        if (record != null) {
            record.gpsData = data
        }
    }

    override fun onSwimData(data: TGSyncSwim) {
        val record = getSportRecord(data.date)
        if (record != null) {
            record.swimData = data
        }
    }

    override fun onCompleted() {
        if (callback != null && records.size > 0) {
            callback.onSyncSportData(records)
        }
    }

    override fun onError(code: Int, message: String) {
        callback?.onError(code, message)
    }

    private fun getSportRecord(date: Date): SportRecordMerge? {
        for (record in records) {
            if (formatDate(date) == formatDate(record.record.date)) {
                return record
            }
        }
        return null
    }

    class SportRecordMerge(var record: TGSportRecord) {
        var gpsData: TGSyncGps? = null
        var swimData: TGSyncSwim? = null
    }

    interface Callback {
        fun onSyncSportData(records: List<SportRecordMerge>?)
        fun onError(code: Int, message: String)
    }

    companion object {
        private fun formatDate(date: Date): String {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
        }
    }
}