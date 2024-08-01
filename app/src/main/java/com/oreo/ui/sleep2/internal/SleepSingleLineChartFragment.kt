package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentSleepSingleLineChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.TrendsGraphData
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt


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

        val dataList = pageData?.data?.map {
            if (pageData?.contributorType == SleepInternalLaunchState.SLEEP_DURATION ||
                pageData?.contributorType == SleepInternalLaunchState.REM_SLEEP ||
                pageData?.contributorType == SleepInternalLaunchState.DEEP_SLEEP) {
                if (it.value1 != null) {
                    (it.value1 ?: 0) / 60
                } else null
            } else {
                it.value1
            }
        } ?: ArrayList()

        val maxValue = sharedViewModel.getMaxValue(dataList, pageData?.contributorType)
        val yAxisRange = sharedViewModel.getYAxisRange(maxValue, pageData?.contributorType)
        val xAxisRange = getXAxisRange(pageData)
        val avgValue = sharedViewModel.getAvgValue(dataList, pageData?.contributorType)
        val showOverlay =
            if (pageData?.selectedPeriod == InternalSelectedPeriod.DAY) false else true

        binding.graphBar.setDataSet(
            dataList, yAxisRange, xAxisRange, yAxisRange.last().first, avgValue, -1, showOverlay,
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

    private fun getAvgValue(list: List<Int?>): Pair<Int, String>? {
        val filteredData = list.filterNotNull()
        if (filteredData.isEmpty()) {
            return null
        }

        val avg = list.filterNotNull().average().roundToInt()
        return Pair(avg, "$avg%")
    }

    private fun getXAxisRange(pageData: TrendsGraphData?): List<String> {
        return when (pageData?.selectedPeriod) {
            InternalSelectedPeriod.MONTH -> {
                arrayListOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
            }

            InternalSelectedPeriod.WEEK -> {
                val weekList = HashSet<Int>()
                val weekListReturn = ArrayList<String>()

                pageData.data?.forEach {
                    val date = LocalDate.parse(it.date)

                    val weekFields = WeekFields.of(Locale.getDefault())
                    val weekNumber = date.get(weekFields.weekOfWeekBasedYear())
                    weekList.add(weekNumber)
                }
                weekList.sorted().forEach {
                    weekListReturn.add("W$it")
                }
                weekListReturn
                //arrayListOf("W1", "W2", "W3", "W4", "W5", "W6")
            }

            else -> {
                arrayListOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            }
        }
    }

    private fun getMaxValue(list: List<Int?>): Int {
        return 100
    }


    fun getYAxisRange(maxValue: Int): List<Pair<Int, String>> {
        return arrayListOf(
            Pair(0, "0%"), Pair(25, "25%"), Pair(50, "50%"), Pair(75, "75%"), Pair(100, "100%")
        )
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}