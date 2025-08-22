package com.oreo.ui.calendar

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import com.kizitonwose.calendar.view.ViewContainer
import com.noisefit.luna.databinding.BottomSheetCalendarBinding
import com.noisefit.luna.databinding.CalendarDayBinding
import com.noisefit.luna.databinding.CalendarHeaderNewBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale


const val SELECTED_DATE = "SELECTED_DATE"

@AndroidEntryPoint
class BottomSheetCalendar :
    BaseBottomSheetWithTransparent<BottomSheetCalendarBinding>(
        BottomSheetCalendarBinding::inflate
    ) {

    private var selectedDate: String? = null
    private val viewModel: HealthCalendarViewModel by viewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedDate = arguments?.getString("selectedDate")
        viewModel.registerDate = mainViewModel.registerDate

        viewModel.launchedFrom = arguments?.getString("launchedFrom")

        viewModel.startDate = viewModel.endDate.minusMonths(2).with(TemporalAdjusters.firstDayOfMonth())
        if(viewModel.launchedFrom.equals("timeline")){
            hideLegends()
        }else {
            viewModel.getCalendarData(viewModel.startDate.toString(), viewModel.endDate.toString())
        }
        initCalendar()

    }


    override fun initListener() {


    }

    override fun subscribeObservers() {
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.datesToUpdate.observe(this) {
            it.getContent()?.let {
                it.forEach { date ->
                    binding.calendar.notifyDateChanged(date)
                }
            }
        }
    }

    private fun hideLegends(){
        binding.shapeableImageView5.invisible()
        binding.textView123.invisible()
        binding.shapeableImageView6.invisible()
        binding.textView124.invisible()
        binding.shapeableImageView9.invisible()
        binding.textView126.invisible()
    }

    private fun initCalendar() {

        val currentMonth = YearMonth.now()
        val todayDate = LocalDate.now()

        val selectedLocalDate = /*if (selectedDate.isNullOrEmpty()) {*/
            LocalDate.now()
        /*} else {
            val parsedDate = LocalDate.parse(
                selectedDate,
                DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
            )
            parsedDate
        }*/

        val daysOfWeek = daysOfWeekFromLocale()

        val startMonth = viewModel.getCalendarStartDate()

        binding.calendar.setup(
            startMonth,
            currentMonth.plusMonths(0), daysOfWeek.first()
        )
        val userStartDate = viewModel.getUserStartDate()

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDayBinding.bind(view)

            init {
                binding.root.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (day.date <= LocalDate.now() && day.date >= userStartDate) {
                            setFragmentResult(
                                SELECTED_DATE,
                                bundleOf("selected_date" to "${day.date}")
                            )
                            navigateUpSafe()
                        }

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

                if (day.position == DayPosition.MonthDate) {
                    dayLayoutMain.visible()

                    when (day.date) {
                        todayDate -> {
                            container.binding.dayBack.visible()
                        }

                        else -> {
                            container.binding.dayBack.invisible()
                        }
                    }

                    if (isSunday(day.date)) {
                        container.binding.tvDay.setTextColor(Color.parseColor("#CCff2c52"))
                    }

                    val status = viewModel.getStatusByDate(day.date)
                    if (status.equals("optimal", true)) {
                        container.binding.tvDay.setTextColor(Color.GREEN/*Color.parseColor("#00ff66")*/)
                    } else if (status.equals("warning", true)) {
                        container.binding.tvDay.setTextColor(Color.RED/*Color.parseColor("#ff557e")*/)
                    } else if (status.equals("good", true) || status.equals("fair", true)) {
                        container.binding.tvDay.setTextColor(Color.parseColor("#ffffff"))
                    } else {
                        container.binding.tvDay.setTextColor(Color.parseColor("#66ffffff"))
                    }

                    if (isAfterToday(day.date)) {
                        container.binding.tvDay.setTextColor(Color.parseColor("#66ffffff"))
                    }
                } else {
                    dayLayoutMain.invisible()
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = CalendarHeaderNewBinding.bind(view).exTwoHeaderText
        }
        binding.calendar.monthHeaderBinder = object :
            MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                @SuppressLint("SetTextI18n") // Concatenation warning for `setText` call.
                container.textView.text =
                    "${
                        month.yearMonth.month.name.lowercase().capitalizeWords()
                    } ${month.yearMonth.year}"
            }
        }

        binding.calendar.scrollToDate(selectedLocalDate)

        binding.calendar.monthScrollListener = object : MonthScrollListener {
            override fun invoke(p1: CalendarMonth) {
                viewModel.checkAndLoadMoreData(p1)
            }

        }

    }

    private fun isAfterToday(date: LocalDate): Boolean {
        return date > LocalDate.now()
    }

    fun isSunday(date: LocalDate): Boolean {
        val day = DayOfWeek.of(date.get(ChronoField.DAY_OF_WEEK))
        return day == DayOfWeek.SUNDAY
    }

    fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(DateFormats.defaultLocale).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = true
                isCancelable = true
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }


}