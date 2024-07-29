package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentSleepBarChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.TrendsGraphData
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class SleepBarChartFragment :
    BaseFragment<FragmentSleepBarChartBinding>(FragmentSleepBarChartBinding::inflate) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils
    private var pageData: TrendsGraphData? = null

    companion object {
        private const val graphData = "GRAPH_DATA"

        @JvmStatic
        fun newInstance(pageData: TrendsGraphData) =
            SleepBarChartFragment().apply {
                arguments = Bundle().apply {
                    this.putParcelable(graphData, pageData)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            pageData = bundle.getParcelable(SleepBarChartFragment.graphData)
        }

        val dataList = pageData?.data?.map { it.value1 } ?: ArrayList()

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