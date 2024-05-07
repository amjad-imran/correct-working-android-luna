package com.oreo.ui.femalehealth.cycletracker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CalendarDayFmhOnboardBinding
import com.noisefit.luna.databinding.FragmentCycleLogBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class CycleLogFragment : BaseFragment<FragmentCycleLogBinding>(FragmentCycleLogBinding::inflate) {
    private val mViewModel: CycleLogViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCalender()
    }

    private fun initCalender() {
        val currentDay = LocalDate.parse(DateFormats.getCurrentDate(DateFormats.dateFormat3))
        val currentMonth = YearMonth.now()
        val calendarStart = LocalDate.parse("2023-03-01")
        val daysOfWeek = DateFormats.daysOfWeekFromLocale()

        binding.calendar.setup(
            calendarStart.yearMonth, currentMonth.plusMonths(1), daysOfWeek.first()
        )
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDayFmhOnboardBinding.bind(view)

            init {

                binding.root.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {

                    }
                }
            }

        }

        binding.calendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.binding.tvDay
                val dayLayoutMain = container.binding.dayLayoutMain
                textView.text = day.date.dayOfMonth.toString()
            }
        }
        binding.calendar.scrollToDate(currentDay)
    }

    override fun initListener() {
        binding.btnLog.setOnClickListener {
            setFragmentResultListener(
                CYCLE_LOG_SAVE
            ) { _, bundle ->
                val agree = bundle.getBoolean("agree")
                if (agree) {
                //
                }
            }
            navigate(R.id.bottomSheetCycleLog, Bundle().apply {
                putParcelable("data", mViewModel.getCycleLogData())
            })
        }
    }

    override fun subscribeObservers() {

    }


}