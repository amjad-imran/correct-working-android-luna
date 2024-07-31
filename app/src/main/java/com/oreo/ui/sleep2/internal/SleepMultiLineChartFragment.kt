package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentSleepMultiLineChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.TrendsGraphData
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.max

@AndroidEntryPoint
class SleepMultiLineChartFragment :
    BaseFragment<FragmentSleepMultiLineChartBinding>(FragmentSleepMultiLineChartBinding::inflate) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    private var pageData: TrendsGraphData? = null

    private val sharedViewModel: OSPTrendsSharedViewModel by activityViewModels()

    companion object {
        private const val GRAPH_DATA = "GRAPH_DATA"

        @JvmStatic
        fun newInstance(pageData: TrendsGraphData) =
            SleepMultiLineChartFragment().apply {
                arguments = Bundle().apply {
                    this.putParcelable(GRAPH_DATA, pageData)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            pageData = bundle.getParcelable(SleepMultiLineChartFragment.GRAPH_DATA)
        }

        val dataList = pageData?.data?.map { Pair(it.value1,it.value2) } ?: ArrayList()

        //Value in minutes
       /* val dataList = arrayListOf(
            Pair(480, 560),
            Pair(null, null),
            Pair(600, 480),
            Pair(null, null),
            Pair(480, 500),
            Pair(450, 480),
            Pair(480, 560),
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

            var max = it.first ?: 0
            if ((it.second ?: 0) > max) {
                max = it.second ?: 0
            }

            if (max > mMax) {
                mMax = max
            }
        }

        mMax += ((0.2) * mMax).toInt()
        return mMax
    }


    fun getYAxisRange(maxValue: Any): List<Pair<Int, String>> {
        return when (maxValue) {
            in 0..720 -> {
                arrayListOf(Pair(0, "0"), Pair(180, "3"), Pair(360, "6"), Pair(540, "9"), Pair(720, "12"))
            }

            else/*in 13..24*/ -> {
                arrayListOf(Pair(0, "0"), Pair(360, "6"), Pair(720, "12"), Pair(1080, "18"), Pair(144, "24"))
            }
        }
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}
