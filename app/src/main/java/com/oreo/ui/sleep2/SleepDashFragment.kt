package com.oreo.ui.sleep2

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.moengage.core.internal.utils.getRandomInt
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CalenderCycleTrackerDayBinding
import com.noisefit.luna.databinding.CalenderSleepDayBinding
import com.noisefit.luna.databinding.FragmentSleepDashBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.ui.femalehealth.cycletracker.DayState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class SleepDashFragment :
    BaseFragment<FragmentSleepDashBinding>(FragmentSleepDashBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCalender()

        initTempUi()

    }

    private fun initTempUi() {
        binding.lytScore.circularProgressBar.setProgress(80)

        binding.lytSleepTrends.lytSleepPerformance.graphPerformance.setDataSet(
            arrayListOf(
                20,
                30,
                null,
                50,
                100,
                70,
                null
            ),
            4
        )

        binding.lytSleepTrends.lytHourVsNeed.graphHourVsNeed.setDataSet(
            arrayListOf(
                Pair(60, 100),
                Pair(null, null),
                Pair(90, 100),
                Pair(null, null),
                Pair(120, 130),
                Pair(150, 180),
                Pair(180, 200),
            ),
            4
        )
        binding.lytSleepTrends.lytRestorativeSleep.graphRestorative.setDataSet(
            arrayListOf(
                Pair(60, 40),
                Pair(80, 50),
                Pair(90, 60),
                Pair(100, 40),
                Pair(120, 20),
                Pair(150, 10),
                Pair(180, 0),
            ),
            4
        )
    }

    private fun initCalender() {

        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = CalenderSleepDayBinding.bind(view)
            lateinit var day: WeekDay
            val dateToday = LocalDate.now()

            init {
                view.setOnClickListener {
                    /*if (viewModel.selectedDate.value != day.date) {
                        viewModel.updateSelectedDate(day.date)
                    }*/
                }
            }

            fun bind(day: WeekDay) {
                this.day = day

                bind.exSevenDateText.text =
                    DateFormats.getDayFromDate(DateFormats.convertLocalDateToDate(day.date))
                bind.exSevenDayText.text =
                    DateFormats.getDayString(DateFormats.convertLocalDateToDate(day.date))

                bind.circularProgressBar.setProgress(getRandomInt(20, 100))

            }
        }

        binding.vCalendar.dayBinder =
            object : WeekDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
            }

        val currentMonth = YearMonth.now()

        binding.vCalendar.setup(
            currentMonth.atStartOfMonth(),
            currentMonth.atEndOfMonth(),
            DayOfWeek.MONDAY,
        )
        binding.vCalendar.scrollToDate(
            LocalDate.now()
        )
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}