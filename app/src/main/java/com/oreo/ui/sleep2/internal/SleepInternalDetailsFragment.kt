package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSleepInternalDetailsBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.ui.heartrate.OHRLearnMoreAdapter
import com.oreo.ui.heartrate.OnItemClickListener
import com.oreo.ui.sleep2.ODropDownFragment
import com.oreo.ui.sleep2.SLEEP_DROP_DOWN_ITEM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SleepInternalDetailsFragment :
    BaseFragment<FragmentSleepInternalDetailsBinding>(FragmentSleepInternalDetailsBinding::inflate) {

    private val viewModel: SleepInternalDetailsViewModel by viewModels()
    private val sharedViewModel: OSPTrendsSharedViewModel by activityViewModels()
    private val args: SleepInternalDetailsFragmentArgs by navArgs()

    private val learnMoreAdapter: OHRLearnMoreAdapter by lazy {
        OHRLearnMoreAdapter(object : OnItemClickListener {
            override fun onItemClick(item: LearnMoreDataModel) {

            }

        })
    }

    companion object {
        fun getStartData(launchMode: SleepInternalLaunchState): Pair<Int, Bundle?> {
            return Pair(R.id.sleepInternalDetailsFragment, Bundle().apply {
                putSerializable("launchMode", launchMode)
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.selectedLaunchMode = args.launchMode

        binding.toolbar.tvTitle.text = getString(R.string.text_trends_view)
        viewModel.updateTitle()
        setRecycler()
        viewModel.getTrendsInternalDetailsData()
//        viewModel.findLastSixMonthDatesList()
    }

    private fun showTopContent() {
        if (viewModel.selectedLaunchMode == SleepInternalLaunchState.HOUR_VS_NEED) {
            binding.lytTopView.lytTopMultipleView.root.visible()
            binding.lytTopView.lytTopSingleView.root.gone()
        } else {
            binding.lytTopView.lytTopSingleView.root.visible()
            binding.lytTopView.lytTopMultipleView.root.gone()
        }

        val data = viewModel.pageData
        if (data != null) {
            if (data.trendType == SleepInternalLaunchState.HOUR_VS_NEED) {
                binding.lytTopView.lytTopMultipleView.tvDateTime.text = data.dayDate
                binding.lytTopView.lytTopMultipleView.lytContentView.apply {

                    val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                        data.dspValue2 ?: 0
                    )
                    if (hour > 0) {
                        lytNeed.tvHour.visible()
                        lytNeed.tvUnitHr.visible()
                    } else {
                        lytNeed.tvUnitHr.gone()
                        lytNeed.tvHour.gone()
                    }
                    lytNeed.tvHour.text = hour.toString()
                    if (minute > 0) {
                        lytNeed.tvMin.visible()
                        lytNeed.tvUnitMin.visible()
                    } else {
                        lytNeed.tvMin.gone()
                        lytNeed.tvUnitMin.gone()
                    }
                    lytNeed.tvMin.text = minute.toString()
                    lytNeed.lytTrendsHighlight.tvRangeValue.text = "5%"//todo will discuss
                    lytNeed.tvDesc.text = getString(R.string.text_avg_need)
                }
                binding.lytTopView.lytTopMultipleView.lytContentView.apply {
                    val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                        data.dspValue ?: 0
                    )
                    if (hour > 0) {
                        lytHours.tvHour.visible()
                        lytHours.tvUnitHr.visible()
                    } else {
                        lytHours.tvUnitHr.gone()
                        lytHours.tvHour.gone()
                    }
                    lytHours.tvHour.text = hour.toString()
                    if (minute > 0) {
                        lytHours.tvMin.visible()
                        lytHours.tvUnitMin.visible()
                    } else {
                        lytHours.tvMin.gone()
                        lytHours.tvUnitMin.gone()
                    }
                    lytHours.tvHour.text = hour.toString()
                    lytHours.tvMin.text = minute.toString()
                    lytHours.lytTrendsHighlight.tvRangeValue.text = "15%"
                    lytHours.tvDesc.text = getString(R.string.text_avg_hours)
                }
            } else {
                if (data.trendType == SleepInternalLaunchState.RESTFULNESS || data.trendType == SleepInternalLaunchState.SLEEP_PERFORMANCE) {
                    //show single post fix
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.root.visible()
                    binding.lytTopView.lytTopSingleView.lytTopHourView.root.gone()

                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvScore.text =
                        data.dspValue.toString()
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvUnit.text =
                        viewModel.getPostFixAbr(data.trendType)

                } else {
                    //show hour/minute post fix
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.root.gone()
                    binding.lytTopView.lytTopSingleView.lytTopHourView.root.visible()
                    val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                        data.dspValue ?: 0
                    )
                    binding.lytTopView.lytTopSingleView.lytTopHourView.apply {
                        if (hour > 0) {
                            tvHour.visible()
                            tvUnitHr.visible()
                        } else {
                            tvHour.gone()
                            tvUnitHr.gone()
                        }
                        tvHour.text = hour.toString()

                        if (minute > 0) {
                            tvMin.visible()
                            tvUnitMin.visible()
                        } else {
                            tvMin.gone()
                            tvUnitMin.gone()
                        }
                        tvMin.text = minute.toString()
                    }
                }

                binding.lytTopView.lytTopSingleView.apply {
                    tvDateTime.text = data.dayDate
                    tvDesc.text = data.description
                    if (data.isShowHighlight) {
                        val colors = viewModel.getHighlightBackType(0)
                        lytHighlightTrends.main.setBackgroundResource(colors.first)
                        val textColor = ContextCompat.getColor(
                            binding.lytTopView.lytTopSingleView.tvDesc.context,
                            colors.second
                        )
                        lytHighlightTrends.tvRangeValue.setTextColor(textColor)
                        val icons = viewModel.returnTrendsArrow(data.trendType)
                        if (icons == 0) {
                            lytHighlightTrends.ivTick.gone()
                        } else {
                            lytHighlightTrends.ivTick.visible()
                            lytHighlightTrends.ivTick.setImageResource(icons)
                        }
                        lytHighlightTrends.root.visible()
                        lytHighlightTrends.tvRangeValue.visible()

                        lytHighlightTrends.tvRangeValue.text = "14% from yesterday"
                    } else {
                        lytHighlightTrends.root.gone()
                        lytHighlightTrends.root.gone()
                    }
                }
            }
        } else {
            defaultDataView()
        }
    }

    private fun defaultDataView() {
    }

    private fun setRecycler() {
        with(binding.lytLearnMore.rvLearnMode) {
            isNestedScrollingEnabled = false
            adapter = learnMoreAdapter
        }
        learnMoreAdapter.setData(viewModel.getLearnMoreData())
    }


    private fun setGraphPagerView() {
        val fragments = ArrayList<Fragment>()
        val pageData = viewModel.overAllPageData
        when (viewModel.selectedLaunchMode) {
            SleepInternalLaunchState.SLEEP_TIME, SleepInternalLaunchState.EFFICIENCY -> fragments.add(
                SleepSingleLineChartFragment.newInstance(pageData)
            )

            SleepInternalLaunchState.HOUR_VS_NEED -> fragments.add(SleepMultiLineChartFragment.newInstance())
            SleepInternalLaunchState.RESTORATIVE_SLEEP -> fragments.add(SleepMultiBarChartFragment.newInstance())
            else -> fragments.add(SleepBarChartFragment.newInstance())
        }

        val sleepBannerAdapter = InternalSleepVPAdapter(childFragmentManager, lifecycle)

        binding.graphPager.adapter = sleepBannerAdapter
        binding.graphPager.layoutDirection = ViewPager2.LAYOUT_DIRECTION_RTL

        sleepBannerAdapter.setDataSet(fragments)

        binding.graphPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val total = sleepBannerAdapter.itemCount

                if (position == (total - 1)) {
                    sleepBannerAdapter.addFragment(SleepMultiBarChartFragment.newInstance())
                }
            }
        })

    }

    override fun initListener() {
        binding.lytSpinnerView.setOnClickListener {
            setFragmentResultListener(SLEEP_DROP_DOWN_ITEM) { _, bundle ->
                val data = bundle.getString("itemName")
                viewModel.updateTrendsName(data)
                viewModel.updateTitle()
            }
            when (viewModel.selectedLaunchMode) {
                SleepInternalLaunchState.RESTORATIVE_SLEEP -> {
                    val (frag, bundle) = ODropDownFragment.getStartData(SleepInternalLaunchState.RESTORATIVE_SLEEP)
                    navigate(frag, bundle)
                }

                SleepInternalLaunchState.SLEEP_PERFORMANCE -> {
                    val (frag, bundle) = ODropDownFragment.getStartData(SleepInternalLaunchState.SLEEP_PERFORMANCE)
                    navigate(frag, bundle)
                }

                SleepInternalLaunchState.HOUR_VS_NEED -> {
                    val (frag, bundle) = ODropDownFragment.getStartData(SleepInternalLaunchState.HOUR_VS_NEED)
                    navigate(frag, bundle)
                }

                SleepInternalLaunchState.SLEEP_TIME -> {
                    val (frag, bundle) = ODropDownFragment.getStartData(SleepInternalLaunchState.SLEEP_TIME)
                    navigate(frag, bundle)
                }

                SleepInternalLaunchState.EFFICIENCY -> {
                    val (frag, bundle) = ODropDownFragment.getStartData(SleepInternalLaunchState.EFFICIENCY)
                    navigate(frag, bundle)
                }

                SleepInternalLaunchState.REM_SLEEP -> {
                    val (frag, bundle) = ODropDownFragment.getStartData(SleepInternalLaunchState.REM_SLEEP)
                    navigate(frag, bundle)
                }

                SleepInternalLaunchState.DEEP_SLEEP -> {
                    val (frag, bundle) = ODropDownFragment.getStartData(SleepInternalLaunchState.DEEP_SLEEP)
                    navigate(frag, bundle)
                }

                SleepInternalLaunchState.SLEEP_DURATION -> {
                    val (frag, bundle) = ODropDownFragment.getStartData(SleepInternalLaunchState.SLEEP_DURATION)
                    navigate(frag, bundle)
                }

                SleepInternalLaunchState.LATENCY -> {
                    val (frag, bundle) = ODropDownFragment.getStartData(SleepInternalLaunchState.LATENCY)
                    navigate(frag, bundle)
                }

                SleepInternalLaunchState.RESTFULNESS -> {
                    val (frag, bundle) = ODropDownFragment.getStartData(SleepInternalLaunchState.RESTFULNESS)
                    navigate(frag, bundle)
                }

                else -> {}
            }
        }
        binding.lytSelector.tvDay.setOnClickListener {
            viewModel.setSelectedPeriod(InternalSelectedPeriod.DAY)


        }

        binding.lytSelector.tvWeek.setOnClickListener {
            viewModel.setSelectedPeriod(InternalSelectedPeriod.WEEK)

        }

        binding.lytSelector.tvMonth.setOnClickListener {
            viewModel.setSelectedPeriod(InternalSelectedPeriod.MONTH)

        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {
        sharedViewModel.interactGraphData.observe(this) {
//            val parseData = viewModel.parsePageData(pos = it)
//            viewModel.pageData = parseData
//            showTopContent()
        }

        viewModel.selectedPeriod.observe(this) {
            setPeriodUiState(it)
            showTopContent()
        }
        viewModel.titleUpdate.observe(this) {
            binding.tvTrendName.text = it.first
            binding.ivTrendsIcon.setImageResource(it.second)
            showTopContent()
        }

        viewModel.trendsInternalData.observe(this) {
            if (it != null) {
                if (it.values?.isNotEmpty() == true) {
                    val parseData = viewModel.parsePageData(it.values?.size?.minus(1) ?: 0)
                    viewModel.overAllPageData = it
                    viewModel.pageData = parseData
                    showTopContent()
                    setGraphPagerView()
                }

            }
        }

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
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar1.root.visible()
            } else {
                binding.progressBar1.root.gone()
            }
        }


    }

    private fun setPeriodUiState(state: InternalSelectedPeriod) {
        binding.lytSelector.tvDay.setBackgroundResource(0)
        binding.lytSelector.tvDay.setTextColor(resources.getColor(R.color.white_40))
        binding.lytSelector.tvWeek.setBackgroundResource(0)
        binding.lytSelector.tvWeek.setTextColor(resources.getColor(R.color.white_40))
        binding.lytSelector.tvMonth.setBackgroundResource(0)
        binding.lytSelector.tvMonth.setTextColor(resources.getColor(R.color.white_40))

        when (state) {
            InternalSelectedPeriod.DAY -> {
                binding.lytSelector.tvDay.setBackgroundResource(R.drawable.back_modal)
                binding.lytSelector.tvDay.setTextColor(resources.getColor(R.color.white))
            }

            InternalSelectedPeriod.WEEK -> {
                binding.lytSelector.tvWeek.setBackgroundResource(R.drawable.back_modal)
                binding.lytSelector.tvWeek.setTextColor(resources.getColor(R.color.white))
            }

            InternalSelectedPeriod.MONTH -> {
                binding.lytSelector.tvMonth.setBackgroundResource(R.drawable.back_modal)
                binding.lytSelector.tvMonth.setTextColor(resources.getColor(R.color.white))
            }
        }
    }
}

enum class SleepInternalLaunchState {
    RESTORATIVE_SLEEP, SLEEP_PERFORMANCE, HOUR_VS_NEED, SLEEP_TIME, EFFICIENCY, REM_SLEEP, DEEP_SLEEP, SLEEP_DURATION, LATENCY, RESTFULNESS
}

