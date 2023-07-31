package com.noisefit.ui.dashboard.graphs.sleep

import android.content.Context
import android.graphics.Paint
import android.view.View
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.utils.Utils
import com.noisefit.luna.R

object SleepGraphUtil {

    fun setChartData(
        values: ArrayList<Entry>,
        chart: LineChart,
        context: Context
    ) {
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(
            getLineChartDataSet(
                context,
                values,
                chart,
                R.color.heart_rate,
                R.color.heart_rate
            )
        )
        val data = LineData(dataSets)
        chart.data = data
        chart.invalidate()
        chart.notifyDataSetChanged()
    }

    private fun getLineChartDataSet(
        context: Context,
        values: List<Entry>,
        lineChart: LineChart,
        lineColor: Int,
        bgColor: Int
    ): LineDataSet {

        val lineDataSet: LineDataSet
        if (lineChart.data != null &&
            lineChart.data.dataSetCount > 0
        ) {
            lineDataSet = lineChart.data.getDataSetByIndex(0) as LineDataSet
            lineDataSet.values = values
            lineDataSet.notifyDataSetChanged()
            lineChart.data.notifyDataChanged()
            lineChart.notifyDataSetChanged()
        } else {
            lineDataSet = LineDataSet(values, "")
            lineDataSet.apply {
                setDrawIcons(false)
                color = context.resources.getColor(lineColor)
                mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                cubicIntensity = 0.2f
                setDrawCircles(false)
                lineWidth = 1f
                circleRadius = 3f
                setDrawCircleHole(false)
                val leftAxis = lineChart.axisLeft
                leftAxis.axisMinimum = 0f
                formLineWidth = 1f
                formSize = 15f
                valueTextSize = 0f
                setDrawFilled(true)
            }
            if (Utils.getSDKInt() >= 18) {
                val drawable =
                    ContextCompat.getDrawable(context, R.drawable.back_hr_history)
                lineDataSet.fillDrawable = drawable
            } else {
                lineDataSet.fillColor = context.resources.getColor(bgColor)
            }
            lineDataSet.fillFormatter =
                IFillFormatter { _, _ -> lineChart.axisLeft.axisMinimum }
        }

        lineChart.apply {
            description.isEnabled = false

            legend.isEnabled = false
            this.isDragEnabled = false

            axisLeft.isEnabled = false

            axisRight.spaceTop = 0f

            axisRight.textColor = context.resources.getColor(android.R.color.darker_gray)
            axisRight.setDrawGridLines(false)

            xAxis.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = context.resources.getColor(android.R.color.darker_gray)
            xAxis.setDrawGridLines(false)

            minimumHeight = 1000
        }
        return lineDataSet
    }

    fun setDayStressChart(chart: CandleStickChart, context: Context) {
        chart.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            animateX(500)
        }

        val legend = chart.legend
        legend.isEnabled = false


        val leftAxis = chart.axisLeft
        leftAxis.isEnabled = false

        val rightAxis = chart.axisRight

        rightAxis.setDrawGridLines(false)

        rightAxis.spaceTop = 0f
        rightAxis.textColor = context.resources.getColor(android.R.color.darker_gray)

        val xAxis = chart.xAxis
        xAxis.isEnabled = true
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = context.resources.getColor(android.R.color.darker_gray)
        xAxis.setDrawGridLines(false)

    }

    fun setCandleStickChartData(
        values: List<CandleEntry>,
        chart: CandleStickChart,
        context: Context,
        color: Int
    ) {
        val dataSets = ArrayList<ICandleDataSet>()
        dataSets.add(
            getChartDataSet(
                context,
                values,
                chart,
                color
            )
        ) // add the data sets
        val data = CandleData(dataSets)
        chart.data = data
        chart.invalidate()
        chart.notifyDataSetChanged()

    }

    private fun getChartDataSet(
        context: Context,
        values: List<CandleEntry>,
        chart: CandleStickChart,
        color: Int
    ): CandleDataSet {
        val candleDataSet: CandleDataSet
        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            candleDataSet = chart.data.getDataSetByIndex(0) as CandleDataSet
            candleDataSet.values = values
            candleDataSet.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            candleDataSet = CandleDataSet(values, "")
            candleDataSet.apply {

                setDrawIcons(false)
                setDrawValues(false)
                axisDependency = YAxis.AxisDependency.LEFT
                val startColor = context.resources.getColor(color)
                val endColor = context.resources.getColor(color)
                decreasingColor = endColor
                increasingPaintStyle = Paint.Style.FILL
                increasingColor = startColor
                valueTextSize = 0f

            }


        }
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            animateX(500)
        }

        val legend = chart.legend
        legend.isEnabled = false


        val leftAxis = chart.axisLeft
        leftAxis.isEnabled = false

        val rightAxis = chart.axisRight

        rightAxis.setDrawGridLines(false)
        rightAxis.labelCount = 5
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        rightAxis.setDrawAxisLine(false)
        rightAxis.axisMinimum = 0f
        rightAxis.spaceTop = 0f
        rightAxis.textColor = context.resources.getColor(android.R.color.darker_gray)

        rightAxis.setValueFormatter { value, axis ->
            try {
                val intValue = value.toInt()
                String.format("%03d", intValue)
            } catch (exp: Exception) {
                value.toString()
            }
        }

        val xAxis = chart.xAxis
        xAxis.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = context.resources.getColor(android.R.color.darker_gray)
        xAxis.setDrawGridLines(false)

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
        candleDataSet.barSpace = spacingValue
        return candleDataSet
    }
}