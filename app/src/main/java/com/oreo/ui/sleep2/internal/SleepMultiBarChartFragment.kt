package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentSleepMultiBarChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.TrendsGraphData
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class SleepMultiBarChartFragment :
    BaseFragment<FragmentSleepMultiBarChartBinding>(FragmentSleepMultiBarChartBinding::inflate) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    private var pageData: TrendsGraphData? = null
    private val sharedViewModel: OSPTrendsSharedViewModel by activityViewModels()

    companion object {
        private const val GRAPH_DATA = "GRAPH_DATA"

        @JvmStatic
        fun newInstance(pageData: TrendsGraphData) =
            SleepMultiBarChartFragment().apply {
                arguments = Bundle().apply {
                    this.putParcelable(GRAPH_DATA, pageData)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            pageData = bundle.getParcelable(SleepMultiBarChartFragment.GRAPH_DATA)
        }

        val dataList = pageData?.data?.map { Pair(it.value1, it.value2) } ?: ArrayList()

        /*val dataList = arrayListOf(
            Pair(60, 40),
            Pair(80, 50),
            Pair(null, null),
            Pair(null, null),
            Pair(120, 20),
            Pair(150, 10),
            Pair(180, 0),
        )*/

        val maxValue = getMaxValue(dataList)
        val yAxisRange = getYAxisRange(maxValue)

        binding.graphBar.setDataSet(
            dataList,
            yAxisRange,
            yAxisRange.last().first,
            -1
        )


        binding.graphBar.setVibrationUtil(vibrationUtils)

        binding.graphBar.setClickListener(object : SleepSingleBarAction {
            override fun onValueSelected(position: Int) {
                try {
                    val date = pageData?.data?.get(position)?.date
                    sharedViewModel.sendInteractDay(LocalDate.parse(date))
                } catch (exp: Exception) {
                    sharedViewModel.sendInteractDay(null)
                }
            }

            override fun isInteractionOnGoing(onGoing: Boolean) {
                sharedViewModel.sendInteractDay(null)
            }

        })
    }

    private fun getMaxValue(list: List<Pair<Int?, Int?>>): Int {
        var mMax = 0
        list.forEach {
            val sum = (it.first ?: 0) + (it.second ?: 0)
            if (sum > mMax) {
                mMax = sum
            }
        }

        mMax += ((0.2) * mMax).toInt()

        return mMax
    }


    private fun getYAxisRange(maxValue: Any): List<Pair<Int, String>> {
        return when (maxValue) {
            in 0..360 -> {
                arrayListOf(
                    Pair(0, "0"),
                    Pair(120, "2"),
                    Pair(240, "4"),
                    Pair(360, "6"),
                )
            }

            else -> {
                arrayListOf(
                    Pair(0, "0"),
                    Pair(180, "3"),
                    Pair(360, "6"),
                    Pair(540, "9"),
                    Pair(720, "12")
                )
            }
        }
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}