package com.oreo.ui.sleep2.internal

import android.graphics.Color
import android.os.Bundle
import android.view.View
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
import com.noisefit_commans.utils.Event
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.ui.heartrate.OHRLearnMoreAdapter
import com.oreo.ui.heartrate.OnItemClickListener
import com.oreo.ui.sleep2.ODropDownFragment
import com.oreo.ui.sleep2.SLEEP_DROP_DOWN_ITEM
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@AndroidEntryPoint
class SleepInternalDetailsFragment :
    BaseFragment<FragmentSleepInternalDetailsBinding>(FragmentSleepInternalDetailsBinding::inflate) {

    private val viewModel: SleepInternalDetailsViewModel by viewModels()
    private val sharedViewModel: OSPTrendsSharedViewModel by activityViewModels()
    private val args: SleepInternalDetailsFragmentArgs by navArgs()
    private var pagerAdapter: InternalSleepVPAdapter? = null

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

        viewModel.startDate =
            sharedViewModel.calendarStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        binding.toolbar.tvTitle.text = getString(R.string.text_trends_view)

        initUi()
        viewModel.updateTitle()
        setRecycler()
        initViewPager()
        viewModel.getTrendsInternalDetailsData()
    }

    private fun initUi() {
        if (viewModel.selectedLaunchMode == SleepInternalLaunchState.HOUR_VS_NEED) {
            binding.lytLegendRestorative.apply {
                root.visible()
                shapeableImageView7.setBackgroundColor(Color.parseColor("#465c8a"))
                textView116.text = "hours"
                shapeableImageView8.setBackgroundColor(Color.parseColor("#7858cc"))
                textView117.text = "needs"
            }
        } else if (viewModel.selectedLaunchMode == SleepInternalLaunchState.RESTORATIVE_SLEEP) {
            binding.lytLegendRestorative.apply {
                root.visible()
                shapeableImageView7.setBackgroundColor(Color.parseColor("#7858cc"))
                textView116.text = "deep"
                shapeableImageView8.setBackgroundColor(Color.parseColor("#c3a3e3"))
                textView117.text = "rem"
            }

        } else {
            binding.lytLegendRestorative.root.gone()
        }
    }

    override fun initListener() {
        binding.lytSpinnerView.setOnClickListener {
            setFragmentResultListener(SLEEP_DROP_DOWN_ITEM) { _, bundle ->
                val data = bundle.getSerializable("itemName") as SleepInternalLaunchState

                viewModel.reloadFragment.postValue(Event(data))
            }

            val (frag, bundle) = ODropDownFragment.getStartData(viewModel.selectedLaunchMode, false)
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

    }

    override fun subscribeObservers() {

        viewModel.reloadFragment.observe(this) {
            it.getContent()?.let {
                navigate(
                    SleepInternalDetailsFragmentDirections.actionSleepInternalDetailsFragmentSelf(
                        it
                    )
                )
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
                            lytHighlightTrends.root.alpha = 0.5f
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
                                    "${displayValue?.roundToInt()}"
                            }
                        } else {
                            binding.lytTopView.lytTopSingleView.lytTopPercentView.apply {
                                tvUnit.text = viewModel.getUnit()
                                tvScore.text =
                                    "--"
                            }
                        }
                    }

                    TrendsTopState.SINGLE_DATE -> {
                        binding.lytTopView.lytTopMultipleView.apply {
                            tvNudge.alpha = 0.5f
                        }

                        val dayFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy")
                        binding.lytTopView.lytTopMultipleView.tvDateTime.text = it.format(dayFormat)

                        val data = viewModel.trendsData[it]
                        if (data != null) {
                            binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.apply {
                                val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                                    ((data.value1 ?: 0.0f) + (data.value2 ?: 0.0f)).roundToInt()
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

                    TrendsTopState.DOUBLE_DATE -> {
                        binding.lytTopView.lytTopMultipleView.apply {
                            tvNudge.alpha = 0.5f
                        }

                        val dayFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy")
                        binding.lytTopView.lytTopMultipleView.tvDateTime.text = it.format(dayFormat)

                        val data = viewModel.trendsData[it]
                        if (data != null) {
                            val hours = data.value1
                            val need = data.value2
                            if (hours != null) {
                                binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.apply {
                                    val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                                        hours.roundToInt()
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
                            if (need != null) {
                                binding.lytTopView.lytTopMultipleView.lytContentView.lytNeed.apply {
                                    val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                                        need.roundToInt()
                                    )
                                    tvHour.text = String.format(locale = Locale.US, "%02d", hour)
                                    tvMin.text = String.format(locale = Locale.US, "%02d", minute)
                                }
                            } else {
                                binding.lytTopView.lytTopMultipleView.lytContentView.lytNeed.apply {
                                    tvHour.text = "-"
                                    tvMin.text = "-"
                                }
                            }
                        } else {
                            binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.apply {
                                tvHour.text = "-"
                                tvMin.text = "-"
                            }
                            binding.lytTopView.lytTopMultipleView.lytContentView.lytNeed.apply {
                                tvHour.text = "-"
                                tvMin.text = "-"
                            }
                        }
                    }
                }


            }
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

    private fun showDefaultDates() {
        val topState = viewModel.getTopState()

        when (topState) {
            TrendsTopState.SINGLE -> {
                binding.lytTopView.lytTopSingleView.apply {
                    tvNudge.alpha = 1.0f
                    tvOptimalRangeLabel.alpha = 1.0f
                    ivCircle.alpha = 1.0f
                    lytHighlightTrends.root.alpha = 1.0f
                }
            }

            TrendsTopState.SINGLE_DATE -> {
                binding.lytTopView.lytTopMultipleView.apply {
                    tvNudge.alpha = 1.0f

                    lytContentView.divider1.root.gone()
                    lytContentView.lytNeed.root.gone()
                    lytContentView.lytHours.lytTrendsHighlight.root.gone()
                }
            }

            TrendsTopState.DOUBLE_DATE -> {
                binding.lytTopView.lytTopMultipleView.apply {
                    tvNudge.alpha = 1.0f
                    lytContentView.lytHours.lytTrendsHighlight.root.gone()
                }
            }
        }


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
            }

            TrendsTopState.SINGLE_DATE -> {

                binding.lytTopView.lytTopMultipleView.tvDateTime.text =
                    viewModel.getTopDisplayDate()

                binding.lytTopView.lytTopMultipleView.lytContentView.root.visible()

                val avgValue = when (viewModel.selectedPeriod.value) {
                    InternalSelectedPeriod.DAY, null -> viewModel.dayAvg?.avg
                    InternalSelectedPeriod.WEEK -> viewModel.weekAvg?.avg
                    InternalSelectedPeriod.MONTH -> viewModel.monthAvg?.avg
                }

                binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.lytTrendsHighlight.tvRangeValue.text =
                    "${viewModel.dayAvg?.status}"

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

                binding.lytTopView.lytTopMultipleView.lytContentView.lytNeed.lytTrendsHighlight.root.gone()
            }

            TrendsTopState.DOUBLE_DATE -> {
                binding.lytTopView.lytTopMultipleView.tvDateTime.text =
                    viewModel.getTopDisplayDate()

                binding.lytTopView.lytTopMultipleView.lytContentView.root.visible()

                val (avgHour, avgNeed) = when (viewModel.selectedPeriod.value) {
                    InternalSelectedPeriod.DAY, null -> Pair(
                        viewModel.dayAvg?.avg_hour,
                        viewModel.dayAvg?.avg_need
                    )

                    InternalSelectedPeriod.WEEK -> Pair(
                        viewModel.weekAvg?.avg_hour,
                        viewModel.weekAvg?.avg_need
                    )

                    InternalSelectedPeriod.MONTH -> Pair(
                        viewModel.monthAvg?.avg_hour,
                        viewModel.monthAvg?.avg_need
                    )
                }

                /* binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.lytTrendsHighlight.tvRangeValue.text =
                     "${viewModel.dayAvg?.status}"*/

                if (avgHour == null) {
                    binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.tvHour.text =
                        "-"
                    binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.tvMin.text =
                        "-"
                } else {
                    val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                        avgHour.roundToInt()
                    )

                    binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.tvHour.text =
                        "$hour"
                    binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.tvMin.text =
                        "$minute"
                }
                if (avgNeed == null) {
                    binding.lytTopView.lytTopMultipleView.lytContentView.lytNeed.tvHour.text =
                        "-"
                    binding.lytTopView.lytTopMultipleView.lytContentView.lytNeed.tvMin.text =
                        "-"
                } else {
                    val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                        avgNeed.roundToInt()
                    )

                    binding.lytTopView.lytTopMultipleView.lytContentView.lytNeed.tvHour.text =
                        "$hour"
                    binding.lytTopView.lytTopMultipleView.lytContentView.lytNeed.tvMin.text =
                        "$minute"

                }
                binding.lytTopView.lytTopMultipleView.lytContentView.lytNeed.lytTrendsHighlight.root.gone()
            }

        }
    }

    private fun showTopContent() {
        if (viewModel.selectedLaunchMode == SleepInternalLaunchState.HOUR_VS_NEED) {
            binding.lytTopView.lytTopMultipleView.root.visible()
            binding.lytTopView.lytTopSingleView.root.gone()
        } else if (viewModel.selectedLaunchMode == SleepInternalLaunchState.SLEEP_DURATION ||
            viewModel.selectedLaunchMode == SleepInternalLaunchState.RESTORATIVE_SLEEP
        ) {
            binding.lytTopView.lytTopMultipleView.root.visible()
            binding.lytTopView.lytTopMultipleView.apply {
                this.lytContentView.lytNeed.root.gone()
                this.lytContentView.divider1.root.gone()
            }
            binding.lytTopView.lytTopSingleView.root.gone()
        } else {
            binding.lytTopView.lytTopSingleView.root.visible()
            binding.lytTopView.lytTopMultipleView.root.gone()
        }

        when (viewModel.selectedPeriod.value) {
            InternalSelectedPeriod.DAY, null -> {
                if (viewModel.dayAvg != null) {

                    binding.lytTopView.lytTopSingleView.tvNudge.text = viewModel.dayAvg?.nudge ?: ""
                    binding.lytTopView.lytTopMultipleView.tvNudge.text =
                        viewModel.dayAvg?.nudge ?: ""
                    handleDayComparison()
                }
            }

            InternalSelectedPeriod.WEEK -> {
                if (viewModel.weekAvg != null) {
                    binding.lytTopView.lytTopSingleView.tvNudge.text =
                        viewModel.weekAvg?.nudge ?: ""
                    binding.lytTopView.lytTopMultipleView.tvNudge.text =
                        viewModel.weekAvg?.nudge ?: ""
                    handleWeekComparison()
                }
            }

            InternalSelectedPeriod.MONTH -> {
                if (viewModel.monthAvg != null) {
                    binding.lytTopView.lytTopSingleView.tvNudge.text =
                        viewModel.monthAvg?.nudge ?: ""
                    binding.lytTopView.lytTopMultipleView.tvNudge.text =
                        viewModel.monthAvg?.nudge ?: ""
                    handleMonthComparison()
                }
            }
        }
        showDefaultDates()
    }

    private fun handleDayComparison() {
        if (viewModel.dayAvg?.percent != null) {
            binding.lytTopView.lytTopSingleView.lytHighlightTrends.root.visible()
            viewModel.dayAvg
            if (viewModel.dayAvg!!.percent!! > 0) {
                binding.lytTopView.lytTopSingleView.lytHighlightTrends.apply {
                    tvRangeValue.text =
                        "${viewModel.dayAvg?.percent}% from yesterday"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_up
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(0)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }

            } else if (viewModel.dayAvg!!.percent!! == 0) {

                binding.lytTopView.lytTopSingleView.lytHighlightTrends.apply {
                    tvRangeValue.text =
                        "${viewModel.dayAvg?.percent}% from yesterday"
                    ivTick.setImageResource(
                        0
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(1)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            } else {
                binding.lytTopView.lytTopSingleView.lytHighlightTrends.apply {
                    tvRangeValue.text =
                        "${abs(viewModel.dayAvg?.percent!!)}% from yesterday"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_down
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(2)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            }
        } else {
            binding.lytTopView.lytTopSingleView.lytHighlightTrends.root.gone()
        }
    }

    private fun handleWeekComparison() {
        if (viewModel.weekAvg?.percent != null) {
            binding.lytTopView.lytTopSingleView.lytHighlightTrends.root.visible()
            viewModel.weekAvg
            if (viewModel.weekAvg!!.percent!! > 0) {
                binding.lytTopView.lytTopSingleView.lytHighlightTrends.apply {
                    tvRangeValue.text =
                        "${viewModel.weekAvg?.percent}% from last 6 week"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_up
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(0)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }

            } else if (viewModel.weekAvg!!.percent!! == 0) {

                binding.lytTopView.lytTopSingleView.lytHighlightTrends.apply {
                    tvRangeValue.text =
                        "${viewModel.weekAvg?.percent}% from last 6 week"
                    ivTick.setImageResource(
                        0
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(1)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            } else {
                binding.lytTopView.lytTopSingleView.lytHighlightTrends.apply {
                    tvRangeValue.text =
                        "${abs(viewModel.weekAvg?.percent!!)}% from last 6 week"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_down
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(2)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            }
        } else {
            binding.lytTopView.lytTopSingleView.lytHighlightTrends.root.gone()
        }
    }

    private fun handleMonthComparison() {
        if (viewModel.monthAvg?.percent != null) {
            binding.lytTopView.lytTopSingleView.lytHighlightTrends.root.visible()
            viewModel.monthAvg
            if (viewModel.monthAvg!!.percent!! > 0) {
                binding.lytTopView.lytTopSingleView.lytHighlightTrends.apply {
                    tvRangeValue.text =
                        "${viewModel.monthAvg?.percent}% from last 6 month"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_up
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(0)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }

            } else if (viewModel.monthAvg!!.percent!! == 0) {

                binding.lytTopView.lytTopSingleView.lytHighlightTrends.apply {
                    tvRangeValue.text =
                        "${viewModel.monthAvg?.percent}% from last 6 month"
                    ivTick.setImageResource(
                        0
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(1)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            } else {
                binding.lytTopView.lytTopSingleView.lytHighlightTrends.apply {
                    tvRangeValue.text =
                        "${abs(viewModel.monthAvg?.percent!!)}% from last 6 month"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_down
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(2)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            }
        } else {
            binding.lytTopView.lytTopSingleView.lytHighlightTrends.root.gone()
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
        /* when (viewModel.selectedLaunchMode) {
             SleepInternalLaunchState.SLEEP_TIME, SleepInternalLaunchState.EFFICIENCY -> fragments.add(
                 SleepSingleLineChartFragment.newInstance(pageData)
             )

             SleepInternalLaunchState.HOUR_VS_NEED -> fragments.add(SleepMultiLineChartFragment.newInstance())
             SleepInternalLaunchState.RESTORATIVE_SLEEP -> fragments.add(SleepMultiBarChartFragment.newInstance())
             else -> fragments.add(SleepBarChartFragment.newInstance())
         }*/

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

enum class SleepInternalLaunchState(val key: String) {
    RESTORATIVE_SLEEP("restorative_sleep"),
    SLEEP_PERFORMANCE("performance"),
    HOUR_VS_NEED("hourvsneed"),
    SLEEP_TIME("performance"),//pending
    TIMING("timing"),//pending
    EFFICIENCY("efficiency"),
    REM_SLEEP("rem_sleep"),
    DEEP_SLEEP("deep_sleep"),
    SLEEP_DURATION("sleep_duration"),
    LATENCY("latency"),
    RESTFULNESS("restfulness"),
    RESPIRATORY_RATE("resp"),
    RESTING_HEART_RATE("rhr"),
    HRV("hrv"),
    SKIN_TEMPERATURE("skin_temp"),
    BLOOD_OXYGEN("blood_oxy")
}

