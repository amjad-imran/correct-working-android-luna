//package com.noisefit.util.graph
//
//
//import com.github.mikephil.charting.charts.BarChart
//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.data.BarData
//import com.github.mikephil.charting.data.BarDataSet
//import com.github.mikephil.charting.data.BarEntry
//import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
//import com.github.mikephil.charting.utils.Utils
//import com.noisefit.NoiseFitApplicationMain
//import com.noisefit.luna.R
//import com.noisefit.util.RoundedBarChart
//
//
//object DistanceBarChartUtils {
//
//    private fun getSmallLineChartDataSet(
//        values: List<BarEntry>,
//        chart: BarChart
//    ): BarDataSet {
//
//        val barDataSet: BarDataSet
//        if (chart.data != null &&
//            chart.data.dataSetCount > 0
//        ) {
//            barDataSet = chart.data.getDataSetByIndex(0) as BarDataSet
//            barDataSet.values = values
//            barDataSet.notifyDataSetChanged()
//            chart.data.notifyDataChanged()
//            chart.notifyDataSetChanged()
//        } else {
//            barDataSet = BarDataSet(values, "")
//            barDataSet.apply {
//                setDrawIcons(false)
//                color = NoiseFitApplicationMain.context!!.resources.getColor(R.color.distance)
//                val leftAxis = chart.axisLeft
//                leftAxis.axisMinimum = 0f
//                formLineWidth = 1f
//                formSize = 15f
//                valueTextSize = 0f
//            }
//        }
//        return barDataSet
//    }
//
//
//    fun setSmallChart(
//        chart: RoundedBarChart
//    ) {
//        //  chart.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
//        chart.apply {
//            description.isEnabled = false
//            setTouchEnabled(false)
//            isDragEnabled = false
//            setScaleEnabled(false)
//            setPinchZoom(false)
//            setRadius(Utils.convertDpToPixel(15f).toInt())
//        }
//
//        val legend = chart.legend
//        legend.isEnabled = false
//
//
//        val leftAxis = chart.axisLeft
//        leftAxis.isEnabled = false
//
//        val rightAxis = chart.axisRight
//        rightAxis.axisMinimum = 0f
//        rightAxis.spaceTop = 0f
//        rightAxis.isEnabled = false
//
//
//        val xAxis = chart.xAxis
//        xAxis.isEnabled = false
//        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
//
//    }
//
//
//    fun setSmallChartData(
//        values: ArrayList<BarEntry>,
//        chart: BarChart
//    ) {
//        val dataSets = ArrayList<IBarDataSet>()
//
//        dataSets.add(
//            getSmallLineChartDataSet(
//                values,
//                chart
//            )
//        ) // add the data sets
//        val data = BarData(dataSets)
//        chart.data = data
//        data.barWidth = 0.5f
//
//        chart.invalidate()
//        chart.notifyDataSetChanged()
//    }
//
//    fun stressType(value: Int): String {
//        return when (value) {
//            in 0..29 -> {
//                "Relax"
//            }
//            in 30..59 -> {
//                "Normal"
//            }
//            in 60..79 -> {
//                "Medium"
//            }
//            in 80..100 -> {
//                "High"
//            }
//            else -> ""
//        }
//    }
//
//
//}
