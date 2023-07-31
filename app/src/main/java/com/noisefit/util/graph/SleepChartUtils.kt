package com.noisefit.util.graph

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.R
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval

object SleepChartUtils {
    fun getSleepScoreMessage(sleepScore: Int): Pair<String, String> {
        val context = NoiseFitApplicationMain.context!!
        return when {
            sleepScore < 60 -> {
                Pair(
                    "Poor",
                    context.getString(R.string.text_sleep_message_poor)
                )
            }
            sleepScore in 60..79 -> {
                Pair(
                    "Fair",
                    context.getString(R.string.text_sleep_message_fair)
                )
            }
            sleepScore in 80..89 -> {
                Pair(
                    "Good",
                    context.getString(R.string.text_sleep_message_good)
                )
            }
            sleepScore in 90..100 -> {
                Pair(
                    "Excellent",
                    context.getString(R.string.text_sleep_message_excellent)
                )
            }
            else -> {
                Pair("", "")
            }
        }
    }

    fun setSleepChartData(
        values: List<CandleEntry>,
        chart: CandleStickChart,
        overlayMode: Boolean,
        graphInterval: GraphInterval
    ) {
        val dataSets = ArrayList<ICandleDataSet>()

        dataSets.add(
            geCandleChartDataSet(
                values,
                chart,
                overlayMode
            )
        )
        val data = CandleData(dataSets)
        chart.data = data
        /* data.barWidth = when (graphInterval) {
             GraphInterval.WEEK -> {
                 0.1f
             }
             GraphInterval.DAY -> 0.1f
             GraphInterval.MONTH -> 0.5f
             GraphInterval.YEAR -> 0.1f
         }*/

        chart.invalidate()
        chart.notifyDataSetChanged()
    }

    fun setInvalidate(chart: CandleStickChart) {
        chart.clear()
        chart.notifyDataSetChanged()
        chart.invalidate()
    }


    private fun geCandleChartDataSet(
        values: List<CandleEntry>,
        chart: CandleStickChart,
        overlayMode: Boolean
    ): CandleDataSet {

        val barDataSet: CandleDataSet
        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            barDataSet = chart.data.getDataSetByIndex(0) as CandleDataSet
            barDataSet.values = values
            barDataSet.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            barDataSet = CandleDataSet(values, "")

        }
        barDataSet.apply {

            setDrawIcons(false)
            setDrawValues(false)
            axisDependency = YAxis.AxisDependency.RIGHT
            if (overlayMode) {
                val startColor =
                    NoiseFitApplicationMain.context!!.resources.getColor(R.color.sleep_gray)
                val endColor =
                    NoiseFitApplicationMain.context!!.resources.getColor(R.color.sleep_gray)
                decreasingColor = endColor
                increasingColor = startColor
            } else {
                val startColor =
                    NoiseFitApplicationMain.context!!.resources.getColor(R.color.sleep_color)
                val endColor =
                    NoiseFitApplicationMain.context!!.resources.getColor(R.color.sleep_color)
                decreasingColor = endColor
                increasingColor = startColor
            }
            increasingPaintStyle = Paint.Style.FILL

            valueTextSize = 0f
        }

        barDataSet.setDrawHorizontalHighlightIndicator(false)
        barDataSet.setDrawVerticalHighlightIndicator(false)

        val spacingValue = when {
            values.size < 10 -> {
                0.45f
            }
            values.size in 11..19 -> {
                0.40f
            }
            else -> {
                0.30f
            }
        }
        barDataSet.barSpace = spacingValue



        return barDataSet
    }

    fun setSleepChart(
        chart: CandleStickChart,
        graphInterval: GraphInterval,
        monthList: ArrayList<String>,
        overlayMode: Boolean
    ) {
        chart.apply {
            description.isEnabled = false
            // extraTopOffset = 80f - minOffset
            setTouchEnabled(true)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            if (!overlayMode) {
                animateX(500)
            }
            //setRadius(Utils.convertDpToPixel(15f).toInt())
        }

        val legend = chart.legend
        legend.isEnabled = false


        val leftAxis = chart.axisLeft
        leftAxis.isEnabled = false


        if (!overlayMode) {
            val rightAxis = chart.axisRight
            rightAxis.textColor =
                NoiseFitApplicationMain.context!!.resources.getColor(R.color.lightest_gray)
            rightAxis.setDrawGridLines(true)
            rightAxis.setLabelCount(5, true)
            rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            rightAxis.setDrawAxisLine(false)
            rightAxis.axisMinimum = 0f
            rightAxis.isEnabled = true
        } else {
            chart.axisRight.isEnabled = false
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
                        list.add("0$num.00")
                    } else {
                        list.add("$num.00")
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

    fun setBedTimeVarianceGraph(
        chart: LineChart, context: Context, data: LineData, labelList: ArrayList<String>
    ) {

        (data.getDataSetByIndex(0) as LineDataSet).circleHoleColor = Color.parseColor("#6AC5FF")
        (data.getDataSetByIndex(0) as LineDataSet).setCircleColor(R.color.app_background)
        (data.getDataSetByIndex(0) as LineDataSet).setColor(Color.parseColor("#6AC5FF"), 255)
        (data.getDataSetByIndex(0) as LineDataSet).fillAlpha = 255

        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
//            animateX(100)
        }

        chart.data = data

        val l = chart.legend
        l.isEnabled = false

        chart.axisLeft.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.isDoubleTapToZoomEnabled = false
        chart.xAxis.isEnabled = true
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        chart.axisLeft.setDrawGridLines(false)
        chart.xAxis.setDrawGridLines(false)

        chart.axisRight.spaceTop = 0f
        chart.xAxis.textColor = context.resources.getColor(android.R.color.darker_gray)


        //val xLabel = arrayListOf<String>("S", "M", "T", "W", "T", "F", "S")

        chart.xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return try {
                    labelList[value.toInt()]
                } catch (exp: Exception) {
                    ""
                }
            }
        }
        chart.animateX(1000)
    }


}