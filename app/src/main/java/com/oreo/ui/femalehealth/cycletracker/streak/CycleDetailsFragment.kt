package com.oreo.ui.femalehealth.cycletracker.streak

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CalenderCycleTrackerDayBinding
import com.noisefit.luna.databinding.FragmentCycleDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.ui.femalehealth.cycletracker.DayState
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CycleDetailsFragment :
    BaseFragment<FragmentCycleDetailsBinding>(FragmentCycleDetailsBinding::inflate) {
    private val viewModel: CycleTrackerStreakViewModel by viewModels()
    val args: CycleDetailsFragmentArgs by navArgs()

    companion object {
        fun getStartData(cycle: FMHCycleHistoryDataModel): Pair<Int, Bundle?> {
            return Pair(R.id.cycleDetailsFragment, Bundle().apply {
                this.putParcelable(
                    "cycle", cycle
                )
            })
        }
    }

    private val symptomsAdapter: CTStreakSymptomsAdapter by lazy {
        CTStreakSymptomsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.cycleData = args.cycle
        setUI()
        setRecycler()
        initCalender()
        viewModel.cycleData.periodDate?.let {
            viewModel.selectedDate.value = LocalDate.parse(it)
        }
    }

    private fun setUI() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_cycle_details)

        binding.lytToolbar.view1.gone()
        binding.lytToolbar.ivAddFriend.invisible()
        binding.lytToolbar.view1.loadImage(
            binding.lytToolbar.view1.context,
            R.drawable.ic_ct_streak_info
        )

        binding.lytInsight.lytCycleLength.apply {
            ivMore.gone()
            tvHeader.text = getString(R.string.text_cycle_length)
            tvValue.text = "${viewModel.cycleData.cycleLength}"
            tvUnit.text = "days"
            with(viewModel.isCycleLengthNormal(viewModel.cycleData.cycleLength ?: 0)) {
                tvStatus.text = if (this) "Normal" else "Abnormal"
                ivState.setImageResource(if (this) R.drawable.ic_fmh_normal else R.drawable.ic_fmh_abnormal)
            }

        }

        binding.lytInsight.lytPeriodLength.apply {
            ivMore.gone()

            tvHeader.text = getString(R.string.text_period_duration)
            tvValue.text = "${viewModel.cycleData.periodLength}"
            tvUnit.text = "days"
            with(viewModel.isPeriodLengthNormal(viewModel.cycleData.periodLength ?: 0)) {
                tvStatus.text = if (this) "Normal" else "Abnormal"
                ivState.setImageResource(if (this) R.drawable.ic_fmh_normal else R.drawable.ic_fmh_abnormal)
            }

        }
    }

    private fun setRecycler() {
        with(binding.lytSymptomsRecord.rvSymptomsRecord) {
            adapter = symptomsAdapter
        }
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {
        viewModel.symptomList.observe(this) {
            binding.lytSymptomsRecord.root.setVisibilityByCondition(it.isNotEmpty())
            symptomsAdapter.setData(it)
        }

        viewModel.selectedDate.observe(this) {
            try {
                binding.lytTopCalender.vCalendar.weekCalender.notifyDateChanged(it)
            } catch (exp: Exception) {
            }
            viewModel.getDataForDate(it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
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


        }
        viewModel.notifyDateChange.observe(this) {
            it.getContent()?.let {
                try {
                    binding.lytTopCalender.vCalendar.weekCalender.notifyDateChanged(
                        it
                    )
                } catch (exp: Exception) {
                }
            }
        }
    }

    private fun initCalender() {
        val start = viewModel.cycleData.getCycleStart()
        val end = viewModel.cycleData.getCycleEnd()

        binding.lytTopCalender.tvDateRangeValue.text =
            "${
                start.format(DateTimeFormatter.ofPattern("dd MMM"))
            } - ${
                end.format(
                    DateTimeFormatter.ofPattern("dd MMM")
                )
            }"

        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = CalenderCycleTrackerDayBinding.bind(view)
            lateinit var day: WeekDay
            val dateToday = LocalDate.now()

            init {
                view.setOnClickListener {
                    if (viewModel.selectedDate.value != day.date) {
                        if (day.date < start || day.date > end) {
                            return@setOnClickListener
                        }
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



        binding.lytTopCalender.vCalendar.weekCalender.setup(
            start,
            end,
            DayOfWeek.MONDAY,
        )
        binding.lytTopCalender.vCalendar.weekCalender.scrollToDate(start)
    }


}