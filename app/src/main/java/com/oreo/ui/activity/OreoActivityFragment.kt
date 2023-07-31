package com.oreo.ui.activity


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
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOreoActivityBinding
import com.noisefit.ui.dashboard.graphs.HistoryCalendarActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.CandleChartModel
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributors
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.ui.custom.ScrollListener
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.sleep.scoredetails.ClickViewType
import com.oreo.ui.sleep.scoredetails.SharedOSCDViewModel
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OreoActivityFragment :
    BaseFragment<FragmentOreoActivityBinding>(FragmentOreoActivityBinding::inflate),
    ScrollListener {


    private val mViewModel: OreoActivityViewModel by viewModels()
    private val mSharedViewModel: SharedOSCDViewModel by activityViewModels()
    var currentItem: Int = 0

    private val mWorkoutAdapter: OreoAWorkoutAdapter by lazy {
        OreoAWorkoutAdapter(object : OreoAWorkoutAdapter.OnItemClickListener {
            override fun onItemClick(data: OActivityListModal) {
                moveToDetailsScreen(data)
            }

        })
    }

    private fun moveToDetailsScreen(data: OActivityListModal) {
        navigate(R.id.oWorkoutDetailsFragment, Bundle().apply {
            putString("workoutName", data.getFormattedActivityName())
            putString("workoutId", data.id)
        })
    }

    private val mActivityAdapter: OreoAContributorAdapter by lazy {
        OreoAContributorAdapter(object : OreoAContributorAdapter.ContributorItemClickListener {
            override fun onItemClick(resultData: ArrayList<Contributors>, position: Int) {
                if (resultData[position].barPercent > 0) {
                    openContributorBottomSheet(resultData, position)
                }
            }
        })
    }

    private fun openContributorBottomSheet(resultData: ArrayList<Contributors>, position: Int) {
        val desListData = mViewModel.prepareDataForDescriptionArray(resultData)
        navigate(
            OreoActivityFragmentDirections.actionNavigationActivityDetailsFragToDescriptionPopUpBottomDialogFragment(
                position, desListData.toTypedArray(), resultData[position].title
            )
        )

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        mViewModel.getActivityDetailsData()

    }

    private fun setSleepBannerViewPager(data: List<Nudges>?) {
        if (data.isNullOrEmpty()) {
            binding.lytAScoreData.lytAScoreBanner.root.gone()
            return
        } else {
            binding.lytAScoreData.lytAScoreBanner.root.visible()
        }
        val fragments = ArrayList<OreoActivityBannerFragment>()
        data.forEach {
            fragments.add(OreoActivityBannerFragment.newInstance(it))
        }
        val winsAdapter =
            OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)
        binding.lytAScoreData.lytAScoreBanner.vpBannerSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })
            adapter = winsAdapter
        }

        TabLayoutMediator(
            binding.lytAScoreData.lytAScoreBanner.tabLayout,
            binding.lytAScoreData.lytAScoreBanner.vpBannerSlider
        ) { _, _ -> }.attach()

        if (fragments.size > 0) {
            binding.lytAScoreData.lytAScoreBanner.tabLayout.visible()
        } else {
            binding.lytAScoreData.lytAScoreBanner.tabLayout.invisible()
        }
    }

    private fun updateUi(it: OreoActivityModel) {
        //activity score data
        binding.lytAScoreData.lytSec1.tvTitle.text = getString(R.string.text_active_calorie)
        binding.lytAScoreData.lytSec2.tvTitle.text = getString(R.string.text_total_calories)
        binding.lytAScoreData.lytSec3.tvTitle.text = getString(R.string.text_steps)
        binding.lytAScoreData.lytSec4.tvTitle.text = getString(R.string.text_distance)
        binding.lytAScoreData.lytScore.tvTitle.text = getString(R.string.text_activity_score)

        setSleepBannerViewPager(it.nudges)

        val scoreData = it.activityScore
        if (scoreData != null) {
            if (scoreData.value != null) {
                binding.lytAScoreData.lytScore.tvValue.text = scoreData.value.toString()
                if (scoreData.level != null) {
                    val statusColor = ContextCompat.getColor(
                        binding.lytAScoreData.lytScore.tvValue.context,
                        mViewModel.getStatusColors(scoreData.status)
                    )
                    binding.lytAScoreData.lytScore.tvQuality.setTextColor(statusColor)

                    binding.lytAScoreData.lytScore.tvQuality.text = scoreData.level
                    binding.lytAScoreData.lytScore.tvQuality.visible()
                } else {
                    binding.lytAScoreData.lytScore.tvQuality.gone()
                }
            } else {
                binding.lytAScoreData.lytScore.tvValue.text = "-"
                binding.lytAScoreData.lytScore.tvQuality.gone()
            }

            if (it.activeCalories != null) {
                binding.lytAScoreData.lytSec1.lytHrMn.root.gone()
                binding.lytAScoreData.lytSec1.tvPercentValue.gone()
                binding.lytAScoreData.lytSec1.lytBpmView.root.visible()
                binding.lytAScoreData.lytSec1.lytBpmView.tvUnit.visible()

                val user = mViewModel.localDataStore.getUser()
                val actCalories = "${it.activeCalories}/${user?.userGoals?.caloriesGoal}"
                binding.lytAScoreData.lytSec1.lytBpmView.tvValue.text = actCalories
                binding.lytAScoreData.lytSec1.lytBpmView.tvUnit.text = "kcal"
            } else {
                goalProgressDefaultView()
            }



            if (it.totalCalories != null) {
                binding.lytAScoreData.lytSec2.lytHrMn.root.gone()
                binding.lytAScoreData.lytSec2.tvPercentValue.gone()
                binding.lytAScoreData.lytSec2.lytBpmView.root.visible()
                binding.lytAScoreData.lytSec2.lytBpmView.tvUnit.visible()

                binding.lytAScoreData.lytSec2.lytBpmView.tvValue.text =
                    it.totalCalories.toString()
                binding.lytAScoreData.lytSec2.lytBpmView.tvUnit.text = "kcal"
            } else {
                totalBurnDefaultView()
            }

            if (it.steps != null) {
                binding.lytAScoreData.lytSec3.lytHrMn.root.gone()
                binding.lytAScoreData.lytSec3.tvPercentValue.visible()
                binding.lytAScoreData.lytSec3.lytBpmView.root.gone()
                binding.lytAScoreData.lytSec3.tvPercentValue.text = it.steps.toString()
            } else {
                stepCountDefaultView()
            }

            if (it.distance != null) {
                binding.lytAScoreData.lytSec4.lytHrMn.root.gone()
                binding.lytAScoreData.lytSec4.tvPercentValue.gone()
                binding.lytAScoreData.lytSec4.lytBpmView.root.visible()
                binding.lytAScoreData.lytSec4.lytBpmView.tvUnit.visible()
                binding.lytAScoreData.lytSec4.lytBpmView.tvValue.text =
                    DistanceUtil.convertMeterToKm(it.distance)
                binding.lytAScoreData.lytSec4.lytBpmView.tvUnit.text = "km"

            } else {
                distanceDefaultView()
            }
        } else {
            binding.lytAScoreData.lytScore.tvValue.text = "-"
            binding.lytAScoreData.lytScore.tvQuality.gone()
            goalProgressDefaultView()
            totalBurnDefaultView()
            stepCountDefaultView()
            distanceDefaultView()
        }

        //handle contributors data
        binding.lytAContributor.tvTitle.text = getString(R.string.text_activity_contributors)
        mActivityAdapter.setData(mViewModel.getContributorsData(it) as ArrayList<Contributors>)


        //handle daily movement views
        handleMovementViews(it)


        it.workout?.let { it1 -> updateWorkoutUI(it1) }

    }

    private fun returnMovementProgress(highMovValue: Int): Pair<Int, String> {
        val valueInSec = highMovValue.times(60)
        val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(valueInSec)
        val leftText = "$hour hr $minute min"
        val progress = valueInSec.toFloat().times(100).div(100).toInt()
        return Pair(progress, leftText)

    }

    private fun getHour(index: Int): String {

        return when (index) {
            0 -> {
                "12 am"
            }

            47 -> {
                "4 am"
            }

            95 -> {
                "8 am"
            }

            143 -> {
                "12 pm"
            }

            191 -> {
                "4 pm"
            }

            239 -> {
                "8 pm"
            }

            287 -> {
                "12 am     "
            }

            else -> {
                ""
            }

        }

    }

    private fun handleMovementViews(it: OreoActivityModel) {


        val candleChartModelList: MutableList<CandleChartModel> =
            java.util.ArrayList<CandleChartModel>()

//        for (i in 0..287) {
//
//            val chartModel = CandleChartModel()
//            chartModel.bottomLineText = getHour(i)
//            LOGS.d("dsadssadhjjksadhjkdsahkLOW ${chartModel.bottomLineText}")
//            chartModel.index = (i.toString())
//            if (i % 5 == 0) {
//                LOGS.d("dsadssadhjjksadhjkdsahk LOW ${binding.lytDailyMovement.candleChart.getMax()}")
//                chartModel.setLength((binding.lytDailyMovement.candleChart.getMax() * 0.2).toInt())
//                chartModel.setColor(Color.parseColor("#4cffd230"))
//                chartModel.setType(CandleChartModel.Type.LOW)
//            } else if (i % 4 == 0) {
//                LOGS.d("dsadssadhjjksadhjkdsahk MEDIUM ${binding.lytDailyMovement.candleChart.getMax() * 0.3}")
//                chartModel.setLength((binding.lytDailyMovement.candleChart.getMax() * 0.3).toInt())
//                chartModel.setColor(Color.parseColor("#ffd230"))
//                chartModel.setType(CandleChartModel.Type.MEDIUM)
//            } else if (i % 3 == 0) {
//                LOGS.d("dsadssadhjjksadhjkdsahk HIGH ${binding.lytDailyMovement.candleChart.getMax() * 0.4}")
//                chartModel.setLength((binding.lytDailyMovement.candleChart.getMax() * 0.4).toInt())
//                chartModel.setColor(Color.parseColor("#ffffff"))
//                chartModel.setMarkText("2")
//                chartModel.setType(CandleChartModel.Type.HIGH)
//            } else {
//                LOGS.d("dsadssadhjjksadhjkdsahk INACTIVE ${binding.lytDailyMovement.candleChart.getMax() * 0.1}")
//                chartModel.setLength((binding.lytDailyMovement.candleChart.getMax() * 0.1).toInt())
//                chartModel.setColor(Color.parseColor("#4c4c4c"))
//                chartModel.setType(CandleChartModel.Type.INACTIVE)
//            }
//            //            chartModel.setLength((int) (candleChart.getMax() * 0.4));
//            candleChartModelList.add(chartModel)
//        }


        val defaultInterval = 5
        binding.lytDailyMovement.lytDMHigh.tvTitle.text = getString(R.string.text_high_movement)
        var highProgress = 1
        var highRemark = ""
        var medProgress = 1
        var medRemark = ""
        var lowProgress = 1
        var lowRemark = ""
        var inactiveProgress = 1
        var inactiveRemark = ""

        if (it.daytimeMovement?.movement != null) {
            var highMovValue: Int = 0
            var medMovValue: Int = 0
            var lowMovValue: Int = 0
            var inactiveMovValue: Int = 0
            val movementList = it.daytimeMovement.movement


            //"startTime": "2023-07-25 07:34:00",
            //"endTime": "2023-07-25 23:59:59",
//            LOGS.d("dsadssadhjjksadhjkdsahk  ${movementList.size}")
//            val startDate = DateFormats.getDateFromTimeStamp(it.daytimeMovement.startTime)
//            val endDate = DateFormats.getDateFromTimeStamp(it.daytimeMovement.endTime)
//
//            val day1Minutes =
//                DateFormats.getDayElapsedMinutesFromTimeStamp(startTimeStamp)
//            val day2Minutes = DateFormats.getDayElapsedMinutesFromTimeStamp(endTimeStamp)
//
//            val day1MinutesCeil = 5 * (floor(abs(day1Minutes.toDouble() / 5)))
//            val day2MinutesCeil = 5 * (ceil(abs(day2Minutes.toDouble() / 5)))

            if (movementList.isNotEmpty()) {
                movementList.forEachIndexed { index, data ->

                    val chartModel = CandleChartModel()

                    chartModel.bottomLineText = getHour(index)
                    when (data) {
                        1 -> {
                            lowMovValue++
                            chartModel.length =
                                (binding.lytDailyMovement.candleChart.max * 0.4).toInt()
                            chartModel.color = Color.parseColor("#4cffd230")
                            chartModel.type = CandleChartModel.Type.LOW

                        }

                        2 -> {
                            medMovValue++
                            chartModel.length =
                                (binding.lytDailyMovement.candleChart.max * 0.6).toInt()
                            chartModel.color = Color.parseColor("#ffd230")
                            chartModel.type = CandleChartModel.Type.MEDIUM
                        }

                        3, 4 -> {
                            highMovValue++
                            chartModel.length =
                                (binding.lytDailyMovement.candleChart.max * 0.8).toInt()
                            chartModel.color = Color.parseColor("#ffffff")

                            chartModel.type = CandleChartModel.Type.HIGH
                        }

                        else -> {
                            chartModel.length =
                                (binding.lytDailyMovement.candleChart.max * 0.2).toInt()
                            chartModel.color = Color.parseColor("#4c4c4c")
                            chartModel.type = CandleChartModel.Type.INACTIVE
                            inactiveMovValue++
                        }
                    }
                    chartModel.value = data
                    candleChartModelList.add(chartModel)
                }

                candleChartModelList.forEach {
                    if (it.bottomLineText.isNotEmpty()) {
                        LOGS.d("dsadssadhjjksadhjkdsahk  ${it.bottomLineText}")
                    }

                }


                highProgress = returnMovementProgress(highMovValue * defaultInterval).first
                highRemark = returnMovementProgress(highMovValue * defaultInterval).second

                medProgress = returnMovementProgress(medMovValue * defaultInterval).first
                medRemark = returnMovementProgress(medMovValue * defaultInterval).second

                lowProgress = returnMovementProgress(lowMovValue * defaultInterval).first
                lowRemark = returnMovementProgress(lowMovValue * defaultInterval).second

                inactiveProgress = returnMovementProgress(inactiveMovValue * defaultInterval).first
                inactiveRemark = returnMovementProgress(inactiveMovValue * defaultInterval).second

                binding.lytDailyMovement.candleChart.updateData(candleChartModelList)
            }
        }


        binding.lytDailyMovement.lytDMHigh.pbSteps.progress = highProgress
        binding.lytDailyMovement.lytDMHigh.tvRemark.text = highRemark
        binding.lytDailyMovement.lytDMHigh.pbSteps.setIndicatorColor(
            ContextCompat.getColor(requireContext(), R.color.low_movement)
        )
        binding.lytDailyMovement.lytDMMed.tvTitle.text = getString(R.string.text_medium_movement)
        binding.lytDailyMovement.lytDMMed.pbSteps.progress = medProgress
        binding.lytDailyMovement.lytDMMed.tvRemark.text = medRemark
        binding.lytDailyMovement.lytDMMed.pbSteps.setIndicatorColor(
            ContextCompat.getColor(requireContext(), R.color.medium_movement)
        )
        binding.lytDailyMovement.lytDMLow.tvTitle.text = getString(R.string.text_low_movement)
        binding.lytDailyMovement.lytDMLow.pbSteps.progress = lowProgress
        binding.lytDailyMovement.lytDMLow.tvRemark.text = lowRemark
        binding.lytDailyMovement.lytDMLow.pbSteps.setIndicatorColor(
            ContextCompat.getColor(requireContext(), R.color.low_movement)
        )
        binding.lytDailyMovement.lytDMInactive.tvTitle.text = getString(R.string.text_inactive)
        binding.lytDailyMovement.lytDMInactive.pbSteps.progress = inactiveProgress
        binding.lytDailyMovement.lytDMInactive.tvRemark.text = inactiveRemark
        binding.lytDailyMovement.lytDMInactive.pbSteps.setIndicatorColor(
            ContextCompat.getColor(requireContext(), R.color.inactive_movement)
        )
    }


    private fun distanceDefaultView() {
        binding.lytAScoreData.lytSec4.lytHrMn.root.gone()
        binding.lytAScoreData.lytSec4.tvPercentValue.gone()
        binding.lytAScoreData.lytSec4.lytBpmView.root.visible()
        binding.lytAScoreData.lytSec4.lytBpmView.tvValue.text = "-"
        binding.lytAScoreData.lytSec4.lytBpmView.tvUnit.gone()
    }

    private fun stepCountDefaultView() {
        binding.lytAScoreData.lytSec3.lytHrMn.root.gone()
        binding.lytAScoreData.lytSec3.tvPercentValue.visible()
        binding.lytAScoreData.lytSec3.lytBpmView.root.gone()
        binding.lytAScoreData.lytSec3.tvPercentValue.text = "-"
    }

    private fun totalBurnDefaultView() {
        binding.lytAScoreData.lytSec2.lytHrMn.root.gone()
        binding.lytAScoreData.lytSec2.tvPercentValue.gone()
        binding.lytAScoreData.lytSec2.lytBpmView.root.visible()

        binding.lytAScoreData.lytSec2.lytBpmView.tvValue.text = "-"
        binding.lytAScoreData.lytSec2.lytBpmView.tvUnit.gone()
    }

    private fun goalProgressDefaultView() {
        binding.lytAScoreData.lytSec1.lytHrMn.root.gone()
        binding.lytAScoreData.lytSec1.tvPercentValue.gone()
        binding.lytAScoreData.lytSec1.lytBpmView.root.visible()
        binding.lytAScoreData.lytSec1.lytBpmView.tvValue.text = "-"
        binding.lytAScoreData.lytSec1.lytBpmView.tvUnit.gone()
    }

    private fun setRecyclerView() {
        binding.rvTopGraph.setOnChartScrollChangedListener(this)

        with(binding.lytAContributor.rvContributor) {
            adapter = mActivityAdapter
        }
        with(binding.lytWorkouts.rvWorkouts) {
            adapter = mWorkoutAdapter
        }


    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val selectedDate = data?.getStringExtra("selected_date")
                LOGS.d("Selected Date  :${selectedDate}")
                mViewModel.getActivityDetailsData(selectedDate)
                if (selectedDate != null) {
                    mViewModel.updateSelectedDate(selectedDate)
                }

            }
        }


    private fun updateWorkoutUI(recentWorkout: List<OActivityListModal>) {
        val itemCount = recentWorkout?.size
        if ((itemCount ?: 0) > 0) {
            mWorkoutAdapter.setData(recentWorkout)
            binding.lytWorkouts.rvWorkouts.visible()
            binding.lytWorkouts.tvEmptyMsg.gone()
        } else {
            binding.lytWorkouts.rvWorkouts.gone()
            binding.lytWorkouts.tvEmptyMsg.visible()
            if (mSharedViewModel.selectedDate == DateFormats.getCurrentDateOreoFormat()) {
                binding.lytWorkouts.viewAddWorkout.visible()
                binding.lytWorkouts.tvAddWorkout.visible()
                binding.lytWorkouts.tvEmptyMsg.text =
                    getString(R.string.text_you_haven_t_added_any_workouts_for_today)

            } else {
                binding.lytWorkouts.viewAddWorkout.gone()
                binding.lytWorkouts.tvAddWorkout.gone()
                binding.lytWorkouts.tvEmptyMsg.text =
                    getString(R.string.text_you_haven_t_added_any_workouts_for_this_day)
            }

        }
    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_workout_page_title)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.visible()
        binding.lytToolbar.ivAddFriend.setImageResource(R.drawable.ic_calenders)
        binding.lytToolbar.backBtn.invisible()

        binding.lytToolbar.view1.setOnClickListener {
            resultLauncher.launch(
                HistoryCalendarActivity.getStartIntent(
                    requireContext(),
                    mSharedViewModel.selectedDate,
                    "ring"
                )
            )
        }

        binding.lytWorkouts.viewAddWorkout.setOnClickListener {
            navigate(R.id.addWorkoutFragment)
        }

        binding.lytWorkouts.ivViewAll.setOnClickListener {
            navigate(R.id.oActivityListFragment)
        }

        binding.lytAScoreData.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.ACTIVITY.name
            mSharedViewModel.itemClickType = ViewItemClickType.ACTIVITY_SCORE.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "activity")
            })
        }
        binding.lytAScoreData.lytSec1.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.ACTIVITY.name
            mSharedViewModel.itemClickType = ViewItemClickType.ACTIVE_CALORIES.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "activity")
            })
        }
        binding.lytAScoreData.lytSec2.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.ACTIVITY.name
            mSharedViewModel.itemClickType = ViewItemClickType.TOTAL_CALORIES_BURNED.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "activity")
            })
        }
        binding.lytAScoreData.lytSec3.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.ACTIVITY.name
            mSharedViewModel.itemClickType = ViewItemClickType.STEPS.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "activity")
            })
        }
        binding.lytAScoreData.lytSec4.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.ACTIVITY.name
            mSharedViewModel.itemClickType = ViewItemClickType.DISTANCE.name
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "activity")
            })
        }


    }

    override fun subscribeObservers() {

        mViewModel.activityHistoryResponse.observe(this) {
            binding.svMain.visible()
            val topGraphData = mViewModel.getPrefixAndSuffixList(it)
            mSharedViewModel.selectedDate = mViewModel.dateList[mViewModel.dateList.size - 1]
            binding.rvTopGraph.updateDataWithMax(
                topGraphData.first,
                topGraphData.third,
                topGraphData.second
            )
            mViewModel.getContributorInfo()
        }

        mViewModel.dayActivityData.observe(this) {

            updateUi(it)
        }

        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
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
    }


    override fun onPositionSelected(position: Int, chartModel: ChartModel?) {

        if (mSharedViewModel.selectedDate == chartModel?.date!!) {
            return
        }
        mSharedViewModel.selectedDate = chartModel.date!!
        chartModel.date?.let { mViewModel.updateSelectedDate(it) }
    }

    override fun onScrolling(position: Int, chartModel: ChartModel?) {

    }


}