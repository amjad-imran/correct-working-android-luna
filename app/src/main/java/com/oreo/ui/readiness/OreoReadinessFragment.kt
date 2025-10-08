package com.oreo.ui.readiness

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOreoReadinessBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppConversionUtils
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MiscUtil
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributors
import com.oreo.data.model.GraphDummyModel
import com.oreo.data.model.SleepChartModel
import com.oreo.data.model.health.CommonDataModel
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.data.model.health.UnitDataModelArrayFloat
import com.oreo.data.model.sleep.HealthTrend
import com.oreo.ui.calendar.SELECTED_DATE
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.custom.LineChartType
import com.oreo.ui.custom.OnLinearChartClickAction
import com.oreo.ui.custom.ScrollListener
import com.oreo.ui.sleep.OreoSleepContributorAdapter
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.sleep.scoredetails.ClickViewType
import com.oreo.ui.sleep.scoredetails.SharedOSCDViewModel
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import com.oreo.ui.sleep2.internal.SleepInternalDetailsFragment
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import com.oreo.util.EventUtil
import com.oreo.util.UtilClass
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import androidx.core.graphics.toColorInt
import com.noisefit.oreo.OreoMainActivity
import com.oreo.data.model.IrregularEventsChipModel
import com.oreo.ui.chatGpt.ChatGptFragment
import com.oreo.ui.chatGpt.PlanType


@AndroidEntryPoint
class OreoReadinessFragment :
    BaseFragment<FragmentOreoReadinessBinding>(FragmentOreoReadinessBinding::inflate),
    ScrollListener {
    private val mViewModel: OreoReadinessViewModel by viewModels()
    private val mSharedViewModel: SharedOSCDViewModel by activityViewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()


    @Inject
    lateinit var vibrationUtils: VibrationUtils


    private val mReadinessConAdapter: OreoSleepContributorAdapter by lazy {
        OreoSleepContributorAdapter(object :
            OreoSleepContributorAdapter.ContributorItemClickListener {
            override fun onItemClick(
                resultData: ArrayList<Contributors>,
                position: Int,
                version: Int
            ) {
                openContributorBottomSheet(resultData, position, version)
                //handleEvent(resultData[position].title)
            }

        })
    }

    private fun handleEvent(title: String) {
        var eventName = ""
        when (title) {
            "Sleep score" -> eventName = MoEngageLunaAppEvents.luna_readiness_contrib_sscore_click
            "Activity Score" -> eventName =
                MoEngageLunaAppEvents.luna_readiness_contrib_activity_click

            "Recovery index" -> eventName =
                MoEngageLunaAppEvents.luna_readiness_contrib_recovery_click

            "Sleep regularity" -> eventName =
                MoEngageLunaAppEvents.luna_readiness_contrib_regularity_click

            "Sleep balance" -> eventName =
                MoEngageLunaAppEvents.luna_readiness_contrib_sbalance_click

            "Average HR" -> eventName = MoEngageLunaAppEvents.luna_readiness_contrib_heartrate_click
            "Activity balance" -> eventName =
                MoEngageLunaAppEvents.luna_readiness_contrib_abalance_click

            "HRV balance" -> eventName =
                MoEngageLunaAppEvents.luna_readiness_contrib_hrv_balance_click

            "Skin temperature" -> eventName =
                MoEngageLunaAppEvents.luna_readiness_contrib_skin_temp_click
        }
        mViewModel.sessionManager.logMoEngageAppEvent(eventName)

    }

    private fun openContributorBottomSheet(
        resultData: ArrayList<Contributors>,
        position: Int,
        contriVer: Int
    ) {
        val descList = mViewModel.prepareDataForDescriptionArray(resultData, contriVer)
        navigate(
            OreoReadinessFragmentDirections.actionNavigationReadinessDetailsFragToDescriptionPopUpBottomDialogFragment(
                position,
                descList.toTypedArray(),
                resultData[position].title,
                ClickViewType.READINESS.name
            )
        )

    }

    override fun onResume() {
        super.onResume()
        mainViewModel.shouldResetMasterDates()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()

        mViewModel.loadAlertsData()

        if (mViewModel.ringDataStore.isReadinessWalkAroundShown()) {
            mViewModel.getReadinessDetailsData()
        } else {
            showWalkAround(true)
        }

    }

    fun showInternalTrend(state: SleepInternalLaunchState, selectedDate: String) {
        val (frag, bundle) = SleepInternalDetailsFragment.getStartData(
            state, selectedDate, "readiness"
        )
        navigate(frag, bundle)
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

    private fun handleNudges(){
        val data = mainViewModel.localDataStore.getNudgeReadinessData()
        val list = ArrayList<Nudges>()
        data?.cue1?.let { list.add(Nudges(
            it.title ?: "",
            it.description ?: ""
        )) }
        data?.cue2?.let { list.add(Nudges(
            it.title ?: "",
            it.description ?: ""
        )) }
        setReadinessBannerViewPager(list)
    }

    private fun setReadinessBannerViewPager(data: List<Nudges>?) {

        if (data.isNullOrEmpty()) {
            binding.lytRScoreData.lytAScoreBanner.root.gone()
            return
        } else {
            binding.lytRScoreData.lytAScoreBanner.root.visible()
        }
        val fragments = ArrayList<OreoReadinessBannerFragment>()
        data.forEach {
            fragments.add(OreoReadinessBannerFragment.newInstance(it).apply {
                setClickListener(
                    object : NudgeBannerListener {
                        override fun onAiClicked() {
                            if (mainViewModel.ringDataStore.getRingDevice() == null) {
                                context.showShortToast(getString(R.string.text_luna_ai_message))
                                return
                            }

                            uiController.logAppEvent(
                                MoEngageLunaAppEvents.aichat_initiated_clicked,
                                hashMapOf("source" to "readiness")
                            )


                            navigate(
                                R.id.aiTopQuestionsFragment,
                                bundleOf("aiTopic" to AITopics.READINESS)
                            )
                        }
                    }
                )
            })
        }
        val sleepBannerAdapter =
            OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)
        binding.lytRScoreData.lytAScoreBanner.vpBannerSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })
            adapter = sleepBannerAdapter
        }

        TabLayoutMediator(
            binding.lytRScoreData.lytAScoreBanner.tabLayout,
            binding.lytRScoreData.lytAScoreBanner.vpBannerSlider
        ) { _, _ -> }.attach()

        if (fragments.size > 1) {
            binding.lytRScoreData.lytAScoreBanner.tabLayout.visible()
        } else {
            binding.lytRScoreData.lytAScoreBanner.tabLayout.invisible()
        }
    }


    private fun setHrLowestHr() {

        binding.lytHeartRate.tvSubtitle1.text = getString(R.string.text_lowest_hr)
        if (mViewModel.lowestHr == null || mViewModel.lowestHr == 0 || mViewModel.lowestHr == 255) {
            binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "-"
            binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
        } else {
            binding.lytHeartRate.lytSubtitleValue1.tvValue.text = "${mViewModel.lowestHr}"
            binding.lytHeartRate.lytSubtitleValue1.tvUnit.text = getString(R.string.text_bpm_small)
            binding.lytHeartRate.lytSubtitleValue1.tvUnit.visible()
        }
    }

    private fun setHrvAvg() {

        binding.lytHRVariability.tvSubtitle1.text = getString(R.string.text_average)
        if (mViewModel.avgHrv != null && mViewModel.avgHrv != 0) {
            binding.lytHRVariability.lytSubtitleValue1.tvValue.text = "${mViewModel.avgHrv}"
            binding.lytHRVariability.lytSubtitleValue1.tvUnit.text = getString(R.string.text_ms)
            binding.lytHRVariability.lytSubtitleValue1.tvUnit.visible()
        } else {
            binding.lytHRVariability.lytSubtitleValue1.tvValue.text = "-"
            binding.lytHRVariability.lytSubtitleValue1.tvUnit.gone()
        }
    }


    private fun showHeartRateGraph(
        data: OreoReadinessModel?,
        sleepStartTime: String?,
        sleepEndTime: String?,
    ) {

        binding.lytHeartRate.tvTitle.text = getString(R.string.text_heart_rate)


        mViewModel.lowestHr = data?.hrBreakUp?.low
        setHrLowestHr()

        if (data?.hrBreakUp?.avg != null && data?.hrBreakUp?.avg != 0) {
            binding.lytHeartRate.tvSubtitle2.text = "Average ${data?.hrBreakUp?.avg} bpm"
        } else {
            binding.lytHeartRate.tvSubtitle2.text = ""
        }


        val ssTime: String?
        val seTime: String?
        var breakUpData = ArrayList<Int>()
        var hasDummyData = false
        if (data?.hrBreakUp?.value.isNullOrEmpty()) {
            breakUpData = mViewModel.getDummyBreakUpDataForTimeDisplay()
            ssTime = null
            seTime = null
            hasDummyData = true
        } else {
            hasDummyData = false
            seTime = sleepEndTime
            ssTime = sleepStartTime
            breakUpData = data?.hrBreakUp?.value as ArrayList<Int>
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
        binding.lytHeartRate.lineChart.apply {

            setVibrationUtil(vibrationUtils)

            updateGraphColor(
                Color.parseColor("#ff7f96"),
                Color.parseColor("#844B60"),
                Color.parseColor("#99ff718b"),
                Color.parseColor("#00ff5f7c")
            )
            val lowValueIndex = updateDataWithMax(
                sleepChart, 5, false, false,
                GraphDummyModel(
                    hasDummyData, 40, 100
                ),
                data?.hrBreakUp?.avg,
                sleepStartTime,
                sleepEndTime,
                LineChartType.HEART_RATE
            )

            setInteractiveMode(!hasDummyData)

            setClickListener(object : OnLinearChartClickAction {
                override fun onValueSelected(value: Int, isInteracting: Boolean, time: String?) {
                    if (isInteracting) {
                        binding.lytHeartRate.tvSubtitle1.text = time ?: ""
                        binding.lytHeartRate.lytSubtitleValue1.tvValue.text =
                            if (value > 0) "$value" else "-"

                    } else {
                        setHrLowestHr()
                    }
                }

                override fun onTopClicked() {

                }

            })

        }


    }


    private fun showHeartRateVariabilityGraph(
        data: OreoReadinessModel?,
        sleepStartTime: String?,
        sleepEndTime: String?,
    ) {

        binding.lytHRVariability.tvTitle.text = getString(R.string.text_heart_rate_variability)

        mViewModel.avgHrv = data?.hrvBreakUp?.avg
        setHrvAvg()
        if (data?.hrvBreakUp?.max == null || data.hrvBreakUp.max == 0 || data.hrvBreakUp.max == 255) {
            binding.lytHRVariability.tvSubtitle2.text = ""
        } else {
            binding.lytHRVariability.tvSubtitle2.text = "Maximum ${data?.hrvBreakUp?.max} ms"
        }

        val ssTime: String?
        val seTime: String?
        var breakUpData = ArrayList<Int>()
        var hasDummyData = false
        if (data?.hrvBreakUp?.value.isNullOrEmpty()) {
            breakUpData = mViewModel.getDummyBreakUpDataForTimeDisplay()
            ssTime = null
            seTime = null
            hasDummyData = true
        } else {
            hasDummyData = false
            seTime = sleepEndTime
            ssTime = sleepStartTime
            breakUpData = data?.hrvBreakUp?.value as ArrayList<Int>
        }

        val baseTimeListNew =
            UtilClass.getXAxisPoints(ssTime, seTime, breakUpData.size ?: 288)

        binding.lytHRVariability.lineChart.visible()
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
        binding.lytHRVariability.lineChart.apply {
            setVibrationUtil(vibrationUtils)
            updateGraphColor(
                Color.parseColor("#ff7fd6"),
                Color.parseColor("#844A7E"),
                Color.parseColor("#99ff71d2"),
                Color.parseColor("#00ff5fcc")
            )
            val lowValueIndex = updateDataWithMax(
                sleepChart, 5, true, false,
                GraphDummyModel(
                    hasDummyData, 0, 200
                ),
                data?.hrvBreakUp?.avg,
                sleepStartTime,
                sleepEndTime,
                LineChartType.HRV
            )

            setInteractiveMode(!hasDummyData)

            setClickListener(object : OnLinearChartClickAction {
                override fun onValueSelected(
                    value: Int,
                    isInteracting: Boolean,
                    time: String?
                ) {
                    if (isInteracting) {
                        binding.lytHRVariability.tvSubtitle1.text = time ?: ""
                        binding.lytHRVariability.lytSubtitleValue1.tvValue.text =
                            if (value > 0) "$value" else "-"

                    } else {
                        setHrvAvg()
                    }
                }

                override fun onTopClicked() {

                }

            })

        }


    }


    private fun showTemperatureGraph(
        temperatureBreakUpData: UnitDataModelArrayFloat?,
        startTime: String,
        endTime: String
    ) {
        /*   var hasDummyData = true
           val breakUpData = if (temperatureBreakUp.isNullOrEmpty()) {
               mViewModel.getDummyBreakUpDataForTimeDisplay()
           } else{
               hasDummyData = false
               temperatureBreakUp

           }

           val baseHrList = UtilClass.graphBaseInterval(null, null, breakUpData.size ?: 0)
   */

        val ssTime: String?
        val seTime: String?
        var breakUpData = ArrayList<Float>()
        var hasDummyData = true
        if (temperatureBreakUpData?.value.isNullOrEmpty()) {
            breakUpData = mViewModel.getDummyBreakUpDataForTimeDisplayFloat()
            ssTime = null
            seTime = null
        } else {
            hasDummyData = false
            seTime = endTime
            ssTime = startTime
            breakUpData = temperatureBreakUpData?.value as ArrayList<Float>
        }

        /* val baseTimeList = UtilClass.graphTwoHoursInterval(
             ssTime,
             seTime,
             breakUpData.size ?: 288
         )*/
        val baseTimeListNew =
            UtilClass.getXAxisPoints(ssTime, seTime, breakUpData.size ?: 288)

        binding.lytTemperature.lineChart.visible()
        val chartList = ArrayList<ChartModel>()
        val sleepChart = SleepChartModel()
        breakUpData.forEachIndexed { index, it ->
            val chartModel = ChartModel()

            var value = it
            if (value.toInt() == 255) {
                value = 0F
            }

            chartModel.value = value.toInt()
            chartModel.index = baseTimeListNew[index]
            chartList.add(chartModel)
        }

        sleepChart.list = chartList

        binding.lytTemperature.lineChart.updateGraphColor(
            Color.parseColor("#ff9659"),
            Color.parseColor("#4cff6624"),
            Color.parseColor("#00ff6624")
        )


        binding.lytTemperature.lineChart.updateDataWithMax(
            sleepChart, 5,
            true, false, GraphDummyModel(
                hasDummyData, 80, 110
            ),
            null
        )

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

    private fun showCalendar() {
        setFragmentResultListener(SELECTED_DATE) { requestKey, bundle ->
            val selectedDate =
                bundle.getString("selected_date") ?: return@setFragmentResultListener

            uiController.logAppEvent(
                MoEngageLunaAppEvents.calender_day_selected,
                hashMapOf("source" to "readiness")
            )

            mainViewModel.onCalendarDateSelected(selectedDate)
            mainViewModel.getUserHealthData(mainViewModel.mStartDate, mainViewModel.mEndDate)
        }

        navigate(R.id.bottomSheetCalendar, Bundle().apply {
            this.putString("selectedDate", mainViewModel.selectedDate)
            this.putString("launchedFrom", "readiness")
        })
    }

    private fun setRecycler() {
        binding.rvTopGraph.setOnChartScrollChangedListener(this)


        binding.rvTopGraph.setOnDateClickListener { showCalendar() }

        //  mAdapter.setData(mViewModel.getDummyData())
        with(binding.lytRContributor.rvContributor) {
            adapter = mReadinessConAdapter
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

                mainViewModel.getUserHealthData(
                    mainViewModel.mStartDate,
                    mainViewModel.mEndDate
                )


            }
        }

    private fun handleComfortDietFoodClick(triple: Triple<Boolean, Boolean, Boolean>) {
        if(triple.third) {
            // Workout
            val workoutSetup = triple.first
            if (workoutSetup) {
                mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.home_lunaai_workout_plan)

                navigate(
                    R.id.workoutPlansFragment,
                    bundleOf(
                        "isComfortEnabled" to true
                    )
                )

            } else {
                if (mViewModel.ringDataStore.getRingDevice() == null) {
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
                mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.home_lunaai_nutrition_plan)

                navigate(
                    R.id.aiMealPlanFragment,
                    bundleOf(
                        "isComfortEnabled" to true
                    )
                )
            } else {
                if (mViewModel.ringDataStore.getRingDevice() == null) {
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

    override fun initListener() {

        binding.svMain.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (Math.abs(scrollY - oldScrollY) > 0) {
                binding.lytHeartRate.lineChart.resetIfInteracting()
                binding.lytHRVariability.lineChart.resetIfInteracting()
            }
        }


        binding.lytHeartRate.bInfo.setOnClickListener {
            mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_readiness_heart_rate_info_click)
            mViewModel.contributorInfo.value?.hr_graph?.let { content ->
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", content)
                })
            }
        }
        binding.lytHRVariability.bInfo.setOnClickListener {
            mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_readiness_hrv_info_click)
            mViewModel.contributorInfo.value?.hrv_graph?.let { content ->
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", content)
                })
            }
        }
        binding.lytTemperature.bInfo.setOnClickListener {
            mViewModel.contributorInfo.value?.temp_graph?.let { content ->
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", content)
                })
            }
        }


        binding.lytEmptyView.bGoToSettings.setOnClickListener {
            showWalkAround(false)
            mViewModel.ringDataStore.setReadinessWalkAroundShown(true)
            mViewModel.getReadinessDetailsData()
        }
        binding.lytToolbar.tvTitle.text = getString(R.string.text_readiness)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.visible()
        binding.lytToolbar.ivAddFriend.setImageResource(R.drawable.ic_calenders)
        binding.lytToolbar.backBtn.invisible()

        binding.lytToolbar.view1.setOnClickListener {
            mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_readiness_date_range_click)
            showCalendar()
        }

        binding.lytRScoreData.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.READINESS.name
            mSharedViewModel.itemClickType = ViewItemClickType.READINESS_SCORE
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "readiness")
                putString("infoData", mViewModel.contributorInfo.value?.readiness_score)
                putString("date", mainViewModel.selectedDate)
            })

            uiController.logAppEvent(
                MoEngageLunaAppEvents.score_clicked,
                hashMapOf("source" to "readiness")
            )

            mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_readiness_readiness_score_click)
        }
        binding.lytRScoreData.lytSec1.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.READINESS.name
            mSharedViewModel.itemClickType = ViewItemClickType.RESTING_HR
            /*navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "readiness")
                putString("infoData", mViewModel.contributorInfo.value?.resting_hr_top)
                putString("date", mainViewModel.selectedDate)
            })*/
            showInternalTrend(
                SleepInternalLaunchState.RESTING_HEART_RATE,
                mainViewModel.selectedDate ?: LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            )

            uiController.logAppEvent(
                MoEngageLunaAppEvents.readiness_trend_clicked,
                hashMapOf("readiness_trend" to "resting_heart_rate")
            )
        }
        binding.lytRScoreData.lytSec2.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.READINESS.name
            mSharedViewModel.itemClickType = ViewItemClickType.HR_VARIABILITY

            showInternalTrend(
                SleepInternalLaunchState.HRV,
                mainViewModel.selectedDate ?: LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            )

            uiController.logAppEvent(
                MoEngageLunaAppEvents.readiness_trend_clicked,
                hashMapOf("readiness_trend" to "hrv")
            )
        }

        binding.lytRScoreData.lytSec3.root.setOnClickListener {

            val baselineAvg = mViewModel.baseTemp
                ?: (mainViewModel.temperatureBaseLine ?: mainViewModel.DEFAULT_TEMPERATURE_BASELINE)

            /*mainViewModel.temperatureBaseLine ?: mainViewModel.DEFAULT_TEMPERATURE_BASELINE*/
            if (baselineAvg == mainViewModel.DEFAULT_TEMPERATURE_BASELINE) {
                mViewModel.contributorInfo.value?.temperature_readiness_top?.let { content ->
                    navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                        this.putString("infoData", content)
                        this.putString("type", ViewItemClickType.BODY_TEMPERATURE.name)
                    })
                }
            } else {
                mSharedViewModel.selectedTab = 0
                mSharedViewModel.itemType = ClickViewType.READINESS.name
                mSharedViewModel.itemClickType = ViewItemClickType.BODY_TEMPERATURE
                showInternalTrend(
                    SleepInternalLaunchState.SKIN_TEMPERATURE,
                    mainViewModel.selectedDate ?: LocalDate.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )

                uiController.logAppEvent(
                    MoEngageLunaAppEvents.readiness_trend_clicked,
                    hashMapOf("health_monitor_name" to "skin_temperature")
                )

                //mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_readiness_skin_temp_click)

            }
        }

        binding.lytRScoreData.lytSec4.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.READINESS.name
            mSharedViewModel.itemClickType = ViewItemClickType.RESPIRATORY_RATE
            showInternalTrend(
                SleepInternalLaunchState.RESPIRATORY_RATE,
                mainViewModel.selectedDate ?: LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            )
            uiController.logAppEvent(
                MoEngageLunaAppEvents.readiness_trend_clicked,
                hashMapOf("readiness_trend" to "respiratory_rate")
            )
        }

        binding.lytIrregularityEvents.btnClose.setOnClickListener {
            (activity as OreoMainActivity).hideSoftKeyboard()
            /*mainViewModel.localDataStore.setIrregularityCardsVisibilityReadiness(false)
            binding.lytIrregularityEvents.root.gone()*/
            mViewModel.updateAlert(true)
            mViewModel.loadAlertsData()
        }

        binding.lytIrregularityEvents.imgChatEtx.setOnClickListener {
            (activity as OreoMainActivity).hideSoftKeyboard()
            if (binding.lytIrregularityEvents.chatEtx.text.toString().isEmpty()) {
                return@setOnClickListener
            }

            mViewModel.submitIrregularityEvents(binding.lytIrregularityEvents.chatEtx.text.toString()) {
                mViewModel.isEventSubmitted = true
                mViewModel.updateAlert(true)
                mViewModel.loadAlertsData()
            }
        }

        binding.lytIrregularityEvents.btnSubmit.setOnClickListener {

            if (mViewModel.selectedChips.isEmpty()) {
                return@setOnClickListener
            }

            mViewModel.submitIrregularityEvents() {
                mViewModel.isEventSubmitted = true
                mViewModel.updateAlert(true)
                mViewModel.loadAlertsData()
            }
        }

        binding.lytIrregularityEvents.lytSubmittedIrregularityEvents.ivClose.setOnClickListener {
            (activity as OreoMainActivity).hideSoftKeyboard()
            mViewModel.isEventSubmitted = false
            mViewModel.loadAlertsData()
        }

        binding.lytWomenDayAnnouncement.lytWorkoutAnnc.root.setOnClickListener {
            if (mViewModel.ringDataStore.getRingDevice() == null) {
                context.showShortToast(getString(R.string.text_luna_ai_message))
                return@setOnClickListener
            }
            mViewModel.getComfortDietFoodData(true)
        }

        binding.lytWomenDayAnnouncement.lytDietAnnc.root.setOnClickListener {
            if (mViewModel.ringDataStore.getRingDevice() == null) {
                context.showShortToast(getString(R.string.text_luna_ai_message))
                return@setOnClickListener
            }
            mViewModel.getComfortDietFoodData(false)
        }

    }

    override fun subscribeObservers() {
        /* mainViewModel.sessionManager.syncCompleted.observe(this) {
             it?.getContent()?.let { syncDataStatus ->
                 when (syncDataStatus) {
                     SyncEvents.ServerSyncSuccess -> {
                         mainViewModel.reloadTodaysData()
                     }

                     else -> {}
                 }
             }
         }*/

        mainViewModel.nudgeReadinessData.observe(this){
            it.getContent()?.let {
                if(mainViewModel.selectedDate.equals(LocalDate.now().toString())) {
                    handleNudges()
                }
            }
        }

        mViewModel.planState.observe(this){
            it.getContent()?.let { triple ->
                handleComfortDietFoodClick(triple)
            }
        }

        mViewModel.hrvAlertsData.observe(this) {
            it.getContent()?.let {
                val hrvAlerts = mainViewModel.localDataStore.getHrvAlerts(false)
                if (hrvAlerts != null) {

                    val drop = hrvAlerts.data.spikePercent

                    binding.lytIrregularityEvents.tvDropPercent.text =
                        getString(R.string.text_your_hrv_dropped_by_last_night, drop)

                    setIrregularityEventsChips(
                        mViewModel.getIrregularityEventsChips()
                    )
                    binding.lytIrregularityEvents.root.visible()
                    binding.divider111.root.visible()
                } else {
                    if (mViewModel.isEventSubmitted) {
                        mViewModel.isEventSubmitted = false
                        binding.lytIrregularityEvents.irrEventsCard.gone()
                        binding.lytIrregularityEvents.lytSubmittedIrregularityEvents.root.visible()
                        binding.lytIrregularityEvents.root.visible()
                        binding.divider111.root.visible()
                    } else {
                        binding.lytIrregularityEvents.root.gone()
                        binding.divider111.root.gone()
                    }
                }
            }
        }

        mainViewModel.readinessHistoryResponse.observe(this) {

            if (it.isNullOrEmpty()) return@observe

            binding.svMain.visible()
            binding.groupHeader.visible()
            binding.lytToolbar.root.visible()

            val topGraphData = mViewModel.getPrefixAndSuffixList(it)
            //mSharedViewModel.selectedDate = mViewModel.dateList[mViewModel.dateList.size - 1]
            var moveToPos = -1

            LOGS.d("moveToPosition date initia ${mainViewModel.selectedDate}")

            if (mainViewModel.selectedDate != null) {
                val index = it?.indexOfFirst { data ->
                    data.date.equals(mainViewModel.selectedDate, true)
                }
                if (index != null) {

                    moveToPos = 15 + (it.size - index - 1)
                    LOGS.d("moveToPosition date ${mainViewModel.selectedDate}")
                    //binding.rvTopGraph.moveToPosition(15 + (15-index-1))
                }

            }

            binding.rvTopGraph.updateDataWithMax(
                topGraphData.first,
                topGraphData.third,
                topGraphData.second,
                moveToPos
            )
            setScrollDate()
            mViewModel.getContributorInfo()

            val returnDate =
                mainViewModel.updateSelectedDateReadiness(mainViewModel.selectedDate)
            if (returnDate != null) {
                mainViewModel.selectedDate = returnDate
            }


        }
        mainViewModel.dayReadinessData.observe(this) {
            updateUiRead(it)
        }


        mViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar1.root.visible()
            } else {
                binding.progressBar1.root.gone()
            }
        }
        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

    }

    fun setIrregularityEventsChips(category: List<IrregularEventsChipModel>?) {
        binding.lytIrregularityEvents.chipsPrograms.removeAllViews()

        if (category != null) {
            for (item in category) {
                val mChip: Chip =
                    layoutInflater.inflate(R.layout.item_chip_feedback, null, false) as Chip
                mChip.text = item.displayName
                mChip.tag = item.key

                // Create a ColorStateList programmatically
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),  // Checked state
                    intArrayOf(-android.R.attr.state_checked)   // Unchecked state
                )

                // Set your colors here (replace with your desired colors)
                val colors = intArrayOf(
                    "#7C404E".toColorInt(),  // Checked color
                    "#0affffff".toColorInt()   // Unchecked color
                )

                val colorStateList = ColorStateList(states, colors)

                // Apply the color state list to the chip
                mChip.chipBackgroundColor = colorStateList

                /*val paddingDp = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10F, resources.displayMetrics
                )
                mChip.setPadding(paddingDp.toInt(), 0, paddingDp.toInt(), 0)*/
                mChip.setOnCheckedChangeListener { compoundButton, isChecked ->
                    mViewModel.updateChipSelection(item.key, isChecked)
                    if (mChip.tag.toString().equals("others")) {
                        if (isChecked) {
                            binding.lytIrregularityEvents.chatEtx.visible()
                            binding.lytIrregularityEvents.imgChatEtx.visible()
                            binding.lytIrregularityEvents.btnSubmit.gone()
                            binding.lytIrregularityEvents.btnSubmitDisabled.gone()
                        } else {
                            binding.lytIrregularityEvents.chatEtx.gone()
                            binding.lytIrregularityEvents.imgChatEtx.gone()
                            binding.lytIrregularityEvents.btnSubmit.visible()
                        }
                    }
                    if (isChecked) {
                        LOGS.d("Checked Chips ${mChip.text}")
                        val name = MiscUtil.addUnderscore(mChip.text.toString())
//                        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_rateus_feedback + "_${name}_SELECT")
                    }

                    if(mViewModel.selectedChips.contains("others").not()){
                        if(mViewModel.selectedChips.isEmpty()){
                            binding.lytIrregularityEvents.btnSubmitDisabled.visible()
                            binding.lytIrregularityEvents.btnSubmit.gone()
                        }else{
                            binding.lytIrregularityEvents.btnSubmit.visible()
                            binding.lytIrregularityEvents.btnSubmitDisabled.gone()
                        }
                    }

                }
                binding.lytIrregularityEvents.chipsPrograms.addView(mChip)

            }
        }
    }

    private fun setHealthMonitor(healthTrend: HealthTrend?) {
        val hasHealthData = mViewModel.hasHealthData(healthTrend)

        healthTrend?.apply {

            binding.lytHealthMonitor.tvNudge.visible()
            binding.lytHealthMonitor.tvNudge.text = nudge

            if (!bloodOxy?.status.isNullOrEmpty()) {
                binding.lytHealthMonitor.imvSpo2.setImageResource(
                    mViewModel.getHealthTrendIcon(
                        bloodOxy?.status
                    )
                )
            } else {
                binding.lytHealthMonitor.imvSpo2.setImageResource(R.drawable.ic_hm_check_default)
            }

            if (!hrv?.status.isNullOrEmpty()) {
                binding.lytHealthMonitor.imvHrv.setImageResource(mViewModel.getHealthTrendIcon(hrv?.status))
            } else {
                binding.lytHealthMonitor.imvHrv.setImageResource(R.drawable.ic_hm_check_default)
            }

            if (!rhr?.status.isNullOrEmpty()) {
                binding.lytHealthMonitor.imvRHR.setImageResource(mViewModel.getHealthTrendIcon(rhr?.status))
            } else {
                binding.lytHealthMonitor.imvRHR.setImageResource(R.drawable.ic_hm_check_default)
            }

            if (!skinTemp?.status.isNullOrEmpty()) {
                binding.lytHealthMonitor.imvSkin.setImageResource(
                    mViewModel.getHealthTrendIcon(
                        skinTemp?.status
                    )
                )
            } else {
                binding.lytHealthMonitor.imvSkin.setImageResource(R.drawable.ic_hm_check_default)
            }

            if (!resp?.status.isNullOrEmpty()) {
                binding.lytHealthMonitor.imvResp.setImageResource(mViewModel.getHealthTrendIcon(resp?.status))
            } else {
                binding.lytHealthMonitor.imvResp.setImageResource(R.drawable.ic_hm_check_default)
            }
        }

        if (hasHealthData.not()) {
            binding.lytHealthMonitor.apply {
                imvResp.setImageResource(R.drawable.ic_hm_check_default)
                imvRHR.setImageResource(R.drawable.ic_hm_check_default)
                imvSpo2.setImageResource(R.drawable.ic_hm_check_default)
                imvHrv.setImageResource(R.drawable.ic_hm_check_default)
                imvSkin.setImageResource(R.drawable.ic_hm_check_default)
                tvNudge.visible()
                tvNudge.text = getString(R.string.text_no_data_so_far)
            }
        }

        binding.lytHealthMonitor.root.setOnClickListener {
            navigate(R.id.healthMonitorInternal, Bundle().apply {
                this.putParcelable("healthTrend", healthTrend)
                this.putString("selectedDate", mainViewModel.selectedDate)
                this.putString("source", "readiness")
            })
        }
    }

    private fun setReadinessScore(readinessData: CommonDataModel) {
        binding.lytRScoreData.lytScore.tvValue.text =
            readinessData.value.toString()
        if (readinessData.text != null) {
            val statusColor = ContextCompat.getColor(
                binding.lytRScoreData.lytScore.tvValue.context,
                mViewModel.getStatusColors(readinessData.status)
            )
            binding.lytRScoreData.lytScore.tvQuality.setTextColor(statusColor)
            binding.lytRScoreData.lytScore.tvQuality.text = readinessData.text
            binding.lytRScoreData.lytScore.tvQuality.visible()
        } else {
            binding.lytRScoreData.lytScore.tvQuality.gone()
        }
        mViewModel.sessionManager.logMoEngageAppEvent(
            MoEngageLunaAppEvents.luna_readiness_page_visit,
            HashMap<String, Any>().apply {
                this[MoEngageAppEventParams.status] = readinessData.status ?: ""
            })
    }

    private fun updateUiRead(it: OreoReadinessModel) {
        binding.lytRScoreData.lytScore.tvTitle.text = getString(R.string.text_readiness_score)
        binding.lytRScoreData.lytSec1.tvTitle.text = getString(R.string.text_resting_heart_rate)
        binding.lytRScoreData.lytSec2.tvTitle.text = getString(R.string.text_hrv)
        binding.lytRScoreData.lytSec3.tvTitle.text = getString(R.string.text_skin_temperature)
        binding.lytRScoreData.lytSec4.tvTitle.text = getString(R.string.text_respiratory_rate)

//        setReadinessBannerViewPager(it.nudges)

        setHealthMonitor(it.healthTrend)

        //readiness score
        if (it.readinessScore != null) {
            binding.lytRScoreData.lytScore.emptyText.gone()
            binding.lytRScoreData.lytScore.tvValue.visible()
            val readinessData = it.readinessScore
            if (readinessData.value != null) {
                if (readinessData.value == 0) {
                    if (it.totalSleep?.value != 0) {
                        setReadinessScore(readinessData)
                    } else {
                        binding.lytRScoreData.lytScore.tvValue.text = "-"
                        binding.lytRScoreData.lytScore.tvQuality.gone()
                    }
                } else {
                    setReadinessScore(readinessData)
                }
            } else {
                binding.lytRScoreData.lytScore.tvValue.text = "-"
                binding.lytRScoreData.lytScore.tvQuality.gone()

            }

        } else {
            if (mViewModel.ringDataStore.getRegisterDay() == 0) {
                binding.lytRScoreData.lytScore.emptyText.text =
                    getString(R.string.text_you_will_see_your_readiness_score_after_first_sleep_analysis)
                binding.lytRScoreData.lytScore.emptyText.visible()
                binding.lytRScoreData.lytScore.tvValue.gone()
                binding.lytRScoreData.lytScore.tvQuality.gone()
            } else {
                binding.lytRScoreData.lytScore.emptyText.gone()
                binding.lytRScoreData.lytScore.tvValue.text = "-"
                binding.lytRScoreData.lytScore.tvQuality.gone()
            }
        }
        //resting HR
        if (it.restingHr != null) {
            val rHrData = it.restingHr
            if (rHrData.value == 0 || rHrData.value == 255) {
                restHrDefaultView()
            } else {
                binding.lytRScoreData.lytSec1.lytBpmView.tvValue.text =
                    rHrData.value.toString()
                binding.lytRScoreData.lytSec1.lytHrMn.root.gone()
                binding.lytRScoreData.lytSec1.tvPercentValue.gone()
                binding.lytRScoreData.lytSec1.lytBpmView.root.visible()
                binding.lytRScoreData.lytSec1.lytBpmView.tvUnit.text = "bpm"
                binding.lytRScoreData.lytSec1.lytBpmView.tvUnit.visible()
            }

        } else {
            restHrDefaultView()
        }
        //hrv
        if (it.hrv != null) {
            val hrvData = it.hrv
            if (hrvData.value == 0 || hrvData.value == 255) {
                hrVariabilityDefaultView()
            } else {
                binding.lytRScoreData.lytSec2.lytHrMn.root.gone()
                binding.lytRScoreData.lytSec2.tvPercentValue.gone()
                binding.lytRScoreData.lytSec2.lytBpmView.root.visible()

                binding.lytRScoreData.lytSec2.lytBpmView.tvValue.text =
                    hrvData.value.toString()
                binding.lytRScoreData.lytSec2.lytBpmView.tvUnit.text = "ms"
                binding.lytRScoreData.lytSec2.lytBpmView.tvUnit.visible()
            }
        } else {
            hrVariabilityDefaultView()
        }
        //temperature
        if (it.avg_temp?.value != null && it.avg_temp.value != 0f) {
            binding.lytRScoreData.lytSec3.lytHrMn.root.gone()
            binding.lytRScoreData.lytSec3.tvPercentValue.visible()
            binding.lytRScoreData.lytSec3.lytBpmView.root.gone()

            mViewModel.baseTemp = it.base_temp

            val baselineAvg = it.base_temp
                ?: (mainViewModel.temperatureBaseLine ?: mainViewModel.DEFAULT_TEMPERATURE_BASELINE)
            if (baselineAvg != mainViewModel.DEFAULT_TEMPERATURE_BASELINE) {

                val deviation = it.avg_temp.value - baselineAvg
                val deviationString = StringBuilder().apply {
                    if (deviation > 0) {
                        this.append("+")
                    }
                    this.append(
                        String.format(
                            locale = Locale.US, "%.2f", if (mViewModel.sessionManager.isMetric()) {
                                AppConversionUtils.fahrenheitToCelsius(32 + deviation)
                            } else {
                                deviation
                            }
                        )
                    )
                }

                binding.lytRScoreData.lytSec3.tvPercentValue.text =
                    if (mViewModel.sessionManager.isMetric()) {
                        String.format(
                            locale = Locale.US,
                            "%.1f°C (%s)",
                            AppConversionUtils.fahrenheitToCelsius(it.avg_temp.value),
                            deviationString
                        )
                    } else {
                        String.format(
                            locale = Locale.US,
                            "%.1f°F (%s)",
                            it.avg_temp.value,
                            deviationString
                        )
                    }
            } else {
                binding.lytRScoreData.lytSec3.tvPercentValue.text =
                    if (mViewModel.sessionManager.isMetric()) {
                        String.format(
                            locale = Locale.US,
                            "%.1f°C",
                            AppConversionUtils.fahrenheitToCelsius(it.avg_temp.value)
                        )

                    } else {
                        String.format(locale = Locale.US, "%.1f°F", it.avg_temp.value)
                    }

                //binding.lytRScoreData.lytSec3.tvPercentValue.text = "-"
            }


        } else {
            temperatureDefaultView()

            /*if ((it.temperature?.value ?: 0) != 0) {
                val baselineAvg = it.base_temp
                    ?: (mainViewModel.temperatureBaseLine
                        ?: mainViewModel.DEFAULT_TEMPERATURE_BASELINE)

                binding.lytRScoreData.lytSec3.lytHrMn.root.gone()
                binding.lytRScoreData.lytSec3.tvPercentValue.visible()
                binding.lytRScoreData.lytSec3.lytBpmView.root.gone()
                if (baselineAvg != mainViewModel.DEFAULT_TEMPERATURE_BASELINE) {

                    val todayAvg = it.temperature?.value ?: baselineAvg
                    val deviation = todayAvg - baselineAvg

                    binding.lytRScoreData.lytSec3.tvPercentValue.text =
                        String.format(locale = Locale.US, "%.1f°F (%.2f)", todayAvg, deviation)

                } else {
                    binding.lytRScoreData.lytSec3.tvPercentValue.text = "-"
                }

            } else {
                temperatureDefaultView()
            }*/
        }

        //respiration
        if (it.respiration != null) {
            val resData = it.respiration
            if (resData.value == 0 || resData.value == 255) {
                respiratoryRateDefaultView()
            } else {
                binding.lytRScoreData.lytSec4.lytHrMn.root.gone()
                binding.lytRScoreData.lytSec4.tvPercentValue.gone()
                binding.lytRScoreData.lytSec4.lytBpmView.root.visible()

                binding.lytRScoreData.lytSec4.lytBpmView.tvValue.text =
                    resData.value.toString()
                binding.lytRScoreData.lytSec4.lytBpmView.tvUnit.text = "/ min"
                binding.lytRScoreData.lytSec4.lytBpmView.tvUnit.visible()
            }
        } else {
            respiratoryRateDefaultView()
        }

        //readiness contributor
        binding.lytRContributor.tvTitle.text = getString(R.string.text_readiness_contributor)
        val contriVersion = it.contriVersion ?: 2
        mReadinessConAdapter.setData(
            mViewModel.getContributorsData(it, contriVersion),
            contriVersion
        )


        val sleepStartTime = it.start_time
        val sleepEndTime = it.end_time

        //showHeartRateGraph(it, sleepStartTime, sleepEndTime)
        //showHeartRateVariabilityGraph(it, sleepStartTime, sleepEndTime)


        //set data on temperature
        /* binding.lytTemperature.tvTitle.text = getString(R.string.text_temperature)
         binding.lytTemperature.tvSubtitle1.text = getString(R.string.text_max)
         binding.lytTemperature.tvSubtitle2.gone()
         binding.lytTemperature.divider1.root.invisible()
         if (!it.temperatureBreakUp?.value.isNullOrEmpty()) {
             binding.lytTemperature.lytSubtitleValue1.tvValue.text = "${it.temperatureBreakUp?.max}"
             binding.lytTemperature.lytSubtitleValue1.tvUnit.visible()
             binding.lytTemperature.lytSubtitleValue1.tvUnit.text = "°F"
         } else {
             temperatureGraphDefaultView()
         }
         //todo will change startTime, endTime
         showTemperatureGraph(
             it.temperatureBreakUp,
             sleepStartTime,
             sleepEndTime
         )*/
        if (it.date.equals(LocalDate.now().toString())) {
            handleNudges()
            mViewModel.loadAlertsData()
            if(mViewModel.getLdwReadinessData()){
                displayWomansDayCard()
            }else{
                binding.dividerWomenDayAnnouncement.root.gone()
                binding.lytWomenDayAnnouncement.root.gone()
            }
        } else {
            binding.lytRScoreData.lytAScoreBanner.root.gone()
            binding.lytIrregularityEvents.root.gone()
            binding.divider111.root.gone()
            binding.dividerWomenDayAnnouncement.root.gone()
            binding.lytWomenDayAnnouncement.root.gone()
        }
    }


    private fun temperatureGraphDefaultView() {
        binding.lytTemperature.lytSubtitleValue1.tvUnit.gone()
        binding.lytTemperature.lytSubtitleValue1.tvValue.text = "-"
    }


    private fun setScrollDate() {
    }

    private fun respiratoryRateDefaultView() {
        binding.lytRScoreData.lytSec4.lytHrMn.root.gone()
        binding.lytRScoreData.lytSec4.tvPercentValue.gone()
        binding.lytRScoreData.lytSec4.lytBpmView.root.visible()

        binding.lytRScoreData.lytSec4.lytBpmView.tvValue.text = "-"
        binding.lytRScoreData.lytSec4.lytBpmView.tvUnit.gone()
    }

    private fun temperatureDefaultView() {
        binding.lytRScoreData.lytSec3.lytHrMn.root.gone()
        binding.lytRScoreData.lytSec3.tvPercentValue.visible()
        binding.lytRScoreData.lytSec3.lytBpmView.root.gone()

        binding.lytRScoreData.lytSec3.tvPercentValue.text = "-"
    }

    private fun hrVariabilityDefaultView() {
        binding.lytRScoreData.lytSec2.lytHrMn.root.gone()
        binding.lytRScoreData.lytSec2.tvPercentValue.gone()
        binding.lytRScoreData.lytSec2.lytBpmView.root.visible()

        binding.lytRScoreData.lytSec2.lytBpmView.tvValue.text = "-"
        binding.lytRScoreData.lytSec2.lytBpmView.tvUnit.gone()
    }

    private fun restHrDefaultView() {
        binding.lytRScoreData.lytSec1.lytHrMn.root.gone()
        binding.lytRScoreData.lytSec1.tvPercentValue.gone()
        binding.lytRScoreData.lytSec1.lytBpmView.root.visible()
        binding.lytRScoreData.lytSec1.lytBpmView.tvValue.text = "-"
        binding.lytRScoreData.lytSec1.lytBpmView.tvUnit.gone()
    }


    override fun onPositionSelected(position: Int, chartModel: ChartModel?) {

        if (mainViewModel.selectedDate == chartModel?.date!!) {
            return
        }
        //mSharedViewModel.selectedDate = chartModel.date!!
        LOGS.w("moveToPosition onPositionSelected ${chartModel.date}")
        uiController.logAppEvent(
            MoEngageLunaAppEvents.day_selected,
            hashMapOf("source" to "readiness")
        )
        mainViewModel.selectedDate = chartModel.date!!
        val returnDate = mainViewModel.updateSelectedDateReadiness(mainViewModel.selectedDate)
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