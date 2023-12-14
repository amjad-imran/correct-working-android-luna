package com.oreo.ui.sleep

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentBodyTempScoreDetailBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.clearDrawables
import com.noisefit_commans.common.setCompoundDrawable
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DistanceUtil
import com.oreo.data.model.ChartModel
import com.oreo.data.model.OInternalPageResponseModal
import com.oreo.data.model.ResultData
import com.oreo.ui.custom.ScrollListener
import com.oreo.ui.sleep.scoredetails.ClickViewType
import com.oreo.ui.sleep.scoredetails.OSCDViewModel
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class BodyTempScoreDetailFragment :
    BaseFragment<FragmentBodyTempScoreDetailBinding>(FragmentBodyTempScoreDetailBinding::inflate),
    ScrollListener {

    private val mViewModel: OSCDViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel.dayType = "Day"
        mViewModel.selectedDate = "2023-12-14"//TODO change
        mViewModel.itemClickType = ViewItemClickType.AVG_TEMP.name

        mViewModel.getReadinessInternalDetailsData()
    }

    private fun initUi(it: OInternalPageResponseModal) {
        val topGraphData = mViewModel.getPrefixAndSuffixListTemp(
            it.result as ArrayList<ResultData>,
            mViewModel.dayType
        )
        binding.rvTopBarGraph.updateDataWithMax(
            topGraphData.first.first,
            topGraphData.third,
            topGraphData.second
        )


        var trendTodayTitle = ""
        var trendYesterdayTitle = ""
        var trendScoreMsg = ""

        trendTodayTitle = getString(R.string.text_today)
        trendYesterdayTitle = getString(R.string.text_yesterday)

        binding.lytScoreOverview.tvTitle.text = "Body temperature trend"

        trendScoreMsg = if (mViewModel.isProgressEqual)
            "same as yesterday"
        else {
            if (mViewModel.isTodayGreater)
                "more than yesterday"
            else
                "less than yesterday"
        }


        binding.lytScoreOverview.lytToday.tvToday.text = trendTodayTitle
        binding.lytScoreOverview.lytYesterday.tvToday.text = trendYesterdayTitle
        binding.lytScoreOverview.tvScoreMsg.text = trendScoreMsg

        handleShowTrendCompareProgress(it)
        bindDataOnUi(it)
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

    }


    override fun initListener() {
        binding.rvTopBarGraph.setOnChartScrollChangedListener(this)

    }

    override fun subscribeObservers() {
        mViewModel.internalDetailsData.observe(viewLifecycleOwner) {
            if (it != null) {
                initUi(it)
            }
        }
    }

    private fun barGraphScoreColor(): Pair<Int, Int> {
        var normalColor: Int = 0
        var selectedColor: Int = 0

        normalColor = ContextCompat.getColor(
            requireContext(),
            R.color.readiness_un_selected_bar_color
        )
        selectedColor = ContextCompat.getColor(
            requireContext(),
            R.color.readiness_selected_bar_color
        )

        return Pair(normalColor, selectedColor)

    }

    override fun onPositionSelected(position: Int, chartModel: ChartModel?) {

    }

    override fun onScrolling(position: Int, chartModel: ChartModel?) {

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
            }
        }
    }
}