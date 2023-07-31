package com.noisefit.ui.dashboard.graphs.temperature

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
import com.noisefit.luna.R
import com.noisefit_commans.data.model.history.BodyTempHistory
import com.noisefit.luna.databinding.FragmentBodyTemperatureDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.graphs.HistoryCalendarActivity
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.ui.dashboard.graphs.steps.StepsDetailsFragmentDirections
import com.noisefit.ui.dashboard.graphs.stress.StressGraphFragment
import com.noisefit.ui.settings.help.FitnessHealthViewModel
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TotalNumberOfDayGraphScroll = 7

@AndroidEntryPoint
class BodyTemperatureDetailsFragment :
    BaseFragment<FragmentBodyTemperatureDetailsBinding>(FragmentBodyTemperatureDetailsBinding::inflate) {

    private val viewModel: BodyTemperatureDetailsViewModel by viewModels()
    private var graphAdapter: BodyTempSlidePagerAdapter? = null
    private val healthViewModel: FitnessHealthViewModel by viewModels()

    @Inject
    lateinit var localDataStore: DataStoredInterface

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        binding.lytDay.tvTabTitle.text = getString(R.string.text_day)
        binding.lytWeek.tvTabTitle.text = getString(R.string.text_week)
        binding.lytMonth.tvTabTitle.text = getString(R.string.text_month)
        binding.lytYear.tvTabTitle.text = getString(R.string.text_year)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            binding.icHistoryCalendar.gone()
            binding.icCalendar.gone()
        }

    }


    private fun setGraphAdapter(
        count: Int,
        stepsDataList: ArrayList<BodyTempHistory>,
        historyType: String,
        range: String,
        steps: Float,
        date: String
    ) {
        graphAdapter = BodyTempSlidePagerAdapter(
            this,
            count,
            stepsDataList,
            historyType,
            range,
            steps,
            date
        )
        binding.viewPagerGraph.adapter = graphAdapter
        binding.viewPagerGraph.setCurrentItem(count, false)
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
            navigate(
                BodyTemperatureDetailsFragmentDirections.actionBodyTemperatureDetailsFragmentToFitnessHealthDetailsFragment(
                    "Body Temperature",
                    healthViewModel.bodyTemperatureData().toTypedArray()
                )
            )
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
                        viewModel.getStressData()
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
                        viewModel.getStressData()
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
                viewModel.getStressData()
            }
        }


    override fun subscribeObservers() {

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) binding.progressBar.root.visible() else binding.progressBar.root.gone()
        }


        viewModel.graphInterval.observe(viewLifecycleOwner) {
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
            viewModel.getStressData()
            binding.svMain.scrollTo(0, 0)
        }


        viewModel.bodyTempHistoryResponse.observe(viewLifecycleOwner) {
            binding.svMain.visible()

            if (!it.history.isNullOrEmpty()) {
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
                        date = viewModel.getWeeklyMonth(it.history!!)

                    }
                    GraphInterval.MONTH -> {
                        date = viewModel.getCurrentMonth(it.history!!)

                    }
                    GraphInterval.YEAR -> {
                        date = viewModel.getYear()
                    }
                    else -> {
                        throw NullPointerException("Invalid steps graph state")
                    }
                }

                val range = "${viewModel.getBodyTemp(it.cumulative?.min ?: 0f)}-${
                    viewModel.getBodyTempWithUnit(it.cumulative?.max ?: 0f)
                }"
                setGraphAdapter(
                    count,
                    it.history!!,
                    historyType,
                    range,
                    it.cumulative?.count ?: 0f,
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

    private inner class BodyTempSlidePagerAdapter(
        fa: Fragment,
        val count: Int,
        val stepsDataList: ArrayList<BodyTempHistory>,
        val historyType: String,
        val range: String,
        val steps: Float,
        val date: String
    ) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = count

        override fun createFragment(position: Int): Fragment {

            return if (viewModel.graphInterval.value == GraphInterval.DAY) {
                try {
                    val todayData = stepsDataList[position]
                    val hourlySteps = todayData.hourly_breakup ?: ArrayList()
                    val steps = todayData.count ?: 0f
                    val range = "${viewModel.getBodyTemp(todayData.minCount ?: 0f)}-${
                        viewModel.getBodyTempWithUnit(todayData.maxCount ?: 0f)
                    }"

                    logInsiderAppEvent(historyType)
                    BodyTemperatureGraphFragment.newInstance(
                        hourlySteps,
                        historyType,
                        range,
                        todayData.date ?: "",
                        steps
                    )


                } catch (exp: Exception) {
                    exp.printStackTrace()
                    logInsiderAppEvent(historyType)
                    StressGraphFragment()
                }
            } else {
                logInsiderAppEvent(historyType)
                BodyTemperatureGraphFragment.newInstance(
                    stepsDataList,
                    historyType,
                    range,
                    date,
                    steps
                )
            }
        }

    }

    private fun logInsiderAppEvent(historyType: String) {
        if (historyType.equals("daily", true)) {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.BODYTEMPERATURE_DAY_CLICK)
        } else if (historyType.equals("weekly", true))
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.BODYTEMPERATURE_WEEK_CLICK)
        else if (historyType.equals("monthly", true)) {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.BODYTEMPERATURE_MONTH_CLICK)
        } else {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.BODYTEMPERATURE_YEAR_CLICK)
        }
    }


}