package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.noisefit.luna.databinding.FragmentSleepSingleLineChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.OSleepInternalTrendsDataModel
import com.oreo.data.model.TrendsGraphData
import com.oreo.data.model.TrendsValues
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class SleepSingleLineChartFragment :
    BaseFragment<FragmentSleepSingleLineChartBinding>(FragmentSleepSingleLineChartBinding::inflate) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils
    private var pageData: TrendsGraphData? = null

    companion object {
        private const val graphData = "GRAPH_DATA"

        @JvmStatic
        fun newInstance(pageData: TrendsGraphData) =
            SleepSingleLineChartFragment().apply {
                arguments = Bundle().apply {
                    this.putParcelable(graphData, pageData)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            pageData = bundle.getParcelable(graphData)
        }

        //Value in minutes
        /*val dataList = arrayListOf(
            10, 30, 20, 70, 10, 50, 90,
            10, 70, 80, 90, null, null, null,
            null, null, null, 50, 90, 10, 20,
            8, 70, 20, 10, 50, 90, 20,
            60, 70, 80, 90, 10, 50, 90,
            10, 50, 90, 90, 10, 10, 20,
        )*/


        val tempList = ArrayList<Int?>()
        //pageData = tempList.add(it.value?.div(60))

        //31+29+30+31+30+31+30
        /*val dataList = ArrayList<Int?>()
        for (i in 0 until 182) {
            dataList.add(getRandomInt(20, 80))
        }*/

        val maxValue = getMaxValue(tempList)
        val yAxisRange = getYAxisRange(maxValue)
        val xAxisRange = getXAxisRange()
//        val avgValue = getAvgValue(tempList)
        val avgValue = getAvgValue(tempList)


        binding.graphBar.setDataSet(
            tempList,
            yAxisRange,
            xAxisRange,
            yAxisRange.last().first,
            avgValue,
            -1
        )


        binding.graphBar.setVibrationUtil(vibrationUtils)

        binding.graphBar.setClickListener(object : SleepSingleBarAction {
            override fun onValueSelected(position: Int) {

            }

            override fun isInteractionOnGoing(onGoing: Boolean) {

            }

        })
    }

    private fun getAvgValue(list: ArrayList<Int?>): Pair<Int, String> {
        val avg = list.filterNotNull().average().roundToInt()
        return Pair(avg, "$avg%")
    }

    private fun getXAxisRange(): List<String> {
        return arrayListOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
        //return arrayListOf("W1", "W2", "W3", "W4", "W5", "W6")
    }

    private fun getMaxValue(list: List<Int?>): Int {
        return 100
    }


    fun getYAxisRange(maxValue: Int): List<Pair<Int, String>> {
        return arrayListOf(
            Pair(0, "0%"),
            Pair(25, "25%"),
            Pair(50, "50%"),
            Pair(75, "75%"),
            Pair(100, "100%")
        )
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}