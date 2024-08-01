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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
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

        binding.toolbar.tvTitle.text = getString(R.string.text_trends_view)

        initUi()
        viewModel.updateTitle()
        setRecycler()
        initViewPager()
        viewModel.getTrendsInternalDetailsData()
    }

    private fun initUi() {
        if (viewModel.selectedLaunchMode == SleepInternalLaunchState.HOUR_VS_NEED) {
            binding.lytLegendRestorative.root.visible()
        } else {
            binding.lytLegendRestorative.root.gone()
        }
    }

    override fun initListener() {
        binding.lytSpinnerView.setOnClickListener {
            setFragmentResultListener(SLEEP_DROP_DOWN_ITEM) { _, bundle ->
                val data = bundle.getSerializable("itemName") as SleepInternalLaunchState

                viewModel.selectedLaunchMode = data
                viewModel.updateTitle()

                //todo reload data
            }

            val (frag, bundle) = ODropDownFragment.getStartData(viewModel.selectedLaunchMode,false)
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
                                    (data.value1 ?: 0) / 60
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
                                    data.value1 ?: 0
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

    //TODO optimize
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

            TrendsTopState.SINGLE_DATE -> {
                binding.lytTopView.lytTopMultipleView.apply {
                    tvNudge.alpha = 1.0f

                    lytContentView.divider1.root.gone()
                    lytContentView.lytNeed.root.gone()
                    lytContentView.lytHours.lytTrendsHighlight.root.alpha = 1.0f
                }
            }
        }


        val todayDate = LocalDate.now()
        when (viewModel.selectedPeriod.value) {
            InternalSelectedPeriod.DAY, null -> {
                val dayFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy")
                when (topState) {
                    TrendsTopState.SINGLE -> {
                        binding.lytTopView.lytTopSingleView.tvDateTime.text =
                            todayDate.format(dayFormat)

                        binding.lytTopView.lytTopSingleView.lytTopPercentView.root.visible()

                        if (viewModel.dayAvg?.avg == null) {
                            binding.lytTopView.lytTopSingleView.lytTopPercentView.tvScore.text =
                                "--"
                            binding.lytTopView.lytTopSingleView.lytTopPercentView.tvUnit.text =
                                viewModel.getUnit()
                        } else {
                            binding.lytTopView.lytTopSingleView.lytTopPercentView.tvScore.text =
                                "${viewModel.dayAvg?.avg}"
                            binding.lytTopView.lytTopSingleView.lytTopPercentView.tvUnit.text =
                                "${viewModel.getUnit()} - average"

                        }

                        binding.lytTopView.lytTopSingleView.lytHighlightTrends.root.gone()//todo set
                    }

                    TrendsTopState.SINGLE_DATE -> {
                        binding.lytTopView.lytTopMultipleView.tvDateTime.text =
                            todayDate.format(dayFormat)

                        binding.lytTopView.lytTopMultipleView.lytContentView.root.visible()

                        if (viewModel.dayAvg?.avg == null) {
                            binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.tvHour.text =
                                "-"
                            binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.tvMin.text =
                                "-"
                        } else {
                            val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                                viewModel.dayAvg?.avg?.roundToInt() ?: 0
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

            InternalSelectedPeriod.WEEK -> {
                val weekFormatStart = DateTimeFormatter.ofPattern("dd MMMM")
                val weekFormatEnd = DateTimeFormatter.ofPattern("dd MMMM, yyyy")
                val weekStart = todayDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val weekEnd = todayDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                val dateText =
                    "${weekStart.format(weekFormatStart)} - ${weekEnd.format(weekFormatEnd)}"

                binding.lytTopView.lytTopSingleView.tvDateTime.text = dateText

                if (viewModel.weekAvg?.avg == null) {
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvScore.text = "--"
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvUnit.text =
                        viewModel.getUnit()
                } else {
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvScore.text =
                        "${viewModel.weekAvg?.avg}"
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvUnit.text =
                        "${viewModel.getUnit()} - average"
                }
                binding.lytTopView.lytTopSingleView.lytHighlightTrends.root.gone()//todo set

            }

            InternalSelectedPeriod.MONTH -> {
                val dayFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
                binding.lytTopView.lytTopSingleView.tvDateTime.text = todayDate.format(dayFormat)

                if (viewModel.monthAvg?.avg == null) {
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvScore.text = "--"
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvUnit.text =
                        viewModel.getUnit()
                } else {
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvScore.text =
                        "${viewModel.monthAvg?.avg}"
                    binding.lytTopView.lytTopSingleView.lytTopPercentView.tvUnit.text =
                        "${viewModel.getUnit()} - average"
                }
                binding.lytTopView.lytTopSingleView.lytHighlightTrends.root.gone()//todo set
            }

        }
    }

    private fun showTopContent() {
        if (viewModel.selectedLaunchMode == SleepInternalLaunchState.HOUR_VS_NEED) {
            binding.lytTopView.lytTopMultipleView.root.visible()
            binding.lytTopView.lytTopSingleView.root.gone()
        } else if (viewModel.selectedLaunchMode == SleepInternalLaunchState.SLEEP_DURATION) {
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
                }
            }

            InternalSelectedPeriod.WEEK -> {
                if (viewModel.weekAvg != null) {
                    binding.lytTopView.lytTopSingleView.tvNudge.text =
                        viewModel.weekAvg?.nudge ?: ""
                }
            }

            InternalSelectedPeriod.MONTH -> {
                if (viewModel.monthAvg != null) {
                    binding.lytTopView.lytTopSingleView.tvNudge.text =
                        viewModel.monthAvg?.nudge ?: ""
                }
            }
        }
        showDefaultDates()


        /*val data = viewModel
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
        }*/
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
    HOUR_VS_NEED("performance"),//pending
    SLEEP_TIME("performance"),//pending
    TIMING("timing"),//pending
    EFFICIENCY("efficiency"),
    REM_SLEEP("rem_sleep"),
    DEEP_SLEEP("deep_sleep"),
    SLEEP_DURATION("sleep_duration"),
    LATENCY("latency"),
    RESTFULNESS("restfulness"),
    RESPIRATORY_RATE("respiratory_rate"),
    RESTING_HEART_RATE("resting_heart_rate"),
    HRV("hrv"),
    SKIN_TEMPERATURE("skin_temperature"),
    BLOOD_OXYGEN("blood_oxygen")
}

