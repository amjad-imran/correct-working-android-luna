package com.oreo.ui.timelineScreen

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentTimelineScreenBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.ChartModel
import com.oreo.ui.calendar.SELECTED_DATE
import com.oreo.ui.custom.ScrollListener
import com.oreo.ui.timelineScreen.habits.ADD_HABITS_BEGIN_KEY
import com.oreo.util.setSafeOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class TimelineScreenFragment : BaseFragment<FragmentTimelineScreenBinding>(FragmentTimelineScreenBinding::inflate),
    ScrollListener {

    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: TimelineScreenViewmodel by viewModels()
    private var pagerAdapter: TimelinePagerAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi()
        setViewPager()
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
    }

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
                binding.lytSetupHabits.root.setVisibilityByCondition(LocalDate.parse(mainViewModel.selectedDate)==todayDate)

                if (!binding.tabLayout.isInteracting) {
                    setTopBar()
                }

                if (mainViewModel.shouldLoadMoreData()) {
                    LOGS.w("Loading more data")
                }
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