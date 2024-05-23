package com.oreo.ui.femalehealth.cycletracker.streak

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CalenderCycleTrackerDayBinding
import com.noisefit.luna.databinding.FragmentCycleTrackerStreaksBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.ui.femalehealth.cycletracker.DayState
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CycleTrackerStreaksFragment :
    BaseFragment<FragmentCycleTrackerStreaksBinding>(FragmentCycleTrackerStreaksBinding::inflate) {
    private val viewModel: CycleTrackerStreakViewModel by viewModels()
    val args: CycleTrackerStreaksFragmentArgs by navArgs()

    private val symptomsAdapter: CTStreakSymptomsAdapter by lazy {
        CTStreakSymptomsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.id = args.id
        setUI()
        setRecycler()
        viewModel.getStreakInfoData()
    }

    private fun setUI() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_cycle_details)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.invisible()
        binding.lytToolbar.view1.loadImage(
            binding.lytToolbar.view1.context,
            R.drawable.ic_ct_streak_info
        )
    }

    private fun setRecycler() {
        with(binding.lytSymptomsRecord.rvSymptomsRecord) {
            adapter = symptomsAdapter
        }
        symptomsAdapter.setData(viewModel.getSymptomsData())
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {
        viewModel.selectedDate.observe(this) {
            try {
                binding.lytTopCalender.vCalendar.weekCalender.notifyDateChanged(it)
            } catch (exp: Exception) {
            }
            binding.lytTopCalender.tvDateRangeValue.text =
                it.format(DateTimeFormatter.ofPattern("MMM"))

            viewModel.getDataForDate(it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        }

        viewModel.cycleStreakData.observe(this) {
            initCalender()
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
        viewModel.femaleHealthData.observe(this) {
            if (it.currentDay == null) {
                context.showShortToast("Screen pending")
            } else {
                //update UI
            }

        }
        viewModel.notifyDateChange.observe(this) {
            it.getContent()?.let {
                try {
                    binding.lytTopCalender.vCalendar.weekCalender.notifyDateChanged(
                        it
                    )
                    LOGS.d("sdjfhksjdfhk old date $it")
                } catch (exp: Exception) {
                }
            }
        }
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
                        bind.ivBackPeriod.setImageResource(R.drawable.back_period_day)
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

                if (day.date > dateToday) {
                    bind.ivBackPeriod.alpha = 0.5f
                } else {
                    bind.ivBackPeriod.alpha = 1f
                }
            }
        }

        binding.lytTopCalender.vCalendar.weekCalender.weekScrollListener = { weekDays ->
            viewModel.onWeekScrolled(weekDays.days.get(0).date)

            /* val selectedWeekDate = weekDays.days.get(0).date
             viewModel.getDataForDate(selectedWeekDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))*/
        }

        binding.lytTopCalender.vCalendar.weekCalender.dayBinder =
            object : WeekDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
            }

        val currentMonth = YearMonth.now()
        binding.lytTopCalender.vCalendar.weekCalender.setup(
            currentMonth.minusMonths(5).atStartOfMonth(),
            currentMonth.plusMonths(5).atEndOfMonth(),
            DayOfWeek.MONDAY,
        )
        binding.lytTopCalender.vCalendar.weekCalender.scrollToDate(LocalDate.now())
    }


}