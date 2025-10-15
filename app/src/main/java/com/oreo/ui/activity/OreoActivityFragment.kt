package com.oreo.ui.activity


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOreoActivityBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.common.px
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributor
import com.oreo.data.model.Contributors
import com.oreo.data.model.DayTimeDataModel
import com.oreo.data.model.OActivityListModal
import com.oreo.data.model.health.ActivityScore
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.OreoActivityModel
import com.oreo.ui.calendar.SELECTED_DATE
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.custom.OnDayTimeClickAction
import com.oreo.ui.custom.ScrollListener
import com.oreo.ui.readiness.NudgeBannerListener
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.sleep.scoredetails.ClickViewType
import com.oreo.ui.sleep.scoredetails.SharedOSCDViewModel
import com.oreo.ui.sleep.scoredetails.ViewItemClickType
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject


@AndroidEntryPoint
class OreoActivityFragment :
    BaseFragment<FragmentOreoActivityBinding>(FragmentOreoActivityBinding::inflate),
    ScrollListener {


    private val mViewModel: OreoActivityViewModel by viewModels()
    private val mSharedViewModel: SharedOSCDViewModel by activityViewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    @Inject
    lateinit var vibrationUtils: VibrationUtils


    private val mWorkoutAdapter: OreoAWorkoutAdapter by lazy {
        OreoAWorkoutAdapter(object : OreoAWorkoutAdapter.OnItemClickListener {
            override fun onItemClick(data: OActivityListModal, position: Int) {
                mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_activity_workouts_item_click)
                moveToDetailsScreen(data, position)
            }

        })
    }

    private val mDayMovementAdapter: DayMovementsAdapter by lazy {
        DayMovementsAdapter()
    }

    private fun moveToDetailsScreen(data: OActivityListModal, position: Int) {

       /* uiController.logAppEvent(
            MoEngageLunaAppEvents.workout_selected,
            hashMapOf("action" to "view_all", "source" to "activity")
        )*/

        if (data.getDisplayVersionType() == 2) {
            navigate(R.id.oWorkoutDetailsFragmentV2, Bundle().apply {
                putString("workoutId", data.id ?: "")
                putInt("position", position)
            })
        } else {
            navigate(R.id.oWorkoutDetailsFragment, Bundle().apply {
                putString("workoutId", data.id ?: "")
                putString("workoutName", data.getTranslatedActivityName())
                putInt("position", position)
            })
        }
    }

    private val mActivityAdapter: OreoAContributorAdapter by lazy {
        OreoAContributorAdapter(object : OreoAContributorAdapter.ContributorItemClickListener {
            override fun onItemClick(resultData: ArrayList<Contributors>, position: Int) {
//                if (resultData[position].barPercent > 0) {
                openContributorBottomSheet(resultData, position)
                handleEvent(resultData[position].contriType)
                //                }
            }
        })
    }

    private fun handleEvent(contriType: Contributor) {
        var contriName: String? = null

        contriName = when (contriType) {
            Contributor.SLEEP_SCORE -> null
            Contributor.ACTIVITY_SCORE -> null
            Contributor.RECOVERY_INDEX -> null
            Contributor.SLEEP_REGULARITY -> null
            Contributor.SLEEP_BALANCE -> null
            Contributor.AVERAGE_HR -> null
            Contributor.ACTIVITY_BALANCE -> null
            Contributor.HRV_BALANCE -> null
            Contributor.SKIN_TEMP -> null
            Contributor.SLEEP_DURATION -> null
            Contributor.STAY_ACTIVE -> "stay_active"
            Contributor.MOVE_EVERY_HOUR -> "move_every_hour"
            Contributor.CALORIE_GOAL -> "calorie_goal"
            Contributor.TRAINING_FREQUENCY -> "training_frequency"
            Contributor.TRAINING_VOLUME -> "training_volume"
        }

        if (contriName != null) {
            uiController.logAppEvent(
                MoEngageLunaAppEvents.activity_analysis_clicked,
                hashMapOf("contributor" to contriName, "source" to "activity")
            )
        }


    }

    private fun openContributorBottomSheet(resultData: ArrayList<Contributors>, position: Int) {
        val desListData = mViewModel.prepareDataForDescriptionArray(resultData)
        navigate(
            OreoActivityFragmentDirections.actionNavigationActivityDetailsFragToDescriptionPopUpBottomDialogFragment(
                position,
                desListData.toTypedArray(),
                resultData[position].title,
                ClickViewType.ACTIVITY.name
            )
        )

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()


        if (mViewModel.ringDataStore.isActivityWalkAroundShown()) {
            mViewModel.getActivityDetailsData()
        } else {
            showWalkAround(true)
            mainViewModel.addWorkoutCtaVisibility.postValue(false)
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

    private fun handleNudges(){
        val data = mainViewModel.localDataStore.getNudgeActivityData()
        val list = ArrayList<Nudges>()
        data?.cue1?.let { list.add(Nudges(
            it.title ?: "",
            it.description ?: ""
        )) }
        data?.cue2?.let { list.add(Nudges(
            it.title ?: "",
            it.description ?: ""
        )) }
        setSleepBannerViewPager(list)
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
            fragments.add(OreoActivityBannerFragment.newInstance(it).apply {
                setClickListener(
                    object : NudgeBannerListener {
                        override fun onAiClicked() {
                            if (mainViewModel.ringDataStore.getRingDevice() == null) {
                                context.showShortToast(getString(R.string.text_luna_ai_message))
                                return
                            }

                            uiController.logAppEvent(
                                MoEngageLunaAppEvents.aichat_initiated_clicked,
                                hashMapOf("source" to "activity")
                            )

                            navigate(
                                R.id.aiTopQuestionsFragment,
                                bundleOf("aiTopic" to AITopics.ACTIVITY)
                            )
                        }
                    }
                )
            })
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

        if (fragments.size > 1) {
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
        mViewModel.sessionManager.logMoEngageAppEvent(
            MoEngageLunaAppEvents.luna_activity_page_visit,
            HashMap<String, Any>().apply {
                this[MoEngageAppEventParams.status] = scoreData.status ?: ""
            })
    }

    private fun updateUi(it: OreoActivityModel) {
        //activity score data
        binding.lytAScoreData.lytSec1.tvTitle.text = getString(R.string.text_goal_progress)
        binding.lytAScoreData.lytSec2.tvTitle.text = getString(R.string.text_total_calories)
        binding.lytAScoreData.lytSec3.tvTitle.text = getString(R.string.text_steps)
        binding.lytAScoreData.lytSec4.tvTitle.text = getString(R.string.text_distance)
        binding.lytAScoreData.lytScore.tvTitle.text = getString(R.string.text_activity_score)
        if (it.date.equals(LocalDate.now().toString())) {
            handleNudges()
        }else{
            binding.lytAScoreData.lytAScoreBanner.root.gone()
        }
//        setSleepBannerViewPager(it.nudges)
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
                    if (mViewModel.sessionManager.isMetric()) {
                        binding.lytAScoreData.lytSec4.lytBpmView.tvValue.text =
                            DistanceUtil.convertMeterToKm(it.distance)
                        binding.lytAScoreData.lytSec4.lytBpmView.tvUnit.text = "km"
                    } else {
                        binding.lytAScoreData.lytSec4.lytBpmView.tvValue.text =
                            DistanceUtil.convertMeterToMiles(it.distance)
                        binding.lytAScoreData.lytSec4.lytBpmView.tvUnit.text = "mi"
                    }
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
        mViewModel.contriData = mViewModel.getContributorsData(it) as ArrayList<Contributors>
        mActivityAdapter.setData(mViewModel.contriData ?: ArrayList())

        //handle daily movement views
//        handleMovementViews(it)
        handleMovementNewViews(it)
        updateWorkoutUI(it.workout)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.shouldResetMasterDates()
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
            if (total == 0) {
                "-"
            } else {
                "0 min"
            }
        }
        var progress = (value.toFloat() / total).times(100).toInt()

        if (progress == 0) {
            progress = 0
        }

        return Pair(progress, leftText)

    }


    private fun handleMovementNewViews(it: OreoActivityModel) {
        var dayTimeDataModel: DayTimeDataModel? = null
        val dayData = mainViewModel.getDayMovementData(mainViewModel.selectedDate)
        if (dayData != null) {
            mViewModel.prepareStressActivityData(dayData)
        }
        binding.lytDailyMovement.lytInteractiveGraph.graphDayTime.enableInteractiveMode(true)
        binding.lytDailyMovement.lytInteractiveGraph.graphDayTime.setVibrationUtil(vibrationUtils)
        dayData?.let {
            dayTimeDataModel = mViewModel.dayTimeDataConvertor.getDayTimeCombinedData(
                it
            )

            binding.lytDailyMovement.lytInteractiveGraph.graphDayTime.updateData(
                dayTimeDataModel
            )

            var marginTop = 0.px()
            if (dayTimeDataModel?.sections.isNullOrEmpty()) {
                marginTop = (-8).px()
            }
            binding.lytDailyMovement.lytInteractiveGraph.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                this.topMargin = dpToPx(
                    marginTop.toInt(),
                    binding.lytDailyMovement.lytInteractiveGraph.root.context
                ).toInt()
            }
        } ?: run {
            var marginTop = 0.px()
            binding.lytDailyMovement.lytInteractiveGraph.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                this.topMargin = dpToPx(
                    marginTop.toInt(),
                    binding.lytDailyMovement.lytInteractiveGraph.root.context
                ).toInt()
            }
        }


        val movementList = it.daytimeMovement?.movement
        val newListInvalid = mViewModel.getCombinedMovementData(movementList, true)
        var highMovValue = 0
        var medMovValue = 0
        var lowMovValue = 0
        var inactiveMovValue = 0
        newListInvalid.forEachIndexed { index, data ->
            var uData = data
            val updatedDayTimeData = dayTimeDataModel?.items?.getOrNull(index)
            if (updatedDayTimeData?.value != 255) {
                uData = updatedDayTimeData?.value ?: data
            }
            when (uData) {
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


        val totalValue = highMovValue + medMovValue + lowMovValue + inactiveMovValue

        val (highProgress, highRemark) = returnMovementProgress(highMovValue, totalValue)
        val (medProgress, medRemark) = returnMovementProgress(medMovValue, totalValue)
        val (lowProgress, lowRemark) = returnMovementProgress(lowMovValue, totalValue)
//        val (inactiveProgress, inactiveRemark) = returnMovementProgress(
//            inactiveMovValue, totalValue
//        )
        mViewModel.activeMinutes = (lowMovValue + medMovValue + highMovValue) * 15

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

        resetDayTimeTopLevelUi()

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


    private fun showCalendar() {
        setFragmentResultListener(SELECTED_DATE) { requestKey, bundle ->
            val selectedDate =
                bundle.getString("selected_date") ?: return@setFragmentResultListener

            uiController.logAppEvent(
                MoEngageLunaAppEvents.calender_day_selected,
                hashMapOf("source" to "activity")
            )

            mainViewModel.onCalendarDateSelected(selectedDate)
            mainViewModel.getUserHealthData(mainViewModel.mStartDate, mainViewModel.mEndDate)
        }

        navigate(R.id.bottomSheetCalendar, Bundle().apply {
            this.putString("selectedDate", mainViewModel.selectedDate)
            this.putString("launchedFrom", "activity")
        })
    }

    private fun setRecyclerView() {
        binding.rvTopGraph.setOnChartScrollChangedListener(this)

        binding.rvTopGraph.setOnDateClickListener { showCalendar() }

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

                val selectedDate =
                    data?.getStringExtra("selected_date") ?: return@registerForActivityResult

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
            binding.lytWorkouts.ivViewAll.visible()
            binding.lytWorkouts.rvWorkouts.gone()
            binding.lytWorkouts.tvEmptyMsg.visible()
        }

        if (mainViewModel.selectedDate == DateFormats.getCurrentDateOreoFormat()) {
            /*if (mViewModel.ringDataStore.getRingDevice() != null) {
                binding.lytWorkouts.viewAddWorkout.visible()
            } else {
                binding.lytWorkouts.viewAddWorkout.gone()
            }*/
            binding.lytWorkouts.tvEmptyMsg.text =
                getString(R.string.text_tap_plus_workout)

        } else {
            //binding.lytWorkouts.viewAddWorkout.gone()
            binding.lytWorkouts.tvEmptyMsg.text =
                getString(R.string.text_you_haven_t_added_any_workouts_for_this_day)
        }
    }

    private fun resetDayTimeTopLevelUi() {
        val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(mViewModel.activeMinutes)
        nullableBinding?.lytDailyMovement?.apply {
            tvMovementType.text = getString(R.string.text_active_duration)
            view1.gone()
            tvStartTime.text = "$hour"
            tvStartTimeUnit.text = "hr"
            tvEndTime.text = "$minute"
            tvEndTimeUnit.text = "min"
        }

    }

    private fun updateDayTimeTopLabelUi(value: Int, position: Int) {
        LOGS.d("CLICKED POS value $value Pos $position")
        binding.lytDailyMovement.view1.visible()
        val labelValue: String
        val labelColor: Int
        when (value) {
            0 -> {
                labelValue = getString(R.string.text_inactive)
                labelColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.daytime_analysis_inactive_label_color
                )
            }

            1 -> {
                labelValue = getString(R.string.text_low_movement)
                labelColor =
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.daytime_low_movement_label_color
                    )
            }

            2 -> {
                labelValue = getString(R.string.text_medium_movement)
                labelColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.daytime_medium_movement_label_color
                )
            }

            3 -> {
                labelValue = getString(R.string.text_high_movement)
                labelColor = ContextCompat.getColor(requireContext(), R.color.white)
            }

            else -> {
                labelValue = getString(R.string.text_no_data)
                labelColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.daytime_analysis_inactive_label_color
                )
            }
        }
        binding.lytDailyMovement.tvMovementType.text = labelValue
        binding.lytDailyMovement.tvMovementType.setTextColor(labelColor)
        val startTime = mViewModel.formattedTime(mViewModel.getStartTimeFromPosition(position))
        val endTime = mViewModel.formattedTime(mViewModel.getEndTimeFromPosition(position))
        binding.lytDailyMovement.tvStartTime.text = startTime.first
        binding.lytDailyMovement.tvStartTimeUnit.text = startTime.second
        binding.lytDailyMovement.tvEndTime.text = endTime.first
        binding.lytDailyMovement.tvEndTimeUnit.text = endTime.second
    }

    override fun initListener() {

        binding.svMain.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (Math.abs(scrollY - oldScrollY) > 0) {
                binding.lytDailyMovement.lytInteractiveGraph.graphDayTime.resetIfInteracting()
            }
        }


        binding.lytDailyMovement.lytInteractiveGraph.graphDayTime.setClickListener(object :
            OnDayTimeClickAction {
            override fun onValueSelected(value: Int, position: Int) {
                updateDayTimeTopLabelUi(value, position)
            }

            override fun isInteractionOnGoing(onGoing: Boolean) {
                resetDayTimeTopLevelUi()

            }

            override fun onTopClicked() {
                if (mViewModel.stressActivityData?.isNotEmpty() == true)
                    mViewModel.stressActivityData?.toTypedArray()?.let { it1 ->
                        navigate(
                            OreoActivityFragmentDirections.actionNavigationActivityDetailsFragToDayTimeActivitiesBottomSheet(
                                it1
                            )
                        )
                    }
            }
        })

        binding.lytDailyMovement.bInfo.setOnClickListener {

            uiController.logAppEvent(
                MoEngageLunaAppEvents.info_clicked,
                hashMapOf("source" to "activity", "section" to "movement_analysis")
            )


            //mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_activity_movement_info_click)
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
            mainViewModel.handleAddWorkoutVisibility()
        }
        binding.lytToolbar.tvTitle.text = getString(R.string.text_workout_page_title)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.visible()
        binding.lytToolbar.ivAddFriend.setImageResource(R.drawable.ic_calenders)
        binding.lytToolbar.backBtn.invisible()

        binding.lytToolbar.view1.setOnClickListener {
            showCalendar()

        }

        binding.lytWorkouts.viewAddWorkout.setOnClickListener {
            navigate(R.id.addWorkoutFragment)
        }

        binding.lytWorkouts.ivViewAll.setOnClickListener {
            uiController.logAppEvent(
                MoEngageLunaAppEvents.workout_selected,
                hashMapOf("action" to "view_all", "source" to "activity")
            )
            navigate(R.id.oActivityListFragment)
        }
        binding.lytWorkouts.textView66.setOnClickListener {
            uiController.logAppEvent(
                MoEngageLunaAppEvents.workout_selected,
                hashMapOf("action" to "view_all", "source" to "activity")
            )
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

            uiController.logAppEvent(
                MoEngageLunaAppEvents.score_clicked,
                hashMapOf("source" to "activity")
            )
            //mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_activity_activity_score_click)
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

            uiController.logAppEvent(
                MoEngageLunaAppEvents.activity_analysis_clicked,
                hashMapOf("analysis_type" to "goal_progress")
            )

            //mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_activity_goal_progress_click)
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

            uiController.logAppEvent(
                MoEngageLunaAppEvents.activity_analysis_clicked,
                hashMapOf("analysis_type" to "total_calories")
            )

            //mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_activity_total_calories_click)
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

            uiController.logAppEvent(
                MoEngageLunaAppEvents.activity_analysis_clicked,
                hashMapOf("analysis_type" to "steps")
            )

            //mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_activity_steps_click)
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

            uiController.logAppEvent(
                MoEngageLunaAppEvents.activity_analysis_clicked,
                hashMapOf("analysis_type" to "distance")
            )
            //mViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_activity_distance_click)
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

        mainViewModel.nudgeActivityData.observe(this){
            it.getContent()?.let {
                if(mainViewModel.selectedDate.equals(LocalDate.now().toString())) {
                    handleNudges()
                }
            }
        }

        mainViewModel.activityHistoryResponse.observe(this) {
            if (it.isNullOrEmpty()) return@observe

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

        uiController.logAppEvent(
            MoEngageLunaAppEvents.day_selected,
            hashMapOf("source" to "activity")
        )

        mainViewModel.selectedDate = chartModel.date!!
        val returnDate = mainViewModel.updateSelectedDateActivity(mainViewModel.selectedDate)
        if (returnDate != null) {
            mainViewModel.selectedDate = returnDate
        }

        if (mainViewModel.shouldLoadMoreData()) {
            LOGS.w("Loading more data")
        }
        mainViewModel.handleAddWorkoutVisibility()
    }

    override fun onScrolling(position: Int, chartModel: ChartModel?) {

    }


}