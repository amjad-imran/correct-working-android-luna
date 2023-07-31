package com.noisefit.ui.reward.streak

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.color
import androidx.fragment.app.viewModels
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthScrollListener
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import com.noisefit.R
import com.noisefit_commans.data.response.StreakDetailsResponse
import com.noisefit.databinding.CalendarDayStreakBinding
import com.noisefit.databinding.FragmentStepStreakBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields


@AndroidEntryPoint
class StepStreakFragment :
    BaseFragment<FragmentStepStreakBinding>(FragmentStepStreakBinding::inflate) {

    private var currentSelectedMonth: CalendarMonth? = null

    private val viewModel: StreakDetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getStreakData()

        if (!viewModel.localDataStore.isCoinsWalkAroundShown()) {
            navigate(StepStreakFragmentDirections.actionStepStreakFragmentToCoinsWalkAroundBottomDialogFragment())
        }
    }

    override fun initListener() {
        binding.tvAboutStreak.setOnClickListener {
            navigate(R.id.aboutStreaksFragment)
        }
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytCalendar.ivArrowLeft.setOnClickListener {
            currentSelectedMonth?.let {
                binding.lytCalendar.calendar.smoothScrollToMonth(it.yearMonth.minusMonths(1))
            }
        }
        binding.lytCalendar.ivArrowRight.setOnClickListener {
            currentSelectedMonth?.let {
                binding.lytCalendar.calendar.smoothScrollToMonth(it.yearMonth.plusMonths(1))
            }
        }
        binding.lytMultiplier.ivInfo.setOnClickListener {
            navigate(
                StepStreakFragmentDirections.actionStepStreakFragmentToBottomSheetStreakInfo(
                    binding.lytMultiplier.tvDailyMultiplier.text.toString(),
                    binding.lytMultiplier.tvNextMultiplier.text.toString()
                )
            )
        }

    }

    override fun subscribeObservers() {

        viewModel.streakMessage.observe(this) {
            if (it.second) {//danger mode
                val string = SpannableStringBuilder()
                    .append("Your streak is in danger of breaking! Meet your daily step goal in ")
                    .color(requireContext().getColor(R.color.white)) { append(it.first) }
                    .append(" to keep it alive.")
                binding.tvActiveMsg.text = string

            } else {
                binding.tvActiveMsg.text = it.first
            }
        }

        viewModel.streakData.observe(this) {


            if (it.curr_streak == null) {
                binding.lytStreakReward.tvCurrentStreakDays.text = "0 days"
                binding.lytStreakReward.tvDailyReward.text = "Inactive"
                binding.lytMultiplier.apply {
                    tvDailyMultiplier.text = "0x"
                    tvNextMultiplier.text = "1x"
                }
                binding.lytStreakReward.ivStreak1.setImageResource(R.drawable.ic_streak_dash)
                binding.lytLevelUp.tvNextLevelUpIn.text = "Inactive"
            } else {
                binding.lytStreakReward.ivStreak1.setImageResource(R.drawable.ic_streak_dash)
                val streakDays = it.curr_streak!!.curr_streak_length
                binding.lytStreakReward.tvCurrentStreakDays.text =
                    "$streakDays ${if (streakDays == 1) "day" else "days"}"
                binding.lytStreakReward.tvDailyReward.text = "${it.curr_streak!!.daily_target}"

                binding.lytMultiplier.apply {
                    tvDailyMultiplier.text = "${it.curr_streak!!.current_multiplier}x"
                    tvNextMultiplier.text = "${it.curr_streak!!.next_multiplier}x"
                }
                binding.lytLevelUp.apply {
                    val levelUpIn = it.curr_streak!!.levelUpIn
                    tvNextLevelUpIn.text = "$levelUpIn ${if (levelUpIn == 1) "day" else "days"}"
                }
            }

            val longestStreakCount = it.longestStreakCount ?: 0
            binding.lytLevelUp.tvLongestStreak.text =
                "$longestStreakCount ${if (longestStreakCount == 1) "day" else "days"}"


            initCalendar(it)
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

    }

    //TODO Exception handling
    private fun initCalendar(streakDetail: StreakDetailsResponse) {

        val currentDay = LocalDate.parse(streakDetail.current_date)
        val currentMonth = YearMonth.now()
        val calendarStart = LocalDate.parse("2023-03-01")


        val currentStreakDays = if (streakDetail.curr_streak != null) {
            val currentStreakStartDate = LocalDate.parse(streakDetail.curr_streak?.start_date)
            calculateDaysList(currentStreakStartDate, currentDay)
        } else {
            HashMap()
        }

        val highlightStreaks = HashMap<LocalDate, BackType>()

        streakDetail.previous_streaks?.forEachIndexed { index, streakDate ->
            val data = calculateDaysList(
                LocalDate.parse(streakDate.start_date), LocalDate.parse(streakDate.end_date)
            )
            data.forEach {
                highlightStreaks.set(it.key, it.value)
            }
        }

        val streakMilestoneHighlightDays = HashSet<LocalDate>()
        streakDetail.curr_streak?.milestone_dates?.forEach {
            streakMilestoneHighlightDays.add(LocalDate.parse(it))

        }

        val daysOfWeek = daysOfWeekFromLocale()

        binding.lytCalendar.calendar.setup(
            calendarStart.yearMonth, currentMonth.plusMonths(1), daysOfWeek.first()
        )

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDayStreakBinding.bind(view)

            init {
                binding.root.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {

                        val message = viewModel.getMessageToDisplay(
                            day.date,
                            currentDay,
                            currentStreakDays,
                            highlightStreaks,
                            streakMilestoneHighlightDays
                        )

                        message?.let { msg ->
                            showInfoPopup(it, msg)
                        }

                        LOGS.d(
                            "CLICK",
                            "Selected date ${day.date} $message"
                        )

                    }
                }
            }
        }

        binding.lytCalendar.calendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.binding.tvDay
                val dayLayoutMain = container.binding.dayLayoutMain
                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    dayLayoutMain.visible()

                    when (day.date) {
                        currentDay -> {
                            container.binding.tvDay.setTextColor(Color.parseColor("#ffffff"))
                            container.binding.tvDay.typeface =
                                ResourcesCompat.getFont(requireContext(), com.noisefit_commans.R.font.gilroy_bold)
                            if (viewModel.streakMessage.value?.second == true) {
                                container.binding.ivBackToday.setImageResource(R.color.accent_color_pink)
                            } else {
                                container.binding.ivBackToday.setImageResource(R.color.accent_color_purple)
                            }
                            container.binding.ivBackToday.visible()
                        }
                        else -> {
                            container.binding.tvDay.setTextColor(Color.parseColor("#7Affffff"))
                            container.binding.tvDay.typeface =
                                ResourcesCompat.getFont(requireContext(), com.noisefit_commans.R.font.gilroy_medium)
                            container.binding.ivBackToday.gone()
                        }
                    }

                    val currentStreak = currentStreakDays[day.date]
                    val highlightDate = highlightStreaks[day.date]

                    if (currentStreak != null) {
                        if (day.date != currentDay) {
                            container.binding.tvDay.setTextColor(Color.parseColor("#ca99ff"))
                            container.binding.tvDay.typeface =
                                ResourcesCompat.getFont(requireContext(), com.noisefit_commans.R.font.gilroy_medium)
                        }
                        setBackgroundResCurrent(
                            currentStreak, container.binding, 0
                        )
                    } else if (highlightDate != null) {
                        container.binding.tvDay.setTextColor(Color.parseColor("#ffffff"))
                        container.binding.tvDay.typeface =
                            ResourcesCompat.getFont(requireContext(), com.noisefit_commans.R.font.gilroy_bold)
                        setBackgroundResCurrent(
                            highlightDate, container.binding, 1
                        )
                    } else {
                        container.binding.ivBackStart.gone()
                        container.binding.ivBackMid.gone()
                        container.binding.ivBackEnd.gone()
                    }

                    /*val isAfterToday = isAfterToday(day.date)
                    if (isAfterToday) {
                        container.binding.tvDay.setTextColor(Color.parseColor("#33ffffff"))
                        container.binding.tvDay.typeface =
                            ResourcesCompat.getFont(requireContext(), R.font.gilroy_medium)
                    }*/

                    //TODO show second milestone faded
                    if (streakMilestoneHighlightDays.contains(day.date)) {
                        if (day.date == currentDay) {
                            container.binding.ivBackToday.gone()
                            container.binding.ivBackMilestone.visible()
                            container.binding.ivBackMilestone.setBackgroundResource(R.drawable.back_streak_level_current)
                            container.binding.tvDay.setTextColor(Color.parseColor("#ffffff"))
                            container.binding.tvDay.typeface =
                                ResourcesCompat.getFont(requireContext(), com.noisefit_commans.R.font.gilroy_bold)
                        } else {
                            container.binding.ivBackToday.gone()
                            container.binding.ivBackMilestone.visible()
                            container.binding.ivBackMilestone.setBackgroundResource(R.drawable.back_streak_level)
                            container.binding.tvDay.setTextColor(Color.parseColor("#ffffff"))
                            container.binding.tvDay.typeface =
                                ResourcesCompat.getFont(requireContext(), com.noisefit_commans.R.font.gilroy_bold)
                        }
                    } else {
                        container.binding.ivBackMilestone.gone()
                    }
                } else {
                    dayLayoutMain.invisible()
                }
            }
        }

        binding.lytCalendar.calendar.monthScrollListener = object : MonthScrollListener {
            override fun invoke(month: CalendarMonth) {
                currentSelectedMonth = month
                nullableBinding?.lytCalendar?.tvMonth?.text =
                    "${month.yearMonth.month.name.lowercase().capitalizeWords()} ${month.year}"
            }
        }

        binding.lytCalendar.calendar.scrollToDate(currentDay)

    }


    fun showInfoPopup(clickedView: View, message: String) {

        val balloon = Balloon.Builder(requireContext())
            .setWidth(BalloonSizeSpec.WRAP)
            .setHeight(BalloonSizeSpec.WRAP)
            .setText(message)
            /*.setTextTypeface(R.font.gilroy_medium)*/
            .setTextColorResource(R.color.white)
            .setTextSize(16f)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowSize(10)
            .setArrowPosition(0.5f)
            .setPadding(12)
            .setCornerRadius(10f)
            .setBackgroundColorResource(R.color.popup_back)
            .setBalloonAnimation(BalloonAnimation.ELASTIC)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        balloon.showAlignTop(clickedView)
    }

    private fun setBackgroundResCurrent(
        backgroundRes: BackType, binding: CalendarDayStreakBinding, type: Int = 0
    ) {
        when (backgroundRes) {
            BackType.START -> {
                binding.ivBackStart.visible()
                binding.ivBackMid.gone()
                binding.ivBackSingle.gone()
                binding.ivBackEnd.gone()
                binding.ivBackStart.setBackgroundResource(
                    if (type == 0) {
                        R.drawable.back_streak_day_highlight_start
                    } else {
                        R.drawable.back_streak_last_start
                    }
                )
            }
            BackType.CENTRE -> {
                binding.ivBackStart.gone()
                binding.ivBackSingle.gone()
                binding.ivBackMid.visible()
                binding.ivBackEnd.gone()
                binding.ivBackMid.setBackgroundResource(
                    if (type == 0) {
                        R.drawable.back_streak_day_highlight_mid
                    } else {
                        R.drawable.back_streak_last_mid
                    }
                )
            }
            BackType.END -> {
                binding.ivBackStart.gone()
                binding.ivBackMid.gone()
                binding.ivBackSingle.gone()
                binding.ivBackEnd.visible()
                binding.ivBackEnd.setBackgroundResource(
                    if (type == 0) {
                        R.drawable.back_streak_day_highlight_end
                    } else {
                        R.drawable.back_streak_last_end
                    }
                )
            }
            BackType.SINGLE -> {
                binding.ivBackStart.gone()
                binding.ivBackMid.gone()
                binding.ivBackSingle.visible()
                binding.ivBackEnd.gone()
                binding.ivBackSingle.setBackgroundResource(
                    if (type == 0) {
                        R.drawable.back_streak_day_highlight_single
                    } else {
                        R.drawable.back_streak_last_center
                    }
                )
            }
        }
    }

    private fun isAfterToday(date: LocalDate): Boolean {
        return date > LocalDate.now()
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

    fun calculateDaysList(
        startDate: LocalDate,
        endDate: LocalDate
    ): HashMap<LocalDate, BackType> {
        var currentDate = startDate
        val responseList = HashMap<LocalDate, BackType>()
        var position = 0
        while (currentDate != endDate) {
            val backType = if (position == 0) {
                BackType.START
            } else {
                BackType.CENTRE
            }
            responseList.set(currentDate, backType)
            currentDate = currentDate.plusDays(1)
            position++
        }
        responseList.set(currentDate, BackType.END)


        if (responseList.size == 1) {
            responseList.clear()
            responseList.set(currentDate, BackType.SINGLE)
        }
        return responseList
    }


}

enum class BackType {
    START, CENTRE, END, SINGLE
}