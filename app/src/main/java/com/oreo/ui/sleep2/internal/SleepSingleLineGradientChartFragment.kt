package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentSleepSingleLineGradientChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.TrendsGraphData
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import com.oreo.ui.custom.sleep.internal.SleepSingleGradientChartType
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class SleepSingleLineGradientChartFragment :
    BaseFragment<FragmentSleepSingleLineGradientChartBinding>(
        FragmentSleepSingleLineGradientChartBinding::inflate
    ) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils
    private var pageData: TrendsGraphData? = null

    private val sharedViewModel: OSPTrendsSharedViewModel by activityViewModels()

    companion object {
        private const val graphData = "GRAPH_DATA"

        @JvmStatic
        fun newInstance(pageData: TrendsGraphData) = SleepSingleLineGradientChartFragment().apply {
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

        val dataList = pageData?.data?.map {
            if (pageData?.contributorType == SleepInternalLaunchState.SLEEP_DURATION) {
                if (it.value1 != null) {
                    (it.value1 ?: 0) / 60
                } else it.value1
            } else {
                it.value1
            }

        } ?: ArrayList()

        val maxValue = sharedViewModel.getMaxValue(dataList, pageData?.contributorType)
        val yAxisRange = sharedViewModel.getYAxisRange(maxValue, pageData?.contributorType)
        val xAxisRange = getXAxisRange()
        val avgValue = sharedViewModel.getAvgValue(dataList, pageData?.contributorType)

        val type = if (pageData?.contributorType == SleepInternalLaunchState.SLEEP_DURATION) {
            SleepSingleGradientChartType.TIME
        } else {
            SleepSingleGradientChartType.PERCENT
        }

        binding.graphBar.setDataSet(
            dataList, yAxisRange, xAxisRange, yAxisRange.last().first, avgValue, -1,
            type
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

    private fun getAvgValue(list: List<Int?>): Pair<Int, String>? {
        val filteredData = list.filterNotNull()
        if (filteredData.isEmpty()) {
            return null
        }

        val avg = list.filterNotNull().average().roundToInt()
        if (pageData?.contributorType == SleepInternalLaunchState.SLEEP_DURATION) {
            return Pair(avg, "${avg / 60}")
        } else {
            return Pair(avg, "$avg%")
        }
    }

    private fun getXAxisRange(): List<String> {
        return when (pageData?.selectedPeriod) {
            InternalSelectedPeriod.MONTH -> {
                arrayListOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
            }

            InternalSelectedPeriod.WEEK -> {
                arrayListOf("W1", "W2", "W3", "W4", "W5", "W6")
            }

            else -> {
                arrayListOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            }
        }
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}