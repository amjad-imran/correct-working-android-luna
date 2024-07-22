package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSleepInternalDetailsBinding
import com.noisefit.luna.databinding.FragmentSleepSingleLineChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.OSleepTrendsDataModel
import com.oreo.ui.heartrate.OHRLearnMoreAdapter
import com.oreo.ui.heartrate.OnItemClickListener
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.sleep2.ODropDownFragment
import com.oreo.ui.sleep2.SLEEP_DROP_DOWN_ITEM
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.addHeaderLenient

@AndroidEntryPoint
class SleepInternalDetailsFragment :
    BaseFragment<FragmentSleepInternalDetailsBinding>(FragmentSleepInternalDetailsBinding::inflate) {

    private val viewModel: SleepInternalDetailsViewModel by viewModels()
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
        setGraphPagerView()
        setRecycler()
        showTopContent()
    }

    private fun showTopContent() {
        if (viewModel.selectedLaunchMode == SleepInternalLaunchState.HOUR_VS_NEED) {
            binding.lytTopView.lytTopMultipleView.root.visible()
            binding.lytTopView.lytTopSingleView.root.gone()
        } else {
            binding.lytTopView.lytTopSingleView.root.visible()
            binding.lytTopView.lytTopMultipleView.root.gone()
        }
        val data=viewModel.trendsDummyData()
        if (data != null) {
            if (data.trendType == SleepInternalLaunchState.HOUR_VS_NEED) {
                val dayDate:String = when (viewModel.selectedPeriod.value) {
                    InternalSelectedPeriod.DAY -> data.dayDate
                    InternalSelectedPeriod.WEEK -> {
                        "23 April - 30 May, 2024"
                    }
                    else -> {
                        "July  2024"
                    }
                }
                binding.lytTopView.lytTopMultipleView.tvDateTime.text=dayDate
                binding.lytTopView.lytTopMultipleView.lytContentView.apply {
                    lytNeed.tvHour.text = "8"
                    lytNeed.tvMin.text = "28"
                    lytNeed.lytTrendsHighlight.tvRangeValue.text = "5%"
                    lytNeed.tvDesc.text = "avg hours"
                }
                binding.lytTopView.lytTopMultipleView.lytContentView.apply {
                    lytHours.tvHour.text = "3"
                    lytHours.tvMin.text = "28"
                    lytHours.lytTrendsHighlight.tvRangeValue.text = "15%"
                    lytHours.tvDesc.text = "avg need"
                }
            } else {
                if (data.trendType == SleepInternalLaunchState.RESTFULNESS || data.trendType == SleepInternalLaunchState.SLEEP_PERFORMANCE) {
                    //show single post fix
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.root.visible()
                    binding.lytTopView.lytTopSingleView.lytTopHourView.root.gone()

                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvUnit.text =
                        data.dspValue
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvUnit.text =
                        viewModel.getPostFixAbr(data.trendType)

                } else {
                    //show hour/minute post fix
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.root.gone()
                    binding.lytTopView.lytTopSingleView.lytTopHourView.root.visible()
                    val hour = 8
                    val min = 19
                    binding.lytTopView.lytTopSingleView.lytTopHourView.apply {
                        if (hour > 0 && min > 0) {
                            tvHour.visible()
                            tvUnitHr.visible()
                            tvMin.visible()
                            tvUnitMin.visible()
                        } else if (hour <= 0 && min > 0) {
                            tvHour.gone()
                            tvMin.gone()
                            tvMin.visible()
                            tvUnitMin.visible()
                        }
                        tvHour.text = hour.toString()
                        tvMin.text = min.toString()
                    }
                }

                binding.lytTopView.lytTopSingleView.apply {
                    val dayDate:String = when (viewModel.selectedPeriod.value) {
                        InternalSelectedPeriod.DAY -> data.dayDate
                        InternalSelectedPeriod.WEEK -> {
                            "23 April - 30 May, 2024"
                        }
                        else -> {
                            "July  2024"
                        }
                    }
                    tvDateTime.text = dayDate
                    tvDesc.text = data.description
                    if (data.isShowHighlight) {
                        val colors = viewModel.getHighlightBackType(0)
                        lytHighlightTrends.main.setBackgroundResource(colors.first)
                        lytHighlightTrends.tvRangeValue.setTextColor(colors.second)
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
        }
        else{
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
        fragments.add(SleepSingleLineChartFragment.newInstance())
        fragments.add(SleepMultiLineChartFragment.newInstance())
        fragments.add(SleepMultiBarChartFragment.newInstance())
        fragments.add(SleepBarChartFragment.newInstance())

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

        viewModel.selectedPeriod.observe(this) {
            setPeriodUiState(it)
            showTopContent()
        }
        viewModel.titleUpdate.observe(this) {
            binding.tvTrendName.text = it.first
            binding.ivTrendsIcon.setImageResource(it.second)
            showTopContent()
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

