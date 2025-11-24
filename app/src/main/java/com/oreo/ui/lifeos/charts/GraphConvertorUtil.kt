package com.oreo.ui.lifeos.charts

import com.noisefit_commans.common.maxWithoutZero
import com.noisefit_commans.common.minWithoutZero
import com.oreo.data.model.HRModel
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.TapMeasureState

object GraphConvertorUtil {

    fun parseHrData(breakup: List<Int>?): OHealthOverview.HeartRateDataModel {
        var breakupArray = breakup
        if (breakupArray.isNullOrEmpty()) {
            val dummyArray = ArrayList<Int>()
            for (i in 0..287) {
                dummyArray.add(0)
            }
            breakupArray = dummyArray
        }
        var lastHrValue: Pair<Int, Long>? = null//HR value,timer
        breakupArray.forEachIndexed { index2, value ->
            if (value != 0 && value != 255)
                lastHrValue = Pair(value, 0)

        }
        val excludeDataList = arrayListOf<Int>()
        breakupArray.forEach { value ->
            if (value == 255) {
                excludeDataList.add(0)
            } else
                excludeDataList.add(value)
        }

        val hRWithIntervalList = excludeDataList.chunked(6)
        val avgList = ArrayList<Int>()
        var overAllMinValue = Int.MAX_VALUE
        var overAllMaxValue = -1
        var hrCount = 0

        val listData = ArrayList<HRModel>()
        hRWithIntervalList.forEachIndexed { index, hrList ->
            val sortedBreakUpList = hrList.sorted()

            val minValue = sortedBreakUpList.minWithoutZero()
            val maxValue = sortedBreakUpList.maxWithoutZero()

            var min = minValue
            var max = maxValue

            if (min == 0 && max != 0) {
                min = max
            }

            if (max == 0 && min != 0) {
                max = min
            }


            val avg = (min + max) / 2
            if (avg != 0) {
                if (min < overAllMinValue) {
                    overAllMinValue = min
                }
                if (max > overAllMaxValue) {
                    overAllMaxValue = max
                }
                avgList.add(avg)
            }


            //if any change chunk value then divide 12 by that chunk value to get below correct xlabel list
            if (index % 2 == 0) {
                hrCount += 1

            }
            listData.add(
                HRModel(
                    maxValues = max,
                    minValues = min,
                    values = sortedBreakUpList,
                    midValues = avg
                )
            )
        }
        val average = avgList.average().toFloat()


        val measureState = TapMeasureState.HIDE
        return OHealthOverview.HeartRateDataModel(
            listData = listData,
            rawData = breakupArray,
            average = average,
            lastTime = "0",
            value = lastHrValue?.first.toString(),
            maxValues = breakupArray.maxWithoutZero(),
            minValues = breakupArray.minWithoutZero(),
            measureState = measureState,
            hrCombineModel = null,
            lastMeasuredValue = 0,
            lastMeasuredIndex = 0,
            trendPercent = 0,
            ringGeneration = 1,

            )
    }
}