package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSkinTempInternalDetailsBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.Event
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.ui.heartrate.OHRLearnMoreAdapter
import com.oreo.ui.heartrate.OnItemClickListener
import com.oreo.ui.sleep2.ODropDownFragment
import com.oreo.ui.sleep2.SLEEP_DROP_DOWN_ITEM
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@AndroidEntryPoint
class SkinTempInternalDetailsFragment :
    BaseFragment<FragmentSkinTempInternalDetailsBinding>(FragmentSkinTempInternalDetailsBinding::inflate) {
    private val sharedViewModel: OSPTrendsSharedViewModel by activityViewModels()
    private var pagerAdapter: InternalSleepVPAdapter? = null
    private val viewModel: SkinTempInternalDetailsViewModel by viewModels()
    private val args: SkinTempInternalDetailsFragmentArgs by navArgs()
    private val learnMoreAdapter: OHRLearnMoreAdapter by lazy {
        OHRLearnMoreAdapter(object : OnItemClickListener {
            override fun onItemClick(item: LearnMoreDataModel) {

            }

        })
    }

    companion object {
        fun getStartData(launchMode: SleepInternalLaunchState): Pair<Int, Bundle?> {
            return Pair(R.id.skinTempInternalDetailsFragment, Bundle().apply {
                putSerializable("launchMode", launchMode)
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectedLaunchMode = args.launchMode
        viewModel.startDate =
            sharedViewModel.calendarStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        binding.toolbar.tvTitle.text = getString(R.string.text_trends_view)
        viewModel.setSelectedPeriod(InternalSelectedPeriod.DAY)
        initViewPager()
        viewModel.updateTitle()
        setGraphPagerView()
        setRecycler()
        initUI()
        viewModel.getTrendsInternalDetailsData()
    }


    private fun initViewPager() {
        pagerAdapter = InternalSleepVPAdapter(childFragmentManager, lifecycle)

        binding.graphPager.adapter = pagerAdapter
        binding.graphPager.layoutDirection = ViewPager2.LAYOUT_DIRECTION_RTL

        binding.graphPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pagerAdapter?.let {
                    val total = it.itemCount
                    if (position == (total - 1)) {//is last page
                        viewModel.loadMoreData()
                    }
                }

            }
        })
    }

    private fun initUI() {
        if (viewModel.selectedLaunchMode == SleepInternalLaunchState.SKIN_TEMPERATURE) {
            binding.lytDeviation.root.visible()
            binding.lytSelector.root.gone()
        } else {
            binding.lytSelector.root.visible()
            binding.lytDeviation.root.gone()
        }
    }

    override fun initListener() {
        binding.lytSpinnerView.setOnClickListener {
            setFragmentResultListener(SLEEP_DROP_DOWN_ITEM) { _, bundle ->
                val data = bundle.getSerializable("itemName") as SleepInternalLaunchState

                viewModel.reloadFragment.postValue(Event(data))
            }

            val (frag, bundle) = ODropDownFragment.getStartData(viewModel.selectedLaunchMode, true)
            navigate(frag, bundle)
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
        viewModel.reloadFragment.observe(this) {
            it.getContent()?.let {

                if (sharedViewModel.redirectToSkinTempInternal(viewModel.selectedLaunchMode)) {
                    navigate(
                        SkinTempInternalDetailsFragmentDirections.actionSkinTempInternalDetailsFragmentSelf(
                            it
                        )
                    )
                } else {
//                    navigate(
//                        SkinTempInternalDetailsFragmentDirections.actionSkinTempInternalDetailsFragmentToSleepFragment(
//                            it
//                        )
//                    )
                }

            }
        }


        viewModel.fragments.observe(this) {

            if (it == null) {
                pagerAdapter = InternalSleepVPAdapter(childFragmentManager, lifecycle)
                binding.graphPager.adapter = pagerAdapter
            }
            pagerAdapter?.setDataSet(it ?: ArrayList())

            showTopContent()
        }


        viewModel.selectedPeriod.observe(this) {
            setPeriodUiState(it)
            showTopContent()
            viewModel.reloadData()
        }


        viewModel.titleUpdate.observe(this) {
            binding.tvTrendName.text = it.first
            binding.ivTrendsIcon.setImageResource(it.second)
            showTopContent()
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

        sharedViewModel.interactGraphData.observe(this) {
            if (it == null) {
                showDefaultDates()
            } else {


                val topState = viewModel.getTopState()
                when (topState) {
                    TrendsTopState.SINGLE -> {
                        binding.lytTopView.lytTopSingleView.apply {
                            tvNudge.alpha = 0.5f
                            tvOptimalRangeLabel.alpha = 0.5f
                            ivCircle.alpha = 0.5f
                            lytHighlightTrends.root.gone()
                        }

                        val dayFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy")
                        binding.lytTopView.lytTopSingleView.tvDateTime.text = it.format(dayFormat)

                        val data = viewModel.trendsData[it]
                        if (data != null) {
                            val displayValue =
                                if (viewModel.selectedLaunchMode == SleepInternalLaunchState.REM_SLEEP ||
                                    viewModel.selectedLaunchMode == SleepInternalLaunchState.DEEP_SLEEP
                                ) {
                                    (data.value1 ?: 0.0f) / 60
                                } else {
                                    data.value1
                                }
                            binding.lytTopView.lytTopSingleView.lytTopPercentView.apply {
                                tvUnit.text = viewModel.getUnit()
                                tvScore.text =
                                    "$displayValue"
                            }
                        } else {
                            binding.lytTopView.lytTopSingleView.lytTopPercentView.apply {
                                tvUnit.text = viewModel.getUnit()
                                tvScore.text =
                                    "--"
                            }
                        }
                    }

                    TrendsTopState.SINGLE_DATE, TrendsTopState.DOUBLE_DATE -> {
                        binding.lytTopView.lytTopMultipleView.apply {
                            tvNudge.alpha = 0.5f
                        }

                        val dayFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy")
                        binding.lytTopView.lytTopMultipleView.tvDateTime.text = it.format(dayFormat)

                        val data = viewModel.trendsData[it]
                        if (data != null) {
                            binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.apply {
                                val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                                    (data.value1 ?: 0.0f).toInt() + (data.value2 ?: 0.0f).toInt()
                                )
                                tvHour.text = String.format(locale = Locale.US, "%02d", hour)
                                tvMin.text = String.format(locale = Locale.US, "%02d", minute)
                            }
                        } else {
                            binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.apply {
                                tvHour.text = "-"
                                tvMin.text = "-"
                            }
                        }
                    }

                }


            }
        }


    }

    private fun showDefaultDates() {
        val topState = viewModel.getTopState()

        when (topState) {
            TrendsTopState.SINGLE -> {
                binding.lytTopView.lytTopSingleView.apply {
                    tvNudge.alpha = 1.0f
                    tvOptimalRangeLabel.alpha = 1.0f
                    ivCircle.alpha = 1.0f
                    lytHighlightTrends.root.visible()
                }
            }

            TrendsTopState.SINGLE_DATE, TrendsTopState.DOUBLE_DATE -> {
                binding.lytTopView.lytTopMultipleView.apply {
                    tvNudge.alpha = 1.0f

                    lytContentView.divider1.root.gone()
                    lytContentView.lytNeed.root.gone()
                    lytContentView.lytHours.lytTrendsHighlight.root.alpha = 1.0f
                }
            }
        }


        val todayDate = LocalDate.now()
        val dayFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy")
        when (topState) {
            TrendsTopState.SINGLE -> {

                binding.lytTopView.lytTopSingleView.tvDateTime.text = viewModel.getTopDisplayDate()

                //Avg Value
                binding.lytTopView.lytTopSingleView.lytTopPercentView.root.visible()
                val avgValue = when (viewModel.selectedPeriod.value) {
                    InternalSelectedPeriod.DAY, null -> viewModel.dayAvg?.avg
                    InternalSelectedPeriod.WEEK -> viewModel.weekAvg?.avg
                    InternalSelectedPeriod.MONTH -> viewModel.monthAvg?.avg
                }


                if (avgValue == null) {
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvScore.text =
                        "--"
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvUnit.text =
                        viewModel.getUnit()
                } else {

                    val displayValue =
                        if (viewModel.selectedLaunchMode == SleepInternalLaunchState.REM_SLEEP
                            || viewModel.selectedLaunchMode == SleepInternalLaunchState.DEEP_SLEEP
                        ) {
                            val minValue = ((avgValue) / 60).roundToInt()
                            "$minValue"
                        } else {
                            "${avgValue.roundToInt()}"
                        }

                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvScore.text =
                        "${displayValue}"
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvUnit.text =
                        "${viewModel.getUnit()} - average"

                }

                binding.lytTopView.lytTopSingleView.lytHighlightTrends.root.gone()//todo set
            }

            TrendsTopState.SINGLE_DATE,TrendsTopState.DOUBLE_DATE -> {


                binding.lytTopView.lytTopMultipleView.tvDateTime.text =
                    viewModel.getTopDisplayDate()

                binding.lytTopView.lytTopMultipleView.lytContentView.root.visible()

                val avgValue = when (viewModel.selectedPeriod.value) {
                    InternalSelectedPeriod.DAY, null -> viewModel.dayAvg?.avg
                    InternalSelectedPeriod.WEEK -> viewModel.weekAvg?.avg
                    InternalSelectedPeriod.MONTH -> viewModel.monthAvg?.avg
                }

                if (avgValue == null) {
                    binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.tvHour.text =
                        "-"
                    binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.tvMin.text =
                        "-"
                } else {
                    val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                        avgValue.roundToInt() ?: 0
                    )

                    binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.tvHour.text =
                        "$hour"
                    binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.tvMin.text =
                        "$minute"

                }

                binding.lytTopView.lytTopMultipleView.lytContentView.lytNeed.lytTrendsHighlight.root.gone()//todo set
            }
        }
    }

    private fun showTopContent() {

        binding.lytTopView.lytTopSingleView.root.visible()
        binding.lytTopView.lytTopMultipleView.root.gone()


        when (viewModel.selectedPeriod.value) {
            InternalSelectedPeriod.DAY, null -> {
                if (viewModel.dayAvg != null) {

                    binding.lytTopView.lytTopSingleView.tvNudge.text = viewModel.dayAvg?.nudge ?: ""
                    binding.lytTopView.lytTopMultipleView.tvNudge.text =
                        viewModel.dayAvg?.nudge ?: ""
                }
            }

            InternalSelectedPeriod.WEEK -> {
                if (viewModel.weekAvg != null) {
                    binding.lytTopView.lytTopSingleView.tvNudge.text =
                        viewModel.weekAvg?.nudge ?: ""
                    binding.lytTopView.lytTopMultipleView.tvNudge.text =
                        viewModel.weekAvg?.nudge ?: ""
                }
            }

            InternalSelectedPeriod.MONTH -> {
                if (viewModel.monthAvg != null) {
                    binding.lytTopView.lytTopSingleView.tvNudge.text =
                        viewModel.monthAvg?.nudge ?: ""
                    binding.lytTopView.lytTopMultipleView.tvNudge.text =
                        viewModel.monthAvg?.nudge ?: ""
                }
            }
        }
        showDefaultDates()
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
        //fragments.add(SleepMultiLineChartFragment.newInstance())
        //fragments.add(SleepMultiBarChartFragment.newInstance())
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
                    //sleepBannerAdapter.addFragment(SleepMultiBarChartFragment.newInstance())
                }
            }
        })

    }
}

