package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentSleepDailyGradientChartBinding
import com.noisefit.luna.databinding.FragmentSleepSingleLineGradientChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.LOGS
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
class SleepDailyGradientChartFragment :
    BaseFragment<FragmentSleepDailyGradientChartBinding>(
        FragmentSleepDailyGradientChartBinding::inflate
    ) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils
    private var pageData: TrendsGraphData? = null

    private val sharedViewModel: OSPTrendsSharedViewModel by activityViewModels()

    companion object {
        private const val graphData = "GRAPH_DATA"

        @JvmStatic
        fun newInstance(pageData: TrendsGraphData) = SleepDailyGradientChartFragment().apply {
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

        val avgValue = sharedViewModel.getAvgValuePair(
            pageData?.avgValue,
            contributorType = pageData?.contributorType
        )
        val optimalRange = sharedViewModel.getOptimalRangeMinMax(pageData?.contributorType)

        val firstData = pageData?.data?.firstOrNull()
        binding.graphBar.setDataSet(
            dataList, yAxisRange, yAxisRange.last().first, avgValue, -1,
            optimalRange,
            firstData?.start_time,
            firstData?.end_time,
        )

        binding.graphBar.setVibrationUtil(vibrationUtils)

        binding.graphBar.setClickListener(object : SleepSingleBarAction {
            override fun onValueSelected(position: Int) {
                try {
                    val firstValue = pageData?.data?.firstOrNull()
                    val date = firstValue?.date
                    val value = firstValue?.breakup?.get(position)
                    sharedViewModel.sendInteractDaily(LocalDate.parse(date), value)
                } catch (exp: Exception) {
                    sharedViewModel.sendInteractDaily(null, null)
                }
            }

            override fun isInteractionOnGoing(onGoing: Boolean) {
                val firstValue = pageData?.data?.firstOrNull()
                val date = firstValue?.date

                sharedViewModel.sendInteractDaily(null, null)
            }

        })
    }

    private fun convertData(data: List<TrendsValues>?): List<GraphDataModel> {
        val firstValue = data?.firstOrNull() ?: return ArrayList()
        val date = LocalDate.parse(firstValue.date)

        return firstValue.breakup?.map {
            GraphDataModel(
                date = date,
                value1 = it
            )
        } ?: ArrayList()
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}