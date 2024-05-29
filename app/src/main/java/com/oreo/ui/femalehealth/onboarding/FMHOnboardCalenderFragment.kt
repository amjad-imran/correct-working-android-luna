package com.oreo.ui.femalehealth.onboarding

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import com.kizitonwose.calendar.view.ViewContainer
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CalendarDayFmhOnboardBinding
import com.noisefit.luna.databinding.FragmentFMHOnboardCalenderBinding
import com.noisefit_commans.common.yearMonth
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DateFormats.daysOfWeekFromLocale
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class FMHOnboardCalenderFragment :
    BaseFragment<FragmentFMHOnboardCalenderBinding>(FragmentFMHOnboardCalenderBinding::inflate) {
    private var currentSelectedMonth: CalendarMonth? = null
    private val mViewModel: FMHOnboardingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        mViewModel.calculateStartAndEndPeriodDates()
        initCalender()
    }

    private fun initCalender() {
        val currentDay = LocalDate.parse(DateFormats.getCurrentDate(DateFormats.dateFormat3()))
        val currentMonth = YearMonth.now()
        val calendarStart = LocalDate.parse("2024-01-01")

        binding.lytCalender.calendar.setup(
            calendarStart.yearMonth, currentMonth, DayOfWeek.MONDAY
        )
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDayFmhOnboardBinding.bind(view)

            init {

                binding.root.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (!day.date.isAfter(LocalDate.now())) {
                            this@FMHOnboardCalenderFragment.binding.lytBottomControls.bNext.enable()

                            mViewModel.selectedPStartDate = day.date

                            mViewModel.selectedPEndDate =
                                mViewModel.calculatePeriodEndDate(day.date)

                            this@FMHOnboardCalenderFragment.binding.lytCalender.calendar.notifyCalendarChanged()
                        }
                    }
                }
            }

        }

        binding.lytCalender.calendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.binding.tvDay
                val dayLayoutMain = container.binding.dayLayoutMain
                textView.text = day.date.dayOfMonth.toString()

                if (day.position == DayPosition.MonthDate) {
                    dayLayoutMain.visible()
                    val currentDate = day.date

                    val startDate = mViewModel.selectedPStartDate
                    val endDate = mViewModel.selectedPEndDate

                    if (startDate != null && endDate != null) {
                        if (currentDate == startDate && currentDate == endDate) {
                            container.binding.ivBackSingle.visible()
                            container.binding.ivBackStart.gone()
                            container.binding.ivBackEnd.gone()
                            container.binding.ivBackMid.gone()
                        } else if (currentDate == startDate) {
                            container.binding.ivBackStart.visible()
                            container.binding.ivBackSingle.gone()
                            container.binding.ivBackEnd.gone()
                            container.binding.ivBackMid.gone()
                        } else if (currentDate == endDate) {
                            container.binding.ivBackSingle.gone()
                            container.binding.ivBackStart.gone()
                            container.binding.ivBackEnd.visible()
                            container.binding.ivBackMid.gone()
                        } else if (currentDate.isBefore(endDate) && currentDate.isAfter(
                                startDate
                            )
                        ) {
                            container.binding.ivBackSingle.gone()
                            container.binding.ivBackStart.gone()
                            container.binding.ivBackEnd.gone()
                            container.binding.ivBackMid.visible()
                        } else {
                            hideAllBack(container.binding)
                        }
                    } else {
                        hideAllBack(container.binding)
                    }
                    if (day.date.isAfter(LocalDate.now())) {
                        textView.setTextColor(
                            ContextCompat.getColor(
                                textView.context,
                                R.color.color_future_dates
                            )
                        )
                        container.binding.ivBackSingle.alpha = 0.5f
                        container.binding.ivBackStart.alpha = 0.5f
                        container.binding.ivBackEnd.alpha = 0.5f
                        container.binding.ivBackMid.alpha = 0.5f
                    } else {
                        container.binding.ivBackSingle.alpha = 1f
                        container.binding.ivBackStart.alpha = 1f
                        container.binding.ivBackEnd.alpha = 1f
                        container.binding.ivBackMid.alpha = 1f
                    }
                } else {
                    dayLayoutMain.invisible()
                }
            }

            private fun hideAllBack(binding: CalendarDayFmhOnboardBinding) {
                binding.ivBackSingle.gone()
                binding.ivBackStart.gone()
                binding.ivBackEnd.gone()
                binding.ivBackMid.gone()
            }
        }

        binding.lytCalender.calendar.monthScrollListener = object : MonthScrollListener {
            override fun invoke(month: CalendarMonth) {
                currentSelectedMonth = month
                nullableBinding?.lytCalender?.tvMonth?.text =
                    "${
                        month.yearMonth.month.name.lowercase().capitalizeWords()
                    } ${month.yearMonth.year}"
            }
        }

        if (mViewModel.selectedPStartDate == null) {
            binding.lytBottomControls.bNext.disable()
            binding.lytCalender.calendar.scrollToDate(currentDay)
        } else {
            binding.lytBottomControls.bNext.enable()
            binding.lytCalender.calendar.scrollToDate(mViewModel.selectedPStartDate ?: currentDay)
        }


    }

    override fun initListener() {
        binding.lytBottomControls.bNext.setOnClickListener {
            mViewModel.onNextPress.value = Event(true)
        }

        binding.lytCalender.ivArrowLeft.setOnClickListener {
            currentSelectedMonth?.let {
                binding.lytCalender.calendar.smoothScrollToMonth(it.yearMonth.minusMonths(1))
            }
        }
        binding.lytCalender.ivArrowRight.setOnClickListener {
            currentSelectedMonth?.let {
                binding.lytCalender.calendar.smoothScrollToMonth(it.yearMonth.plusMonths(1))
            }
        }

    }

    override fun subscribeObservers() {

    }

}