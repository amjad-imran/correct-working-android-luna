package com.noisefit.util.graph

import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.Utils
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.R
import com.noisefit_commans.data.model.MarkerEntry
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.RoundedBarChart

object HeartChartUtils {

    fun setHrChartData(
        values: List<BarEntry>,
        chart: BarChart,
        color: Int,
        markEntryList: ArrayList<MarkerEntry>,
        containerWidth: Float = 0f
    ) {
        val dataSets = ArrayList<IBarDataSet>()

        dataSets.add(
            geBarChartDataSet(
                values,
                chart
            )
        )
        val data = BarData(dataSets)
        chart.data = data
        data.barWidth = ApplicationUtils.getBarWidth(containerWidth, markEntryList.size)
        chart.invalidate()
        chart.notifyDataSetChanged()
    }

    fun setInvalidate(chart: RoundedBarChart) {
        chart.clear()
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    fun setInvalidate(chart: LineChart) {
        chart.clear()
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun geBarChartDataSet(
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
                color = NoiseFitApplicationMain.context!!.resources.getColor(R.color.heart_rate)
                val leftAxis = chart.axisLeft
                leftAxis.axisMinimum = 0f
                formLineWidth = 1f
                formSize = 15f
                valueTextSize = 0f
            }
        }

        return barDataSet
    }

    fun setDayHrChartData(
        values: List<Entry>,
        chart: LineChart,
        isActivityGraph: Boolean
    ) {
        var startColor = R.color.heart_rate
        var endColor = R.color.heart_rate
        if (isActivityGraph) {
            startColor = R.color.activity_heart_rate_start
            endColor = R.color.activity_heart_rate_end
        }
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(
            getLineChartDataSet(
                values,
                chart,
                startColor,
                endColor,
                isActivityGraph
            )
        ) // add the data sets setHighlightEnabled
        val data = LineData(dataSets)
        chart.data = data
        // chart.isHighlightPerTapEnabled = false

        chart.invalidate()
        chart.notifyDataSetChanged()
    }

    fun setDayHrChartData(
        values: List<Entry>,
        chart: LineChart
    ) {
        val startColor = R.color.activity_hrv_start
        val endColor = R.color.activity_hrv_end

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(
            getLineChartDataSet(
                values,
                chart,
                startColor,
                endColor,
                true
            )
        ) // add the data sets setHighlightEnabled
        val data = LineData(dataSets)
        chart.data = data
        // chart.isHighlightPerTapEnabled = false

        chart.invalidate()
        chart.notifyDataSetChanged()
    }

    private fun getLineChartDataSet(
        values: List<Entry>,
        lineChart: LineChart,
        lineColor: Int,
        bgColor: Int,
        isActivityGraph: Boolean
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
                color = NoiseFitApplicationMain.context!!.resources.getColor(lineColor)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.1f
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

            //hide line indicator on chart
            lineDataSet.setDrawHorizontalHighlightIndicator(false)
            lineDataSet.setDrawVerticalHighlightIndicator(false)


            if (Utils.getSDKInt() >= 18) {
                val drawable =
                    ContextCompat.getDrawable(
                        NoiseFitApplicationMain.context!!,
                        R.drawable.back_hr_history
                    )
                lineDataSet.fillDrawable = drawable
            } else {
                lineDataSet.fillColor =
                    NoiseFitApplicationMain.context!!.resources.getColor(bgColor)
            }
            lineDataSet.fillFormatter =
                IFillFormatter { _, _ -> lineChart.axisLeft.axisMinimum }
        }
        return lineDataSet
    }

    /* private fun getCandleChartDataSet(
         values: List<BarEntry>,
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
                 val startColor = NoiseFitApplicationMain.context!!.resources.getColor(color)
                 val endColor = NoiseFitApplicationMain.context!!.resources.getColor(color)
                 decreasingColor = endColor
                 increasingPaintStyle = Paint.Style.FILL
                 increasingColor = startColor
                 valueTextSize = 0f

             }


         }
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
     }*/


    fun setHrChart(
        chart: RoundedBarChart,
        graphInterval: GraphInterval,
        monthList: ArrayList<String>
    ) {
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
        rightAxis.setLabelCount(5, true)
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

    fun setDayHrChart(chart: LineChart, isActivityGraph: Boolean, labelCount: Int = 5) {

        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
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
        rightAxis.textColor =
            NoiseFitApplicationMain.context!!.resources.getColor(R.color.lightest_gray)
        rightAxis.setDrawGridLines(true)
        rightAxis.setLabelCount(labelCount, true)
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        rightAxis.setDrawAxisLine(false)
        rightAxis.axisMinimum = 0f
        //rightAxis.spaceTop = 0f

        val xAxis = chart.xAxis

        if (isActivityGraph) {
            xAxis.isEnabled = false
        } else {
            xAxis.isEnabled = true
            chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
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
        }


        /*xAxis.axisMaximum = xLabel.size.toFloat()
        chart.setVisibleXRange(1f, xLabel.size.toFloat())*/


    }

    fun setSmallHrChart(chart: LineChart) {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
//            setRadius(Utils.convertDpToPixel(15f).toInt())
        }

        val legend = chart.legend
        legend.isEnabled = false


        val leftAxis = chart.axisLeft
        leftAxis.isEnabled = false

        val rightAxis = chart.axisRight
        rightAxis.axisMinimum = 0f
        rightAxis.spaceTop = 0f
        rightAxis.setLabelCount(5, true)
        rightAxis.isEnabled = false


        val xAxis = chart.xAxis
        xAxis.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
    }
}