package com.oreo.ui.sleep2

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CalenderSleepDayBinding
import com.noisefit.luna.databinding.FragmentSleepDashBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.dashboard.graphs.HistoryCalendarActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.setTextGradient
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.custom.NightTimeGraphViewOreo
import com.noisefit_commans.ui.custom.SleepGraphViewOreo
import com.noisefit_commans.ui.custom.SleepStageAction
import com.noisefit_commans.ui.custom.ToolTipEntry
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.OHMDataModel
import com.oreo.data.model.health.Nap
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.SleepHourlyBreakup
import com.oreo.data.model.health.SleepMovementBreakup
import com.oreo.data.model.sleep.SleepDay
import com.oreo.data.model.sleep.SleepSummary
import com.oreo.ui.home.summary.DashNapAdapter
import com.oreo.ui.home.summary.OnNapSelectedAction
import com.oreo.ui.internal.OHMInternalAdapter
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.sleep.banner.OreoSleepBannerFragment
import com.oreo.ui.sleep2.internal.SleepInternalDetailsFragment
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject


@AndroidEntryPoint
class SleepDashFragment :
    BaseFragment<FragmentSleepDashBinding>(FragmentSleepDashBinding::inflate) {

    private val viewModel: SleepDashViewModel by viewModels()
    private var sleepDayGraphView: SleepGraphViewOreo? = null
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    @Inject
    lateinit var vibrationUtils: VibrationUtils


    private val mSleepStageAdapter: SleepAnalysisAdapter by lazy {
        SleepAnalysisAdapter()
    }

    private val multiSleepAdapter: MultiSleepAdapter by lazy {
        MultiSleepAdapter(object : MultiSleepAction {
            override fun onSleepClicked(position: Int) {
                viewModel.updateSelectedMultiSleep(position)
            }
        })
    }

    private val adapterSleepContributor: OHMInternalAdapter by lazy {
        OHMInternalAdapter(object : OHMInternalAdapter.HMItemClickListener {
            override fun onItemClick(resultData: OHMDataModel, position: Int) {
                showInternalTrend(viewModel.getLaunchState(resultData.type))
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.tvTitle.text = getString(R.string.text_sleep)
        initCalender()
        setRecycler()
    }

    override fun initListener() {

        binding.svMain.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (Math.abs(scrollY - oldScrollY) > 0) {
                sleepDayGraphView?.resetIfInteracting()
            }
        }

        binding.lytSSAnalysis.lytNightMovement.bInfo.setOnClickListener {
            viewModel.contributorInfo.value?.night_time_movements?.let { content ->
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", content)
                })
            }
        }

        binding.lytSleepContributor.ivArrowOpen.setOnClickListener {
            val data = viewModel.getSelectedDateData()
            if (data != null) {
                adapterSleepContributor.setData(viewModel.generateSleepContributorData(data, true))
                binding.lytSleepContributor.ivArrowOpen.gone()
                binding.lytSleepContributor.ivClose.visible()
            }
        }

        binding.lytSleepContributor.ivClose.setOnClickListener {
            val data = viewModel.getSelectedDateData()
            if (data != null) {
                adapterSleepContributor.setData(viewModel.generateSleepContributorData(data))
                binding.lytSleepContributor.ivArrowOpen.visible()
                binding.lytSleepContributor.ivClose.gone()
            }
        }

        binding.vCalendar.weekScrollListener = { weekDays ->
            viewModel.onWeekScrolled(weekDays.days.get(0).date)
        }

        binding.toolbar.viewBackCalendar.setOnClickListener {
            resultLauncher.launch(
                HistoryCalendarActivity.getStartIntent(
                    requireContext(), viewModel.selectedDate.value.toString(),
                    "ring"
                )
            )
        }
        binding.lytScore.ivInfo.setOnClickListener {
            navigate(R.id.sleepPlannerFragment)
        }


        binding.lytSleepTrends.lytSleepPerformance.root.setOnClickListener {
            showInternalTrend(SleepInternalLaunchState.SLEEP_PERFORMANCE)
        }
        binding.lytSleepTrends.lytHourVsNeed.root.setOnClickListener {
            showInternalTrend(SleepInternalLaunchState.HOUR_VS_NEED)
        }
        binding.lytSleepTrends.lytRestorativeSleep.root.setOnClickListener {
            showInternalTrend(SleepInternalLaunchState.RESTORATIVE_SLEEP)
        }
        binding.lytSleepTrends.lytSleepTime.root.setOnClickListener {
            showInternalTrend(SleepInternalLaunchState.SLEEP_TIME)
        }

    }

    override fun subscribeObservers() {

        viewModel.calendarStartDate.observe(this) {
            it.getContent()?.let {
                val lastDayOfWeek: LocalDate =
                    LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

                binding.vCalendar.setup(
                    it,
                    lastDayOfWeek,
                    DayOfWeek.MONDAY,
                )
                binding.vCalendar.scrollToDate(
                    viewModel.selectedDate.value ?: LocalDate.now()
                )
            }

        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.selectedMultiSleep.observe(this) {

            if (it?.start_time == null || it.end_time == null) {
                totalSleepNoDataView()
            } else {
                val startTime = LocalDateTime.parse(
                    it.start_time,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                val endTime = LocalDateTime.parse(
                    it.end_time,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                val duration = Duration.between(startTime, endTime).toSeconds()

                val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                    duration.toInt()
                )
                binding.lytSSAnalysis.lytTotalSleep.tvHour.text = "$hour"
                binding.lytSSAnalysis.lytTotalSleep.tvMin.text = "$minute"
                totalSleepDataView()
            }

            showNightTimeMovementGraph(it?.night_time_movement, it?.start_time, it?.end_time)
            initSleepAnalysisGraph(it?.hourly)
        }

        viewModel.trendsData.observe(this) {

            //sleep performance
            binding.lytSleepTrends.lytSleepPerformance.graphPerformance.setDataSet(
                it.sleepPerformance, it.selectedPosition
            )

            //Hour vs Need
            binding.lytSleepTrends.lytHourVsNeed.graphHourVsNeed.setDataSet(
                it.hourVsNeed, it.selectedPosition
            )

            //Restorative Sleep
            binding.lytSleepTrends.lytRestorativeSleep.graphRestorative.setDataSet(
                it.restorative, it.selectedPosition
            )

            //Sleep Time
            binding.lytSleepTrends.lytSleepTime.graphSleepTime.setDataSet(
                it.sleepTime, it.selectedPosition
            )
            setTrendsTopIcons(it)


        }

        viewModel.sleepDayData.observe(this) {
            updateSleepUi(it)
        }

        viewModel.notifyDateChange.observe(this) {
            it.getContent()?.let {
                try {
                    binding.vCalendar.notifyDateChanged(
                        it
                    )
                } catch (exp: Exception) {
                }
            }
        }

        viewModel.selectedDate.observe(this) {
            try {
                binding.vCalendar.notifyDateChanged(it)
            } catch (exp: Exception) {
            }

            binding.toolbar.tvMonth.text = it.format(DateTimeFormatter.ofPattern("MMM"))

            viewModel.getDataForDate(it)
        }
    }

    private fun setRecycler() {
        with(binding.lytSleepContributor.rvHm) {
            adapter = adapterSleepContributor
        }
        with(binding.lytSSAnalysis.rvSleepStage) {
            adapter = mSleepStageAdapter
        }
        with(binding.rvSleeps) {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = multiSleepAdapter
        }
    }

    private fun initCalender() {

        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = CalenderSleepDayBinding.bind(view)
            lateinit var day: WeekDay
            val dateToday = LocalDate.now()

            init {
                view.setOnClickListener {
                    if (day.date > dateToday) {
                        return@setOnClickListener
                    }

                    if (viewModel.selectedDate.value != day.date) {
                        viewModel.updateSelectedDate(day.date)
                    }
                }
            }

            fun bind(day: WeekDay) {
                this.day = day

                bind.exSevenDateText.text =
                    DateFormats.getDayFromDate(DateFormats.convertLocalDateToDate(day.date))
                bind.exSevenDayText.text =
                    DateFormats.getDayString(DateFormats.convertLocalDateToDate(day.date))


                val score = viewModel.sleepData[day.date]?.sleepScore?.value
                if (score == null) {
                    bind.circularProgressBar.setProgress(0)
                    bind.exSevenDayText.alpha = 0.3f
                } else {
                    bind.circularProgressBar.setProgress(score)
                    bind.exSevenDayText.alpha = 1f
                }

                if (day.date == viewModel.selectedDate.value) {
                    bind.vSelected.visible()
                } else {
                    bind.vSelected.invisible()
                }

                if (day.date > dateToday) {
                    bind.exSevenDayText.alpha = 0.3f
                } else {
                    bind.exSevenDayText.alpha = 1f
                }

            }
        }

        binding.vCalendar.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
        }

        val lastDayOfWeek: LocalDate =
            LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        binding.vCalendar.setup(
            LocalDate.now().minusDays(10),
            lastDayOfWeek,
            DayOfWeek.MONDAY,
        )
        binding.vCalendar.scrollToDate(
            viewModel.selectedDate.value ?: LocalDate.now()
        )
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val selectedDate =
                    data?.getStringExtra("selected_date") ?: return@registerForActivityResult

                viewModel.updateSelectedDate(LocalDate.parse(selectedDate))
                binding.vCalendar.scrollToDate(
                    viewModel.selectedDate.value ?: LocalDate.now()
                )

                LOGS.d("moveToPosition Selected Date  :${selectedDate}")

            }
        }


    private fun totalSleepDataView() {
        binding.lytSSAnalysis.lytTotalSleep.tvHour.visible()
        binding.lytSSAnalysis.lytTotalSleep.textHour.visible()
        binding.lytSSAnalysis.lytTotalSleep.tvMin.visible()
        binding.lytSSAnalysis.lytTotalSleep.textMin.visible()
    }

    private fun totalSleepNoDataView() {
        binding.lytSSAnalysis.lytTotalSleep.tvHour.text = "-"
        binding.lytSSAnalysis.lytTotalSleep.tvHour.visible()
        binding.lytSSAnalysis.lytTotalSleep.textHour.gone()
        binding.lytSSAnalysis.lytTotalSleep.tvMin.gone()
        binding.lytSSAnalysis.lytTotalSleep.textMin.gone()
    }

    private fun setTrendsTopIcons(trendsData: SleepTrendsData) {
        if (trendsData.sleepPerformanceIcon.size == 7) {
            binding.lytSleepTrends.lytSleepPerformance.apply {
                ivPos1.setImageResource(trendsData.sleepPerformanceIcon[0])
                ivPos2.setImageResource(trendsData.sleepPerformanceIcon[1])
                ivPos3.setImageResource(trendsData.sleepPerformanceIcon[2])
                ivPos4.setImageResource(trendsData.sleepPerformanceIcon[3])
                ivPos5.setImageResource(trendsData.sleepPerformanceIcon[4])
                ivPos6.setImageResource(trendsData.sleepPerformanceIcon[5])
                ivPos7.setImageResource(trendsData.sleepPerformanceIcon[6])
            }
        }
        if (trendsData.hourVsNeedIcon.size == 7) {
            binding.lytSleepTrends.lytHourVsNeed.apply {
                ivPos1.setImageResource(trendsData.hourVsNeedIcon[0])
                ivPos2.setImageResource(trendsData.hourVsNeedIcon[1])
                ivPos3.setImageResource(trendsData.hourVsNeedIcon[2])
                ivPos4.setImageResource(trendsData.hourVsNeedIcon[3])
                ivPos5.setImageResource(trendsData.hourVsNeedIcon[4])
                ivPos6.setImageResource(trendsData.hourVsNeedIcon[5])
                ivPos7.setImageResource(trendsData.hourVsNeedIcon[6])
            }
        }
        if (trendsData.restorativeIcon.size == 7) {
            binding.lytSleepTrends.lytRestorativeSleep.apply {
                ivPos1.setImageResource(trendsData.restorativeIcon[0])
                ivPos2.setImageResource(trendsData.restorativeIcon[1])
                ivPos3.setImageResource(trendsData.restorativeIcon[2])
                ivPos4.setImageResource(trendsData.restorativeIcon[3])
                ivPos5.setImageResource(trendsData.restorativeIcon[4])
                ivPos6.setImageResource(trendsData.restorativeIcon[5])
                ivPos7.setImageResource(trendsData.restorativeIcon[6])
            }
        }
        if (trendsData.sleepTimeIcons.size == 7) {
            binding.lytSleepTrends.lytSleepTime.apply {
                ivPos1.setImageResource(trendsData.sleepTimeIcons[0])
                ivPos2.setImageResource(trendsData.sleepTimeIcons[1])
                ivPos3.setImageResource(trendsData.sleepTimeIcons[2])
                ivPos4.setImageResource(trendsData.sleepTimeIcons[3])
                ivPos5.setImageResource(trendsData.sleepTimeIcons[4])
                ivPos6.setImageResource(trendsData.sleepTimeIcons[5])
                ivPos7.setImageResource(trendsData.sleepTimeIcons[6])
            }
        }
    }

    private fun updateSleepUi(data: SleepDay?) {

        var hasNoData = false
        if (data?.sleepScore?.value == null) {
            hasNoData = true
        }

        binding.lytScore.tvScore.text =
            if (hasNoData) "--" else "${data?.sleepScore?.value}"

        binding.lytScore.tvScoreStatus.apply {
            if (hasNoData) {
                gone()
            } else {
                visible()
                text = "${data?.sleepScore?.text}"
                val statusColor = ContextCompat.getColor(
                    this.context,
                    viewModel.getStatusColors(data?.sleepScore?.status ?: "")
                )
                setTextColor(statusColor)
            }
        }

        binding.lytScore.lytSleepActual.apply {
            if (data?.sleepScore?.value == null) {
                tvNoData.visible()

                tvHour.gone()
                tvMin.gone()
                textHour.gone()
                textMin.gone()
            } else {
                tvNoData.gone()

                tvHour.visible()
                tvMin.visible()
                textHour.visible()
                textMin.visible()

                val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                    data.sleepDuration?.value ?: 0
                )

                tvHour.text = "$hour"
                tvMin.text = "$minute"

                val (startColor, endColor) = viewModel.getGradientColor(
                    data.sleepScore?.status ?: ""
                )

                tvHour.setTextGradient(
                    requireActivity().getColor(R.color.white),
                    startColor,
                    endColor
                )
                tvMin.setTextGradient(
                    requireActivity().getColor(R.color.white),
                    startColor,
                    endColor
                )
            }


        }

        binding.lytScore.lytSleepNeeded.apply {

            if (data?.sleepScore?.value == null) {
                tvNoData.visible()

                tvHour.gone()
                tvMin.gone()
                textHour.gone()
                textMin.gone()
            } else {
                tvNoData.gone()

                tvHour.visible()
                tvMin.visible()
                textHour.visible()
                textMin.visible()

                val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                    data.sleepNeed ?: 0
                )

                tvHour.text = "$hour"
                tvMin.text = "$minute"

            }
        }

        binding.lytScore.circularProgressBar.setProgress(data?.sleepScore?.value ?: 0)

        setNudgesView(data?.nudges)

        setNapData(data?.naps, data?.date ?: "")

        adapterSleepContributor.setData(viewModel.generateSleepContributorData(data))
        binding.lytSleepContributor.ivArrowOpen.visible()
        binding.lytSleepContributor.ivClose.gone()


        val multiSleep = viewModel.generateMultiSleepData(data?.sleepChild)
        if (multiSleep == null || multiSleep.size == 1) {
            binding.rvSleeps.gone()
        } else {
            binding.rvSleeps.visible()
            multiSleepAdapter.setData(multiSleep)
        }

        viewModel.updateSelectedMultiSleep(0)


        updateSleepSummary(data?.summary)

    }

    private fun updateSleepSummary(summary: SleepSummary?) {
        val sleepSummary = arrayListOf(
            SleepAnalysisData(
                name = "REM sleep",
                icon = R.drawable.ic_sleep_rem,
                currentValue = summary?.rem?.curr_val,
                avgValue = summary?.rem?.avg
            ), SleepAnalysisData(
                name = "Deep sleep",
                icon = R.drawable.ic_sleep_deep,
                currentValue = summary?.deep?.curr_val,
                avgValue = summary?.deep?.avg
            ), SleepAnalysisData(
                name = "Awake sleep",
                icon = R.drawable.ic_sleep_awake,
                currentValue = summary?.awake?.curr_val,
                avgValue = summary?.awake?.avg
            ), SleepAnalysisData(
                name = "Light sleep",
                icon = R.drawable.ic_sleep_light,
                currentValue = summary?.light?.curr_val,
                avgValue = summary?.light?.avg
            )
        )
        mSleepStageAdapter.setData(sleepSummary)
    }

    private fun setNapData(naps: List<Nap>?, date: String) {
        val filteredNaps = naps?.filter { !it.isNextDayNap }

        if (filteredNaps.isNullOrEmpty()) {
            binding.dividerNap.root.gone()
            binding.lytNaps.root.gone()
            return
        } else {
            binding.dividerNap.root.visible()
            binding.lytNaps.root.visible()
        }

        binding.lytNaps.lytNap.rvNap.layoutManager =
            LinearLayoutManager(binding.lytNaps.lytNap.rvNap.context)
        binding.lytNaps.lytNap.rvNap.adapter = DashNapAdapter(filteredNaps, date, true).apply {

            this.setOnNapSelectedListener(object : OnNapSelectedAction {
                override fun onNapSelected(napId: String) {
                    navigate(R.id.napDetails, bundleOf("napId" to napId))
                }
            })
        }
    }

    private fun initSleepAnalysisGraph(hourlyBreakup: List<SleepHourlyBreakup>?) {

        sleepDayGraphView = SleepGraphViewOreo(requireContext())
        sleepDayGraphView?.setClickListener(object : SleepStageAction {
            override fun onValueSelected(data: ToolTipEntry) {
                setInteractionDate(data)
            }

            override fun isInteractionOnGoing(onGoing: Boolean) {
                if (onGoing) {
                    binding.lytSSAnalysis.lytSleepInteraction.root.visible()
                    binding.lytSSAnalysis.lytTotalSleep.root.gone()
                } else {
                    binding.lytSSAnalysis.lytSleepInteraction.root.gone()
                    binding.lytSSAnalysis.lytTotalSleep.root.visible()
                }
            }
        })

        if (hourlyBreakup.isNullOrEmpty()) {
            sleepDayGraphView?.enableInteractiveMode(false)
        } else {
            sleepDayGraphView?.enableInteractiveMode(true)
        }

        sleepDayGraphView?.setVibrationUtil(vibrationUtils)


        binding.lytSSAnalysis.flSleepGraph.removeAllViews()
        binding.lytSSAnalysis.flSleepGraph.addView(sleepDayGraphView)

        val sleepData =
            viewModel.getHourlySleepBreakup(hourlyBreakup)//viewModel.getHourlySleepData()
        sleepDayGraphView?.init(false)

        sleepDayGraphView?.setData(sleepData.second)
        sleepDayGraphView?.setData(
            sleepData.first
        )
        sleepDayGraphView?.invalidate()

    }

    fun setInteractionDate(data: ToolTipEntry) {
        binding.lytSSAnalysis.lytSleepInteraction.apply {

            if (data.type.equals("deep", true)) {
                this.tvSleepType.text = getString(R.string.text_deep_sleep)
                this.tvSleepType.setTextColor(Color.parseColor("#a882ff"))

            } else if (data.type.equals("light", true)) {
                this.tvSleepType.text = getString(R.string.text_light_sleep)
                this.tvSleepType.setTextColor(Color.parseColor("#cc9cfb"))

            } else if (data.type.equals("rem", true)) {
                this.tvSleepType.text = getString(R.string.text_rem_sleep)
                this.tvSleepType.setTextColor(Color.parseColor("#cbade8"))

            } else if (data.type.equals("awake", true)) {
                this.tvSleepType.text = getString(R.string.text_awake)
                this.tvSleepType.setTextColor(Color.parseColor("#e5dafa"))
            }

            val startTime = DateFormats.formatDate(
                data.startTime, DateFormats.dateTimeFormat5(), DateFormats.timeFormat12_2()
            )
            val startTimeUnit = DateFormats.formatDate(
                data.startTime, DateFormats.dateTimeFormat5(), DateFormats.timeFormat12_unit()
            )
            val endTime = DateFormats.formatDate(
                data.endTime, DateFormats.dateTimeFormat5(), DateFormats.timeFormat12_2()
            )
            val endTimeUnit = DateFormats.formatDate(
                data.endTime, DateFormats.dateTimeFormat5(), DateFormats.timeFormat12_unit()
            )

            this.tvStartTime.text = startTime
            this.tvStartUnit.text = startTimeUnit.lowercase()
            this.tvEndTime.text = endTime
            this.tvEndUnit.text = endTimeUnit.lowercase()

        }
    }

    private fun showNightTimeMovementGraph(
        nightMovementBreakUp: List<SleepMovementBreakup>?,
        sleepStartTime: String?,
        sleepEndTime: String?
    ) {

        val nightTimeMovementGraph = NightTimeGraphViewOreo(requireContext())
        binding.lytSSAnalysis.lytNightMovement.flNightTimeMovement.removeAllViews()
        binding.lytSSAnalysis.lytNightMovement.flNightTimeMovement.addView(nightTimeMovementGraph)

        val sleepData =
            viewModel.getMovementBreakup(nightMovementBreakUp, sleepStartTime, sleepEndTime)
        nightTimeMovementGraph.init(false)

        nightTimeMovementGraph.setData(sleepData.second)
        nightTimeMovementGraph.setData(
            sleepData.first
        )
        nightTimeMovementGraph.invalidate()

    }

    private fun setNudgesView(data: List<Nudges>?) {

        if (data.isNullOrEmpty()) {
            binding.nudgesSleep.gone()
            return
        } else {
            binding.nudgesSleep.visible()
        }

        val fragments = ArrayList<OreoSleepBannerFragment>()
        data.forEach {
            fragments.add(OreoSleepBannerFragment.newInstance(it))
        }

        val sleepBannerAdapter = OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)

        binding.nudgesSleep.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(20))
            })
            adapter = sleepBannerAdapter
        }
    }

    fun showInternalTrend(state: SleepInternalLaunchState) {
        val (frag, bundle) = SleepInternalDetailsFragment.getStartData(
            state
        )
        navigate(frag, bundle)
    }


}