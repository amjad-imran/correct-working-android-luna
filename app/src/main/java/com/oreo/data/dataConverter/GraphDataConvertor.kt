package com.oreo.data.dataConverter

import com.google.gson.Gson
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.yearMonth
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.data.model.OreoSleepData
import com.noisefit_commans.models.SleepMovementType
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.GraphType
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
import com.oreo.data.model.TrendsGraphData
import com.oreo.data.model.TrendsValues
import com.oreo.data.model.health.SleepMovementBreakup
import com.oreo.data.model.lifeos.dashModels.InsightGraph
import com.oreo.ui.custom.sleep.internal.GraphDataModel
import com.oreo.ui.custom.sleep.internal.SleepSingleGradientChartType
import com.oreo.ui.lifeos.charts.PayloadData
import com.oreo.ui.lifeos.charts.TimeSeriesPayload
import com.oreo.ui.lifeos.charts.TrendGraphData
import com.oreo.ui.sleep2.internal.InternalSelectedPeriod
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.collections.forEach
import kotlin.math.roundToInt


class GraphDataConvertor @Inject constructor(
    val hrDataConvertor: OreoHRDataConvertor,
    val sessionManager: SessionManager,
    val resourcesProvider: ResourcesProvider
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
            timeText = getInsightRelevantGeneratedTime(rawData.dateTime).second,
            chartKey = GraphsKey.HEART_RATE,
            payload = PayloadData(
                hrData = HrChartPayload(
                    combinedHrData, yAxisCount = 5, minYAxis = hrData.minValues,
                    maxYAxis = hrData.maxValues
                )
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
            timeText = getInsightRelevantGeneratedTime(rawData.dateTime).second,
            chartKey = GraphsKey.STRESS,
            payload = PayloadData(stressData = StressChartPayload(stressModel)),
            styleRes = R.style.StressChartStyle,
            raw = rawData
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
            timeText = getInsightRelevantGeneratedTime(rawData.dateTime).second,
            chartKey = GraphsKey.DAY_TIME_MOVEMENT,
            payload = PayloadData(dayTimeData = DayTimeChartPayload(dtModel)),
            styleRes = R.style.DayTimeGraphStyle,
            raw = rawData
        )
    }

    fun generateSleepBreakupGraphData(rawData: InsightItemResponseModel): InsightCardUiModel {
        val mainObjString =
            "{\"breakup_sleep\":[ { \"duration\": 180, \"end_time\": \"2025-09-09 23:52:00\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-09 23:49:00\" }, { \"duration\": 120, \"end_time\": \"2025-09-09 23:54:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-09 23:52:00\" }, { \"duration\": 210, \"end_time\": \"2025-09-09 23:57:30\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-09 23:54:00\" }, { \"duration\": 540, \"end_time\": \"2025-09-10 00:06:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-09 23:57:30\" }, { \"duration\": 330, \"end_time\": \"2025-09-10 00:12:00\", \"sleep_type\": \"deep\", \"start_time\": \"2025-09-10 00:06:30\" }, { \"duration\": 510, \"end_time\": \"2025-09-10 00:20:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 00:12:00\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 00:26:30\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 00:20:30\" }, { \"duration\": 540, \"end_time\": \"2025-09-10 00:35:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 00:26:30\" }, { \"duration\": 930, \"end_time\": \"2025-09-10 00:51:00\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 00:35:30\" }, { \"duration\": 210, \"end_time\": \"2025-09-10 00:54:30\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-10 00:51:00\" }, { \"duration\": 870, \"end_time\": \"2025-09-10 01:09:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 00:54:30\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 01:15:00\", \"sleep_type\": \"deep\", \"start_time\": \"2025-09-10 01:09:00\" }, { \"duration\": 270, \"end_time\": \"2025-09-10 01:19:30\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 01:15:00\" }, { \"duration\": 150, \"end_time\": \"2025-09-10 01:22:00\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-10 01:19:30\" }, { \"duration\": 690, \"end_time\": \"2025-09-10 01:33:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 01:22:00\" }, { \"duration\": 1410, \"end_time\": \"2025-09-10 01:57:00\", \"sleep_type\": \"deep\", \"start_time\": \"2025-09-10 01:33:30\" }, { \"duration\": 1680, \"end_time\": \"2025-09-10 02:25:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 01:57:00\" }, { \"duration\": 150, \"end_time\": \"2025-09-10 02:27:30\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-10 02:25:00\" }, { \"duration\": 2790, \"end_time\": \"2025-09-10 03:14:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 02:27:30\" }, { \"duration\": 180, \"end_time\": \"2025-09-10 03:17:00\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-10 03:14:00\" }, { \"duration\": 1350, \"end_time\": \"2025-09-10 03:39:30\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 03:17:00\" }, { \"duration\": 450, \"end_time\": \"2025-09-10 03:47:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 03:39:30\" }, { \"duration\": 120, \"end_time\": \"2025-09-10 03:49:00\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 03:47:00\" }, { \"duration\": 180, \"end_time\": \"2025-09-10 03:52:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 03:49:00\" }, { \"duration\": 1470, \"end_time\": \"2025-09-10 04:16:30\", \"sleep_type\": \"deep\", \"start_time\": \"2025-09-10 03:52:00\" }, { \"duration\": 180, \"end_time\": \"2025-09-10 04:19:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 04:16:30\" }, { \"duration\": 270, \"end_time\": \"2025-09-10 04:24:00\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 04:19:30\" }, { \"duration\": 1290, \"end_time\": \"2025-09-10 04:45:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 04:24:00\" }, { \"duration\": 1050, \"end_time\": \"2025-09-10 05:03:00\", \"sleep_type\": \"deep\", \"start_time\": \"2025-09-10 04:45:30\" }, { \"duration\": 690, \"end_time\": \"2025-09-10 05:14:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 05:03:00\" }, { \"duration\": 510, \"end_time\": \"2025-09-10 05:23:00\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 05:14:30\" }, { \"duration\": 1320, \"end_time\": \"2025-09-10 05:45:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 05:23:00\" }, { \"duration\": 450, \"end_time\": \"2025-09-10 05:52:30\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 05:45:00\" }, { \"duration\": 810, \"end_time\": \"2025-09-10 06:06:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 05:52:30\" }, { \"duration\": 510, \"end_time\": \"2025-09-10 06:14:30\", \"sleep_type\": \"deep\", \"start_time\": \"2025-09-10 06:06:00\" }, { \"duration\": 600, \"end_time\": \"2025-09-10 06:24:30\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 06:14:30\" }, { \"duration\": 570, \"end_time\": \"2025-09-10 06:34:00\", \"sleep_type\": \"rem\", \"start_time\": \"2025-09-10 06:24:30\" }, { \"duration\": 1020, \"end_time\": \"2025-09-10 06:51:00\", \"sleep_type\": \"light\", \"start_time\": \"2025-09-10 06:34:00\" }, { \"duration\": 480, \"end_time\": \"2025-09-10 06:59:00\", \"sleep_type\": \"awake\", \"start_time\": \"2025-09-10 06:51:00\" } ] }"
        val mainObj = Gson().fromJson<InsightGraph>(mainObjString, InsightGraph::class.java)

        return InsightCardUiModel(
            id = 4L,
            title = "Sleep analysis demo",
            timeText = getInsightRelevantGeneratedTime(rawData.dateTime).second,
            chartKey = GraphsKey.SLEEP_BREAKUP,
            payload = PayloadData(sleepBreakup = mainObj.breakup_sleep),
            raw = rawData
        )
    }

    fun generateSleepMovementData(rawData: InsightItemResponseModel): InsightCardUiModel {

        val mainObjString =
            "{\"sleep_movement\":[ { \"duration\": 360, \"end_time\": \"2025-09-09 23:55:00\", \"start_time\": \"2025-09-09 23:49:00\", \"movement_type\": \"INTENSE\" }, { \"duration\": 2160, \"end_time\": \"2025-09-10 00:31:00\", \"start_time\": \"2025-09-09 23:55:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 00:43:00\", \"start_time\": \"2025-09-10 00:31:00\", \"movement_type\": \"LOW\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 00:49:00\", \"start_time\": \"2025-09-10 00:43:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 00:55:00\", \"start_time\": \"2025-09-10 00:49:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 1080, \"end_time\": \"2025-09-10 01:13:00\", \"start_time\": \"2025-09-10 00:55:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 01:19:00\", \"start_time\": \"2025-09-10 01:13:00\", \"movement_type\": \"INTENSE\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 01:25:00\", \"start_time\": \"2025-09-10 01:19:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 01:31:00\", \"start_time\": \"2025-09-10 01:25:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 1440, \"end_time\": \"2025-09-10 01:55:00\", \"start_time\": \"2025-09-10 01:31:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 02:07:00\", \"start_time\": \"2025-09-10 01:55:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 02:13:00\", \"start_time\": \"2025-09-10 02:07:00\", \"movement_type\": \"LOW\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 02:19:00\", \"start_time\": \"2025-09-10 02:13:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 02:31:00\", \"start_time\": \"2025-09-10 02:19:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 02:37:00\", \"start_time\": \"2025-09-10 02:31:00\", \"movement_type\": \"LOW\" }, { \"duration\": 2160, \"end_time\": \"2025-09-10 03:13:00\", \"start_time\": \"2025-09-10 02:37:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 03:19:00\", \"start_time\": \"2025-09-10 03:13:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 3240, \"end_time\": \"2025-09-10 04:13:00\", \"start_time\": \"2025-09-10 03:19:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 04:25:00\", \"start_time\": \"2025-09-10 04:13:00\", \"movement_type\": \"LOW\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 04:31:00\", \"start_time\": \"2025-09-10 04:25:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 04:37:00\", \"start_time\": \"2025-09-10 04:31:00\", \"movement_type\": \"INTENSE\" }, { \"duration\": 1800, \"end_time\": \"2025-09-10 05:07:00\", \"start_time\": \"2025-09-10 04:37:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 05:13:00\", \"start_time\": \"2025-09-10 05:07:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 05:19:00\", \"start_time\": \"2025-09-10 05:13:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 05:31:00\", \"start_time\": \"2025-09-10 05:19:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 1080, \"end_time\": \"2025-09-10 05:49:00\", \"start_time\": \"2025-09-10 05:31:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 05:55:00\", \"start_time\": \"2025-09-10 05:49:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 1080, \"end_time\": \"2025-09-10 06:13:00\", \"start_time\": \"2025-09-10 05:55:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 06:19:00\", \"start_time\": \"2025-09-10 06:13:00\", \"movement_type\": \"MEDIUM\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 06:31:00\", \"start_time\": \"2025-09-10 06:19:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 06:37:00\", \"start_time\": \"2025-09-10 06:31:00\", \"movement_type\": \"LOW\" }, { \"duration\": 720, \"end_time\": \"2025-09-10 06:49:00\", \"start_time\": \"2025-09-10 06:37:00\", \"movement_type\": \"NO_MOVEMENT\" }, { \"duration\": 360, \"end_time\": \"2025-09-10 06:55:00\", \"start_time\": \"2025-09-10 06:49:00\", \"movement_type\": \"INTENSE\" } ]}"
        val mainObj = Gson().fromJson<InsightGraph>(mainObjString, InsightGraph::class.java)

        return InsightCardUiModel(
            id = 5L,
            title = "Sleep Movement demo",
            timeText = getInsightRelevantGeneratedTime(rawData.dateTime).second,
            chartKey = GraphsKey.SLEEP_MOVEMENT,
            payload = PayloadData(sleepMovement = getMovementBreakup(mainObj.sleep_movement)),
            raw = rawData
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


//    fun handleTrendsData(data: InsightItemResponseModel): InsightCardUiModel? {
//
//        "rem_sleep_day"
//        val period = when {
//            data.graph_type?.endsWith("week") == true -> InternalSelectedPeriod.WEEK
//            data.graph_type?.endsWith("month") == true -> InternalSelectedPeriod.MONTH
//            else -> InternalSelectedPeriod.DAY
//        }
//
//        val contributor = when {
//            data.graph_type == null -> SleepInternalLaunchState.DEEP_SLEEP
//            data.graph_type.startsWith("circadian_mid_point_day") -> SleepInternalLaunchState.TIMING
//            data.graph_type.startsWith("hour_vs_need_day") -> SleepInternalLaunchState.HOUR_VS_NEED
//            data.graph_type.startsWith("restorative_sleep") -> SleepInternalLaunchState.RESTORATIVE_SLEEP
//            data.graph_type.startsWith("rem_sleep") -> SleepInternalLaunchState.REM_SLEEP
//            data.graph_type.startsWith("deep_sleep") -> SleepInternalLaunchState.DEEP_SLEEP
//            data.graph_type.startsWith("sleep_perf") -> SleepInternalLaunchState.SLEEP_PERFORMANCE
//            else -> SleepInternalLaunchState.DEEP_SLEEP
//        }
//
//        when (period) {
//            InternalSelectedPeriod.DAY, null -> {
//                return when (contributor) {
//                    SleepInternalLaunchState.REM_SLEEP,
//                    SleepInternalLaunchState.DEEP_SLEEP,
//                    SleepInternalLaunchState.RESPIRATORY_RATE,
//                    SleepInternalLaunchState.BLOOD_OXYGEN,
//                    SleepInternalLaunchState.LATENCY,
//                    SleepInternalLaunchState.RESTFULNESS,
//                    SleepInternalLaunchState.SLEEP_PERFORMANCE -> {
//                        generateSleepSingleBarChartData(data)
//                    }
//
//                    SleepInternalLaunchState.SLEEP_DURATION,
//                    SleepInternalLaunchState.HRV,
//                    SleepInternalLaunchState.RESTING_HEART_RATE,
//                    SleepInternalLaunchState.SKIN_TEMPERATURE,
//                    SleepInternalLaunchState.EFFICIENCY -> {
//                        generateSleepSingleLineGradientChartData(data, period, contributor)
//                    }
//
//                    SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
//                        generateSleepMultiBarChartData(data, contributor)
//                    }
//
//                    SleepInternalLaunchState.HOUR_VS_NEED -> {
//                        generateSleepHourVsNeedChartInternalData(data,period, contributor)
//                    }
//
//                    SleepInternalLaunchState.SLEEP_TIME -> {
//                        generateSleepSingleBarChartData(data)
//                    }
//
//                    SleepInternalLaunchState.TIMING -> {
//                        generateSleepTimingChartInternalData(data, period, contributor)
//                    }
//
//                    else -> null
//                }
//            }
//
//            InternalSelectedPeriod.WEEK -> {
//                return when (contributor) {
//                    SleepInternalLaunchState.HOUR_VS_NEED,
//                    SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
//                        generateSleepSingleBarChartData(data)
//                    }
//
//                    SleepInternalLaunchState.SLEEP_TIME,
//                    SleepInternalLaunchState.TIMING -> {
//                        generateSleepSingleBarChartData(data)
//                    }
//
//                    SleepInternalLaunchState.RESPIRATORY_RATE,
//                    SleepInternalLaunchState.RESTING_HEART_RATE,
//                    SleepInternalLaunchState.BLOOD_OXYGEN,
//                    SleepInternalLaunchState.SKIN_TEMPERATURE,
//                    SleepInternalLaunchState.HRV -> {
//                        generateSleepSingleBarChartData(data)
//                    }
//
//                    else -> {
//                        generateSleepSingleBarChartData(data)
//                    }
//                }
//            }
//
//            InternalSelectedPeriod.MONTH -> {
//                return when (contributor) {
//                    SleepInternalLaunchState.HOUR_VS_NEED,
//                    SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
//                        generateSleepSingleBarChartData(data)
//                    }
//
//                    SleepInternalLaunchState.SLEEP_TIME,
//                    SleepInternalLaunchState.TIMING -> {
//                        generateSleepSingleBarChartData(data)
//                    }
//
//                    SleepInternalLaunchState.RESPIRATORY_RATE,
//                    SleepInternalLaunchState.RESTING_HEART_RATE,
//                    SleepInternalLaunchState.BLOOD_OXYGEN,
//                    SleepInternalLaunchState.SKIN_TEMPERATURE,
//                    SleepInternalLaunchState.HRV -> {
//                        generateSleepSingleBarChartData(data)
//                    }
//
//                    else -> {
//                        generateSleepSingleBarChartData(data)
//                    }
//                }
//            }
//
//            InternalSelectedPeriod.DAILY -> {
//                return generateSleepSingleBarChartData(data)
//            }
//        }
//    }

     fun generateSleepMultiBarChartData(
         rawData: InsightItemResponseModel,
         contributor: SleepInternalLaunchState,
         id: Int
     ): InsightCardUiModel? {

        /*val mainObjString =
            "{\"trends_breakup\":[ { \"date\": \"2025-12-01\" }, { \"date\": \"2025-12-02\", \"value1\": 4230, \"value2\": 4380 }, { \"date\": \"2025-12-03\" } ]}"
        val mainObj = Gson().fromJson<InsightGraph>(mainObjString, InsightGraph::class.java)*/

         if(rawData.graph_type.isNullOrEmpty()) return null

         val period = when {
             rawData.graph_type.endsWith("week") -> InternalSelectedPeriod.WEEK
             rawData.graph_type.endsWith("month") -> InternalSelectedPeriod.MONTH
             else -> InternalSelectedPeriod.DAY
         }

        fun convertData(data: List<TrendsValues>?): List<GraphDataModel> {
            data?.map { Pair(((it.value2 ?: 0.0f) / 60), ((it.value1 ?: 0.0f) / 60)) }
                ?: ArrayList()

            return data?.map {
                GraphDataModel(
                    date = LocalDate.parse(it.date),
                    value1 =if (it.value2 == null) {
                        null
                    } else {
                        (it.value2 ?: 0.0f) / 60
                    },
                    value2 = if (it.value1 == null) {
                        null
                    } else {
                        (it.value1 ?: 0.0f) / 60
                    }
                )
            } ?: ArrayList()
        }

        val dataList = convertData(rawData.graph)

        val maxValue = getMaxValue(
            dataListType1 = dataList,
            contributorType = contributor
        )
        val yAxisRange =
            getYAxisRange(maxValue, contributorType = contributor)

        val xAxisRange = getXAxisRangeInsights(rawData.graph, period)

        return InsightCardUiModel(
            id = id.toLong(),
            title = "generateSleepMultiBarChartData() demo",
            timeText = getInsightRelevantGeneratedTime(rawData.dateTime).second,
            chartKey = GraphsKey.TREND_SLEEP_MULTI_BAR,
            payload = PayloadData(
                trendData = TrendGraphData(
                    list = dataList,
                    yAxisRange = yAxisRange,
                    maxValue = yAxisRange.last().first,
                    xAxisRangeInsights = xAxisRange
                )
            ),
            raw = rawData
        )
    }

    fun generateSleepSingleLineChartData(
        rawData: InsightItemResponseModel,
        period: InternalSelectedPeriod,
        contributor: SleepInternalLaunchState,
        id: Int
    ): InsightCardUiModel {

        /*val mainObjString =
            "{\"trends_breakup\":[ { \"date\": \"2025-11-03\" }, { \"date\": \"2025-11-04\" }, { \"date\": \"2025-11-05\" }, { \"date\": \"2025-11-06\" }, { \"date\": \"2025-11-07\", \"value1\": 18000 }, { \"date\": \"2025-11-08\" }, { \"date\": \"2025-11-09\" } ]}"
        val mainObj = Gson().fromJson<InsightGraph>(mainObjString, InsightGraph::class.java)*/


        fun convertData(data: List<TrendsValues>?): List<GraphDataModel> {
            val isMetric = sessionManager.isMetric()

            return data?.map {
                GraphDataModel(
                    date = LocalDate.parse(it.date),
                    value1 = if (contributor== SleepInternalLaunchState.SLEEP_DURATION ||
                        contributor == SleepInternalLaunchState.REM_SLEEP ||
                        contributor == SleepInternalLaunchState.DEEP_SLEEP
                    ) {
                        if (it.value1 != null) {
                            (it.value1 ?: 0.0f) / 60
                        } else null
                    } else if (it.value1 != null && contributor == SleepInternalLaunchState.SKIN_TEMPERATURE && isMetric) {
                        val convertedValue = AppConversionUtils.fahrenheitToCelsius(
                            it.value1!!
                        )
                        if (convertedValue < 0) {
                            0.0f
                        } else {
                            convertedValue
                        }
                    } else if (contributor == SleepInternalLaunchState.RESPIRATORY_RATE
                        || contributor == SleepInternalLaunchState.RESTING_HEART_RATE
                        || contributor == SleepInternalLaunchState.HRV
                    ) {
                        if (it.value1 == 255f) null else it.value1
                    } else {
                        it.value1
                    }
                )
            } ?: ArrayList()
        }


        val dataList = convertData(rawData.graph)
        val nonNullDataCount =
            getNonNullDataCount(contributor, dataList)

        val minMax = getMinMaxValue(
            dataListType1 = dataList,
            contributorType = contributor
        )
        val yAxisRange = getYAxisRange(
            minMax.second,
            contributor,
            minValue = minMax.first
        )
        val xAxisRange = getXAxisRangeInsights(rawData.graph,period)

        val avgValue = getAvgValuePair(
            0f,
            contributorType = contributor
        )
        val showOverlay =
            if (period == InternalSelectedPeriod.DAY) false else true

        val type = if (rawData.graph_type?.equals(GraphType.Day.CIRCADIAN_MID_POINT) == true ||
            rawData.graph_type?.equals(GraphType.Week.CIRCADIAN_MID_POINT) == true ||
            rawData.graph_type?.equals(GraphType.Month.CIRCADIAN_MID_POINT) == true) {
            SleepSingleGradientChartType.TIME
        } else {
            null
        }

        return InsightCardUiModel(
            id = id.toLong(),
            title = "generateSleepSingleLineChartData() demo",
            timeText = getInsightRelevantGeneratedTime(rawData.dateTime).second,
            chartKey = GraphsKey.TREND_SLEEP_SINGLE_LINE_GRADIENT,
            payload = PayloadData(
                trendData = TrendGraphData(
                    dataList,
                    yAxisRange,
                    null,
                    avgValue,
                    -1,
                    contributor,
                    null,
                    nonNullDataCount,
                    selectedPeriod = period,
                    showOverlay = showOverlay,
                    xAxisRangeInsights = xAxisRange,
                    chartType = type
                )
            ),
            raw = rawData
        )

    }

    fun generateSleepSingleLineGradientChartData(
        rawData: InsightItemResponseModel,
        period: InternalSelectedPeriod,
        contributor: SleepInternalLaunchState
    ): InsightCardUiModel {
        /*val mainObjString =
            "{\"trends_breakup\":[ { \"date\": \"2025-11-03\" }, { \"date\": \"2025-11-04\" }, { \"date\": \"2025-11-05\" }, { \"date\": \"2025-11-06\" }, { \"date\": \"2025-11-07\", \"value1\": 18000 }, { \"date\": \"2025-11-08\" }, { \"date\": \"2025-11-09\" } ]}"
        val mainObj = Gson().fromJson<InsightGraph>(mainObjString, InsightGraph::class.java)*/


        fun convertData(data: List<TrendsValues>?): List<GraphDataModel> {
            val isMetric = sessionManager.isMetric()

            return data?.map {
                GraphDataModel(
                    date = LocalDate.parse(it.date),
                    value1 = if (contributor == SleepInternalLaunchState.SLEEP_DURATION) {
                        if (it.value1 != null) {
                            (it.value1 ?: 0.0f) / 60
                        } else null
                    } else if (it.value1 != null && contributor == SleepInternalLaunchState.SKIN_TEMPERATURE && isMetric) {
                        val convertedValue = AppConversionUtils.fahrenheitToCelsius(
                            it.value1!!
                        )
                        if (convertedValue < 0) {
                            0.0f
                        } else {
                            convertedValue
                        }
                    } else if (contributor == SleepInternalLaunchState.RESPIRATORY_RATE ||
                        contributor == SleepInternalLaunchState.HRV
                    ) {
                        if (it.value1 == 255f) null else it.value1
                    } else {
                        it.value1
                    }
                )
            } ?: ArrayList()
        }

        val dataList = convertData(rawData.graph)
        val nonNullDataCount =
            getNonNullDataCount(contributor, dataList)

        val minMax = getMinMaxValue(
            dataListType1 = dataList,
            contributorType = contributor
        )
        val yAxisRange =
            getYAxisRange(minMax.second, contributor, minMax.first)
        val xAxisRange = getXAxisRange(rawData.graph, period)
        val avgValue = getAvgValuePair(
            0f,
            contributorType = contributor
        )
        val optimalRange = getOptimalRangeMinMax(contributor)

        val type = if (contributor == SleepInternalLaunchState.SLEEP_DURATION) {
            SleepSingleGradientChartType.TIME
        } else if (contributor == SleepInternalLaunchState.RESTING_HEART_RATE
            || contributor == SleepInternalLaunchState.HRV
        ) {
            SleepSingleGradientChartType.DEFAULT
        } else if (contributor == SleepInternalLaunchState.SKIN_TEMPERATURE) {
            SleepSingleGradientChartType.FLOAT
        } else {
            SleepSingleGradientChartType.PERCENT
        }

        return InsightCardUiModel(
            id = 6L,
            title = "Sleep single line gradient demo",
            timeText = getInsightRelevantGeneratedTime(rawData.dateTime).second,
            chartKey = GraphsKey.TREND_SLEEP_SINGLE_LINE_GRADIENT,
            payload = PayloadData(
                trendData = TrendGraphData(
                    dataList,
                    yAxisRange,
                    xAxisRange,
                    avgValue,
                    -1,
                    contributor,
                    optimalRange,
                    nonNullDataCount,
                    chartType = type
                )
            ),
            raw = rawData
        )
    }


    fun generateSleepSingleBarChartData(rawData: InsightItemResponseModel): InsightCardUiModel {
        /*val mainObjString =
            "{\"trends_breakup\":[ { \"date\": \"2025-09-08\" }, { \"date\": \"2025-09-09\" }, { \"date\": \"2025-09-10\", \"value1\": 4830 }, { \"date\": \"2025-09-11\" }, { \"date\": \"2025-09-12\" }, { \"date\": \"2025-09-13\" }, { \"date\": \"2025-09-14\" } ]}"
        val mainObj = Gson().fromJson<InsightGraph>(mainObjString, InsightGraph::class.java)*/

        val period = when {
            rawData.graph_type?.endsWith("week") == true -> InternalSelectedPeriod.WEEK
            rawData.graph_type?.endsWith("month") == true -> InternalSelectedPeriod.MONTH
            else -> InternalSelectedPeriod.DAY
        }

        fun convertData(
            data: List<TrendsValues>?,
            contributor: SleepInternalLaunchState
        ): List<GraphDataModel> {
            return data?.map {
                GraphDataModel(
                    date = LocalDate.parse(it.date),
                    value1 = if (contributor == SleepInternalLaunchState.REM_SLEEP ||
                        contributor == SleepInternalLaunchState.DEEP_SLEEP
                    ) {
                        if (it.value1 == null) {
                            null
                        } else {
                            (it.value1 ?: 0.0f) / 60
                        }
                    } else if (contributor == SleepInternalLaunchState.RESTING_HEART_RATE) {
                        if (it.value1 == 255f) null else it.value1
                    } else {
                        it.value1
                    }
                )
            } ?: ArrayList()
        }

        val contributor = SleepInternalLaunchState.REM_SLEEP
        val dataList = convertData(rawData.graph, contributor)


        val nonNullDataCount =
            getNonNullDataCount(contributor, dataList)

        val minMax = getMinMaxValue(
            dataListType1 = dataList,
            contributorType = contributor
        )

        val avgValue = getAvgValuePair(
            0f, //todo change avg value
            contributorType = contributor
        )
        val yAxisRange = getYAxisRange(minMax.second, contributor)
        val optimalRange = getOptimalRangeMinMax(contributor)

        return InsightCardUiModel(
            id = 6L,
            title = "Rem Day demo",
            timeText = getInsightRelevantGeneratedTime(rawData.dateTime).second,
            chartKey = GraphsKey.TREND_SLEEP_SINGLE,
            payload = PayloadData(
                trendData = TrendGraphData(
                    dataList,
                    yAxisRange,
                    null,
                    avgValue,
                    -1,
                    contributor,
                    optimalRange,
                    nonNullDataCount,
                    xAxisRangeInsights = getXAxisRangeInsights(rawData.graph, period)
                )
            ),
            raw = rawData
        )
    }

    fun generateSleepHourVsNeedChartInternalData(
        rawData: InsightItemResponseModel,
        period: InternalSelectedPeriod,
        contributorType: SleepInternalLaunchState?,
    ): InsightCardUiModel {
        /*val mainObjString =
            "{\"trends_breakup\":[ { \"date\": \"2025-12-01\" }, { \"date\": \"2025-12-02\", \"value1\": 17310, \"value2\": 33000 }, { \"date\": \"2025-12-03\" } ]}"

        val mainObj = Gson().fromJson<InsightGraph>(mainObjString, InsightGraph::class.java)*/

        fun convertData(data: List<TrendsValues>?): List<GraphDataModel> {
            return data?.map {
                GraphDataModel(
                    date = LocalDate.parse(it.date),
                    value1 = if (it.value1 == null) {
                        null
                    } else {
                        (it.value1 ?: 0.0f) / 60
                    },
                    value2 = if (it.value2 == null) {
                        null
                    } else {
                        (it.value2 ?: 0.0f) / 60
                    }
                )
            } ?: ArrayList()
        }

        val dataList = convertData(rawData.graph)

        val maxValue = getMaxValue(
            dataListType1 = dataList, contributorType = contributorType
        )
        val yAxisRange = getYAxisRange(maxValue, contributorType)

        return InsightCardUiModel(
            id = 6L,
            title = "Rem Day demo",
            timeText = getInsightRelevantGeneratedTime(rawData.dateTime).second,
            chartKey = GraphsKey.TREND_SLEEP_HOUR_VS_NEED_CHARD_INTERNAL,
            payload = PayloadData(
                trendData = TrendGraphData(
                    list = dataList,
                    yAxisRange = yAxisRange,
                    maxValue = yAxisRange.last().first,
                    selectedPosition = -1
                )
            ),
            raw = rawData
        )
    }

    fun generateSleepTimingChartInternalData(
        rawData: InsightItemResponseModel,
        period: InternalSelectedPeriod,
        contributorType: SleepInternalLaunchState?,
    ): InsightCardUiModel {
        /*val mainObjString =
            "{\"trends_breakup\":[ { \"date\": \"2025-12-01\" }, { \"date\": \"2025-12-02\", \"master_mid_time\": \"05:27:00\" }, { \"date\": \"2025-12-03\" } ]}"

        val mainObj = Gson().fromJson<InsightGraph>(mainObjString, InsightGraph::class.java)*/

        fun convertData(data: List<TrendsValues>?): Pair<List<GraphDataModel>, Float> {
            val midTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

            val returnData = ArrayList<GraphDataModel>()

            var maxDeviation = 0.0f//in minutes
            data?.forEach {

                val currentDay = LocalDate.parse(it.date).atStartOfDay()

                if (it.master_mid_time == null) {
                    returnData.add(
                        GraphDataModel(
                            date = LocalDate.parse(it.date), value1 = null
                        )
                    )
                } else {
                    val midTime = LocalTime.parse(it.master_mid_time, midTimeFormat)
                    val dateToAppend = if (midTime.hour >= 20) {
                        LocalDate.parse(it.date).minusDays(1).toString()
                    } else {
                        it.date
                    }

                    val startDate = "$dateToAppend ${it.master_mid_time}"

                    val sleepStartTime = LocalDateTime.parse(startDate, dateTimeFormatter)

                    val difference = Duration.between(currentDay, sleepStartTime).toMinutes()
                    if (difference > maxDeviation) {
                        maxDeviation = difference.toFloat()
                    }

                    returnData.add(
                        GraphDataModel(
                            date = LocalDate.parse(it.date), value1 = difference.toFloat()
                        )
                    )
                }
            }

            return Pair(returnData, maxDeviation)
        }

        //
        val dataList = convertData(rawData.graph)
        val xAxisRange = getXAxisRange(rawData.graph, period)
        val yAxisRange = getYAxisRange(maxValue = dataList.second,contributorType = contributorType)

        val optimalRange = getOptimalRangeMinMax(contributorType)

        return InsightCardUiModel(
            id = 6L,
            title = "Rem Day demo",
            timeText = getInsightRelevantGeneratedTime(rawData.dateTime).second,
            chartKey = GraphsKey.TREND_SLEEP_TIMING_INTERNAL,
            payload = PayloadData(
                trendData = TrendGraphData(
                    list = dataList.first,
                    xAxisRange = xAxisRange,
                    yAxisRange = yAxisRange,
                    optimalRange = optimalRange,
                    maxDeviation = yAxisRange.last().first
                )
            ),
            raw = rawData
        )
    }

    fun generateTrendsGraphInsightsData(data: InsightItemResponseModel, id: Int): InsightCardUiModel? {
        if(data.graph_type==null) return null

        val period = when {
            data.graph_type.endsWith("week") -> InternalSelectedPeriod.WEEK
            data.graph_type.endsWith("month") -> InternalSelectedPeriod.MONTH
            else -> InternalSelectedPeriod.DAY
        }

        val contributor = when {
            data.graph_type.contains("rem_sleep", ignoreCase = true) -> SleepInternalLaunchState.REM_SLEEP
            data.graph_type.contains("deep_sleep", ignoreCase = true) -> SleepInternalLaunchState.DEEP_SLEEP //Verified
            data.graph_type.contains("sleep_efficiency", ignoreCase = true) -> SleepInternalLaunchState.EFFICIENCY
            data.graph_type.contains("total_duration", ignoreCase = true) -> SleepInternalLaunchState.SLEEP_DURATION //Verified
            data.graph_type.contains("latency", ignoreCase = true) -> SleepInternalLaunchState.LATENCY
            data.graph_type.contains("restfullness", ignoreCase = true) -> SleepInternalLaunchState.RESTFULNESS
            data.graph_type.contains("hrv", ignoreCase = true) -> SleepInternalLaunchState.HRV //Verified
            data.graph_type.contains("rhr", ignoreCase = true) -> SleepInternalLaunchState.RESTING_HEART_RATE
            data.graph_type.contains("avg_skin_temp", ignoreCase = true) -> SleepInternalLaunchState.SKIN_TEMPERATURE
            data.graph_type.contains("avg_oxy", ignoreCase = true) -> SleepInternalLaunchState.BLOOD_OXYGEN
            data.graph_type.contains("avg_respiration", ignoreCase = true) -> SleepInternalLaunchState.RESPIRATORY_RATE
            data.graph_type.contains("circadian_mid_point", ignoreCase = true) -> SleepInternalLaunchState.SLEEP_DURATION //Verified

            else -> SleepInternalLaunchState.DEEP_SLEEP
        }

        //
        fun convertData(
            data: List<TrendsValues>?,
            contributor: SleepInternalLaunchState
        ): List<GraphDataModel> {
            return data?.map {
                GraphDataModel(
                    date = LocalDate.parse(it.date),
                    value1 = if (contributor == SleepInternalLaunchState.REM_SLEEP ||
                        contributor == SleepInternalLaunchState.DEEP_SLEEP ||
                        contributor == SleepInternalLaunchState.SLEEP_DURATION
                    ) {
                        if (it.value1 == null) {
                            null
                        } else {
                            (it.value1 ?: 0.0f) / 60
                        }
                    } else if (contributor == SleepInternalLaunchState.RESTING_HEART_RATE) {
                        if (it.value1 == 255f) null else it.value1
                    } else {
                        it.value1
                    }
                )
            } ?: ArrayList()
        }

        val dataList = convertData(data.graph, contributor)

        val nonNullDataCount =
            getNonNullDataCount(contributor, dataList)

        val minMax = getMinMaxValue(
            dataListType1 = dataList,
            contributorType = contributor
        )

        val xAxisRange = getXAxisRangeInsights(data.graph, period)
        val yAxisRange = getYAxisRange(minMax.second, contributor)
        val optimalRange = getOptimalRangeMinMax(contributor)

        return InsightCardUiModel(
            id = id.toLong(),
            title = "Rem Day demo",
            timeText = getInsightRelevantGeneratedTime(data.dateTime).second,
            chartKey = GraphsKey.TREND_SLEEP_SINGLE,
            payload = PayloadData(
                trendData = TrendGraphData(
                    dataList,
                    yAxisRange,
                    null,
                    null,
                    -1,
                    contributor,
                    optimalRange,
                    nonNullDataCount,
                    xAxisRangeInsights = xAxisRange
                )
            ),
            raw = data
        )
        //
    }

    fun getXAxisRangeInsights(data: List<TrendsValues>?, period: InternalSelectedPeriod): List<String>{
        when(period){
            InternalSelectedPeriod.MONTH -> {
                try {
                    val monthList = ArrayList<String>()
                    data?.forEach {
                        val date = LocalDate.parse(it.date)
                        val month3 = date.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH))
                        monthList.add(
                            month3
                        )
                    }
                    return monthList
                }catch (e: Exception){
                    e
                }
            }

            InternalSelectedPeriod.DAY -> {
                return try{

                    fun dayName(dayInt: Int): String {
                        return when(dayInt){
                            1 -> resourcesProvider.getString(R.string.text_mon)
                            2 -> resourcesProvider.getString(R.string.text_tue)
                            3 -> resourcesProvider.getString(R.string.text_wed)
                            4 -> resourcesProvider.getString(R.string.text_thu)
                            5 -> resourcesProvider.getString(R.string.text_fri)
                            6 -> resourcesProvider.getString(R.string.text_sat)
                            else -> resourcesProvider.getString(R.string.text_sun)
                        }
                    }

                    val fmt = DateTimeFormatter.ISO_LOCAL_DATE
                    return data!!
                        .sortedBy { LocalDate.parse(it.date, fmt) }
                        .map { LocalDate.parse(it.date, fmt) }
                        .map { dayName(it.dayOfWeek.value) }

                }catch (_: Exception){
                    arrayListOf(
                        resourcesProvider.getString(R.string.text_mon),
                        resourcesProvider.getString(R.string.text_tue),
                        resourcesProvider.getString(R.string.text_wed),
                        resourcesProvider.getString(R.string.text_thu),
                        resourcesProvider.getString(R.string.text_fri),
                        resourcesProvider.getString(R.string.text_sat),
                        resourcesProvider.getString(R.string.text_sun)
                    )
                }
            }
            InternalSelectedPeriod.WEEK -> {
                data?.let {
                    val weekNoList = ArrayList<String>()
                    it.forEach {
                        val date = LocalDate.parse(it.date)
                        val weekNo = date.get(WeekFields.ISO.weekOfWeekBasedYear())
                        weekNoList.add("W$weekNo")
                    }
                    return weekNoList
                }
            }

            else -> {}
        }

        return emptyList()
    }

    fun getXAxisRange(
        pageData: List<TrendsValues>?,
        period: InternalSelectedPeriod
    ): List<LocalDate> {
        return when (period) {
            InternalSelectedPeriod.MONTH -> {
                val monthListString = ArrayList<LocalDate>()
                var lastYearMonth: YearMonth? = null
                pageData?.forEach {
                    val currentYearMonth = LocalDate.parse(it.date).yearMonth
                    if (lastYearMonth == null) {
                        lastYearMonth = currentYearMonth
                        monthListString.add(currentYearMonth.atDay(1))
                    } else if (lastYearMonth != currentYearMonth) {
                        lastYearMonth = currentYearMonth
                        monthListString.add(currentYearMonth.atDay(1))
                    }
                }
                return monthListString
            }

            InternalSelectedPeriod.WEEK -> {
                val weekListReturn = ArrayList<LocalDate>()

                var lastWeek: Int? = null
                pageData?.forEach {
                    val date = LocalDate.parse(it.date)

                    val weekFields = WeekFields.of(DayOfWeek.MONDAY, 7)
                    val weekNumber = date.get(weekFields.weekOfWeekBasedYear())

                    if (lastWeek == null) {
                        lastWeek = weekNumber
                        weekListReturn.add(date)
                    } else if (lastWeek != weekNumber) {
                        lastWeek = weekNumber
                        weekListReturn.add(date)
                    }
                }
                weekListReturn
            }

            else -> {
                val dayList = ArrayList<LocalDate>()
                pageData?.forEach {
                    dayList.add(LocalDate.parse(it.date))
                }
                return dayList
            }
        }
    }


    /**
     * Returns optimal range
     */
    fun getOptimalRangeMinMax(contributorType: SleepInternalLaunchState?): Pair<Float, Float>? {

        return when (contributorType) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> null
            SleepInternalLaunchState.SLEEP_PERFORMANCE -> Pair(85f, 100f)
            SleepInternalLaunchState.HOUR_VS_NEED -> null
            SleepInternalLaunchState.SLEEP_TIME -> null
            SleepInternalLaunchState.TIMING -> null
            SleepInternalLaunchState.EFFICIENCY -> Pair(85f, 100f)
            SleepInternalLaunchState.REM_SLEEP -> Pair(1.5f * 60.0f, 2 * 60.0f)
            SleepInternalLaunchState.DEEP_SLEEP -> Pair(1.5f * 60.0f, 2.25f * 60.0f)
            SleepInternalLaunchState.SLEEP_DURATION -> Pair(7 * 60.0f, 9 * 60.0f)
            SleepInternalLaunchState.LATENCY -> Pair(5f, 20f)
            SleepInternalLaunchState.RESTFULNESS -> Pair(0f, 2f)
            SleepInternalLaunchState.RESPIRATORY_RATE -> null
            SleepInternalLaunchState.RESTING_HEART_RATE -> null
            SleepInternalLaunchState.HRV -> null
            SleepInternalLaunchState.SKIN_TEMPERATURE -> null
            SleepInternalLaunchState.BLOOD_OXYGEN -> null
            null -> null
        }
    }


    fun getMaxValue(
        dataListType1: List<GraphDataModel>? = null,
        contributorType: SleepInternalLaunchState?
    ): Float {
        val nonNullValues = dataListType1?.mapNotNull { it.value1 }
        return when (contributorType) {
            SleepInternalLaunchState.SLEEP_PERFORMANCE -> 100.0f
            SleepInternalLaunchState.HOUR_VS_NEED,
            SleepInternalLaunchState.SLEEP_TIME -> {
                var mMax = 0.0f
                dataListType1?.forEach {

                    var max = it.value1 ?: 0.0f
                    if ((it.value2 ?: 0.0f) > max) {
                        max = it.value2 ?: 0.0f
                    }

                    if (max > mMax) {
                        mMax = max
                    }
                }

                mMax += ((0.2) * mMax).toInt()
                return mMax
            }

            SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                var mMax = 0.0f
                dataListType1?.forEach {
                    val sum = (it.value2 ?: 0.0f)
                    if (sum > mMax) {
                        mMax = sum
                    }
                }

                mMax += ((0.2) * mMax).toInt()

                return mMax
            }

            SleepInternalLaunchState.TIMING -> 100.0f
            SleepInternalLaunchState.EFFICIENCY -> {
                100.0f
            }

            SleepInternalLaunchState.DEEP_SLEEP, SleepInternalLaunchState.REM_SLEEP -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    60.0f
                } else {
                    nonNullValues.max()
                }
            }

            SleepInternalLaunchState.SLEEP_DURATION -> {
                if (nonNullValues.isNullOrEmpty()) {
                    12 * 60.0f
                } else {
                    nonNullValues.max()
                }
            }

            SleepInternalLaunchState.LATENCY -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    25.0f
                } else {
                    nonNullValues.max()
                }
            }

            SleepInternalLaunchState.RESTFULNESS -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    4.0f
                } else {
                    nonNullValues.max()
                }
            }

            SleepInternalLaunchState.RESPIRATORY_RATE -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    20.0f
                } else {
                    nonNullValues.max()
                }
            }

            SleepInternalLaunchState.RESTING_HEART_RATE -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    80.0f
                } else {
                    nonNullValues.max()
                }
            }

            SleepInternalLaunchState.HRV -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    80.0f
                } else {
                    nonNullValues.max()
                }
            }

            SleepInternalLaunchState.SKIN_TEMPERATURE -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    120.0f
                } else {
                    nonNullValues.max()
                }
            }

            null -> 100.0f
            else -> 100.0f
        }
    }

    fun getYAxisRange(
        maxValue: Float,
        contributorType: SleepInternalLaunchState?,
        minValue: Float = 0f,
    ): List<Pair<Int, String>> {
        val offset = 4
        val default = arrayListOf(
            Pair(0, "0%"),
            Pair(25, "25%"),
            Pair(50, "50%"),
            Pair(75, "75%"),
            Pair(100, "100%")
        )
        return when (contributorType) {
            SleepInternalLaunchState.SKIN_TEMPERATURE,
            SleepInternalLaunchState.RESTING_HEART_RATE,

            SleepInternalLaunchState.HRV, SleepInternalLaunchState.RESPIRATORY_RATE -> {
                val newMax = maxValue + offset
                var newMin = minValue - offset
                if (newMin < 0) {
                    newMin = 0f
                }

                val step = ((newMax - newMin) / 4).roundToInt()
                val yAxis = (0..4).map { i ->
                    val value = (newMin + i * step).roundToInt()
                    value to value.toString()
                }
                return yAxis
            }

            SleepInternalLaunchState.HOUR_VS_NEED, SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                return when (maxValue) {
                    in 0.0f..360.0f -> {
                        arrayListOf(
                            Pair(0, "0"),
                            Pair(120, "2"),
                            Pair(240, "4"),
                            Pair(360, "6")
                        )
                    }

                    in 0.0f..720.0f -> {
                        arrayListOf(
                            Pair(0, "0"),
                            Pair(180, "3"),
                            Pair(360, "6"),
                            Pair(540, "9"),
                            Pair(720, "12")
                        )
                    }

                    else -> {
                        arrayListOf(
                            Pair(0, "0"),
                            Pair(360, "6"),
                            Pair(720, "12"),
                            Pair(1080, "18"),
                            Pair(1440, "24")
                        )
                    }
                }
            }

            SleepInternalLaunchState.SLEEP_TIME -> default
            SleepInternalLaunchState.TIMING -> {
                return if (maxValue <= (6 * 60)) {
                    arrayListOf(
                        Pair(-6 * 60, "6 PM"),
                        Pair(-3 * 60, "9 PM"),
                        Pair(0, "12 AM"),
                        Pair(3 * 60, "3 AM"),
                        Pair(6 * 60, "6 AM")
                    )
                } else {
                    arrayListOf(
                        Pair(-12 * 60, "12 PM"),
                        Pair(-6 * 60, "6 PM"),
                        Pair(0, "12 AM"),
                        Pair(6 * 60, "6 AM"),
                        Pair(12 * 60, "12 PM")
                    )
                }
            }

            SleepInternalLaunchState.REM_SLEEP, SleepInternalLaunchState.DEEP_SLEEP -> {
                val newMax = maxValue + offset
                var newMin = minValue - offset
                if (newMin < 0) {
                    newMin = 0f
                }

                val step = ((newMax - newMin) / 4).roundToInt()
                val yAxis = (0..4).map { i ->
                    val value = (newMin + i * step).roundToInt()

                    val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(value)
                    value to String.format(locale = Locale.US, "%dh %02dm", hour, minute)
                }
                return yAxis
            }

            SleepInternalLaunchState.SLEEP_DURATION -> {
                if (maxValue <= 12 * 60.0f) {
                    arrayListOf(
                        Pair(0, "0"),
                        Pair(3 * 60, "3"),
                        Pair(6 * 60, "6"),
                        Pair(9 * 60, "9"),
                        Pair(12 * 60, "12")
                    )
                } else {
                    arrayListOf(
                        Pair(0, "0"),
                        Pair(6 * 60, "6"),
                        Pair(12 * 60, "12"),
                        Pair(18 * 60, "18"),
                        Pair(24 * 60, "24")
                    )
                }
            }

            SleepInternalLaunchState.LATENCY -> {

                val newMax = maxValue + offset
                var newMin = minValue - offset
                if (newMin < 0) {
                    newMin = 0f
                }

                val step = ((newMax - newMin) / 4).roundToInt()
                val yAxis = (0..4).map { i ->
                    val value = (newMin + i * step).roundToInt()
                    value to value.toString()
                }
                return yAxis
            }

            SleepInternalLaunchState.RESTFULNESS -> {

                val newMax = maxValue + offset
                var newMin = minValue - offset
                if (newMin < 0) {
                    newMin = 0f
                }

                val step = ((newMax - newMin) / 4).roundToInt()
                val yAxis = (0..4).map { i ->
                    val value = (newMin + i * step).roundToInt()
                    value to value.toString()
                }
                return yAxis
            }

            SleepInternalLaunchState.BLOOD_OXYGEN,
            SleepInternalLaunchState.SLEEP_PERFORMANCE,
            SleepInternalLaunchState.EFFICIENCY -> {
                var newMax = maxValue + offset
                if (newMax > 100f) {
                    newMax = 100f
                }
                var newMin = minValue - offset
                if (newMin < 0f) {
                    newMin = 0f
                }

                val yAxis = ArrayList<Pair<Int, String>>()
                val step = ((newMax - newMin) / 4).roundToInt()
                var lastValue = newMax.roundToInt()
                for (i in 4 downTo 0) {
                    if (lastValue >= 0) {
                        yAxis.add(Pair(lastValue, "$lastValue%"))
                    }
                    val value = lastValue - step
                    lastValue = value
                }
                return yAxis.reversed()
            }

            else -> default
        }
    }

    fun getAvgValuePair(
        value: Float?,
        contributorType: SleepInternalLaunchState?
    ): Pair<Float, String>? {
        if (value == null) {
            return null
        }

        return when (contributorType) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP,
            SleepInternalLaunchState.SLEEP_TIME,
            SleepInternalLaunchState.TIMING,
            SleepInternalLaunchState.HOUR_VS_NEED -> Pair(value, "$value%")

            SleepInternalLaunchState.EFFICIENCY, SleepInternalLaunchState.SLEEP_PERFORMANCE -> Pair(
                value,
                "${value.roundToInt()}%"
            )

            SleepInternalLaunchState.REM_SLEEP, SleepInternalLaunchState.DEEP_SLEEP -> {
                val min = value.div(60)
                val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(min.roundToInt())
                /*val text = if (hour == 0) {
                    "${minute}m"
                } else {
                    "${hour}h${minute}m"
                }*/
                Pair(min, String.format(locale = Locale.US, "%02dh %02dm", hour, minute))
            }

            SleepInternalLaunchState.LATENCY -> {
                Pair(value, "${value.roundToInt()}min")
            }

            SleepInternalLaunchState.SLEEP_DURATION -> {
                val minValue = (value / 60)
                val (hour, min) = ApplicationUtils.getFormattedSleepDuration(minValue.roundToInt())
                Pair(minValue, String.format(locale = Locale.US, "%d:%02d", hour, min))
            }

            SleepInternalLaunchState.SKIN_TEMPERATURE -> {
                if (sessionManager.isMetric()) {
                    val convertedValue = AppConversionUtils.fahrenheitToCelsius(
                        value
                    )

                    Pair(
                        convertedValue,
                        String.format(
                            locale = Locale.US,
                            "%.1f",
                            convertedValue,
                        )
                    )
                } else {
                    Pair(
                        value,
                        String.format(
                            locale = Locale.US,
                            "%.1f",
                            value
                        )
                    )
                }
            }

            SleepInternalLaunchState.RESTING_HEART_RATE,
            SleepInternalLaunchState.RESTFULNESS,
            SleepInternalLaunchState.BLOOD_OXYGEN,
            SleepInternalLaunchState.RESPIRATORY_RATE,
            SleepInternalLaunchState.HRV -> Pair(
                value,
                "${value.roundToInt()}"
            )

            else -> Pair(value, "$value%")
        }

    }

    /**
     * Returns Pair(min,max)
     */
    fun getMinMaxValue(
        dataListType1: List<GraphDataModel>? = null,
        contributorType: SleepInternalLaunchState?
    ): Pair<Float, Float> {
        val nonNullValues = dataListType1?.mapNotNull { it.value1 }
        return when (contributorType) {
            SleepInternalLaunchState.HOUR_VS_NEED,
            SleepInternalLaunchState.SLEEP_TIME -> {
                var mMax = 0.0f
                dataListType1?.forEach {

                    var max = it.value1 ?: 0.0f
                    if ((it.value2 ?: 0.0f) > max) {
                        max = it.value2 ?: 0.0f
                    }

                    if (max > mMax) {
                        mMax = max
                    }
                }

                mMax += ((0.2) * mMax).toInt()
                return Pair(0f, mMax)
            }

            SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                var mMax = 0.0f
                dataListType1?.forEach {
                    val sum = (it.value2 ?: 0.0f)
                    if (sum > mMax) {
                        mMax = sum
                    }
                }

                mMax += ((0.2) * mMax).toInt()

                return Pair(0f, mMax)
            }

            SleepInternalLaunchState.TIMING -> Pair(0f, 100.0f)

            SleepInternalLaunchState.DEEP_SLEEP, SleepInternalLaunchState.REM_SLEEP -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    Pair(0f, 60.0f)
                } else {
                    Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                }
            }

            SleepInternalLaunchState.SLEEP_DURATION -> {
                if (nonNullValues.isNullOrEmpty()) {
                    Pair(0f, 12 * 60.0f)
                } else {
                    Pair(0f, nonNullValues.max())
                }
            }

            SleepInternalLaunchState.LATENCY -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    Pair(0f, 25.0f)
                } else {
                    Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                }
            }

            SleepInternalLaunchState.RESTFULNESS -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    Pair(0f, 4.0f)
                } else {
                    Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                }
            }

            SleepInternalLaunchState.RESPIRATORY_RATE -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    Pair(0f, 20.0f)
                } else {
                    Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                }
            }

            SleepInternalLaunchState.RESTING_HEART_RATE, SleepInternalLaunchState.HRV -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    Pair(0f, 80.0f)
                } else {
                    Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                }
            }

            SleepInternalLaunchState.SKIN_TEMPERATURE -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    if (sessionManager.isMetric()) {
                        Pair(0f, 48.0f)
                    } else {
                        Pair(0f, 120.0f)
                    }
                } else {
                    Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                }
            }

            SleepInternalLaunchState.SLEEP_PERFORMANCE,
            SleepInternalLaunchState.BLOOD_OXYGEN,
            SleepInternalLaunchState.EFFICIENCY -> {
                return if (nonNullValues.isNullOrEmpty()) {
                    Pair(0f, 100.0f)
                } else {
                    Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                }
            }

            null -> Pair(0f, 100.0f)
            else -> Pair(0f, 100.0f)
        }
    }

    fun getNonNullDataCount(
        contributorType: SleepInternalLaunchState?,
        dataList: List<GraphDataModel>
    ): Int {

        if (contributorType == SleepInternalLaunchState.SLEEP_DURATION ||
            contributorType == SleepInternalLaunchState.REM_SLEEP ||
            contributorType == SleepInternalLaunchState.DEEP_SLEEP
        ) {
            val filteredData = dataList.filter {
                it.value1 != null && it.value1 != 0.0f
            }
            return filteredData.size

        } else {
            val filteredData = dataList.filter {
                it.value1 != null
            }
            return filteredData.size
        }
    }

    fun getInsightRelevantGeneratedTime(dateTime: String?): Pair<Int, String>{ // Int - Quantity - 1/2/3 , String - will be : seconds/minutes/hour(s)/day(s)
        try {
            // Define the format of the incoming date-time string
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            // Parse the provided date-time string into a Date object
            val inputDate = dateFormat.parse(dateTime)

            // Get the current date and time
            val currentDate = Date()

            // Calculate the difference in milliseconds
            val diffInMillis = currentDate.time - inputDate.time

            // Determine the most appropriate time unit and return the quantity and unit as a Pair
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
            val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
            val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)

            return when {
                seconds < 60 -> Pair(
                    seconds.toInt(),
                    "Just Now"
                )

                minutes < 60 -> {
                    val mins = minutes.toInt()
                    Pair(
                        mins,
                        if(mins==1) "$mins min ago"
                        else "$mins mins ago"
                        )
                }

                hours < 24 -> {
                    val hours = hours.toInt()
                    Pair(
                        hours,
                        if(hours==1) "$hours hr ago"
                        else "$hours hrs ago"
                    )
                }

                else -> {
                    val days = days.toInt()
                    Pair(
                        days,
                        if(days==1) "$days day ago"
                        else "$days days ago"
                    )
                }
            }
        }catch (_: Exception){
            return Pair(0, "Unknown")
        }
    }

    fun getBarPlotColorData(
        pageData: InsightItemResponseModel,
        contributor: SleepInternalLaunchState,
        unitLabel: String,
        id: Int
    ): InsightCardUiModel? {
        if (pageData.graph_type == null) return null

        val period = when {
            pageData.graph_type.endsWith("week") -> InternalSelectedPeriod.WEEK
            pageData.graph_type.endsWith("month") -> InternalSelectedPeriod.MONTH
            else -> InternalSelectedPeriod.DAY
        }

        fun convertData(data: List<TrendsValues>?): List<GraphDataModel> {
            return data?.map {
                GraphDataModel(
                    date = LocalDate.parse(it.date),
                    value1 = if (contributor == SleepInternalLaunchState.SLEEP_DURATION ||
                        contributor == SleepInternalLaunchState.REM_SLEEP ||
                        contributor == SleepInternalLaunchState.DEEP_SLEEP
                    ) {
                        if (it.value1 != null) {
                            (it.value1 ?: 0.0f) / 60
                        } else null
                    } else if (it.value1 != null && contributor == SleepInternalLaunchState.SKIN_TEMPERATURE && sessionManager.isMetric()) {
                        val convertedValue = AppConversionUtils.fahrenheitToCelsius(
                            it.value1!!
                        )
                        if (convertedValue < 0) {
                            0.0f
                        } else {
                            convertedValue
                        }
                    } else if (contributor == SleepInternalLaunchState.RESPIRATORY_RATE
                        || contributor == SleepInternalLaunchState.RESTING_HEART_RATE
                        || contributor == SleepInternalLaunchState.HRV
                    ) {
                        if (it.value1 == 255f) null else it.value1
                    } else {
                        it.value1
                    }
                )
            } ?: ArrayList()
        }

        fun getNonNullDataCount(
            contributorType: SleepInternalLaunchState?,
            dataList: List<GraphDataModel>
        ): Int {

            if (contributorType == SleepInternalLaunchState.SLEEP_DURATION ||
                contributorType == SleepInternalLaunchState.REM_SLEEP ||
                contributorType == SleepInternalLaunchState.DEEP_SLEEP
            ) {
                val filteredData = dataList.filter {
                    it.value1 != null && it.value1 != 0.0f
                }
                return filteredData.size

            } else {
                val filteredData = dataList.filter {
                    it.value1 != null
                }
                return filteredData.size
            }
        }

        fun getMinMaxValue(
            dataListType1: List<GraphDataModel>? = null,
            contributorType: SleepInternalLaunchState?
        ): Pair<Float, Float> {
            val nonNullValues = dataListType1?.mapNotNull { it.value1 }
            return when (contributorType) {
                SleepInternalLaunchState.HOUR_VS_NEED,
                SleepInternalLaunchState.SLEEP_TIME -> {
                    var mMax = 0.0f
                    dataListType1?.forEach {

                        var max = it.value1 ?: 0.0f
                        if ((it.value2 ?: 0.0f) > max) {
                            max = it.value2 ?: 0.0f
                        }

                        if (max > mMax) {
                            mMax = max
                        }
                    }

                    mMax += ((0.2) * mMax).toInt()
                    return Pair(0f, mMax)
                }

                SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                    var mMax = 0.0f
                    dataListType1?.forEach {
                        val sum = (it.value2 ?: 0.0f)
                        if (sum > mMax) {
                            mMax = sum
                        }
                    }

                    mMax += ((0.2) * mMax).toInt()

                    return Pair(0f, mMax)
                }

                SleepInternalLaunchState.TIMING -> Pair(0f, 100.0f)

                SleepInternalLaunchState.DEEP_SLEEP, SleepInternalLaunchState.REM_SLEEP -> {
                    return if (nonNullValues.isNullOrEmpty()) {
                        Pair(0f, 60.0f)
                    } else {
                        Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                    }
                }

                SleepInternalLaunchState.SLEEP_DURATION -> {
                    if (nonNullValues.isNullOrEmpty()) {
                        Pair(0f, 12 * 60.0f)
                    } else {
                        Pair(0f, nonNullValues.max())
                    }
                }

                SleepInternalLaunchState.LATENCY -> {
                    return if (nonNullValues.isNullOrEmpty()) {
                        Pair(0f, 25.0f)
                    } else {
                        Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                    }
                }

                SleepInternalLaunchState.RESTFULNESS -> {
                    return if (nonNullValues.isNullOrEmpty()) {
                        Pair(0f, 4.0f)
                    } else {
                        Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                    }
                }

                SleepInternalLaunchState.RESPIRATORY_RATE -> {
                    return if (nonNullValues.isNullOrEmpty()) {
                        Pair(0f, 20.0f)
                    } else {
                        Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                    }
                }

                SleepInternalLaunchState.RESTING_HEART_RATE, SleepInternalLaunchState.HRV -> {
                    return if (nonNullValues.isNullOrEmpty()) {
                        Pair(0f, 80.0f)
                    } else {
                        Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                    }
                }

                SleepInternalLaunchState.SKIN_TEMPERATURE -> {
                    return if (nonNullValues.isNullOrEmpty()) {
                        if (sessionManager.isMetric()) {
                            Pair(0f, 48.0f)
                        } else {
                            Pair(0f, 120.0f)
                        }
                    } else {
                        Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                    }
                }

                SleepInternalLaunchState.SLEEP_PERFORMANCE,
                SleepInternalLaunchState.BLOOD_OXYGEN,
                SleepInternalLaunchState.EFFICIENCY -> {
                    return if (nonNullValues.isNullOrEmpty()) {
                        Pair(0f, 100.0f)
                    } else {
                        Pair(nonNullValues.filter { it != 0f }.minOrNull() ?: 0f, nonNullValues.max())
                    }
                }

                null -> Pair(0f, 100.0f)
                else -> Pair(0f, 100.0f)
            }
        }

        fun getBarXAxisRange(pageData: TrendsGraphData?): List<LocalDate> =
             when (period) {
                InternalSelectedPeriod.MONTH -> {
                    val monthListString = ArrayList<LocalDate>()
                    var lastYearMonth: YearMonth? = null
                    pageData?.data?.forEach {
                        val currentYearMonth = LocalDate.parse(it.date).yearMonth
                        if (lastYearMonth == null) {
                            lastYearMonth = currentYearMonth
                            monthListString.add(currentYearMonth.atDay(1))
                        } else if (lastYearMonth != currentYearMonth) {
                            lastYearMonth = currentYearMonth
                            monthListString.add(currentYearMonth.atDay(1))
                        }
                    }
                    monthListString
                }

                InternalSelectedPeriod.WEEK -> {
                    val weekListReturn = ArrayList<LocalDate>()

                    var lastWeek: Int? = null
                    pageData?.data?.forEach {
                        val date = LocalDate.parse(it.date)

                        val weekFields = WeekFields.of(DayOfWeek.MONDAY, 7)
                        val weekNumber = date.get(weekFields.weekOfWeekBasedYear())

                        if (lastWeek == null) {
                            lastWeek = weekNumber
                            weekListReturn.add(date)
                        } else if (lastWeek != weekNumber) {
                            lastWeek = weekNumber
                            weekListReturn.add(date)
                        }
                    }
                    weekListReturn
                }

                else -> {
                    val dayList = ArrayList<LocalDate>()
                    pageData?.data?.forEach {
                        dayList.add(LocalDate.parse(it.date))
                    }
                    dayList
                }
            }

        val dataList = convertData(pageData.graph)
        val nonNullDataCount = getNonNullDataCount(contributor, dataList)

        val minMax = getMinMaxValue(
            dataListType1 = dataList,
            contributorType = contributor
        )
        val yAxisRange = getYAxisRange(minMax.second, contributor, minValue = minMax.first)
        val xAxisRange = getBarXAxisRange(TrendsGraphData().apply {
            data = pageData.graph
            contributorType = contributor
            selectedPeriod = period
        })

        val showOverlay = period != InternalSelectedPeriod.DAY

        return InsightCardUiModel(
            id = id.toLong(),
            title = "Rem Day demo",
            timeText = getInsightRelevantGeneratedTime(pageData.dateTime).second,
            chartKey = GraphsKey.BAR_PLOT_COLOR,
            payload = PayloadData(
                timeSeriesPayload = TimeSeriesPayload(
                    list = dataList,
                    yAxisRange = yAxisRange,
                    xAxisRange = xAxisRange,
                    avgValue = null,
                    showOverlay = showOverlay,
                    selectedPeriod = period,
                    nonNullDataCount = nonNullDataCount,
                    unitLabel = unitLabel
                )
            ),
            raw = pageData
        )
    }
}

enum class GraphsKey {
    HEART_RATE, STRESS, DAY_TIME_MOVEMENT, SLEEP_BREAKUP, SLEEP_MOVEMENT,
    TREND_SLEEP_SINGLE, TREND_SLEEP_SINGLE_LINE_GRADIENT, TREND_SLEEP_TIMING_INTERNAL,
    TREND_SLEEP_MULTI_BAR, TREND_SLEEP_HOUR_VS_NEED_CHARD_INTERNAL, BAR_PLOT_COLOR
}
