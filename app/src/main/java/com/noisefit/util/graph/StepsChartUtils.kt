package com.noisefit.util.graph

import android.graphics.Color
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.Utils
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.R
import com.noisefit_commans.data.model.MarkerEntry
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.RoundedBarChart


object StepsChartUtils {

    private fun getLineChartDataSet(
        colors1: ArrayList<Int>,
        values: List<BarEntry>,
        chart: BarChart
    ): BarDataSet {

        val lineDataSet: BarDataSet
        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            lineDataSet = chart.data.getDataSetByIndex(0) as BarDataSet
            lineDataSet.values = values
            lineDataSet.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            lineDataSet = BarDataSet(values, "")
            lineDataSet.apply {
                setDrawIcons(false)
                colors = colors1
                val leftAxis = chart.axisLeft
                leftAxis.axisMinimum = 0f
                formLineWidth = 1f
                formSize = 15f
                valueTextSize = 0f


            }

//            val mv = XYMarkerView(NoiseFitApplicationMain.context, markEntryList)
//            mv.chartView = chart // For bounds control
//            chart.marker = mv // Set the marker to the chart
        }
//        lineDataSet.isHighlightEnabled = false
        lineDataSet.highLightColor = Color.TRANSPARENT
        lineDataSet.highLightAlpha = 0
        return lineDataSet
    }

    fun setInvalidate(chart: BarChart) {
        chart.clear()
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    fun setChartData(
        values: ArrayList<BarEntry>,
        chart: BarChart,
        colors: ArrayList<Int>,
        markEntryList: ArrayList<MarkerEntry>,
        containerWidth: Float
    ) {
        val dataSets = ArrayList<IBarDataSet>()

        dataSets.add(
            getLineChartDataSet(
                colors,
                values,
                chart
            )
        ) // add the data sets
        val data = BarData(dataSets)
        chart.data = data
        data.barWidth = ApplicationUtils.getBarWidth(containerWidth, markEntryList.size)

        chart.invalidate()
        chart.notifyDataSetChanged()
    }



    fun setChart(
        color: Int,
        chart: RoundedBarChart,
        graphInterval: GraphInterval,
        average: Int,
        monthList: ArrayList<String>
    ) {
        //  chart.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
        chart.apply {
            description.isEnabled = false
            // extraTopOffset = 80f - minOffset
            setTouchEnabled(true)

            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
//            animateX(100)
            setRadius(Utils.convertDpToPixel(15f).toInt())
        }

        val legend = chart.legend
        legend.isEnabled = false


        val leftAxis = chart.axisLeft
        leftAxis.isEnabled = false

        val rightAxis = chart.axisRight
        rightAxis.textColor =
            NoiseFitApplicationMain.context!!.resources.getColor(R.color.lightest_gray)
        rightAxis.setDrawGridLines(true)
        rightAxis.setLabelCount(5,true)
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        rightAxis.setDrawAxisLine(false)
        rightAxis.axisMinimum = 0f
        //rightAxis.spaceTop = 0f

        if (average > 0) {
            val nameLimitLine = LimitLine(average.toFloat(), average.toString()).apply {
                enableDashedLine(10f, 15f, 0f) //For "- - - -"
                lineWidth = 2f
                textColor = color
                //NoiseFitApplicationMain.context!!.resources.getColor(R.color.steps_color)
                lineColor = color
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM

            }
            chart.axisRight.addLimitLine(nameLimitLine)
        }

        val xAxis = chart.xAxis
        xAxis.isEnabled = true
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor =
            NoiseFitApplicationMain.context!!.resources.getColor(R.color.lightest_gray)
        xAxis.setDrawGridLines(false)


        var xLabel = ArrayList<String>()
        when (graphInterval) {
            GraphInterval.DAY -> {
                val list = ArrayList<String>()
                for (i in 0..23) {
                    var num = i
                    if (num == 23) {
                        num = 0
                    }
                    if (num < 10) {
                        list.add("0$num:00")
                    } else {
                        list.add("$num:00")
                    }

                }
                xLabel = list
                xAxis.labelCount = 5
            }
            GraphInterval.WEEK -> {
                xLabel = monthList
            }
            GraphInterval.MONTH -> {
                xLabel = monthList
                xAxis.labelCount = 5
            }
            GraphInterval.YEAR -> {
                xLabel =
                    arrayListOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")

                xAxis.labelCount = xLabel.size
            }
        }

        xAxis.axisMaximum = xLabel.size.toFloat()
        chart.setVisibleXRange(1f, xLabel.size.toFloat())

        xAxis.valueFormatter =
            IAxisValueFormatter { value, _ ->
                try {
                    xLabel.get(value.toInt())
                } catch (exp: Exception) {
                    ""
                }
            }

    }


}

//open class YAxisFormatter(val type: String) : YAxisFormatter() {
//
//    override fun toString(): String {
//        return super.toString()
//    }
//
////    override fun getlue: Float, axis: AxisBase?): String {
////        var returnString = ""
////        when(type){
////            AppConstants.BLOOD_OXYGEN -> returnString = (value.toInt()).toString()
////            AppConstants.STEP_COUNT -> returnString = convertNumberToKilo(value)
////            AppConstants.SLEEP_HOURS -> returnString = convertNumberToKilo(value)
////            AppConstants.STRESS_COUNT -> returnString = (value.toInt()).toString()
////            AppConstants.HEART_RATE -> returnString = (value.toInt()).toString()
////            AppConstants.BODY_TEMPERATURE -> returnString = (value.toInt()).toString()
////            AppConstants.DISTANCE -> returnString = String.format("%.2f", value)
////        }
////        return returnString
////    }
//
//  private fun convertNumberToKilo(number: Float) : String{
//        if(number < 100){
//            return (number.roundToInt()).toString()
//        }
//        val kilo = number / 1000
//        return String.format("%.1f", kilo)+"K"
//    }
//
//
//}

//class MyValueFormatter : ValueFormatter() {
//    private val days = mapOf(0.0f to "Mon", 20.0f to "Tu", 40.0f to "Wed", 60.0f to "Th", 80.0f to "Fr", 100.0f to "Sa", 120.0f to "Su")
//
//    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
//        return if (days.containsKey(value)) days[value] else value.toString()
//    }
//}
