package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.noisefit.luna.databinding.FragmentSleepSingleLineChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.TrendsGraphData
import com.oreo.data.model.TrendsValues
import com.oreo.ui.custom.sleep.internal.GraphDataModel
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject


@AndroidEntryPoint
class SleepSingleLineChartFragment :
    BaseFragment<FragmentSleepSingleLineChartBinding>(FragmentSleepSingleLineChartBinding::inflate) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils
    private var pageData: TrendsGraphData? = null

    private val sharedViewModel: OSPTrendsSharedViewModel by activityViewModels()

    companion object {
        private const val graphData = "GRAPH_DATA"

        @JvmStatic
        fun newInstance(pageData: TrendsGraphData) = SleepSingleLineChartFragment().apply {
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
        val nonNullDataCount =
            sharedViewModel.getNonNullDataCount(pageData?.contributorType, dataList)

        val maxValue = sharedViewModel.getMaxValue(
            dataListType1 = dataList,
            contributorType = pageData?.contributorType
        )
        val yAxisRange = sharedViewModel.getYAxisRange(maxValue, pageData?.contributorType)
        val xAxisRange = sharedViewModel.getXAxisRange(pageData)

        val avgValue = sharedViewModel.getAvgValuePair(
            pageData?.avgValue,
            contributorType = pageData?.contributorType
        )
        val showOverlay =
            if (pageData?.selectedPeriod == InternalSelectedPeriod.DAY) false else true

        binding.graphBar.setDataSet(
            dataList,
            yAxisRange,
            xAxisRange,
            yAxisRange.last().first,
            avgValue,
            -1,
            showOverlay,
            pageData?.contributorType,
            pageData?.selectedPeriod,
            nonNullDataCount

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
        val isMetric = sharedViewModel.sessionManager.isMetric()

        return data?.map {
            GraphDataModel(
                date = LocalDate.parse(it.date),
                value1 = if (pageData?.contributorType == SleepInternalLaunchState.SLEEP_DURATION ||
                    pageData?.contributorType == SleepInternalLaunchState.REM_SLEEP ||
                    pageData?.contributorType == SleepInternalLaunchState.DEEP_SLEEP
                ) {
                    if (it.value1 != null) {
                        (it.value1 ?: 0.0f) / 60
                    } else null
                } else if (it.value1 != null && pageData?.contributorType == SleepInternalLaunchState.SKIN_TEMPERATURE && isMetric) {
                    val convertedValue = AppConversionUtils.fahrenheitToCelsius(
                        it.value1!!
                    )
                    if (convertedValue < 0) {
                        0.0f
                    } else {
                        convertedValue
                    }
                } else if (pageData?.contributorType == SleepInternalLaunchState.RESPIRATORY_RATE
                    || pageData?.contributorType == SleepInternalLaunchState.RESTING_HEART_RATE
                    || pageData?.contributorType == SleepInternalLaunchState.HRV
                ) {
                    if (it.value1 == 255f) null else it.value1
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