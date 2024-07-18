package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSleepInternalDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.ui.heartrate.OHRLearnMoreAdapter
import com.oreo.ui.heartrate.OnItemClickListener
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.sleep2.ODropDownFragment
import com.oreo.ui.sleep2.SLEEP_DROP_DOWN_ITEM
import dagger.hilt.android.AndroidEntryPoint

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
//        binding.tvTrendName.text = viewModel.getTitle()
        viewModel.updateTitle()

        setGraphPagerView()
        setRecycler()
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
        fragments.add(SleepMultiLineChartFragment.newInstance())
        fragments.add(SleepMultiBarChartFragment.newInstance())
        fragments.add(SleepBarChartFragment.newInstance())

        val sleepBannerAdapter =
            OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)

        binding.graphPager.adapter = sleepBannerAdapter

        binding.graphPager.setCurrentItem(sleepBannerAdapter.itemCount - 1, true)
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
        }
        viewModel.titleUpdate.observe(this) {
            binding.tvTrendName.text = it
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

