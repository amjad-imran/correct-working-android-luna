package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentSleepMultiLineChart2Binding
import com.noisefit.luna.databinding.FragmentSleepSleepTimeBinding
import com.noisefit.util.ApplicationUtils.getFormattedSleepDuration
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.TrendsGraphData
import com.oreo.data.model.TrendsValues
import com.oreo.ui.custom.sleep.internal.GraphDataModel
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class SleepSleepTImeChartFragment :
    BaseFragment<FragmentSleepSleepTimeBinding>(FragmentSleepSleepTimeBinding::inflate) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    private var pageData: TrendsGraphData? = null

    private val sharedViewModel: OSPTrendsSharedViewModel by activityViewModels()

    companion object {
        private const val GRAPH_DATA = "GRAPH_DATA"

        @JvmStatic
        fun newInstance(pageData: TrendsGraphData) = SleepSleepTImeChartFragment().apply {
            arguments = Bundle().apply {
                this.putParcelable(GRAPH_DATA, pageData)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            pageData = bundle.getParcelable(SleepSleepTImeChartFragment.GRAPH_DATA)
        }


        val dataList = convertData(pageData?.data)

        val maxValue = sharedViewModel.getMaxValue(
            dataListType1 = dataList, contributorType = pageData?.contributorType
        )
        val avgValue =
            getAvgValue(dataListType2 = dataList, contributorType = pageData?.contributorType)
        val yAxisRange = sharedViewModel.getYAxisRange(maxValue, pageData?.contributorType)
        val xAxisRange = sharedViewModel.getXAxisRange(pageData)

        binding.graphBar.setDataSet(
            dataList, yAxisRange, xAxisRange, yAxisRange.last().first,
            avgValue,
            -1,
            pageData?.contributorType, pageData?.selectedPeriod
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
                value1 = if (it.value1 == null) {
                    null
                } else {
                    (it.value1 ?: 0.0f) / 60
                },
                value2 = if (it.value2 == null) {
                    null
                } else {
                    (it.value2 ?: 0.0f) / 60
                }
            )
        } ?: ArrayList()
    }

    private fun getAvgValue(
        dataListType2: List<GraphDataModel>,
        contributorType: SleepInternalLaunchState?
    ): Pair<Pair<Int, String>?, Pair<Int, String>?> {

        val filteredDataHour = dataListType2.mapNotNull { it.value1 }
        val filteredDataNeed = dataListType2.mapNotNull { it.value2 }


        var averageHour: Int? = null
        var averageHourString: String? = null
        var averageNeed: Int? = null
        var averageNeedString: String? = null
        if (filteredDataHour.isNotEmpty()) {
            averageHour = filteredDataHour.average().roundToInt()

            val (hour, minute) = getFormattedSleepDuration(
                averageHour
            )

            averageHourString = String.format(locale = Locale.US, "%d:%02d", hour, minute)
        }
        if (filteredDataNeed.isNotEmpty()) {
            averageNeed = filteredDataNeed.average().roundToInt()
            val (hour, minute) = getFormattedSleepDuration(
                averageNeed
            )

            averageNeedString = String.format(locale = Locale.US, "%d:%02d", hour, minute)
        }

        return Pair(
            if (averageHour == null) null else Pair(
                averageHour,
                "$averageHourString"
            ),
            if (averageNeed == null) null else Pair(averageNeed, "$averageNeedString")
        )
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}
