package com.oreo.ui.femalehealth.cycletracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CalendarDayFmhOnboardBinding
import com.noisefit.luna.databinding.FragmentCycleLogBinding
import com.noisefit.luna.databinding.LayoutCycleLogCalHeaderBinding
import com.noisefit.ui.dashboard.graphs.HistoryCalendarActivity
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.StringUtils.capitalizeWords
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
                    if (day.position == DayPosition.MonthDate) {

                    }
                }
            }

        }

        binding.calendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.binding.tvDay
                val dayLayoutMain = container.binding.dayLayoutMain
                textView.text = day.date.dayOfMonth.toString()
            }
        }
        class MonthViewContainer(view: View) : com.kizitonwose.calendar.view.ViewContainer(view) {
            val textView = LayoutCycleLogCalHeaderBinding.bind(view).exTwoHeaderText
        }
        binding.calendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    container.textView.text = "${data.yearMonth.month.name.lowercase().capitalizeWords()} ${data.yearMonth.year}"
                }
            }
        binding.calendar.scrollToDate(currentDay)
    }
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val selectedDate =
                    data?.getStringExtra("selected_date") ?: return@registerForActivityResult

                mViewModel.onCalendarDateSelected(selectedDate)

                LOGS.d("moveToPosition Selected Date  :${selectedDate}")
            }
        }

    override fun initListener() {
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.invisible()
        binding.lytToolbar.view1.loadImage(binding.lytToolbar.view1.context, R.drawable.ic_log_settings)
        binding.lytToolbar.tvTitle.text=getString(R.string.text_calender)

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnLog.setOnClickListener {
            setFragmentResultListener(
                CYCLE_LOG_SAVE
            ) { _, bundle ->
                val agree = bundle.getBoolean("agree")
                if (agree) {
                //
                }
                else{
                        resultLauncher.launch(
                            LogPeriodActivity.getStartIntent(
                                requireContext(),
                            )
                        )

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