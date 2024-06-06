package com.oreo.ui.femalehealth.cycletracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ActivityLogPeriodBinding
import com.noisefit.luna.databinding.CalendarDay3Binding
import com.noisefit.luna.databinding.LayoutCycleLogCalHeaderBinding
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.oreo.ui.femalehealth.cycletracker.log.CycleLogViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class LogPeriodActivity : BaseActivity<ActivityLogPeriodBinding>() {

    private val viewModel: CycleLogViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.shouldGenerateFutureData = false
        viewModel.getCycleHistoryData()
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
        val currentMonth = YearMonth.now()
        val firstPeriodDate = viewModel.getFirstPeriodDate()

        binding.calendar.setup(
            firstPeriodDate.minusMonths(2).yearMonth, currentMonth.plusMonths(12), DayOfWeek.MONDAY
        )
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDay3Binding.bind(view)

            init {
                binding.root.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (day.date > viewModel.todayDate || day.date < firstPeriodDate) {
                            return@setOnClickListener
                        }

                        viewModel.onCalendarDateClicked(day.date)


                        if (day.date <= LocalDate.now()) {
//                            val intent = Intent()
//                            intent.putExtra("selected_date", "${day.date}")
//                            setResult(RESULT_OK, intent)
//                            finish()
                        }

                    }
                }
            }

            fun bind(day: CalendarDay) {
                this.day = day
                val dayLayoutMain = binding.dayLayoutMain
                binding.tvDay.text = this.day.date.dayOfMonth.toString()

                if (this.day.position == DayPosition.MonthDate) {
                    dayLayoutMain.visible()

                    val (state, isDateSelected) = viewModel.getCurrentState(day.date)


                    when (state) {

                        is DayState.Period -> {
                            binding.tvDay.setTextColor(
                                ContextCompat.getColor(
                                    binding.tvDay.context, R.color.color_bubble_gum_pink
                                )
                            )
                            if (this.day.date > viewModel.todayDate) {
                                binding.dayBack.setImageResource(com.noisefit_commans.R.drawable.back_modal_workout)
                                //binding.dayBack.alpha = 0.5f
                            } else {
                                //binding.dayBack.setImageResource(R.drawable.back_circle_bubble_gum_pink)
                                binding.dayBack.setImageResource(R.drawable.ic_check_mark_period)
                                //binding.dayBack.alpha = 1f
                            }

                        }


                        else -> {
                            binding.dayBack.setImageResource(com.noisefit_commans.R.drawable.back_modal_workout)
                            hideAllBack(binding)

                            binding.tvDay.setTextColor(
                                ContextCompat.getColor(
                                    binding.tvDay.context, R.color.white
                                )
                            )
                        }
                    }


                } else {
                    dayLayoutMain.invisible()
                }
            }

            private fun hideAllBack(binding: CalendarDay3Binding) {
            }
        }

        binding.calendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.bind(day)
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
        binding.calendar.scrollToMonth(viewModel.todayDate.yearMonth)

    }

    override fun initListener() {
        //binding.toolbar.view1.visible()
        //binding.toolbar.ivAddFriend.invisible()
        //binding.toolbar.view1.loadImage(this, R.drawable.ic_log_settings)
        binding.toolbar.tvTitle.text = getString(R.string.text_log_period)

        binding.toolbar.backBtn.setOnClickListener {
            finish()
        }

        binding.btnLog.setOnClickListener {
            viewModel.savePeriodLog()
        }
    }


    override fun observeSubscriber() {


        viewModel.logPeriodData.observe(this) {
            it?.getContent()?.let {
                val (start, end) = viewModel.getLastInteractedRange()
                val intent = Intent()
                intent.putExtra("period_date_start", start)
                intent.putExtra("period_date_end", end)
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        viewModel.notifyDateChanged.observe(this) {
            it.getContent()?.let { dates ->
                dates.forEach { date ->
                    binding.calendar.notifyDateChanged(date)
                }
            }
        }
        viewModel.cycleHistoryData.observe(this) {
            initCalendar()
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }

    override fun getViewBinding() = ActivityLogPeriodBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding? = null
    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {

    }

}