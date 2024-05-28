package com.oreo.ui.femalehealth.cycletracker

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CalenderCycleTrackerDayBinding
import com.noisefit.luna.databinding.FragmentCycleTrackerBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.model.femaleh.TempPrediction
import com.oreo.data.model.health.Nudges
import com.oreo.ui.femalehealth.cycletracker.history.INFO_LOG
import com.oreo.ui.femalehealth.cycletracker.insight.CycleInsightLaunchMode
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.workout.details.WorkoutNudgeFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@AndroidEntryPoint
class CycleTrackerFragment :
    BaseFragment<FragmentCycleTrackerBinding>(FragmentCycleTrackerBinding::inflate) {
    private val viewModel: CycleTrackerViewModel by viewModels()

    private val cycleHistoryAdapter by lazy {
        FMHCycleHistoryAdapter(object : OnHistoryItemClickListener {
            override fun onHistoryItemClick(data: FMHCycleHistoryDataModel, position: Int) {
                navigate(R.id.cycleTrackerStreakFragment, Bundle().apply {
                    this.putString(
                        "id",
                        "1"
                    )//id will be getting later, as discussed right now not getting id
                })
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
    }

    private fun initCalender() {
        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = CalenderCycleTrackerDayBinding.bind(view)
            lateinit var day: WeekDay
            val dateToday = LocalDate.now()

            init {
                view.setOnClickListener {
                    if (viewModel.selectedDate.value != day.date) {
                        viewModel.updateSelectedDate(day.date)
                    }
                }
            }

            fun bind(day: WeekDay) {
                this.day = day

                bind.exSevenDateText.text =
                    DateFormats.getDayFromDate(DateFormats.convertLocalDateToDate(day.date))
                bind.exSevenDayText.text =
                    DateFormats.getDayString(DateFormats.convertLocalDateToDate(day.date))

                val (state, isDateSelected) = viewModel.getCurrentState(day.date)

                if (isDateSelected) {
                    bind.ivBackSelected.visible()
                } else {
                    bind.ivBackSelected.gone()
                }

                when (state) {
                    DayState.Fertile -> {
                        bind.ivBackPeriod.gone()
                        bind.exSevenDateText.setTextColor(
                            ContextCompat.getColor(
                                bind.exSevenDayText.context, R.color.color_ovulation
                            )
                        )
                    }

                    DayState.OvulationDay -> {
                        bind.ivBackPeriod.setImageResource(R.drawable.back_circle_fertile)
                        bind.ivBackPeriod.visible()
                        bind.exSevenDateText.setTextColor(
                            ContextCompat.getColor(
                                bind.exSevenDayText.context, R.color.color_ovulation
                            )
                        )
                    }

                    is DayState.Period -> {
                        bind.ivBackPeriod.setImageResource(R.drawable.back_period_day)
                        bind.ivBackPeriod.visible()
                        bind.exSevenDateText.setTextColor(
                            ContextCompat.getColor(
                                bind.exSevenDayText.context, R.color.white
                            )
                        )
                    }

                    DayState.Default -> {
                        bind.ivBackPeriod.gone()
                        bind.exSevenDateText.setTextColor(
                            ContextCompat.getColor(
                                bind.exSevenDayText.context, R.color.white
                            )
                        )
                    }
                }

                if (day.date > dateToday) {
                    bind.ivBackPeriod.alpha = 0.5f
                } else {
                    bind.ivBackPeriod.alpha = 1f
                }
            }
        }

        binding.lytTrackerTop.vCalendar.weekCalender.weekScrollListener = { weekDays ->
            viewModel.onWeekScrolled(weekDays.days.get(0).date)

            /* val selectedWeekDate = weekDays.days.get(0).date
             viewModel.getDataForDate(selectedWeekDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))*/
        }

        binding.lytTrackerTop.vCalendar.weekCalender.dayBinder =
            object : WeekDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
            }

        val currentMonth = YearMonth.now()
        binding.lytTrackerTop.vCalendar.weekCalender.setup(
            currentMonth.minusMonths(5).atStartOfMonth(),
            currentMonth.plusMonths(5).atEndOfMonth(),
            DayOfWeek.MONDAY,
        )
        binding.lytTrackerTop.vCalendar.weekCalender.scrollToDate(LocalDate.now())

    }

    private fun setRecycler() {
        with(binding.lytCycleHistory.lytCycleList.rvHistory) {
            adapter = cycleHistoryAdapter
        }
    }

    override fun initListener() {
        binding.toolbar.viewBackCalendar.setOnClickListener {
            navigate(R.id.cycleLogFragment)
        }

        binding.lytTrackerTop.tvPhase.setOnClickListener {
            var launchMode = ""
            if (binding.lytTrackerTop.tvPhase.text.equals("Luteal phase")) {
                launchMode = "Luteal"
            } else if (binding.lytTrackerTop.tvPhase.text.equals("Follicular phase")) {
                launchMode = "Follicular"
            }
            if (launchMode.isNotEmpty()) {
                setFragmentResultListener(INFO_LOG) { _, bundle ->
                }
                navigate(
                    R.id.dialogCtOvulationInfo, Bundle().apply {
                        this.putString("launchMode", launchMode)
                    }
                )
            }
        }
        binding.lytTrackerTop.ivInfo.setOnClickListener {
            var launchMode = ""
            if (binding.lytTrackerTop.tvPhase.text.equals("Luteal phase")) {
                launchMode = "Luteal"
            } else if (binding.lytTrackerTop.tvPhase.text.equals("Follicular phase")) {
                launchMode = "Follicular"
            }
            if (launchMode.isNotEmpty()) {
                setFragmentResultListener(INFO_LOG) { _, bundle ->
                }
                navigate(
                    R.id.dialogCtOvulationInfo, Bundle().apply {
                        this.putString("launchMode", launchMode)
                    }
                )
            }
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytPrediction.ivMore.setOnClickListener {
            navigate(R.id.cycleSkinTemperature)
        }
        binding.lytInsight.lytCycleLength.root.setOnClickListener {
            navigate(R.id.cycleInsightDetails, Bundle().apply {
                this.putSerializable("launchMode", CycleInsightLaunchMode.CYCLE_LENGTH)
            })
        }
        binding.lytInsight.lytPeriodLength.root.setOnClickListener {
            navigate(R.id.cycleInsightDetails, Bundle().apply {
                this.putSerializable("launchMode", CycleInsightLaunchMode.PERIOD_DURATION)
            })
        }
        binding.lytCycleHistory.ivMore.setOnClickListener {
            navigate(R.id.cycleTrackerHistory)
        }
        /*binding.lytTrackerTop.btnLog.setOnClickListener {
            navigate(R.id.cycleLogFragment)
        }*/
    }

    private fun initInsightUI(data: FemaleHealthUserInfoModel) {

        binding.lytInsight.lytCycleLength.apply {
            tvHeader.text = getString(R.string.text_cycle_length)
            tvValue.text = "${data.cycleLength}"
            tvUnit.text = "days"
            with(viewModel.isCycleLengthNormal(data.cycleLength ?: 0)) {
                tvStatus.text = if (this) "Normal" else "Abnormal"
                ivState.setImageResource(if (this) R.drawable.ic_fmh_normal else R.drawable.ic_fmh_abnormal)
            }

        }

        binding.lytInsight.lytPeriodLength.apply {
            tvHeader.text = getString(R.string.text_period_duration)
            tvValue.text = "${data.periodLength}"
            tvUnit.text = "days"
            with(viewModel.isPeriodLengthNormal(data.periodLength ?: 0)) {
                tvStatus.text = if (this) "Normal" else "Abnormal"
                ivState.setImageResource(if (this) R.drawable.ic_fmh_normal else R.drawable.ic_fmh_abnormal)
            }

        }


    }

    override fun subscribeObservers() {
        viewModel.notifyDateChange.observe(this) {
            it.getContent()?.let {
                try {
                    binding.lytTrackerTop.vCalendar.weekCalender.notifyDateChanged(
                        it
                    )
                } catch (exp: Exception) {
                }
            }
        }

        viewModel.selectedDate.observe(this) {
            try {
                binding.lytTrackerTop.vCalendar.weekCalender.notifyDateChanged(it)
            } catch (exp: Exception) {
            }
            binding.toolbar.tvMonth.text = it.format(DateTimeFormatter.ofPattern("MMM"))

            viewModel.getDataForDate(it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        }

        viewModel.cycleHistoryData.observe(this) {
            if (it == null) {
                binding.lytCycleHistory.root.gone()
            } else {
                binding.lytCycleHistory.root.visible()
                cycleHistoryAdapter.setData(it.take(3))
            }
            initCalender()
        }
        viewModel.cyclePredictionData.observe(this) {
            if (it == null) {
                binding.dividerPrediction.root.gone()
                binding.lytPrediction.root.gone()
            } else {
                binding.dividerPrediction.root.visible()
                binding.lytPrediction.root.visible()
                initSkinTempPredictionWidget(it)
            }
        }
        viewModel.femaleHealthData.observe(this) {
            if (it.currentDay == null) {
                context.showShortToast("Screen pending")
            } else {
                initInsightUI(it)
                setNudgesViewPager(it.nudges)
                setTopData(it)
            }

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

    private fun initSkinTempPredictionWidget(data: TempPrediction) {
        binding.lytPrediction.vCard.apply {
            val variation = data.tempVariation
            if (variation == null) {
                tvValue.text = "-"
                tvUnit.gone()
            } else {
                tvValue.text = if ((data.tempVariation ?: 0f) > 0f) {
                    "+${String.format("%.1f", data.tempVariation)}"
                } else {
                    "-${String.format("%.1f", abs(data.tempVariation))}"
                }
                tvUnit.visible()
            }

            if (data.pendingNights == null) {
                tvMoreNight.text = ""
            } else {
                tvMoreNight.text = "Data for ${data.pendingNights} more nights is required"
            }

            tvDescription.text = data.message

            vTempGraph.updateData(viewModel.combineTempData(data.tempData))

        }
    }

    private fun setTopData(data: FemaleHealthUserInfoModel) {
        binding.lytTrackerTop.apply {
            val selectedDateLocal = if (viewModel.selectedDate.value == null) {
                LocalDate.now()
            } else {
                viewModel.selectedDate.value!!
            }
            val selectedDate = selectedDateLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            if (selectedDateLocal > LocalDate.now()) {
                btnLog.isEnabled = false
            } else {
                btnLog.isEnabled = true
            }


            tvCurrentDay.text = "Day ${(data.currentDay ?: 0)}"
            tvTotalDays.text = "of ${(data.cycleLength ?: 0)}"
            tvCurrentState.text = "Current state here"
            tvPregnancyChances.text = viewModel.getPregnancyText(data.pregnancyChances)
            with(
                viewModel.getCurrentPhaseText(
                    data.fertileWindowList, data.periodDate, selectedDate
                )
            ) {
                if (this == null) {
                    tvPhase.text = "-"
                } else {
                    tvPhase.text = this.first
                    tvPhase.setTextColor(tvPhase.context.getColor(this.second))
                }
            }

            if (data.isPeriod || data.isOvulation) {
                if (data.isPeriod) {
                    tvCurrentState.text = if (data.otaLog) "Period" else "Predicted period"
                    tvStateDay.text = "Day ${data.currentDay}"

                    binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_high)

                } else {
                    if (selectedDate.equals(data.ovulationDate)) {
                        tvCurrentState.text = "Predicted day of"
                        tvStateDay.text = "Ovulation"
                        binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_blue_high)
                    }
                }
            } else {
                val daysUntilOvulation =
                    viewModel.calculateDaysLeft(data.ovulationDate!!, selectedDate)
                val daysUntilNextPeriod =
                    viewModel.calculateDaysLeft(data.nextPeriodDate!!, selectedDate)

                if (daysUntilOvulation < daysUntilNextPeriod && daysUntilOvulation > 0) {
                    tvCurrentState.text = "Ovulation in"
                    tvStateDay.text = "${daysUntilOvulation} Days"
                    if (daysUntilOvulation > 3) {
                        binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_blue_low)
                    } else {
                        binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_blue_med)
                    }
                } else {
                    tvCurrentState.text = "Period in"
                    tvStateDay.text = "${daysUntilNextPeriod} Days"

                    if (daysUntilNextPeriod > 2) {
                        if (data.isFertileWindow) {
                            binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_blue_med)
                        } else {
                            binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_low)
                        }
                    } else {
                        binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_med)
                    }
                }
            }
        }
    }

    private fun setNudgesViewPager(data: List<Nudges>?) {

        if (data.isNullOrEmpty()) {
            binding.lytCues.root.gone()
            return
        } else {
            binding.lytCues.root.visible()
        }
        val fragments = ArrayList<WorkoutNudgeFragment>()
        data.forEach {
            fragments.add(WorkoutNudgeFragment.newInstance(it))
        }
        val sleepBannerAdapter = OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)
        binding.lytCues.vpBannerSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })
            adapter = sleepBannerAdapter
        }

        TabLayoutMediator(
            binding.lytCues.tabLayout, binding.lytCues.vpBannerSlider
        ) { _, _ -> }.attach()

        if (fragments.size > 1) {
            binding.lytCues.tabLayout.visible()
        } else {
            binding.lytCues.tabLayout.invisible()
        }
    }


}