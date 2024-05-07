package com.oreo.ui.femalehealth.onboarding

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import com.kizitonwose.calendar.view.ViewContainer
import com.noisefit.luna.databinding.CalendarDayFmhOnboardBinding
import com.noisefit.luna.databinding.FragmentFMHOnboardCalenderBinding
import com.noisefit_commans.common.ContinuousSelectionHelper.getSelection
import com.noisefit_commans.common.DateSelection
import com.noisefit_commans.common.yearMonth
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DateFormats.daysOfWeekFromLocale
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class FMHOnboardCalenderFragment :
    BaseFragment<FragmentFMHOnboardCalenderBinding>(FragmentFMHOnboardCalenderBinding::inflate) {
    private var currentSelectedMonth: CalendarMonth? = null
    private val mViewModel: FMHOnboardingViewModel by activityViewModels()
    private val TAG="calenderFragment"

    private var selection = DateSelection()
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
            val binding = CalendarDayFmhOnboardBinding.bind(view)

            init {

                binding.root.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (mViewModel.selectedEndDate == null)
                            mViewModel.selectedEndDate =
                                DateFormats.convertDateToLocalDate(DateFormats.getDaysAgo(mViewModel.calenderDayRange()))
                        LOGS.d("start end date ${mViewModel.selectedEndDate}")

                        selection = getSelection(
                            clickedDate = day.date,
                            dateSelection = selection,
                            selectionStartDate = day.date,
                            selectionEndDate = mViewModel.selectedEndDate!!
                        )
                        LOGS.d("start selection ${Gson().toJson(selection)}")
                        this@FMHOnboardCalenderFragment.binding.lytCalender.calendar.notifyCalendarChanged()
//                        LOGS.d("Selected Date ${day.date}")
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
                val (startDate, endDate) = selection

                LOGS.d("start date $startDate")
                LOGS.d("start end date $endDate")
                if (day.position == DayPosition.MonthDate) {
                    when (day.date) {
                        startDate -> {
                            container.binding.tvDay.setTextColor(Color.parseColor("#ffffff"))
                            container.binding.ivBackStart.visible()
                        }

                        endDate -> {
                            container.binding.tvDay.setTextColor(Color.parseColor("#ffffff"))
                            container.binding.ivBackStart.visible()
                        }
                    }

                } else {
                    dayLayoutMain.invisible()
                }
            }
        }

        binding.lytCalender.calendar.monthScrollListener = object : MonthScrollListener {
            override fun invoke(month: CalendarMonth) {
                currentSelectedMonth = month
                nullableBinding?.lytCalender?.tvMonth?.text =
                    "${month.yearMonth.month.name.lowercase().capitalizeWords()} ${month.yearMonth.year}"
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