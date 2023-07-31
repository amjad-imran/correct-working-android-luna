package com.noisefit.ui.dashboard.graphs.bloodOxygen

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit_commans.data.model.history.BoHistory
import com.noisefit.luna.databinding.FragmentBloodOxygenDetailsBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit.ui.dashboard.graphs.HistoryCalendarActivity
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.ui.settings.help.FitnessHealthViewModel
import com.noisefit.util.ImageUtil
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.ScreenUtils
import com.noisefit.util.graph.StressBarChartUtils
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TotalNumberOfDayGraphScroll = 7

@AndroidEntryPoint
class BloodOxygenDetailsFragment :
    BaseFragment<FragmentBloodOxygenDetailsBinding>(FragmentBloodOxygenDetailsBinding::inflate) {

    private val viewModel: BloodOxygenDetailsViewModel by viewModels()
    private var graphAdapter: BloodOxygenSlidePagerAdapter? = null
    private val healthViewModel: FitnessHealthViewModel by viewModels()

    @Inject
    lateinit var screenUtils: ScreenUtils

    @Inject
    lateinit var imageUtil: ImageUtil

    @Inject
    lateinit var localDataStore: DataStoredInterface

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.BLOOD_OXYGEN_PAGE_VIEWED)
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
        boDataList: ArrayList<BoHistory>,
        historyType: String,
        range: String,
        steps: Int,
        date: String
    ) {
        graphAdapter = BloodOxygenSlidePagerAdapter(
            this,
            count,
            boDataList,
            historyType,
            range,
            steps,
            date
        )
        binding.viewPagerGraph.adapter = graphAdapter
        binding.viewPagerGraph.setCurrentItem(count, false)
        binding.viewPagerGraph.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //  Log.e("Selected_Page", position.toString())
                if (viewModel.graphInterval.value == GraphInterval.DAY) {
                    viewModel.boHistoryResponse.value?.history?.get(position)?.let {
                        viewModel.updateSpo2GoalPercentage(it.count)

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


        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvLearnMore.setOnClickListener {
            navigate(
                BloodOxygenDetailsFragmentDirections.actionBloodOxygenDetailsFragmentToFitnessHealthDetailsFragment(
                    getString(R.string.text_blood_oxygen_title),
                    healthViewModel.bloodOxygenData().toTypedArray()
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
                        viewModel.getBoData()
                    }
                }
                navigate(
                    BloodOxygenDetailsFragmentDirections.actionBloodOxygenDetailsFragmentToValueSelectorBottomSheet(
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
                        viewModel.getBoData()
                    }


                }
                navigate(
                    BloodOxygenDetailsFragmentDirections.actionBloodOxygenDetailsFragmentToValueSelectorBottomSheet(
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
                viewModel.getBoData()
            }
        }


    override fun subscribeObservers() {

        viewModel.spo2ProgressPercent.observe(this) {
            //binding.lytBoQuality.pbBo.setProgress(it)
            val percentage = "$it%"
            binding.lytBoQuality.tvBoScore.text = percentage.replaceUnderScore()
            if (it > 0) {
                val spo2Value = StressBarChartUtils.bloodOxygenType(it)
                binding.lytBoQuality.tvBoLevel.text =
                    getString(R.string.text_your_blood_oxygen_level_is_normal, spo2Value)

            } else {
                binding.lytBoQuality.tvBoLevel.text = ""
            }

            val totalWidth =
                resources.displayMetrics.widthPixels - screenUtils.dpToPx(
                    80,
                    requireContext()
                )

            val barWidth =
                (it!!.toFloat() / 100) * totalWidth


            val gd = GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT,
                imageUtil.getBOGradientList(it)
            )
            gd.cornerRadius = screenUtils.dpToPx(2, requireContext())
            binding.lytBoQuality.pbBo.background = gd

            val params = binding.lytBoQuality.pbBo.layoutParams
            params.width = barWidth.toInt()
            binding.lytBoQuality.pbBo.layoutParams = params


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
            viewModel.getBoData()
            binding.svMain.scrollTo(0, 0)
        }


        viewModel.boHistoryResponse.observe(viewLifecycleOwner) {
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

                val range = "${it.cumulative?.min ?: 0}-${it.cumulative?.max ?: 0}"
                setGraphAdapter(
                    count,
                    it.history!!,
                    historyType,
                    range,
                    it.cumulative?.count ?: 0,
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

    private inner class BloodOxygenSlidePagerAdapter(
        fa: Fragment,
        val count: Int,
        val boDataList: ArrayList<BoHistory>,
        val historyType: String,
        val range: String,
        val steps: Int,
        val date: String
    ) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = count

        override fun createFragment(position: Int): Fragment {

            return if (viewModel.graphInterval.value == GraphInterval.DAY) {
                try {
                    val todayData = boDataList[position]
                    val hourlySteps = todayData.hourly_breakup ?: ArrayList()
                    val steps = todayData.count ?: 0
                    val range = "${todayData.minCount ?: 0}-${todayData.maxCount ?: 0}"


                    logInsiderAppEvent(historyType)
                    BloodOxygenGraphFragment.newInstance(
                        hourlySteps,
                        historyType,
                        range,
                        todayData.date ?: "",
                        steps
                    )


                } catch (exp: Exception) {
                    exp.printStackTrace()
                    logInsiderAppEvent(historyType)
                    BloodOxygenGraphFragment()
                }
            } else {
                logInsiderAppEvent(historyType)
                BloodOxygenGraphFragment
                    .newInstance(
                        boDataList,
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
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.BLOOD_OXYGEN_DAY_CLICK)
        } else if (historyType.equals("weekly", true))
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.BLOOD_OXYGEN_WEEK_CLICK)
        else if (historyType.equals("monthly", true)) {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.BLOOD_OXYGEN_MONTH_CLICK)
        } else {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.BLOOD_OXYGEN_YEAR_CLICK)
        }
    }


}