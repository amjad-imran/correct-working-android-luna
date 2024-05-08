package com.oreo.ui.femalehealth.cycletracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ActivityLogPeriodBinding
import com.noisefit.luna.databinding.CalendarDay3Binding
import com.noisefit.luna.databinding.CalendarDayBinding
import com.noisefit.luna.databinding.LayoutCycleLogCalHeaderBinding
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DateFormats.daysOfWeekFromLocale
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class LogPeriodActivity : BaseActivity<ActivityLogPeriodBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCalendar()
    }

    companion object {
        fun getStartIntent(
            context: Context
        ): Intent {
            return Intent(context, LogPeriodActivity::class.java).apply {
            }
        }
    }

    private fun initCalendar() {
        val currentDay = LocalDate.parse(DateFormats.getCurrentDate(DateFormats.dateFormat3))
        val currentMonth = YearMonth.now()
        val daysOfWeek = daysOfWeekFromLocale()

        val startMonth = YearMonth.of(2023, 8)

        binding.calendar.setup(
            startMonth,
            currentMonth.plusMonths(0), daysOfWeek.first()
        )
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDay3Binding.bind(view)

            init {
                binding.root.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {

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

        binding.calendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.binding.tvDay
                val dayLayoutMain = container.binding.dayLayoutMain
                textView.text = day.date.dayOfMonth.toString()

            }
        }
        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = LayoutCycleLogCalHeaderBinding.bind(view).exTwoHeaderText
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
        binding.calendar.scrollToDate(currentDay)

    }

    override fun initListener() {
        binding.toolbar.view1.visible()
        binding.toolbar.ivAddFriend.invisible()
        binding.toolbar.view1.loadImage(this, R.drawable.ic_log_settings)
        binding.toolbar.tvTitle.text=getString(R.string.text_log_period)

        binding.toolbar.backBtn.setOnClickListener {
            finish()
        }
    }

    override fun observeSubscriber() {

    }

    override fun getViewBinding() = ActivityLogPeriodBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding? = null
    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {

    }

}