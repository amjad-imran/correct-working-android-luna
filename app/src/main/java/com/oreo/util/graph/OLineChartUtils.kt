package com.noisefit.oreo.util.graph

import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.Utils
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.R
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.ui.getColor


object OLineChartUtils {

    private fun getLineChartDataSet(
        color1: ArrayList<Int>,
        values1: List<Entry>,
        chart: LineChart
    ): LineDataSet {

        val lineDataSet: LineDataSet
        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            lineDataSet = chart.data.getDataSetByIndex(0) as LineDataSet
            lineDataSet.entries = values1
            lineDataSet.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            lineDataSet = LineDataSet(values1, "")
            lineDataSet.apply {
                setDrawIcons(false)
                color = ContextCompat.getColor(
                    NoisefitApplication.context!!,
                    com.noisefit_commans.R.color.steps_arc
                )
                setDrawCircles(false)
                val leftAxis = chart.axisLeft
                leftAxis.axisMinimum = 0f
                formLineWidth = 1f
                formSize = 15f
                valueTextSize = 0f
            }

        }
        lineDataSet.highLightColor = Color.TRANSPARENT
        return lineDataSet
    }

    fun setInvalidate(chart: BarChart) {
        chart.clear()
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    fun setChartData(
        values: ArrayList<Entry>,
        chart: LineChart,
        color1: ArrayList<Int>
    ) {
        val dataSets = ArrayList<ILineDataSet>()

        dataSets.add(
            getLineChartDataSet(
                color1,
                values,
                chart
            )
        ) // add the data sets
        val data = LineData(dataSets)
        chart.data = data
//        data.barWidth = ApplicationUtils.getBarWidth(containerWidth, markEntryList.size)

        chart.invalidate()
        chart.notifyDataSetChanged()
    }


    private fun baseChart(chart: LineChart) {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            setViewPortOffsets(8f, 8f, 0f, 0f)
        }

        val legend = chart.legend
        legend.isEnabled = false

    }

    private fun baseChartWithXLabel(chart: LineChart, xLabelList: ArrayList<String>) {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            setViewPortOffsets(8f, 8f, 0f, 0f)
        }

        val legend = chart.legend
        legend.isEnabled = false


    }

    private fun baseHeartChartWithXLabel(chart: LineChart, xLabelList: ArrayList<String>) {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
//            setViewPortOffsets(8f, 8f, 0f, 0f)//culprit, it restrict to user not plot value formatter
            animateX(500)
        }

        val legend = chart.legend
        legend.isEnabled = false

        val leftAxis = chart.axisLeft
        leftAxis.isEnabled = false

        val rightAxis = chart.axisRight
        rightAxis.textColor =
            NoiseFitApplicationMain.context!!.resources.getColor(R.color.lightest_gray)
        rightAxis.setDrawGridLines(false)
        rightAxis.setLabelCount(3, true)
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        rightAxis.setDrawAxisLine(false)
        rightAxis.axisMinimum = 0f
        //rightAxis.spaceTop = 0f

        val xAxis = chart.xAxis
        xAxis.isEnabled = true
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor =
            NoiseFitApplicationMain.context!!.resources.getColor(R.color.lightest_gray)
        xAxis.setDrawGridLines(false)

        val xLabel: ArrayList<String>
        val list = ArrayList<String>()
        for (i in 0 until 23) {
            list.add("$i:00")
        }
        xLabel = list
        xAxis.labelCount = 5
        xAxis.valueFormatter =
            IAxisValueFormatter { value, _ ->
                try {
                    xLabel[value.toInt()]
                } catch (exp: Exception) {
                    ""
                }
            }
    }

    fun setChart(
        chart: LineChart,
    ) {
        baseChart(chart)

        val leftAxis = chart.axisLeft
        leftAxis.isEnabled = false

        val rightAxis = chart.axisRight
        rightAxis.setDrawGridLines(true)
        rightAxis.textColor =
            ContextCompat.getColor(
                NoisefitApplication.context!!,
                com.noisefit_commans.R.color.white_64
            )

        rightAxis.setLabelCount(3, true)
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        rightAxis.setDrawAxisLine(false)
//        rightAxis.axisMaximum = 121f
        rightAxis.axisMinimum = 0f
        rightAxis.isEnabled = true
        val xAxis = chart.xAxis
        xAxis.isEnabled = false

    }


    private fun addLimitLine(average: Int, color: Int, chart: LineChart) {
        if (average > 0) {
            val nameLimitLine = LimitLine(average.toFloat(), average.toString()).apply {
                enableDashedLine(10f, 15f, 0f) //For "- - - -"
                lineWidth = 2f
                lineColor = ContextCompat.getColor(
                    NoisefitApplication.context!!,
                    color
                )
                label = ""


                // labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM

            }
            chart.axisRight.addLimitLine(nameLimitLine)
        }
    }

    fun setChartWithOnlyXAxis(
        chart: LineChart,
        average: Int,
        values: ArrayList<Entry>,
        color1: ArrayList<Int>,
        bgGradient: Int
    ) {
        baseChart(chart)
        val leftAxis = chart.axisLeft
        leftAxis.isEnabled = false
        val rightAxis = chart.axisRight
        rightAxis.axisMinimum = 0f
        rightAxis.spaceTop = 0.8f
        rightAxis.spaceBottom = 0.8f
        rightAxis.setDrawGridLines(false)
        rightAxis.setDrawAxisLine(false)
        rightAxis.gridColor = com.noisefit_commans.R.color.white_12.getColor()
        leftAxis.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.setDrawAxisLine(false)
        xAxis.gridColor = com.noisefit_commans.R.color.white_12.getColor()

        //xAxis.setLabelCount(8, true)
        xAxis.isEnabled = true
        xAxis.spaceMax = 0.5f
        xAxis.spaceMin = 0.5f
        addLimitLine(average, com.noisefit_commans.R.color.white_12, chart)
        val dataSets = ArrayList<ILineDataSet>()

        val lineDataSet: LineDataSet
        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            lineDataSet = chart.data.getDataSetByIndex(0) as LineDataSet
            lineDataSet.entries = values
            lineDataSet.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            lineDataSet = LineDataSet(values, "")
            lineDataSet.apply {
                setDrawIcons(false)

                mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                colors = color1
                circleRadius = 5f
                isShowLastCircle = false
                setDrawCircles(false)
                lineWidth = 3f
                valueTextSize = 0f
                cubicIntensity = 0.2f
                setDrawCircleHole(false)
                formLineWidth = 1f
                formSize = 15f
                valueTextSize = 0f
                setDrawFilled(true)
            }

        }
        lineDataSet.highLightColor = Color.TRANSPARENT
        dataSets.add(lineDataSet)
        if (Utils.getSDKInt() >= 18) {
            val drawable =
                ContextCompat.getDrawable(
                    NoisefitApplication.context!!,
                    bgGradient
                )
            lineDataSet.fillDrawable = drawable
        } else {
            lineDataSet.fillColor =
                com.noisefit_commans.R.color.lightest_gray.getColor()
        }
        lineDataSet.fillFormatter =
            IFillFormatter { _, _ -> chart.axisLeft.axisMinimum }
        val data = LineData(dataSets)
        chart.data = data

        chart.invalidate()
        chart.notifyDataSetChanged()
    }

    fun setHeartChartWithOnlyXAxis(
        chart: LineChart,
        average: Int,
        values: ArrayList<Entry>,
        color1: ArrayList<Int>,
        bgGradient: Int,
        xLabelList: ArrayList<String>

    ) {
        baseChart(chart)
        val leftAxis = chart.axisLeft
        leftAxis.isEnabled = true
        leftAxis.axisMinimum = 40f
        leftAxis.setDrawGridLines(true)
        leftAxis.setDrawAxisLine(false)
        leftAxis.isGranularityEnabled = true


        val rightAxis = chart.axisRight
        rightAxis.axisMinimum = 40f
        rightAxis.spaceTop = 0.8f
        rightAxis.spaceBottom = 0.8f
        rightAxis.setDrawGridLines(false)
        rightAxis.setDrawAxisLine(false)
        rightAxis.setDrawZeroLine(false)
        rightAxis.textColor = com.noisefit_commans.R.color.white.getColor()
        rightAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        rightAxis.gridColor = com.noisefit_commans.R.color.white_12.getColor()

        rightAxis.setDrawLimitLinesBehindData(true)

        val xAxis = chart.xAxis
        xAxis.setDrawAxisLine(false)
        xAxis.gridColor = com.noisefit_commans.R.color.white_12.getColor()
        xAxis.isEnabled = true
        xAxis.spaceMax = 0.5f
        xAxis.spaceMin = 0.5f
        xAxis.isEnabled = true
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor =
            NoiseFitApplicationMain.context!!.resources.getColor(R.color.lightest_gray)
        xAxis.setDrawGridLines(false)


        var xLabel = ArrayList<String>()
        val list = ArrayList<String>()
        for (i in 0 until 23) {
            list.add("$i:00")
        }
        xLabel = list
        xAxis.labelCount = 5

        xAxis.valueFormatter =
            IAxisValueFormatter { value, _ ->
                try {
                    xLabel[value.toInt()]
                } catch (exp: Exception) {
                    ""
                }
            }

        addLimitLine(average, com.noisefit_commans.R.color.white_12, chart)
        val dataSets = ArrayList<ILineDataSet>()

        val lineDataSet: LineDataSet
        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            lineDataSet = chart.data.getDataSetByIndex(0) as LineDataSet
            lineDataSet.entries = values
            lineDataSet.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            lineDataSet = LineDataSet(values, "")
            lineDataSet.apply {
                setDrawIcons(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                colors = color1
                circleRadius = 1f
                isShowLastCircle = false
                setDrawCircles(false)
                lineWidth = 1f
                valueTextSize = 0f
                cubicIntensity = 0.2f
                setDrawCircleHole(false)
                formLineWidth = 1f
                formSize = 15f
                valueTextSize = 0f
                setDrawFilled(true)
                lineDataSet.fillDrawable = getDrawable(chart.context, bgGradient)
            }

        }
        lineDataSet.highLightColor = Color.TRANSPARENT
        dataSets.add(lineDataSet)
//        if (Utils.getSDKInt() >= 18) {
//            val drawable =
//                ContextCompat.getDrawable(
//                    NoisefitApplication.context!!,
//                    bgGradient
//                )
//            lineDataSet.fillDrawable = drawable
//        } else {
//            lineDataSet.fillColor =
//                com.noisefit_commans.R.color.lightest_gray.getColor()
//        }
        lineDataSet.fillFormatter =
            IFillFormatter { _, _ -> chart.axisLeft.axisMinimum }
        val data = LineData(dataSets)
        chart.data = data

        chart.invalidate()
        chart.notifyDataSetChanged()
    }

    fun setChartWithOnlyXAxisAndXLabel(
        chart: LineChart,
        xLabelList: ArrayList<String>,
        average: Int,
        values: ArrayList<Entry>,
        color1: ArrayList<Int>,
        bgGradient: Int
    ) {
        baseChartWithXLabel(chart, xLabelList)
        val leftAxis = chart.axisLeft
        leftAxis.isEnabled = false

        val rightAxis = chart.axisRight
        rightAxis.axisMinimum = 0f
        rightAxis.spaceTop = 0.8f
        rightAxis.spaceBottom = 0.8f
        rightAxis.setDrawGridLines(false)
        rightAxis.setDrawAxisLine(false)
        rightAxis.setDrawZeroLine(false)
        rightAxis.gridColor = com.noisefit_commans.R.color.white_12.getColor()

        val xAxis = chart.xAxis
        //xAxis.setLabelCount(8, true)
        xAxis.isEnabled = true
        xAxis.spaceMax = 0.5f
        xAxis.spaceMin = 0.5f
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.gridColor = com.noisefit_commans.R.color.white_12.getColor()
        xAxis.axisLineColor = com.noisefit_commans.R.color.white_12.getColor()
        xAxis.textColor = com.noisefit_commans.R.color.white_64.getColor()

        xAxis.setDrawGridLines(true)
        xAxis.setLabelCount(5, true)


        xAxis.valueFormatter =
            IAxisValueFormatter { value, _ ->
                try {
                    xLabelList[value.toInt()]
                } catch (exp: Exception) {
                    ""
                }
            }

        addLimitLine(average, com.noisefit_commans.R.color.white_12, chart)
        val dataSets = ArrayList<ILineDataSet>()

        val lineDataSet: LineDataSet
        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            lineDataSet = chart.data.getDataSetByIndex(0) as LineDataSet
            lineDataSet.entries = values
            lineDataSet.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            lineDataSet = LineDataSet(values, "")
            lineDataSet.apply {
                setDrawIcons(false)

                mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                colors = color1
                circleRadius = 5f
                isShowLastCircle = false
                setDrawCircles(false)
                lineWidth = 2f
                valueTextSize = 0f
                setDrawCircleHole(false)
                formLineWidth = 1f
                formSize = 15f
                valueTextSize = 0f
                setDrawFilled(true)
            }

        }
        lineDataSet.highLightColor = Color.TRANSPARENT
        dataSets.add(lineDataSet)
        if (Utils.getSDKInt() >= 18) {
            val drawable =
                ContextCompat.getDrawable(
                    NoisefitApplication.context!!,
                    bgGradient
                )
            lineDataSet.fillDrawable = drawable
        } else {
            lineDataSet.fillColor =
                com.noisefit_commans.R.color.lightest_gray.getColor()
        }
        lineDataSet.fillFormatter =
            IFillFormatter { _, _ -> chart.axisLeft.axisMinimum }
        val data = LineData(dataSets)
        chart.data = data

        chart.invalidate()
        chart.notifyDataSetChanged()
    }


    fun setHeartChartWithXAxisAndXLabel(
        chart: LineChart,
        xLabelList: ArrayList<String>,
        average: Int,
        values: ArrayList<Entry>,
        color1: ArrayList<Int>,
        bgGradient: Int
    ) {
        baseHeartChartWithXLabel(chart, xLabelList)
        /*val leftAxis = chart.axisLeft
        leftAxis.isEnabled = false

        val rightAxis = chart.axisRight
        rightAxis.axisMinimum = 0f
        rightAxis.spaceTop = 0.8f
        rightAxis.spaceBottom = 0.8f
        rightAxis.setDrawGridLines(false)
        rightAxis.setDrawAxisLine(false)
        rightAxis.setDrawZeroLine(false)
        rightAxis.gridColor = com.noisefit_commans.R.color.white_12.getColor()

        val xAxis = chart.xAxis
        //xAxis.setLabelCount(8, true)
        xAxis.isEnabled = true
        xAxis.spaceMax = 0.5f
        xAxis.spaceMin = 0.5f
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.gridColor = com.noisefit_commans.R.color.white_12.getColor()
        xAxis.axisLineColor = com.noisefit_commans.R.color.white_12.getColor()
        xAxis.textColor = com.noisefit_commans.R.color.white_64.getColor()

        xAxis.setDrawGridLines(true)
        xAxis.setLabelCount(5, true)


        xAxis.valueFormatter =
            IAxisValueFormatter { value, _ ->
                try {
                    xLabelList[value.toInt()]
                } catch (exp: Exception) {
                    ""
                }
            }*/

        addLimitLine(average, com.noisefit_commans.R.color.white_12, chart)
        val dataSets = ArrayList<ILineDataSet>()

        val lineDataSet: LineDataSet
        if (chart.data != null &&
            chart.data.dataSetCount > 0
        ) {
            lineDataSet = chart.data.getDataSetByIndex(0) as LineDataSet
            lineDataSet.entries = values
            lineDataSet.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            lineDataSet = LineDataSet(values, "")
            lineDataSet.apply {
                setDrawIcons(false)

                mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                colors = color1
                circleRadius = 5f
                isShowLastCircle = false
                setDrawCircles(false)
                lineWidth = 2f
                valueTextSize = 0f
                setDrawCircleHole(false)
                formLineWidth = 1f
                formSize = 15f
                valueTextSize = 0f
                setDrawFilled(true)
            }

        }
        lineDataSet.highLightColor = Color.TRANSPARENT
        dataSets.add(lineDataSet)
        if (Utils.getSDKInt() >= 18) {
            val drawable =
                ContextCompat.getDrawable(
                    NoisefitApplication.context!!,
                    bgGradient
                )
            lineDataSet.fillDrawable = drawable
        } else {
            lineDataSet.fillColor =
                com.noisefit_commans.R.color.lightest_gray.getColor()
        }
        lineDataSet.fillFormatter =
            IFillFormatter { _, _ -> chart.axisLeft.axisMinimum }
        val data = LineData(dataSets)
        chart.data = data

        chart.invalidate()
        chart.notifyDataSetChanged()
    }

    fun setComparisonChart(
        compChart: LineChart,
        graphEntriesData: Triple<List<Entry>, List<Entry>, Pair<List<Int>, List<Int>>>
    ) {
        compChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            animateX(500)
        }
        val chart = compChart

        val legend = chart.legend
        legend.isEnabled = false

        val leftAxis = chart.axisLeft
        leftAxis.isEnabled = false

        val rightAxis = chart.axisRight
        rightAxis.textColor =
            NoiseFitApplicationMain.context!!.resources.getColor(R.color.lightest_gray)
        rightAxis.setDrawGridLines(true)
        rightAxis.setLabelCount(5, true)
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        rightAxis.setDrawAxisLine(true)
        rightAxis.axisMinimum = 0f

        val xAxis = chart.xAxis
        xAxis.isEnabled = true
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor =
            NoiseFitApplicationMain.context!!.resources.getColor(R.color.lightest_gray)
        xAxis.setDrawGridLines(false)

        val xLabel: ArrayList<String>
        val list = ArrayList<String>()
        for (i in 0 until 23) {
            val t: String = if (i == 0)
                "12"
            else
                i.toString()
            list.add("$t:00")
        }
        xLabel = list
        xAxis.labelCount = 5
        xAxis.valueFormatter =
            IAxisValueFormatter { value, _ ->
                try {
                    xLabel[value.toInt()]
                } catch (exp: Exception) {
                    ""
                }
            }


        val dataSets = java.util.ArrayList<ILineDataSet>()


        val d1 = LineDataSet(graphEntriesData.first, "")
        d1.apply {
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            isShowLastCircle = true
            setDrawCircles(false)
            lineWidth = 1f
            valueTextSize = 0f
            cubicIntensity = 0.2f
            setDrawCircleHole(false)
            formLineWidth = 1f
            formSize = 15f
            valueTextSize = 0f
            colors = graphEntriesData.third.first
        }
        dataSets.add(d1)
        val d2 = LineDataSet(graphEntriesData.second, "")
        d2.apply {
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            isShowLastCircle = true
            setDrawCircles(false)
            lineWidth = 1f
            valueTextSize = 0f
            cubicIntensity = 0.2f
            setDrawCircleHole(false)
            formLineWidth = 1f
            formSize = 15f
            valueTextSize = 0f
            colors = graphEntriesData.third.second
        }
        dataSets.add(d2)
        val data = LineData(dataSets)
        chart.data = data
        chart.invalidate()
    }


}