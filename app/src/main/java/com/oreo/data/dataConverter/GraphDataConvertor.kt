package com.oreo.data.dataConverter

import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.models.SleepMovementType
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel
import com.oreo.ui.custom.Item
import com.oreo.data.model.Item as DTItem
import com.oreo.ui.custom.StressCombineModel
import com.oreo.ui.lifeos.charts.DayTimeChartPayload
import com.oreo.ui.lifeos.charts.GraphConvertorUtil
import com.oreo.ui.lifeos.charts.HrChartPayload
import com.oreo.ui.lifeos.charts.InsightCardUiModel
import com.oreo.ui.lifeos.charts.StressChartPayload
import java.util.ArrayList
import javax.inject.Inject
import com.oreo.data.model.DayTimeDataModel
import com.oreo.data.model.health.SleepMovementBreakup
import com.oreo.data.model.lifeos.dashModels.InsightGraph
import kotlin.collections.forEach


class GraphDataConvertor @Inject constructor(
    val hrDataConvertor: OreoHRDataConvertor
) {


    fun generateHrGraphData(rawData: InsightItemResponseModel): InsightCardUiModel {

        val hrItems = ArrayList<Int>()
        for (i in 0 until 288) {
            val v = (50..150).random()
            hrItems.add(v)
        }

        val hrData = GraphConvertorUtil.parseHrData(hrItems)
        val combinedHrData = hrDataConvertor.getHrCombinedData(
            null, hrData
        )

        return InsightCardUiModel(
            id = 1L,
            title = "Heart rate demo",
            timeText = "now",
            chartKey = GraphsKey.HEART_RATE,
            payload = HrChartPayload(
                combinedHrData, yAxisCount = 5, minYAxis = hrData.minValues,
                maxYAxis = hrData.maxValues
            ),
            styleRes = R.style.HrChartStyle,
            raw = rawData
        )
    }

    fun generateStressGraphData(rawData: InsightItemResponseModel): InsightCardUiModel {
        val stressItems = ArrayList<Item>()
        for (i in 0 until 96) {
            val v = (30..100).random()
            stressItems.add(Item(value = v, index = i, minValue = 0, maxValue = 100))
        }
        val stressModel = StressCombineModel(
            sections = arrayListOf(),
            items = stressItems,
            high = 75,
            medium = 50
        )

        return InsightCardUiModel(
            id = 2L,
            title = "Stress demo",
            timeText = "today",
            chartKey = GraphsKey.STRESS,
            payload = StressChartPayload(stressModel),
            styleRes = R.style.StressChartStyle,
            raw = null
        )
    }

    fun generateDayTimeGraphData(rawData: InsightItemResponseModel): InsightCardUiModel {
        val dtItems = ArrayList<DTItem>()
        for (i in 0 until 288) {
            val v = (0..5).random()
            dtItems.add(DTItem(value = v, index = i))
        }
        val dtModel = DayTimeDataModel(sections = arrayListOf(), items = dtItems)
        return InsightCardUiModel(
            id = 3L,
            title = "Daytime movement demo",
            timeText = "this week",
            chartKey = GraphsKey.DAY_TIME_MOVEMENT,
            payload = DayTimeChartPayload(dtModel),
            styleRes = R.style.DayTimeGraphStyle,
            raw = null
        )
    }

    fun generateSleepBreakupGraphData(rawData: InsightItemResponseModel): InsightCardUiModel {
        val mainObjString =
            "{\"breakup_sleep\":[ { \"duration\": 180, \"end_time\": \"2025-09-09 23:52:00\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-09 23:49:00\" }, { \"duration\": 120, \"end_time\": \"2025-09-09 23:54:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-09 23:52:00\" }, { \"duration\": 210, \"end_time\": \"2025-09-09 23:57:30\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-09 23:54:00\" }, { \"duration\": 540, \"end_time\": \"2025-09-10 00:06:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-09 23:57:30\" }, { \"duration\": 330, \"end_time\": \"2025-09-10 00:12:00\", \"sleep_type\": \"deep\", \"start_time\": \"2025-09-10 00:06:30\" }, { \"duration\": 510, \"end_time\": \"2025-09-10 00:20:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 00:12:00\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 00:26:30\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 00:20:30\" }, { \"duration\": 540, \"end_time\": \"2025-09-10 00:35:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 00:26:30\" }, { \"duration\": 930, \"end_time\": \"2025-09-10 00:51:00\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 00:35:30\" }, { \"duration\": 210, \"end_time\": \"2025-09-10 00:54:30\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-10 00:51:00\" }, { \"duration\": 870, \"end_time\": \"2025-09-10 01:09:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 00:54:30\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 01:15:00\", \"sleep_type\": \"deep\", \"start_time\": \"2025-09-10 01:09:00\" }, { \"duration\": 270, \"end_time\": \"2025-09-10 01:19:30\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 01:15:00\" }, { \"duration\": 150, \"end_time\": \"2025-09-10 01:22:00\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-10 01:19:30\" }, { \"duration\": 690, \"end_time\": \"2025-09-10 01:33:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 01:22:00\" }, { \"duration\": 1410, \"end_time\": \"2025-09-10 01:57:00\", \"sleep_type\": \"deep\", \"start_time\": \"2025-09-10 01:33:30\" }, { \"duration\": 1680, \"end_time\": \"2025-09-10 02:25:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 01:57:00\" }, { \"duration\": 150, \"end_time\": \"2025-09-10 02:27:30\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-10 02:25:00\" }, { \"duration\": 2790, \"end_time\": \"2025-09-10 03:14:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 02:27:30\" }, { \"duration\": 180, \"end_time\": \"2025-09-10 03:17:00\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-10 03:14:00\" }, { \"duration\": 1350, \"end_time\": \"2025-09-10 03:39:30\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 03:17:00\" }, { \"duration\": 450, \"end_time\": \"2025-09-10 03:47:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 03:39:30\" }, { \"duration\": 120, \"end_time\": \"2025-09-10 03:49:00\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 03:47:00\" }, { \"duration\": 180, \"end_time\": \"2025-09-10 03:52:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 03:49:00\" }, { \"duration\": 1470, \"end_time\": \"2025-09-10 04:16:30\", \"sleep_type\": \"deep\", \"start_time\": \"2025-09-10 03:52:00\" }, { \"duration\": 180, \"end_time\": \"2025-09-10 04:19:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 04:16:30\" }, { \"duration\": 270, \"end_time\": \"2025-09-10 04:24:00\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 04:19:30\" }, { \"duration\": 1290, \"end_time\": \"2025-09-10 04:45:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 04:24:00\" }, { \"duration\": 1050, \"end_time\": \"2025-09-10 05:03:00\", \"sleep_type\": \"deep\", \"start_time\": \"2025-09-10 04:45:30\" }, { \"duration\": 690, \"end_time\": \"2025-09-10 05:14:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 05:03:00\" }, { \"duration\": 510, \"end_time\": \"2025-09-10 05:23:00\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 05:14:30\" }, { \"duration\": 1320, \"end_time\": \"2025-09-10 05:45:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 05:23:00\" }, { \"duration\": 450, \"end_time\": \"2025-09-10 05:52:30\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 05:45:00\" }, { \"duration\": 810, \"end_time\": \"2025-09-10 06:06:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 05:52:30\" }, { \"duration\": 510, \"end_time\": \"2025-09-10 06:14:30\", \"sleep_type\": \"deep\", \"start_time\": \"2025-09-10 06:06:00\" }, { \"duration\": 600, \"end_time\": \"2025-09-10 06:24:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 06:14:30\" }, { \"duration\": 570, \"end_time\": \"2025-09-10 06:34:00\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 06:24:30\" }, { \"duration\": 1020, \"end_time\": \"2025-09-10 06:51:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 06:34:00\" }, { \"duration\": 480, \"end_time\": \"2025-09-10 06:59:00\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-10 06:51:00\" } ] }"
        val mainObj = Gson().fromJson<InsightGraph>(mainObjString, InsightGraph::class.java)

        return InsightCardUiModel(
            id = 4L,
            title = "Sleep analysis demo",
            timeText = "last night",
            chartKey = GraphsKey.SLEEP_BREAKUP,
            payload = mainObj.breakup_sleep,
            raw = null
        )
    }

    fun generateSleepMovementData(rawData: InsightItemResponseModel): InsightCardUiModel {

        val mainObjString =
            "{\"sleep_movement\":[ { \"duration\": 360, \"end_time\": \"2025-09-09 23:55:00\", \"start_time\": \"2025-09-09 23:49:00\", \"movement_type\": \"INTENSE\" }, { \"duration\": 2160, \"end_time\": \"2025-09-10 00:31:00\", \"start_time\": \"2025-09-09 23:55:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 00:43:00\", \"start_time\": \"2025-09-10 00:31:00\", \"movement_type\": \"LOW\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 00:49:00\", \"start_time\": \"2025-09-10 00:43:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 00:55:00\", \"start_time\": \"2025-09-10 00:49:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 1080, \"end_time\": \"2025-09-10 01:13:00\", \"start_time\": \"2025-09-10 00:55:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 01:19:00\", \"start_time\": \"2025-09-10 01:13:00\", \"movement_type\": \"INTENSE\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 01:25:00\", \"start_time\": \"2025-09-10 01:19:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 01:31:00\", \"start_time\": \"2025-09-10 01:25:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 1440, \"end_time\": \"2025-09-10 01:55:00\", \"start_time\": \"2025-09-10 01:31:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 02:07:00\", \"start_time\": \"2025-09-10 01:55:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 02:13:00\", \"start_time\": \"2025-09-10 02:07:00\", \"movement_type\": \"LOW\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 02:19:00\", \"start_time\": \"2025-09-10 02:13:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 02:31:00\", \"start_time\": \"2025-09-10 02:19:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 02:37:00\", \"start_time\": \"2025-09-10 02:31:00\", \"movement_type\": \"LOW\" }, { \"duration\": 2160, \"end_time\": \"2025-09-10 03:13:00\", \"start_time\": \"2025-09-10 02:37:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 03:19:00\", \"start_time\": \"2025-09-10 03:13:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 3240, \"end_time\": \"2025-09-10 04:13:00\", \"start_time\": \"2025-09-10 03:19:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 04:25:00\", \"start_time\": \"2025-09-10 04:13:00\", \"movement_type\": \"LOW\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 04:31:00\", \"start_time\": \"2025-09-10 04:25:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 04:37:00\", \"start_time\": \"2025-09-10 04:31:00\", \"movement_type\": \"INTENSE\" }, { \"duration\": 1800, \"end_time\": \"2025-09-10 05:07:00\", \"start_time\": \"2025-09-10 04:37:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 05:13:00\", \"start_time\": \"2025-09-10 05:07:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 05:19:00\", \"start_time\": \"2025-09-10 05:13:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 05:31:00\", \"start_time\": \"2025-09-10 05:19:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 1080, \"end_time\": \"2025-09-10 05:49:00\", \"start_time\": \"2025-09-10 05:31:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 05:55:00\", \"start_time\": \"2025-09-10 05:49:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 1080, \"end_time\": \"2025-09-10 06:13:00\", \"start_time\": \"2025-09-10 05:55:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 06:19:00\", \"start_time\": \"2025-09-10 06:13:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 06:31:00\", \"start_time\": \"2025-09-10 06:19:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 06:37:00\", \"start_time\": \"2025-09-10 06:31:00\", \"movement_type\": \"LOW\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 06:49:00\", \"start_time\": \"2025-09-10 06:37:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 06:55:00\", \"start_time\": \"2025-09-10 06:49:00\", \"movement_type\": \"INTENSE\" } ]}"
        val mainObj = Gson().fromJson<InsightGraph>(mainObjString, InsightGraph::class.java)

        return InsightCardUiModel(
            id = 5L,
            title = "Sleep Movement demo",
            timeText = "last night",
            chartKey = GraphsKey.SLEEP_MOVEMENT,
            payload = getMovementBreakup(mainObj.sleep_movement),
            raw = null
        )
    }

    fun getMovementBreakup(
        sleepBreakup: List<SleepMovementBreakup>?,
    ): Pair<List<OreoSleepData.OreoSleepMovementDataBreakup>,
            CountCardData> {
        val countCData = CountCardData(
            type = "Movement",
            imageSourceId = 0,
            cardSourceId = 0,
            imageBgSourceId = 0
        )
        countCData.count = "_"
        countCData.countSubText = "sub"

        countCData.leftValue = sleepBreakup?.firstOrNull()?.start_time ?: ""
        countCData.rightValue = sleepBreakup?.lastOrNull()?.end_time ?: ""

        val sleepArray = ArrayList<OreoSleepData.OreoSleepMovementDataBreakup>()
        sleepBreakup?.forEach { breakup ->
            /*if (breakup.duration >= 60) {*/
            sleepArray.add(
                OreoSleepData.OreoSleepMovementDataBreakup(
                    movementType = SleepMovementType.getValueFromString(breakup.movement_type).name,
                    duration = breakup.duration,
                    startTime = breakup.start_time,
                    endTime = breakup.end_time
                )
            )
            //}
        }
        return Pair(sleepArray, countCData)
    }

    fun generateRemDayData():InsightCardUiModel{
        val mainObjString =
            "{\"breakup_day\":[ { \"date\": \"2025-09-08\" }, { \"date\": \"2025-09-09\" }, { \"date\": \"2025-09-10\", \"value1\": 4830 }, { \"date\": \"2025-09-11\" }, { \"date\": \"2025-09-12\" }, { \"date\": \"2025-09-13\" }, { \"date\": \"2025-09-14\" } ]}"
        val mainObj = Gson().fromJson<InsightGraph>(mainObjString, InsightGraph::class.java)

        return InsightCardUiModel(
            id = 6L,
            title = "Rem Day demo",
            timeText = "last night",
            chartKey = GraphsKey.REM_DAY,
            payload = getMovementBreakup(mainObj.sleep_movement),
            raw = null
        )
    }
}

enum class GraphsKey {
    HEART_RATE, STRESS, DAY_TIME_MOVEMENT, SLEEP_BREAKUP, SLEEP_MOVEMENT,
    REM_DAY
}
