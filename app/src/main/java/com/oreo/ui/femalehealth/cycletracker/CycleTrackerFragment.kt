package com.oreo.ui.femalehealth.cycletracker

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CalenderCycleTrackerDayBinding
import com.noisefit.luna.databinding.FragmentCycleTrackerBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class CycleTrackerFragment :
    BaseFragment<FragmentCycleTrackerBinding>(FragmentCycleTrackerBinding::inflate) {
    private val mViewModel: CycleTrackerViewModel by viewModels()

    private var selectedDate = LocalDate.now()
    private val cycleHistoryAdapter by lazy {
        FMHCycleHistoryAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCalender()
        setRecycler()

    }

    private fun initCalender() {
        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = CalenderCycleTrackerDayBinding.bind(view)
            lateinit var day: WeekDay

            init {
                view.setOnClickListener {
                    if (selectedDate != day.date) {
                        val oldDate = selectedDate
                        selectedDate = day.date
                        binding.lytTrackerTop.vCalendar.weekCalender.notifyDateChanged(day.date)
                        oldDate?.let {
                            binding.lytTrackerTop.vCalendar.weekCalender.notifyDateChanged(
                                it
                            )
                        }
                    }
                }
            }

            fun bind(day: WeekDay) {
                this.day = day

                bind.exSevenDateText.text =
                    DateFormats.getDayFromDate(DateFormats.convertLocalDateToDate(day.date))
                bind.exSevenDayText.text =
                    DateFormats.getDayString(DateFormats.convertLocalDateToDate(day.date))

                val colorRes = if (day.date == selectedDate) {
                    ContextCompat.getColor(bind.exSevenDayText.context, R.color.carolina_blue)
                } else {
                    ContextCompat.getColor(bind.exSevenDayText.context, R.color.white)
                }
                bind.exSevenDateText.setTextColor(colorRes)
            }
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
        cycleHistoryAdapter.setData(mViewModel.getCycleHistoryData())
    }

    override fun initListener() {
        initInsightUI()
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytPrediction.ivMore.setOnClickListener {
            navigate(R.id.cycleSkinTemperature)
        }
        binding.lytInsight.lytCycleLength.root.setOnClickListener {
            navigate(R.id.cycleInsightDetails, Bundle().apply {
                this.putString("pageTitle", getString(R.string.text_cycle_length))
            })
        }
        binding.lytInsight.lytPeriodLength.root.setOnClickListener {
            navigate(R.id.cycleInsightDetails, Bundle().apply {
                this.putString("pageTitle", getString(R.string.text_period_duration))
            })
        }
        binding.lytCycleHistory.ivMore.setOnClickListener {
            navigate(R.id.cycleTrackorHistory)
        }
    }

    private fun initInsightUI() {
        binding.lytInsight.lytCycleLength.tvHeader.text = getString(R.string.text_cycle_length)
        binding.lytInsight.lytCycleLength.tvValue.text = "37"
        binding.lytInsight.lytCycleLength.tvUnit.text = "days"
        binding.lytInsight.lytCycleLength.tvStatus.text = "Abnormal"
        binding.lytInsight.lytPeriodLength.tvHeader.text = getString(R.string.text_period_duration)
        binding.lytInsight.lytPeriodLength.tvValue.text = "6"
        binding.lytInsight.lytPeriodLength.tvUnit.text = "days"
        binding.lytInsight.lytPeriodLength.tvStatus.text = "Normal"


    }

    override fun subscribeObservers() {

    }


}