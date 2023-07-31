package com.noisefit.ui.dashboard.graphs.steps

import com.github.mikephil.charting.data.BarEntry
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit_commans.data.enums.HealthOverViewHistoryType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.MarkerEntry
import com.noisefit_commans.data.model.history.StepsHistoryData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class StepsGraphViewModel
@Inject
constructor(
    localDataStore: DataStoredInterface,
    private val dataUnitConverter: DataUnitConverter
) : BaseViewModel() {


    var unit = Units.METRIC
    var distanceUnit = ""
    var healthOverViewHistoryType: HealthOverViewHistoryType = HealthOverViewHistoryType.Steps
    var average = 0.0
    var steps = 0L
    var stepsGoal = 0
    var caloriesGoal = 0
    var distanceGoal = 0
    var historyType: String = ""
    var date: String = ""
    var markEntryList = ArrayList<MarkerEntry>()
    var stepsDataList: ArrayList<StepsHistoryData> = ArrayList()

    init {
        val user = localDataStore.getUser()
        unit = user?.userGoals?.getUnit() ?: Units.METRIC
        stepsGoal = user?.userGoals?.stepGoal ?: 0
        distanceGoal = user?.userGoals?.distanceGoal ?: 0
        caloriesGoal = user?.userGoals?.caloriesGoal ?: 0
        distanceUnit = dataUnitConverter.distanceUnit(unit)
    }

    fun getXAxisMarker(graphInterval: GraphInterval): ArrayList<String> {
        var monthlyList = ArrayList<String>()
        if (graphInterval == GraphInterval.MONTH) {
            monthlyList = getMonthList(stepsDataList)
        } else if (graphInterval == GraphInterval.WEEK) {
            monthlyList = getWeekly(stepsDataList)
        }
        return monthlyList
    }

    private fun getMonthList(stepsDataList: ArrayList<StepsHistoryData>): ArrayList<String> {
        val monthlyList = ArrayList<String>()
        stepsDataList.forEach { stepData ->
            if (stepData.date != null) {
                monthlyList.add(DateFormats.formatMonthly(stepData.date))
            }
        }
        return monthlyList
    }

    private fun getWeekly(stepsDataList: ArrayList<StepsHistoryData>): ArrayList<String> {
        val monthlyList = ArrayList<String>()
        stepsDataList.forEach { stepData ->
            if (stepData.date != null) {
                monthlyList.add(DateFormats.formatWeek(stepData.date))
            }
        }
        return monthlyList
    }

    fun getDataUnitConverter(): DataUnitConverter {
        return dataUnitConverter
    }

    private fun getEmptyHourList(): Triple<ArrayList<BarEntry>, ArrayList<Int>, ArrayList<MarkerEntry>> {
        var type = ""
        var colorToFill = 0
        when (healthOverViewHistoryType) {
            HealthOverViewHistoryType.Calories -> {
                type = "kcal"
                colorToFill =
                    NoiseFitApplicationMain.context!!.resources.getColor(R.color.calories_color)
            }
            HealthOverViewHistoryType.Distance -> {
                type = distanceUnit
                colorToFill =
                    NoiseFitApplicationMain.context!!.resources.getColor(R.color.distance_color)
            }
            else -> {
                type = "Steps"
                colorToFill =
                    NoiseFitApplicationMain.context!!.resources.getColor(R.color.steps_color)
            }
        }
        val colors = ArrayList<Int>()
        val markEntryList = ArrayList<MarkerEntry>()
        val list = ArrayList<BarEntry>()
        for (i in 0..23) {
            val markerEntry = MarkerEntry()
            markerEntry.date = getHours(i)
            markerEntry.type = type
            markerEntry.value = "0"
            colors.add(colorToFill)
            list.add(BarEntry(i.toFloat(), 0.0f))
            markEntryList.add(markerEntry)
        }

        return Triple(list, colors, markEntryList)
    }


    fun parseGraph(
        stepsDataList: ArrayList<StepsHistoryData>,
        graphInterval: GraphInterval,
        healthOverViewHistoryType: HealthOverViewHistoryType
    ): Triple<ArrayList<BarEntry>, ArrayList<Int>, ArrayList<MarkerEntry>>? {
        return parseGraphData(stepsDataList, graphInterval, healthOverViewHistoryType)
    }

    fun parseGraph(
        stepsDataList: ArrayList<StepsHistoryData>,
        graphInterval: GraphInterval
    ): Triple<ArrayList<BarEntry>, ArrayList<Int>, ArrayList<MarkerEntry>>? {
        return parseGraphData(stepsDataList, graphInterval, healthOverViewHistoryType)
    }

    private fun parseGraphData(
        stepsDataList: ArrayList<StepsHistoryData>,
        graphInterval: GraphInterval,
        healthOverViewHistoryType: HealthOverViewHistoryType
    ): Triple<ArrayList<BarEntry>, ArrayList<Int>, ArrayList<MarkerEntry>>? {
        var type = ""
        var colorToFill = 0
        when (healthOverViewHistoryType) {
            HealthOverViewHistoryType.Calories -> {
                type = "kcal"
                colorToFill =
                    NoiseFitApplicationMain.context!!.resources.getColor(R.color.calories_color)
            }
            HealthOverViewHistoryType.Distance -> {
                type = distanceUnit
                colorToFill =
                    NoiseFitApplicationMain.context!!.resources.getColor(R.color.distance_color)
            }
            else -> {
                type = "Steps"
                colorToFill =
                    NoiseFitApplicationMain.context!!.resources.getColor(R.color.steps_color)
            }
        }


        when (graphInterval) {
            GraphInterval.YEAR -> {
                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<BarEntry>()

                stepsDataList.forEachIndexed { index, stepData ->
                    val markerEntry = MarkerEntry()
                    markerEntry.date = getMonth(index)
                    markerEntry.type = type

                    val steps1 = when (healthOverViewHistoryType) {
                        HealthOverViewHistoryType.Calories -> {
                            stepData.calories?.toString() ?: "0"
                        }
                        HealthOverViewHistoryType.Distance -> {
                            dataUnitConverter.formatDistance(stepData.distance?.toInt() ?: 0, unit)
                        }
                        else -> {
                            stepData.steps?.toString() ?: "0"
                        }
                    }


                    markerEntry.value = steps1
                    list.add(BarEntry(index.toFloat(), steps1.toFloat()))
                    markEntryList.add(markerEntry)
                }
                val colors = ArrayList<Int>()
                colors.add(colorToFill)
                return Triple(list, colors, markEntryList)
            }
            GraphInterval.MONTH -> {
                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<BarEntry>()

                stepsDataList.forEachIndexed { index, stepData ->
                    val markerEntry = MarkerEntry()
                    markerEntry.date = DateFormats.formatDateTime(
                        stepData.date,
                        DateFormats.dateFormat3,
                        DateFormats.dateTimeFormatWithWeekDay
                    )

                    markerEntry.type = type


                    val steps1 = when (healthOverViewHistoryType) {
                        HealthOverViewHistoryType.Calories -> {
                            stepData.calories?.toString() ?: "0"
                        }
                        HealthOverViewHistoryType.Distance -> {
                            dataUnitConverter.formatDistance(stepData.distance?.toInt() ?: 0, unit)
                        }
                        else -> {
                            stepData.steps?.toString() ?: "0"
                        }
                    }
                    markerEntry.value = steps1
                    list.add(BarEntry(index.toFloat(), steps1.toFloat()))
                    markEntryList.add(markerEntry)
                }
                val colors = ArrayList<Int>()
                colors.add(colorToFill)
                return Triple(list, colors, markEntryList)
            }
            GraphInterval.WEEK -> {
                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<BarEntry>()

                stepsDataList.forEachIndexed { index, stepData ->
                    val markerEntry = MarkerEntry()
                    markerEntry.date = DateFormats.formatDateTime(
                        stepData.date,
                        DateFormats.dateFormat3,
                        DateFormats.dateTimeFormatWithWeekDay
                    )
                    markerEntry.type = type
                    val steps1 = when (healthOverViewHistoryType) {
                        HealthOverViewHistoryType.Calories -> {
                            stepData.calories?.toString() ?: "0"
                        }
                        HealthOverViewHistoryType.Distance -> {
                            dataUnitConverter.formatDistance(stepData.distance?.toInt() ?: 0, unit)
                        }
                        else -> {
                            stepData.steps?.toString() ?: "0"
                        }
                    }
                    markerEntry.value = steps1
                    list.add(BarEntry(index.toFloat(), steps1.toFloat()))
                    markEntryList.add(markerEntry)
                }
                val colors = ArrayList<Int>()
                colors.add(colorToFill)
                return Triple(list, colors, markEntryList)
            }
            GraphInterval.DAY -> {
                if (stepsDataList.isNullOrEmpty()) {
                    return getEmptyHourList()
                }
                val markEntryList = ArrayList<MarkerEntry>()
                val list = ArrayList<BarEntry>()

                var stepsCount = 0L
                val colors = ArrayList<Int>()

                for (index in 0..23) {
                    val stepData = findStepsData(index, stepsDataList)
                    val markerEntry = MarkerEntry()
                    markerEntry.date = getHours(index)
                    markerEntry.type = type
                    var goal = 0L
                    var steps1: String = ""


                    when (healthOverViewHistoryType) {
                        HealthOverViewHistoryType.Calories -> {
                            goal = stepData.calories ?: 0
                            stepsCount += goal
                            steps1 = stepData.calories?.toString() ?: "0"
                            if (stepsCount >= caloriesGoal) {
                                colors.add(NoiseFitApplicationMain.context!!.resources.getColor(R.color.calories_complete_goal))
                            } else {
                                colors.add(colorToFill)
                            }
                        }
                        HealthOverViewHistoryType.Distance -> {
                            goal = stepData.distance ?: 0
                            stepsCount += goal
                            steps1 = dataUnitConverter.formatDistance(
                                stepData.distance?.toInt() ?: 0,
                                unit
                            )
                            if (stepsCount >= distanceGoal) {
                                colors.add(NoiseFitApplicationMain.context!!.resources.getColor(R.color.distance_complete_color))
                            } else {
                                colors.add(colorToFill)
                            }
                        }
                        else -> {
                            goal = stepData.steps ?: 0
                            stepsCount += goal
                            steps1 = stepData.steps?.toString() ?: "0"
                            if (stepsCount >= stepsGoal) {
                                colors.add(NoiseFitApplicationMain.context!!.resources.getColor(R.color.steps_complete_color))
                            } else {
                                colors.add(colorToFill)
                            }
                        }
                    }


                    markerEntry.value = steps1
                    list.add(BarEntry(index.toFloat(), steps1.toFloat()))
                    markEntryList.add(markerEntry)
                }



                return Triple(list, colors, markEntryList)
            }
        }
        return null
    }

    private fun findStepsData(
        hour_of_the_day: Int,
        stepsDataList: ArrayList<StepsHistoryData>
    ): StepsHistoryData {
        val stepsData = stepsDataList.find { it.hour_of_the_day == hour_of_the_day }
        if (stepsData != null) {
            return stepsData
        }
        return StepsHistoryData(0, null, 0, 0, 0, 0, hour_of_the_day, ArrayList())
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