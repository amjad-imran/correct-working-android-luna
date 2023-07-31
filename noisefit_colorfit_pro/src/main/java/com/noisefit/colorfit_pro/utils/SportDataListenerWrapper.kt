package com.noisefit.colorfit_pro.utils

import com.crrepa.ble.conn.bean.CRPHistoryTrainingInfo
import com.crrepa.ble.conn.bean.CRPMovementHeartRateInfo
import com.crrepa.ble.conn.bean.CRPTrainingInfo
import com.crrepa.ble.conn.listener.CRPTrainingChangeListener
import com.google.gson.Gson
import com.noisefit.colorfit_pro.handler.connect.ProConnectHandler
import com.noisefit_commans.utils.LOGS

private const val TAG = "SportDataListenerWrapper"
open class SportDataListenerWrapper(private val callback: Callback?) : CRPTrainingChangeListener {
    private val activityDataHm = LinkedHashMap<Long, CRPTrainingInfo?>()
    private val trainingList = ArrayList<Int>()
    private var count = 0

    interface Callback {
        fun onSyncSportData(records: List<CRPTrainingInfo>)
    }

    override fun onHistoryTrainingChange(list: MutableList<CRPHistoryTrainingInfo>?) {
        trainingList.clear()
        count = 0
        activityDataHm.clear()
        LOGS.d("$TAG start")
        list?.forEachIndexed { index, crpHistoryTrainingInfo ->
            if (1000 < crpHistoryTrainingInfo.startTime) {
                trainingList.add(index)
//                LOGS.d("$TAG ${Gson().toJson(crpHistoryTrainingInfo)}")
            }
        }


        if (trainingList.isNotEmpty()) {
            queryTrainingDetails()
            LOGS.d("$TAG querying")
        }
    }

    private fun queryTrainingDetails() {
        trainingList.forEach {
            ProConnectHandler.bleConnection?.queryTraining(it)
        }
    }

    override fun onTrainingChange(crpTrainingInfo: CRPTrainingInfo?) {
        count += 1
//        LOGS.d("$TAG ${Gson().toJson(crpTrainingInfo)}")
        val id = crpTrainingInfo?.startTime?.plus(crpTrainingInfo.endTime) ?: 0
        if (id != 0L) {
            activityDataHm[id] = crpTrainingInfo
        }

//        LOGS.d("$TAG activityDataHm ${Gson().toJson(activityDataHm)}")
        LOGS.d("$TAG count ${trainingList.size} $count")
        if (trainingList.size == count) {
            val crpMovementHrList = ArrayList<CRPTrainingInfo>()
            for ((key, value) in activityDataHm) {
                if (value != null) {
                    crpMovementHrList.add(value)
                }
            }
            callback?.onSyncSportData(crpMovementHrList)
        }

    }


}