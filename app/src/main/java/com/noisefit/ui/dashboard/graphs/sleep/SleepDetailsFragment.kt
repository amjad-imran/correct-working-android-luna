package com.noisefit.ui.dashboard.graphs.sleep

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.data.SleepAbout
import com.noisefit.data.dataConverter.OfflineDataMapper
import com.noisefit_commans.data.model.SleepExtraData
import com.noisefit_commans.data.response.SleepBlogCategories
import com.noisefit_commans.data.response.SleepHighlightResponse
import com.noisefit.luna.databinding.FragmentSleepDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.graphs.HistoryCalendarActivity
import com.noisefit.ui.dashboard.graphs.steps.GraphInterval
import com.noisefit.util.ApplicationUtils
import com.noisefit.util.ImageUtil
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.ScreenUtils
import com.noisefit.util.graph.SleepChartUtils
import com.noisefit.watch.SDKWatchType
import com.noisefit_commans.data.enums.SleepExtraType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.model.SleepStageAnalysis
import com.noisefit_commans.models.SleepType
import com.noisefit_commans.response.SleepBreakup
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class SleepDetailsFragment :
    BaseFragment<FragmentSleepDetailsBinding>(FragmentSleepDetailsBinding::inflate) {

    private val viewModel: SleepDetailsViewModel by viewModels()
    private val sharedViewModel: SleepSharedViewModel by activityViewModels()

    private val sleepExtraDataAdapter by lazy { SleepExtraDataAdapter() }
    private val sleepStageAdapter by lazy { SleepStageAnalysisAdapter() }
    private var graphAdapter: SleepGraphSlidePagerAdapter? = null

    @Inject
    lateinit var screenUtils: ScreenUtils

    @Inject
    lateinit var offlineDataMapper: OfflineDataMapper

    @Inject
    lateinit var imageUtil: ImageUtil

    private var lastExtraSelectedIndex = -1

    @Inject
    lateinit var localDataStore: DataStoredInterface

    private val sleepBlogAdapter: SleepBlogAdapter by lazy {
        SleepBlogAdapter(object : SleepBlogCardClickListener {
            override fun onSleepBlogCardClicked(blog: SleepBlogCategories) {
                navigate(
                    SleepDetailsFragmentDirections.actionSleepDetailsFragmentToSleepBlogDetailFragment(
                        blog
                    )
                )
            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.SLEEP_PAGE_VIEWED)

        binding.lytDay.tvTabTitle.text = getString(R.string.text_day)
        binding.lytWeek.tvTabTitle.text = getString(R.string.text_week)
        binding.lytMonth.tvTabTitle.text = getString(R.string.text_month)
        binding.lytYear.tvTabTitle.text = getString(R.string.text_year)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            binding.icHistoryCalendar.gone()
            binding.icCalendar.gone()
        }

        setRecycler()
        binding.layoutSleepQuality.pbSleep.setBackgroundColor(resources.getColor(R.color.modal_color))

        viewModel.getHighlights()
    }

    private fun setRecycler() {
        binding.layoutSleepStage.rvSleepStage.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sleepStageAdapter
        }
        binding.rvStats.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = sleepExtraDataAdapter
        }
        binding.layoutAboutSleep.rvSleepAbout.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sleepBlogAdapter
        }

        /*sleepExtraDataAdapter.setData(getSleepExtraList())*/

        sleepExtraDataAdapter.setInterface(object :
            SleepExtraDataAdapter.SleepExtraInteractionListener {
            override fun onDetailClicked(
                overlayType: SleepExtraType,
                position: Int
            ) {
                if (lastExtraSelectedIndex != -1) {
                    sleepExtraDataAdapter.setData(SleepExtraType.NONE, lastExtraSelectedIndex)
                }

                lastExtraSelectedIndex = position
                sleepExtraDataAdapter.setData(overlayType, lastExtraSelectedIndex)

                //setOverlayGraph(overlayType)
                sharedViewModel.setSelected(overlayType)

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

        binding.viewPagerGraph.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                try {
                    if (viewModel.graphInterval.value == GraphInterval.DAY) {
                        viewModel.sleepHistoryResponse.value?.sleepHistory?.get(position)?.let {
                            setSleepAnalysis(it)
                            if (it.sleepScore == null || it.sleepScore == 0) {
                                binding.layoutSleepQuality.root.gone()
                            } else {
                                binding.layoutSleepQuality.tvSleepScore.text =
                                    "${it.sleepScore ?: "_"}"
                                //binding.layoutSleepQuality.pbSleep.setProgress(it.sleepScore!!)
                                binding.layoutSleepQuality.root.visible()
                                val (sleepScore, sleepMessage) = SleepChartUtils.getSleepScoreMessage(
                                    it.sleepScore!!
                                )
                                binding.layoutSleepQuality.textView9.text = sleepScore
                                binding.layoutSleepQuality.tvSleepScoreMessage.text = sleepMessage

                                val totalWidth =
                                    resources.displayMetrics.widthPixels - screenUtils.dpToPx(
                                        48,
                                        requireContext()
                                    )

                                val barWidth =
                                    (it.sleepScore!!.toFloat() / 100) * totalWidth


                                val gd = GradientDrawable(
                                    GradientDrawable.Orientation.RIGHT_LEFT,
                                    imageUtil.getGradientList(it.sleepScore ?: 0)
                                )
                                gd.cornerRadius = screenUtils.dpToPx(2, requireContext())
                                binding.layoutSleepQuality.pbSleep.background = gd

                                val params = binding.layoutSleepQuality.pbSleep.layoutParams
                                params.width = barWidth.toInt()
                                binding.layoutSleepQuality.pbSleep.layoutParams = params

                            }

                            sleepExtraDataAdapter.setData(
                                viewModel.generateSleepExtraData(
                                    it,
                                    sharedViewModel.isSelected.value
                                )
                            )
                        }

                    }
                } catch (exp: Exception) {
                    //Out of bound
                    exp.printStackTrace()
                }
            }

        })

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
                        viewModel.getSleepData()
                    }
                }
                navigate(
                    SleepDetailsFragmentDirections.actionSleepDetailsFragmentToValueSelectorBottomSheet(
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
                        viewModel.getSleepData()
                    }


                }
                navigate(
                    SleepDetailsFragmentDirections.actionSleepDetailsFragmentToValueSelectorBottomSheet(
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
                viewModel.getSleepData()
            }
        }

    override fun subscribeObservers() {

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) binding.progressBar.root.visible() else binding.progressBar.root.gone()
        }


        viewModel.graphInterval.observe(viewLifecycleOwner) {
            when (it) {
                GraphInterval.DAY -> {
                    setTabBackground(0)
                    binding.rvStats.visible()
                }
                GraphInterval.WEEK -> {
                    setTabBackground(1)
                    binding.rvStats.visible()
                }
                GraphInterval.MONTH -> {
                    setTabBackground(2)
                    binding.rvStats.visible()
                }
                GraphInterval.YEAR -> {
                    setTabBackground(3)
                    binding.rvStats.gone()
                }
            }
            viewModel.selectedStartDate = null
            viewModel.selectedEndDate = null
            viewModel.getSleepData()
            binding.svSleep.scrollTo(0, 0)
            sleepExtraDataAdapter.reset()
            sharedViewModel.setSelected(SleepExtraType.NONE)
        }

        viewModel.sleepHistoryResponse.observe(viewLifecycleOwner) {
            binding.svSleep.visible()
            sleepExtraDataAdapter.setData(getSleepExtraList(it.sleepHistory))


            if (it.sleepHistory.isNullOrEmpty()) {
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
                    date = viewModel.getWeeklyMonth(it.sleepHistory?:ArrayList())
                    historyType = "weekly"

                }
                GraphInterval.MONTH -> {
                    date = viewModel.getCurrentMonth(it.sleepHistory?:ArrayList())
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

            val averageBedTime = DateFormats.convert24HourTo12(it.avg_bedtime)

            setGraphAdapter(
                count, it.sleepHistory?:ArrayList(), historyType, it.avg_duration ?: 0,
                averageBedTime/*"_:_ PM"*/, date
            )

        }

        viewModel.sleepHighlightResponse.observe(viewLifecycleOwner) {


            it.bedtime_variance?.let { variance ->
                if (variance.available) {
                    SleepChartUtils.setBedTimeVarianceGraph(
                        binding.layoutSleepHighlights.graphBedTimeVariance,
                        requireContext(),
                        viewModel.parseBedTimeVariance(variance),
                        viewModel.getXAxisMarker(variance)
                    )

                    binding.layoutSleepHighlights.tvBedTimeVariance.text =
                        "${(variance.avg ?: 0.0).roundToInt()}"
                    binding.layoutSleepHighlights.tvBedTimeVarianceUnit.text = "mins"
                    binding.layoutSleepHighlights.graphBedTimeVariance.visible()
                    binding.layoutSleepHighlights.vCentreBedTimeVariance.visible()
                    binding.layoutSleepHighlights.layoutBedTime.root.visible()
                    binding.layoutSleepHighlights.tvBedTimeVariance.visible()
                    binding.layoutSleepHighlights.tvBedTimeVarianceUnit.visible()
                } else {
                    binding.layoutSleepHighlights.graphBedTimeVariance.gone()
                    binding.layoutSleepHighlights.vCentreBedTimeVariance.gone()
                    binding.layoutSleepHighlights.layoutBedTime.root.gone()
                    binding.layoutSleepHighlights.tvBedTimeVariance.gone()
                    binding.layoutSleepHighlights.tvBedTimeVarianceUnit.gone()
                }
            }

            setHighlightsGraphs(it)

        }

        viewModel.sleepBlogs.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                binding.layoutAboutSleep.root.visible()
                sleepBlogAdapter.setDataSet(it)
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

    }

    private fun setSleepAnalysis(it: SleepBreakup) {

        val awake = it.awake ?: 0
        val rem = it.rem ?: 0
        val light = it.light ?: 0
        val deep = it.deep ?: 0

        val totalSleep = offlineDataMapper.getTotalSleep(it.total_duration, it.awake ?: 0)

        var deepPer = 0
        var awakePer = 0
        var remPer = 0
        var lightPer = 0

        if (totalSleep != 0) {
            deepPer = ((deep.toFloat() / totalSleep.toFloat()) * 100).toInt()
            awakePer = ((awake.toFloat() / totalSleep.toFloat()) * 100).toInt()
            remPer = ((rem.toFloat() / totalSleep.toFloat()) * 100).toInt()
            lightPer = 100 - (deepPer + awakePer + remPer)
        }

        val sleepStageList = ArrayList<SleepStageAnalysis>()
        sleepStageList.add(
            SleepStageAnalysis(
                "Awake",
                awake,
                awakePer,
                SleepType.AWAKE
            )
        )


        if(viewModel.watchesSDK.getWatchType() !=  SDKWatchType.SDK_RYEEX){
            sleepStageList.add(
                SleepStageAnalysis(
                    "REM Sleep",
                    rem,
                    remPer,
                    SleepType.REM
                )
            )
        }

        sleepStageList.add(
            SleepStageAnalysis(
                "Light Sleep",
                light,
                lightPer,
                SleepType.LIGHT
            )
        )
        sleepStageList.add(
            SleepStageAnalysis(
                "Deep Sleep",
                deep,
                deepPer,
                SleepType.DEEP
            )
        )
        sleepStageAdapter.setData(sleepStageList)

    }


    private fun setHighlightsGraphs(sleepHighlightResponse: SleepHighlightResponse) {
        val elementWidth = screenUtils.dpToPx(180, requireContext()) / 7
        val viewHeight = screenUtils.dpToPx(180, requireContext())
        val barHeightTotal = viewHeight - screenUtils.dpToPx(30, requireContext())


        val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(
            sleepHighlightResponse.avg_duration ?: 0
        )

        binding.layoutSleepHighlights.tvSleepDurationHour.text = "$hour"
        binding.layoutSleepHighlights.tvSleepDurationMinute.text = "$minute"

        val maxValue = sleepHighlightResponse.breakup_duration?.maxOf { data -> data.duration } ?: 0

        sleepHighlightResponse.breakup_duration?.forEachIndexed { index, sleepBreakup ->

            val view = LayoutInflater.from(context).inflate(R.layout.layout_bar, null, false)

            view.findViewById<TextView>(R.id.tvLabel).text =
                DateFormats.formatWeek(sleepBreakup.date)

            val barHeight = if (maxValue == 0) {
                0
            } else {
                if (sleepBreakup.duration == 0) {
                    0
                } else {
                    val currentLevel = (sleepBreakup.duration.toFloat() / maxValue) * 100
                    barHeightTotal * (currentLevel) / 100
                }
            }

            val linearLayoutLayoutPrams = RelativeLayout.LayoutParams(
                elementWidth.roundToInt(),
                viewHeight.roundToInt()
            )

            val bar = view.findViewById<View>(R.id.vBar)
            val params = bar.layoutParams
            params.height = barHeight.toInt()
            bar.layoutParams = params


            view.layoutParams = linearLayoutLayoutPrams
            binding.layoutSleepHighlights.graphSleepDuration.addView(view)

        }


        //QUALITY
        var sleepSum = 0
        var sleepQualityCount = 0

        sleepHighlightResponse.sleep_breakup?.forEachIndexed { index, sleepBreakup ->
            val view = LayoutInflater.from(context).inflate(R.layout.layout_bar, null, false)

            view.findViewById<TextView>(R.id.tvLabel).text =
                DateFormats.formatWeek(sleepBreakup.date)


            val sleepScore = sleepBreakup.sleepScore ?: 0

            val barHeight = barHeightTotal * (sleepScore) / 100

            if (sleepScore != 0) {
                sleepSum += sleepScore
                sleepQualityCount++
            }

            val linearLayoutLayoutPrams = RelativeLayout.LayoutParams(
                elementWidth.roundToInt(),
                viewHeight.roundToInt()
            )

            val linearLayoutLayoutPramsLevel = ConstraintLayout.LayoutParams(
                screenUtils.dpToPx(4, requireContext()).roundToInt(),
                barHeight.roundToInt()
            )

            val bar = view.findViewById<View>(R.id.vBar)
            val params = bar.layoutParams
            params.height = barHeight.roundToInt()
            bar.layoutParams = params

            val gd = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                imageUtil.getGradientList(sleepBreakup.sleepScore ?: 0)
            )
            gd.cornerRadius = screenUtils.dpToPx(2, requireContext())
            view.findViewById<View>(R.id.vBar).apply {
                background = gd
                layoutParams = params
            }
            view.layoutParams = linearLayoutLayoutPrams

            binding.layoutSleepHighlights.graphAverageSleepQuality.addView(view)
        }

        if (sleepSum != 0) {
            showSleepQuality()
            binding.layoutSleepHighlights.tvAverageSleepQuality.text =
                if (sleepSum == 0) {
                    "_/100"
                } else {
                    "${(sleepSum.toFloat() / sleepQualityCount).roundToInt()}/100"
                }
        } else {
            hideSleepQuality()
        }


        /**
         * TBD in Next phase
         */
        /*DummyDataUtils.getBedTimeData().forEach {
            val view = LayoutInflater.from(context).inflate(R.layout.layout_bar, null, false)
            view.findViewById<TextView>(R.id.tvLabel).text = it.text
            val barHeight = barHeightTotal * (it.level) / 100
            val linearLayoutLayoutPrams = LinearLayout.LayoutParams(
                elementWidth.roundToInt(),
                barHeight.roundToInt()
            )
            view.layoutParams = linearLayoutLayoutPrams
            binding.layoutSleepHighlights.graphAverageBedTime.addView(view)
        }*/
    }

    fun hideSleepQuality() {
        binding.layoutSleepHighlights.layoutAverageSleepQuality.root.gone()
        binding.layoutSleepHighlights.vAverageSleepQuantity.gone()
        binding.layoutSleepHighlights.tvAverageSleepQuality.gone()
        binding.layoutSleepHighlights.graphAverageSleepQuality.gone()
    }

    fun showSleepQuality() {
        binding.layoutSleepHighlights.layoutAverageSleepQuality.root.visible()
        binding.layoutSleepHighlights.vAverageSleepQuantity.visible()
        binding.layoutSleepHighlights.tvAverageSleepQuality.visible()
        binding.layoutSleepHighlights.graphAverageSleepQuality.visible()

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

    fun getSleepAboutList(): ArrayList<SleepAbout> {
        val sleepAboutList = ArrayList<SleepAbout>()
        sleepAboutList.add(
            SleepAbout(
                "Why Sleep Is Important",
                "Learn about how sleep helps the body."
            )
        )
        sleepAboutList.add(
            SleepAbout(
                "Getting a Good Night’s Sleep",
                "Having trouble sleeping? These tips might help."
            )
        )
        return sleepAboutList
    }

    private fun getSleepExtraList(sleepHistory: ArrayList<SleepBreakup>?): ArrayList<SleepExtraData> {
        val data = ArrayList<SleepExtraData>()
        data.add(
            SleepExtraData(
                getString(R.string.text_heart_rate),
                viewModel.getHrMaxMinValues(),
                SleepExtraType.HeartRate,
                false
            )
        )

        if (hasStressData(sleepHistory)) {
            data.add(
                SleepExtraData(
                    getString(R.string.text_stress_levels),
                    viewModel.getStressMaxMinValues(),
                    SleepExtraType.StressLevel,
                    false
                )
            )
        }

        return data
    }

    private fun hasStressData(sleepHistory: ArrayList<SleepBreakup>?): Boolean {
        if (sleepHistory.isNullOrEmpty()) {
            return false
        }
        sleepHistory.forEach {
            if (it.stress_min != null && it.stress_max != null) {
                return true
            }
        }
        return false

    }

    private fun setGraphAdapter(
        count: Int,
        sleepData: List<SleepBreakup>,
        historyType: String,
        duration: Int,
        averageBedTime: String,
        date: String
    ) {
        graphAdapter =
            SleepGraphSlidePagerAdapter(
                this,
                count,
                sleepData,
                historyType,
                duration,
                averageBedTime,
                date
            )
        binding.viewPagerGraph.adapter = graphAdapter
        binding.viewPagerGraph.setCurrentItem(count, false)
    }

    private inner class SleepGraphSlidePagerAdapter(
        fa: Fragment,
        val count: Int,
        val sleepData: List<SleepBreakup>,
        val historyType: String,
        val duration: Int,
        val averageBedTime: String,
        val date: String
    ) :
        FragmentStateAdapter(fa) {

        var position = 0

        override fun getItemCount(): Int = count

        val sleepGraphArray = HashMap<Int, SleepDayFragment>()

        override fun createFragment(position: Int): Fragment {
            this.position = position
            return if (historyType.equals("daily", true)) {
                try {

                    val todayData = sleepData[position]
                    val hourlyHr = todayData.hourly_breakup ?: ArrayList()
                    val heartRateBreakup = todayData.hr_breakup ?: ArrayList()
                    val stressBreakup = todayData.stress_breakup ?: ArrayList()

                    val sleepDayFragment = SleepDayFragment.newInstance(
                        hourlyHr,
                        heartRateBreakup,
                        stressBreakup,
                        historyType,
                        todayData.total_duration,
                        todayData.date ?: "",
                        todayData.start_time ?: "",
                        todayData.end_time ?: ""
                    )
                    sleepGraphArray[position] = sleepDayFragment

                    logInsiderAppEvent(historyType)
                    sleepDayFragment
                } catch (exp: Exception) {
                    logInsiderAppEvent(historyType)
                    SleepDayFragment()
                }
            } else {
                logInsiderAppEvent(historyType)
                SleepGraphFragment.newInstance(
                    sleepData,
                    historyType,
                    duration,
                    averageBedTime,
                    date
                )
            }
        }
    }
    private fun logInsiderAppEvent(historyType: String) {
        if (historyType.equals("daily", true)) {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.SLEEP_DAY_CLICK)
        } else if (historyType.equals("weekly", true))
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.SLEEP_WEEK_CLICK)
        else if (historyType.equals("monthly", true)) {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.SLEEP_MONTH_CLICK)
        } else {
            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.SLEEP_YEAR_CLICK)
        }
    }
}

