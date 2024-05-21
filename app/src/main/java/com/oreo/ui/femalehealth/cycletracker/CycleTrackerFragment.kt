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
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CalenderCycleTrackerDayBinding
import com.noisefit.luna.databinding.FragmentCycleTrackerBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.model.health.Nudges
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.workout.details.WorkoutNudgeFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class CycleTrackerFragment :
    BaseFragment<FragmentCycleTrackerBinding>(FragmentCycleTrackerBinding::inflate) {
    private val viewModel: CycleTrackerViewModel by viewModels()

    private val cycleHistoryAdapter by lazy {
        FMHCycleHistoryAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
    }

    private fun initCalender() {
        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = CalenderCycleTrackerDayBinding.bind(view)
            lateinit var day: WeekDay

            init {
                view.setOnClickListener {
                    if (viewModel.selectedDate.value != day.date) {
                        /*val oldDate = viewModel.selectedDate.value

                        oldDate?.let {
                            binding.lytTrackerTop.vCalendar.weekCalender.notifyDateChanged(
                                it
                            )
                        }*/

                        viewModel.updateSelectedDate(day.date)

                        /* viewModel.getDataForDate(
                             viewModel.selectedDate.format(
                                 DateTimeFormatter.ofPattern(
                                     "yyyy-MM-dd"
                                 )
                             )
                         )*/
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
                    DayState.FERTILE -> {
                        bind.ivBackPeriod.gone()
                        bind.exSevenDateText.setTextColor(
                            ContextCompat.getColor(
                                bind.exSevenDayText.context, R.color.color_ovulation
                            )
                        )
                    }

                    DayState.OVULATION_DAY -> {
                        bind.ivBackPeriod.setImageResource(R.drawable.back_circle_fertile)
                        bind.ivBackPeriod.visible()
                        bind.exSevenDateText.setTextColor(
                            ContextCompat.getColor(
                                bind.exSevenDayText.context, R.color.color_ovulation
                            )
                        )
                    }

                    DayState.PERIOD -> {
                        bind.ivBackPeriod.setImageResource(R.drawable.back_circle_period)
                        bind.ivBackPeriod.visible()
                        bind.exSevenDateText.setTextColor(
                            ContextCompat.getColor(
                                bind.exSevenDayText.context, R.color.white
                            )
                        )
                    }

                    DayState.DEFAULT -> {
                        bind.ivBackPeriod.gone()
                        bind.exSevenDateText.setTextColor(
                            ContextCompat.getColor(
                                bind.exSevenDayText.context, R.color.white
                            )
                        )
                    }
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
            firstDayOfWeekFromLocale(),
        )
        binding.lytTrackerTop.vCalendar.weekCalender.scrollToDate(LocalDate.now())

    }

    private fun setRecycler() {
        with(binding.lytCycleHistory.lytCycleList.rvHistory) {
            adapter = cycleHistoryAdapter
        }
    }

    override fun initListener() {
        binding.toolbar.view1.visible()
        binding.toolbar.ivAddFriend.invisible()
        binding.toolbar.view1.loadImage(binding.toolbar.view1.context, R.drawable.ic_ct_calender)
        binding.toolbar.tvTitle.text = getString(R.string.text_cycle_tracker)

        binding.lytTrackerTop.tvPhase.setOnClickListener {
            if (binding.lytTrackerTop.tvPhase.text.equals("Luteal phase")) {
                setFragmentResultListener(NOTIFY_LOG) { _, bundle ->
                }
                navigate(
                    R.id.dialogComingSoon
                )
            }
        }
        binding.lytTrackerTop.ivInfo.setOnClickListener {
            if (binding.lytTrackerTop.tvPhase.text.equals("Luteal phase")) {
                setFragmentResultListener(NOTIFY_LOG) { _, bundle ->
                }
                navigate(
                    R.id.dialogComingSoon
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
        binding.lytTrackerTop.btnLog.setOnClickListener {
            navigate(R.id.cycleLogFragment)
        }
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
                    LOGS.d("sdjfhksjdfhk old date $it")
                } catch (exp: Exception) {
                }
            }
        }

        viewModel.selectedDate.observe(this) {
            try {
                binding.lytTrackerTop.vCalendar.weekCalender.notifyDateChanged(it)
                LOGS.d("sdjfhksjdfhk new date $it")
            } catch (exp: Exception) {
            }
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
        viewModel.femaleHealthData.observe(this) {
            initInsightUI(it)
            setNudgesViewPager(it.nudges)
            setTopData(it)

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

    /*{
       "ota_log": false,
       "isPeriod": false,
       "isOvulation": false,
       "isFertileWindow": true,
       "period_date": "2024-05-05",
       "ovulation_date": "2024-05-19",
       "fertile_window": [
       "2024-05-14",
       "2024-05-20"
       ],
       "next_period_date": "2024-06-02",
       "nudges": [
       {
           "label": "",
           "message": ""
       }
       ],
       "current_day": 13,
       "period_length": 5,
       "pregency_chances": "high",
       "cycle_length": 28
   }*/

    private fun setTopData(data: FemaleHealthUserInfoModel) {
        binding.lytTrackerTop.apply {
            val selectedDate = if (viewModel.selectedDate.value == null) {
                LocalDate.now()
            } else {
                viewModel.selectedDate.value!!
            }.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))


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