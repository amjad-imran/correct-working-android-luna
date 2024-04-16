package com.oreo.ui.heartrate

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOHeartRateDataBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.common.maxWithoutZero
import com.noisefit_commans.common.minWithoutZero
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.ServerUserHealthData
import com.oreo.ui.sleep.banner.OreoSleepBannerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OHeartRateDataFragment :
    BaseFragment<FragmentOHeartRateDataBinding>(FragmentOHeartRateDataBinding::inflate) {
    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: OHeartRateDataViewModel by viewModels()
    private val ARGS_DATE = "ARGS_DATE"
    private val TAG = "HeartRateDataFragment"
    private val learnMoreAdapter: OHRLearnMoreAdapter by lazy {
        OHRLearnMoreAdapter(object : OnItemClickListener {
            override fun onItemClick(item: LearnMoreDataModel) {
                navigate(R.id.fragmentLearMoreDetails, Bundle().apply {
                    this.putParcelable("data", item)
                })
            }

        })
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        LOGS.d(TAG, "Today Load data")
        viewModel.date?.let {
            mainViewModel.getDashBoardData(it)?.let { dash ->
                setUi()
                viewModel.summaryHealthData = dash.first
            }
        }
    }

    private fun setUi() {
        viewModel.getTodayHeartRate()
    }

    companion object {
        @JvmStatic
        fun newInstance(date: String) = OHeartRateDataFragment().apply {
            arguments = Bundle().apply {
                putString(ARGS_DATE, date)
            }
        }
    }

    override fun initListener() {
        binding.lytHeartRate.candleChart.setClickListener(object : OnHRClickAction {

            override fun onValueSelected(value: Int, position: Int) {

            }

            override fun isInteractionOnGoing(onGoing: Boolean) {
                if (onGoing) {
                    binding.lytHeartRate.tvSubtitle2.gone()
                } else {
                    binding.lytHeartRate.tvSubtitle2.visible()
                }

            }

            override fun onTopClicked() {

            }
        })

    }

    override fun subscribeObservers() {
        viewModel.heartRateData.observe(viewLifecycleOwner) {
            if (it != null) {
                LOGS.d(TAG, Gson().toJson(it))
                updateUI(it)
                initHeartRateGraph(viewModel.summaryHealthData, it)
            }
        }
    }

    private fun updateUI(it: OHealthOverview.HeartRateDataModel) {
        if (it.average.toInt() != 0) {
            binding.lytHeartRate.lytSubtitleValue1.tvValue.text = it.average.toInt().toString()
            binding.lytHeartRate.lytSubtitleValue1.tvUnit.visible()
            binding.lytHeartRate.lytSubtitleValue1.tvUnit.text = "bpm"
        } else {
            binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
            binding.lytHeartRate.lytSubtitleValue1.tvUnit.text = "bpm"
        }
        var maxValue: Int = 0
        var minValue: Int = 0
        it.listData?.forEach {
            maxValue = it.values?.maxWithoutZero() ?: 0
            minValue = it.values?.minWithoutZero() ?: 0
        }
        binding.lytHeartRate.tvSubtitle2.text = "Range $minValue-$maxValue bpm"


    }

    private fun initHeartRateGraph(
        dayData: ServerUserHealthData?,
        heartRate: OHealthOverview.HeartRateDataModel
    ) {
        binding.lytHeartRate.candleChart.enableInteractiveMode(true)
        binding.lytHeartRate.candleChart.updateData(
            viewModel.hrDataConvertor.getHrCombinedData(
                dayData, heartRate
            ), 5,heartRate.minValues,heartRate.maxValues
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val date = arguments?.getString(ARGS_DATE)
        LOGS.d("Current data $date")
        viewModel.date = date
//        setHRBannerViewPager()
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.lytLearnMore.vRecycler) {
            adapter = learnMoreAdapter
        }
        learnMoreAdapter.setData(viewModel.getLearnMoreData())
    }

    private fun setHRBannerViewPager() {
        val data = viewModel.getNudges()
        if (data.isNullOrEmpty()) {
            binding.lytBanner.root.gone()
            return
        } else {
            binding.lytBanner.root.visible()
        }

        val fragments = ArrayList<OreoSleepBannerFragment>()

        data.forEach {
            fragments.add(OreoSleepBannerFragment.newInstance(it))
        }

        val winsAdapter =
            OHRBannerAdapter(childFragmentManager, lifecycle, fragments)
        binding.lytBanner.vpBannerSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })
            adapter = winsAdapter
        }

        TabLayoutMediator(
            binding.lytBanner.tabLayout,
            binding.lytBanner.vpBannerSlider
        ) { _, _ -> }.attach()

        binding.lytBanner.vpBannerSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            }

        })

        if (fragments.size > 1) {
            binding.lytBanner.tabLayout.visible()
        } else {
            binding.lytBanner.tabLayout.invisible()
        }
    }

}