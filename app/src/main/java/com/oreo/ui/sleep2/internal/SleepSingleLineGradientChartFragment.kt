package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentSleepSingleLineGradientChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.TrendsGraphData
import com.oreo.data.model.TrendsValues
import com.oreo.ui.custom.sleep.internal.GraphDataModel
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import com.oreo.ui.custom.sleep.internal.SleepSingleGradientChartType
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

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

        val dataList = convertData(pageData?.data)

        val maxValue = sharedViewModel.getMaxValue(
            dataListType1 = dataList,
            contributorType = pageData?.contributorType
        )
        val yAxisRange = sharedViewModel.getYAxisRange(maxValue, pageData?.contributorType)
        val xAxisRange = sharedViewModel.getXAxisRange(pageData)
        val avgValue = sharedViewModel.getAvgValue(
            dataListType1 = dataList,
            contributorType = pageData?.contributorType
        )

        val type = if (pageData?.contributorType == SleepInternalLaunchState.SLEEP_DURATION) {
            SleepSingleGradientChartType.TIME
        } else if (pageData?.contributorType == SleepInternalLaunchState.RESTING_HEART_RATE
            || pageData?.contributorType == SleepInternalLaunchState.HRV
            || pageData?.contributorType == SleepInternalLaunchState.SKIN_TEMPERATURE
        ) {
            SleepSingleGradientChartType.DEFAULT
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

    private fun convertData(data: List<TrendsValues>?): List<GraphDataModel> {
        return data?.map {
            GraphDataModel(
                date = LocalDate.parse(it.date),
                value1 = if (pageData?.contributorType == SleepInternalLaunchState.SLEEP_DURATION) {
                    if (it.value1 != null) {
                        (it.value1 ?: 0.0f) / 60
                    } else null
                } else {
                    it.value1
                }
            )
        } ?: ArrayList()
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}