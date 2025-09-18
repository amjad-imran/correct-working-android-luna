package com.oreo.ui.femalehealth.cycletracker

import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CalenderCycleTrackerDayBinding
import com.noisefit.luna.databinding.FragmentCycleTrackerBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.data.model.femaleh.TempPrediction
import com.oreo.data.model.health.Nudges
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.chatGpt.ChatGptFragment
import com.oreo.ui.chatGpt.PlanType
import com.oreo.ui.femalehealth.cycletracker.history.INFO_LOG
import com.oreo.ui.femalehealth.cycletracker.insight.CycleInsightLaunchMode
import com.oreo.ui.femalehealth.cycletracker.log.CycleLogFragment
import com.oreo.ui.femalehealth.cycletracker.streak.CycleDetailsFragment
import com.oreo.ui.readiness.NudgeBannerListener
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.workout.details.NudgeBgColor
import com.oreo.ui.workout.details.WorkoutNudgeFragment
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@AndroidEntryPoint
class CycleTrackerFragment :
    BaseFragment<FragmentCycleTrackerBinding>(FragmentCycleTrackerBinding::inflate) {
    private val viewModel: CycleTrackerViewModel by viewModels()

    private val cycleHistoryAdapter by lazy {
        FMHCycleHistoryAdapter(object : OnHistoryItemClickListener {
            override fun onHistoryItemClick(data: FMHCycleHistoryDataModel, position: Int) {
                val (frag, bundle) = CycleDetailsFragment.getStartData(
                    data
                )
                navigate(frag, bundle)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_cycle_tracking_page_visit)

        binding.lytTrackerTop.tvPhase.setPaintFlags(binding.lytTrackerTop.tvPhase.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG);
        viewModel.getNotificationToggle()
        setRecycler()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCycleHistoryData()
    }

    private fun initCalender() {

        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = CalenderCycleTrackerDayBinding.bind(view)
            lateinit var day: WeekDay
            val dateToday = LocalDate.now()

            init {
                view.setOnClickListener {
                    if (viewModel.selectedDate.value != day.date) {
                        viewModel.updateSelectedDate(day.date)
                    }
                }
            }

            fun bind(day: WeekDay) {
                this.day = day
                bind.exSevenDateText.text = day.date.format(
                    DateTimeFormatter.ofPattern(
                        "dd",
                        Locale(NoiseFitApplicationMain.appLanguage.languageCode)
                    )
                )
                bind.exSevenDayText.text = day.date.format(
                    DateTimeFormatter.ofPattern(
                        "EEE",
                        Locale(NoiseFitApplicationMain.appLanguage.languageCode)
                    )
                )

                val (state, isDateSelected) = viewModel.getCurrentState(day.date)

                if (isDateSelected) {
                    bind.ivBackSelected.visible()
                } else {
                    bind.ivBackSelected.gone()
                }

                when (state) {
                    DayState.Fertile -> {
                        bind.ivBackPeriod.gone()
                        bind.exSevenDateText.setTextColor(
                            ContextCompat.getColor(
                                bind.exSevenDayText.context, R.color.color_ovulation
                            )
                        )
                    }

                    DayState.OvulationDay -> {
                        bind.ivBackPeriod.setImageResource(R.drawable.back_circle_fertile)
                        bind.ivBackPeriod.visible()
                        bind.exSevenDateText.setTextColor(
                            ContextCompat.getColor(
                                bind.exSevenDayText.context, R.color.color_ovulation
                            )
                        )
                    }

                    is DayState.Period -> {
                        bind.ivBackPeriod.setImageResource(R.drawable.back_period_day)
                        bind.ivBackPeriod.visible()
                        bind.exSevenDateText.setTextColor(
                            ContextCompat.getColor(
                                bind.exSevenDayText.context, R.color.white
                            )
                        )
                    }

                    DayState.Default -> {
                        bind.ivBackPeriod.gone()
                        bind.exSevenDateText.setTextColor(
                            ContextCompat.getColor(
                                bind.exSevenDayText.context, R.color.white
                            )
                        )
                    }
                }

                if (day.date > dateToday) {
                    bind.ivBackPeriod.alpha = 0.5f
                } else {
                    bind.ivBackPeriod.alpha = 1f
                }
            }
        }

        binding.lytTrackerTop.vCalendar.weekCalender.dayBinder =
            object : WeekDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
            }

        val currentMonth = YearMonth.now()
        val calendarStart = viewModel.getCalendarStart()

        binding.lytTrackerTop.vCalendar.weekCalender.setup(
            calendarStart.yearMonth.atStartOfMonth(),
            currentMonth.plusYears(2).atEndOfMonth(),
            DayOfWeek.MONDAY,
        )
        binding.lytTrackerTop.vCalendar.weekCalender.scrollToDate(
            viewModel.selectedDate.value ?: LocalDate.now()
        )

        viewModel.isCalendarSetupDone = true
    }

    private fun setRecycler() {
        with(binding.lytCycleHistory.lytCycleList.rvHistory) {
            adapter = cycleHistoryAdapter
        }
    }

    override fun initListener() {

        binding.lytTrackerTop.ivNotificationStatus.setOnClickListener {
            val lastValue = viewModel.notificationToggleModel.value?.female_health ?: false
            viewModel.notificationToggleModel.value?.female_health = lastValue.not()
            viewModel.updateNotificationToggle()

            if (lastValue){
                binding.lytTrackerTop.tvReminderMessage.text = getString(R.string.text_reminder_silent)
            }else{
                binding.lytTrackerTop.tvReminderMessage.text = getString(R.string.text_reminder_active)
            }
            notificationTextFade(binding.lytTrackerTop.tvPhase, binding.lytTrackerTop.tvReminderMessage)
        }

        binding.lytPrediction.vCard.ivInfo.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.info_clicked,
                HashMap<String, Any>().apply {
                    this["source"] = "homepage"
                    this["section"] = "cycle_prediction"
                }
            )
            navigate(
                R.id.dialogCtOvulationInfo, Bundle().apply {
                    this.putString("launchMode", "Ovulation Graph")
                }
            )
        }
        binding.lytTrackerTop.vCalendar.weekCalender.weekScrollListener = { weekDays ->
            viewModel.onWeekScrolled(weekDays.days.get(0).date)

            /* val selectedWeekDate = weekDays.days.get(0).date
             viewModel.getDataForDate(selectedWeekDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))*/
        }


        binding.toolbar.viewBackCalendar.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_cycle_tracking_calendar_button_click)
            val (frag, bundle) = CycleLogFragment.getStartData(null)
            navigate(frag, bundle)
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.calen_change,
                HashMap<String, Any>().apply {
                    this["source"] = "cycle_tracker"
                }
            )
        }

        binding.toolbar.icInfo.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_cycle_i_page_visit)
            navigate(R.id.cycleTrackStressInfoFragment)
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.info_clicked,
                HashMap<String, Any>().apply {
                    this["source"] = "homepage"
                    this["section"] = "cycle_tracking"
                }
            )
        }

        /*binding.lytTrackerTop.tvPhase.setOnClickListener {
            var launchMode = ""
            if (binding.lytTrackerTop.tvPhase.text.equals(getString(R.string.text_luteal_phase))) {
                launchMode = "Luteal"
            } else if (binding.lytTrackerTop.tvPhase.text.equals(getString(R.string.text_follicular_phase))) {
                launchMode = "Follicular"
            }
            if (launchMode.isNotEmpty()) {
                setFragmentResultListener(INFO_LOG) { _, bundle ->
                }
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_cycle_i_phase_page_visit)
                navigate(
                    R.id.dialogCtOvulationInfo, Bundle().apply {
                        this.putString("launchMode", launchMode)
                    }
                )
            }
        }*/
        binding.lytTrackerTop.tvPhase.setOnClickListener {
            var launchMode = ""
            if (binding.lytTrackerTop.tvPhase.text.equals(getString(R.string.text_luteal_phase))) {
                launchMode = "Luteal"
            } else if (binding.lytTrackerTop.tvPhase.text.equals(getString(R.string.text_follicular_phase))) {
                launchMode = "Follicular"
            }
            if (launchMode.isNotEmpty()) {
                setFragmentResultListener(INFO_LOG) { _, bundle ->
                }
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_cycle_i_phase_page_visit)
                navigate(
                    R.id.dialogCtOvulationInfo, Bundle().apply {
                        this.putString("launchMode", launchMode)
                    }
                )
            }
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytPrediction.root.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.cycle_information_clicked,
                HashMap<String, Any>().apply {
                    this["source"] = "homepage"
                    this["section"] = "cycle_prediction"
                }
            )
            navigate(R.id.cycleSkinTemperature, Bundle().apply {
                this.putString("selectedDate", viewModel.selectedDate.value.toString())
            })
        }
        binding.lytInsight.lytCycleLength.root.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_cycle_cycle_length_page_visit)
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.cycle_information_clicked,
                HashMap<String, Any>().apply {
                    this["source"] = "homepage"
                    this["section"] = "cycle_length"
                }
            )
            navigate(R.id.cycleInsightDetails, Bundle().apply {
                this.putSerializable("launchMode", CycleInsightLaunchMode.CYCLE_LENGTH)
            })
        }
        binding.lytInsight.lytPeriodLength.root.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_cycle_period_duration_page_visit)
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.cycle_information_clicked,
                HashMap<String, Any>().apply {
                    this["source"] = "homepage"
                    this["section"] = "cycle_duration"
                }
            )
            navigate(R.id.cycleInsightDetails, Bundle().apply {
                this.putSerializable("launchMode", CycleInsightLaunchMode.PERIOD_DURATION)
            })
        }
        binding.lytCycleHistory.ivMore.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.cycle_information_clicked,
                HashMap<String, Any>().apply {
                    this["source"] = "homepage"
                    this["section"] = "cycle_history"
                }
            )
            navigate(R.id.cycleTrackerHistory)
        }
        binding.lytTrackerTop.btnLog.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_cycle_log_button_click)
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.insight_log,
                HashMap<String, Any>().apply {
                    this["source"] = "cycle"
                }
            )
            val (frag, bundle) = CycleLogFragment.getStartData(viewModel.selectedDate.value.toString())
            navigate(frag, bundle)
        }
        binding.lytTrackerTop.btnLogNew.setOnClickListener {
            val (frag, bundle) = CycleLogFragment.getStartData(viewModel.selectedDate.value.toString())
            navigate(frag, bundle)
        }

        binding.lytWomenDayAnnouncement.lytWorkoutAnnc.root.setOnClickListener {
            viewModel.getComfortDietWorkoutData(true)
        }

        binding.lytWomenDayAnnouncement.lytDietAnnc.root.setOnClickListener {
            viewModel.getComfortDietWorkoutData(false)
        }
    }

    private fun handleComfortDietFoodClick(triple: Triple<Boolean, Boolean, Boolean>) {
        if(triple.third) {
            // Workout
            val workoutSetup = triple.first
            if (workoutSetup) {
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.home_lunaai_workout_plan)

                navigate(
                    R.id.workoutPlansFragment,
                    bundleOf(
                        "isComfortEnabled" to true
                    )
                )

            } else {
                if (viewModel.ringDataStore.getRingDevice() == null) {
                    context.showShortToast(getString(R.string.text_luna_ai_message))
                    return
                }

                val (frag, bundle) = ChatGptFragment.getStartData(
                    null,
                    null,
                    getString(R.string.text_build_me_a_workout_plan),
                    null,
                    AITopics.GENERAL,
                    planType = PlanType.WORKOUT
                )
                navigate(frag, bundle)
            }
        }
        else {
            // Diet
            val mealSetup = triple.second
            if (mealSetup) {
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.home_lunaai_nutrition_plan)

                navigate(
                    R.id.aiMealPlanFragment,
                    bundleOf(
                        "isComfortEnabled" to true
                    )
                )
            } else {
                if (viewModel.ringDataStore.getRingDevice() == null) {
                    context.showShortToast(getString(R.string.text_luna_ai_message))
                    return
                }
                val (frag, bundle) = ChatGptFragment.getStartData(
                    null,
                    null,
                    getString(R.string.text_build_me_a_weekly_diet_plan),
                    null,
                    AITopics.GENERAL,
                    planType = PlanType.DIET
                )
                navigate(frag, bundle)
            }
        }
    }

    private fun displayWomansDayCard(){
        binding.lytWomenDayAnnouncement.lytDietAnnc.apply {
            root.setBackgroundResource(R.drawable.bg_comfort_food_for_you_readiness)
            tvTitle.apply {
                text = getString(R.string.text_comfort_food_for_you)
                setTextColor("#CEDDFF".toColorInt())
            }
            tvDesc.text = getString(R.string.text_plan_nourishing_meals_to_help_you_feel_your_best)
        }

        binding.lytWomenDayAnnouncement.lytWorkoutAnnc.apply {
            root.setBackgroundResource(R.drawable.bg_gentle_movement_readiness)
            tvTitle.apply {
                text = getString(R.string.text_gentle_movement_for_your_flow)
                setTextColor("#B7DEFF".toColorInt())
            }
            tvDesc.text =
                getString(R.string.text_create_a_light_workout_to_support_your_body_s_needs_today)
        }

        binding.dividerWomenDayAnnouncement.root.visible()
        binding.lytWomenDayAnnouncement.root.visible()
    }

    private fun notificationTextFade(textView1: TextView, textView2: TextView) {
        textView1.visibility = View.VISIBLE
        textView2.visibility = View.INVISIBLE

        val fadeIn: Animation =
            AnimationUtils.loadAnimation(textView1.context, R.anim.fade_in_goal)
        val fadeOut: Animation =
            AnimationUtils.loadAnimation(textView1.context, R.anim.fade_out_goal)

        textView1.startAnimation(fadeOut)
        textView1.visibility = View.INVISIBLE
        textView2.visibility = View.VISIBLE
        textView2.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            textView2.startAnimation(fadeOut)
            textView2.visibility = View.INVISIBLE
            textView1.visibility = View.VISIBLE
            textView1.startAnimation(fadeIn)
        }, 1500)

    }

    private fun initInsightUI(cycleLength: Int, periodLength: Int) {
        binding.dividerInsight.root.visible()
        binding.lytInsight.root.visible()

        binding.lytInsight.lytCycleLength.apply {
            tvHeader.text = getString(R.string.text_cycle_length)
            tvValue.text = "${cycleLength}"
            tvUnit.text = getString(R.string.text_days)
            with(viewModel.isCycleLengthNormal(cycleLength)) {
                tvStatus.text =
                    if (this) getString(R.string.text_normal) else getString(R.string.text_abnormal)
                ivState.setImageResource(if (this) R.drawable.ic_fmh_normal else R.drawable.ic_fmh_abnormal)
            }

        }

        binding.lytInsight.lytPeriodLength.apply {
            tvHeader.text = getString(R.string.text_period_duration)
            tvValue.text = "${periodLength}"
            tvUnit.text = getString(R.string.text_days)
            with(viewModel.isPeriodLengthNormal(periodLength)) {
                tvStatus.text =
                    if (this) getString(R.string.text_normal) else getString(R.string.text_abnormal)
                ivState.setImageResource(if (this) R.drawable.ic_fmh_normal else R.drawable.ic_fmh_abnormal)
            }

        }


    }

    override fun subscribeObservers() {

        viewModel.planState.observe(this){
            it.getContent()?.let { triple ->
                handleComfortDietFoodClick(triple)
            }
        }

        viewModel.notificationToggleModel.observe(this) {
            binding.lytTrackerTop.ivNotificationStatus.setImageResource(
                viewModel.getBellResource(it.female_health?:false)
            )
        }
        viewModel.navigateToBack.observe(this) {
            it.getContent()?.let {
                navigateUpSafe()
            }
        }

        viewModel.notifyDateChange.observe(this) {
            it.getContent()?.let {
                try {
                    binding.lytTrackerTop.vCalendar.weekCalender.notifyDateChanged(
                        it
                    )
                } catch (exp: Exception) {
                }
            }
        }

        viewModel.selectedDate.observe(this) {
            try {
                binding.lytTrackerTop.vCalendar.weekCalender.notifyDateChanged(it)
            } catch (exp: Exception) {
            }
            binding.toolbar.tvMonth.text = it.format(
                DateTimeFormatter.ofPattern(
                    "MMM",
                    Locale(NoiseFitApplicationMain.appLanguage.languageCode)
                )
            )

            if(it==LocalDate.now()){
                if(viewModel.localDataStore.getLdwCycleTrackerData()){
                    displayWomansDayCard()
                }else{
                    binding.dividerWomenDayAnnouncement.root.gone()
                    binding.lytWomenDayAnnouncement.root.gone()
                }
            }else{
                binding.dividerWomenDayAnnouncement.root.gone()
                binding.lytWomenDayAnnouncement.root.gone()
            }

            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.day_change_click,
                HashMap<String, Any>().apply {
                    this["source"] = "cycle_tracker"
                }
            )
            viewModel.getDataForDate(it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        }

        viewModel.cycleHistoryData.observe(this) {
            if (it?.cycleHistory.isNullOrEmpty()) {
                binding.lytCycleHistory.root.gone()
                binding.dividerInsight.root.gone()
            } else {
                if (viewModel.femaleHealthData.value?.currentDay == null) {
                    binding.lytCycleHistory.root.gone()
                    binding.dividerInsight.root.gone()
                } else {
                    binding.lytCycleHistory.root.visible()
                    cycleHistoryAdapter.setData(it?.cycleHistory!!.take(3))
                }
            }
            initCalender()
        }
        viewModel.avgInsightData.observe(this) {

            if (it == null) {
                binding.dividerInsight.root.gone()
                binding.lytInsight.root.gone()
            } else {
                initInsightUI(it.second, it.first)
            }

        }

        viewModel.cyclePredictionData.observe(this) {
            if (it == null) {
                binding.dividerPrediction.root.gone()
                binding.lytPrediction.root.gone()
            } else {
                binding.dividerPrediction.root.visible()
                binding.lytPrediction.root.visible()
                initSkinTempPredictionWidget(it)
            }
        }
        viewModel.femaleHealthData.observe(this) {
            if (it?.currentDay == null) {
                binding.lytTrackerTop.groupPeriodData.invisible()

                binding.lytTrackerTop.layoutGetStarted.visible()

                binding.dividerInsight.root.gone()
                binding.lytInsight.root.gone()
                binding.dividerCues.root.gone()
                binding.lytCues.root.gone()

                binding.lytCycleHistory.root.gone()

            } else {
                binding.lytTrackerTop.groupPeriodData.visible()
                binding.lytTrackerTop.layoutGetStarted.gone()


                if (viewModel.avgInsightData.value == null) {
                    binding.dividerInsight.root.gone()
                    binding.lytInsight.root.gone()
                } else {
                    binding.dividerInsight.root.visible()
                    binding.lytInsight.root.visible()
                }



                setTopData(it)
                setNudgesViewPager(it.nudges, it)

                if (viewModel.cycleHistoryData.value?.cycleHistory.isNullOrEmpty()) {
                    binding.lytCycleHistory.root.gone()
                } else {
                    binding.lytCycleHistory.root.visible()
                    cycleHistoryAdapter.setData(
                        viewModel.cycleHistoryData.value?.cycleHistory?.take(3) ?: ArrayList()
                    )
                }

            }
            viewModel.notificationToggleModel.value?.let {
                viewModel.notificationToggleModel.postValue(it)
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar1.root.visible()
            } else {
                binding.progressBar1.root.gone()
            }
        }
    }

    private fun initSkinTempPredictionWidget(data: TempPrediction) {
        binding.lytPrediction.vCard.apply {
            val variation = data.tempVariation
            if (variation == null) {
                tvValue.text = "-"
                tvUnit.gone()
            } else {
                val tempVariation = if (viewModel.sessionManager.isMetric()) {
                    tvUnit.text = "°C"
                    AppConversionUtils.fahrenheitToCelsius(32 + data.tempVariation)
                } else {
                    tvUnit.text = "°F"
                    data.tempVariation
                }

                tvValue.text = if ((data.tempVariation ?: 0f) > 0f) {
                    "+${String.format(locale = Locale.US, "%.1f", tempVariation)}"
                } else {
                    "-${String.format(locale = Locale.US, "%.1f", abs(tempVariation))}"
                }
                tvUnit.visible()
            }

            /* if (data.pendingNights == null) {
                 tvMoreNight.text = ""
             } else {
                 tvMoreNight.text =
                     "Temperature data for upcoming 3 cycles is required" //"Data for ${data.pendingNights} more nights is required"
             }*/

            tvDescription.setVisibilityByCondition(data.message.isNullOrEmpty().not())
            tvDescription.text = data.message

            if (data.pendingNights == null) {
                tvMoreNight.gone()
                ivInfo.gone()
                divider1.root.gone()
            } else {
                tvMoreNight.visible()
                ivInfo.visible()
                divider1.root.visible()
                tvMoreNight.text =
                    getString(R.string.text_temperature_data_for_upcoming)//"Data for ${data.pendingNights} more nights is required"
            }

            vTempGraph.updateData(viewModel.combineTempData(data.tempData))

        }
    }

    private fun setTopData(data: FemaleHealthUserInfoModel) {
        binding.lytTrackerTop.apply {
            val selectedDateLocal = if (viewModel.selectedDate.value == null) {
                LocalDate.now()
            } else {
                viewModel.selectedDate.value!!
            }
            val selectedDate = selectedDateLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))


            if (selectedDateLocal > LocalDate.now()) {
                btnLog.isEnabled = false
            } else {
                btnLog.isEnabled = true
            }

            val isPastDate = viewModel.isPastCycle(selectedDateLocal)

            tvCurrentDay.text = getString(R.string.text_day_value, data.currentDay ?: 0)
            tvTotalDays.text = getString(R.string.text_of_value, data.cycleLength ?: 0)
            tvPregnancyChances.text = viewModel.getPregnancyText(data.pregnancyChances)
            with(
                viewModel.getCurrentPhaseText(
                    data.ovulationDate, data.periodDate, selectedDate
                )
            ) {
                viewModel.currentSelectedPhase = null
                if (this == null) {
                    tvPhase.text = "-"
                } else {
                    tvPhase.text = this.first
                    tvPhase.setTextColor(tvPhase.context.getColor(this.second))
                    if (binding.lytTrackerTop.tvPhase.text.equals(getString(R.string.text_luteal_phase))) {
                        viewModel.currentSelectedPhase = CyclePhase.LUTEAL
                    } else if (binding.lytTrackerTop.tvPhase.text.equals(getString(R.string.text_follicular_phase))) {
                        viewModel.currentSelectedPhase = CyclePhase.FOLLECULAR
                    }
                }
            }

            if (data.isPeriod || data.isOvulation) {

                if (data.isPeriod) {
                    if (isPastDate) {
                        showPastCycleUI(data.currentDay ?: 0)
                    } else {

                        tvCurrentState.text = if (data.otaLog) {
                            binding.lytTrackerTop.btnLog.text = getString(R.string.edit)
                            getString(R.string.text_period_cap)
                        } else {
                            binding.lytTrackerTop.btnLog.text = getString(R.string.text_log)
                            getString(R.string.text_predicted_period)
                        }

                        tvStateDay.text = if (data.otaLog) {
                            getString(R.string.text_day_value, data.currentDay)
                        } else {
                            getString(R.string.text_day_value, data.currentDay)
                        }
                    }

                    binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_high)
                } else {
                    if (selectedDate.equals(data.ovulationDate)) {
                        if (isPastDate) {
                            showPastCycleUI(data.currentDay ?: 0)
                        } else {
                            tvCurrentState.text = getString(R.string.text_predicted_day_of)
                            tvStateDay.text = getString(R.string.text_ovulation_first)
                        }
                        binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_blue_high)
                    }
                    binding.lytTrackerTop.btnLog.text = getString(R.string.text_log)
                }
            } else {
                binding.lytTrackerTop.btnLog.text = getString(R.string.text_log)
                val daysUntilOvulation = if (data.ovulationDate != null) {
                    viewModel.calculateDaysLeft(data.ovulationDate, selectedDate)
                } else {
                    null
                }
                val daysUntilNextPeriod =
                    viewModel.calculateDaysLeft(data.nextPeriodDate!!, selectedDate)

                if (daysUntilOvulation != null && (daysUntilOvulation < daysUntilNextPeriod && daysUntilOvulation > 0)) {
                    if (isPastDate) {
                        showPastCycleUI(data.currentDay ?: 0)
                    } else {
                        tvCurrentState.text = getString(R.string.text_ovulation_in)
                        tvStateDay.text = getString(R.string.text_value_days, daysUntilOvulation)
                    }
                    if (daysUntilOvulation > 3) {
                        binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_blue_low)
                    } else {
                        binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_blue_med)
                    }
                } else {
                    if (isPastDate) {
                        showPastCycleUI(data.currentDay ?: 0)
                    } else {

                        val isPeriodLate = data.confirmPeriodDate != null

                        tvCurrentState.text = if (isPeriodLate) {
                            getString(R.string.text_period_late_for)
                        } else {
                            getString(R.string.text_period_in)
                        }

                        tvStateDay.text = if (isPeriodLate) {
                            getString(R.string.text_value_day, data.confirmPeriodDate?.day)
                        } else {
                            getString(R.string.text_value_days, daysUntilNextPeriod)
                        }
                    }

                    if (daysUntilNextPeriod > 2) {
                        if (data.isFertileWindow) {
                            binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_blue_med)
                        } else {
                            binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_low)
                        }
                    } else {
                        binding.lytTrackerTop.ivBack.setImageResource(R.drawable.image_back_period_med)
                    }
                }
            }
        }
    }

    private fun showPastCycleUI(currentDay: Int) {
        binding.lytTrackerTop.tvCurrentState.text = getString(R.string.text_past_cycle)
        binding.lytTrackerTop.tvStateDay.text = getString(R.string.text_day_value, currentDay)
    }

    private fun setNudgesViewPager(
        data: List<Nudges>?,
        femaleHealthUserInfoModel: FemaleHealthUserInfoModel
    ) {

        if (data.isNullOrEmpty()) {
            binding.dividerCues.root.gone()
            binding.lytCues.root.gone()
            return
        } else {
            binding.dividerCues.root.visible()
            binding.lytCues.root.visible()
        }

        var nudgeBgColor = NudgeBgColor.NONE
        if (femaleHealthUserInfoModel.isPeriod) {
            nudgeBgColor = NudgeBgColor.PERIOD_HIGH
        } else if (femaleHealthUserInfoModel.isOvulation) {
            nudgeBgColor = NudgeBgColor.OVULATION_HIGH
        }

        val currentState = binding.lytTrackerTop.tvCurrentState.text.toString()
        if (currentState.equals(getString(R.string.text_period_in), true)) {
            nudgeBgColor = NudgeBgColor.PERIOD_LOW
        } else if (currentState.equals(getString(R.string.text_ovulation_in), true)) {
            nudgeBgColor = NudgeBgColor.OVULATION_LOW
        }

        if (femaleHealthUserInfoModel.confirmPeriodDate != null) {
            nudgeBgColor = NudgeBgColor.PERIOD_LOW
        }

        val fragments = ArrayList<WorkoutNudgeFragment>()
        data.forEach {
            fragments.add(WorkoutNudgeFragment.newInstance(it, nudgeBgColor).apply {
                setClickListener(
                    object :
                        NudgeBannerListener {
                        override fun onAiClicked() {
                            if (viewModel.ringDataStore.getRingDevice() == null) {
                                context.showShortToast(getString(R.string.text_luna_ai_message))
                                return
                            }
                            viewModel.sessionManager.logMoEngageAppEvent(
                                MoEngageLunaAppEvents.ai_widget_clicked,
                                HashMap<String, Any>().apply {
                                    this["source"] = "cycle"
                                }
                            )
                            navigate(
                                R.id.aiTopQuestionsFragment,
                                bundleOf("aiTopic" to AITopics.MENSTRUAL_HEALTH)
                            )
                        }
                    }
                )
            })
        }
        val sleepBannerAdapter = OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)
        binding.lytCues.vpBannerSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })
            adapter = sleepBannerAdapter
        }

        TabLayoutMediator(
            binding.lytCues.tabLayout, binding.lytCues.vpBannerSlider
        ) { _, _ -> }.attach()

        if (fragments.size > 1) {
            binding.lytCues.tabLayout.visible()
        } else {
            binding.lytCues.tabLayout.invisible()
        }
    }


}