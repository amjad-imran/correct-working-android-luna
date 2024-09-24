package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentSleepDailyGradientChartBinding
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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.Locale
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

        val dataList = convertData(pageData?.data, pageData?.contributorType)

        val minMax = sharedViewModel.getMinMaxValue(
            dataListType1 = dataList,
            contributorType = pageData?.contributorType
        )
        val yAxisRange = sharedViewModel.getYAxisRange(minMax.second, pageData?.contributorType, minValue = minMax.first)

        val avgValue = sharedViewModel.getAvgValuePair(
            pageData?.avgValue,
            contributorType = pageData?.contributorType
        )
        val optimalRange = sharedViewModel.getOptimalRangeMinMax(pageData?.contributorType)

        val firstData = pageData?.data?.firstOrNull()
        binding.graphBar.setDataSet(
            dataList, yAxisRange, avgValue, -1,
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

                    val currentTime = if (firstData?.start_time == null) {
                        null
                    } else {
                        val time = LocalDateTime.parse(
                            firstData.start_time,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        )
                        val multiplier =
                            if (pageData?.contributorType == SleepInternalLaunchState.BLOOD_OXYGEN) {
                                15
                            } else {
                                5
                            }
                        time.plus((multiplier * position).toLong(), ChronoUnit.MINUTES).format(
                            DateTimeFormatter.ofPattern("hh:mm a")
                        )
                    }

                    sharedViewModel.sendInteractDaily(
                        LocalDate.parse(date),
                        value,
                        currentTime.toString()
                    )
                } catch (exp: Exception) {
                    sharedViewModel.sendInteractDaily(null, null, null)
                }
            }

            override fun isInteractionOnGoing(onGoing: Boolean) {
                sharedViewModel.sendInteractDaily(null, null, null)
            }

        })
    }

    private fun convertData(
        data: List<TrendsValues>?,
        contributorType: SleepInternalLaunchState?
    ): List<GraphDataModel> {
        val firstValue = data?.firstOrNull() ?: return ArrayList()
        val date = LocalDate.parse(firstValue.date)

        val isMetric = sharedViewModel.sessionManager.isMetric()
        return firstValue.breakup?.map {
            GraphDataModel(
                date = date,
                value1 = if (contributorType == SleepInternalLaunchState.SKIN_TEMPERATURE && isMetric) {
                    val convertedValue = AppConversionUtils.fahrenheitToCelsius(
                        it
                    )
                    if (convertedValue < 0) {
                        0.0f
                    } else {
                        convertedValue
                    }
                } else if (contributorType == SleepInternalLaunchState.RESPIRATORY_RATE
                    || contributorType == SleepInternalLaunchState.RESTING_HEART_RATE
                    || contributorType == SleepInternalLaunchState.HRV
                ) {
                    if (it == 255f) null else it
                } else {
                    it
                }
            )
        } ?: ArrayList()
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}