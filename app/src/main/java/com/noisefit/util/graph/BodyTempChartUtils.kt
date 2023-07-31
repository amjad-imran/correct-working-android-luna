package com.noisefit.util.graph

import android.graphics.Color
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.Utils
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit_commans.data.model.MarkerEntry
import com.noisefit_commans.data.model.StressType
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.RoundedBarChart


object BodyTempChartUtils {

    private fun getLineChartDataSet(
        colors1: ArrayList<Int>,
        values: List<BarEntry>,
        chart: BarChart,
        markEntryList: ArrayList<MarkerEntry>
    ): BarDataSet {

        val barDataSet: BarDataSet
        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            barDataSet = chart.data.getDataSetByIndex(0) as BarDataSet
            barDataSet.values = values
            barDataSet.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            barDataSet = BarDataSet(values, "")
            barDataSet.apply {
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
        barDataSet.highLightColor = Color.TRANSPARENT
        barDataSet.highLightAlpha = 0
        return barDataSet
    }

    fun setChartData(
        values: ArrayList<BarEntry>,
        chart: BarChart,
        colors: ArrayList<Int>,
        markEntryList: ArrayList<MarkerEntry>,
        containerWidth: Float = 0f
    ) {
        val dataSets = ArrayList<IBarDataSet>()

        dataSets.add(
            getLineChartDataSet(
                colors,
                values,
                chart,
                markEntryList
            )
        ) // add the data sets
        val data = BarData(dataSets)
        chart.data = data
        data.barWidth = ApplicationUtils.getBarWidth(containerWidth, markEntryList.size)

        chart.invalidate()
        chart.notifyDataSetChanged()
    }

    fun setChart(
        chart: RoundedBarChart,
        graphInterval: GraphInterval,
        average: Int,
        monthList: ArrayList<String>
    ) {
        //  chart.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
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
        rightAxis.axisMinimum = 0f
        rightAxis.spaceTop = 0f

        val nameLimitLine = LimitLine(average.toFloat(), average.toString()).apply {
            enableDashedLine(10f, 15f, 0f) //For "- - - -"
            lineWidth = 2f
            textColor = NoiseFitApplicationMain.context!!.resources.getColor(R.color.steps_color)
            lineColor = NoiseFitApplicationMain.context!!.resources.getColor(R.color.steps_color)
        }
        chart.axisRight.addLimitLine(nameLimitLine)

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
                xLabel = arrayListOf("S", "M", "T", "W", "T", "F", "S")
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

//        val  yLabel  =
//        arrayListOf<String>("10","30","50")
//        rightAxis.valueFormatter =
//            IAxisValueFormatter { value, _ ->
//                try {
//                    yLabel.get(value.toInt()) ?: ""
//                } catch (exp: Exception) {
//                    ""
//                }
//            }
        xAxis.valueFormatter =
            IAxisValueFormatter { value, _ ->
                try {
                    xLabel.get(value.toInt())
                } catch (exp: Exception) {
                    ""
                }
            }

    }


    fun setChart(
        chart: RoundedBarChart
    ) {
        //  chart.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            //animateX(1500)
            setRadius(Utils.convertDpToPixel(15f).toInt())
        }

        val legend = chart.legend
        legend.isEnabled = false


        val leftAxis = chart.axisLeft
        leftAxis.isEnabled = false

        val rightAxis = chart.axisRight
        rightAxis.axisMinimum = 0f
        rightAxis.spaceTop = 0f
        rightAxis.isEnabled = false


        val xAxis = chart.xAxis
        xAxis.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM

    }

    private fun getLineChartDataSet(
        colors1: ArrayList<Int>,
        values: List<BarEntry>,
        chart: BarChart
    ): BarDataSet {

        val barDataSet: BarDataSet
        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            barDataSet = chart.data.getDataSetByIndex(0) as BarDataSet
            barDataSet.values = values
            barDataSet.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            barDataSet = BarDataSet(values, "")
            barDataSet.apply {
                setDrawIcons(false)
                colors = colors1
                val leftAxis = chart.axisLeft
                leftAxis.axisMinimum = 0f
                formLineWidth = 1f
                formSize = 15f
                valueTextSize = 0f


            }

        }
        return barDataSet
    }

    fun setChartData(
        values: ArrayList<BarEntry>,
        chart: BarChart,
        colors: ArrayList<Int>
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
        data.barWidth = 0.5f

        chart.invalidate()
        chart.notifyDataSetChanged()
    }

    fun stressType(value: Float): String {
        return when (value) {
            in 0f..29f -> {
                "Relax"
            }
            in 30f..59f -> {
                "Normal"
            }
            in 60f..79f -> {
                "Medium"
            }
            in 80f..100f -> {
                "High"
            }
            else -> ""
        }
    }



    fun getStressType(value: Float): StressType {
        return when (value) {
            in 0f..29f -> {
                StressType.Relax
            }
            in 30f..59f -> {
                StressType.Normal
            }
            in 60f..79f -> {
                StressType.Medium
            }
            in 80f..100f -> {
                StressType.High
            }
            else -> {
                StressType.High
            }
        }
    }


}