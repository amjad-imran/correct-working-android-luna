package com.oreo.ui.femalehealth.cycletracker.insight

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCycleInsightDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.PeriodChartModel
import com.oreo.ui.custom.ScrollListenerPeriod
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CycleInsightDetailsFragment :
    BaseFragment<FragmentCycleInsightDetailsBinding>(FragmentCycleInsightDetailsBinding::inflate),
    ScrollListenerPeriod {
    private val args: CycleInsightDetailsFragmentArgs by navArgs()
    private val viewModel: CycleInsightDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.launchMode = args.launchMode

        viewModel.getGraphData()
        setUI()
    }

    private fun setUI() {
        binding.lytToolbar.tvTitle.text = viewModel.getToolbarTitle()

        binding.lytNormalLength.apply {
            textLength.text = getString(R.string.text_normal_length)
            ivColorBox.setBackgroundColor(android.graphics.Color.parseColor("#2bca79"))
            when (viewModel.launchMode) {
                CycleInsightLaunchMode.CYCLE_LENGTH -> {
                    tvDays.text = "21-35 days"
                }

                CycleInsightLaunchMode.PERIOD_DURATION -> {
                    tvDays.text = "2-7 days"
                }
            }

        }

        binding.lytAbnormalLength.apply {
            textLength.text = getString(R.string.text_abnormal_length)
            ivColorBox.setBackgroundColor(android.graphics.Color.parseColor("#ff84d5"))
            when (viewModel.launchMode) {
                CycleInsightLaunchMode.CYCLE_LENGTH -> {
                    tvDays.text = "<21, >25 days"
                }

                CycleInsightLaunchMode.PERIOD_DURATION -> {
                    tvDays.text = "<2, >7 days"
                }
            }

        }

    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.vGraph.setOnChartScrollChangedListener(this)

    }

    override fun subscribeObservers() {
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar1.root.visible()
            } else {
                binding.progressBar1.root.gone()
            }
        }

        viewModel.periodList.observe(this) {

            if (it.length.isNotEmpty()) {
                binding.vGraph.visible()
                val moveToPos = -1
                val topGraphData = viewModel.getPrefixAndSuffixList(it.length)
                val (normalMin, normalMax) = viewModel.getNormalMinMax()
                binding.vGraph.updateData(
                    topGraphData.first,
                    topGraphData.third,
                    topGraphData.second,
                    moveToPos,
                    normalMin,
                    normalMax,
                    it.avg,
                    if (viewModel.launchMode == CycleInsightLaunchMode.CYCLE_LENGTH) 10 else 5
                )
            }

            binding.tvValue.text = if (it.avg == null) "-" else "${it.avg}"
            binding.tvUnit.text = getString(R.string.text_days)
            if (it.nudge?.message.isNullOrEmpty()) {
                binding.tvDescription.gone()
            } else {
                binding.tvDescription.visible()
                binding.tvDescription.text = it.nudge?.message
            }
        }
    }

    override fun onPositionSelected(position: Int, chartModel: PeriodChartModel?) {
        if (viewModel.selectedDate == chartModel?.date!!) {
            return
        }


        viewModel.selectedDate = chartModel.date!!


        if (viewModel.shouldLoadMoreData()) {
            LOGS.w("Loading more data")
        }
    }

    override fun onScrolling(position: Int, chartModel: PeriodChartModel?) {
    }

}

enum class CycleInsightLaunchMode {
    CYCLE_LENGTH, PERIOD_DURATION
}