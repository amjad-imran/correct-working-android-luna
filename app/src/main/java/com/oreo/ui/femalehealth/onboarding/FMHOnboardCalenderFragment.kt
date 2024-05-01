package com.oreo.ui.femalehealth.onboarding

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthScrollListener
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import com.noisefit.luna.databinding.CalendarDayStreakBinding
import com.noisefit.luna.databinding.FragmentFMHOnboardCalenderBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DateFormats.daysOfWeekFromLocale
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class FMHOnboardCalenderFragment :
    BaseFragment<FragmentFMHOnboardCalenderBinding>(FragmentFMHOnboardCalenderBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCalender()
    }

    private fun initCalender() {
        val currentDay = LocalDate.parse(DateFormats.getCurrentDate(DateFormats.dateFormat3))
        val currentMonth = YearMonth.now()
        val calendarStart = LocalDate.parse("2023-03-01")
        val daysOfWeek = daysOfWeekFromLocale()

        binding.lytCalender.calendar.setup(
            calendarStart.yearMonth, currentMonth.plusMonths(1), daysOfWeek.first()
        )
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDayStreakBinding.bind(view)

        }

        binding.lytCalender.calendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.binding.tvDay
                val dayLayoutMain = container.binding.dayLayoutMain
                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    when (day.date) {
                        currentDay -> {
                            container.binding.tvDay.setTextColor(Color.parseColor("#ffffff"))
                        }
                    }

                } else {
                    dayLayoutMain.invisible()
                }
            }
        }

        binding.lytCalender.calendar.monthScrollListener = object : MonthScrollListener {
            override fun invoke(month: CalendarMonth) {

                nullableBinding?.lytCalender?.tvMonth?.text =
                    "${month.yearMonth.month.name.lowercase().capitalizeWords()} ${month.year}"
            }
        }

        binding.lytCalender.calendar.scrollToDate(currentDay)
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}