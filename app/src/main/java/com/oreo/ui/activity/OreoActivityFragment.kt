package com.oreo.ui.activity


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOreoActivityBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.dashboard.graphs.HistoryCalendarActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.FirebaseLunaAppEvents
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributors
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.health.ActivityScore
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
    private val mainViewModel: OreoMainViewModel by activityViewModels()


    private val mWorkoutAdapter: OreoAWorkoutAdapter by lazy {
        OreoAWorkoutAdapter(object : OreoAWorkoutAdapter.OnItemClickListener {
            override fun onItemClick(data: OActivityListModal, position: Int) {
                moveToDetailsScreen(data, position)
            }

        })
    }

    private val mDayMovementAdapter: DayMovementsAdapter by lazy {
        DayMovementsAdapter()
    }

    private fun moveToDetailsScreen(data: OActivityListModal, position: Int) {
        navigate(R.id.oWorkoutDetailsFragment, Bundle().apply {
            putString("workoutName", data.getFormattedActivityName())
            putString("workoutId", data.id)
            putInt("position", position)
        })
    }

    private val mActivityAdapter: OreoAContributorAdapter by lazy {
        OreoAContributorAdapter(object : OreoAContributorAdapter.ContributorItemClickListener {
            override fun onItemClick(resultData: ArrayList<Contributors>, position: Int) {
//                if (resultData[position].barPercent > 0) {
                openContributorBottomSheet(resultData, position)
//                }
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
        mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_READINESS_PAGE_VISIT)
        setRecyclerView()


        if (mViewModel.ringDataStore.isActivityWalkAroundShown()) {
            mViewModel.getActivityDetailsData()
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
        val winsAdapter = OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)
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

    private fun setActivityScore(scoreData: ActivityScore) {
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
    }

    private fun updateUi(it: OreoActivityModel) {
        //activity score data
        binding.lytAScoreData.lytSec1.tvTitle.text = getString(R.string.text_goal_progress)
        binding.lytAScoreData.lytSec2.tvTitle.text = getString(R.string.text_total_calories)
        binding.lytAScoreData.lytSec3.tvTitle.text = getString(R.string.text_steps)
        binding.lytAScoreData.lytSec4.tvTitle.text = getString(R.string.text_distance)
        binding.lytAScoreData.lytScore.tvTitle.text = getString(R.string.text_activity_score)
        setSleepBannerViewPager(it.nudges)
        val scoreData = it.activityScore
        if (scoreData != null) {
            binding.lytAScoreData.lytScore.emptyText.gone()
            binding.lytAScoreData.lytScore.tvValue.visible()
            if (scoreData.value != null) {
                if (scoreData.value == 0) {
                    if (it.activeCalories != 0) {
                        setActivityScore(scoreData)
                    } else {
                        binding.lytAScoreData.lytScore.tvValue.text = "-"
                        binding.lytAScoreData.lytScore.tvQuality.gone()
                    }
                } else {
                    setActivityScore(scoreData)
                }
            } else {
                binding.lytAScoreData.lytScore.tvValue.text = "-"
                binding.lytAScoreData.lytScore.tvQuality.gone()

            }

            if (it.activeCalories != null) {
                if (it.activeCalories == 0) {
                    goalProgressDefaultView()
                } else {
                    binding.lytAScoreData.lytSec1.lytHrMn.root.gone()
                    binding.lytAScoreData.lytSec1.tvPercentValue.gone()
                    binding.lytAScoreData.lytSec1.lytBpmView.root.visible()
                    binding.lytAScoreData.lytSec1.lytBpmView.tvUnit.visible()

                    val user = mViewModel.localDataStore.getUser()
                    val actCalories = "${it.activeCalories}/${user?.userGoals?.caloriesGoal}"
                    binding.lytAScoreData.lytSec1.lytBpmView.tvValue.text = actCalories
                    binding.lytAScoreData.lytSec1.lytBpmView.tvUnit.text = "kcal"
                }
            } else {
                goalProgressDefaultView()
            }



            if (it.totalCalories != null) {
                if (it.totalCalories == 0) {
                    totalBurnDefaultView()
                } else {
                    binding.lytAScoreData.lytSec2.lytHrMn.root.gone()
                    binding.lytAScoreData.lytSec2.tvPercentValue.gone()
                    binding.lytAScoreData.lytSec2.lytBpmView.root.visible()
                    binding.lytAScoreData.lytSec2.lytBpmView.tvUnit.visible()

                    binding.lytAScoreData.lytSec2.lytBpmView.tvValue.text =
                        it.totalCalories.toString()
                    binding.lytAScoreData.lytSec2.lytBpmView.tvUnit.text = "kcal"
                }
            } else {
                totalBurnDefaultView()
            }

            if (it.steps != null) {
                if (it.steps == 0) {
                    stepCountDefaultView()
                } else {
                    binding.lytAScoreData.lytSec3.lytHrMn.root.gone()
                    binding.lytAScoreData.lytSec3.tvPercentValue.visible()
                    binding.lytAScoreData.lytSec3.lytBpmView.root.gone()
                    binding.lytAScoreData.lytSec3.tvPercentValue.text = it.steps.toString()
                }
            } else {
                stepCountDefaultView()
            }

            if (it.distance != null) {
                if (it.distance == 0) {
                    distanceDefaultView()
                } else {
                    binding.lytAScoreData.lytSec4.lytHrMn.root.gone()
                    binding.lytAScoreData.lytSec4.tvPercentValue.gone()
                    binding.lytAScoreData.lytSec4.lytBpmView.root.visible()
                    binding.lytAScoreData.lytSec4.lytBpmView.tvUnit.visible()
                    binding.lytAScoreData.lytSec4.lytBpmView.tvValue.text =
                        DistanceUtil.convertMeterToKm(it.distance)
                    binding.lytAScoreData.lytSec4.lytBpmView.tvUnit.text = "km"
                }

            } else {
                distanceDefaultView()
            }
        } else {
            if (mViewModel.ringDataStore.getRegisterDay() == 0) {
                binding.lytAScoreData.lytScore.emptyText.text =
                    getString(R.string.text_you_will_see_your_activity_score_after_wearing_the_ring)
                binding.lytAScoreData.lytScore.emptyText.visible()
                binding.lytAScoreData.lytScore.tvValue.gone()
                binding.lytAScoreData.lytScore.tvQuality.gone()
            } else {
                binding.lytAScoreData.lytScore.emptyText.gone()
                binding.lytAScoreData.lytScore.tvValue.text = "-"
                binding.lytAScoreData.lytScore.tvQuality.gone()
            }

            goalProgressDefaultView()
            totalBurnDefaultView()
            stepCountDefaultView()
            distanceDefaultView()
        }

        //handle contributors data
        binding.lytAContributor.tvTitle.text = getString(R.string.text_activity_contributors)
        mActivityAdapter.setData(mViewModel.getContributorsData(it) as ArrayList<Contributors>)

        //handle daily movement views
//        handleMovementViews(it)
        handleMovementNewViews(it)
        updateWorkoutUI(it.workout)
    }


    private fun returnMovementProgress(value: Int, total: Int): Pair<Int, String> {
        val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(value.times(15))
        val leftText: String = if (hour > 0)
            if (minute > 0)
                "$hour h $minute min"
            else
                "$hour h"
        else if (minute > 0) {
            "$minute min"
        } else {
            "-"
        }
        var progress = (value.toFloat() / total).times(100).toInt()

        if (progress == 0) {
            progress = 0
        }

        return Pair(progress, leftText)

    }

    private fun handleMovementNewViews(it: OreoActivityModel) {
        val movementList = it.daytimeMovement?.movement

        val newList = mViewModel.getCombinedMovementData(movementList, false)
        val newListInvalid = mViewModel.getCombinedMovementData(movementList, true)

        var highMovValue = 0
        var medMovValue = 0
        var lowMovValue = 0
        var inactiveMovValue = 0
        if (movementList?.isNotEmpty() == true) {
            newListInvalid.forEachIndexed { index, data ->
                when (data) {
                    0 -> {
                        inactiveMovValue++
                    }

                    1 -> {
                        lowMovValue++
                    }

                    2 -> {
                        medMovValue++
                    }

                    3 -> {
                        highMovValue++
                    }

                    else -> {}
                }
            }
        }

        binding.lytDailyMovement.movementChart.setData(newList)

        //mDayMovementAdapter.setData(newList)


        val totalValue = highMovValue + medMovValue + lowMovValue + inactiveMovValue

        val (highProgress, highRemark) = returnMovementProgress(highMovValue, totalValue)
        val (medProgress, medRemark) = returnMovementProgress(medMovValue, totalValue)
        val (lowProgress, lowRemark) = returnMovementProgress(lowMovValue, totalValue)
        val (inactiveProgress, inactiveRemark) = returnMovementProgress(
            inactiveMovValue, totalValue
        )

        //for high value
        binding.lytDailyMovement.lytDMHigh.view1.layoutParams =
            binding.lytDailyMovement.lytDMHigh.view1.layoutParams.apply {
                (this as LinearLayout.LayoutParams).weight =
                    calculateWeightPercent(highProgress)
            }
        binding.lytDailyMovement.lytDMHigh.lytChildContainer.layoutParams =
            binding.lytDailyMovement.lytDMHigh.lytChildContainer.layoutParams.apply {

                (this as LinearLayout.LayoutParams).weight =
                    100 - calculateWeightPercent(highProgress)
            }

        binding.lytDailyMovement.lytDMHigh.view1.setBackgroundResource(R.drawable.high_bar_with_round_edge)
        binding.lytDailyMovement.lytDMHigh.tvStageName.text = getString(R.string.text_high)
        binding.lytDailyMovement.lytDMHigh.tvDuration.text = highRemark
        if (calculateWeightPercent(highProgress) > 0)
            binding.lytDailyMovement.lytDMHigh.view1.visible()
        else
            binding.lytDailyMovement.lytDMHigh.view1.gone()

        //for med value
        binding.lytDailyMovement.lytDMMed.view1.layoutParams =
            binding.lytDailyMovement.lytDMMed.view1.layoutParams.apply {
                (this as LinearLayout.LayoutParams).weight =
                    calculateWeightPercent(medProgress)
            }
        binding.lytDailyMovement.lytDMMed.lytChildContainer.layoutParams =
            binding.lytDailyMovement.lytDMMed.lytChildContainer.layoutParams.apply {

                (this as LinearLayout.LayoutParams).weight =
                    100 - calculateWeightPercent(medProgress)
            }

        binding.lytDailyMovement.lytDMMed.view1.setBackgroundResource(R.drawable.med_bar_with_round_edge)
        binding.lytDailyMovement.lytDMMed.tvStageName.text = getString(R.string.text_medium)
        binding.lytDailyMovement.lytDMMed.tvDuration.text = medRemark
        if (calculateWeightPercent(medProgress) > 0)
            binding.lytDailyMovement.lytDMMed.view1.visible()
        else
            binding.lytDailyMovement.lytDMMed.view1.gone()


        //for low value
        binding.lytDailyMovement.lytDMLow.view1.layoutParams =
            binding.lytDailyMovement.lytDMLow.view1.layoutParams.apply {
                (this as LinearLayout.LayoutParams).weight =
                    calculateWeightPercent(lowProgress)
            }
        binding.lytDailyMovement.lytDMLow.lytChildContainer.layoutParams =
            binding.lytDailyMovement.lytDMLow.lytChildContainer.layoutParams.apply {

                (this as LinearLayout.LayoutParams).weight =
                    100 - calculateWeightPercent(lowProgress)
            }

        binding.lytDailyMovement.lytDMLow.view1.setBackgroundResource(R.drawable.low_bar_with_round_edge)

        binding.lytDailyMovement.lytDMLow.tvStageName.text = getString(R.string.text_low)
        binding.lytDailyMovement.lytDMLow.tvDuration.text = lowRemark
        if (calculateWeightPercent(lowProgress) > 0)
            binding.lytDailyMovement.lytDMLow.view1.visible()
        else
            binding.lytDailyMovement.lytDMLow.view1.gone()


        //for inactive value
        /* binding.lytDailyMovement.lytDMInactive.view1.layoutParams =
             binding.lytDailyMovement.lytDMInactive.view1.layoutParams.apply {
                 (this as LinearLayout.LayoutParams).weight =
                     calculateWeightPercent(inactiveProgress)
             }
         binding.lytDailyMovement.lytDMInactive.lytChildContainer.layoutParams =
             binding.lytDailyMovement.lytDMInactive.lytChildContainer.layoutParams.apply {

                 (this as LinearLayout.LayoutParams).weight =
                     100 - calculateWeightPercent(inactiveProgress)
             }

         binding.lytDailyMovement.lytDMInactive.view1.setBackgroundResource(R.drawable.inactive_bar_with_round_edge)
         binding.lytDailyMovement.lytDMInactive.tvStageName.text = getString(R.string.text_inactive)
         binding.lytDailyMovement.lytDMInactive.tvDuration.text = inactiveRemark
         if (calculateWeightPercent(inactiveProgress) > 0)
             binding.lytDailyMovement.lytDMInactive.view1.visible()
         else
             binding.lytDailyMovement.lytDMInactive.view1.gone()*/


    }

    private fun calculateWeightPercent(progress: Int): Float {
        return (progress.toFloat() / 100).times(42)
    }

    private fun handleMovementViews(it: OreoActivityModel) {
//        binding.lytDailyMovement.lytDMHigh.tvTitle.text = getString(R.string.text_high_movement)

        val movementList = it.daytimeMovement?.movement

        val newList = mViewModel.getCombinedMovementData(movementList, false)
        val newListInvalid = mViewModel.getCombinedMovementData(movementList, true)

        var highMovValue = 0
        var medMovValue = 0
        var lowMovValue = 0
        var inactiveMovValue = 0
        if (movementList?.isNotEmpty() == true) {
            newListInvalid.forEachIndexed { index, data ->
                when (data) {
                    0 -> {
                        inactiveMovValue++
                    }

                    1 -> {
                        lowMovValue++
                    }

                    2 -> {
                        medMovValue++
                    }

                    3 -> {
                        highMovValue++
                    }

                    else -> {}
                }
            }
        }

        binding.lytDailyMovement.movementChart.setData(newList)

        //mDayMovementAdapter.setData(newList)


        val totalValue = highMovValue + medMovValue + lowMovValue + inactiveMovValue

        val (highProgress, highRemark) = returnMovementProgress(highMovValue, totalValue)
        val (medProgress, medRemark) = returnMovementProgress(medMovValue, totalValue)
        val (lowProgress, lowRemark) = returnMovementProgress(lowMovValue, totalValue)
        val (inactiveProgress, inactiveRemark) = returnMovementProgress(
            inactiveMovValue, totalValue
        )


        /*binding.lytDailyMovement.lytDMHigh.pbSteps.progress = highProgress
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
        )*/
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

        /* binding.lytDailyMovement.rvMovements.apply {
             layoutManager =
                 LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
             addItemDecoration(OverlapDecoration(dpToPx(-29, this.context).toInt()))
             adapter = mDayMovementAdapter
         }*/


    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val selectedDate = data?.getStringExtra("selected_date")?: return@registerForActivityResult

                mainViewModel.onCalendarDateSelected(selectedDate)

                LOGS.d("moveToPosition Selected Date  :${selectedDate}")

                mainViewModel.getUserHealthData(mainViewModel.mStartDate, mainViewModel.mEndDate)


            }
        }


    private fun updateWorkoutUI(recentWorkout: List<OActivityListModal>?) {
        val itemCount = recentWorkout?.size
        if ((itemCount ?: 0) > 0) {
            mWorkoutAdapter.setData(recentWorkout ?: ArrayList())
            binding.lytWorkouts.rvWorkouts.visible()
            binding.lytWorkouts.tvEmptyMsg.gone()
            binding.lytWorkouts.ivViewAll.visible()
        } else {
            binding.lytWorkouts.ivViewAll.invisible()
            binding.lytWorkouts.rvWorkouts.gone()
            binding.lytWorkouts.tvEmptyMsg.visible()
        }

        if (mainViewModel.selectedDate == DateFormats.getCurrentDateOreoFormat()) {
            if (mViewModel.ringDataStore.getRingDevice() != null) {
                binding.lytWorkouts.viewAddWorkout.visible()
            } else {
                binding.lytWorkouts.viewAddWorkout.gone()
            }
            binding.lytWorkouts.tvEmptyMsg.text =
                getString(R.string.text_you_haven_t_added_any_workouts_for_today)

        } else {
            binding.lytWorkouts.viewAddWorkout.gone()
            binding.lytWorkouts.tvEmptyMsg.text =
                getString(R.string.text_you_haven_t_added_any_workouts_for_this_day)
        }
    }

    override fun initListener() {

        binding.lytDailyMovement.bInfo.setOnClickListener {
            mViewModel.contributorInfo.value?.daytime_movement?.let {
                navigate(R.id.bottomSheetDataMetrics, Bundle().apply {
                    this.putString("infoData", it)
                })
            }
        }


        binding.lytEmptyView.bGoToSettings.setOnClickListener {
            mViewModel.ringDataStore.setActivityWalkAroundShown(true)
            showWalkAround(false)
            mViewModel.getActivityDetailsData()
        }
        binding.lytToolbar.tvTitle.text = getString(R.string.text_workout_page_title)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.visible()
        binding.lytToolbar.ivAddFriend.setImageResource(R.drawable.ic_calenders)
        binding.lytToolbar.backBtn.invisible()

        binding.lytToolbar.view1.setOnClickListener {
            resultLauncher.launch(
                HistoryCalendarActivity.getStartIntent(
                    requireContext(),
                    /*viewModel.sleepHistoryResponse.value?.lastOrNull()?.date
                        ?:*/ mainViewModel.selectedDate,
                    "ring"
                )
            )
        }

        binding.lytWorkouts.viewAddWorkout.setOnClickListener {
            mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_ACTIVITY_ADD_WORKOUT_CLICK)
            navigate(R.id.addWorkoutFragment)
        }

        binding.lytWorkouts.ivViewAll.setOnClickListener {
            navigate(R.id.oActivityListFragment)
        }

        binding.lytAScoreData.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.ACTIVITY.name
            mSharedViewModel.itemClickType = ViewItemClickType.ACTIVITY_SCORE
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "activity")
                putString("infoData", mViewModel.contributorInfo.value?.activity_score)
                putString("date", mainViewModel.selectedDate)
            })
            mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_ACTIVITY_ACTIVITY_SCORE_CLICK)
        }
        binding.lytAScoreData.lytSec1.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.ACTIVITY.name
            mSharedViewModel.itemClickType = ViewItemClickType.ACTIVE_CALORIES
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "activity")
                putString("infoData", mViewModel.contributorInfo.value?.active_calories)
                putString("date", mainViewModel.selectedDate)
            })
            mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_ACTIVITY_GOAL_PROGRESS_CLICK)
        }
        binding.lytAScoreData.lytSec2.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.ACTIVITY.name
            mSharedViewModel.itemClickType = ViewItemClickType.TOTAL_CALORIES_BURNED
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "activity")
                putString("infoData", mViewModel.contributorInfo.value?.total_calories)
                putString("date", mainViewModel.selectedDate)
            })
            mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_ACTIVITY_TOTAL_CALORIES_CLICK)
        }
        binding.lytAScoreData.lytSec3.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.ACTIVITY.name
            mSharedViewModel.itemClickType = ViewItemClickType.STEPS
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "activity")
                putString("infoData", mViewModel.contributorInfo.value?.total_steps)
                putString("date", mainViewModel.selectedDate)
            })
            mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_ACTIVITY_ACTIVITY_SCORE_CLICK)
        }
        binding.lytAScoreData.lytSec4.root.setOnClickListener {
            mSharedViewModel.selectedTab = 0
            mSharedViewModel.itemType = ClickViewType.ACTIVITY.name
            mSharedViewModel.itemClickType = ViewItemClickType.DISTANCE
            navigate(R.id.sleepDetailsParentOreo, Bundle().apply {
                putString("viewType", "activity")
                putString("infoData", mViewModel.contributorInfo.value?.total_distance)
                putString("date", mainViewModel.selectedDate)
            })
            mViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_ACTIVITY_DISTANCE_CLICK)
        }


    }

    override fun subscribeObservers() {

        mainViewModel.activityHistoryResponse.observe(this) {
            if(it.isNullOrEmpty()) return@observe

            binding.svMain.visible()
            binding.groupHeader.visible()
            //binding.lytToolbar.root.visible()
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
                topGraphData.first, topGraphData.third, topGraphData.second, moveToPos
            )
            mViewModel.getContributorInfo()

            val returnDate = mainViewModel.updateSelectedDateActivity(mainViewModel.selectedDate)
            if (returnDate != null) {
                mainViewModel.selectedDate = returnDate
            }
        }

        mainViewModel.dayActivityData.observe(this) {

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
        if (mainViewModel.selectedDate == chartModel?.date!!) {
            return
        }
        //mSharedViewModel.selectedDate = chartModel.date!!
        LOGS.w("moveToPosition onPositionSelected ${chartModel.date}")
        mainViewModel.selectedDate = chartModel.date!!
        val returnDate = mainViewModel.updateSelectedDateActivity(mainViewModel.selectedDate)
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