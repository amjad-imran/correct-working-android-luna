package com.oreo.ui.timelineScreen.habits.yourHabits

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentYourHabitsTimelineBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.timeline.habits.HabitsByDateResponse
import com.oreo.ui.circadianAlignment.CircadianAlignmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class YourHabitsTimelineFragment : BaseFragment<FragmentYourHabitsTimelineBinding>(FragmentYourHabitsTimelineBinding::inflate) {

    private val viewModel: YourHabitsTimelineViewModel by viewModels()

    private val args: YourHabitsTimelineFragmentArgs by navArgs()

    private val habitsAdapter by lazy {
        YourHabitsTimelineAdapter(
            onCross = { habit, isAlreadyMarked ->
                if(isAlreadyMarked){
                    displayModifyingEntriesNotiBS()
                }
                else {
                    viewModel.sessionManager.logMoEngageAppEvent(
                        MoEngageLunaAppEvents.habits_not_done,
                        hashMapOf(
                            "category" to "${habit.type}"
                        )
                    )
                    viewModel.onCrossClicked(habit.timeTrackerOptionId)
                }
            },
            onCheck = { habit, isAlreadyMarked ->
                if(isAlreadyMarked){
                    displayModifyingEntriesNotiBS()
                }
                else {
                    viewModel.sessionManager.logMoEngageAppEvent(
                        MoEngageLunaAppEvents.habits_tick_clicked,
                        hashMapOf(
                            "category" to "${habit.type}"
                        )
                    )
                    handleOnCheckClicked(habit, viewModel.mDate)
                }
            }
        )
    }

    private fun displayModifyingEntriesNotiBS() {
        navigate(
            R.id.modifyingEntriesBottomSheet,
            bundleOf(
                "title" to getString(R.string.text_modifying_entries),
                "description" to getString(R.string.text_edit_entries_by_adding_or_removing_them_from_your_timeline),
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.mDate = args.date

        viewModel.habitsResponseData = args.selectedOptions

        viewModel.getUserSavedHabits(viewModel.mDate)
        habitsAdapter.isButtonsDisabled = viewModel.getIsButtonsDisabled(viewModel.mDate)

        setUi()
        setRecycler()
    }

    private fun setUi() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_your_habits)

        binding.lytToolbar.ivAddFriend.invisible()
        binding.lytToolbar.view1.setBackgroundResource(R.drawable.ic_customize_btn_your_habits)
        binding.lytToolbar.view1.visible()

        binding.lytToolbar.backBtn.setImageResource(0)
        binding.lytToolbar.backBtn.setBackgroundResource(R.drawable.ic_close_add_habits)
        binding.lytToolbar.backBtn.visible()
    }

    private fun setRecycler() {
        binding.rvHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitsAdapter
        }
    }

    override fun initListener() {
        binding.lytToolbar.view1.setOnClickListener {
            navigate(
                R.id.addHabitsFragment,
                bundleOf("selectedOptions" to viewModel.habitsResponseData)
            )
        }

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allHabits.collect { list ->
                    if(list.isEmpty()) return@collect
                    val logged = list.filter { it.isCancelled || it.isCompleted }.size
                    binding.tvProgressVal.visible()
                    binding.tvProgressVal.text = "$logged/${list.size}"
                    binding.habitProgress.visible()
                    binding.habitProgress.max = list.size
                    binding.habitProgress.progress = logged
                    habitsAdapter.submitList(list)
                }
            }
        }

        //
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }

    private fun handleOnCheckClicked(
        habit: HabitsByDateResponse.Options,
        selectedDate: String?
    ) {
        when(habit.type){
            "workout" -> {
                val activityType = when(habit.workoutType) {
                    "freestyle_workout" -> "freestyle"
                    "outdoor_running" -> "running"
                    "indoor_running" -> "running"
                    "outdoor_cycling" -> "bicycling"
                    "indoor_cycling" -> "bicycling"
                    else -> null
                }
                navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to habit.type,
                        "srcKey" to "habits_timeline",
                        "habitData" to if(activityType==null) habit else habit.copy(workoutType = activityType)
                    )
                )
            }

            "supplements" -> {
                navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to habit.type,
                        "srcKey" to "habits_timeline",
                        "editData" to null,
                        "lunaOption" to habit.options
                    )
                )
            }

            "alcohol" -> {
                navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to "alcohol",
                        "srcKey" to "habits_timeline",
                        "editData" to null,
                        "lunaOption" to null
                    )
                )
            }

            "caffeine" -> {
                navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to CircadianAlignmentViewModel.caffeine_window_key,
                        "srcKey" to "habits_timeline",
                        "editData" to null,
                        "lunaOption" to null
                    )
                )
            }

            "light_exposure" -> {
                navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to CircadianAlignmentViewModel.light_exposure_key,
                        "srcKey" to "habits_timeline",
                        "editData" to null,
                        "lunaOption" to null
                    )
                )
            }

            "recovery" -> {
                navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to habit.type,
                        "srcKey" to "habits_timeline",
                        "editData" to null,
                        "lunaOption" to habit.options
                    )
                )
            }

            "sleep_env" -> {
                val timelineData = viewModel.localDataStore.getTimelineActivitiesData()
                val mostRecentSleep = timelineData?.find { it.event.equals("sleep") }
                if (mostRecentSleep == null) {
                    val mostRecentNap = timelineData?.find { it.event.equals("nap") }
                    if (mostRecentNap == null) {
                        navigate(
                            R.id.addActivityTimelineFragment,
                            bundleOf(
                                "showTimeline" to false,
                                "key" to CircadianAlignmentViewModel.sleep_key,
                                "srcKey" to "habits_timeline",
                                "editData" to null,
                                "lunaOption" to null
                            )
                        )
                    } else {
                        mostRecentNap.canBeEditedOrDeleted = 1
                        navigate(
                            R.id.addActivityTimelineFragment,
                            bundleOf(
                                "showTimeline" to false,
                                "key" to mostRecentNap.event,
                                "srcKey" to null,
                                "editData" to mostRecentNap
                            )
                        )
                    }
                } else {
                    mostRecentSleep.canBeEditedOrDeleted = 1
                    navigate(
                        R.id.addActivityTimelineFragment,
                        bundleOf(
                            "showTimeline" to false,
                            "key" to mostRecentSleep.event,
                            "srcKey" to null,
                            "editData" to mostRecentSleep
                        )
                    )
                }
            }

            /*"sleep_env" -> {
                val timelineData = mainViewModel.localDataStore.getTimelineActivitiesData()
                val mostRecentSleep = timelineData?.find { it.event.equals("sleep") }
                if (mostRecentSleep == null) {
                    val mostRecentNap = timelineData?.find { it.event.equals("nap") }
                    if (mostRecentNap == null) {
                        navigate(
                            R.id.addActivityTimelineFragment,
                            bundleOf(
                                "showTimeline" to false,
                                "key" to CircadianAlignmentViewModel.sleep_key,
                                "srcKey" to "habits_timeline",
                                "editData" to null,
                                "lunaOption" to null
                            )
                        )
                    } else {
                        val mRecentNap = mostRecentNap.copy().apply {
                            val lunaTrackingOptionIds = this.metadata?.lunaTrackingOptionIds
                            if(lunaTrackingOptionIds.isNullOrEmpty()){
                                this.metadata?.copy(
                                    lunaTrackingOptionIds = listOf(habit.timeTrackerOptionId!!)
                                )
                            }else{
                                this.metadata?.copy(
                                    lunaTrackingOptionIds = ArrayList(lunaTrackingOptionIds).apply {
                                        add(habit.timeTrackerOptionId)
                                    }
                                )
                            }
                        }

                        mRecentNap.canBeEditedOrDeleted = 1
                        navigate(
                            R.id.addActivityTimelineFragment,
                            bundleOf(
                                "showTimeline" to false,
                                "key" to mRecentNap.event,
                                "srcKey" to null,
                                "editData" to mRecentNap
                            )
                        )
                    }
                } else {
                    val mRecentSleep = mostRecentSleep.copy().apply {
                        val lunaTrackingOptionIds = this.metadata?.lunaTrackingOptionIds
                        if(lunaTrackingOptionIds.isNullOrEmpty()){
                            this.metadata?.copy(
                                lunaTrackingOptionIds = listOf(habit.timeTrackerOptionId!!)
                            )
                        }else{
                            this.metadata?.copy(
                                lunaTrackingOptionIds = ArrayList(lunaTrackingOptionIds).apply {
                                    add(habit.timeTrackerOptionId)
                                }
                            )
                        }
                    }

                    mRecentSleep.canBeEditedOrDeleted = 1
                    navigate(
                        R.id.addActivityTimelineFragment,
                        bundleOf(
                            "showTimeline" to false,
                            "key" to mRecentSleep.event,
                            "srcKey" to null,
                            "editData" to mRecentSleep
                        )
                    )
                }
            }*/

            else -> {}
        }
    }

}