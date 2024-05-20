package com.oreo.ui.femalehealth.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import com.kizitonwose.calendar.view.ViewContainer
import com.noisefit.luna.databinding.CalendarDayFmhOnboardBinding
import com.noisefit.luna.databinding.FragmentFMHOnboardCalenderBinding
import com.noisefit_commans.common.yearMonth
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DateFormats.daysOfWeekFromLocale
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import dagger.hilt.android.AndroidEntryPoint
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
        val currentDay = LocalDate.parse(DateFormats.getCurrentDate(DateFormats.dateFormat3))
        val currentMonth = YearMonth.now()
        val calendarStart = LocalDate.parse("2023-03-01")
        val daysOfWeek = daysOfWeekFromLocale()

        binding.lytCalender.calendar.setup(
            calendarStart.yearMonth, currentMonth, daysOfWeek.first()
        )
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDayFmhOnboardBinding.bind(view)

            init {

                binding.root.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        mViewModel.selectedPStartDate = day.date

                        mViewModel.selectedPEndDate =
                            mViewModel.calculatePeriodEndDate(day.date)

                        this@FMHOnboardCalenderFragment.binding.lytCalender.calendar.notifyCalendarChanged()
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
                        } else if (currentDate.isBefore(endDate) && currentDate.isAfter(startDate)) {
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

        binding.lytCalender.calendar.scrollToDate(currentDay)
    }

    override fun initListener() {
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