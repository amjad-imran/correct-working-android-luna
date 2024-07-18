package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentSleepMultiLineChartBinding
import com.noisefit.luna.databinding.FragmentSleepSingleLineChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

@AndroidEntryPoint
class SleepSingleLineChartFragment :
    BaseFragment<FragmentSleepSingleLineChartBinding>(FragmentSleepSingleLineChartBinding::inflate) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    companion object {
        @JvmStatic
        fun newInstance() =
            SleepSingleLineChartFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Value in minutes
        val dataList = arrayListOf(
            10, 30, 20, 70, 10, 50, 90,
            10, 70, 80, 90, null, 10, 20,
            60, 10, 10, 50, 90, 10, 20,
            8, 70, 20, 10, 50, 90, 20,
            60, 70, 80, 90, 10, 50, 90,
            10, 50, 90, 90, 10, 10, 20,
        )
        val maxValue = getMaxValue(dataList)
        val yAxisRange = getYAxisRange(maxValue)
        val xAxisRange = getXAxisRange()
        val avgValue = getAvgValue(dataList)


        binding.graphBar.setDataSet(
            dataList,
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
        return arrayListOf("W1", "W2", "W3", "W4", "W5", "W6")
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