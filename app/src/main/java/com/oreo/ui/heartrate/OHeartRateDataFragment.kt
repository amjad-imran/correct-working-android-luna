package com.oreo.ui.heartrate

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOHeartRateDataBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.data.model.OHealthOverview
import com.oreo.data.model.ServerUserHealthData
import com.oreo.ui.sleep.banner.OreoSleepBannerFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OHeartRateDataFragment :
    BaseFragment<FragmentOHeartRateDataBinding>(FragmentOHeartRateDataBinding::inflate) {
    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: OHeartRateDataViewModel by viewModels()
    private val ARGS_DATE = "ARGS_DATE"
    private val TAG = "HeartRateDataFragment"

    @Inject
    lateinit var vibrationUtils: VibrationUtils
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
                viewModel.summaryHealthData = dash.first
                viewModel.prepareActivityData(dash.first)
                setUi()

            }
        }
    }

    private fun setUi() {

        if (viewModel.summaryHealthData?.date == DateFormats.getCurrentDate(DateFormats.dateFormat3))
            viewModel.getTodayHeartRate()
        else
            viewModel.summaryHealthData?.let { viewModel.parseHealthData(it) }
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

            override fun onValueSelected(value: Int, position: Int, time: String?) {
                if (value != 0) {
                    LOGS.d("time to display $time")
                    binding.lytHeartRate.lytSubtitleValue1.tvValue.text = value.toString()
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.visible()
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.text = "bpm"
                    binding.lytHeartRate.tvSubtitle1.text = time
                } else {
                    binding.lytHeartRate.tvSubtitle1.text = "-"
                    binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.text = "bpm"
                }
            }

            override fun isInteractionOnGoing(onGoing: Boolean) {
                if (onGoing) {
                    binding.lytHeartRate.tvSubtitle2.gone()
                } else {
                    binding.lytHeartRate.tvSubtitle1.text = getString(R.string.text_average_hr)
                    binding.lytHeartRate.tvSubtitle2.visible()
                    viewModel.heartRateData.value?.let { updateUI(it, true) }
                }

            }

            override fun onTopClicked() {
                if (viewModel.activityData?.isNotEmpty() == true)
                    viewModel.activityData?.toTypedArray()?.let { it1 ->
                        navigate(
                            OHeartRateDetailsFragmentDirections.actionNavigationHrDetailsFragToDayTimeActivitiesBottomSheet(
                                it1
                            )
                        )
                    }

            }
        })



        binding.lytLearnMore.vRecycler.addOnItemTouchListener(object :
            RecyclerView.OnItemTouchListener {

            override fun onTouchEvent(view: RecyclerView, event: MotionEvent) {}

            override fun onInterceptTouchEvent(view: RecyclerView, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        binding.lytLearnMore.vRecycler.parent?.requestDisallowInterceptTouchEvent(
                            true
                        )
                    }
                }
                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    override fun subscribeObservers() {
        viewModel.heartRateData.observe(viewLifecycleOwner) {
            if (it != null) {
                LOGS.d(TAG, Gson().toJson(it))
                updateUI(it, false)
                initHeartRateGraph(viewModel.summaryHealthData, it)
            }
        }
    }

    private fun updateUI(it: OHealthOverview.HeartRateDataModel, isAvgShown: Boolean) {
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
        LOGS.d("LIST data ${Gson().toJson(it.listData)}")
        it.listData?.forEach {
            maxValue = it.maxValues
            minValue = it.minValues
        }
        binding.lytHeartRate.tvSubtitle2.text = "Range $minValue-$maxValue bpm"


    }

    private fun initHeartRateGraph(
        dayData: ServerUserHealthData?,
        heartRate: OHealthOverview.HeartRateDataModel
    ) {
        binding.lytHeartRate.candleChart.enableInteractiveMode(true)
        binding.lytHeartRate.candleChart.setVibrationUtil(vibrationUtils)
        binding.lytHeartRate.candleChart.updateData(
            viewModel.hrDataConvertor.getHrCombinedData(
                dayData, heartRate
            ), 5, heartRate.minValues, heartRate.maxValues
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
            isNestedScrollingEnabled = false
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