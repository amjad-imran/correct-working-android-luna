package com.oreo.ui.sleep2.internal

import android.graphics.Color
import android.os.Bundle
import android.view.View
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
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.Event
import com.oreo.data.model.LearnMoreDataModel
import com.oreo.ui.heartrate.OHRLearnMoreAdapter
import com.oreo.ui.heartrate.OnItemClickListener
import com.oreo.ui.sleep2.ODropDownFragment
import com.oreo.ui.sleep2.SLEEP_DROP_DOWN_ITEM
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
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
        fun getStartData(
            launchMode: SleepInternalLaunchState,
            selectedDate: String?
        ): Pair<Int, Bundle?> {
            return Pair(R.id.sleepInternalDetailsFragment, Bundle().apply {
                putSerializable("launchMode", launchMode)
                putSerializable("selectedDate", selectedDate)
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.selectedLaunchMode = args.launchMode
        args.selectedDate?.let {
            viewModel.selectedDate = LocalDate.parse(it)
        }

        viewModel.startDate =
            sharedViewModel.calendarStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        binding.toolbar.tvTitle.text = getString(R.string.text_trends_view)

        initUi()
        setRecycler()
        initViewPager()

        viewModel.loadGraphData(false)
    }

    private fun initUi() {
        viewModel.updateTitle()

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


        if (viewModel.selectedLaunchMode == SleepInternalLaunchState.SKIN_TEMPERATURE) {
            binding.lytDeviation.root.visible()
            binding.lytSelector.root.gone()
        } else {
            binding.lytDeviation.root.gone()
            binding.lytSelector.root.visible()
        }

        if (viewModel.isHealthMonitorTrend()) {
            binding.lytSelector.tvDaily.visible()
            viewModel.setSelectedPeriod(InternalSelectedPeriod.DAILY)
        } else {
            binding.lytSelector.tvDaily.gone()
            viewModel.setSelectedPeriod(InternalSelectedPeriod.DAY)
        }
    }


    override fun initListener() {
        binding.tvPrevious.setOnClickListener {
            viewModel.loadPreviousPeriodData()
        }

        binding.tvNext.setOnClickListener {
            viewModel.loadNextPeriodData()
        }

        binding.lytDeviation.tvDeviation.setOnClickListener {
            binding.lytDeviation.tvDeviation.setBackgroundResource(R.drawable.back_deviation_selected)
            binding.lytDeviation.tvAbsolute.setBackgroundResource(0)
            binding.lytSelector.root.gone()
            viewModel.isDeviationSelected = true
            viewModel.reloadData()
        }

        binding.lytDeviation.tvAbsolute.setOnClickListener {
            binding.lytDeviation.tvAbsolute.setBackgroundResource(R.drawable.back_deviation_selected)
            binding.lytDeviation.tvDeviation.setBackgroundResource(0)
            binding.lytSelector.root.visible()
            viewModel.isDeviationSelected = false
            viewModel.reloadData()
        }

        binding.lytSpinnerView.setOnClickListener {
            setFragmentResultListener(SLEEP_DROP_DOWN_ITEM) { _, bundle ->
                val data = bundle.getSerializable("itemName") as SleepInternalLaunchState

                viewModel.reloadFragment.postValue(Event(data))
            }

            val (frag, bundle) = ODropDownFragment.getStartData(viewModel.selectedLaunchMode, false)
            navigate(frag, bundle)
        }
        binding.lytSelector.tvDaily.setOnClickListener {
            viewModel.setSelectedPeriod(InternalSelectedPeriod.DAILY)
            viewModel.reloadData()
        }
        binding.lytSelector.tvDay.setOnClickListener {
            viewModel.setSelectedPeriod(InternalSelectedPeriod.DAY)
            viewModel.reloadData()
        }

        binding.lytSelector.tvWeek.setOnClickListener {
            viewModel.setSelectedPeriod(InternalSelectedPeriod.WEEK)
            viewModel.reloadData()
        }

        binding.lytSelector.tvMonth.setOnClickListener {
            viewModel.setSelectedPeriod(InternalSelectedPeriod.MONTH)
            viewModel.reloadData()
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {
        viewModel.topContentData.observe(this) {
            showTopContent(it)
        }


        viewModel.currentFragment.observe(this) {
            pagerAdapter = InternalSleepVPAdapter(childFragmentManager, lifecycle)
            binding.graphPager.adapter = pagerAdapter
            pagerAdapter?.setDataSet(arrayListOf(it))
        }

        viewModel.reloadFragment.observe(this) {
            it.getContent()?.let {
                navigate(
                    SleepInternalDetailsFragmentDirections.actionSleepInternalDetailsFragmentSelf(
                        it, viewModel.selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    )
                )
            }
        }

        sharedViewModel.interactGraphData.observe(this) {
            if (it == null) {
                viewModel.topContentData.postValue(
                    TopContentData(
                        isInteracting = false,
                        trendsData = viewModel.topContentDataAverage
                    )
                )
            } else {
                viewModel.topContentData.postValue(
                    TopContentData(
                        isInteracting = true,
                        date = it,
                        trendsData = viewModel.topContentDataAverage
                    )
                )
            }
        }
        sharedViewModel.interactGraphDataDaily.observe(this) {
            if (it == null) {
                viewModel.topContentData.postValue(
                    TopContentData(
                        isInteracting = false,
                        trendsData = viewModel.topContentDataAverage
                    )
                )
            } else {
                viewModel.topContentData.postValue(
                    TopContentData(
                        isInteracting = true,
                        date = it.first,
                        dailyValue = it.second,
                        trendsData = viewModel.topContentDataAverage
                    )
                )
            }
        }

        viewModel.selectedPeriod.observe(this) {
            setPeriodUiState(it)
        }

        viewModel.titleUpdate.observe(this) {
            binding.tvTrendName.text = it.first
            binding.ivTrendsIcon.setImageResource(it.second)
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

    }

    private fun showSingleDateData(avgValue: Float?, percent: Int?) {
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

        val singleBind =
            binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.lytTrendsHighlight
        if (percent == null) {
            singleBind.root.invisible()
        } else {
            singleBind.root.visible()
            if (percent > 0) {
                singleBind.apply {
                    tvRangeValue.text =
                        "${percent}% ${getRangeText()}"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_up
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(0)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            } else if (percent == 0) {
                singleBind.apply {
                    tvRangeValue.text =
                        "${percent}% ${getRangeText()}"
                    ivTick.setImageResource(
                        0
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(1)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            } else {
                singleBind.apply {
                    tvRangeValue.text =
                        "${abs(percent)}% ${getRangeText()}"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_down
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(2)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            }
        }
    }

    private fun setSingleData(avgValue: Float?, percent: Int?) {
        binding.lytTopView.lytTopSingleView.lytTopPercentView.root.visible()
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
                "${viewModel.getUnit()}"

        }

        val singleBind = binding.lytTopView.lytTopSingleView.lytHighlightTrends
        if (percent == null) {
            singleBind.root.gone()
        } else {
            singleBind.root.visible()
            if (percent > 0) {
                singleBind.apply {
                    tvRangeValue.text =
                        "${percent}% ${getRangeText()}"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_up
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(0)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            } else if (percent == 0) {
                singleBind.apply {
                    tvRangeValue.text =
                        "${percent}% ${getRangeText()}"
                    ivTick.setImageResource(
                        0
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(1)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            } else {
                singleBind.apply {
                    tvRangeValue.text =
                        "${abs(percent)}% ${getRangeText()}"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_down
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(2)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            }
        }
    }

    private fun showTopContent(topContentData: TopContentData) {
        val topState = viewModel.getTopState()
        when (topState) {
            TrendsTopState.SINGLE -> {
                binding.lytTopView.lytTopSingleView.root.visible()
                binding.lytTopView.lytTopMultipleView.root.gone()
                binding.lytTopView.lytTopSingleView.tvNudge.text =
                    topContentData.trendsData?.nudge ?: ""

                if (topContentData.isInteracting) {
                    binding.lytTopView.lytTopSingleView.apply {
                        tvNudge.alpha = 0.5f
                        tvOptimalRangeLabel.alpha = 0.5f
                        ivCircle.alpha = 0.5f
                        lytHighlightTrends.root.alpha = 0.5f

                        val dayFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy")
                        tvDateTime.text = topContentData.date?.format(dayFormat)
                    }

                    val data = viewModel.trendsData[topContentData.date]

                    val value = topContentData.dailyValue ?: data?.value1
                    setSingleData(value, null)
                } else {
                    binding.lytTopView.lytTopSingleView.apply {
                        tvNudge.alpha = 1.0f
                        tvOptimalRangeLabel.alpha = 1.0f
                        ivCircle.alpha = 1.0f
                        lytHighlightTrends.root.alpha = 1.0f
                        tvDateTime.text = getString(R.string.text_average)
                    }
                    setSingleData(
                        topContentData.trendsData?.avg,
                        topContentData.trendsData?.percent
                    )
                }
            }

            TrendsTopState.SINGLE_DATE -> {
                binding.lytTopView.lytTopSingleView.root.gone()
                binding.lytTopView.lytTopMultipleView.root.visible()
                binding.lytTopView.lytTopMultipleView.apply {
                    this.lytContentView.lytNeed.root.gone()
                    this.lytContentView.divider1.root.gone()
                }
                binding.lytTopView.lytTopMultipleView.tvNudge.text =
                    topContentData.trendsData?.nudge ?: ""

                if (topContentData.isInteracting) {
                    binding.lytTopView.lytTopMultipleView.apply {
                        tvNudge.alpha = 0.5f
                        tvOptimalRangeLabel.alpha = 0.5f
                        ivCircle.alpha = 0.5f

                        val dayFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy")
                        tvDateTime.text = topContentData.date?.format(dayFormat)
                        lytContentView.lytHours.lytTrendsHighlight.root.invisible()
                    }
                    val value =
                        if (viewModel.selectedLaunchMode == SleepInternalLaunchState.SLEEP_TIME) {
                            viewModel.getDuration(viewModel.trendsData[topContentData.date])
                        } else {
                            viewModel.trendsData[topContentData.date]?.value1
                        }
                    showSingleDateData(value, null)
                } else {
                    binding.lytTopView.lytTopMultipleView.apply {
                        tvNudge.alpha = 1.0f
                        tvOptimalRangeLabel.alpha = 1.0f
                        ivCircle.alpha = 1.0f
                        tvDateTime.text = getString(R.string.text_average)
                        lytContentView.lytHours.lytTrendsHighlight.root.visible()
                    }
                    showSingleDateData(
                        topContentData.trendsData?.avg,
                        topContentData.trendsData?.percent
                    )
                }
            }

            TrendsTopState.DOUBLE_DATE -> {
                binding.lytTopView.lytTopMultipleView.root.visible()
                binding.lytTopView.lytTopSingleView.root.gone()

                binding.lytTopView.lytTopMultipleView.tvNudge.text =
                    topContentData.trendsData?.nudge ?: ""


                if (topContentData.isInteracting) {
                    binding.lytTopView.lytTopMultipleView.apply {
                        tvNudge.alpha = 0.5f
                        tvOptimalRangeLabel.alpha = 0.5f
                        ivCircle.alpha = 0.5f

                        lytContentView.lytHours.lytTrendsHighlight.root.alpha = 0.5f
                        lytContentView.lytNeed.lytTrendsHighlight.root.alpha = 0.5f

                        val dayFormat = DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy")
                        tvDateTime.text = topContentData.date?.format(dayFormat)

                    }
                    val data = viewModel.trendsData[topContentData.date]
                    showDoubleDateData(data?.value1, data?.value2, null, null)

                } else {
                    binding.lytTopView.lytTopMultipleView.apply {
                        tvNudge.alpha = 1.0f
                        tvOptimalRangeLabel.alpha = 1.0f
                        ivCircle.alpha = 1.0f
                        tvDateTime.text = getString(R.string.text_average)
                        lytContentView.lytHours.lytTrendsHighlight.root.alpha = 1.0f
                        lytContentView.lytNeed.lytTrendsHighlight.root.alpha = 1.0f
                    }

                    showDoubleDateData(
                        topContentData.trendsData?.avg_hour,
                        topContentData.trendsData?.avg_need,
                        topContentData.trendsData?.percent_hour,
                        topContentData.trendsData?.percent_need,
                    )

                }
            }
        }

        val optimalRange =
            sharedViewModel.getOptimalRangeMinMax(viewModel.selectedLaunchMode)
        if (optimalRange == null) {
            binding.lytTopView.lytTopSingleView.ivCircle.gone()
            binding.lytTopView.lytTopSingleView.tvOptimalRangeLabel.gone()

            binding.lytTopView.lytTopMultipleView.ivCircle.gone()
            binding.lytTopView.lytTopMultipleView.tvOptimalRangeLabel.gone()
        } else {
            binding.lytTopView.lytTopSingleView.ivCircle.visible()
            binding.lytTopView.lytTopSingleView.tvOptimalRangeLabel.visible()

            binding.lytTopView.lytTopMultipleView.ivCircle.visible()
            binding.lytTopView.lytTopMultipleView.tvOptimalRangeLabel.visible()
        }
    }

    private fun showDoubleDateData(value1: Float?, value2: Float?, percent1: Int?, percent2: Int?) {
        if (value1 == null && value2 == null) {
            binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.apply {
                tvHour.text = "-"
                tvMin.text = "-"
            }
            binding.lytTopView.lytTopMultipleView.lytContentView.lytNeed.apply {
                tvHour.text = "-"
                tvMin.text = "-"
            }
        } else {
            val hours = value1
            val need = value2
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
        }

        val singleBind =
            binding.lytTopView.lytTopMultipleView.lytContentView.lytHours.lytTrendsHighlight
        if (percent1 == null) {
            singleBind.root.invisible()
        } else {
            singleBind.root.visible()
            if (percent1 > 0) {
                singleBind.apply {
                    tvRangeValue.text =
                        "${percent1}% ${getRangeText()}"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_up
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(0)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            } else if (percent1 == 0) {
                singleBind.apply {
                    tvRangeValue.text =
                        "${percent1}% ${getRangeText()}"
                    ivTick.setImageResource(
                        0
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(1)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            } else {
                singleBind.apply {
                    tvRangeValue.text =
                        "${abs(percent1)}% ${getRangeText()}"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_down
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(2)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            }
        }

        val singleBind2 =
            binding.lytTopView.lytTopMultipleView.lytContentView.lytNeed.lytTrendsHighlight
        if (percent2 == null) {
            singleBind2.root.invisible()
        } else {
            singleBind2.root.visible()
            if (percent2 > 0) {
                singleBind2.apply {
                    tvRangeValue.text =
                        "${percent2}% ${getRangeText()}"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_up
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(0)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            } else if (percent2 == 0) {
                singleBind2.apply {
                    tvRangeValue.text =
                        "${percent2}% ${getRangeText()}"
                    ivTick.setImageResource(
                        0
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(1)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            } else {
                singleBind2.apply {
                    tvRangeValue.text =
                        "${abs(percent2)}% ${getRangeText()}"
                    ivTick.setImageResource(
                        R.drawable.ic_trend_down
                    )
                    val (bgColor, textColor) = viewModel.getHighlightBackType(2)
                    bgImage.setBackgroundResource(bgColor)
                    tvRangeValue.setTextColor(textColor)
                }
            }
        }
    }

    fun getRangeText(): String {
        return when (viewModel.selectedPeriod.value) {
            InternalSelectedPeriod.DAILY -> ""
            InternalSelectedPeriod.DAY -> "from yesterday"
            InternalSelectedPeriod.WEEK -> "from last 6 week"
            InternalSelectedPeriod.MONTH -> "from last 6 month"
            null -> ""
        }
    }

    private fun setRecycler() {
        with(binding.lytLearnMore.rvLearnMode) {
            isNestedScrollingEnabled = false
            adapter = learnMoreAdapter
        }
        learnMoreAdapter.setData(viewModel.getLearnMoreData())
    }


    private fun setPeriodUiState(state: InternalSelectedPeriod) {
        binding.lytSelector.tvDaily.setBackgroundResource(0)
        binding.lytSelector.tvDaily.setTextColor(resources.getColor(R.color.white_40))
        binding.lytSelector.tvDay.setBackgroundResource(0)
        binding.lytSelector.tvDay.setTextColor(resources.getColor(R.color.white_40))
        binding.lytSelector.tvWeek.setBackgroundResource(0)
        binding.lytSelector.tvWeek.setTextColor(resources.getColor(R.color.white_40))
        binding.lytSelector.tvMonth.setBackgroundResource(0)
        binding.lytSelector.tvMonth.setTextColor(resources.getColor(R.color.white_40))

        when (state) {
            InternalSelectedPeriod.DAILY -> {
                binding.lytSelector.tvDaily.setBackgroundResource(R.drawable.back_modal)
                binding.lytSelector.tvDaily.setTextColor(resources.getColor(R.color.white))
            }

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
    SLEEP_TIME("sleep_time"),
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

