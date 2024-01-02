package com.oreo.ui.sleep.scoredetails

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOSleepScoreDetailsBinding
import com.noisefit.oreo.util.graph.OLineChartUtils
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.clearDrawables
import com.noisefit_commans.common.setCompoundDrawable
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.FirebaseLunaAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MiscUtil
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Comparison
import com.oreo.data.model.OInternalPageResponseModal
import com.oreo.data.model.ResultData
import com.oreo.ui.custom.ScrollListener
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

private const val DAY_TYPE = "DAY_TYPE"
private const val ITEM_TYPE = "ITEM_TYPE"
private const val VIEW_TYPE = "VIEW_TYPE"
private const val DATE = "DATE"

@AndroidEntryPoint
class OSleepScoreDetailsFragment :
    BaseFragment<FragmentOSleepScoreDetailsBinding>(FragmentOSleepScoreDetailsBinding::inflate),
    ScrollListener {
    private val mViewModel: OSCDViewModel by viewModels()
    private val mSharedViewModel: SharedOSCDViewModel by activityViewModels()

    companion object {
        fun newInstance(dayType: String, itemType: String, viewType: String, date: String) =
            OSleepScoreDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(DAY_TYPE, dayType)
                    putString(ITEM_TYPE, itemType)
                    putString(VIEW_TYPE, viewType)
                    putString(DATE, date)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            mViewModel.itemType = it.getString(ITEM_TYPE).toString()
            mViewModel.dayType = it.getString(DAY_TYPE).toString()
            mViewModel.viewType = it.getString(VIEW_TYPE).toString()
            mViewModel.selectedDate = it.getString(DATE)
        }
        mViewModel.itemClickType = mSharedViewModel.itemClickType?.name ?: ""


        if (mViewModel.viewType?.lowercase() == "sleep")
            mViewModel.getInternalDetailsData()
        else if (mViewModel.viewType?.lowercase() == "activity")
            mViewModel.getActivityInternalDetailsData()
        else
            mViewModel.getReadinessInternalDetailsData()
        viewUpdate()
    }

    private fun viewUpdate() {
        when (mViewModel.dayType) {
            "Day" -> {
                when (mViewModel.itemClickType) {
                    ClickViewType.ACTIVITY_SCORE.name -> {
                        binding.lytDailyAvgComp.root.gone()
                        binding.lytTopGraphView.tvLabel2.gone()
                        binding.lytTopGraphView.lytLabelValue2.root.gone()
                    }

                    ClickViewType.GOAL_PROGRESS_DAY.name,
                    ClickViewType.TOTAL_BURN_DAY.name,
                    ClickViewType.STEP_DAY.name -> {
                        binding.lytDailyAvgComp.root.gone()
                        binding.lytTopGraphView.tvLabel2.visible()
                        binding.lytTopGraphView.lytLabelValue2.root.visible()
                    }

                    else -> {
                        binding.lytDailyAvgComp.root.gone()
                        binding.lytTopGraphView.tvLabel2.gone()
                        binding.lytTopGraphView.lytLabelValue2.root.gone()
                    }
                }
                mViewModel.sessionManager.logFirebaseEvent(
                    "${
                        mViewModel.itemClickType?.let {
                            MiscUtil.addUnderscore(
                                it
                            )
                        }
                    }_" + FirebaseLunaAppEvents.DAY_CLICK
                )

            }

            "Week" -> {
                when (mViewModel.itemClickType) {
                    ClickViewType.ACTIVITY_SCORE.name -> {
                        binding.lytDailyAvgComp.root.gone()
                        binding.lytTopGraphView.tvLabel2.gone()
                        binding.lytTopGraphView.lytLabelValue2.root.gone()
                    }

                    ClickViewType.GOAL_PROGRESS_WEEK.name,
                    ClickViewType.GOAL_PROGRESS_MONTH.name,
                    ClickViewType.TOTAL_BURN_DAY.name,
                    ClickViewType.TOTAL_BURN_WEEK.name,
                    ClickViewType.TOTAL_BURN_MONTH.name -> {
                        binding.lytDailyAvgComp.root.gone()
                        binding.lytTopGraphView.tvLabel2.visible()
                        binding.lytTopGraphView.lytLabelValue2.root.visible()
                    }

                    else -> {
                        binding.lytDailyAvgComp.root.gone()
                        binding.lytTopGraphView.tvLabel2.gone()
                        binding.lytTopGraphView.lytLabelValue2.root.gone()
                    }
                }
                mViewModel.sessionManager.logFirebaseEvent(
                    "${
                        mViewModel.itemClickType?.let {
                            MiscUtil.addUnderscore(
                                it
                            )
                        }
                    }_" + FirebaseLunaAppEvents.WEEK_CLICK
                )
            }

            else -> {
                when (mViewModel.itemClickType) {
                    ClickViewType.ACTIVITY_SCORE.name -> {
                        binding.lytDailyAvgComp.root.gone()
                        binding.lytTopGraphView.tvLabel2.gone()
                        binding.lytTopGraphView.lytLabelValue2.root.gone()
                    }

                    ClickViewType.GOAL_PROGRESS_WEEK.name,
                    ClickViewType.GOAL_PROGRESS_MONTH.name,
                    ClickViewType.TOTAL_BURN_DAY.name,
                    ClickViewType.TOTAL_BURN_WEEK.name,
                    ClickViewType.TOTAL_BURN_MONTH.name -> {
                        binding.lytDailyAvgComp.root.gone()
                        binding.lytTopGraphView.tvLabel2.visible()
                        binding.lytTopGraphView.lytLabelValue2.root.visible()
                    }

                    else -> {
                        binding.lytDailyAvgComp.root.gone()
                        binding.lytTopGraphView.tvLabel2.gone()
                        binding.lytTopGraphView.lytLabelValue2.root.gone()
                    }
                }
                mViewModel.sessionManager.logFirebaseEvent(
                    "${
                        mViewModel.itemClickType?.let {
                            MiscUtil.addUnderscore(
                                it
                            )
                        }
                    }_" + FirebaseLunaAppEvents.MONTH_CLICK
                )
            }
        }
    }

    override fun initListener() {
        binding.lytTopGraphView.rvTopGraph.setOnChartScrollChangedListener(this)
        binding.lytTopGraphView.rvTopBarGraph.setOnChartScrollChangedListener(this)
    }

    override fun subscribeObservers() {
//        mViewModel.trendDiff.observe(this) {
//            updateTrendPercent()
//        }

        mViewModel.internalDetailsData.observe(this) {
            if (it != null) {
                binding.lytTopGraphView.root.visible()
                binding.divider1.root.visible()
                binding.lytScoreOverview.root.visible()
                //todo when getting value null from backend we make visible
                if (it.trendData?.allTimeAvg != null) {
                    binding.lytAllTimeAvg.root.gone()
                    binding.view1.gone()
                } else {
                    binding.lytAllTimeAvg.root.gone()
                    binding.view1.gone()
                }
                updateUI(it)
            }
        }

        mViewModel.topLevelData.observe(this) {
            updateTopLabelView(it)
        }

        mViewModel.comparisonData.observe(this) {
            binding.lytDailyAvgComp.root.visible()
//            showComparisonGraph(it)
        }

        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        mViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar1.root.visible()
            } else {
                binding.progressBar1.root.gone()
            }
        }
    }


    private fun updateTopLabelView(it: ResultData?) {
        if (it != null) {
            if (isTrendValueUpdate()) {
                when (mViewModel.itemClickType) {
                    ViewItemClickType.STEPS.name -> {
                        binding.lytTopGraphView.lytLabelValue1.root.visible()
                        binding.lytTopGraphView.lytLabelValue11.root.gone()
                        binding.lytTopGraphView.lytLabelValue1.tvValue.text =
                            checkZeroData(it.data.roundToInt())
                        if (it.data.roundToInt() == 0) {
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.gone()
                        } else {
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.visible()
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.text = "steps"
                        }
                        setTopDateLabel(it.date, it.year)
                    }

                    ViewItemClickType.SLEEP_EFFICIENCY.name -> {
                        binding.lytTopGraphView.lytLabelValue1.root.visible()
                        binding.lytTopGraphView.lytLabelValue11.root.gone()
                        val dataPoint: String =
                            if (checkZeroData(it.data.roundToInt()).equals("-")) {
                                "-"
                            } else
                                checkZeroData(it.data.roundToInt())
                        binding.lytTopGraphView.lytLabelValue1.tvValue.text =
                            dataPoint
                        if (it.data.roundToInt() == 0) {
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.gone()
                        } else {
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.gone()
//                            binding.lytTopGraphView.lytLabelValue1.tvUnit.text = "%"
                        }
                        setTopDateLabel(it.date, it.year)
                    }

                    ViewItemClickType.RESPIRATORY_RATE.name -> {
                        binding.lytTopGraphView.lytLabelValue1.root.visible()
                        binding.lytTopGraphView.lytLabelValue11.root.gone()
                        binding.lytTopGraphView.lytLabelValue1.tvValue.text =
                            checkZeroData(it.data.roundToInt())
                        if (it.data.roundToInt() == 0) {
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.gone()
                        } else {
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.visible()
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.text = "/ min"
                        }
                        setTopDateLabel(it.date, it.year)
                    }

                    ViewItemClickType.RESTING_HR.name -> {
                        binding.lytTopGraphView.lytLabelValue1.root.visible()
                        binding.lytTopGraphView.lytLabelValue11.root.gone()
                        binding.lytTopGraphView.lytLabelValue1.tvValue.text =
                            checkZeroData(it.data.roundToInt())
                        if (it.data.roundToInt() == 0) {
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.gone()
                        } else {
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.visible()
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.text = "bpm"
                        }
                        setTopDateLabel(it.date, it.year)
                    }

                    ViewItemClickType.ACTIVE_CALORIES.name -> {
                        binding.lytTopGraphView.lytLabelValue1.root.visible()
                        binding.lytTopGraphView.lytLabelValue11.root.gone()
                        binding.lytTopGraphView.lytLabelValue1.tvValue.text =
                            checkZeroData(it.data.roundToInt())
                        if (it.data.roundToInt() == 0) {
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.gone()
                        } else {
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.visible()
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.text = "kcal"
                        }
                        setTopDateLabel(it.date, it.year)
                    }

                    ViewItemClickType.DISTANCE.name -> {
                        binding.lytTopGraphView.lytLabelValue1.root.visible()
                        binding.lytTopGraphView.lytLabelValue11.root.gone()
                        if (it.data.roundToInt() == 0) {
                            binding.lytTopGraphView.lytLabelValue1.tvValue.text = "-"
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.gone()
                        } else {
                            binding.lytTopGraphView.lytLabelValue1.tvValue.text =
                                DistanceUtil.convertMeterToKm(it.data.roundToInt())
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.visible()
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.text = "km"
                        }
                        setTopDateLabel(it.date, it.year)
                    }

                    ViewItemClickType.BODY_TEMPERATURE.name -> {
                        binding.lytTopGraphView.lytLabelValue1.root.visible()
                        binding.lytTopGraphView.lytLabelValue11.root.gone()

                        if (it.data == 0F) {
                            binding.lytTopGraphView.lytLabelValue1.tvValue.text = "-"
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.gone()
                        } else {
                            binding.lytTopGraphView.lytLabelValue1.tvValue.text = it.data.toString()
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.visible()
                            binding.lytTopGraphView.lytLabelValue1.tvUnit.text = "°F"
                        }
                        setTopDateLabel(it.date, it.year)
                    }

                    else -> {
                        binding.lytTopGraphView.lytLabelValue1.root.gone()
                        binding.lytTopGraphView.lytLabelValue11.root.visible()
                        val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                            it.data.roundToInt()
                        )
                        if (hour > 0) {
                            binding.lytTopGraphView.lytLabelValue11.tvHour.text = "$hour"
                            binding.lytTopGraphView.lytLabelValue11.tvHourUnit.text =
                                getString(R.string.text_hr_lower)
                            binding.lytTopGraphView.lytLabelValue11.tvHourUnit.visible()
                            if (minute > 0) {
                                binding.lytTopGraphView.lytLabelValue11.tvMinute.text = "$minute"
                                binding.lytTopGraphView.lytLabelValue11.tvMinuteUnit.text = "min"
                                binding.lytTopGraphView.lytLabelValue11.tvMinute.visible()
                                binding.lytTopGraphView.lytLabelValue11.tvMinuteUnit.visible()
                            } else {
                                binding.lytTopGraphView.lytLabelValue11.tvMinute.invisible()
                                binding.lytTopGraphView.lytLabelValue11.tvMinuteUnit.invisible()
                            }
                        } else {
                            if (minute > 0) {
                                binding.lytTopGraphView.lytLabelValue11.tvMinute.text = "$minute"
                                binding.lytTopGraphView.lytLabelValue11.tvMinuteUnit.text = "min"
                                binding.lytTopGraphView.lytLabelValue11.tvMinute.visible()
                                binding.lytTopGraphView.lytLabelValue11.tvMinuteUnit.visible()
                            } else {
                                binding.lytTopGraphView.lytLabelValue11.tvMinute.invisible()
                                binding.lytTopGraphView.lytLabelValue11.tvMinuteUnit.invisible()
                                binding.lytTopGraphView.lytLabelValue11.tvHour.text = "-"
                                binding.lytTopGraphView.lytLabelValue11.tvHourUnit.invisible()
                            }
                        }
                        setTopDateLabel(it.date, it.year)
                    }
                }
            } else {
                binding.lytTopGraphView.lytLabelValue1.root.visible()
                binding.lytTopGraphView.lytLabelValue11.root.gone()
                binding.lytTopGraphView.lytLabelValue1.tvValue.text =
                    checkZeroData(it.data.roundToInt())
                setTopDateLabel(it.date, it.year)
            }

        }
    }

    private fun checkZeroData(value: Int): String {
        val displayValue: String = if (value == 0) {
            "-"
        } else
            value.toString()
        return displayValue
    }

    private fun updateUI(it: OInternalPageResponseModal) {
        var trendTodayTitle = ""
        var trendYesterdayTitle = ""
        var trendScoreMsg = ""
        binding.lytAllTimeAvg.tvToday.text = getString(R.string.text_all_time_average)
        binding.lytScoreOverview.tvTitle.text = getTrendTitle()
        handleShowTrendCompareProgress(it)
        when (mViewModel.itemType) {
            ClickViewType.SLEEP.name -> {
                binding.lytAllTimeAvg.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )

                bindDataOnUi(it)

                when (mViewModel.dayType) {
                    "Day" -> {
                        trendTodayTitle = getString(R.string.text_today)
                        trendYesterdayTitle = getString(R.string.text_yesterday)

                        trendScoreMsg = if (mViewModel.isProgressEqual)
                            "same as yesterday"
                        else {
                            if (mViewModel.itemClickType == ViewItemClickType.TOTAL_SLEEP.name ||
                                mViewModel.itemClickType == ViewItemClickType.TIME_IN_BED.name ||
                                mViewModel.itemClickType == ViewItemClickType.ACTIVE_CALORIES.name ||
                                mViewModel.itemClickType == ViewItemClickType.STEPS.name
                            ) {
                                if (mViewModel.isTodayGreater)
                                    "more than yesterday"
                                else
                                    "less than yesterday"
                            } else
                                "from yesterday"
                        }
                    }

                    "Week" -> {
                        trendTodayTitle = getString(R.string.text_this_week)
                        trendYesterdayTitle = getString(R.string.text_last_week)

                        trendScoreMsg = if (mViewModel.isProgressEqual)
                            "same as last week"
                        else {
                            if (mViewModel.itemClickType == ViewItemClickType.TOTAL_SLEEP.name ||
                                mViewModel.itemClickType == ViewItemClickType.TIME_IN_BED.name ||
                                mViewModel.itemClickType == ViewItemClickType.ACTIVE_CALORIES.name ||
                                mViewModel.itemClickType == ViewItemClickType.STEPS.name
                            ) {
                                if (mViewModel.isTodayGreater)
                                    "more than last week"
                                else
                                    "less than last week"
                            } else
                                "from last week on average"
                        }
                    }

                    else -> {
                        trendTodayTitle = getString(R.string.text_this_month)
                        trendYesterdayTitle = getString(R.string.text_last_month)

                        trendScoreMsg = if (mViewModel.isProgressEqual)
                            "same as last month"
                        else {
                            if (mViewModel.itemClickType == ViewItemClickType.TOTAL_SLEEP.name ||
                                mViewModel.itemClickType == ViewItemClickType.TIME_IN_BED.name ||
                                mViewModel.itemClickType == ViewItemClickType.ACTIVE_CALORIES.name ||
                                mViewModel.itemClickType == ViewItemClickType.STEPS.name
                            ) {
                                if (mViewModel.isTodayGreater)
                                    "more than last month"
                                else
                                    "less than last month"
                            } else
                                "from last month on average"
                        }
                    }
                }
            }

            ClickViewType.ACTIVITY_SCORE.name,
            ClickViewType.ACTIVITY.name,
            ClickViewType.GOAL_PROGRESS_DAY.name,
            ClickViewType.GOAL_PROGRESS_WEEK.name,
            ClickViewType.TOTAL_BURN_DAY.name,
            ClickViewType.TOTAL_BURN_WEEK.name,
            ClickViewType.STEP_DAY.name,
            ClickViewType.TOTAL_BURN_MONTH.name -> {
                binding.lytAllTimeAvg.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                bindDataOnUi(it)

                when (mViewModel.dayType) {
                    "Day" -> {
                        trendTodayTitle = getString(R.string.text_today)
                        trendYesterdayTitle = getString(R.string.text_yesterday)

                        trendScoreMsg = if (mViewModel.isProgressEqual)
                            "same as yesterday"
                        else {
                            if (mViewModel.itemClickType == ViewItemClickType.TOTAL_SLEEP.name ||
                                mViewModel.itemClickType == ViewItemClickType.TIME_IN_BED.name ||
                                mViewModel.itemClickType == ViewItemClickType.ACTIVE_CALORIES.name ||
                                mViewModel.itemClickType == ViewItemClickType.STEPS.name
                            ) {
                                if (mViewModel.isTodayGreater)
                                    "more than yesterday"
                                else
                                    "less than yesterday"
                            } else
                                "from yesterday"
                        }
                    }

                    "Week" -> {
                        trendTodayTitle = getString(R.string.text_this_week)
                        trendYesterdayTitle = getString(R.string.text_last_week)

                        trendScoreMsg = if (mViewModel.isProgressEqual)
                            "same as last week"
                        else {
                            if (mViewModel.itemClickType == ViewItemClickType.TOTAL_SLEEP.name ||
                                mViewModel.itemClickType == ViewItemClickType.TIME_IN_BED.name ||
                                mViewModel.itemClickType == ViewItemClickType.ACTIVE_CALORIES.name ||
                                mViewModel.itemClickType == ViewItemClickType.STEPS.name
                            ) {
                                if (mViewModel.isTodayGreater)
                                    "more than last week"
                                else
                                    "less than last week"
                            } else
                                "from last week on average"
                        }
                    }

                    else -> {
                        trendTodayTitle = getString(R.string.text_this_month)
                        trendYesterdayTitle = getString(R.string.text_last_month)

                        trendScoreMsg = if (mViewModel.isProgressEqual)
                            "same as last month"
                        else {
                            if (mViewModel.itemClickType == ViewItemClickType.TOTAL_SLEEP.name ||
                                mViewModel.itemClickType == ViewItemClickType.TIME_IN_BED.name ||
                                mViewModel.itemClickType == ViewItemClickType.ACTIVE_CALORIES.name ||
                                mViewModel.itemClickType == ViewItemClickType.STEPS.name
                            ) {
                                if (mViewModel.isTodayGreater)
                                    "more than last month"
                                else
                                    "less than last month"
                            } else
                                "from last month on average"
                        }
                    }
                }
            }

            ClickViewType.READINESS.name -> {

                binding.lytAllTimeAvg.pbSteps.setIndicatorColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                bindDataOnUi(it)
                when (mViewModel.dayType) {

                    "Day" -> {
                        trendTodayTitle = getString(R.string.text_today)
                        trendYesterdayTitle = getString(R.string.text_yesterday)

                        trendScoreMsg = if (mViewModel.isProgressEqual)
                            "same as yesterday"
                        else {
                            if (mViewModel.itemClickType == ViewItemClickType.TOTAL_SLEEP.name ||
                                mViewModel.itemClickType == ViewItemClickType.TIME_IN_BED.name ||
                                mViewModel.itemClickType == ViewItemClickType.ACTIVE_CALORIES.name ||
                                mViewModel.itemClickType == ViewItemClickType.STEPS.name
                            ) {
                                if (mViewModel.isTodayGreater)
                                    "more than yesterday"
                                else
                                    "less than yesterday"
                            } else
                                "from yesterday"
                        }
                    }

                    "Week" -> {
                        trendTodayTitle = getString(R.string.text_this_week)
                        trendYesterdayTitle = getString(R.string.text_last_week)

                        trendScoreMsg = if (mViewModel.isProgressEqual)
                            "same as last week"
                        else {
                            if (mViewModel.itemClickType == ViewItemClickType.TOTAL_SLEEP.name ||
                                mViewModel.itemClickType == ViewItemClickType.TIME_IN_BED.name ||
                                mViewModel.itemClickType == ViewItemClickType.ACTIVE_CALORIES.name ||
                                mViewModel.itemClickType == ViewItemClickType.STEPS.name
                            ) {
                                if (mViewModel.isTodayGreater)
                                    "more than last week"
                                else
                                    "less than last week"
                            } else
                                "from last week on average"
                        }
                    }

                    else -> {
                        trendTodayTitle = getString(R.string.text_this_month)
                        trendYesterdayTitle = getString(R.string.text_last_month)

                        trendScoreMsg = if (mViewModel.isProgressEqual)
                            "same as last month"
                        else {
                            if (mViewModel.itemClickType == ViewItemClickType.TOTAL_SLEEP.name ||
                                mViewModel.itemClickType == ViewItemClickType.TIME_IN_BED.name ||
                                mViewModel.itemClickType == ViewItemClickType.ACTIVE_CALORIES.name ||
                                mViewModel.itemClickType == ViewItemClickType.STEPS.name
                            ) {
                                if (mViewModel.isTodayGreater)
                                    "more than last month"
                                else
                                    "less than last month"
                            } else
                                "from last month on average"
                        }
                    }
                }
            }
        }

        binding.lytScoreOverview.lytToday.tvToday.text = trendTodayTitle
        binding.lytScoreOverview.lytYesterday.tvToday.text = trendYesterdayTitle
        binding.lytScoreOverview.tvScoreMsg.text = trendScoreMsg


    }

    private fun showComparisonGraph(comparison: Comparison) {

        binding.lytDailyAvgComp.lytToday.tvValue.text = comparison.today.toString()
        binding.lytDailyAvgComp.lytAverage.tvValue.text = comparison.average.toString()
        binding.lytDailyAvgComp.tvMsg.text = getString(R.string.text_comparison_msg)
        OLineChartUtils.setComparisonChart(
            binding.lytDailyAvgComp.scoreChart,
            mViewModel.getGraphEntriesData(
                ContextCompat.getColor(requireContext(), R.color.activity_selected_bar_color),
                ContextCompat.getColor(requireContext(), R.color.lightest_gray)
            )
        )
    }


    private fun handleShowTrendCompareProgress(it: OInternalPageResponseModal) {
        val trendData = it.trendData
        var todayProgress: Long
        var yesterdayProgress: Long
        if (trendData != null) {
            if (trendData.today?.value == null || trendData.today.value.toInt() == 0 ||
                trendData.yesterday?.value == null || trendData.yesterday.value.toInt() == 0
            ) {
                binding.lytScoreOverview.tvTrendProg.gone()
                binding.lytScoreOverview.tvScoreMsg.gone()
            } else {
                if (isTrendValueUpdate()) {
                    if (mViewModel.itemClickType == ViewItemClickType.STEPS.name
                    ) {
                        var difference = 0
                        todayProgress =
                            trendData.today.value.toLong()
                        yesterdayProgress =
                            trendData.yesterday.value.toLong()
                        if (todayProgress > yesterdayProgress) {
                            binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_up)
                            binding.lytScoreOverview.tvTrendProg.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.steps_arc
                                )
                            )
                            binding.lytScoreOverview.tvTrendProg.visible()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = false
                            difference = todayProgress.toInt() - yesterdayProgress.toInt()
                            mViewModel.isTodayGreater = true
                        } else if (yesterdayProgress > todayProgress) {
                            binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_down)
                            binding.lytScoreOverview.tvTrendProg.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.color_error
                                )
                            )
                            binding.lytScoreOverview.tvTrendProg.visible()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = false
                            difference = yesterdayProgress.toInt() - todayProgress.toInt()
                            mViewModel.isTodayGreater = false
                        } else {
                            binding.lytScoreOverview.tvTrendProg.gone()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = true
                        }
//                        val compPro = "${mViewModel.trendDifferenceProgress} steps"
                        val compPro = "$difference steps"
                        binding.lytScoreOverview.tvTrendProg.text = compPro
                    } else if (mViewModel.itemClickType == ViewItemClickType.ACTIVE_CALORIES.name
                    ) {
                        var difference = 0
                        todayProgress =
                            trendData.today.value.toLong()
                        yesterdayProgress =
                            trendData.yesterday.value.toLong()
                        if (todayProgress > yesterdayProgress) {
                            binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_up)
                            binding.lytScoreOverview.tvTrendProg.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.steps_arc
                                )
                            )
                            binding.lytScoreOverview.tvTrendProg.visible()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = false
                            difference = todayProgress.toInt() - yesterdayProgress.toInt()
                            mViewModel.isTodayGreater = true
                        } else if (yesterdayProgress > todayProgress) {
                            binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_down)
                            binding.lytScoreOverview.tvTrendProg.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.color_error
                                )
                            )
                            binding.lytScoreOverview.tvTrendProg.visible()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = false
                            difference = yesterdayProgress.toInt() - todayProgress.toInt()
                            mViewModel.isTodayGreater = false
                        } else {
                            binding.lytScoreOverview.tvTrendProg.gone()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = true
                        }
//                        val compPro = "${mViewModel.trendDifferenceProgress} steps"
                        val compPro = "$difference kcal"
                        binding.lytScoreOverview.tvTrendProg.text = compPro
                    } else if (
                        mViewModel.itemClickType == ViewItemClickType.DISTANCE.name
                    ) {
                        var difference = 0f
                        todayProgress =
                            trendData.today.value.toLong()
                        yesterdayProgress =
                            trendData.yesterday.value.toLong()
                        if (todayProgress > yesterdayProgress) {
                            binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_up)
                            binding.lytScoreOverview.tvTrendProg.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.steps_arc
                                )
                            )
                            binding.lytScoreOverview.tvTrendProg.visible()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = false
                            difference = DistanceUtil.convertMeterToKm(todayProgress.toInt())
                                .toFloat() - DistanceUtil.convertMeterToKm(yesterdayProgress.toInt())
                                .toFloat()
                            mViewModel.isTodayGreater = true
                        } else if (yesterdayProgress > todayProgress) {
                            binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_down)
                            binding.lytScoreOverview.tvTrendProg.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.color_error
                                )
                            )
                            binding.lytScoreOverview.tvTrendProg.visible()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = false
                            difference = DistanceUtil.convertMeterToKm(yesterdayProgress.toInt())
                                .toFloat() - DistanceUtil.convertMeterToKm(todayProgress.toInt())
                                .toFloat()
                            mViewModel.isTodayGreater = false
                        } else {
                            binding.lytScoreOverview.tvTrendProg.gone()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = true
                        }
                        val compPro = "${String.format("%.1f", difference)} km"
                        binding.lytScoreOverview.tvTrendProg.text = compPro
                    } else if (
                        mViewModel.itemClickType == ViewItemClickType.BODY_TEMPERATURE.name
                    ) {
                        var difference = 0f
                        todayProgress =
                            trendData.today.value.toLong()
                        yesterdayProgress =
                            trendData.yesterday.value.toLong()
                        if (todayProgress > yesterdayProgress) {
                            binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_up)
                            binding.lytScoreOverview.tvTrendProg.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.steps_arc
                                )
                            )
                            binding.lytScoreOverview.tvTrendProg.visible()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = false
                            difference = trendData.today.value - trendData.yesterday.value
                            mViewModel.isTodayGreater = true
                        } else if (yesterdayProgress > todayProgress) {
                            binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_down)
                            binding.lytScoreOverview.tvTrendProg.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.color_error
                                )
                            )
                            binding.lytScoreOverview.tvTrendProg.visible()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = false
                            difference = trendData.yesterday.value - trendData.today.value
                            mViewModel.isTodayGreater = false
                        } else {
                            binding.lytScoreOverview.tvTrendProg.gone()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = true
                        }
                        val compPro = "${String.format("%.1f", difference)} °F"
                        binding.lytScoreOverview.tvTrendProg.text = compPro
                    } else if (mViewModel.itemClickType == ViewItemClickType.RESPIRATORY_RATE.name) {
                        tryCatch {
                            binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_up)
                            todayProgress = trendData.today.value.toLong()
                            yesterdayProgress = trendData.yesterday.value.toLong()
                            if (todayProgress > yesterdayProgress) {
                                val trendDifProgress = todayProgress - yesterdayProgress

                                binding.lytScoreOverview.tvTrendProg.text =
                                    "$trendDifProgress / min"
                                binding.lytScoreOverview.tvTrendProg.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.steps_arc
                                    )
                                )
                                binding.lytScoreOverview.tvTrendProg.visible()
                                binding.lytScoreOverview.tvScoreMsg.visible()
                                mViewModel.isTodayGreater = true
                                mViewModel.isProgressEqual = false
                            } else if (yesterdayProgress > todayProgress) {
                                val trendDifProgress = yesterdayProgress - todayProgress

                                binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_down)
                                binding.lytScoreOverview.tvTrendProg.text =
                                    "$trendDifProgress / min"
                                binding.lytScoreOverview.tvTrendProg.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.errorRed
                                    )
                                )
                                binding.lytScoreOverview.tvTrendProg.visible()
                                binding.lytScoreOverview.tvScoreMsg.visible()
                                mViewModel.isTodayGreater = false
                                mViewModel.isProgressEqual = false

                            } else {
                                binding.lytScoreOverview.tvTrendProg.gone()
                                binding.lytScoreOverview.tvScoreMsg.visible()
                                mViewModel.isProgressEqual = true
                            }
                        }
                    } else if (
                        mViewModel.itemClickType == ViewItemClickType.RESTING_HR.name
                    ) {
                        var difference = 0f
                        todayProgress =
                            trendData.today.value.toLong()
                        yesterdayProgress =
                            trendData.yesterday.value.toLong()
                        if (todayProgress > yesterdayProgress) {
                            binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_up_red)
                            binding.lytScoreOverview.tvTrendProg.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.color_error
                                )
                            )
                            binding.lytScoreOverview.tvTrendProg.visible()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = false
                            difference = trendData.today.value - trendData.yesterday.value
                            mViewModel.isTodayGreater = true
                        } else if (yesterdayProgress > todayProgress) {
                            binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_down_green)
                            binding.lytScoreOverview.tvTrendProg.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.steps_arc
                                )
                            )
                            binding.lytScoreOverview.tvTrendProg.visible()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = false
                            difference = trendData.yesterday.value - trendData.today.value
                            mViewModel.isTodayGreater = false
                        } else {
                            binding.lytScoreOverview.tvTrendProg.gone()
                            binding.lytScoreOverview.tvScoreMsg.visible()
                            mViewModel.isProgressEqual = true
                        }
                        val compPro = "${difference.toInt()} bpm"
                        binding.lytScoreOverview.tvTrendProg.text = compPro
                    } else {
                        tryCatch {
                            binding.lytScoreOverview.tvTrendProg.clearDrawables()
                            val tempProgress: Long
                            todayProgress = trendData.today.value.toLong()
                            yesterdayProgress = trendData.yesterday.value.toLong()
                            if (todayProgress > yesterdayProgress) {
                                tempProgress = todayProgress - yesterdayProgress
                                val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                                    tempProgress.toInt()
                                )
                                val trendDifProgress = if (hour > 0) "$hour hr $minute min"
                                else
                                    "$minute min"
                                binding.lytScoreOverview.tvTrendProg.text = trendDifProgress
                                binding.lytScoreOverview.tvTrendProg.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.steps_arc
                                    )
                                )
                                if (minute == 0) {
                                    mViewModel.isProgressEqual = true
                                    binding.lytScoreOverview.tvTrendProg.gone()
                                } else {
                                    binding.lytScoreOverview.tvTrendProg.visible()
                                    mViewModel.isProgressEqual = false
                                }
                                binding.lytScoreOverview.tvScoreMsg.visible()
                                mViewModel.isTodayGreater = true
                            } else if (yesterdayProgress > todayProgress) {
                                tempProgress = yesterdayProgress - todayProgress
                                val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                                    tempProgress.toInt()
                                )
                                val trendDifProgress = if (hour > 0) "$hour hr $minute min"
                                else
                                    "$minute min"

                                if (minute == 0) {
                                    mViewModel.isProgressEqual = true
                                    binding.lytScoreOverview.tvTrendProg.gone()
                                } else {
                                    binding.lytScoreOverview.tvTrendProg.visible()
                                    mViewModel.isProgressEqual = false
                                }
                                binding.lytScoreOverview.tvTrendProg.text = trendDifProgress
                                binding.lytScoreOverview.tvTrendProg.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.errorRed
                                    )
                                )
                                binding.lytScoreOverview.tvScoreMsg.visible()
                                mViewModel.isTodayGreater = false

                            } else {
                                val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                                    todayProgress.toInt()
                                )
                                val trendDifProgress = if (hour > 0) "$hour hr $minute min"
                                else
                                    "$minute min"
                                binding.lytScoreOverview.tvTrendProg.gone()
                                binding.lytScoreOverview.tvScoreMsg.visible()
                                mViewModel.isProgressEqual = true
                                binding.lytScoreOverview.tvTrendProg.text = trendDifProgress
                            }
                        }
                    }

                } else {
                    todayProgress =
                        trendData.today.value.toLong()
                    yesterdayProgress =
                        trendData.yesterday.value.toLong()
                    if (todayProgress > yesterdayProgress) {
                        binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_up)
                        binding.lytScoreOverview.tvTrendProg.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.steps_arc
                            )
                        )
                        binding.lytScoreOverview.tvTrendProg.visible()
                        binding.lytScoreOverview.tvScoreMsg.visible()
                        mViewModel.isProgressEqual = false
                        mViewModel.isTodayGreater = true
                    } else if (yesterdayProgress > todayProgress) {
                        binding.lytScoreOverview.tvTrendProg.setCompoundDrawable(R.drawable.ic_trend_down)
                        binding.lytScoreOverview.tvTrendProg.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.color_error
                            )
                        )
                        binding.lytScoreOverview.tvTrendProg.visible()
                        binding.lytScoreOverview.tvScoreMsg.visible()
                        mViewModel.isProgressEqual = false
                        mViewModel.isTodayGreater = false
                    } else {
                        binding.lytScoreOverview.tvTrendProg.gone()
                        binding.lytScoreOverview.tvScoreMsg.visible()
                        mViewModel.isProgressEqual = true
                    }

                    val calPercent =
                        (((yesterdayProgress.toFloat() - todayProgress.toFloat()) / yesterdayProgress.toFloat()) * 100).roundToInt()
                    val compPro = "${calPercent.toString().replace("-", "")} %"
                    binding.lytScoreOverview.tvTrendProg.text = compPro
//                    updateTrendPercent()
                }

            }
        }
    }

//    private fun updateTrendPercent() {
//        if (!isTrendValueUpdate())
//            binding.lytScoreOverview.tvTrendProg.text = "${mViewModel.trendDiff.value} %"
//    }

    private fun setTopDateLabel(data: String, year: String? = null) {
        val dateRangeValue: String = when (mViewModel.dayType?.lowercase()) {
            "day" -> {
                DateFormats.getConvertToDateFormat(
                    data,
                    DateFormats.dateFormat3,
                    DateFormats.dateTimeFormatWithWeekWithoutYearShort
                ) ?: ""
            }

            "week" -> {
                val yearVal = year ?: (mViewModel.selectedDate?.substring(0, 4) ?: "")
                LOGS.d("sdfsdfsdf ${mViewModel.selectedDate} $yearVal")
                "Avg from ${DateFormats.getStartAndEndWeek(data.toInt(), yearVal.toInt())}"
            }

            else -> {
                val yearVal = year ?: (mViewModel.selectedDate?.substring(0, 4) ?: "")
                "Avg in ${DateFormats.getCompleteMonthName(data.toInt() - 1)} $yearVal"
            }
        }

        binding.lytTopGraphView.tvLabel1.text = dateRangeValue
    }

    private fun bindDataOnUi(it: OInternalPageResponseModal) {

        //today data
        val todayTrendValue: String
        var todayTrendProg: Int
        if (it.trendData?.today?.value == null || it.trendData.today.value.toInt() == 0) {
            todayTrendValue = "No data"
            todayTrendProg = 0
        } else {
            todayTrendValue = it.trendData.today.value.roundToInt().toString()
            todayTrendProg = it.trendData.today.value.roundToInt()
        }
        if (isTrendValueUpdate()) {
            if (todayTrendValue != "No data") {
                when (mViewModel.itemClickType) {
                    ViewItemClickType.RESTING_HR.name -> {
                        binding.lytScoreOverview.lytToday.tvScore.text = "${todayTrendValue} bpm"
                    }

                    ViewItemClickType.ACTIVE_CALORIES.name -> {
                        binding.lytScoreOverview.lytToday.tvScore.text = "${todayTrendValue} kcal"
                    }

                    ViewItemClickType.SLEEP_EFFICIENCY.name -> {
                        binding.lytScoreOverview.lytToday.tvScore.text = "${todayTrendValue}%"
                    }

                    ViewItemClickType.RESPIRATORY_RATE.name -> {
                        binding.lytScoreOverview.lytToday.tvScore.text = "${todayTrendValue} / min"
                    }

                    ViewItemClickType.STEPS.name -> {
                        binding.lytScoreOverview.lytToday.tvScore.text = "${todayTrendValue} steps"
                    }

                    ViewItemClickType.DISTANCE.name -> {
                        binding.lytScoreOverview.lytToday.tvScore.text =
                            "${DistanceUtil.convertMeterToKm(todayTrendValue.toInt())} km"
                    }

                    ViewItemClickType.BODY_TEMPERATURE.name -> {
                        binding.lytScoreOverview.lytToday.tvScore.text =
                            "${it.trendData?.today?.value.toString()} °F"
                    }

                    else -> {
                        val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                            todayTrendValue.toFloat().roundToInt()
                        )
                        binding.lytScoreOverview.lytToday.tvScore.text = "$hour hr $minute min"
                    }
                }
            } else {
                binding.lytScoreOverview.lytToday.tvScore.text = todayTrendValue
            }
        } else
            binding.lytScoreOverview.lytToday.tvScore.text = todayTrendValue

        //yesterday data
        val yesterdayTrendValue: String
        var yesterdayTrendProg: Int
        if (it.trendData?.yesterday?.value == null || it.trendData.yesterday.value.toInt() == 0) {
            yesterdayTrendValue = "No data"
            yesterdayTrendProg = 0
        } else {
            yesterdayTrendValue = it.trendData.yesterday.value.roundToInt().toString()
            yesterdayTrendProg = it.trendData.yesterday.value.roundToInt()
        }

        //allTimeAvg
        val allTimeTrendValue: String =
            if (it.trendData?.allTimeAvg == null || it.trendData.allTimeAvg.toInt() == 0) {
                "No data"
            } else {
                it.trendData.allTimeAvg.toFloat().roundToInt().toString()
            }

//        var allTimeTrendProg: Int =
//            if (it.trendData?.allTimeAvg == null || it.trendData.allTimeAvg.toInt() == 0) {
//                0
//            } else
//                it.trendData.allTimeAvg.toFloat().roundToInt()


        if (isTrendValueUpdate()
        ) {
            if (yesterdayTrendValue != "No data") {
                when (mViewModel.itemClickType) {
                    ViewItemClickType.RESTING_HR.name -> {
                        binding.lytScoreOverview.lytYesterday.tvScore.text =
                            "${yesterdayTrendValue} bpm"
                    }

                    ViewItemClickType.ACTIVE_CALORIES.name -> {
                        binding.lytScoreOverview.lytYesterday.tvScore.text =
                            "$yesterdayTrendValue kcal"
                    }

                    ViewItemClickType.SLEEP_EFFICIENCY.name -> {
                        binding.lytScoreOverview.lytYesterday.tvScore.text =
                            "$yesterdayTrendValue%"
                    }

                    ViewItemClickType.RESPIRATORY_RATE.name -> {
                        binding.lytScoreOverview.lytYesterday.tvScore.text =
                            "$yesterdayTrendValue / min"
                    }

                    ViewItemClickType.STEPS.name -> {
                        binding.lytScoreOverview.lytYesterday.tvScore.text =
                            "$yesterdayTrendValue steps"
                    }

                    ViewItemClickType.DISTANCE.name -> {
                        binding.lytScoreOverview.lytYesterday.tvScore.text =
                            "${DistanceUtil.convertMeterToKm(yesterdayTrendValue.toInt())} km"
                    }

                    ViewItemClickType.BODY_TEMPERATURE.name -> {
                        binding.lytScoreOverview.lytYesterday.tvScore.text =
                            "${it.trendData?.yesterday?.value.toString()} °F"
                    }

                    else -> {
                        val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                            yesterdayTrendValue.toFloat().roundToInt()
                        )
                        binding.lytScoreOverview.lytYesterday.tvScore.text = "$hour hr $minute min"
                    }
                }
            } else
                binding.lytScoreOverview.lytYesterday.tvScore.text = yesterdayTrendValue
        } else {
            binding.lytScoreOverview.lytYesterday.tvScore.text = yesterdayTrendValue
        }

        /*if (isTrendValueUpdate()) {
            if (allTimeTrendValue != "No data") {
                when (mViewModel.itemClickType) {
                    ViewItemClickType.RESTING_HR.name -> {
                        binding.lytAllTimeAvg.tvScore.text = "${allTimeTrendValue} bpm"
                    }

                    ViewItemClickType.ACTIVE_CALORIES.name -> {
                        binding.lytAllTimeAvg.tvScore.text = "$allTimeTrendValue kcal"
                    }

                    ViewItemClickType.RESPIRATORY_RATE.name -> {
                        binding.lytAllTimeAvg.tvScore.text = "$allTimeTrendValue / min"
                    }

                    ViewItemClickType.SLEEP_EFFICIENCY.name -> {
                        binding.lytAllTimeAvg.tvScore.text = "$allTimeTrendValue%"
                    }

                    ViewItemClickType.STEPS.name -> {
                        binding.lytAllTimeAvg.tvScore.text = "$allTimeTrendValue steps"
                    }

                    ViewItemClickType.DISTANCE.name -> {
                        binding.lytAllTimeAvg.tvScore.text =
                            "${DistanceUtil.convertMeterToKm(allTimeTrendValue.toInt())} km"
                    }

                    ViewItemClickType.BODY_TEMPERATURE.name -> {
                        binding.lytAllTimeAvg.tvScore.text =
                            "${it.trendData?.allTimeAvg.toString()} °F"
                    }

                    else -> {
                        val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                            allTimeTrendValue.toInt()
                        )
                        binding.lytAllTimeAvg.tvScore.text = "$hour hr $minute min"
                    }
                }
            } else
                binding.lytAllTimeAvg.tvScore.text = allTimeTrendValue

        } else {
            binding.lytAllTimeAvg.tvScore.text = allTimeTrendValue
        }*/


        //show top graph
        val topGraphData = mViewModel.getPrefixAndSuffixList(
            it.result as ArrayList<ResultData>,
            mViewModel.dayType
        )
        if (getGraphType() == 0) {
            binding.lytTopGraphView.rvTopBarGraph.visible()
            binding.lytTopGraphView.rvTopGraph.gone()
            binding.lytTopGraphView.rvTopBarGraph.updateDataWithMax(
                topGraphData.first.first,
                topGraphData.third,
                topGraphData.second,
                topGraphData.first.second,
                barGraphScoreColor().first,
                barGraphScoreColor().second

            )
        } else {
            binding.lytTopGraphView.rvTopBarGraph.gone()
            binding.lytTopGraphView.rvTopGraph.visible()
            binding.lytTopGraphView.rvTopGraph.updateDataWithMax(
                topGraphData.first.first,
                topGraphData.third,
                topGraphData.second,
                topGraphData.first.second,
                lineGraphScoreColor().first,
                lineGraphScoreColor().second,
                lineGraphScoreColor().third

            )
        }
        mViewModel.topDateLastScrollPosition = it.result.size - 1

        if (todayTrendProg > yesterdayTrendProg) {
            binding.lytScoreOverview.lytToday.pbSteps.progress = 100
            updateProgressColor(0)
            val showYesPer = yesterdayTrendProg.toFloat().times(100).div(todayTrendProg).toInt()
//            val showAllPer = allTimeTrendProg.toFloat().times(100).div(todayTrendProg).toInt()
            mViewModel.setTrendData(100 - showYesPer)
            binding.lytScoreOverview.lytYesterday.pbSteps.progress = showYesPer
//            binding.lytAllTimeAvg.pbSteps.progress = showAllPer

        } else if (yesterdayTrendProg > todayTrendProg /*&& yesterdayTrendProg > allTimeTrendProg*/) {
            binding.lytScoreOverview.lytYesterday.pbSteps.progress = 100
            updateProgressColor(1)
            val showTodayPer = todayTrendProg.toFloat().times(100).div(yesterdayTrendProg).toInt()
//            val showAllPer = allTimeTrendProg.toFloat().times(100).div(yesterdayTrendProg).toInt()
            mViewModel.setTrendData(100 - showTodayPer)
            binding.lytScoreOverview.lytToday.pbSteps.progress = showTodayPer
//            binding.lytAllTimeAvg.pbSteps.progress = showAllPer
        }
        /*else if (allTimeTrendProg > todayTrendProg && allTimeTrendProg > yesterdayTrendProg) {
            binding.lytAllTimeAvg.pbSteps.progress = 100
            val showTodayPer = todayTrendProg.toFloat().times(100).div(allTimeTrendProg).toInt()
            val showYesPer = yesterdayTrendProg.toFloat().times(100).div(allTimeTrendProg).toInt()

            if (todayTrendProg > yesterdayTrendProg) {
                val yPercent = yesterdayTrendProg.toFloat().times(100).div(todayTrendProg).toInt()
                mViewModel.setTrendData(100 - yPercent)
                mViewModel.isTodayGreater = true
            } else {
                val tPercent =
                    todayTrendProg.toFloat().times(100).div(yesterdayTrendProg).toInt()
                mViewModel.setTrendData(100 - tPercent)
                mViewModel.isTodayGreater = false
            }
            binding.lytScoreOverview.lytYesterday.pbSteps.progress = showYesPer
            binding.lytScoreOverview.lytToday.pbSteps.progress = showTodayPer
            updateProgressColor(2)
        }*/
        else {
            if (todayTrendProg > 0) {
                binding.lytScoreOverview.lytToday.pbSteps.progress = 100
                binding.lytScoreOverview.lytYesterday.pbSteps.progress =
                    100
            }
            /*if (todayTrendProg == allTimeTrendProg) {
                updateProgressColor(3)
                if (todayTrendProg == 0) {
                    binding.lytScoreOverview.lytToday.pbSteps.progress = 1
                    binding.lytScoreOverview.lytYesterday.pbSteps.progress =
                        1
                    binding.lytAllTimeAvg.pbSteps.progress =
                        1
                } else {
                    binding.lytScoreOverview.lytToday.pbSteps.progress = 100
                    binding.lytScoreOverview.lytYesterday.pbSteps.progress =
                        100
                    binding.lytAllTimeAvg.pbSteps.progress =
                        100
                }
            }
            else {
                if (todayTrendProg > 0) {
                    mViewModel.isProgressEqual = true
                    updateProgressColor(3)
                    binding.lytScoreOverview.lytToday.pbSteps.progress = 100
                    binding.lytScoreOverview.lytYesterday.pbSteps.progress =
                        100
                    binding.lytAllTimeAvg.pbSteps.progress =
                        allTimeTrendProg.toFloat().times(100).div(todayTrendProg).toInt()
                } else {
                    updateProgressColor(3)
                    binding.lytScoreOverview.lytToday.pbSteps.progress = 0
                    binding.lytScoreOverview.lytYesterday.pbSteps.progress =
                        0
                    binding.lytAllTimeAvg.pbSteps.progress =
                        allTimeTrendProg.toFloat().times(100).div(todayTrendProg).toInt()

                }
            }*/
        }
        /*else if (yesterdayTrendProg == allTimeTrendProg) {
                updateProgressColor(3)
                binding.lytScoreOverview.lytToday.pbSteps.progress =
                    todayTrendProg.toFloat().times(100).div(yesterdayTrendProg).toInt()
                binding.lytScoreOverview.lytYesterday.pbSteps.progress =
                    100
                binding.lytAllTimeAvg.pbSteps.progress = 100

            } else if (todayTrendProg == allTimeTrendProg) {
                updateProgressColor(3)
                binding.lytScoreOverview.lytToday.pbSteps.progress = 100
                binding.lytScoreOverview.lytYesterday.pbSteps.progress =
                    yesterdayTrendProg.toFloat().times(100).div(todayTrendProg).toInt()
                binding.lytAllTimeAvg.pbSteps.progress =
                    100
            }*/


    }


    private fun isTrendValueUpdate(): Boolean {
        return mViewModel.itemClickType == ViewItemClickType.TOTAL_SLEEP.name ||
                mViewModel.itemClickType == ViewItemClickType.TIME_IN_BED.name ||
                mViewModel.itemClickType == ViewItemClickType.STEPS.name ||
                mViewModel.itemClickType == ViewItemClickType.SLEEP_EFFICIENCY.name ||
                mViewModel.itemClickType == ViewItemClickType.RESPIRATORY_RATE.name ||
                mViewModel.itemClickType == ViewItemClickType.DISTANCE.name ||
                mViewModel.itemClickType == ViewItemClickType.ACTIVE_CALORIES.name ||
                mViewModel.itemClickType == ViewItemClickType.BODY_TEMPERATURE.name ||
                mViewModel.itemClickType == ViewItemClickType.RESTING_HR.name
    }

    /**
     * 0-> Bar
     * 1-> Line
     */
    private fun getGraphType(): Int {
        return if (mViewModel.itemClickType == ViewItemClickType.SLEEP_SCORE.name ||
            mViewModel.itemClickType == ViewItemClickType.ACTIVITY_SCORE.name ||
            mViewModel.itemClickType == ViewItemClickType.READINESS_SCORE.name ||
            mViewModel.itemClickType == ViewItemClickType.RESPIRATORY_RATE.name ||
            mViewModel.itemClickType == ViewItemClickType.RESTING_HR.name
        ) {
            1
        } else {
            0
        }
    }

    private fun lineGraphScoreColor(): Triple<Int, Int, Int> {
        var lineColor = 0
        var fillColorStart = 0
        var fillColorEnd = 0
        when (mViewModel.viewType) {
            "sleep" -> {
                lineColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.sleep_chart_line_color
                )
                fillColorStart = ContextCompat.getColor(
                    requireContext(),
                    R.color.sleep_fill_start_color
                )
                fillColorEnd = ContextCompat.getColor(
                    requireContext(),
                    R.color.sleep_fill_end_color
                )
            }

            "activity" -> {
                lineColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.activity_chart_line_color
                )
                fillColorStart = ContextCompat.getColor(
                    requireContext(),
                    R.color.activity_fill_start_color
                )
                fillColorEnd = ContextCompat.getColor(
                    requireContext(),
                    R.color.activity_fill_end_color
                )
            }

            "readiness" -> {
                lineColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.readiness_chart_line_color
                )
                fillColorStart = ContextCompat.getColor(
                    requireContext(),
                    R.color.readiness_fill_start_color
                )
                fillColorEnd = ContextCompat.getColor(
                    requireContext(),
                    R.color.readiness_fill_end_color
                )
            }
        }
        return Triple(lineColor, fillColorStart, fillColorEnd)

    }

    private fun barGraphScoreColor(): Pair<Int, Int> {
        var normalColor: Int = 0
        var selectedColor: Int = 0
        when (mViewModel.viewType) {
            "sleep" -> {
                normalColor = ContextCompat.getColor(
                    requireContext(), R.color.sleep_un_selected_bar_color
                )
                selectedColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.sleep_selected_bar_color
                )

            }

            "activity" -> {
                normalColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.activity_un_selected_bar_color
                )
                selectedColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.activity_selected_bar_color
                )

            }

            "readiness" -> {
                normalColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.readiness_un_selected_bar_color
                )
                selectedColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.readiness_selected_bar_color
                )

            }
        }
        return Pair(normalColor, selectedColor)

    }

    private fun updateProgressColor(type: Int) {
        val todayColor: Int
        val yesterdayColor: Int
        val allTimeColor: Int
        when (type) {
            0 -> {
                todayColor = returnColor().first
                yesterdayColor = returnColor().second
                allTimeColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )

            }

            1 -> {
                todayColor = returnColor().first
                yesterdayColor = returnColor().second
                allTimeColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            }

            2 -> {
                todayColor = returnColor().first
                yesterdayColor = returnColor().second
                allTimeColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            }

            else -> {
                todayColor = returnColor().first
                yesterdayColor = returnColor().second
                allTimeColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            }
        }
        binding.lytScoreOverview.lytToday.pbSteps.setIndicatorColor(
            todayColor
        )
        binding.lytScoreOverview.lytYesterday.pbSteps.setIndicatorColor(
            yesterdayColor
        )
        binding.lytAllTimeAvg.pbSteps.setIndicatorColor(
            allTimeColor
        )
    }

    private fun returnColor(): Pair<Int, Int> {
        val todayColor: Int
        val yesterdayColor: Int
        when (mViewModel.viewType) {
            "sleep" -> {
                todayColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.sleep_trend_bar_color
                )
                yesterdayColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.sleep_trend_bar_color_50
                )
            }

            "activity" -> {
                todayColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.oreo_activity_bar_color
                )
                yesterdayColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.oreo_activity_bar_color_50
                )
            }

            else -> {
                todayColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.readiness_progress_color
                )
                yesterdayColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.readiness_progress_color_50
                )
            }
        }

        return Pair(todayColor, yesterdayColor)

    }


    private fun getTrendTitle(): String {
        var trendTitle = ""
        when (mSharedViewModel.itemClickType) {
            ViewItemClickType.SLEEP_SCORE -> {
                trendTitle = "Sleep score trend"
            }

            ViewItemClickType.TOTAL_SLEEP -> {
                trendTitle = "Total sleep trend"
            }

            ViewItemClickType.SLEEP_EFFICIENCY -> {
                trendTitle = "Sleep efficiency trend"
            }

            ViewItemClickType.TIME_IN_BED -> {
                trendTitle = "Time in bed trend"
            }

            ViewItemClickType.RESTING_HR -> {
                trendTitle = "Average HR trend"
            }

            ViewItemClickType.READINESS_SCORE -> {
                trendTitle = "Readiness score trend "
            }

            ViewItemClickType.HR_VARIABILITY -> {
                trendTitle = "Hr variability trend"
            }

            ViewItemClickType.BODY_TEMPERATURE -> {
                trendTitle = "Body temperature trend"
            }

            ViewItemClickType.RESPIRATORY_RATE -> {
                trendTitle = "Respiratory rate trend"
            }

            ViewItemClickType.ACTIVITY_SCORE -> {
                trendTitle = "Activity score trend"
            }

            ViewItemClickType.ACTIVE_CALORIES -> {
                trendTitle = "Goal progress trend"
            }

            ViewItemClickType.TOTAL_CALORIES_BURNED -> {
                trendTitle = "Total calories trend"
            }

            ViewItemClickType.STEPS -> {
                trendTitle = "Steps trend"
            }

            ViewItemClickType.DISTANCE -> {
                trendTitle = "Distance trend"
            }

            null -> {

            }
        }
        return trendTitle
    }

    override fun onPositionSelected(position: Int, chartModel: ChartModel?) {
        chartModel?.date?.let { mViewModel.updateSelectedDate(it) }


    }

    override fun onScrolling(position: Int, chartModel: ChartModel?) {

    }
}


