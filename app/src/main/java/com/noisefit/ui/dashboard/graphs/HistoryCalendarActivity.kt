package com.noisefit.ui.dashboard.graphs

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.noisefit.luna.databinding.ActivityHistoryCalendarBinding
import com.noisefit.luna.databinding.CalendarDayBinding
import com.noisefit.luna.databinding.CalendarHeaderBinding
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.WeekFields
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HistoryCalendarActivity : BaseActivity<ActivityHistoryCalendarBinding>() {

    companion object {
        fun getStartIntent(
            context: Context,
            selectedDate: String?,
            deviceType: String
        ): Intent {
            return Intent(context, HistoryCalendarActivity::class.java).apply {
                this.putExtra("selectedDate", selectedDate)
                this.putExtra("deviceType", selectedDate)
            }
        }
    }

    private var selectedDate: String? = null
    private var deviceType: String? = null

    @Inject
    lateinit var localDataStore: DataStoredInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedDate = intent.getStringExtra("selectedDate")
        deviceType = intent.getStringExtra("deviceType")
        initCalendar()
    }


    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            finish()
        }
    }

    override fun observeSubscriber() {
    }

    private fun initCalendar() {

        val currentMonth = YearMonth.now()
        var selectedPreviousDates = ArrayList<LocalDate>()

        val selectedLocalDate = if (selectedDate.isNullOrEmpty()) {
            LocalDate.now()
        } else {
            val parsedDate = LocalDate.parse(
                selectedDate,
                DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
            )
           /* if (deviceType == "watch") {
                selectedPreviousDates = arrayListOf(
                    parsedDate.minusDays(1),
                    parsedDate.minusDays(2),
                    parsedDate.minusDays(3),
                    parsedDate.minusDays(4),
                    parsedDate.minusDays(5),
                    parsedDate.minusDays(6),
                    parsedDate.minusDays(6))
            } else
                selectedPreviousDates=arrayListOf(
                parsedDate.minusDays(1),
                parsedDate.minusDays(2),
                parsedDate.minusDays(3),
                parsedDate.minusDays(4),
                parsedDate.minusDays(5),
                parsedDate.minusDays(6),
                parsedDate.minusDays(6),
                parsedDate.minusDays(7),
                parsedDate.minusDays(8),
                parsedDate.minusDays(9),
                parsedDate.minusDays(10),
                parsedDate.minusDays(11),
                parsedDate.minusDays(12),
                parsedDate.minusDays(13),
                    parsedDate.minusDays(14)
            )*/

            parsedDate
        }

        val daysOfWeek = daysOfWeekFromLocale()

        binding.calendar.setup(
            currentMonth.minusYears(localDataStore.getHistoryYears().toLong()),
            currentMonth.plusMonths(0), daysOfWeek.first()
        )

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDayBinding.bind(view)

            init {
                binding.root.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {

                        //day.date

                        if (day.date <= LocalDate.now()) {
                            val intent = Intent()
                            intent.putExtra("selected_date", "${day.date}")
                            setResult(RESULT_OK, intent)
                            finish()
                        }

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

                if (day.owner == DayOwner.THIS_MONTH) {
                    dayLayoutMain.visible()

                    when (day.date) {
                        selectedLocalDate -> {
                            container.binding.tvDay.setTextColor(Color.parseColor("#ffffff"))
                            container.binding.dayBack.visible()
                        }

                        else -> {
                            container.binding.tvDay.setTextColor(Color.parseColor("#66ffffff"))
                            container.binding.dayBack.invisible()

                            if (selectedPreviousDates.contains(day.date)) {
                                container.binding.tvDay.setTextColor(Color.parseColor("#ffffff"))
                                container.binding.dayBack.visible()
                            }
                        }
                    }
                    if (isSunday(day.date)) {
                        container.binding.tvDay.setTextColor(Color.parseColor("#CCff2c52"))
                    }
                    if (isAfterToday(day.date)) {
                        container.binding.tvDay.setTextColor(Color.parseColor("#33ffffff"))
                    }
                } else {
                    dayLayoutMain.invisible()
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = CalendarHeaderBinding.bind(view).exTwoHeaderText
        }
        binding.calendar.monthHeaderBinder = object :
            MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                @SuppressLint("SetTextI18n") // Concatenation warning for `setText` call.
                container.textView.text =
                    "${month.yearMonth.month.name.lowercase().capitalizeWords()} ${month.year}"
            }
        }

        binding.calendar.scrollToDate(selectedLocalDate)

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
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }


    override fun getViewBinding() = ActivityHistoryCalendarBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding? = null

    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {
    }
}