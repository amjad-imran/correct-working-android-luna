package com.noisefit.ui.dashboard.graphs.steps

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.R
import com.noisefit_commans.data.response.Daily
import com.noisefit_commans.data.response.Monthly
import com.noisefit_commans.data.response.Weekly
import com.noisefit_commans.data.model.history.StepsHistoryData
import com.noisefit.databinding.FragmentStepsDetailsBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit.ui.dashboard.graphs.HistoryCalendarActivity
import com.noisefit.ui.settings.help.FitnessHealthViewModel
import com.noisefit_commans.data.enums.HealthOverViewHistoryType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.Year
import java.time.temporal.WeekFields
import java.util.*
import javax.inject.Inject

private const val TotalNumberOfDayGraphScroll = 7



@AndroidEntryPoint
class StepsDetailsFragment :
    BaseFragment<FragmentStepsDetailsBinding>(FragmentStepsDetailsBinding::inflate) {

    private val viewModel: StepsDetailsViewModel by viewModels()
    private var graphAdapter: StepsGraphSlidePagerAdapter? = null
    private val stepsHealthViewModel: FitnessHealthViewModel by viewModels()

    @Inject
    lateinit var localDataStore: DataStoredInterface


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        arguments?.let {
            val linkType = it.getString("type")
            if (linkType.isNullOrEmpty()) {
                val deepLinkKey = it.getDeeplinkPathArg()
                viewModel.healthOverViewHistoryType = getTypeFromDeepLinks(deepLinkKey)
//                viewModel.isStep = !deepLinkKey.equals("distancescreen", true)
            } else {
                viewModel.healthOverViewHistoryType = getType(linkType)

            }


            setUi()
        }
        binding.lytDay.tvTabTitle.text = getString(R.string.text_day)
        binding.lytWeek.tvTabTitle.text = getString(R.string.text_week)
        binding.lytMonth.tvTabTitle.text = getString(R.string.text_month)
        binding.lytYear.tvTabTitle.text = getString(R.string.text_year)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            binding.icHistoryCalendar.gone()
            binding.icCalendar.gone()
        }

        viewModel.getHighlights()
        viewModel.getStepsPercentage()


    }

    private fun getTypeFromDeepLinks(linkType: String?): HealthOverViewHistoryType {
        when (linkType?.lowercase()) {
            "distancescreen" -> {
                return HealthOverViewHistoryType.Distance
            }
            "caloriesscreen" -> {
                return HealthOverViewHistoryType.Calories
            }
        }
        return HealthOverViewHistoryType.Steps
    }

    private fun getType(linkType: String): HealthOverViewHistoryType {
        when (linkType.lowercase()) {
            HealthOverViewHistoryType.Calories.name.lowercase() -> {
                return HealthOverViewHistoryType.Calories
            }
            HealthOverViewHistoryType.Distance.name.lowercase() -> {
                return HealthOverViewHistoryType.Distance
            }
        }
        return HealthOverViewHistoryType.Steps
    }

    private fun setUi() {
        if (HealthOverViewHistoryType.Distance == viewModel.healthOverViewHistoryType) {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.DISTANCE_PAGE_VIEWED)
            binding.textView8.text = getString(R.string.distance)
            binding.layoutStepsProgress.textView10.text = viewModel.distanceUnit

            binding.layoutStepsProgress.tvCompletionPercent.setTextColor(Color.parseColor("#f4c232"))
            binding.layoutStepsProgress.layouProgress.pbSteps.setIndicatorColor(Color.parseColor("#f4c232"))

            binding.layoutMaxStepCount.textView11.text =
                getString(R.string.text_max_distance_covered)
            binding.layoutMaxStepCount.tvTotalStepsCount.setTextColor(Color.parseColor("#f4c232"))

            binding.layoutDaily.layoutProgress1.pbSteps.setIndicatorColor(Color.parseColor("#f4c232"))
            binding.layoutDaily.layoutProgress2.pbSteps.setIndicatorColor(Color.parseColor("#f7e4b0"))

            binding.layoutWeekly.layoutProgress1.pbSteps.setIndicatorColor(Color.parseColor("#f4c232"))
            binding.layoutWeekly.layoutProgress2.pbSteps.setIndicatorColor(Color.parseColor("#f7e4b0"))

            binding.layoutMonthly.layoutProgress1.pbSteps.setIndicatorColor(Color.parseColor("#f4c232"))
            binding.layoutMonthly.layoutProgress2.pbSteps.setIndicatorColor(Color.parseColor("#f7e4b0"))

            binding.layoutMaxStepCount.tvUnit.text = viewModel.distanceUnit


            binding.textAboutSteps.gone()
            binding.tvAboutSleepContent.gone()
            binding.tvLearnMore.gone()

            binding.parentContainer.setBackgroundResource(R.drawable.back_distance_details_gradient)
        } else if (HealthOverViewHistoryType.Steps == viewModel.healthOverViewHistoryType) {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.STEP_COUNT_PAGE_VIEWED)
            binding.parentContainer.setBackgroundResource(R.drawable.back_step_details_gradient)
        } else {
            binding.textView8.text = getString(R.string.calories)
            binding.parentContainer.setBackgroundResource(R.drawable.back_calories_details_gradient)

            binding.layoutStepsProgress.textView10.text = getString(R.string.text_kcal)

            binding.layoutStepsProgress.tvCompletionPercent.setTextColor(resources.getColor(R.color.calories_color))
            binding.layoutStepsProgress.layouProgress.pbSteps.setIndicatorColor(resources.getColor(R.color.calories_color))

            binding.layoutMaxStepCount.textView11.text =
                getString(R.string.text_max_calories_burned)
            binding.layoutMaxStepCount.tvTotalStepsCount.setTextColor(resources.getColor(R.color.calories_color))

            binding.layoutDaily.layoutProgress1.pbSteps.setIndicatorColor(resources.getColor(R.color.calories_color))
            binding.layoutDaily.layoutProgress2.pbSteps.setIndicatorColor(resources.getColor(R.color.calories_complete_goal))

            binding.layoutWeekly.layoutProgress1.pbSteps.setIndicatorColor(resources.getColor(R.color.calories_color))
            binding.layoutWeekly.layoutProgress2.pbSteps.setIndicatorColor(resources.getColor(R.color.calories_complete_goal))

            binding.layoutMonthly.layoutProgress1.pbSteps.setIndicatorColor(resources.getColor(R.color.calories_color))
            binding.layoutMonthly.layoutProgress2.pbSteps.setIndicatorColor(resources.getColor(R.color.calories_complete_goal))

            binding.layoutMaxStepCount.tvUnit.text = getString(R.string.text_kcal)


            binding.textAboutSteps.gone()
            binding.tvAboutSleepContent.gone()
            binding.tvLearnMore.gone()

        }


    }


    private fun setGraphAdapter(
        count: Int,
        stepsDataList: ArrayList<StepsHistoryData>,
        historyType: String,
        average: Double,
        steps: Long,
        date: String
    ) {
        graphAdapter = StepsGraphSlidePagerAdapter(
            this,
            count,
            stepsDataList,
            historyType,
            average,
            steps,
            date,
            viewModel.healthOverViewHistoryType
        )
        binding.viewPagerGraph.adapter = graphAdapter
        binding.viewPagerGraph.setCurrentItem(count, false)
        binding.viewPagerGraph.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //  Log.e("Selected_Page", position.toString())
                if (viewModel.graphInterval.value == GraphInterval.DAY) {
                    viewModel.stepsHistoryResponse.value?.step_activities?.get(position)?.let {

                        when (viewModel.healthOverViewHistoryType) {
                            HealthOverViewHistoryType.Distance -> {
                                viewModel.updateDistanceGoalPercentage(it.distance)
                            }
                            HealthOverViewHistoryType.Calories -> {
                                viewModel.updateGoalCaloriesPercentage(it.calories)
                            }
                            HealthOverViewHistoryType.Steps -> {
                                viewModel.updateStepsGoalPercentage(it.steps)
                            }
                        }

                    }

                }


            }

        })
    }


    override fun initListener() {

        binding.lytDay.root.setOnClickListener {
            viewModel.onDayClicked()
        }
        binding.lytWeek.root.setOnClickListener {
            viewModel.onWeekClicked()
        }
        binding.lytMonth.root.setOnClickListener {
            viewModel.onMonthClicked()
        }
        binding.lytYear.root.setOnClickListener {
            viewModel.onYearClicked()
        }

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.tvLearnMore.setOnClickListener {

            when (viewModel.healthOverViewHistoryType) {
                HealthOverViewHistoryType.Distance -> {
                    navigate(
                        StepsDetailsFragmentDirections.actionStepsDetailsFragmentToFitnessHealthDetailsFragment(
                            "Distance",
                            stepsHealthViewModel.distanceData().toTypedArray()
                        )
                    )
                }
                HealthOverViewHistoryType.Calories -> {

                }
                HealthOverViewHistoryType.Steps -> {
                    navigate(
                        StepsDetailsFragmentDirections.actionStepsDetailsFragmentToFitnessHealthDetailsFragment(
                            "Steps",
                            stepsHealthViewModel.stepData().toTypedArray()
                        )
                    )
                }
            }
        }

        binding.icHistoryCalendar.setOnClickListener {
            if (viewModel.graphInterval.value == GraphInterval.DAY || viewModel.graphInterval.value == GraphInterval.WEEK) {
                resultLauncher.launch(
                    HistoryCalendarActivity.getStartIntent(
                        requireContext(),
                        viewModel.selectedEndDate,
                        "watch"
                    )
                )
            } else if (viewModel.graphInterval.value == GraphInterval.MONTH) {
                setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                    val position = bundle.getInt("selectedPosition")
                    val selectedValue = bundle.getString("selectedValue")
                    selectedValue?.let { month ->
                        LOGS.i("$position | $selectedValue")
                        val (start, end) = DateFormats.getStartEndDateMonth(month)
                        viewModel.selectedStartDate = start
                        viewModel.selectedEndDate = end
                        viewModel.getStepsData()
                    }
                }

                navigate(
                    StepsDetailsFragmentDirections.actionStepsDetailsFragmentToValueSelectorBottomSheet(
                        DateFormats.getFormattedMonth(viewModel.selectedEndDate),
                        DateFormats.getHistoryMonths(localDataStore.getHistoryYears()),
                        "Select Month"
                    )
                )

            } else if (viewModel.graphInterval.value == GraphInterval.YEAR) {
                setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                    val position = bundle.getInt("selectedPosition")
                    val selectedValue = bundle.getString("selectedValue")
                    selectedValue?.let { year ->
                        LOGS.i("$position | $selectedValue")

                        val (start, end) = DateFormats.getStartEndDateYear(year)
                        viewModel.selectedStartDate = start
                        viewModel.selectedEndDate = end
                        viewModel.getStepsData()
                    }
                }
                navigate(
                    StepsDetailsFragmentDirections.actionStepsDetailsFragmentToValueSelectorBottomSheet(
                        DateFormats.getFormattedYear(viewModel.selectedEndDate),
                        DateFormats.getHistoryYear(localDataStore.getHistoryYears()),
                        "Select Year"
                    )
                )
            }
        }
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val selectedDate = data?.getStringExtra("selected_date")
                LOGS.d("Selected Date  :${selectedDate}")

                if (viewModel.graphInterval.value == GraphInterval.DAY) {
                    selectedDate?.let {
                        val (start, end) = DateFormats.getStartEndDateDay(selectedDate)
                        viewModel.selectedStartDate = start
                        viewModel.selectedEndDate = end
                    }
                } else if (viewModel.graphInterval.value == GraphInterval.WEEK) {
                    selectedDate?.let {
                        val (start, end) = DateFormats.getStartEndDateWeek(it)
                        viewModel.selectedStartDate = start
                        viewModel.selectedEndDate = end
                    }
                }
                viewModel.getStepsData()
            }
        }

    fun getFirstDayOfWeek(weekNumber: Int, locale: Locale?): LocalDate {
        return LocalDate
            .of(Year.now().getValue(), 2, 1)
            .with(WeekFields.of(locale).getFirstDayOfWeek())
            .with(WeekFields.of(locale).weekOfWeekBasedYear(), weekNumber.toLong())
    }

    fun getLastDayOfWeek(weekNumber: Int, locale: Locale?): LocalDate? {
        return getFirstDayOfWeek(weekNumber, locale).plusDays(6)
    }


    override fun subscribeObservers() {

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.stepsProgressPercent.observe(this) {
            binding.layoutStepsProgress.tvTotalStepsCount.text =
                when (viewModel.healthOverViewHistoryType) {
                    HealthOverViewHistoryType.Distance -> {
                        viewModel.distanceGoal
                    }
                    HealthOverViewHistoryType.Calories -> {
                        viewModel.caloriesGoal.toString()
                    }
                    HealthOverViewHistoryType.Steps -> {
                        viewModel.stepsGoal.toString()
                    }
                }


            binding.layoutStepsProgress.layouProgress.pbSteps.progress = it
            val percentage = "$it%"
            binding.layoutStepsProgress.tvCompletionPercent.text = percentage
        }

        viewModel.getLoading().observe(this) {
            if (it) binding.progressBar.root.visible() else binding.progressBar.root.gone()
        }

        viewModel.graphInterval.observe(this) {
            when (it) {
                GraphInterval.DAY -> {

                    setTabBackground(0)
                }
                GraphInterval.WEEK -> {

                    setTabBackground(1)
                }
                GraphInterval.MONTH -> {

                    setTabBackground(2)
                }
                GraphInterval.YEAR -> {

                    setTabBackground(3)
                }
                else -> {

                }
            }
            viewModel.selectedStartDate = null
            viewModel.selectedEndDate = null
            viewModel.getStepsData()
            binding.svMain.scrollTo(0, 0)

        }


        viewModel.stepsHistoryResponse.observe(this) {

            binding.svMain.visible()

            if (!it.step_activities.isNullOrEmpty()) {
                var count = 1
                var historyType = it.history_type ?: ""
                val date: String
                //   binding.tvDate.text = it.step_activities.first().date
                when (viewModel.graphInterval.value) {
                    GraphInterval.DAY -> {
                        count = TotalNumberOfDayGraphScroll
                        historyType = "daily"
                        date = viewModel.selectedDate

                    }
                    GraphInterval.WEEK -> {
                        date = viewModel.getWeeklyMonth(it.step_activities!!)

                    }
                    GraphInterval.MONTH -> {
                        date = viewModel.getCurrentMonth(it.step_activities!!)

                    }
                    GraphInterval.YEAR -> {
                        date = viewModel.getYear()
                    }
                    else -> {
                        throw NullPointerException("Invalid steps graph state")
                    }
                }

                var average = 0.0
                var total = 0L
                when (viewModel.healthOverViewHistoryType) {
                    HealthOverViewHistoryType.Distance -> {
                        average = it.avg?.distance ?: 0.0
                        total = it.total?.distance?.toLong() ?: 0L
                    }
                    HealthOverViewHistoryType.Calories -> {
                        average = it.avg?.calories ?: 0.0
                        total = it.total?.calories?.toLong() ?: 0L
                    }
                    HealthOverViewHistoryType.Steps -> {
                        average = it.avg?.steps ?: 0.0
                        total = it.total?.steps?.toLong() ?: 0L
                    }
                }


                setGraphAdapter(
                    count,
                    it.step_activities!!,
                    historyType,
                    average,
                    total,
                    date
                )
            } else {
                //TODO no Data UI
            }


        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                uiController.onDisplayError(message)
            }
        }
        viewModel.highlightResponse.observe(this) {

            if (it.max != null) {
                //Max

                var showMaxCard = false
                var max = ""
                var maxMsg = ""
                when (viewModel.healthOverViewHistoryType) {
                    HealthOverViewHistoryType.Distance -> {
                        showMaxCard = it.max.distance != 0L
                        maxMsg = it.max.distance_msg
                        max = viewModel.getDataUnitConverter()
                            .formatDistance(it.max.distance ?: 0, viewModel.unit)
                    }
                    HealthOverViewHistoryType.Calories -> {
                        maxMsg = it.max.calories_msg
                        showMaxCard = it.max.calories != 0L
                        max = it.max.calories?.toString() ?: "0"
                    }
                    HealthOverViewHistoryType.Steps -> {
                        maxMsg = it.max.msg
                        showMaxCard = it.max.steps != 0L
                        max = it.max.steps?.toString() ?: "0"
                    }
                }


                if (showMaxCard) {
                    binding.layoutMaxStepCount.tvDate.text = it.max.date //it.max.distance
                    binding.layoutMaxStepCount.tvTotalStepsCount.text = max
                    binding.layoutMaxStepCount.tvMaxStepsText.text = maxMsg
                    binding.layoutMaxStepCount.root.visible()
                } else {
                    binding.layoutMaxStepCount.root.gone()
                }


            }


            setDailyHighlightsData(it.daily)
            setWeeklyHighlightsData(it.weekly)
            setMonthlyHighlightsData(it.monthly)

        }
    }


    private fun setWeeklyHighlightsData(weekly: Weekly) {

        var current = ""
        var weeklyMsg = ""
        var last = ""
        var percentage: Pair<Int, Int>? = null
        when (viewModel.healthOverViewHistoryType) {
            HealthOverViewHistoryType.Distance -> {
                binding.layoutWeekly.layoutProgress2.pbSteps.apply {
                    setIndicatorColor(Color.parseColor("#f7e4b0"))
                }
                percentage = viewModel.calculateProgress(
                    weekly.current.distance,
                    weekly.last.distance
                )
                binding.layoutWeekly.tvUnitTodayDaily.text = viewModel.distanceUnit
                binding.layoutWeekly.tvUnitYestDaily.text = viewModel.distanceUnit
                weeklyMsg = weekly.distance_msg
                current = viewModel.getDataUnitConverter()
                    .formatDistance(weekly.current.distance ?: 0, viewModel.unit)
                last = viewModel.getDataUnitConverter()
                    .formatDistance(weekly.last.distance ?: 0, viewModel.unit)
            }
            HealthOverViewHistoryType.Calories -> {
                binding.layoutWeekly.layoutProgress2.pbSteps.apply {
                    setIndicatorColor(resources.getColor(R.color.calories_complete_goal))

                }
                percentage = viewModel.calculateProgress(
                    weekly.current.calories,
                    weekly.last.calories
                )
                binding.layoutWeekly.tvUnitTodayDaily.text = getString(R.string.text_kcal)
                binding.layoutWeekly.tvUnitYestDaily.text = getString(R.string.text_kcal)
                weeklyMsg = weekly.calories_msg
                current = weekly.current.calories?.toString() ?: "0"
                last = weekly.last.calories?.toString() ?: "0"
            }
            HealthOverViewHistoryType.Steps -> {
                binding.layoutWeekly.layoutProgress2.pbSteps.apply {
                    setIndicatorColor(Color.parseColor("#cbffdc"))

                }
                percentage = viewModel.calculateProgress(
                    weekly.current.steps,
                    weekly.last.steps
                )
                binding.layoutWeekly.tvUnitTodayDaily.text = getString(R.string.steps)
                binding.layoutWeekly.tvUnitYestDaily.text = getString(R.string.steps)
                weeklyMsg = weekly.msg
                current = weekly.current.steps?.toString() ?: "0"
                last = weekly.last.steps?.toString() ?: "0"
            }
        }
        binding.layoutWeekly.textView11.text = getString(R.string.text_weekly_highlights)
        binding.layoutWeekly.tvMessage.text = weeklyMsg
        binding.layoutWeekly.tvTotalStepsCount.text = current
        binding.layoutWeekly.textView13.text = weekly.current.label

        binding.layoutWeekly.tvCount2.text = last
        binding.layoutWeekly.tvData2Title.text = weekly.last.label


        binding.layoutWeekly.layoutProgress1.pbSteps.progress = percentage?.first ?: 0
        binding.layoutWeekly.layoutProgress2.pbSteps.apply {
            progress = percentage?.second ?: 0
        }
    }

    private fun setDailyHighlightsData(daily: Daily) {

        var today = ""
        var dailyMsg = ""
        var yesterday = ""
        var percentage: Pair<Int, Int>? = null
        when (viewModel.healthOverViewHistoryType) {
            HealthOverViewHistoryType.Distance -> {
                binding.layoutDaily.layoutProgress2.pbSteps.apply {
                    setIndicatorColor(Color.parseColor("#f7e4b0"))
                }
                percentage = viewModel.calculateProgress(
                    daily.today.distance,
                    daily.yesterday.distance
                )
                binding.layoutDaily.tvUnitTodayDaily.text = viewModel.distanceUnit
                binding.layoutDaily.tvUnitYestDaily.text = viewModel.distanceUnit
                dailyMsg = daily.distance_msg
                today = viewModel.getDataUnitConverter()
                    .formatDistance(daily.today.distance ?: 0, viewModel.unit)
                yesterday = viewModel.getDataUnitConverter()
                    .formatDistance(daily.yesterday.distance ?: 0, viewModel.unit)
            }
            HealthOverViewHistoryType.Calories -> {
                binding.layoutDaily.layoutProgress2.pbSteps.apply {
                    setIndicatorColor(resources.getColor(R.color.calories_complete_goal))
                }
                percentage = viewModel.calculateProgress(
                    daily.today.calories,
                    daily.yesterday.calories
                )
                binding.layoutDaily.tvUnitTodayDaily.text = getString(R.string.text_kcal)
                binding.layoutDaily.tvUnitYestDaily.text = getString(R.string.text_kcal)
                dailyMsg = daily.calories_msg
                today = daily.today.calories?.toString() ?: "0"
                yesterday = daily.yesterday.calories?.toString() ?: "0"
            }
            HealthOverViewHistoryType.Steps -> {
                binding.layoutDaily.layoutProgress2.pbSteps.apply {
                    setIndicatorColor(Color.parseColor("#cbffdc"))

                }
                percentage = viewModel.calculateProgress(
                    daily.today.steps,
                    daily.yesterday.steps
                )
                binding.layoutDaily.tvUnitTodayDaily.text = getString(R.string.steps)
                binding.layoutDaily.tvUnitYestDaily.text = getString(R.string.steps)
                dailyMsg = daily.msg
                today = daily.today.steps?.toString() ?: "0"
                yesterday = daily.yesterday.steps?.toString() ?: "0"
            }
        }


        binding.layoutDaily.textView11.text = getString(R.string.text_daily_highlights)
        binding.layoutDaily.tvMessage.text = dailyMsg
        binding.layoutDaily.tvTotalStepsCount.text = today

        binding.layoutDaily.textView13.text = daily.today.label

        binding.layoutDaily.tvCount2.text = yesterday

        binding.layoutDaily.tvData2Title.text = daily.yesterday.label


        binding.layoutDaily.layoutProgress1.pbSteps.progress = percentage?.first ?: 0
        binding.layoutDaily.layoutProgress2.pbSteps.apply {
            progress = percentage?.second ?: 0
        }
    }

    private fun setMonthlyHighlightsData(monthly: Monthly) {

        var current = ""
        var monthlyMsg = ""
        var last = ""
        var percentage: Pair<Int, Int>? = null
        when (viewModel.healthOverViewHistoryType) {
            HealthOverViewHistoryType.Distance -> {
                binding.layoutMonthly.layoutProgress2.pbSteps.apply {
                    setIndicatorColor(Color.parseColor("#f7e4b0"))
                }
                percentage = viewModel.calculateProgress(
                    monthly.current.distance,
                    monthly.last.distance
                )
                binding.layoutMonthly.tvUnitTodayDaily.text = viewModel.distanceUnit
                binding.layoutMonthly.tvUnitYestDaily.text = viewModel.distanceUnit
                monthlyMsg = monthly.distance_msg
                current = viewModel.getDataUnitConverter()
                    .formatDistance(monthly.current.distance ?: 0, viewModel.unit)
                last = viewModel.getDataUnitConverter()
                    .formatDistance(monthly.last.distance ?: 0, viewModel.unit)
            }
            HealthOverViewHistoryType.Calories -> {
                if(monthly.last.calories == null){
                    monthly.last.calories = 0
                }
                binding.layoutMonthly.layoutProgress2.pbSteps.apply {
                    setIndicatorColor(resources.getColor(R.color.calories_complete_goal))

                }

                percentage = viewModel.calculateProgress(
                    monthly.current.calories,
                    monthly.last.calories
                )
                binding.layoutMonthly.tvUnitTodayDaily.text = getString(R.string.text_kcal)
                binding.layoutMonthly.tvUnitYestDaily.text = getString(R.string.text_kcal)
                monthlyMsg = monthly.calories_msg
                current = monthly.current.calories?.toString() ?: "0"
                last = monthly.last.calories?.toString() ?: "0"
            }
            HealthOverViewHistoryType.Steps -> {
                binding.layoutMonthly.layoutProgress2.pbSteps.apply {
                    setIndicatorColor(Color.parseColor("#cbffdc"))

                }
                percentage = viewModel.calculateProgress(
                    monthly.current.steps,
                    monthly.last.steps
                )
                binding.layoutMonthly.tvUnitTodayDaily.text = getString(R.string.steps)
                binding.layoutMonthly.tvUnitYestDaily.text = getString(R.string.steps)
                monthlyMsg = monthly.msg
                current = monthly.current.steps?.toString() ?: "0"
                last = monthly.last.steps?.toString() ?: "0"
            }
        }


        binding.layoutMonthly.textView11.text = getString(R.string.text_monthly_highlights)
        binding.layoutMonthly.tvMessage.text = monthlyMsg
        binding.layoutMonthly.tvTotalStepsCount.text = current
        binding.layoutMonthly.textView13.text = monthly.current.label

        binding.layoutMonthly.tvCount2.text = last
        binding.layoutMonthly.tvData2Title.text = monthly.last.label

        binding.layoutMonthly.layoutProgress1.pbSteps.progress = percentage?.first ?: 0
        binding.layoutMonthly.layoutProgress2.pbSteps.apply {
            progress = percentage?.second ?: 0

        }
    }


    private fun setTabBackground(position: Int) {
        val intervals = arrayOf(binding.lytDay, binding.lytWeek, binding.lytMonth, binding.lytYear)
        intervals.forEachIndexed { index, binding ->

            if (index == position) {
                binding.tvTabTitle.setTextColor(binding.tvTabTitle.context.getColor(R.color.accent_color_purple))
                binding.vBottom.visible()
            } else {
                binding.tvTabTitle.setTextColor(Color.parseColor("#a3ffffff"))
                binding.vBottom.invisible()
            }
        }
    }

    private inner class StepsGraphSlidePagerAdapter(
        fa: Fragment,
        val count: Int,
        val stepsDataList: ArrayList<StepsHistoryData>,
        val historyType: String,
        val average: Double,
        val steps: Long,
        val date: String,
        val healthOverViewHistoryType: HealthOverViewHistoryType
    ) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = count

        override fun createFragment(position: Int): Fragment {

            return if (viewModel.graphInterval.value == GraphInterval.DAY) {
                try {
                    val todayData = stepsDataList[position]

                    val hourlySteps = todayData.hourly_breakup ?: ArrayList()

                    val steps = when (healthOverViewHistoryType) {
                        HealthOverViewHistoryType.Calories -> {
                            todayData.calories ?: 0
                        }
                        HealthOverViewHistoryType.Steps -> {
                            todayData.steps ?: 0
                        }
                        HealthOverViewHistoryType.Distance -> {
                            todayData.distance ?: 0
                        }
                    }



                    logInsiderAppEvent(viewModel.graphInterval.value.toString())
                    StepsGraphFragment.newInstance(
                        ArrayList(hourlySteps),
                        historyType,
                        average,
                        todayData.date ?: "",
                        steps,
                        healthOverViewHistoryType.name
                    )

                } catch (exp: Exception) {
                    exp.printStackTrace()
                    logInsiderAppEvent(viewModel.graphInterval.value.toString())
                    StepsGraphFragment()
                }
            } else {
                logInsiderAppEvent(viewModel.graphInterval.value.toString())
                StepsGraphFragment.newInstance(
                    stepsDataList,
                    historyType,
                    average,
                    date,
                    steps,
                    healthOverViewHistoryType.name
                )
            }
        }

    }

    private fun logInsiderAppEvent(historyType: String) {
        if (historyType.equals(GraphInterval.DAY.name, false)) {
            if (HealthOverViewHistoryType.Distance == viewModel.healthOverViewHistoryType)
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.DISTANCE_DAY_CLICK)
            else if (HealthOverViewHistoryType.Steps == viewModel.healthOverViewHistoryType)
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.STEPS_DAY_CLICK)
            else
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CALORIES_DAY_CLICK)
        } else if (historyType.equals(GraphInterval.WEEK.name, false))
            if (HealthOverViewHistoryType.Distance == viewModel.healthOverViewHistoryType)
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.DISTANCE_WEEK_CLICK)
            else if (HealthOverViewHistoryType.Steps == viewModel.healthOverViewHistoryType)
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.STEPS_WEEK_CLICK)
            else
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CALORIES_WEEK_CLICK)
        else if (historyType.equals(GraphInterval.MONTH.name, false)) {
            if (HealthOverViewHistoryType.Distance == viewModel.healthOverViewHistoryType)
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.DISTANCE_MONTH_CLICK)
            else if (HealthOverViewHistoryType.Steps == viewModel.healthOverViewHistoryType)
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.STEPS_MONTH_CLICK)
            else
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CALORIES_MONTH_CLICK)
        } else {
            if (HealthOverViewHistoryType.Distance == viewModel.healthOverViewHistoryType)
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.DISTANCE_YEAR_CLICK)
            else if (HealthOverViewHistoryType.Steps == viewModel.healthOverViewHistoryType)
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.STEPS_YEAR_CLICK)
            else
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.CALORIES_YEAR_CLICK)
        }
    }

}