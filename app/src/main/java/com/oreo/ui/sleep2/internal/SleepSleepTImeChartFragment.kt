package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentSleepSleepTimeBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.TrendsGraphData
import com.oreo.data.model.TrendsValues
import com.oreo.ui.custom.sleep.SleepTimeModel
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.abs

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


        val dataList = convertData(pageData?.data, pageData?.contributorType)


        val yAxisRange = sharedViewModel.getYAxisRange(100f, pageData?.contributorType)
        val xAxisRange = sharedViewModel.getXAxisRange(pageData)

        binding.graphBar.setDataSet(
            dataList.first, yAxisRange, xAxisRange,
            pageData?.selectedPeriod,dataList.second
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

    private fun convertData(
        data: List<TrendsValues>?,
        contributorType: SleepInternalLaunchState?
    ): Pair<List<SleepTimeModel>,LocalDateTime?> {
        //Get min start time based on day start time
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val minTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        var minValue: Long? = null
        var minTime: LocalDateTime? = null

        data?.forEach {

            val startDate = if (contributorType == SleepInternalLaunchState.TIMING) {
                it.master_mid_time
            } else {
                it.master_start_time
            }

            if (startDate != null) {
                val currentDay = LocalDate.parse(it.date).atStartOfDay()

                val sleepStartTime =
                    LocalDateTime.parse(startDate, dateTimeFormatter)

                val difference = Duration.between(currentDay, sleepStartTime).toMinutes()
                val newStartTime = if (difference < 0) {
                    1440 - abs(difference)
                } else {
                    1440 + difference
                }

                if (minValue == null) {
                    minValue = newStartTime
                    minTime  = sleepStartTime
                } else if (newStartTime < minValue!!) {
                    minValue = newStartTime
                }
            }
        }

        val result = ArrayList<SleepTimeModel>()
        data?.forEach {
            if (contributorType == SleepInternalLaunchState.TIMING) {
                if (it.master_mid_time != null) {
                    val currentDay = LocalDate.parse(it.date).atStartOfDay()
                    val sleepStartTime =
                        LocalDateTime.parse(it.master_mid_time, dateTimeFormatter)
                    val sleepEndTime = LocalDateTime.parse(it.master_mid_time, dateTimeFormatter)

                    val difference = Duration.between(currentDay, sleepStartTime).toMinutes()
                    val newStartTime = if (difference < 0) {
                        1440 - abs(difference)
                    } else {
                        1440 + difference
                    }

                    val sleepDifference = Duration.between(sleepEndTime, sleepStartTime).toMinutes()

                    val startTime = (newStartTime - (minValue ?: 0L))
                    val endTime = startTime + abs(sleepDifference)
                    result.add(
                        SleepTimeModel(
                            startTime = startTime,
                            endTime = 0L,
                            startTimeText = sleepStartTime.format(DateTimeFormatter.ofPattern("hh:mm")),
                            endTimeString = sleepEndTime.format(DateTimeFormatter.ofPattern("hh:mm"))
                        )
                    )
                } else {
                    result.add(
                        SleepTimeModel(
                            startTime = 0,
                            endTime = 0,
                            startTimeText = "",
                            endTimeString = ""
                        )
                    )
                }
            } else {
                if (it.master_start_time != null) {
                    val currentDay = LocalDate.parse(it.date).atStartOfDay()
                    val sleepStartTime =
                        LocalDateTime.parse(it.master_start_time, dateTimeFormatter)
                    val sleepEndTime = LocalDateTime.parse(it.master_end_time, dateTimeFormatter)

                    val difference = Duration.between(currentDay, sleepStartTime).toMinutes()
                    val newStartTime = if (difference < 0) {
                        1440 - abs(difference)
                    } else {
                        1440 + difference
                    }

                    val sleepDifference = Duration.between(sleepEndTime, sleepStartTime).toMinutes()

                    val startTime = (newStartTime - (minValue ?: 0L))

                    val endTime = startTime + abs(sleepDifference)
                    result.add(
                        SleepTimeModel(
                            startTime = startTime,
                            endTime = endTime,
                            startTimeText = sleepStartTime.format(DateTimeFormatter.ofPattern("hh:mm")),
                            endTimeString = sleepEndTime.format(DateTimeFormatter.ofPattern("hh:mm"))
                        )
                    )
                } else {
                    result.add(
                        SleepTimeModel(
                            startTime = 0,
                            endTime = 0,
                            startTimeText = "",
                            endTimeString = ""
                        )
                    )
                }
            }


        }
        return Pair(result,minTime)
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}
