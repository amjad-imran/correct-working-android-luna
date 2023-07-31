package com.noisefit.oreo.util.graph

import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.noisefit_commans.NoisefitApplication

object OCandleGraphUtils {
    private fun geCandleChartDataSet(
        values: List<CandleEntry>,
        chart: CandleStickChart,
        overlayMode: Boolean,
        color: Int
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
                    ContextCompat.getColor(
                        NoisefitApplication.context!!,
                        color
                    )

                val endColor =
                    ContextCompat.getColor(
                        NoisefitApplication.context!!,
                        color
                    )

                decreasingColor = endColor
                increasingColor = startColor
            } else {
                val startColor =
                    ContextCompat.getColor(
                        NoisefitApplication.context!!,
                        color
                    )

                val endColor =
                    ContextCompat.getColor(
                        NoisefitApplication.context!!,
                        color
                    )
                decreasingColor = endColor
                increasingColor = startColor
            }
            increasingPaintStyle = Paint.Style.FILL

            valueTextSize = 0f
        }

        barDataSet.setDrawHorizontalHighlightIndicator(false)
        barDataSet.setDrawVerticalHighlightIndicator(false)

//        val spacingValue = when {
//            values.size < 10 -> {
//                0.45f
//            }
//            values.size in 11..19 -> {
//                0.40f
//            }
//            else -> {
//                0.30f
//            }
//        }
//        barDataSet.barSpace = spacingValue



        return barDataSet
    }

    fun setSleepChartData(
        values: List<CandleEntry>,
        chart: CandleStickChart,
        overlayMode: Boolean,
        color: Int
    ) {
        val dataSets = ArrayList<ICandleDataSet>()

        dataSets.add(
            geCandleChartDataSet(
                values,
                chart,
                overlayMode,
                color
            )
        )
        val data = CandleData(dataSets)
        chart.data = data
        chart.invalidate()
        chart.notifyDataSetChanged()
    }

    fun setHrChart(
        chart: CandleStickChart,
        overlayMode: Boolean,
        xLabelList: ArrayList<String>
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
                ContextCompat.getColor(
                    NoisefitApplication.context!!,
                    com.noisefit_commans.R.color.white_64
                )

            rightAxis.setDrawGridLines(true)
            rightAxis.setLabelCount(3, true)
            rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            rightAxis.setDrawAxisLine(false)
            rightAxis.axisMinimum = 0f
            rightAxis.isEnabled = true
            rightAxis.enableGridDashedLine(10f, 15f, 0f)
        } else {
            chart.axisRight.isEnabled = false
        }


        val xAxis = chart.xAxis
        xAxis.isEnabled = true
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor =
            ContextCompat.getColor(
                NoisefitApplication.context!!,
                com.noisefit_commans.R.color.white_64
            )

        xAxis.setDrawGridLines(false)
        xAxis.labelCount = 5

        xAxis.valueFormatter =
            IAxisValueFormatter { value, _ ->
                try {
                    xLabelList[value.toInt()]
                } catch (exp: Exception) {
                    ""
                }
            }
    }

}