package com.noisefit.ui.dashboard.graphs.sleep

import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.noisefit_commans.data.model.CountCardData
import com.noisefit_commans.data.model.MarkerEntry
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.enums.SleepExtraType
import com.noisefit_commans.models.SleepData
import com.noisefit_commans.response.SleepBreakup
import com.noisefit_commans.response.SleepHeartRate
import com.noisefit_commans.response.SleepHourBreakup
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SleepGraphViewModel @Inject constructor() : BaseViewModel() {

    var overlayType = SleepExtraType.NONE
    var sleepHourList: ArrayList<SleepHourBreakup> = ArrayList()
    var sleepHeartRateList: ArrayList<SleepHeartRate>? = ArrayList()
    var sleepStressList: ArrayList<SleepHeartRate>? = ArrayList()
    var markEntryList = ArrayList<MarkerEntry>()
    var averageBedTime = ""
    var duration = 0
    var historyType: String = ""
    var date: String = ""
    var startTime: String = ""
    var endTime: String = ""
    var sleepDataList: ArrayList<SleepBreakup> = ArrayList()


    fun getHourlySleepData(): Pair<ArrayList<SleepData.SleepDataBreakup>, CountCardData> {

        val countCData = CountCardData(
            type = "Sleep",
            imageSourceId = 0,
            cardSourceId = 0,
            imageBgSourceId = 0
        )
        countCData.count = "_"
        countCData.countSubText = "sub"
        countCData.leftValue = startTime
        countCData.rightValue = endTime

        val sleepArray = ArrayList<SleepData.SleepDataBreakup>()
        sleepHourList.forEach { sleepHourBreakup ->
            sleepArray.add(
                SleepData.SleepDataBreakup(
                    sleepType = ApplicationUtils.getSleepType(sleepHourBreakup.type).type,
                    duration = sleepHourBreakup.duration,
                    startTime = sleepHourBreakup.startTime,
                    endTime = sleepHourBreakup.endTime
                )
            )
        }
        return Pair(sleepArray, countCData)
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

    fun parseGraph(
        sleepDataList: ArrayList<SleepBreakup>,
        graphInterval: GraphInterval
    ): Pair<ArrayList<CandleEntry>, ArrayList<MarkerEntry>>? {


        when (graphInterval) {
            GraphInterval.YEAR -> {
                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<CandleEntry>()

                sleepDataList.forEachIndexed { index, data ->
                    val markerEntry = MarkerEntry()
                    markerEntry.date = getMonth(index, date)
                    markerEntry.type = ""
                    val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(data.avg_duration)
                    markerEntry.value = "$hour hrs $minute mins"

                    list.add(
                        CandleEntry(
                            index.toFloat(),
                            data.avg_duration.toFloat() / 60,
                            0f,
                            0f,
                            data.avg_duration.toFloat() / 60
                        )
                    )
                    markEntryList.add(markerEntry)
                }
                return Pair(list, markEntryList)

            }
            GraphInterval.MONTH -> {
                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<CandleEntry>()

                sleepDataList.forEachIndexed { index, data ->
                    val markerEntry = MarkerEntry()
                    markerEntry.date = DateFormats.formatDateTime(
                        data.date,
                        DateFormats.dateFormat3,
                        DateFormats.dateTimeFormatWithWeekDay
                    )
                    val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(data.duration)
                    markerEntry.value = "$hour hrs $minute mins"
                    list.add(
                        CandleEntry(
                            index.toFloat(),
                            data.duration.toFloat() / 60,
                            0f,
                            0f,
                            data.duration.toFloat() / 60
                        )
                    )
                    markEntryList.add(markerEntry)
                }
                return Pair(list, markEntryList)
            }
            GraphInterval.WEEK -> {
                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<CandleEntry>()

                sleepDataList.forEachIndexed { index, data ->
                    val markerEntry = MarkerEntry()
                    markerEntry.date = DateFormats.formatDateTime(
                        data.date,
                        DateFormats.dateFormat3,
                        DateFormats.dateTimeFormatWithWeekDay
                    )
                    val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(data.duration)
                    markerEntry.value = "$hour hrs $minute mins"
                    list.add(
                        CandleEntry(
                            index.toFloat(),
                            data.duration.toFloat() / 60,
                            0f,
                            0f,
                            data.duration.toFloat() / 60
                        )
                    )
                    markEntryList.add(markerEntry)
                }
                return Pair(list, markEntryList)
            }
            else -> {

            }

        }
        return null
    }

    private fun getMonth(month: Int, year: String): String {
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


    fun getXAxisMarker(graphInterval: GraphInterval): ArrayList<String> {
        var monthlyList = ArrayList<String>()
        if (graphInterval == GraphInterval.MONTH) {
            monthlyList = getMonthList(sleepDataList)
        } else if (graphInterval == GraphInterval.WEEK) {
            monthlyList = getWeekly(sleepDataList)
        }
        return monthlyList
    }

    private fun getMonthList(stepsDataList: ArrayList<SleepBreakup>): ArrayList<String> {
        val monthlyList = ArrayList<String>()
        stepsDataList.forEach { stepData ->
            if (stepData.date != null) {
                monthlyList.add(DateFormats.formatMonthly(stepData.date))
            }
        }
        return monthlyList
    }

    private fun getWeekly(stepsDataList: ArrayList<SleepBreakup>): ArrayList<String> {
        val monthlyList = ArrayList<String>()
        stepsDataList.forEach { stepData ->
            if (stepData.date != null) {
                monthlyList.add(DateFormats.formatWeek(stepData.date))
            }
        }
        return monthlyList
    }

    fun getTodayHeartRateData(): ArrayList<Entry> {
        val data = ArrayList<Entry>()
        sleepHeartRateList?.forEachIndexed { index, sleepHeartRate ->
            data.add(Entry(index.toFloat(), sleepHeartRate.avg_value?.toFloat() ?: 0f))
        }
        return data
    }

    fun getTodayStressData(): ArrayList<CandleEntry> {
        val data = ArrayList<CandleEntry>()
        sleepStressList?.forEachIndexed { index, sleepHeartRate ->

            data.add(
                CandleEntry(
                    index.toFloat(),
                    sleepHeartRate.value?.toFloat() ?: 0f,
                    0f,
                    0f,
                    sleepHeartRate.value?.toFloat() ?: 0f
                )
            )
        }
        return data
    }

    fun getOverlayHeartData(): ArrayList<CandleEntry> {
        val data = ArrayList<CandleEntry>()
        sleepDataList.forEachIndexed { index, sleepBreakup ->

            data.add(
                CandleEntry(
                    index.toFloat(),
                    sleepBreakup.hr_max?.toFloat() ?: 0f,
                    sleepBreakup.hr_min?.toFloat() ?: 0f,
                    sleepBreakup.hr_min?.toFloat() ?: 0f,
                    sleepBreakup.hr_max?.toFloat() ?: 0f
                )
            )
        }
        return data
    }

    fun getOverlayStressData(): ArrayList<CandleEntry> {
        val data = ArrayList<CandleEntry>()
        sleepDataList.forEachIndexed { index, sleepBreakup ->
            data.add(
                CandleEntry(
                    index.toFloat(),
                    sleepBreakup.stress_max?.toFloat() ?: 0f,
                    sleepBreakup.stress_min?.toFloat() ?: 0f,
                    sleepBreakup.stress_min?.toFloat() ?: 0f,
                    sleepBreakup.stress_max?.toFloat() ?: 0f
                )
            )
        }
        return data
    }
}