package com.oreo.ui.sleep2.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.yearMonth
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChartModel
import com.oreo.data.model.ResultData
import com.oreo.data.model.TrendsGraphData
import com.oreo.ui.custom.sleep.internal.GraphDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToInt


const val DEFAULT_LONG_PRESS_TIMEOUT = 200L

@HiltViewModel
class OSPTrendsSharedViewModel @Inject constructor(
    val sessionManager: SessionManager
) : BaseViewModel() {

    private val _interactGraphData =
        MutableLiveData<LocalDate?>()

    /**
     * Triple(Date,data,time)
     */
    private val _interactGraphDataDaily =
        MutableLiveData<Triple<LocalDate?, Float?, String?>?>()

    val interactGraphData: LiveData<LocalDate?> = _interactGraphData

    val interactGraphDataDaily: LiveData<Triple<LocalDate?, Float?, String?>?> =
        _interactGraphDataDaily

    var calendarStartDate: LocalDate = LocalDate.now().minusMonths(1)

    fun sendInteractDay(day: LocalDate?) {
        _interactGraphData.postValue(day)
    }

    fun sendInteractDaily(day: LocalDate?, data: Float?, time: String?) {
        if (day == null) {
            _interactGraphDataDaily.postValue(null)
        } else {
            _interactGraphDataDaily.postValue(Triple(day, data, time))
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
            SleepInternalLaunchState.TIMING -> default

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
                    val text = if (hour == 0) {
                        "${minute}m"
                    } else {
                        "${hour}h${minute}m"
                    }

                    value to text
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
                val text = if (hour == 0) {
                    "${minute}m"
                } else {
                    "${hour}h${minute}m"
                }
                Pair(min, text)
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

    fun getAvgValue(
        dataListType1: List<GraphDataModel>? = null,
        dataListType2: List<Pair<Int?, Int?>>? = null,
        contributorType: SleepInternalLaunchState?
    ): Pair<Float, String>? {
        val filteredData = dataListType1?.mapNotNull { it.value1 }
        if (filteredData.isNullOrEmpty()) {
            return null
        }

        val avg = filteredData.average().toFloat()
        return when (contributorType) {
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> Pair(avg, "$avg%")

            SleepInternalLaunchState.HOUR_VS_NEED -> Pair(avg, "$avg%")
            SleepInternalLaunchState.SLEEP_TIME -> Pair(avg, "$avg%")
            SleepInternalLaunchState.TIMING -> Pair(avg, "$avg%")
            SleepInternalLaunchState.EFFICIENCY, SleepInternalLaunchState.SLEEP_PERFORMANCE -> Pair(
                avg,
                "${avg.roundToInt()}%"
            )

            SleepInternalLaunchState.REM_SLEEP, SleepInternalLaunchState.DEEP_SLEEP, SleepInternalLaunchState.LATENCY -> {
                Pair(avg, "${avg.roundToInt()}min")
            }

            SleepInternalLaunchState.SLEEP_DURATION -> {
                val (hour, min) = ApplicationUtils.getFormattedSleepDuration(avg.roundToInt())
                Pair(avg, String.format(locale = Locale.US, "%d:%02d", hour, min))
            }

            SleepInternalLaunchState.RESTFULNESS, SleepInternalLaunchState.RESPIRATORY_RATE,
            SleepInternalLaunchState.BLOOD_OXYGEN, SleepInternalLaunchState.SKIN_TEMPERATURE -> {
                Pair(
                    avg,
                    String.format(locale = Locale.US, "%.1f", avg)
                )
            }

            SleepInternalLaunchState.RESTING_HEART_RATE,
            SleepInternalLaunchState.HRV -> Pair(
                avg,
                "${avg.roundToInt()}"
            )

            else -> Pair(avg, "$avg%")
        }
    }

    fun getXAxisRange(pageData: TrendsGraphData?): List<LocalDate> {
        return when (pageData?.selectedPeriod) {
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
                return monthListString
            }

            InternalSelectedPeriod.WEEK -> {
                val weekListReturn = ArrayList<LocalDate>()

                var lastWeek: Int? = null
                pageData.data?.forEach {
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
                return dayList
                //arrayListOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            }
        }
    }

    fun getPrefixAndSuffixListTemp(
        dataList: ArrayList<ResultData>,
    ): Triple<Pair<ArrayList<ChartModel>, Int>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
        val list = java.util.ArrayList<ChartModel>()
        var max = 10
        dataList.reversed().forEach {

            val chartModel = ChartModel()
            chartModel.date = it.date
            chartModel.valueFloat2 =
                if (it.data != 0.0f) {
                    if (sessionManager.isMetric()) AppConversionUtils.fahrenheitToCelsius(it.data) else it.data
                } else {
                    it.data
                }

            chartModel.index =
                DateFormats.parseDate(
                    it.date,
                    DateFormats.dateFormat3(),
                    DateFormats.dateFormatDay()
                )

            chartModel.valueFloat =
                    /*if (sessionManager.isMetric()) AppConversionUtils.fahrenheitToCelsius(
                        32 + (it.deviation ?: 0.0f)
                    ) else*/ it.deviation ?: 0.0f

            list.add(chartModel)
        }


        val suffix = java.util.ArrayList<ChartModel>()
        for (i in 1..15) {
            val chartModel = ChartModel()
            chartModel.index = ""
            chartModel.value = 0
            chartModel.date = ""
            suffix.add(chartModel)
        }

        val prefix = java.util.ArrayList<ChartModel>()
        for (i in 1..15) {
            val chartModel = ChartModel()
            chartModel.index = ""
            chartModel.value = 0
            chartModel.date = ""
            prefix.add(chartModel)
        }
        return Triple(Pair(list, max), suffix, prefix)
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

}