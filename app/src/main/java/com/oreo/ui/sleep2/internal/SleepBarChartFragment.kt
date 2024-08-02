package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentSleepBarChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.TrendsGraphData
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class SleepBarChartFragment :
    BaseFragment<FragmentSleepBarChartBinding>(FragmentSleepBarChartBinding::inflate) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils
    private var pageData: TrendsGraphData? = null

    private val sharedViewModel: OSPTrendsSharedViewModel by activityViewModels()

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

        val dataList = pageData?.data?.map {
            if (pageData?.contributorType == SleepInternalLaunchState.REM_SLEEP ||
                pageData?.contributorType == SleepInternalLaunchState.DEEP_SLEEP
            ) {
                if (it.value1 == null) {
                    it.value1
                } else {
                    (it.value1 ?: 0) / 60
                }
            } else {
                it.value1
            }
        } ?: ArrayList()

        val maxValue = sharedViewModel.getMaxValue(dataListType1 = dataList, contributorType = pageData?.contributorType)
        val avgValue = sharedViewModel.getAvgValue(dataListType1 =dataList, contributorType = pageData?.contributorType)
        val yAxisRange = sharedViewModel.getYAxisRange(maxValue, pageData?.contributorType)


        binding.graphBar.setDataSet(
            dataList,
            yAxisRange,
            yAxisRange.last().first,
            avgValue,
            -1,
            pageData?.contributorType
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

    private fun getMaxValue(list: List<Int?>): Int {
        if (pageData?.contributorType == SleepInternalLaunchState.LATENCY) {
            val nonNullValues = list.filterNotNull()
            return if (nonNullValues.isEmpty()) {
                25
            } else {
                nonNullValues.max()
            }
        } else if (pageData?.contributorType == SleepInternalLaunchState.REM_SLEEP ||
            pageData?.contributorType == SleepInternalLaunchState.DEEP_SLEEP
        ) {
            val nonNullValues = list.filterNotNull()
            return if (nonNullValues.isEmpty()) {
                60
            } else {
                nonNullValues.max()
            }
        } else if (pageData?.contributorType == SleepInternalLaunchState.RESTFULNESS) {
            val nonNullValues = list.filterNotNull()
            return if (nonNullValues.isEmpty()) {
                4
            } else {
                nonNullValues.max()
            }

        } else {
            return 100
        }
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}