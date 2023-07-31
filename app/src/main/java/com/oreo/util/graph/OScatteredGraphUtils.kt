package com.oreo.util.graph

import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.noisefit.R
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.ui.getColor

object OScatteredGraphUtils {
    fun setChart(chart: ScatterChart) {
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        val l: Legend = chart.legend
        l.isEnabled = false
    }

    fun setAxisData(chart: ScatterChart, xLabelList: ArrayList<String>) {
        val rightAxis = chart.axisRight
        rightAxis.setDrawGridLines(true)
        rightAxis.setLabelCount(3, true)
        rightAxis.axisLineWidth = 2f
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        rightAxis.setDrawAxisLine(false)
        rightAxis.axisMinimum = 0f

        val yl: YAxis = chart.axisLeft
        yl.axisMinimum = 0f
        yl.setDrawLabels(false)
        yl.setDrawAxisLine(false)
        yl.setDrawGridLines(false)
        yl.setDrawZeroLine(true)
        chart.axisLeft.isEnabled = true

        val xAxis: XAxis = chart.xAxis
        xAxis.isEnabled = true
        xAxis.spaceMax = 0.5f
        xAxis.spaceMin = 0.5f
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.gridColor = com.noisefit_commans.R.color.white_12.getColor()
        xAxis.axisLineColor = com.noisefit_commans.R.color.transparent.getColor()
        xAxis.textColor = com.noisefit_commans.R.color.white_64.getColor()
        xAxis.setDrawGridLines(false)
        xAxis.setLabelCount(2, true)
        xAxis.valueFormatter =
            IAxisValueFormatter { value, _ ->
                try {
                    xLabelList[value.toInt()]
                } catch (exp: Exception) {
                    ""
                }
            }
    }

    fun setData(
        chart: ScatterChart,
        scatteredData: Triple<ArrayList<Entry>, ArrayList<Entry>, ArrayList<Entry>>
    ) {
        val set1 = ScatterDataSet(scatteredData.first, "")
        set1.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
        set1.color = ContextCompat.getColor(NoisefitApplication.context!!, R.color.low_color)
        val set2 = ScatterDataSet(scatteredData.second, "")
        set2.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
        set2.color = ContextCompat.getColor(NoisefitApplication.context!!, R.color.medium_color)
        val set3 = ScatterDataSet(scatteredData.third, "")
        set3.color = ContextCompat.getColor(NoisefitApplication.context!!, R.color.high_color)
        set3.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
        set1.scatterShapeSize = 12f
        set2.scatterShapeSize = 12f
        set3.scatterShapeSize = 12f
        val dataSets: ArrayList<IScatterDataSet> = ArrayList()
        dataSets.add(set1) // add the data sets
        dataSets.add(set2)
        dataSets.add(set3)
        val data = ScatterData(dataSets)
        data.setDrawValues(false)
        chart.data = data
        chart.invalidate()
    }
}