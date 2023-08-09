package  com.oreo.ui.sleep

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOreoSleepDetailBinding
import com.noisefit.ui.dashboard.graphs.HistoryCalendarActivity
import com.noisefit.util.ApplicationUtils

import com.noisefit_commans.ui.*
import com.noisefit_commans.ui.custom.NightTimeGraphViewOreo
import com.noisefit_commans.ui.custom.SleepGraphViewOreo
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributors
import com.oreo.data.model.SleepChartModel
import com.oreo.data.model.health.*
import com.oreo.ui.custom.ScrollListener
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
    private var sleepDayGraphView: SleepGraphViewOreo? = null


    private val mSleepStageAdapter: OreoSleepStageAnalysisAdapter by lazy {
        OreoSleepStageAnalysisAdapter()
    }
    private val mSleepContributorAdapter: OreoSleepContributorAdapter by lazy {
        OreoSleepContributorAdapter(object :
            OreoSleepContributorAdapter.ContributorItemClickListener {
            override fun onItemClick(resultData: ArrayList<Contributors>, position: Int) {
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
        } else {
            binding.lytEmptyView.root.gone()
        }
    }

    private fun showHeartRateVariabilityGraph(
        hrv: CommonListDataModel?,
        sleepStartTime: String?,
        sleepEndTime: String?
    ) {

        if (hrv?.value.isNullOrEmpty()) {
            binding.lytHRVariability.lineChart.gone()
            return
        }


        val baseTimeList =
            UtilClass.graphTwoHoursInterval(sleepStartTime, sleepEndTime, hrv?.value?.size ?: 288)



        binding.lytHRVariability.lineChart.visible()
        val sleepChart = SleepChartModel()
        val chartList: MutableList<ChartModel> = java.util.ArrayList()

        hrv?.value?.forEachIndexed { index, it ->
            val chartModel = ChartModel()

            var value = it
            if (value == 255) {
                value = 0
            }

            chartModel.value = value
            chartModel.index = baseTimeList[index]
            chartModel.date = ""
            chartList.add(chartModel)
        }

        sleepChart.list = chartList

        binding.lytHRVariability.lineChart.updateGraphColor(
            Color.parseColor("#ff80e3"),
            Color.parseColor("#4cff59da"),
            Color.parseColor("#00ff59da")
        )
//        binding.lytHRVariability.lineChart.updateDataWithMax(sleepChart, 5, true, false)
    }

    private fun showHeartRateGraph(
        heartRateList: CommonListDataModel?,
        sleepStartTime: String?,
        sleepEndTime: String?
    ) {
        if (heartRateList?.value.isNullOrEmpty()) {
            binding.lytHeartRate.lineChart.gone()
            return
        }



        val baseTimeList = UtilClass.graphTwoHoursInterval(
            sleepStartTime,
            sleepEndTime,
            heartRateList?.value?.size ?: 288
        )

        LOGS.d("dsakjdsalkjsladjlksdajldsajldsajl ${heartRateList?.value?.size} ${Gson().toJson(baseTimeList)}")

        binding.lytHeartRate.lineChart.visible()
        val sleepChart = SleepChartModel()
        val chartList = ArrayList<ChartModel>()
        heartRateList?.value?.forEachIndexed { index, data ->
            val chartModel = ChartModel()

            var value = data
            if (value == 255) {
                value = 0
            }


            chartModel.value = value
            chartModel.index = baseTimeList[index]
            chartList.add(chartModel)
        }


        sleepChart.list = chartList
        binding.lytHeartRate.lineChart.updateGraphColor(
            Color.parseColor("#ff6b86"),
            Color.parseColor("#4cff6581"),
            Color.parseColor("#00ff6581")
        )

        binding.lytHeartRate.lineChart.updateDataWithMax(sleepChart, 5, false, true)

    }


    private fun initSleepAnalysisGraph(hourlyBreakup: List<SleepHourlyBreakup>?) {

        sleepDayGraphView = SleepGraphViewOreo(requireContext())
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

        if (fragments.size > 0) {
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

                val selectedDate = data?.getStringExtra("selected_date")
                LOGS.d("Selected Date  :${selectedDate}")

//                if (viewModel.graphInterval.value == GraphInterval.DAY) {
//                    selectedDate?.let {
//                        val (start, end) = DateFormats.getStartEndDateDay(selectedDate)
//                        viewModel.selectedStartDate = start
//                        viewModel.selectedEndDate = end
//                    }
//                } else if (viewModel.graphInterval.value == GraphInterval.WEEK) {
//                    selectedDate?.let {
//                        val (start, end) = DateFormats.getStartEndDateWeek(it)
//                        viewModel.selectedStartDate = start
//                        viewModel.selectedEndDate = end
//                    }
//                }
//                viewModel.getSleepData()

                viewModel.getSleepDetailsData(selectedDate)
                if (selectedDate != null) {
                    viewModel.updateSelectedDate(selectedDate)
                }

            }
        }

    override fun initListener() {
        binding.lytToolbar.backBtn.invisible()
        binding.lytToolbar.tvTitle.text = getString(R.string.text_sleep)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.visible()
        binding.lytToolbar.ivAddFriend.setImageResource(R.drawable.ic_calenders)

        binding.lytEmptyView.bGoToSettings.setOnClickListener {
            showWalkAround(false)
            viewModel.ringDataStore.setSleepWalkAroundShown(true)
            viewModel.getSleepDetailsData()
        }


        binding.lytToolbar.view1.setOnClickListener {
            resultLauncher.launch(
                HistoryCalendarActivity.getStartIntent(
                    requireContext(),
                    mSharedViewModel.selectedDate,
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
            mSharedViewModel.itemClickType = ViewItemClickType.SLEEP_SCORE.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "sleep")
            })
        }
        binding.lytSleepScore.lytTotalSleep.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.SLEEP.name
            mSharedViewModel.itemClickType = ViewItemClickType.TOTAL_SLEEP.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "sleep")
            })
        }
        binding.lytSleepScore.lytTimeInBed.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.SLEEP.name
            mSharedViewModel.itemClickType = ViewItemClickType.TIME_IN_BED.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "sleep")
            })
        }
        binding.lytSleepScore.lytSleepEfficiency.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.SLEEP.name
            mSharedViewModel.itemClickType = ViewItemClickType.SLEEP_EFFICIENCY.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "sleep")
            })
        }
        binding.lytSleepScore.lytRestHr.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.SLEEP.name
            mSharedViewModel.itemClickType = ViewItemClickType.RESTING_HR.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "sleep")
            })
        }

        //night time see/saw
        binding.lytSSAnalysis.lytNightMovement.root.gone()
        binding.lytSSAnalysis.view1.gone()
        binding.lytSSAnalysis.rvSleepStage.gone()
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
        }
        binding.lytSSAnalysis.viewUp.setOnClickListener {
            binding.lytSSAnalysis.lytNightMovement.root.gone()
            binding.lytSSAnalysis.view1.gone()
            binding.lytSSAnalysis.rvSleepStage.gone()
            binding.lytSSAnalysis.viewUp.gone()
            binding.lytSSAnalysis.ivUp.gone()
            binding.lytSSAnalysis.viewDown.visible()
            binding.lytSSAnalysis.ivDown.visible()
        }
    }

    private fun showNightTimeMovementGraph(nightMovementBreakUp: List<SleepMovementBreakup>?) {

        val nightTimeMovementGraph = NightTimeGraphViewOreo(requireContext())
        binding.lytSSAnalysis.lytNightMovement.flNightTimeMovement.removeAllViews()
        binding.lytSSAnalysis.lytNightMovement.flNightTimeMovement.addView(nightTimeMovementGraph)

        val sleepData =
            viewModel.getMovementBreakup(nightMovementBreakUp)
        nightTimeMovementGraph.init(false)

        nightTimeMovementGraph.setData(sleepData.second)
        nightTimeMovementGraph.setData(
            sleepData.first
        )
        nightTimeMovementGraph.invalidate()

    }


    override fun subscribeObservers() {
        viewModel.sleepHistoryResponse.observe(this) {

            binding.svMain.visible()

            val topGraphData = viewModel.getPrefixAndSuffixList(it)
            mSharedViewModel.selectedDate = viewModel.dateList[viewModel.dateList.size - 1]
            binding.rvTopGraph.updateDataWithMax(
                topGraphData.first,
                topGraphData.third,
                topGraphData.second
            )
            viewModel.getContributorInfo()
        }

        viewModel.daySleepData.observe(this) {
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
    }

    private fun updateUi(dayData: OreoSleepModel) {
        //for sleep score
        binding.lytSleepScore.lytSleepAvg.tvTitle.text = getString(R.string.text_sleep_score_o)
        binding.lytSleepScore.lytTotalSleep.tvTitle.text = getString(R.string.text_total_sleep)
        binding.lytSleepScore.lytTimeInBed.tvTitle.text = getString(R.string.text_time_in_bed)
        binding.lytSleepScore.lytRestHr.tvTitle.text = "Resting HR"
        binding.lytSleepScore.lytSleepEfficiency.tvTitle.text =
            getString(R.string.text_sleep_efficiency)
        setSleepBannerViewPager(dayData.nudges)
        val sleepScoreData = dayData.sleepScore
        if (sleepScoreData != null) {
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
        mSleepContributorAdapter.setData(viewModel.getContributorsData(dayData))


        //sleep night movement
        binding.lytSSAnalysis.lytNightMovement.tvTitle.text =
            getString(R.string.text_night_time_movement)

        val sleepStartTime = DateFormats.formatDate(
            dayData.hourly_breakup?.first()?.start_time,
            DateFormats.dateTimeFormat5,
            DateFormats.time12Meridian
        )
        val sleepEndTime = DateFormats.formatDate(
            dayData.hourly_breakup?.last()?.end_time,
            DateFormats.dateTimeFormat5,
            DateFormats.time12Meridian
        )
        //heart rate
        binding.lytHeartRate.tvTitle.text = getString(R.string.text_heart_rate)
        val heartRateData = dayData.hr
        if (heartRateData != null) {
            showHeartRateGraph(dayData.hr, sleepStartTime, sleepEndTime)

            binding.lytHeartRate.tvSubtitle1.text = getString(R.string.text_average)
            binding.lytHeartRate.tvSubtitle2.text = getString(R.string.text_lowest_heart_rate)
            if (heartRateData.low != null) {
                if (heartRateData.low == 0 || heartRateData.low == 255) {
                    binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "-"
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
                } else {
                    binding.lytHeartRate.lytSubtitleValue2.tvValue.text =
                        heartRateData.low.toString()
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.visible()
                    binding.lytHeartRate.lytSubtitleValue2.tvUnit.text = "bpm"
                }
            } else {
                binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "-"
                binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
            }
            if (heartRateData.avg != null) {
                if (heartRateData.avg == 0 || heartRateData.avg == 255) {
                    binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
                } else {
                    binding.lytHeartRate.lytSubtitleValue1.tvValue.text =
                        heartRateData.avg.toString()
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.visible()
                    binding.lytHeartRate.lytSubtitleValue1.tvUnit.text = "bpm"
                }
            } else {
                binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
                binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
            }
        } else {

            binding.lytHeartRate.lineChart.gone()
            binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
            binding.lytHeartRate.lytSubtitleValue2.tvValue.text = "-"
            binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
            binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
        }


        //heart variability
        binding.lytHRVariability.tvTitle.text = getString(R.string.text_heart_rate_variability)
        val heartVariabilityData = dayData.hrv
        if (heartVariabilityData != null) {
            showHeartRateVariabilityGraph(dayData.hrv, sleepStartTime, sleepEndTime)

            binding.lytHRVariability.tvSubtitle1.text =
                getString(R.string.text_heart_rate_variability)
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
            binding.lytHRVariability.tvSubtitle2.text =
                getString(R.string.text_max)
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
            binding.lytHRVariability.lineChart.gone()
            binding.lytHRVariability.lytSubtitleValue1.tvValue.text = "-"
            binding.lytHRVariability.lytSubtitleValue2.tvValue.text = "-"
            binding.lytHRVariability.lytSubtitleValue2.tvUnit.gone()
            binding.lytHRVariability.lytSubtitleValue1.tvUnit.gone()
        }

        mSleepStageAdapter.setData(viewModel.getStepAnalysisData(dayData))
        initSleepAnalysisGraph(dayData.hourly_breakup)

        setNightTimeMovement(dayData.night_time_movement)

    }

    private fun setNightTimeMovement(hourlyBreakup: List<SleepMovementBreakup>?) {
        showNightTimeMovementGraph(hourlyBreakup)
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
        if (mSharedViewModel.selectedDate == chartModel?.date!!) {
            return
        }
        mSharedViewModel.selectedDate = chartModel.date!!
        chartModel.date?.let { viewModel.updateSelectedDate(it) }

    }

    override fun onScrolling(position: Int, chartModel: ChartModel?) {

    }

}