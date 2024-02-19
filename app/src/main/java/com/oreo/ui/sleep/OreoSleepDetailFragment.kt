package  com.oreo.ui.sleep

import android.annotation.SuppressLint
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
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOreoSleepDetailBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.dashboard.graphs.HistoryCalendarActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.*
import com.noisefit_commans.ui.custom.NightTimeGraphViewOreo
import com.noisefit_commans.ui.custom.SleepGraphViewOreo
import com.noisefit_commans.ui.custom.SleepStageAction
import com.noisefit_commans.ui.custom.ToolTipEntry
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributors
import com.oreo.data.model.GraphDummyModel
import com.oreo.data.model.SleepChartModel
import com.oreo.data.model.health.*
import com.oreo.ui.custom.ScrollListener
import com.oreo.ui.home.summary.DashNapAdapter
import com.oreo.ui.home.summary.OnNapSelectedAction
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.sleep.banner.OreoSleepBannerFragment
import com.oreo.ui.sleep.scoredetails.ClickViewType
import com.oreo.ui.sleep.scoredetails.SharedOSCDViewModel
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import com.oreo.util.UtilClass
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OreoSleepDetailFragment :
    BaseFragment<FragmentOreoSleepDetailBinding>(FragmentOreoSleepDetailBinding::inflate),
    ScrollListener {

    private val viewModel: OreoSleepDetailsViewModel by viewModels()
    private val mSharedViewModel: SharedOSCDViewModel by activityViewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private var sleepDayGraphView: SleepGraphViewOreo? = null

    private val TAG = "OreoSleepDetailFragment"


    private val mSleepStageAdapter: OreoSleepStageAnalysisAdapter by lazy {
        OreoSleepStageAnalysisAdapter()
    }
    private val mSleepContributorAdapter: OreoSleepContributorAdapter by lazy {
        OreoSleepContributorAdapter(object :
            OreoSleepContributorAdapter.ContributorItemClickListener {
            override fun onItemClick(
                resultData: ArrayList<Contributors>, position: Int,
                version: Int
            ) {
//                if (resultData[position].barPercent > 0) {
                openContributorBottomSheet(resultData, position)
//                }
            }
        })
    }

    private fun openContributorBottomSheet(resultData: ArrayList<Contributors>, position: Int) {
        val desListData = viewModel.prepareDataForDescriptionArray(resultData)
        navigate(
            OreoSleepDetailFragmentDirections.actionNavigationSleepDetailsFragToDescriptionPopUpBottomDialogFragment(
                position, desListData.toTypedArray(), resultData[position].title
            )
        )

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //sleepDayGraphView = SleepGraphViewOreo(requireContext())
        setRecycler()

        if (viewModel.ringDataStore.isSleepWalkAroundShown()) {
            viewModel.getSleepDetailsData()
        } else {
            showWalkAround(true)
        }

    }

    private fun showWalkAround(show: Boolean) {
        if (show) {
            binding.lytEmptyView.root.visible()
            binding.svMain.gone()
            binding.groupHeader.gone()
        } else {
            binding.lytEmptyView.root.gone()
        }
    }

    private fun showAverageBloodOxygen(
        oxy: CommonListDataModel?
    ) {
        if ((oxy?.avg ?: 0) == 0) {
            binding.lytAverageBloodOxygen.root.gone()
            return
        }

        binding.lytAverageBloodOxygen.tvNudge.text = viewModel.getBloodOxygenNudge(oxy)

        if ((oxy?.avg ?: 0) < 95) {
            binding.lytAverageBloodOxygen.root.visible()
            binding.lytAverageBloodOxygen.tvAvgValue.text = "<95"
            return
        }
        binding.lytAverageBloodOxygen.root.visible()
        binding.lytAverageBloodOxygen.tvAvgValue.text = (oxy?.avg ?: 0).toString()


    }

    private fun showBloodOxygenGraph(
        oxy: CommonListDataModel?,
        sleepStartTime: String?,
        sleepEndTime: String?
    ) {

        if ((oxy?.avg ?: 0) < 95) {
            binding.divider6.root.gone()
            binding.lytBloodOxygenGraph.root.gone()
            return
        }
        binding.divider6.root.visible()
        binding.lytBloodOxygenGraph.apply {
            root.visible()
            tvTitle.text = getString(R.string.text_spo2)
            tvSubtitle1.text = getString(R.string.text_average)
            tvSubtitle2.gone()
            lytSubtitleValue1.tvValue.text = (oxy?.avg ?: 0).toString() + " %"
            lytSubtitleValue1.tvUnit.gone()
            lytSubtitleValue2.root.gone()
        }

        val ssTime: String?
        val seTime: String?
        var breakUpData = ArrayList<Int>()
        var hasDummyData = true
        if (oxy?.value.isNullOrEmpty()) {
            breakUpData = viewModel.getDummyBreakUpDataForTimeDisplay()
            ssTime = null
            seTime = null
        } else {
            hasDummyData = false
            seTime = sleepEndTime
            ssTime = sleepStartTime
            breakUpData = oxy?.value as ArrayList<Int>
        }

        /* val baseTimeList =
             UtilClass.graphTwoHoursInterval(ssTime, seTime, breakUpData.size ?: 288)*/

        val baseTimeListNew =
            UtilClass.getXAxisPoints15Mins(ssTime, seTime, breakUpData.size ?: 96)

        binding.lytBloodOxygenGraph.lineChart.visible()
        val sleepChart = SleepChartModel()
        val chartList: MutableList<ChartModel> = java.util.ArrayList()

        breakUpData.forEachIndexed { index, it ->
            val chartModel = ChartModel()

            var value = it
            if (value == 255) {
                value = 0
            }

            chartModel.value = value
            chartModel.index = baseTimeListNew[index]//baseTimeList[index]
            chartModel.date = ""
            chartList.add(chartModel)
        }

        sleepChart.list = chartList

        //AppLogs.sendAppLogs("lineChart ${Gson().toJson(sleepChart)}")

        binding.lytBloodOxygenGraph.lineChart.updateGraphColor(
            Color.parseColor("#77dfe5"),
            Color.parseColor("#77dfe5"),
            Color.parseColor("#0025f2ff")
        )
        binding.lytBloodOxygenGraph.lineChart.updateDataWithMax(
            sleepChart, 5, true, false, GraphDummyModel(
                hasDummyData, 0, 200
            ),
            oxy?.avg
        )
    }

    private fun showHeartRateVariabilityGraph(
        hrv: CommonListDataModel?,
        sleepStartTime: String?,
        sleepEndTime: String?
    ) {

//        if (hrv?.value.isNullOrEmpty()) {
//            binding.lytHRVariability.lineChart.gone()
//            return
//        }

        val ssTime: String?
        val seTime: String?
        var breakUpData = ArrayList<Int>()
        var hasDummyData = true
        if (hrv?.value.isNullOrEmpty()) {
            breakUpData = viewModel.getDummyBreakUpDataForTimeDisplay()
            ssTime = null
            seTime = null
        } else {
            hasDummyData = false
            seTime = sleepEndTime
            ssTime = sleepStartTime
            breakUpData = hrv?.value as ArrayList<Int>
        }

        /* val baseTimeList =
             UtilClass.graphTwoHoursInterval(ssTime, seTime, breakUpData.size ?: 288)*/

        val baseTimeListNew =
            UtilClass.getXAxisPoints(ssTime, seTime, breakUpData.size ?: 288)

        binding.lytHRVariability.lineChart.visible()
        val sleepChart = SleepChartModel()
        val chartList: MutableList<ChartModel> = java.util.ArrayList()

        breakUpData.forEachIndexed { index, it ->
            val chartModel = ChartModel()

            var value = it
            if (value == 255) {
                value = 0
            }

            chartModel.value = value
            chartModel.index = baseTimeListNew[index]//baseTimeList[index]
            chartModel.date = ""
            chartList.add(chartModel)
        }

        sleepChart.list = chartList

        AppLogs.sendAppLogs("lineChart ${Gson().toJson(sleepChart)}")

        binding.lytHRVariability.lineChart.updateGraphColor(
            Color.parseColor("#ff80e3"),
            Color.parseColor("#CCff59da"),
            Color.parseColor("#0Dff59da")
        )
        binding.lytHRVariability.lineChart.updateDataWithMax(
            sleepChart, 5, true, false, GraphDummyModel(
                hasDummyData, 0, 200
            ),
            hrv?.avg
        )
    }


    private fun showHeartRateGraph(
        heartRateList: CommonListDataModel?,
        sleepStartTime: String?,
        sleepEndTime: String?,
    ) {
//        if ((heartRateList?.value?.size ?: 0) <= 1) {
//            binding.lytHeartRate.lineChart.gone()
//            return
//        }
        val ssTime: String?
        val seTime: String?
        var breakUpData = ArrayList<Int>()
        var hasDummyData = true
        if (heartRateList?.value.isNullOrEmpty()) {
            breakUpData = viewModel.getDummyBreakUpDataForTimeDisplay()
            ssTime = null
            seTime = null
        } else {
            hasDummyData = false
            seTime = sleepEndTime
            ssTime = sleepStartTime
            breakUpData = heartRateList?.value as ArrayList<Int>
        }


        /* val baseTimeList = UtilClass.graphTwoHoursInterval(
             ssTime,
             seTime,
             breakUpData.size ?: 288
         )*/
        val baseTimeListNew =
            UtilClass.getXAxisPoints(ssTime, seTime, breakUpData.size ?: 288)

        // LOGS.d("dsakjdsalkjsladjlksdajldsajldsajl ${heartRateList?.value?.size} ${Gson().toJson(baseTimeList)}")

        binding.lytHeartRate.lineChart.visible()
        val sleepChart = SleepChartModel()
        val chartList = ArrayList<ChartModel>()
        breakUpData.forEachIndexed { index, data ->
            val chartModel = ChartModel()

            var value = data
            if (value == 255) {
                value = 0
            }


            chartModel.value = value
            chartModel.index = baseTimeListNew[index]
            chartList.add(chartModel)
        }


        sleepChart.list = chartList
        binding.lytHeartRate.lineChart.updateGraphColor(
            Color.parseColor("#ff6b86"),
            Color.parseColor("#CCff6581"),
            Color.parseColor("#0Dff6581")
        )

        binding.lytHeartRate.lineChart.updateDataWithMax(
            sleepChart, 5, false, true,
            GraphDummyModel(
                hasDummyData, 40, 100
            ),
            heartRateList?.avg
        )

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
                DateFormats.dateTimeFormat5,
                DateFormats.timeFormat12_2
            )
            val startTimeUnit = DateFormats.formatDate(
                data.startTime,
                DateFormats.dateTimeFormat5,
                DateFormats.timeFormat12_unit
            )
            val endTime = DateFormats.formatDate(
                data.endTime,
                DateFormats.dateTimeFormat5,
                DateFormats.timeFormat12_2
            )
            val endTimeUnit = DateFormats.formatDate(
                data.endTime,
                DateFormats.dateTimeFormat5,
                DateFormats.timeFormat12_unit
            )

            this.tvStartTime.text = startTime
            this.tvStartUnit.text = startTimeUnit
            this.tvEndTime.text = endTime
            this.tvEndUnit.text = endTimeUnit

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

    private fun setSleepBannerViewPager(data: List<Nudges>?) {
        if (data.isNullOrEmpty()) {
            binding.lytSleepScore.lytSleepScoreBanner.root.gone()
            return
        } else {
            binding.lytSleepScore.lytSleepScoreBanner.root.visible()
        }

        val fragments = ArrayList<OreoSleepBannerFragment>()

        data.forEach {
            fragments.add(OreoSleepBannerFragment.newInstance(it))
        }

        val winsAdapter =
            OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)
        binding.lytSleepScore.lytSleepScoreBanner.vpBannerSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })
            adapter = winsAdapter
        }

        TabLayoutMediator(
            binding.lytSleepScore.lytSleepScoreBanner.tabLayout,
            binding.lytSleepScore.lytSleepScoreBanner.vpBannerSlider
        ) { _, _ -> }.attach()

        if (fragments.size > 1) {
            binding.lytSleepScore.lytSleepScoreBanner.tabLayout.visible()
        } else {
            binding.lytSleepScore.lytSleepScoreBanner.tabLayout.invisible()
        }


    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setRecycler() {
        binding.rvTopGraph.setOnChartScrollChangedListener(this)
        with(binding.lytSSAnalysis.rvSleepStage) {
            adapter = mSleepStageAdapter
        }
        with(binding.lytSleepContributor.rvContributor) {
            adapter = mSleepContributorAdapter
        }

    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val selectedDate =
                    data?.getStringExtra("selected_date") ?: return@registerForActivityResult

                mainViewModel.onCalendarDateSelected(selectedDate)

                LOGS.d("moveToPosition Selected Date  :${selectedDate}")

                mainViewModel.getUserHealthData(mainViewModel.mStartDate, mainViewModel.mEndDate)

            }
        }

    override fun initListener() {
        binding.lytToolbar.backBtn.invisible()
        binding.lytToolbar.tvTitle.text = getString(R.string.text_sleep)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.visible()
        binding.lytToolbar.ivAddFriend.setImageResource(R.drawable.ic_calenders)


        binding.lytSSAnalysis.lytNightMovement.bInfo.setOnClickListener {
            viewModel.contributorInfo.value?.night_time_movements?.let { content ->
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", content)
                })
            }
        }

        binding.lytHeartRate.bInfo.setOnClickListener {
            viewModel.contributorInfo.value?.hr_graph?.let { content ->
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", content)
                })
            }
        }
        binding.lytHRVariability.bInfo.setOnClickListener {
            viewModel.contributorInfo.value?.hrv_graph?.let { content ->
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", content)
                })
            }
        }
        binding.lytBloodOxygenGraph.bInfo.setOnClickListener {
            viewModel.contributorInfo.value?.oxy_graph?.let { content ->
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", content)
                })
            }
        }
        binding.lytAverageBloodOxygen.bInfo.setOnClickListener {
            viewModel.contributorInfo.value?.oxy_graph?.let { content ->
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", content)
                })
            }
        }

        binding.lytAverageBloodOxygen.bInfo.setOnClickListener {
            viewModel.contributorInfo.value?.oxy_graph?.let { content ->
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", content)
                })
            }
        }

        binding.lytEmptyView.bGoToSettings.setOnClickListener {
            showWalkAround(false)
            viewModel.ringDataStore.setSleepWalkAroundShown(true)
            viewModel.getSleepDetailsData()
        }

        /*binding.lytToolbar.view1.setOnLongClickListener {
            mainViewModel.testClearLocalHealthData()
            context.showShortToast("Cleared")
            return@setOnLongClickListener true
        }*/

        binding.lytToolbar.view1.setOnClickListener {

            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_sleep_date_range_click)
            resultLauncher.launch(
                HistoryCalendarActivity.getStartIntent(
                    requireContext(),
                    /*viewModel.sleepHistoryResponse.value?.lastOrNull()?.date
                        ?:*/ mainViewModel.selectedDate,
                    "ring"
                )
            )
        }


        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytSleepScore.lytSleepAvg.root.setOnClickListener {

            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.SLEEP.name
            mSharedViewModel.itemClickType = ViewItemClickType.SLEEP_SCORE
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "sleep")
                putString("infoData", viewModel.contributorInfo.value?.sleep_score)
                putString("date", mainViewModel.selectedDate)
            })
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_sleep_sleep_score_click)
        }
        binding.lytSleepScore.lytTotalSleep.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.SLEEP.name
            mSharedViewModel.itemClickType = ViewItemClickType.TOTAL_SLEEP
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "sleep")
                putString("infoData", viewModel.contributorInfo.value?.totalSleep)
                putString("date", mainViewModel.selectedDate)
            })

            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_sleep_total_sleep_click)
        }
        binding.lytSleepScore.lytTimeInBed.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.SLEEP.name
            mSharedViewModel.itemClickType = ViewItemClickType.TIME_IN_BED
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "sleep")
                putString("infoData", viewModel.contributorInfo.value?.time_in_bed)
                putString("date", mainViewModel.selectedDate)
            })

            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_sleep_bed_time_click)
        }
        binding.lytSleepScore.lytSleepEfficiency.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.SLEEP.name
            mSharedViewModel.itemClickType = ViewItemClickType.SLEEP_EFFICIENCY
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "sleep")
                putString("infoData", viewModel.contributorInfo.value?.sleep_efficiency)
                putString("date", mainViewModel.selectedDate)
            })

            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_sleep_sleep_efficiency_click)
        }
        binding.lytSleepScore.lytRestHr.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.SLEEP.name
            mSharedViewModel.itemClickType = ViewItemClickType.RESTING_HR
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "sleep")
                putString("infoData", viewModel.contributorInfo.value?.resting_hr)
                putString("date", mainViewModel.selectedDate)
            })

            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_sleep_resting_hr_click)
        }

        //night time see/saw
        binding.lytSSAnalysis.lytNightMovement.root.gone()
        binding.lytSSAnalysis.view1.gone()
        binding.lytSSAnalysis.rvSleepStage.gone()
        binding.lytSSAnalysis.tvSummaryTitle.gone()
        binding.lytSSAnalysis.viewUp.gone()
        binding.lytSSAnalysis.ivUp.gone()

        binding.lytSSAnalysis.viewDown.setOnClickListener {
            binding.lytSSAnalysis.lytNightMovement.root.visible()
            binding.lytSSAnalysis.view1.visible()
            binding.lytSSAnalysis.rvSleepStage.visible()
            binding.lytSSAnalysis.viewUp.visible()
            binding.lytSSAnalysis.ivUp.visible()
            binding.lytSSAnalysis.viewDown.gone()
            binding.lytSSAnalysis.ivDown.gone()
            binding.lytSSAnalysis.tvSummaryTitle.visible()


            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_sleep_stage_analysis_expand_click)
        }
        binding.lytSSAnalysis.viewUp.setOnClickListener {
            binding.lytSSAnalysis.lytNightMovement.root.gone()
            binding.lytSSAnalysis.view1.gone()
            binding.lytSSAnalysis.rvSleepStage.gone()
            binding.lytSSAnalysis.viewUp.gone()
            binding.lytSSAnalysis.ivUp.gone()
            binding.lytSSAnalysis.viewDown.visible()
            binding.lytSSAnalysis.ivDown.visible()
            binding.lytSSAnalysis.tvSummaryTitle.gone()

            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_sleep_stage_analysis_compress_click)
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
            viewModel.getMovementBreakup(nightMovementBreakUp,sleepStartTime,sleepEndTime)
        nightTimeMovementGraph.init(false)

        nightTimeMovementGraph.setData(sleepData.second)
        nightTimeMovementGraph.setData(
            sleepData.first
        )
        nightTimeMovementGraph.invalidate()

    }


    override fun subscribeObservers() {
        mainViewModel.sleepHistoryResponse.observe(viewLifecycleOwner) {

            if (it.isNullOrEmpty()) return@observe


            binding.svMain.visible()
            binding.groupHeader.visible()

            val topGraphData = viewModel.getPrefixAndSuffixList(it)
            //mSharedViewModel.selectedDate = viewModel.selectedDate?:viewModel.dateList[viewModel.dateList.size - 1]
            var moveToPos = -1


            LOGS.d("moveToPosition date initia ${mainViewModel.selectedDate}")

            if (mainViewModel.selectedDate != null) {
                val index = it?.indexOfFirst { data ->
                    data.date.equals(mainViewModel.selectedDate, true)
                }
                if (index != null) {

                    moveToPos = 15 + (it.size - index - 1)
                    LOGS.w(TAG, "Selected date ${mainViewModel.selectedDate} $moveToPos")
                    //binding.rvTopGraph.moveToPosition(15 + (15-index-1))
                }

            }
            binding.rvTopGraph.updateDataWithMax(
                topGraphData.first,
                topGraphData.third,
                topGraphData.second,
                moveToPos
            )

            viewModel.getContributorInfo()

            val returnDate = mainViewModel.updateSelectedDate(mainViewModel.selectedDate)
            if (returnDate != null) {
                mainViewModel.selectedDate = returnDate
            }
        }

        mainViewModel.daySleepData.observe(this) {
            updateUi(it)
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
        mainViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar1.root.visible()
            } else {
                binding.progressBar1.root.gone()
            }
        }

        /*
                viewModel.sleepHistoryResponse.observe(this) {
                    if (it.sleepHistory == null) return@observe
                    var date = ""
                    var count = 7
                    var historyType = "daily"
                    val averageBedTime = DateFormats.convert24HourTo12(it.avg_bedtime)
                    showSleepGraph(
                        count,
                        it.sleepHistory,
                        historyType,
                        it.avg_duration ?: 0,
                        averageBedTime,
                        date
                    )

                }*/
    }

    private fun setScrollDate() {

    }

    private fun setSleepScore(sleepScoreData: CommonDataModel) {
        binding.lytSleepScore.lytSleepAvg.tvValue.text = sleepScoreData.value.toString()
        val statusColor = ContextCompat.getColor(
            binding.lytSleepScore.lytSleepAvg.tvValue.context,
            viewModel.getStatusColors(sleepScoreData.status)
        )
        binding.lytSleepScore.lytSleepAvg.tvQuality.setTextColor(statusColor)
        binding.lytSleepScore.lytSleepAvg.tvQuality.text = sleepScoreData.text
        binding.lytSleepScore.lytSleepAvg.tvQuality.visible()
        viewModel.sessionManager.logMoEngageAppEvent(
            MoEngageLunaAppEvents.luna_sleep_page_visit,
            HashMap<String, Any>().apply {
                this[MoEngageAppEventParams.status] = sleepScoreData.status
            })
    }

    private fun updateUi(dayData: OreoSleepModel) {
        //for sleep score
        binding.lytSleepScore.lytSleepAvg.tvTitle.text = getString(R.string.text_sleep_score_o)
        binding.lytSleepScore.lytTotalSleep.tvTitle.text = getString(R.string.text_total_sleep)
        binding.lytSleepScore.lytTimeInBed.tvTitle.text = getString(R.string.text_time_in_bed)
        binding.lytSleepScore.lytRestHr.tvTitle.text = "Average HR"
        binding.lytSleepScore.lytSleepEfficiency.tvTitle.text =
            getString(R.string.text_sleep_efficiency)
        setSleepBannerViewPager(dayData.nudges)
        val sleepScoreData = dayData.sleepScore
        if (sleepScoreData != null) {
            binding.lytSleepScore.lytSleepAvg.emptyText.gone()
            binding.lytSleepScore.lytSleepAvg.tvValue.visible()
            if (sleepScoreData.value != null) {
                if (sleepScoreData.value == 0) {
                    if (dayData.totalSleep?.value != 0) {
                        setSleepScore(sleepScoreData)
                    } else {
                        binding.lytSleepScore.lytSleepAvg.tvValue.text = "-"
                        binding.lytSleepScore.lytSleepAvg.tvQuality.gone()
                    }
                } else {
                    setSleepScore(sleepScoreData)
                }
            } else {
                binding.lytSleepScore.lytSleepAvg.tvValue.text = "-"
                binding.lytSleepScore.lytSleepAvg.tvQuality.gone()

            }

            //total sleep
            if (dayData.totalSleep != null) {
                val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                    dayData.totalSleep?.value ?: 0
                )
                binding.lytSleepScore.lytTotalSleep.lytHrMn.tvHour.text = "$hour"
                binding.lytSleepScore.lytTotalSleep.lytHrMn.tvMinute.text = "$minute"
                totalSleepDataView()

            } else {
                totalSleepNoDataView()
            }

            //time in bed
            if (dayData.timeInBed != null) {
                binding.lytSleepScore.lytTimeInBed.lytHrMn.root.visible()
                val (hourTimeInBed, minuteTimeInBed) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                    dayData.timeInBed?.value ?: 0
                )
                binding.lytSleepScore.lytTimeInBed.lytHrMn.tvHour.text = "$hourTimeInBed"
                binding.lytSleepScore.lytTimeInBed.lytHrMn.tvMinute.text = "$minuteTimeInBed"

                totalTimeInBedDataView()
            } else {
                totalTimeInBedDefaultView()
            }

            //sleep efficiency
            if (dayData.sleepEfficiency != null) {
                binding.lytSleepScore.lytSleepEfficiency.lytHrMn.tvHour.text =
                    "${dayData.sleepEfficiency?.valPrcnt ?: 0}%"
                sleepEfficiencyDataView()
            } else {
                sleepEfficiencyDefaultView()
            }

            //resting heart rate
            if (dayData.restingHr != null) {
                if (dayData.restingHr!!.value == 0 || dayData.restingHr!!.value == 255) {
                    restingHrDefaultView()
                } else {
                    binding.lytSleepScore.lytRestHr.lytHrMn.tvHour.text =
                        "${dayData.restingHr?.value ?: 0}"
                    binding.lytSleepScore.lytRestHr.lytHrMn.tvHourUnit.text = " bpm"
                    restingHrDataView()
                }
            } else {
                restingHrDefaultView()
            }

        } else {
            if (viewModel.ringDataStore.getRegisterDay() == 0) {
                binding.lytSleepScore.lytSleepAvg.emptyText.text =
                    getString(R.string.text_you_will_see_your_sleep_score_after_your_first_sleep_analysis)
                binding.lytSleepScore.lytSleepAvg.emptyText.visible()
                binding.lytSleepScore.lytSleepAvg.tvValue.gone()
                binding.lytSleepScore.lytSleepAvg.tvQuality.gone()
            } else {
                binding.lytSleepScore.lytSleepAvg.emptyText.gone()
                binding.lytSleepScore.lytSleepAvg.tvValue.text = "-"
                binding.lytSleepScore.lytSleepAvg.tvQuality.gone()
            }
            totalSleepNoDataView()
            totalTimeInBedDefaultView()
            sleepEfficiencyDefaultView()
            restingHrDefaultView()
        }


        //sleep contributor
        binding.lytSleepContributor.tvTitle.text = getString(R.string.text_sleep_contributors)
        mSleepContributorAdapter.setData(viewModel.getContributorsData(dayData), 1)


        //sleep night movement
        binding.lytSSAnalysis.lytNightMovement.tvTitle.text =
            getString(R.string.text_night_time_movement)

        val sleepStartTime = dayData.hourly_breakup?.first()?.start_time /*DateFormats.formatDate(
            dayData.hourly_breakup?.first()?.start_time,
            DateFormats.dateTimeFormat5,
            DateFormats.time12Meridian
        )*/
        val sleepEndTime = dayData.hourly_breakup?.last()?.end_time/*DateFormats.formatDate(
            dayData.hourly_breakup?.last()?.end_time,
            DateFormats.dateTimeFormat5,
            DateFormats.time12Meridian
        )*/
        //heart rate
        binding.lytHeartRate.tvTitle.text = getString(R.string.text_heart_rate)
        binding.lytHeartRate.tvSubtitle1.text = getString(R.string.text_lowest_hr)
        binding.lytHeartRate.tvSubtitle2.text = getString(R.string.text_average_hr)
        val heartRateData = dayData.hr
        if (heartRateData != null) {
            if (heartRateData.avg != null) {
                if (heartRateData.avg == 0 || heartRateData.avg == 255) {
                    binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "-"
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
                } else {
                    binding.lytHeartRate.lytSubtitleValue2.tvValue.text =
                        heartRateData.avg.toString()
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.visible()
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.text = "bpm"
                }
            } else {
                binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "-"
                binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
            }
            if (heartRateData.low != null) {
                if (heartRateData.low == 0 || heartRateData.low == 255) {
                    binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
                } else {
                    binding.lytHeartRate.lytSubtitleValue1.tvValue.text =
                        heartRateData.low.toString()
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.visible()
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.text = "bpm"
                }
            } else {
                binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
                binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
            }
        } else {
            binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
            binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "-"
            binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
            binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
        }
        showHeartRateGraph(dayData.hr, sleepStartTime, sleepEndTime)

        //heart variability
        binding.lytHRVariability.tvTitle.text = getString(R.string.text_heart_rate_variability)
        binding.lytHRVariability.tvSubtitle1.text =
            getString(R.string.text_average_hrv)
        binding.lytHRVariability.tvSubtitle2.text =
            getString(R.string.text_max)
        val heartVariabilityData = dayData.hrv
        if (heartVariabilityData != null) {
            if (heartVariabilityData.avg != null) {
                if (heartVariabilityData.avg == 0 || heartVariabilityData.avg == 255) {
                    binding.lytHRVariability.lytSubtitleValue1.tvValue.text = "-"
                    binding.lytHRVariability.lytSubtitleValue1.tvUnit.gone()
                } else {
                    binding.lytHRVariability.lytSubtitleValue1.tvValue.text =
                        heartVariabilityData.avg.toString()
                    binding.lytHRVariability.lytSubtitleValue1.tvUnit.visible()
                    binding.lytHRVariability.lytSubtitleValue1.tvUnit.text = "ms"
                }
            } else {
                binding.lytHRVariability.lytSubtitleValue1.tvValue.text = "-"
                binding.lytHRVariability.lytSubtitleValue1.tvUnit.gone()
            }
            if (heartVariabilityData.max != null) {
                if (heartVariabilityData.max == 0 || heartVariabilityData.max == 255) {
                    binding.lytHRVariability.lytSubtitleValue2.tvValue.text = "-"
                    binding.lytHRVariability.lytSubtitleValue2.tvUnit.gone()
                } else {
                    binding.lytHRVariability.lytSubtitleValue2.tvValue.text =
                        heartVariabilityData.max.toString()
                    binding.lytHRVariability.lytSubtitleValue2.tvUnit.visible()
                    binding.lytHRVariability.lytSubtitleValue2.tvUnit.text = "ms"
                }
            } else {
                binding.lytHRVariability.lytSubtitleValue2.tvValue.text = "-"
                binding.lytHRVariability.lytSubtitleValue2.tvUnit.gone()
            }
        } else {
//            binding.lytHRVariability.lineChart.gone()
            binding.lytHRVariability.lytSubtitleValue1.tvValue.text = "-"
            binding.lytHRVariability.lytSubtitleValue2.tvValue.text = "-"
            binding.lytHRVariability.lytSubtitleValue2.tvUnit.gone()
            binding.lytHRVariability.lytSubtitleValue1.tvUnit.gone()
        }
        showHeartRateVariabilityGraph(dayData.hrv, sleepStartTime, sleepEndTime)

        //showBloodOxygenGraph(dayData.oxy, sleepStartTime, sleepEndTime)

        showAverageBloodOxygen(dayData.oxy)

        mSleepStageAdapter.setData(viewModel.getStepAnalysisData(dayData))

        binding.lytSSAnalysis.lytTotalSleep.apply {

            if (dayData.totalSleep != null) {
                val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(
                    dayData.totalSleep?.value ?: 0
                )
                this.tvHour.text = "$hour"
                this.tvMin.text = "$minute"
            } else {

                this.tvHour.text = "_"
                this.tvMin.text = "_"
            }

        }

        initSleepAnalysisGraph(dayData.hourly_breakup)

        setNightTimeMovement(dayData.night_time_movement,sleepStartTime, sleepEndTime)

        setNapData(dayData.naps, dayData.date)

    }

    private fun setNapData(naps: List<Nap>?, date: String) {

        if (naps.isNullOrEmpty()) {
            binding.lytNaps.root.gone()
            return
        } else {
            binding.lytNaps.root.visible()
        }

        binding.lytNaps.lytNap.rvNap.layoutManager =
            LinearLayoutManager(binding.lytNaps.lytNap.rvNap.context)
        binding.lytNaps.lytNap.rvNap.adapter = DashNapAdapter(naps, date, true).apply {

            this.setOnNapSelectedListener(object : OnNapSelectedAction {
                override fun onNapSelected(napId: String) {
                    navigate(R.id.napDetails, bundleOf("napId" to napId))
                }
            })
        }
    }

    private fun setNightTimeMovement(
        hourlyBreakup: List<SleepMovementBreakup>?,
        sleepStartTime: String?,
        sleepEndTime: String?
    ) {
        showNightTimeMovementGraph(hourlyBreakup,sleepStartTime,sleepEndTime)
    }

    private fun totalSleepDataView() {
        binding.lytSleepScore.lytTotalSleep.lytHrMn.root.visible()
        binding.lytSleepScore.lytTotalSleep.tvPercentValue.gone()
        binding.lytSleepScore.lytTotalSleep.lytBpmView.root.gone()
        binding.lytSleepScore.lytTotalSleep.lytHrMn.tvHourUnit.visible()
        binding.lytSleepScore.lytTotalSleep.lytHrMn.tvMinute.visible()
        binding.lytSleepScore.lytTotalSleep.lytHrMn.tvMinuteUnit.visible()
    }

    private fun totalSleepNoDataView() {
        binding.lytSleepScore.lytTotalSleep.tvPercentValue.gone()
        binding.lytSleepScore.lytTotalSleep.lytBpmView.root.gone()
        binding.lytSleepScore.lytTotalSleep.lytHrMn.root.visible()
        binding.lytSleepScore.lytTotalSleep.lytHrMn.tvHour.text = "-"
        binding.lytSleepScore.lytTotalSleep.lytHrMn.tvHourUnit.gone()
        binding.lytSleepScore.lytTotalSleep.lytHrMn.tvMinute.gone()
        binding.lytSleepScore.lytTotalSleep.lytHrMn.tvMinuteUnit.gone()
    }

    private fun totalTimeInBedDataView() {
        binding.lytSleepScore.lytTimeInBed.tvPercentValue.gone()
        binding.lytSleepScore.lytTimeInBed.lytBpmView.root.gone()
        binding.lytSleepScore.lytTimeInBed.lytHrMn.root.visible()
        binding.lytSleepScore.lytTimeInBed.lytHrMn.tvHourUnit.visible()
        binding.lytSleepScore.lytTimeInBed.lytHrMn.tvMinute.visible()
        binding.lytSleepScore.lytTimeInBed.lytHrMn.tvMinuteUnit.visible()
    }

    private fun totalTimeInBedDefaultView() {
        binding.lytSleepScore.lytTimeInBed.tvPercentValue.gone()
        binding.lytSleepScore.lytTimeInBed.lytBpmView.root.gone()
        binding.lytSleepScore.lytTimeInBed.lytHrMn.root.visible()
        binding.lytSleepScore.lytTimeInBed.lytHrMn.tvHour.text = "-"
        binding.lytSleepScore.lytTimeInBed.lytHrMn.tvHourUnit.gone()
        binding.lytSleepScore.lytTimeInBed.lytHrMn.tvMinute.gone()
        binding.lytSleepScore.lytTimeInBed.lytHrMn.tvMinuteUnit.gone()
    }

    private fun sleepEfficiencyDataView() {
        binding.lytSleepScore.lytSleepEfficiency.tvPercentValue.gone()
        binding.lytSleepScore.lytSleepEfficiency.lytBpmView.root.gone()
        binding.lytSleepScore.lytSleepEfficiency.lytHrMn.tvHourUnit.gone()
        binding.lytSleepScore.lytSleepEfficiency.lytHrMn.tvMinute.gone()
        binding.lytSleepScore.lytSleepEfficiency.lytHrMn.tvMinuteUnit.gone()
        binding.lytSleepScore.lytSleepEfficiency.lytHrMn.root.visible()

    }

    private fun sleepEfficiencyDefaultView() {
        binding.lytSleepScore.lytSleepEfficiency.tvPercentValue.visible()
        binding.lytSleepScore.lytSleepEfficiency.lytBpmView.root.gone()
        binding.lytSleepScore.lytSleepEfficiency.lytHrMn.root.gone()
        binding.lytSleepScore.lytSleepEfficiency.tvPercentValue.text = "-"
    }

    private fun restingHrDataView() {
        binding.lytSleepScore.lytRestHr.lytHrMn.root.visible()
        binding.lytSleepScore.lytRestHr.tvPercentValue.gone()
        binding.lytSleepScore.lytRestHr.lytHrMn.tvMinute.gone()
        binding.lytSleepScore.lytRestHr.lytHrMn.tvMinuteUnit.gone()
        binding.lytSleepScore.lytRestHr.lytBpmView.root.gone()
    }

    private fun restingHrDefaultView() {
        binding.lytSleepScore.lytRestHr.tvPercentValue.gone()
        binding.lytSleepScore.lytRestHr.lytBpmView.root.visible()
        binding.lytSleepScore.lytRestHr.lytHrMn.root.gone()
        binding.lytSleepScore.lytRestHr.lytBpmView.tvValue.text = "-"
        binding.lytSleepScore.lytRestHr.lytBpmView.tvUnit.gone()
    }


    override fun onPositionSelected(position: Int, chartModel: ChartModel?) {
        if (mainViewModel.selectedDate == chartModel?.date!!) {
            return
        }
        //mSharedViewModel.selectedDate = chartModel.date!!
        LOGS.w("moveToPosition onPositionSelected ${chartModel.date}")
        mainViewModel.selectedDate = chartModel.date!!
        val returnDate = mainViewModel.updateSelectedDate(mainViewModel.selectedDate)
        if (returnDate != null) {
            mainViewModel.selectedDate = returnDate
        }

        if (mainViewModel.shouldLoadMoreData()) {
            LOGS.w("Loading more data")
        }

    }

    override fun onScrolling(position: Int, chartModel: ChartModel?) {

    }

}