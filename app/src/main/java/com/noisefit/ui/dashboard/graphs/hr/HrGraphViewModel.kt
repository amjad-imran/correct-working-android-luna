package com.noisefit.ui.dashboard.graphs.hr

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.noisefit_commans.data.model.MarkerEntry
import com.noisefit_commans.data.model.history.HrBreakup
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class HrGraphViewModel
@Inject
constructor() : BaseViewModel() {

    var markEntryList = ArrayList<MarkerEntry>()
    var average = 0
    var stats = ""
    var historyType: String = ""
    var date: String = ""
    var hrDataList: ArrayList<HrBreakup> = ArrayList()

    fun getXAxisMarker(graphInterval: GraphInterval): ArrayList<String> {
        var monthlyList = ArrayList<String>()
        if (graphInterval == GraphInterval.MONTH) {
            monthlyList = getMonthList(hrDataList)
        } else if (graphInterval == GraphInterval.WEEK) {
            monthlyList = getWeekly(hrDataList)
        }
        return monthlyList
    }

    private fun getMonthList(stepsDataList: ArrayList<HrBreakup>): ArrayList<String> {
        val monthlyList = ArrayList<String>()
        stepsDataList.forEach { stepData ->
            if (stepData.date != null) {
                monthlyList.add(DateFormats.formatMonthly(stepData.date))
            }
        }
        return monthlyList
    }

    private fun getWeekly(stepsDataList: ArrayList<HrBreakup>): ArrayList<String> {
        val monthlyList = ArrayList<String>()
        stepsDataList.forEach { stepData ->
            if (stepData.date != null) {
                monthlyList.add(DateFormats.formatWeek(stepData.date))
            }
        }
        return monthlyList
    }

    fun parseHrDayData(hrDataList: ArrayList<HrBreakup>): Pair<ArrayList<Entry>, ArrayList<MarkerEntry>> {
        val data = ArrayList<Entry>()
        val markEntryList = ArrayList<MarkerEntry>()
        if (hrDataList.isEmpty()) {
            for (time in 0..23) {
                val markerEntry = MarkerEntry()
                markerEntry.date = getHours(time)
                markerEntry.type = "BPM"
                val steps1 = "0 _ 0"
                markerEntry.value = steps1
                markEntryList.add(markerEntry)
                data.add(Entry(time.toFloat(), 0.0f))
            }
            return Pair(data, markEntryList)
        }

        for (index in 0..23) {
            val hrData = findHrData(index, hrDataList)
            val markerEntry = MarkerEntry()
            markerEntry.date = getHours(index)
            markerEntry.type = "BPM"
            val steps1 = "${hrData.min} - ${hrData.max}"
            markerEntry.value = steps1
            markEntryList.add(markerEntry)
            data.add(Entry(index.toFloat(), hrData.avg?.toFloat() ?: 0.0f))
        }

        return Pair(data, markEntryList)
    }

    private fun findHrData(
        hour_of_the_day: Int,
        hrDataList: ArrayList<HrBreakup>
    ): HrBreakup {
        val hrData = hrDataList.find { it.hour_of_the_day == hour_of_the_day }
        if (hrData != null) {
            return hrData
        }
        return HrBreakup(hour_of_the_day, null, 0, 0, ArrayList(), 0, hour_of_the_day)
    }


    fun parseGraph(
        hrDataList: ArrayList<HrBreakup>,
        graphInterval: GraphInterval
    ): Pair<ArrayList<BarEntry>, ArrayList<MarkerEntry>>? {


        when (graphInterval) {
            GraphInterval.YEAR -> {
                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<BarEntry>()

                hrDataList.forEachIndexed { index, data ->
                    val markerEntry = MarkerEntry()
                    markerEntry.date = getMonth(index)
                    markerEntry.type = "BPM"
                    val steps1 = "${data.min} - ${data.max}"
                    markerEntry.value = steps1
                    list.add(BarEntry(index.toFloat(), data.avg?.toFloat() ?: 0.0f))
                    markEntryList.add(markerEntry)
                }
                return Pair(list, markEntryList)

            }
            GraphInterval.MONTH -> {
                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<BarEntry>()

                hrDataList.forEachIndexed { index, data ->
                    val markerEntry = MarkerEntry()
                    markerEntry.date = DateFormats.formatDateTime(
                        data.date,
                        DateFormats.dateFormat3,
                        DateFormats.dateTimeFormatWithWeekDay
                    )
                    markerEntry.type = "BPM"
                    val steps1 = "${data.min} - ${data.max}"
                    markerEntry.value = steps1
                    list.add(BarEntry(index.toFloat(), data.avg?.toFloat() ?: 0.0f))
                    markEntryList.add(markerEntry)
                }
                return Pair(list, markEntryList)
            }
            GraphInterval.WEEK -> {
                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<BarEntry>()

                hrDataList.forEachIndexed { index, data ->
                    val markerEntry = MarkerEntry()
                    markerEntry.date = DateFormats.formatDateTime(
                        data.date,
                        DateFormats.dateFormat3,
                        DateFormats.dateTimeFormatWithWeekDay
                    )
                    markerEntry.type = "BPM"
                    val steps1 = "${data.min} - ${data.max}"
                    markerEntry.value = steps1
                    list.add(BarEntry(index.toFloat(), data.avg?.toFloat() ?: 0.0f))
                    markEntryList.add(markerEntry)
                }
                return Pair(list, markEntryList)
            }
            else -> {

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