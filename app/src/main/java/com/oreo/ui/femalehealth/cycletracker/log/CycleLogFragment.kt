package com.oreo.ui.femalehealth.cycletracker.log

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
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
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.oreo.data.model.PeriodCycleHistory
import com.oreo.ui.femalehealth.cycletracker.DayState
import com.oreo.ui.femalehealth.cycletracker.LogPeriodActivity
import com.oreo.ui.femalehealth.cycletracker.PeriodPos
import com.oreo.ui.femalehealth.cycletracker.log.bottom.CALENDER_DAY_LOG_KEY
import com.oreo.ui.femalehealth.cycletracker.log.bottom.CYCLE_LOG_SAVE
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CycleLogFragment : BaseFragment<FragmentCycleLogBinding>(FragmentCycleLogBinding::inflate) {
    companion object {

        fun getStartData(selectedLogDate: String?): Pair<Int, Bundle?> {
            return Pair(R.id.cycleLogFragment, Bundle().apply {
                putString("selectedLogDate", selectedLogDate)
            })
        }
    }

    val args: CycleLogFragmentArgs by navArgs()

    private val viewModel: CycleLogViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()

        args.selectedLogDate?.let {
            viewModel.selectedDate = LocalDate.parse(it)
            showCycleLogBottomSheet()
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.getCycleHistoryData()
    }

    private fun initToolbar() {
        binding.lytToolbar.view1.gone()
        binding.lytToolbar.ivAddFriend.invisible()
        binding.lytToolbar.view1.loadImage(
            binding.lytToolbar.view1.context,
            R.drawable.ic_log_settings
        )
        binding.lytToolbar.tvTitle.text = getString(R.string.text_calender)
    }

    private fun showCalenderDayLog(date: LocalDate) {
        navigate(
            CycleLogFragmentDirections.actionCycleLogFragmentToCalenderDayLogBottomSheet(
                date.toString(),
                viewModel.getFirstPeriodDate().toString()
            )
        )
    }

    private fun initCalender(cycleHistoryData: PeriodCycleHistory) {
        val currentMonth = YearMonth.now()
        val firstPeriodDate = viewModel.getFirstPeriodDate()

        binding.calendar.setup(
            firstPeriodDate.minusMonths(2).yearMonth, currentMonth.plusMonths(12), DayOfWeek.MONDAY
        )
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDayFmhOnboardBinding.bind(view)

            init {

                binding.root.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (day.date > viewModel.todayDate || day.date < firstPeriodDate) {
                            return@setOnClickListener
                        }

                        val oldDate = viewModel.selectedDate
                        this@CycleLogFragment.binding.calendar.notifyDateChanged(
                            oldDate
                        )
                        viewModel.selectedDate = day.date
                        showCalenderDayLog(day.date)
                        this@CycleLogFragment.binding.calendar.notifyDateChanged(
                            day.date
                        )
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

                    if (isDateSelected) {
                        binding.ivBackSelected.visible()
                    } else {
                        binding.ivBackSelected.gone()
                    }


                    when (state) {
                        DayState.Fertile -> {
                            hideAllBack(binding)
                            binding.ivBackPeriod.gone()
                            binding.tvDay.setTextColor(
                                ContextCompat.getColor(
                                    binding.tvDay.context, R.color.color_ovulation
                                )
                            )
                        }

                        DayState.OvulationDay -> {
                            hideAllBack(binding)
                            binding.ivBackPeriod.setImageResource(R.drawable.back_circle_fertile)
                            binding.ivBackPeriod.visible()
                            binding.tvDay.setTextColor(
                                ContextCompat.getColor(
                                    binding.tvDay.context, R.color.color_ovulation
                                )
                            )
                        }

                        is DayState.Period -> {
                            binding.ivBackPeriod.gone()
                            when (state.pos) {
                                PeriodPos.START -> {
                                    binding.ivBackStart.visible()
                                    binding.ivBackSingle.gone()
                                    binding.ivBackEnd.gone()
                                    binding.ivBackMid.gone()
                                }

                                PeriodPos.END -> {
                                    binding.ivBackSingle.gone()
                                    binding.ivBackStart.gone()
                                    binding.ivBackEnd.visible()
                                    binding.ivBackMid.gone()
                                }

                                PeriodPos.CENTER -> {
                                    binding.ivBackSingle.gone()
                                    binding.ivBackStart.gone()
                                    binding.ivBackEnd.gone()
                                    binding.ivBackMid.visible()
                                }

                                PeriodPos.SINGLE -> {
                                    binding.ivBackSingle.visible()
                                    binding.ivBackStart.gone()
                                    binding.ivBackEnd.gone()
                                    binding.ivBackMid.gone()
                                }
                            }
                            binding.tvDay.setTextColor(
                                ContextCompat.getColor(
                                    binding.tvDay.context, R.color.white
                                )
                            )
                        }

                        DayState.Default -> {
                            hideAllBack(binding)
                            binding.ivBackPeriod.gone()
                            binding.tvDay.setTextColor(
                                ContextCompat.getColor(
                                    binding.tvDay.context, R.color.white
                                )
                            )
                        }
                    }

                    if (this.day.date > viewModel.todayDate) {
                        binding.ivBackSingle.alpha = 0.5f
                        binding.ivBackStart.alpha = 0.5f
                        binding.ivBackEnd.alpha = 0.5f
                        binding.ivBackMid.alpha = 0.5f
                    } else {
                        binding.ivBackSingle.alpha = 1f
                        binding.ivBackStart.alpha = 1f
                        binding.ivBackEnd.alpha = 1f
                        binding.ivBackMid.alpha = 1f
                    }
                } else {
                    dayLayoutMain.invisible()
                }
            }

            private fun hideAllBack(binding: CalendarDayFmhOnboardBinding) {
                binding.ivBackSingle.gone()
                binding.ivBackStart.gone()
                binding.ivBackEnd.gone()
                binding.ivBackMid.gone()
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
        binding.calendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    container.textView.text = "${
                        data.yearMonth.month.name.lowercase().capitalizeWords()
                    } ${data.yearMonth.year}"
                }
            }
        binding.calendar.scrollToMonth(viewModel.todayDate.yearMonth)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val periodStartDate =
                    data?.getStringExtra("period_date_start")
                val periodEndDate =
                    data?.getStringExtra("period_date_end")

                if (periodStartDate == null) {
                    return@registerForActivityResult
                }

                showCycleLogBottomSheet(periodStartDate, periodEndDate)
                //viewModel.onCalendarDateSelected(periodStartDate)

                LOGS.d("moveToPosition Selected Date  :${periodStartDate}")
            }
        }

    override fun initListener() {
        setFragmentResultListener(CALENDER_DAY_LOG_KEY) { _, bundle ->
            val right = bundle.getBoolean("right")
            val left = bundle.getBoolean("left")
            val openLog = bundle.getBoolean("open_log")
            if (openLog) {
                viewModel.setOpenLogBottomSheet(true)
            }
            if (right) {
                if (viewModel.selectedDate <= LocalDate.now()) {
                    this@CycleLogFragment.binding.calendar.notifyDateChanged(
                        viewModel.selectedDate
                    )
                    viewModel.selectedDate =
                        LocalDate.parse(viewModel.selectedDate.toString()).plusDays(1)

                    this@CycleLogFragment.binding.calendar.notifyDateChanged(
                        viewModel.selectedDate
                    )
                }


            }
            if (left) {

                this@CycleLogFragment.binding.calendar.notifyDateChanged(
                    viewModel.selectedDate
                )
                viewModel.selectedDate =
                    LocalDate.parse(viewModel.selectedDate.toString()).plusDays(-1)

                this@CycleLogFragment.binding.calendar.notifyDateChanged(
                    viewModel.selectedDate
                )
            }
        }
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnLog.setOnClickListener {
            showCycleLogBottomSheet()
        }
    }

    private fun showCycleLogBottomSheet(
        periodStartDate: String? = null,
        periodEndDate: String? = null
    ) {
        setFragmentResultListener(
            CYCLE_LOG_SAVE
        ) { _, bundle ->
            val logSaved = bundle.getBoolean("log_saved")
            val openActivity = bundle.getBoolean("openActivity")
            if (openActivity) {
                resultLauncher.launch(
                    LogPeriodActivity.getStartIntent(
                        requireContext(),
                    )
                )
            }
            if (logSaved) {
                viewModel.setOpenDayLogBottomSheet(true)
            }
        }
        openLogBottomSheet(periodStartDate, periodEndDate)
    }

    private fun openLogBottomSheet(
        periodStartDate: String? = null,
        periodEndDate: String? = null
    ) {
        navigate(R.id.bottomSheetCycleLog, Bundle().apply {
            if (periodStartDate == null) {
                putString("selectedDate", viewModel.selectedDate.toString())
                putString("periodEndDate", null)

            } else {
                putString("selectedDate", periodStartDate)
                putString("periodEndDate", periodEndDate)
            }
            putString(
                "firstPeriodDate",
                viewModel.getFirstPeriodDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            )
        })
    }

    override fun subscribeObservers() {
        viewModel.openDayLogBottomSheet.observe(this) {
            it?.getContent()?.let {
                showCalenderDayLog(viewModel.selectedDate)
            }
        }
        viewModel.openLogBottomSheet.observe(this) {
            it?.getContent()?.let {
                openLogBottomSheet()
            }
        }
        viewModel.cycleHistoryData.observe(this) {
            it?.let {
                binding.btnLog.visible()
                initCalender(it)
            }

        }

        viewModel.logPeriodData.observe(this) {
            it?.getContent()?.let {

            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
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


}