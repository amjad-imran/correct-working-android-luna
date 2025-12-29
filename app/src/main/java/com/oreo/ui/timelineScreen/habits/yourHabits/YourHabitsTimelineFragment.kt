package com.oreo.ui.timelineScreen.habits.yourHabits

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentYourHabitsTimelineBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.timeline.habits.HabitsByDateResponse
import com.oreo.ui.circadianAlignment.CircadianAlignmentViewModel
import com.oreo.ui.timelineScreen.ItemHabitsTimelineAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class YourHabitsTimelineFragment : BaseFragment<FragmentYourHabitsTimelineBinding>(FragmentYourHabitsTimelineBinding::inflate) {

    private val viewModel: YourHabitsTimelineViewModel by viewModels()

    private val args: YourHabitsTimelineFragmentArgs by navArgs()

    private val habitsAdapter by lazy {
        YourHabitsTimelineAdapter(
            onCross = { habit ->
                viewModel.onCrossClicked(habit.timeTrackerOptionId)
            },
            onCheck = { habit ->
                handleOnCheckClicked(habit, viewModel.mDate)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.mDate = args.date

        LOGS.d("scoakcla : ${viewModel.mDate}")
        viewModel.habitsResponseData = args.selectedOptions
        viewModel.getUserSavedHabits(viewModel.mDate)
        setUi()
        setRecycler()
    }

    private fun setUi() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_your_habits)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.visible()
        binding.lytToolbar.ivAddFriend.setImageResource(R.drawable.ic_btn_customize)
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
                    val logged = list.filter { it.isCancelled || it.isCompleted }.size
                    binding.tvProgressVal.text = "$logged/${list.size}"
                    binding.habitProgress.max = list.size
                    binding.habitProgress.progress = logged
                    habitsAdapter.submitList(list)
                }
            }
        }
    }

    private fun handleOnCheckClicked(
        habit: HabitsByDateResponse.Options,
        selectedDate: String?
    ) {
        LOGS.d("alknsa : ${habit.type}")
        when(habit.type){
            "workout" -> {
                navigate(
                    R.id.addActivityTimelineFragment,
                    bundleOf(
                        "showTimeline" to false,
                        "key" to habit.type,
                        "srcKey" to "habits_timeline",
                        "habitData" to habit
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