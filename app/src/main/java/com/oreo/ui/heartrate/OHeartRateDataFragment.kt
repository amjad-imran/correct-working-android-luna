package com.oreo.ui.heartrate

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.data.CombinedData
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOHeartRateDataBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.OHealthOverview
import com.oreo.ui.sleep.banner.OreoSleepBannerFragment
import com.oreo.util.graph.OCombineChartUtils
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

    }

    override fun subscribeObservers() {
        viewModel.heartRateData.observe(viewLifecycleOwner) {
            if (it != null) {
                LOGS.d(TAG, Gson().toJson(it))
                setHearRateUi(it)
            }
            LOGS.d(TAG, Gson().toJson(it))
        }
    }

    private fun setHearRateUi(data: OHealthOverview.HeartRate) {
        val lytHeartRate = binding.lytHeartRate
        lytHeartRate.root.visible()
        val chart = lytHeartRate.candleChart

        OCombineChartUtils.setChart(chart, data.xLabelList, data.axisMinimum, data.average)

        val combinedData = CombinedData()
        if (data.lineData.first.isNotEmpty() && data.lineData.first.size > 1) {
            combinedData.setData(
                OCombineChartUtils.generateLineData(
                    data.lineData.first,
                    lytHeartRate.candleChart,
                    data.lineData.second,
                    data.axisMinimum
                )
            )
            combinedData.setData(
                OCombineChartUtils.generateCandleData(
                    data.candleValue, R.color.o_heart_bg
                )
            )
            chart.data = combinedData
            chart.invalidate()
        }
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