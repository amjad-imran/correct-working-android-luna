package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSkinTempInternalDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.ui.heartrate.OHRLearnMoreAdapter
import com.oreo.ui.heartrate.OnItemClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkinTempInternalDetailsFragment :
    BaseFragment<FragmentSkinTempInternalDetailsBinding>(FragmentSkinTempInternalDetailsBinding::inflate) {

    private val viewModel: SkinTempInternalDetailsViewModel by viewModels()
    private val args: SkinTempInternalDetailsFragmentArgs by navArgs()
    private val learnMoreAdapter: OHRLearnMoreAdapter by lazy {
        OHRLearnMoreAdapter(object : OnItemClickListener {
            override fun onItemClick(item: LearnMoreDataModel) {

            }

        })
    }

    companion object {
        fun getStartData(launchMode: SkinTempInternalLaunchState): Pair<Int, Bundle?> {
            return Pair(R.id.skinTempInternalDetailsFragment, Bundle().apply {
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
        if (viewModel.selectedLaunchMode == SkinTempInternalLaunchState.SKIN_TEMPERATURE) {
            binding.lytDeviation.root.visible()
            binding.lytSelector.root.gone()
        } else {
            binding.lytSelector.root.visible()
            binding.lytDeviation.root.gone()
        }
    }

    override fun initListener() {
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
        binding.lytDeviation.tvDeviation.setOnClickListener {
            binding.lytDeviation.tvDeviation.setBackgroundResource(R.drawable.back_deviation_selected)
            binding.lytDeviation.tvAbsolute.setBackgroundResource(0)
            binding.lytSelector.root.gone()
            viewModel.isDeviationSelected = true
            showTopContent()
        }
        binding.lytDeviation.tvAbsolute.setOnClickListener {
            binding.lytDeviation.tvAbsolute.setBackgroundResource(R.drawable.back_deviation_selected)
            binding.lytDeviation.tvDeviation.setBackgroundResource(0)
            binding.lytSelector.root.visible()
            viewModel.isDeviationSelected = false
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

    private fun showTopContent() {
        binding.lytTopView.lytTopSingleView.root.visible()
        binding.lytTopView.lytTopMultipleView.root.gone()
        val data = viewModel.hmDummyData()
        if (data != null) {
            //show single post fix
            binding.lytTopView.lytTopSingleView.lytTopPercentView.root.visible()
            binding.lytTopView.lytTopSingleView.lytTopHourView.root.gone()

            binding.lytTopView.lytTopSingleView.lytTopPercentView.tvScore.text =
                data.dspValue
            binding.lytTopView.lytTopSingleView.lytTopPercentView.tvUnit.text =
                viewModel.getPostFixAbr(data.trendType)

            binding.lytTopView.lytTopSingleView.apply {
                if (viewModel.isDeviationSelected) {
                    binding.lytTopView.lytTopSingleView.tvDateTime.text = data.dayDate
                } else {
                    val dayDate: String = when (viewModel.selectedPeriod.value) {
                        InternalSelectedPeriod.DAY -> data.dayDate
                        InternalSelectedPeriod.WEEK -> {
                            "23 April - 30 May, 2024"
                        }

                        else -> {
                            "July  2024"
                        }
                    }
                    tvDateTime.text = dayDate
                }
                tvDesc.text = data.description
                if (data.isShowHighlight) {
                    val colors = viewModel.getHighlightBackType(0)
                    lytHighlightTrends.main.setBackgroundResource(colors.first)
                    val textColor=ContextCompat.getColor(binding.lytTopView.lytTopSingleView.tvDesc.context,colors.second)
                    lytHighlightTrends.tvRangeValue.setTextColor(textColor)
                    val icons = viewModel.returnTrendsArrow(data.trendType)

                    lytHighlightTrends.ivTick.visible()
                    lytHighlightTrends.ivTick.setImageResource(icons)
                    lytHighlightTrends.root.visible()
                    lytHighlightTrends.tvRangeValue.visible()

                    lytHighlightTrends.tvRangeValue.text = "14% from yesterday"
                } else {
                    lytHighlightTrends.root.gone()
                    lytHighlightTrends.root.gone()
                }
            }
        } else {
//            defaultDataView()
        }
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
//        fragments.add(SleepSingleLineChartFragment.newInstance(viewModel.findLastSixMonthDatesList()))
        fragments.add(SleepMultiLineChartFragment.newInstance())
        fragments.add(SleepMultiBarChartFragment.newInstance())
        //fragments.add(SleepBarChartFragment.newInstance())

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
}

enum class SkinTempInternalLaunchState {
    RESPIRATORY_RATE, RESTING_HEART_RATE, HRV, SKIN_TEMPERATURE, BLOOD_OXYGEN
}