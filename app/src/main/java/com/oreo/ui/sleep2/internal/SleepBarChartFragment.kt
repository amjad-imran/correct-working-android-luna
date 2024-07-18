package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentSleepBarChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.custom.SleepGraphInteractionListener
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class SleepBarChartFragment :
    BaseFragment<FragmentSleepBarChartBinding>(FragmentSleepBarChartBinding::inflate) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    companion object {
        @JvmStatic
        fun newInstance() =
            SleepBarChartFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dataList = arrayListOf(
            20,
            30,
            null,
            50,
            100,
            70,
            null
        )
        val maxValue = getMaxValue(dataList)
        val avgValue = getAvgValue(dataList)
        val yAxisRange = getYAxisRange(maxValue)


        binding.graphBar.setDataSet(
            dataList,
            yAxisRange,
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

    private fun getAvgValue(list: List<Int?>): Pair<Int, String> {
        val avg = list.filterNotNull().average().roundToInt()
        return Pair(avg, "$avg%")
    }

    private fun getMaxValue(list: List<Int?>): Int {
        return 100
    }


    private fun getYAxisRange(maxValue: Any): List<Pair<Int, String>> {
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