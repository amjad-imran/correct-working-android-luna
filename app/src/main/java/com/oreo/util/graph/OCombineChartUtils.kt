package com.oreo.util.graph

import android.graphics.Color
import android.graphics.Paint
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.noisefit.luna.R
import com.noisefit_commans.ui.getColor

object OCombineChartUtils {

    fun setChart(
        chart: CombinedChart,
        xLabelList: ArrayList<String>,
        axisMinimum: Float,
        average: Float
    ) {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
        }


        chart.drawOrder = arrayOf(
            CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE
        )


        val legend = chart.legend
        legend.isEnabled = false


        val leftAxis = chart.axisLeft
        leftAxis.isEnabled = false


        val rightAxis = chart.axisRight
        rightAxis.textColor = R.color.white_64.getColor()
        rightAxis.setStartAtZero(false)
        rightAxis.setDrawGridLines(true)
        rightAxis.setLabelCount(3, true)
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        rightAxis.setDrawAxisLine(false)
        rightAxis.axisMinimum = axisMinimum
//        rightAxis.axisMaximum = 95f
        rightAxis.gridColor = com.noisefit_commans.R.color.white_12.getColor()

        rightAxis.gridColor = R.color.graph_line.getColor()
        rightAxis.isEnabled = true
        //    rightAxis.isShowMiddleGrid = true

        if (average > 0) {
            val nameLimitLine = LimitLine(average, average.toString()).apply {
                enableDashedLine(10f, 15f, 0f) //For "- - - -"
                lineWidth = 2f
                textSize = 0f
                textColor =R.color.transparent.getColor()
                //NoiseFitApplicationMain.context!!.resources.getColor(R.color.steps_color)
                lineColor = R.color.graph_line.getColor()
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM

            }
            chart.axisRight.addLimitLine(nameLimitLine)
        }
        val xAxis = chart.xAxis
        xAxis.isEnabled = true
        xAxis.spaceMax = .4f
        xAxis.spaceMin = .4f
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.gridColor = com.noisefit_commans.R.color.white_12.getColor()
        xAxis.axisLineColor = com.noisefit_commans.R.color.white_12.getColor()
        xAxis.textColor = com.noisefit_commans.R.color.white_64.getColor()

        xAxis.setDrawGridLines(false)
        xAxis.setLabelCount(5, true)

        xAxis.valueFormatter =
            IAxisValueFormatter { value, _ ->
                try {
                    xLabelList[value.toInt()]
                } catch (exp: Exception) {
                    ""
                }
            }
    }

    fun generateLineData(
        entries: ArrayList<Entry>,
        chart: CombinedChart,
        colors1: ArrayList<Int>,
        axisMinimum: Float

    ): LineData {
        val lineData = LineData()
        val lineDataSet = LineDataSet(entries, "")
        lineDataSet.apply {
            setDrawIcons(false)
            colors = colors1
            isHideZeroPoints = true
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            setDrawCircles(false)
            val leftAxis = chart.axisLeft
            leftAxis.axisMinimum = axisMinimum
//            leftAxis.axisMaximum = 95f
            lineWidth = 3f
            formLineWidth = 2f
            formSize = 15f
            valueTextSize = 0f

        }
        lineDataSet.setDrawHorizontalHighlightIndicator(false)
        lineDataSet.setDrawVerticalHighlightIndicator(false)
        lineData.addDataSet(lineDataSet)
        return lineData
    }

    fun generateCandleData(
        entries: ArrayList<CandleEntry>,
        colors1: Int
    ): CandleData {
        val d = CandleData()

        val set = CandleDataSet(entries, "")
        set.shadowColor = Color.DKGRAY
//        set.barSpace = .1f
//        when (entries.size) {
//            in 0..10 -> {
//                set.barSpace = .4f
//            }
//            in 10..50 -> {
//                set.barSpace = .2f
//            }
//            else ->{
//                set.barSpace = .4f
//            }
//        }


        set.valueTextSize = 0f
        set.setDrawValues(false)


        set.apply {
            val startColor = colors1.getColor()
            val endColor = colors1.getColor()
            decreasingColor = endColor
            increasingColor = startColor
            increasingPaintStyle = Paint.Style.FILL

        }

        set.setDrawHorizontalHighlightIndicator(false)
        set.setDrawVerticalHighlightIndicator(false)


        d.addDataSet(set)


        return d
    }
}