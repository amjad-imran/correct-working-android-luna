package com.oreo.ui.timelineScreen

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentTimelineScreenBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.ChartModel
import com.oreo.data.model.timeline.habits.HabitsByDateResponse
import com.oreo.ui.calendar.SELECTED_DATE
import com.oreo.ui.circadianAlignment.CircadianAlignmentViewModel
import com.oreo.ui.custom.ScrollListener
import com.oreo.ui.timelineScreen.habits.ADD_HABITS_BEGIN_KEY
import com.oreo.ui.timelineScreen.habits.AddHabitsBeginBottomSheet
import com.oreo.util.setSafeOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class TimelineScreenFragment : BaseFragment<FragmentTimelineScreenBinding>(FragmentTimelineScreenBinding::inflate),
    ScrollListener {

    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: TimelineScreenViewmodel by viewModels()
    private var pagerAdapter: TimelinePagerAdapter? = null

    private val habitsAdapter by lazy {
        ItemHabitsTimelineAdapter(
            onCross = { habit ->
                viewModel.onCrossClicked(habit.timeTrackerOptionId, mainViewModel.selectedDate)
            },
            onCheck = { habit ->
                handleOnCheckClicked(habit, mainViewModel.selectedDate)
            }
        )
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi()
        setViewPager()
        setRecycler()
    }

    private fun setRecycler() {
        binding.lytSavedHabits.rvHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator().apply {
                supportsChangeAnimations = true
            }

            adapter = habitsAdapter
        }
    }

    private fun checkUserHabits(){
        if(viewModel.checkUserFirstTimeForAddHabits()){
            AddHabitsBeginBottomSheet().show(parentFragmentManager, "AddHabitsBeginBottomSheet")
            viewModel.setAddHabitFirstTimeVisibility()
        }
    }

    private fun setUi() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_timeline)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.visible()
        binding.lytToolbar.ivAddFriend.setImageResource(R.drawable.ic_calenders)
        binding.lytToolbar.backBtn.visible()
    }


    override fun initListener() {

        binding.lytSetupHabits.root.setOnClickListener {
            navigate(R.id.addHabitsFragment)
        }

        binding.lytSavedHabits.tvCustomize.setOnClickListener {
            navigate(
                R.id.addHabitsFragment,
                bundleOf("selectedOptions" to viewModel.habitsResponseData)
            )
        }

        binding.lytSavedHabits.tvMoreHabits.setOnClickListener {
            navigate(
                R.id.yourHabitsTimelineFragment,
                bundleOf(
                    "date" to mainViewModel.selectedDate,
                )
            )
        }

        binding.lytSavedHabits.tvHabitsLogged.setOnClickListener {
            navigate(
                R.id.yourHabitsTimelineFragment,
                bundleOf(
                    "date" to mainViewModel.selectedDate,
                )
            )
        }

        binding.ivAddLogFab.setOnClickListener {
            mainViewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.insight_log,
                HashMap<String, Any>().apply {
                    this["source"] = "timeline"
                }
            )
            navigate(
                R.id.addActivityTimelineFragment,
                bundleOf(
                    "showTimeline" to false,
                    "key" to null,
                    "srcKey" to "timeline"
                )
            )
        }

        binding.tabLayout.setOnChartScrollChangedListener(this)

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytToolbar.view1.setSafeOnClickListener() {
            mainViewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.calendar_day_selected,
                HashMap<String, Any>().apply {
                    this["source"] = "timeline"
                }
            )
            showCalendar()
        }
    }

    private fun showCalendar() {
        setFragmentResultListener(SELECTED_DATE) { requestKey, bundle ->
            val selectedDate =
                bundle.getString("selected_date") ?: return@setFragmentResultListener

            /*uiController.logAppEvent(
                MoEngageLunaAppEvents.calender_day_selected,
                hashMapOf("source" to "readiness")
            )*/

            LOGS.d("Setting_data selected date $selectedDate")


            mainViewModel.onCalendarDateSelected(selectedDate)
            mainViewModel.getUserHealthData(mainViewModel.mStartDate, mainViewModel.mEndDate)
        }

        navigate(R.id.bottomSheetCalendar, Bundle().apply {
            this.putString("selectedDate", mainViewModel.selectedDate)
            this.putString("launchedFrom", "timeline")
        })
    }

    fun setTopBar() {

        val it = mainViewModel.dashboard.value
        if (it.isNullOrEmpty()) return

        val topGraphData = viewModel.getPrefixAndSuffixList(it)

        var moveToPos = -1

        if (mainViewModel.selectedDate != null) {
            val index = it?.indexOfFirst { data ->
                data.equals(mainViewModel.selectedDate, true)
            }
            if (index != null) {
                moveToPos = 15 + (it.size - index - 1)
            }
        }

        binding.tabLayout.updateDataWithMax(
            topGraphData.first,
            topGraphData.third,
            topGraphData.second,
            moveToPos
        )
    }


    override fun subscribeObservers() {
        mainViewModel.dashboard.observe(viewLifecycleOwner) {
            LOGS.w("Setting_data size ${it.size}")
            if (pagerAdapter == null) {
                pagerAdapter = TimelinePagerAdapter(this)
                binding.viewPagerTimeline.adapter = pagerAdapter
            }
            pagerAdapter?.setDataSet(it)

            val pos = pagerAdapter?.getPositionForDate(mainViewModel.selectedDate) ?: (it.size - 1)
            LOGS.w("Setting_data pos ${pos} ${mainViewModel.selectedDate}")

            // Jump without smooth scroll to avoid visible page hopping
            if (binding.viewPagerTimeline.currentItem != pos) {
                binding.viewPagerTimeline.setCurrentItem(pos, false)
            }
            binding.tabLayout.visible()
            //setTabDates(pos)

        }

        /*viewModel.allHabits.observe(viewLifecycleOwner){ response ->
            response?.options.let { list ->
                if(list.isNullOrEmpty()){
                    binding.lytSavedHabits.root.gone()
                    binding.lytSetupHabits.root.visible()
                }else{
                    binding.lytSetupHabits.root.gone()
                    binding.lytSavedHabits.root.visible()
                    setLytSavedHabitsUi(list)
                }
            }
        }*/

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allHabits.collectLatest { mList ->
                    if(mList.isEmpty()){
                        checkUserHabits()
                        binding.lytSavedHabits.root.gone()
                        binding.lytSetupHabits.root.visible()
                    }else{
                        val todayDate = LocalDate.now()
                        val isPrevOrCurDay = todayDate.toString() == mainViewModel.selectedDate ||
                                todayDate.minusDays(1).toString() == mainViewModel.selectedDate

                        binding.lytSetupHabits.root.gone()
                        binding.lytSavedHabits.root.visible()
                        val total = mList.size
                        val curProgress = mList.filter { it.isCompleted || it.isCancelled }.size

                        if(total==curProgress || !isPrevOrCurDay) binding.lytSavedHabits.lytContentAndFooter.gone()
                        else binding.lytSavedHabits.lytContentAndFooter.visible()

                        binding.lytSavedHabits.habitProgress.apply {
                            this.max = total
                            this.progress = curProgress
                        }

                        if(total != 0) {
                            binding.lytSavedHabits.tvHabitsLogged.text =
                                getString(R.string.text_val_habits_logged, curProgress, total)
                        }

                        launch {
                            viewModel.visibleHabits.collect { list ->
                                habitsAdapter.submitList(
                                    if (isPrevOrCurDay) list
                                    else emptyList()
                                )
                            }
                        }

                        launch {
                            viewModel.moreCount.collect { count ->
                                if (count > 0){
                                    binding.lytSavedHabits.tvMoreHabits.apply {
                                        text = getString(R.string.text_val_more, count)
                                        isClickable = true
                                    }
                                }
                                else{
                                    binding.lytSavedHabits.tvMoreHabits.apply {
                                        text = ""
                                        isClickable = false
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    /*private fun setLytSavedHabitsUi(list: ArrayList<HabitsByDateResponse.Options>) {
        val itemView = layoutInflater.inflate(
            R.layout.item_habit_timeline_screen,
            null,
            false
        )
        val itemView1 = layoutInflater.inflate(
            R.layout.item_habit_timeline_screen,
            null,
            false
        )
        binding.lytSavedHabits.apply {
            this.lvHabits.addView(itemView.apply{
                this.findViewById<TextView>(R.id.tvHabitTitle).text = "Cold Plunge"
            })
            this.lvHabits.addView(itemView1.apply{
                this.findViewById<TextView>(R.id.tvHabitTitle).text = "Vitamin B12"
            })
            this.tvMoreHabits.text = "+2 more"
        }
    }*/

    override fun onPositionSelected(position: Int, chartModel: ChartModel?) {
        if (mainViewModel.selectedDate == chartModel?.date!!) {
            return
        }
        mainViewModel.selectedDate = chartModel.date!!
        //mainViewModel.handleAddWorkoutVisibility()

        val returnDate = mainViewModel.updateSelectedDate(mainViewModel.selectedDate)
        if (returnDate != null) {
            mainViewModel.selectedDate = returnDate
        }

        val pos = pagerAdapter?.getPositionForDate(mainViewModel.selectedDate)
        if (pos != null && pos != -1) {
            binding.viewPagerTimeline.setCurrentItem(pos, false)
        }

        val shouldShow = mainViewModel.shouldShowStressCard(mainViewModel.selectedDate!!)
        if (shouldShow.not()) return

        if (mainViewModel.shouldLoadMoreData()) {
            LOGS.w("Loading more data")
        }
    }

    override fun onScrolling(position: Int, chartModel: ChartModel?) {

    }


    private fun setViewPager() {
        pagerAdapter = TimelinePagerAdapter(this)
        binding.viewPagerTimeline.adapter = pagerAdapter
        binding.viewPagerTimeline.offscreenPageLimit = 1
        binding.viewPagerTimeline.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                mainViewModel.selectedDate = pagerAdapter?.getDate(position)
                //setTabDates(position)
                val todayDate = LocalDate.now()
                binding.ivAddLogFab.setVisibilityByCondition(LocalDate.parse(mainViewModel.selectedDate)==todayDate)

                if (!binding.tabLayout.isInteracting) {
                    setTopBar()
                }

                if (mainViewModel.shouldLoadMoreData()) {
                    LOGS.w("Loading more data")
                }

                viewModel.getUserSavedHabits(mainViewModel.selectedDate)
            }
        })

    }

    private fun displayAddHabitsBS(){
        setFragmentResultListener(ADD_HABITS_BEGIN_KEY) { _, bundle ->
            val addHabit = bundle.getBoolean("addHabits")
            if(addHabit==true){
                navigate(R.id.addHabitsFragment)
            }
        }
        navigate(R.id.addHabitsBeginBottomSheet)
    }

}