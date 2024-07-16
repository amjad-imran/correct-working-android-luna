package com.oreo.ui.sleep2

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.moengage.core.internal.utils.getRandomInt
import com.noisefit.luna.R
import com.noisefit.luna.databinding.CalenderSleepDayBinding
import com.noisefit.luna.databinding.FragmentSleepDashBinding
import com.noisefit_commans.common.setTextGradient
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.custom.NightTimeGraphViewOreo
import com.noisefit_commans.ui.custom.SleepGraphViewOreo
import com.noisefit_commans.ui.custom.SleepStageAction
import com.noisefit_commans.ui.custom.ToolTipEntry
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.OHMDataModel
import com.oreo.data.model.health.Nap
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.SleepHourlyBreakup
import com.oreo.data.model.health.SleepMovementBreakup
import com.oreo.ui.home.summary.DashNapAdapter
import com.oreo.ui.home.summary.OnNapSelectedAction
import com.oreo.ui.internal.OHMInternalAdapter
import com.oreo.ui.sleep.OreoSleepStageAnalysisAdapter
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.sleep.banner.OreoSleepBannerFragment
import com.oreo.ui.sleep2.internal.SleepInternalDetailsFragment
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@AndroidEntryPoint
class SleepDashFragment :
    BaseFragment<FragmentSleepDashBinding>(FragmentSleepDashBinding::inflate) {

    private val viewModel: SleepDashViewModel by viewModels()
    private var sleepDayGraphView: SleepGraphViewOreo? = null

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    private val mSleepStageAdapter: OreoSleepStageAnalysisAdapter by lazy {
        OreoSleepStageAnalysisAdapter()
    }

    private val multiSleepAdapter: MultiSleepAdapter by lazy {
        MultiSleepAdapter(object : MultiSleepAction {
            override fun onSleepClicked() {

            }
        })
    }

    private val mAdapter: OHMInternalAdapter by lazy {
        OHMInternalAdapter(object : OHMInternalAdapter.HMItemClickListener {
            override fun onItemClick(resultData: OHMDataModel, position: Int) {

            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCalender()
        setRecycler()
        initTempUi()
    }

    private fun setRecycler() {
        with(binding.lytSleepContributor.rvHm) {
            adapter = mAdapter
        }
        with(binding.lytSSAnalysis.rvSleepStage) {
            adapter = mSleepStageAdapter
        }
        with(binding.rvSleeps) {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = multiSleepAdapter
        }
        mAdapter.setData(getHealthMonitorData())

        multiSleepAdapter.setData(arrayListOf("", "", ""))
    }

    fun getHealthMonitorData(): ArrayList<OHMDataModel> {
        val listData = ArrayList<OHMDataModel>()
        listData.add(
            OHMDataModel(
                R.drawable.ic_respiratory_rate,
                "Respiratory rate",
                value = "98.4",
                unit = "rpm",
                rangeValue = "near 11.9-13.8"
            )
        )
        listData.add(OHMDataModel(R.drawable.ic_resting_hr, "Resting heart rate"))
        listData.add(OHMDataModel(R.drawable.ic_blood_oxygen, "Blood oxygen"))
        listData.add(OHMDataModel(R.drawable.ic_hrv, "HRV"))
        listData.add(OHMDataModel(R.drawable.ic_skin_tempreature, "Skin temperature"))
        return listData
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

        val sleepBannerAdapter =
            OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)

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

    private fun initTempUi() {


        setNudgesView(
            arrayListOf(
                Nudges(
                    label = "Sleep data is needed",
                    message = "Wear your luna ring when you go to bed to track your sleep. Make sure to charge your ring to avoid missing out valuable insights."
                ),
                Nudges(
                    label = "Sleep data is needed 2",
                    message = "Wear your luna ring when you go to bed to track your sleep. Make sure to charge your ring to avoid missing out valuable insights."
                )
            )
        )

        binding.lytScore.tvScore.text = "80"
        binding.lytScore.tvScoreStatus.text = "Optimal"
        binding.lytScore.tvScoreStatus.setTextColor(Color.parseColor("#29cc74"))

        binding.lytScore.lytSleepActual.apply {
            tvNoData.gone()
            tvHour.text = "7"
            tvMin.text = "30"

            tvHour.setTextGradient(
                requireActivity().getColor(R.color.white),
                Color.parseColor("#aef8be"),
                Color.parseColor("#2fce77")
            )
            tvMin.setTextGradient(
                requireActivity().getColor(R.color.white),
                Color.parseColor("#aef8be"),
                Color.parseColor("#2fce77")
            )
        }

        binding.lytScore.lytSleepNeeded.apply {
            tvNoData.gone()
            tvHour.text = "8"
            tvMin.text = "30"


        }



        binding.lytScore.circularProgressBar.setProgress(80)

        binding.lytSleepTrends.lytSleepPerformance.graphPerformance.setDataSet(
            arrayListOf(
                20,
                30,
                null,
                50,
                100,
                70,
                null
            ),
            4
        )

        binding.lytSleepTrends.lytHourVsNeed.graphHourVsNeed.setDataSet(
            arrayListOf(
                Pair(60, 100),
                Pair(null, null),
                Pair(90, 100),
                Pair(null, null),
                Pair(120, 130),
                Pair(150, 180),
                Pair(180, 200),
            ),
            4
        )
        binding.lytSleepTrends.lytRestorativeSleep.graphRestorative.setDataSet(
            arrayListOf(
                Pair(60, 40),
                Pair(80, 50),
                Pair(90, 60),
                Pair(100, 40),
                Pair(120, 20),
                Pair(150, 10),
                Pair(180, 0),
            ),
            4
        )


        val data = viewModel.generateSleepTimeData()

        binding.lytSleepTrends.lytSleepTime.graphSleepTime.setDataSet(
            data,
            4
        )
    }

    private fun initCalender() {

        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = CalenderSleepDayBinding.bind(view)
            lateinit var day: WeekDay
            val dateToday = LocalDate.now()

            init {
                view.setOnClickListener {
                    /*if (viewModel.selectedDate.value != day.date) {
                        viewModel.updateSelectedDate(day.date)
                    }*/
                }
            }

            fun bind(day: WeekDay) {
                this.day = day

                bind.exSevenDateText.text =
                    DateFormats.getDayFromDate(DateFormats.convertLocalDateToDate(day.date))
                bind.exSevenDayText.text =
                    DateFormats.getDayString(DateFormats.convertLocalDateToDate(day.date))

                bind.circularProgressBar.setProgress(getRandomInt(20, 100))

            }
        }

        binding.vCalendar.dayBinder =
            object : WeekDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
            }

        val currentMonth = YearMonth.now()

        binding.vCalendar.setup(
            currentMonth.atStartOfMonth(),
            currentMonth.atEndOfMonth(),
            DayOfWeek.MONDAY,
        )
        binding.vCalendar.scrollToDate(
            LocalDate.now()
        )
    }


    override fun initListener() {
        binding.toolbar.viewBackCalendar.setOnClickListener {
            navigate(R.id.healthMonitorInternal)
        }
        binding.lytScore.ivInfo.setOnClickListener {
            navigate(R.id.sleepPlannerFragment)
        }


        binding.lytSleepTrends.lytSleepPerformance.root.setOnClickListener {
            val (frag, bundle) = SleepInternalDetailsFragment.getStartData(SleepInternalLaunchState.RESTORATIVE_SLEEP)
            navigate(frag, bundle)
        }
        binding.lytSleepTrends.lytHourVsNeed.root.setOnClickListener {
            val (frag, bundle) = SleepInternalDetailsFragment.getStartData(SleepInternalLaunchState.RESTORATIVE_SLEEP)
            navigate(frag, bundle)
        }
        binding.lytSleepTrends.lytRestorativeSleep.root.setOnClickListener {
            val (frag, bundle) = SleepInternalDetailsFragment.getStartData(SleepInternalLaunchState.RESTORATIVE_SLEEP)
            navigate(frag, bundle)
        }

    }

    override fun subscribeObservers() {

    }

    private fun setNapData(naps: List<Nap>?, date: String) {
        val filteredNaps = naps?.filter { !it.isNextDayNap }

        if (filteredNaps.isNullOrEmpty()) {
            binding.lytNaps.root.gone()
            return
        } else {
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
                data.startTime,
                DateFormats.dateTimeFormat5(),
                DateFormats.timeFormat12_2()
            )
            val startTimeUnit = DateFormats.formatDate(
                data.startTime,
                DateFormats.dateTimeFormat5(),
                DateFormats.timeFormat12_unit()
            )
            val endTime = DateFormats.formatDate(
                data.endTime,
                DateFormats.dateTimeFormat5(),
                DateFormats.timeFormat12_2()
            )
            val endTimeUnit = DateFormats.formatDate(
                data.endTime,
                DateFormats.dateTimeFormat5(),
                DateFormats.timeFormat12_unit()
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


}