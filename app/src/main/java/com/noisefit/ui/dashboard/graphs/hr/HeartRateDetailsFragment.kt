package com.noisefit.ui.dashboard.graphs.hr

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
import com.noisefit_commans.data.model.history.HrBreakup
import com.noisefit.databinding.FragmentHeartRateDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.graphs.HistoryCalendarActivity
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.ui.settings.help.FitnessHealthViewModel
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HeartRateDetailsFragment :
    BaseFragment<FragmentHeartRateDetailsBinding>(FragmentHeartRateDetailsBinding::inflate) {

    private val viewModel: HeartRateDetailsViewModel by viewModels()
    private var graphAdapter: HrGraphSlidePagerAdapter? = null
    private val healthViewModel: FitnessHealthViewModel by viewModels()

    @Inject
    lateinit var localDataStore: DataStoredInterface


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HEART_RATE_PAGE_VIEWED)

        binding.lytDay.tvTabTitle.text = getString(R.string.text_day)
        binding.lytWeek.tvTabTitle.text = getString(R.string.text_week)
        binding.lytMonth.tvTabTitle.text = getString(R.string.text_month)
        binding.lytYear.tvTabTitle.text = getString(R.string.text_year)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            binding.icHistoryCalendar.gone()
            binding.icCalendar.gone()
        }

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
                HeartRateDetailsFragmentDirections.actionHeartRateDetailsFragmentToFitnessHealthDetailsFragment(
                    "Heart Rate",
                    healthViewModel.tempHeartRateData().toTypedArray()
                )
            )
        }

        binding.viewPagerGraph.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                try {
                    if (viewModel.graphInterval.value == GraphInterval.DAY) {
                        viewModel.hrHistoryResponse.value?.heart_rates?.get(position)?.let {


                            val restingHr = it.restingHr ?: 0
                            if (restingHr > 0) {
                                binding.layoutRestingHr.root.visible()
                                binding.layoutRestingHr.tvHr.text = "$restingHr"

                                val hrRating = viewModel.getRestingHrRating(restingHr)
                                if (hrRating != HRRanges.NO_VALUE) {
                                    binding.layoutRestingHr.tvHrValue.text =
                                        hrRating.name.lowercase().replaceFirstChar { char ->
                                            if (char.isLowerCase()) char.titlecase(
                                                Locale.getDefault()
                                            ) else char.toString()
                                        }
                                }
                                val progress = when (hrRating) {
                                    HRRanges.LOW -> 20
                                    HRRanges.EXCELLENT -> 40
                                    HRRanges.GOOD -> 60
                                    HRRanges.FAST -> 80
                                    HRRanges.HIGH -> 100
                                    HRRanges.NO_VALUE -> 0
                                }
                                binding.layoutRestingHr.layoutProgress.pbSteps.setProgress(progress)
                            } else {
                                binding.layoutRestingHr.root.gone()
                            }


                            /*binding.layoutRestingHr.layoutProgress.pbSteps.setProgress(position * 20)
                            binding.layoutRestingHr.tvHr.text = "${position * 20}"*/


                        }
                    }
                } catch (exp: Exception) {
                    //Out of bound
                    exp.printStackTrace()
                }
            }

        })


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
            }
            viewModel.selectedStartDate = null
            viewModel.selectedEndDate = null
            viewModel.getHrData()
            binding.svMain.scrollTo(0, 0)

        }

        viewModel.hrHistoryResponse.observe(viewLifecycleOwner) {
            /*  binding.tvDataStart.text = "${it.cumulative?.min ?: 0} - ${it.cumulative?.max ?: 0}"
              binding.tvAverageSteps.text = "${it.cumulative?.avg ?: 0}"

              binding.layoutRestingHr.tvHr.text = "${it.cumulative?.restingHr ?: 0}"
              binding.layoutRestingHr.layoutProgress.pbSteps.progress = it.cumulative?.restingHr ?: 0

              if (it.cumulative?.date != null) {
                  binding.tvDate.text = it.cumulative.date
              } else {
                  binding.tvDate.text = ""
              }*/

            binding.svMain.visible()

            if (it.heart_rates == null) {
                return@observe
            }
            var date = ""
            var count = 1
            var historyType = ""
            when (viewModel.graphInterval.value) {
                GraphInterval.DAY -> {
                    date = ""
                    count = 7
                    historyType = "daily"
                }
                GraphInterval.WEEK -> {
                    date = viewModel.getWeeklyMonth(it.heart_rates!!)
                    historyType = "weekly"

                }
                GraphInterval.MONTH -> {
                    date = viewModel.getCurrentMonth(it.heart_rates!!)
                    historyType = "monthly"

                }
                GraphInterval.YEAR -> {
                    date = viewModel.getYear()
                    historyType = "yearly"
                }
                else -> {
                    throw Exception("heart rate invalid state")
                }
            }

            val stats = "${it.cumulative?.min ?: 0} - ${it.cumulative?.max ?: 0}"
            val avg = it.cumulative?.avg ?: 0
            setGraphAdapter(count, it.heart_rates!!, historyType, stats, avg, date)
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
                        viewModel.getHrData()
                    }
                }
                navigate(
                    HeartRateDetailsFragmentDirections.actionHeartRateDetailsFragmentToValueSelectorBottomSheet(
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
                        viewModel.getHrData()
                    }


                }
                navigate(
                    HeartRateDetailsFragmentDirections.actionHeartRateDetailsFragmentToValueSelectorBottomSheet(
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
                viewModel.getHrData()
            }
        }


    private fun setGraphAdapter(
        count: Int,
        hrData: List<HrBreakup>,
        historyType: String,
        stats: String,
        average: Int,
        date: String
    ) {
        graphAdapter =
            HrGraphSlidePagerAdapter(this, count, hrData, historyType, stats, average, date)
        binding.viewPagerGraph.adapter = graphAdapter
        binding.viewPagerGraph.setCurrentItem(count, false)
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

    private inner class HrGraphSlidePagerAdapter(
        fa: Fragment,
        val count: Int,
        val hrData: List<HrBreakup>,
        val historyType: String,
        val stats: String,
        val average: Int,
        val date: String
    ) :
        FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = count

        override fun createFragment(position: Int): Fragment {
            return if (historyType.equals("daily", true)) {
                try {

                    val todayData = hrData[position]
                    val hourlyHr = todayData.hourlyBreakUp ?: ArrayList()
                    val stats = "${todayData.min ?: 0} - ${todayData.max ?: 0}"
                    logInsiderAppEvent(historyType)
                    HrDayGraphFragment.newInstance(
                        hourlyHr,
                        historyType,
                        stats,
                        todayData.avg ?: 0,
                        todayData.date ?: ""
                    )

                } catch (exp: Exception) {
                    logInsiderAppEvent(historyType)
                    HrDayGraphFragment()
                }
            } else {
                //HrGraphFragment()
                logInsiderAppEvent(historyType)
                HrGraphFragment.newInstance(hrData, historyType, stats, average, date)
            }
        }
    }
    private fun logInsiderAppEvent(historyType: String) {
        if (historyType.equals("daily", true)) {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HEART_RATE_DAY_CLICK)
        } else if (historyType.equals("weekly", true))
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HEART_RATE_WEEK_CLICK)
        else if (historyType.equals("monthly", true)) {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HEART_RATE_MONTH_CLICK)
        } else {
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HEART_RATE_YEAR_CLICK)
        }
    }
}