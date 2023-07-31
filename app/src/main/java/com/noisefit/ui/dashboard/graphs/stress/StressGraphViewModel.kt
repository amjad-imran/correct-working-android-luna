package com.noisefit.ui.dashboard.graphs.stress

import com.github.mikephil.charting.data.BarEntry
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.R
import com.noisefit_commans.data.model.MarkerEntry
import com.noisefit_commans.data.model.history.StressHistory
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.util.graph.StressBarChartUtils
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt


@HiltViewModel
class StressGraphViewModel
@Inject
constructor(
    private val localDataStore: DataStoredInterface
) : BaseViewModel() {


    var range = ""
    var steps = 0
    var historyType: String = ""
    var date: String = ""
    var markEntryList = ArrayList<MarkerEntry>()
    var stressDataList: ArrayList<StressHistory> = ArrayList()


    fun getXAxisMarker(graphInterval: GraphInterval): ArrayList<String> {
        var monthlyList = ArrayList<String>()
        if (graphInterval == GraphInterval.MONTH) {
            monthlyList = getMonthList(stressDataList)
        } else if (graphInterval == GraphInterval.WEEK) {
            monthlyList = getWeekly(stressDataList)
        }
        return monthlyList
    }

    private fun getMonthList(stressDataList: ArrayList<StressHistory>): ArrayList<String> {
        val monthlyList = ArrayList<String>()
        stressDataList.forEach { stepData ->
            if (stepData.date != null) {
                monthlyList.add(DateFormats.formatMonthly(stepData.date))
            }
        }
        return monthlyList
    }

    private fun getWeekly(stressDataList: ArrayList<StressHistory>): ArrayList<String> {
        val monthlyList = ArrayList<String>()
        stressDataList.forEach { stepData ->
            if (stepData.date != null) {
                monthlyList.add(DateFormats.formatWeek(stepData.date))
            }
        }
        return monthlyList
    }


    private fun getEmptyHourList(): Triple<ArrayList<BarEntry>, ArrayList<Int>, ArrayList<MarkerEntry>> {
        val colors = ArrayList<Int>()
        val markEntryList = ArrayList<MarkerEntry>()
        val list = ArrayList<BarEntry>()
        for (i in 0..23) {
            val markerEntry = MarkerEntry()
            markerEntry.date = getHours(i)
            markerEntry.type = "_"
            markerEntry.value = "0"

            colors.add(NoiseFitApplicationMain.context!!.resources.getColor(R.color.steps_color))
            list.add(BarEntry(i.toFloat(), 0.0f))
            markEntryList.add(markerEntry)
        }

        return Triple(list, colors, markEntryList)
    }


    fun parseGraph(
        stressDataList: ArrayList<StressHistory>,
        graphInterval: GraphInterval
    ): Triple<ArrayList<BarEntry>, ArrayList<Int>, ArrayList<MarkerEntry>>? {
        when (graphInterval) {
            GraphInterval.YEAR -> {
                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<BarEntry>()

                stressDataList.forEachIndexed { index, stepData ->
                    val markerEntry = MarkerEntry()
                    val count = stepData.count ?: 0
                    markerEntry.date = getMonth(index)
                    markerEntry.type = StressBarChartUtils.stressType(count)


                    markerEntry.value = count.toString()

                    list.add(BarEntry(index.toFloat(), count.toFloat()))
                    markEntryList.add(markerEntry)
                }
                val colors = ArrayList<Int>()
                colors.add(NoiseFitApplicationMain.context!!.resources.getColor(R.color.steps_color))
                return Triple(list, colors, markEntryList)
            }
            GraphInterval.MONTH -> {
                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<BarEntry>()

                stressDataList.forEachIndexed { index, stepData ->
                    val count = stepData.count ?: 0

                    val markerEntry = MarkerEntry()
                    markerEntry.date = DateFormats.formatDateTime(
                        stepData.date,
                        DateFormats.dateFormat3,
                        DateFormats.dateTimeFormatWithWeekDay
                    )

                    markerEntry.type = StressBarChartUtils.stressType(count)

                    markerEntry.value = count.toString()

                    list.add(BarEntry(index.toFloat(), count.toFloat()))

                    markEntryList.add(markerEntry)
                }
                val colors = ArrayList<Int>()
                colors.add(NoiseFitApplicationMain.context!!.resources.getColor(R.color.steps_color))
                return Triple(list, colors, markEntryList)
            }
            GraphInterval.WEEK -> {
                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<BarEntry>()

                stressDataList.forEachIndexed { index, stepData ->
                    val count = stepData.count ?: 0
                    val markerEntry = MarkerEntry()
                    markerEntry.date = DateFormats.formatDateTime(
                        stepData.date,
                        DateFormats.dateFormat3,
                        DateFormats.dateTimeFormatWithWeekDay
                    )
                    markerEntry.type = StressBarChartUtils.stressType(count)

                    markerEntry.value = count.toString()

                    list.add(BarEntry(index.toFloat(), count.toFloat()))
                    markEntryList.add(markerEntry)
                }
                val colors = ArrayList<Int>()
                colors.add(NoiseFitApplicationMain.context!!.resources.getColor(R.color.steps_color))

                return Triple(list, colors, markEntryList)
            }
            GraphInterval.DAY -> {
                if (stressDataList.isNullOrEmpty()) {
                    return getEmptyHourList()
                }
                val hmStressData = HashMap<Int, ArrayList<Int>>()
                stressDataList.forEach { stress ->
                    val timeIn24HoursFormat = DateFormats.formatTimeInto24HoursValue(stress.time)
                    if (timeIn24HoursFormat.isNotEmpty() && stress.value != 0) {
                        val timeInInt = timeIn24HoursFormat.toInt()
                        if (hmStressData.containsKey(timeInInt)) {
                            val heartValueList = hmStressData[timeInInt]!!
                            heartValueList.add(stress.value ?: 0)
                            hmStressData[timeInInt] = heartValueList
                        } else {
                            val heartValueList = ArrayList<Int>()
                            heartValueList.add(stress.value ?: 0)
                            hmStressData[timeInInt] = heartValueList
                        }

                    }

                }


                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<BarEntry>()

                val colors = ArrayList<Int>()


                for (time in 0..23) {

                    var count = 0

                    if (hmStressData.containsKey(time)) {
                        count = hmStressData[time]?.average()?.roundToInt() ?: 0

                    }


                    val markerEntry = MarkerEntry()
                    markerEntry.date = getHours(time)
                    markerEntry.type = StressBarChartUtils.stressType(count)
                    markerEntry.value = count.toString()

                    list.add(BarEntry(time.toFloat(), count.toFloat()))
                    markEntryList.add(markerEntry)
                    colors.add(
                        NoiseFitApplicationMain.context!!.resources.getColor(
                            StressBarChartUtils.getStressColor(count)
                        )
                    )


                }




                return Triple(list, colors, markEntryList)
            }
        }
        return null
    }


    private fun getMonth(month: Int): String {
        val year = 2022
        when (month) {
            0 -> {
                return "Jan $year"
            }
            1 -> {
                return "Feb $year"
            }
            2 -> {
                return "Mar $year"
            }
            3 -> {
                return "Apr $year"
            }
            4 -> {
                return "May $year"
            }
            5 -> {
                return "Jun $year"
            }
            6 -> {
                return "Jul $year"
            }
            7 -> {
                return "Aug $year"
            }
            8 -> {
                return "Sep $year"
            }
            9 -> {
                return "Oct $year"
            }
            10 -> {
                return "Nov $year"
            }
            11 -> {
                return "Dec $year"
            }

            else -> {
                return "$year"
            }
        }
    }

    private fun getHours(hour: Int): String {
        return when {
            hour == 0 -> {
                "${12}pm - ${hour + 1}am"
            }
            hour < 11 -> {
                "${hour}am - ${hour + 1}am"
            }
            hour == 11 -> {
                "${hour}am - ${hour + 1}pm"
            }
            hour == 12 -> {
                val lastHr = hour - 12
                "${hour}pm - ${lastHr + 1}pm"
            }
            else -> {
                val lastHr = hour - 12
                "${lastHr}pm - ${lastHr + 1}pm"
            }
        }
    }

    fun getGraphInterval(historyType: String): GraphInterval {
        when (historyType) {
            "yearly" -> {
                return GraphInterval.YEAR
            }
            "daily" -> {
                return GraphInterval.DAY
            }
            "weekly" -> {
                return GraphInterval.WEEK
            }
            "monthly" -> {
                return GraphInterval.MONTH
            }
        }
        return GraphInterval.DAY
    }


}