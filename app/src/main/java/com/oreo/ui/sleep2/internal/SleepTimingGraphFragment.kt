package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentSleepTimingGraphBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.TrendsGraphData
import com.oreo.data.model.TrendsValues
import com.oreo.ui.custom.sleep.internal.GraphDataModel
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class SleepTimingGraphFragment : BaseFragment<FragmentSleepTimingGraphBinding>(
    FragmentSleepTimingGraphBinding::inflate
) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils
    private var pageData: TrendsGraphData? = null

    private val sharedViewModel: OSPTrendsSharedViewModel by activityViewModels()

    companion object {
        private const val graphData = "GRAPH_DATA"

        @JvmStatic
        fun newInstance(pageData: TrendsGraphData) = SleepTimingGraphFragment().apply {
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
        val xAxisRange = sharedViewModel.getXAxisRange(pageData)
        val yAxisRange = getYAxisRange(dataList.second)

        val optimalRange = sharedViewModel.getOptimalRangeMinMax(pageData?.contributorType)

        binding.graphBar.setDataSet(
            dataList.first, xAxisRange,
            yAxisRange,
            yAxisRange.last().first,
            optimalRange
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

    private fun getYAxisRange(maxDeviation: Float): List<Pair<Int, String>> {
        return if (maxDeviation <= (6 * 60)) {
            arrayListOf(
                Pair(-6 * 60, "6 PM"),
                Pair(-3 * 60, "9 PM"),
                Pair(0, "12 AM"),
                Pair(3 * 60, "3 AM"),
                Pair(6 * 60, "6 AM")
            )
        } else {
            arrayListOf(
                Pair(-12 * 60, "12 PM"),
                Pair(-6 * 60, "6 PM"),
                Pair(0, "12 AM"),
                Pair(6 * 60, "6 AM"),
                Pair(12 * 60, "12 PM")
            )
        }
    }

    /**
     * return pair(data,max deviation)
     */
    private fun convertData(data: List<TrendsValues>?): Pair<List<GraphDataModel>, Float> {
        val midTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val returnData = ArrayList<GraphDataModel>()

        var maxDeviation = 0.0f//in minutes
        data?.forEach {

            val currentDay = LocalDate.parse(it.date).atStartOfDay()

            if (it.master_mid_time == null) {
                returnData.add(
                    GraphDataModel(
                        date = LocalDate.parse(it.date), value1 = null
                    )
                )
            } else {
                val midTime = LocalTime.parse(it.master_mid_time, midTimeFormat)
                val dateToAppend = if (midTime.hour >= 20) {
                    LocalDate.parse(it.date).minusDays(1).toString()
                } else {
                    it.date
                }

                val startDate = "$dateToAppend ${it.master_mid_time}"

                val sleepStartTime = LocalDateTime.parse(startDate, dateTimeFormatter)

                val difference = Duration.between(currentDay, sleepStartTime).toMinutes()
                if (difference > maxDeviation) {
                    maxDeviation = difference.toFloat()
                }

                returnData.add(
                    GraphDataModel(
                        date = LocalDate.parse(it.date), value1 = difference.toFloat()
                    )
                )
            }
        }

        return Pair(returnData, maxDeviation)
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}