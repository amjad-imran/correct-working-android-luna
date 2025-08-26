package com.oreo.ui.stress

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOStressDataMovementBinding
import com.noisefit.luna.databinding.LayoutStressHeaderSubItemBinding
import com.noisefit.luna.databinding.OreoLayoutHourMnBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.ServerUserHealthData
import com.oreo.data.model.Stress
import com.oreo.data.model.StressNudge
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.readiness.NudgeBannerListener
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import com.oreo.ui.stress.banner.OreoStressBannerFragment
import com.oreo.ui.stress.help.StressInfoCardAction
import com.oreo.ui.stress.help.StressUnderstandingImageAdapter
import com.oreo.util.DateTimeUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt


@AndroidEntryPoint
class OStressDataMovementFragment :
    BaseFragment<FragmentOStressDataMovementBinding>(FragmentOStressDataMovementBinding::inflate) {
    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val sharedViewModel: StressDetailSharedViewModel by activityViewModels()
    private val viewModel: OStressDetailViewModel by viewModels()
    private val ARGS_DATE = "ARGS_DATE"

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    private val setBackHandler = Handler(Looper.getMainLooper())
    private val howItWorksAdapter: StressUnderstandingImageAdapter by lazy {
        StressUnderstandingImageAdapter(object : StressInfoCardAction {
            override fun onStressInfoCardClicked() {
                navigate(R.id.stressUnderstandingFragment)
            }

            override fun onCircadianCardClicked(pos: Int) {}
        })
    }

    private var setBackRunnable = Runnable {
        sharedViewModel.setSelectedType(viewModel.getStressType(viewModel.lastStressValue))
    }


    companion object {

        @JvmStatic
        fun newInstance(date: String) = OStressDataMovementFragment().apply {
            arguments = Bundle().apply {
                putString(ARGS_DATE, date)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val date = arguments?.getString(ARGS_DATE)
        viewModel.date = date
        viewModel.checkIsToday(date)

        handleMovementViews()
        setHowItWorksRecycler()


    }

    private fun setHowItWorksRecycler() {
        binding.rvHowItWorks.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHowItWorks.adapter = howItWorksAdapter
    }


    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        viewModel.date?.let {
            mainViewModel.getStressData(it)?.let { dayData ->

                viewModel.isSelectedMode = false

                val day = viewModel.getDayFromDate(dayData.date)
                binding.lytStressHeader.tvHeader.text =
                    if (viewModel.isToday) getString(R.string.text_today) else day.capitalizeWords()
                binding.lytStressHeader.tvTypical.text =
                    getString(R.string.text_vs_typical_value, day)

                binding.lytInactiveStressHeader.tvHeader.text =
                    if (viewModel.isToday) getString(R.string.text_today) else day.capitalizeWords()
                binding.lytInactiveStressHeader.tvTypical.text = getString(R.string.text_vs_typical_value, day)

                viewModel.defaultMeterData = Pair(
                    dayData.stress?.stressValue?.value, dayData.stress?.stressValue?.lastUpdated
                )


                initCombineChart(dayData)
                val combinedData = viewModel.getCombinedMovementData(
                    dayData.activity?.daytimeMovement?.movement, true
                )
                viewModel.dayTimeMovement = combinedData

                setMovementData(combinedData, viewModel.isSelectedMode, -1)
                handleStressProgressView(dayData.stress)
                handleNonActiveStressProgressView(dayData.stress)
                //viewModel.prepareStressActivityData(dayData)
                setStressBannerViewPager(dayData.stress?.nudges)
                viewModel.prepareStressActivityData(dayData)

                setTopMeter(
                    dayData.stress?.stressValue?.value, dayData.stress?.stressValue?.lastUpdated
                )

            }
        }
    }

    private fun setTopMeter(
        value: Int? = null, lastUpdated: Long? = null, selectedValueTime: String? = null
    ) {

        binding.lytTopStressGraph.tvStressValue.text = if (value == null || value == 0) {
            "--"
        } else {
            "$value"
        }


        if (viewModel.isToday) {
            val lastUpdatedTimestamp = lastUpdated ?: 0
            if (lastUpdatedTimestamp == 0L) {
                binding.lytTopStressGraph.tvLastSyncStatus.text = ""
            } else {
                binding.lytTopStressGraph.tvLastSyncStatus.text =
                    DateTimeUtil.getRelativeTime(lastUpdatedTimestamp,viewModel.resourcesProvider)
            }
        } else {
            binding.lytTopStressGraph.tvLastSyncStatus.text = ""
        }

        if (!selectedValueTime.isNullOrEmpty() && value != null && value != 0) {
            binding.lytTopStressGraph.tvLastSyncStatus.text = selectedValueTime
        }

        val (stressValue, stressColor) = viewModel.getStressStatus(value)
        binding.lytTopStressGraph.tvStressStatus.text = stressValue
        binding.lytTopStressGraph.tvStressStatus.setTextColor(resources.getColor(stressColor, null))

        viewModel.lastStressValue = value
        setBackHandler.removeCallbacks(setBackRunnable)
        setBackHandler.postDelayed(setBackRunnable, 200)

        val newDegree = viewModel.getRotationDegree(value)

        val rotate = RotateAnimation(
            viewModel.oldDegree,
            newDegree,
            Animation.RELATIVE_TO_SELF,
            1f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = 500
        rotate.fillAfter = true
        rotate.interpolator = AccelerateDecelerateInterpolator()
        binding.lytTopStressGraph.lytTicker.startAnimation(rotate)
        viewModel.oldDegree = newDegree

    }


    override fun initListener() {

        binding.ivHowItWorks.setOnClickListener {
            navigate(R.id.stressUnderstandingFragment)
            mainViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_how_it_works_stress_click)
        }

        binding.svMain.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (Math.abs(scrollY - oldScrollY) > 0) {
                binding.lytStressMidGraph.graphStress.resetIfInteracting()
            }
        }

        binding.lytStressHeader.root.setOnClickListener {
            mainViewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.deep_insights_clicked,
                HashMap<String, Any>().apply {
                    this["source"] = "overall_stress"
                }
            )
            navigate(R.id.stressInternalParentOreo, Bundle().apply {
                putString("date", mainViewModel.selectedDate)
                putString("cameFrom", "active")
            })
            mainViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_overall_stress_click)
        }
        binding.lytInactiveStressHeader.root.setOnClickListener {
            mainViewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.deep_insights_clicked,
                HashMap<String, Any>().apply {
                    this["source"] = "non_active_stress"
                }
            )
            navigate(R.id.stressInternalParentOreo, Bundle().apply {
                putString("date", mainViewModel.selectedDate)
                putString("cameFrom", "inactive")
            })
        }

        binding.lytStressMidGraph.graphStress.setVibrationUtil(vibrationUtils)

        binding.lytStressMidGraph.graphStress.setClickListener(object : OnStressClickAction {

            override fun onValueSelected(value: Int, position: Int) {

                val time = viewModel.getTimeFromPosition(position)
                setTopMeter(value, 0, time)
            }

            override fun isInteractionOnGoing(onGoing: Boolean) {
                if (!onGoing) {
                    if (viewModel.defaultMeterData != null) {
                        setTopMeter(
                            viewModel.defaultMeterData?.first, viewModel.defaultMeterData?.second
                        )
                    }
                }
            }

            override fun onTopClicked() {
                if (viewModel.stressActivityData.isNullOrEmpty()) return

                viewModel.stressActivityData?.toTypedArray()?.let { it1 ->
                    navigate(
                        OStressDetailsFragmentDirections.actionStressDetailFragmentToBottomSheetStressActivity(
                            it1
                        )
                    )
                }
            }
        })

        binding.ivOpen.setOnClickListener {
            mainViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_stress_movement_down_click)
            handleMovementViews(true)
        }
        binding.ivClose.setOnClickListener {
            handleMovementViews(false)
            removeMovementHighlights()
        }

        binding.lytHighMovement.root.setOnClickListener {
            handleMovementClick(3)
        }
        binding.lytMediumMovement.root.setOnClickListener {
            handleMovementClick(2)
        }
        binding.lytLowMovement.root.setOnClickListener {
            handleMovementClick(1)
        }
        binding.lytNoMovement.root.setOnClickListener {
            handleMovementClick(0)
        }

        binding.rvHowItWorks.addOnItemTouchListener(object :
            RecyclerView.OnItemTouchListener {

            override fun onTouchEvent(view: RecyclerView, event: MotionEvent) {}

            override fun onInterceptTouchEvent(view: RecyclerView, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        binding.rvHowItWorks.parent?.requestDisallowInterceptTouchEvent(
                            true
                        )
                    }
                }
                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    fun removeMovementHighlights() {
        setMovementData(viewModel.dayTimeMovement, false, -1)
        viewModel.isSelectedMode = false
        viewModel.lastSelectedType = -1
        binding.lytStressMidGraph.graphStress.removeHighlights()
    }

    private fun handleMovementClick(type: Int) {
        val lastSelected = viewModel.lastSelectedType

        if (lastSelected == type) {
            removeMovementHighlights()
        } else {
            setMovementData(viewModel.dayTimeMovement, true, type)
            viewModel.isSelectedMode = true
            viewModel.lastSelectedType = type
            val highlights = viewModel.getHighlights(type, viewModel.dayTimeMovement)
            val color = viewModel.getMovementColor(type)
            binding.lytStressMidGraph.graphStress.updateHighlight(
                highlights, resources.getColor(color, null)
            )
        }
        mainViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_stress_movement_click)
    }

    private fun handleProgress(pgbr: ProgressBar, progress: Int) {
        val height = viewModel.screenUtils.dpToPx(
            50, pgbr.context
        )// change if you change progress height in xml
        val percentageFactor = 100 / height
        val newProgress = progress.toFloat() / percentageFactor // because the height is 50

        val layoutHeight = calculateWeightPercent(newProgress, height)

        val params = pgbr.layoutParams
        params.height = if (layoutHeight == 0) 1 else layoutHeight.toInt()
        pgbr.layoutParams = params
    }

    private fun handleComparisonsBar(
        layout: LayoutStressHeaderSubItemBinding,
        todayValue: Int,
        typicalValue: Int,
        drawableToday: Int,
        drawableCompare: Int,
        maxValue: Int
    ) {
        layout.pgBrToday.progressDrawable =
            ContextCompat.getDrawable(requireContext(), drawableToday)
        layout.pgBrPrevious.progressDrawable =
            ContextCompat.getDrawable(requireContext(), drawableCompare)

        handleProgress(layout.pgBrToday, viewModel.getBarPercent(todayValue, maxValue))
        handleProgress(layout.pgBrPrevious, viewModel.getBarPercent(typicalValue, maxValue))
        val diff = viewModel.getDifference(todayValue, typicalValue)
        layout.tvDifference.text = "${abs(diff)}%"
        if (diff > 0) {
            layout.icTrend.visible()
            layout.icTrend.rotation = 0f
        } else if (diff < 0) {
            layout.icTrend.visible()
            layout.icTrend.rotation = 180f
        } else {
            layout.icTrend.gone()
            layout.tvDifference.text = getString(R.string.text_no_change)
        }

    }

    private fun setHourMin(
        layout: OreoLayoutHourMnBinding, total: Int, hour: Int, minute: Int
    ) {
        if (total == 0) {
            layout.tvHour.text = "--"

            layout.tvHour.visible()
            layout.tvHourUnit.gone()
            layout.tvMinute.gone()
            layout.tvMinuteUnit.gone()
        } else {
            layout.tvHour.text = "$hour"
            layout.tvMinute.text = "$minute"

            layout.tvHour.visible()
            layout.tvHourUnit.visible()
            layout.tvMinute.visible()
            layout.tvMinuteUnit.visible()
        }

    }

    private fun handleStressProgressView(stress: Stress?) {
        LOGS.d("Stress data ${Gson().toJson(stress)}")

        val (calm, focused, stressed) = viewModel.getStressMinutes(stress)
        val total = calm + focused + stressed

        val stressDays = mainViewModel.stressDaysFromCurrent(viewModel.date)

        binding.lytStressHeader.apply {
            tvTitle.text = getString(R.string.text_overall_stress)
            tvSubTitle.text = getString(R.string.text_active_stress_definition)
            var hasComparisonData =
                (stress?.typicalCalm != null && stress.typicalFocused != null && stress.typicalStressed != null)

            if (stressDays < 7) {
                hasComparisonData = false
            }

            val (hourCalm, minuteCalm) = ApplicationUtils.getFormattedSleepDuration(
                calm
            )
            setHourMin(lytCalm.lytHrMn, total, hourCalm, minuteCalm)

            lytCalm.tvCalm.setTextColor(resources.getColor(R.color.stress_nap_calm, null))
            lytCalm.tvCalm.text = getString(R.string.text_relaxed)

            val (hourFocused, minuteFocused) = ApplicationUtils.getFormattedSleepDuration(
                focused
            )

            setHourMin(lytFocussed.lytHrMn, total, hourFocused, minuteFocused)

            lytFocussed.tvCalm.setTextColor(resources.getColor(R.color.stress_nap_focussed, null))
            lytFocussed.tvCalm.text = getString(R.string.text_focussed)

            val (hourStressed, minuteStressed) = ApplicationUtils.getFormattedSleepDuration(
                stressed
            )
            setHourMin(lytStressed.lytHrMn, total, hourStressed, minuteStressed)

            lytStressed.tvCalm.setTextColor(resources.getColor(R.color.stress_nap_stressed, null))
            lytStressed.tvCalm.text = getString(R.string.text_stressed)

            lytStressed.view1.gone()

            if (hasComparisonData) {
                lytCalm.lytComparison.visible()
                lytFocussed.lytComparison.visible()
                lytStressed.lytComparison.visible()
            } else {
                lytCalm.lytComparison.gone()
                lytCalm.view1.gone()
                lytFocussed.lytComparison.gone()
                lytFocussed.view1.gone()
                lytStressed.lytComparison.gone()
                lytStressed.view1.gone()
                return@apply
            }

            val maxValue = arrayListOf(
                calm,
                stress?.typicalCalm ?: 0,
                focused,
                stress?.typicalFocused ?: 0,
                stressed,
                stress?.typicalStressed ?: 0
            ).max()

            handleComparisonsBar(
                lytCalm,
                calm,
                stress?.typicalCalm ?: 0,
                R.drawable.grad_today_calm,
                R.drawable.grad_previous_calm,
                maxValue
            )
            handleComparisonsBar(
                lytFocussed,
                focused,
                stress?.typicalFocused ?: 0,
                R.drawable.grad_today_focused,
                R.drawable.grad_previous_focused,
                maxValue
            )
            handleComparisonsBar(
                lytStressed,
                stressed,
                stress?.typicalStressed ?: 0,
                R.drawable.grad_today_stressed,
                R.drawable.grad_previous_stressed,
                maxValue
            )
        }
    }

    private fun handleNonActiveStressProgressView(stress: Stress?) {
        LOGS.d("Stress data ${Gson().toJson(stress?.nonActive)}")

//        val (calm, focused, stressed) = viewModel.getStressMinutes(stress)
        val nonActiveData = stress?.nonActive
        val calm = nonActiveData?.nonActiveCalm ?: 0
        val focused = nonActiveData?.nonActiveFocused ?: 0
        val stressed = nonActiveData?.nonActiveStressed ?: 0
        val total = calm + focused + stressed
        val stressDays = mainViewModel.stressDaysFromCurrent(viewModel.date)

        binding.lytInactiveStressHeader.apply {
            tvSubTitle.text = getString(R.string.text_inactive_stress_definition)
            tvTitle.text = getString(R.string.text_non_active_stress)
            var hasComparisonData =
                (nonActiveData?.typicalNonActiveCalm != null && nonActiveData.typicalNonActiveFocused != null && nonActiveData.typicalNonActivestressed != null)

            if (stressDays < 7) {
                hasComparisonData = false
            }

            val (hourCalm, minuteCalm) = ApplicationUtils.getFormattedSleepDuration(
                calm
            )
            setHourMin(lytCalm.lytHrMn, total, hourCalm, minuteCalm)

            lytCalm.tvCalm.setTextColor(resources.getColor(R.color.stress_nap_calm, null))
            lytCalm.tvCalm.text = getString(R.string.text_relaxed)

            val (hourFocused, minuteFocused) = ApplicationUtils.getFormattedSleepDuration(
                focused
            )

            setHourMin(lytFocussed.lytHrMn, total, hourFocused, minuteFocused)

            lytFocussed.tvCalm.setTextColor(resources.getColor(R.color.stress_nap_focussed, null))
            lytFocussed.tvCalm.text = getString(R.string.text_focussed)

            val (hourStressed, minuteStressed) = ApplicationUtils.getFormattedSleepDuration(
                stressed
            )
            setHourMin(lytStressed.lytHrMn, total, hourStressed, minuteStressed)

            lytStressed.tvCalm.setTextColor(resources.getColor(R.color.stress_nap_stressed, null))
            lytStressed.tvCalm.text = getString(R.string.text_stressed)

            lytStressed.view1.gone()

            if (hasComparisonData) {
                lytCalm.lytComparison.visible()
                lytFocussed.lytComparison.visible()
                lytStressed.lytComparison.visible()
            } else {
                lytCalm.lytComparison.gone()
                lytCalm.view1.gone()
                lytFocussed.lytComparison.gone()
                lytFocussed.view1.gone()
                lytStressed.lytComparison.gone()
                lytStressed.view1.gone()
                return@apply
            }
            val maxValue = arrayListOf(
                calm,
                nonActiveData?.typicalNonActiveCalm ?: 0,
                focused,
                nonActiveData?.typicalNonActiveFocused ?: 0,
                stressed,
                nonActiveData?.typicalNonActivestressed ?: 0
            ).max()

            handleComparisonsBar(
                lytCalm,
                calm,
                nonActiveData?.typicalNonActiveCalm ?: 0,
                R.drawable.grad_today_calm,
                R.drawable.grad_previous_calm,
                maxValue
            )
            handleComparisonsBar(
                lytFocussed,
                focused,
                nonActiveData?.typicalNonActiveFocused ?: 0,
                R.drawable.grad_today_focused,
                R.drawable.grad_previous_focused,
                maxValue
            )
            handleComparisonsBar(
                lytStressed,
                stressed,
                nonActiveData?.typicalNonActivestressed ?: 0,
                R.drawable.grad_today_stressed,
                R.drawable.grad_previous_stressed,
                maxValue
            )
        }
    }

    private fun calculateWeightPercent(progress: Float, total: Float): Int {
        return (progress / total).times(100).roundToInt()
    }

    private fun setStressBannerViewPager(data: List<StressNudge>?) {
        val nudgeList = ArrayList<StressNudge>()
        if (data.isNullOrEmpty()) {
            nudgeList.add(
                StressNudge(
                    label = getString(R.string.text_no_summary_available),
                    message = getString(R.string.text_there_wasn_t_enough_data)
                )
            )
        } else {
            nudgeList.addAll(data)
        }

        binding.lytStressBanner.root.visible()
        binding.divider1.root.visible()

        val fragments = ArrayList<OreoStressBannerFragment>()

        nudgeList.forEach {
            fragments.add(OreoStressBannerFragment.newInstance(it).apply {
                setClickListener(
                    object : NudgeBannerListener {
                        override fun onAiClicked() {
                            mainViewModel.sessionManager.logMoEngageAppEvent(
                                MoEngageLunaAppEvents.ai_widget_clicked,
                                HashMap<String, Any>().apply {
                                    this["source"] = "stress"
                                }
                            )
                            navigate(
                                R.id.aiTopQuestionsFragment,
                                bundleOf("aiTopic" to AITopics.STRESS)
                            )
                        }
                    }
                )
            })
        }

        val winsAdapter = OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)
        binding.lytStressBanner.vpBannerSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })
            adapter = winsAdapter
        }

        TabLayoutMediator(
            binding.lytStressBanner.tabLayout, binding.lytStressBanner.vpBannerSlider
        ) { _, _ -> }.attach()

        binding.lytStressBanner.vpBannerSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)


            }

        })

        if (fragments.size > 1) {
            binding.lytStressBanner.tabLayout.visible()
        } else {
            binding.lytStressBanner.tabLayout.invisible()
        }


    }

    override fun subscribeObservers() {
        viewModel.howItWorksDataList.observe(this) {
            howItWorksAdapter.setDataSet(it)
            binding.tvHow.visible()
            binding.ivHowItWorks.visible()
            binding.rvHowItWorks.visible()
        }
    }

    private fun setMovementData(
        combinedData: List<Int>?, isSelectedMode: Boolean, selectedType: Int
    ) {

        binding.lytHighMovement.apply {
            tvHeader.text = getString(R.string.text_high_movement)
            compareChart.updateInitData(3, requireContext().getColor(R.color.white))
            compareChart.setDrawData(
                combinedData ?: ArrayList(), isSelectedMode
            )

            if (selectedType == 3) {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10)
                this.root.alpha = 1.0f
            } else {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10_normal)
                if (selectedType == -1) {
                    this.root.alpha = 1f
                } else {
                    this.root.alpha = 0.5f
                }
            }
        }

        binding.lytMediumMovement.apply {
            binding.lytMediumMovement.tvHeader.text = getString(R.string.text_medium_movement)
            compareChart.updateInitData(2, requireContext().getColor(R.color.medium_movement_color))
            binding.lytMediumMovement.compareChart.setDrawData(
                combinedData ?: ArrayList(), isSelectedMode
            )

            if (selectedType == 2) {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10)
                this.root.alpha = 1.0f
            } else {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10_normal)
                if (selectedType == -1) {
                    this.root.alpha = 1f
                } else {
                    this.root.alpha = 0.5f
                }
            }
        }


        binding.lytLowMovement.apply {
            binding.lytLowMovement.tvHeader.text = getString(R.string.text_low_movement)
            compareChart.updateInitData(1, requireContext().getColor(R.color.low_movement_color))
            binding.lytLowMovement.compareChart.setDrawData(
                combinedData ?: ArrayList(), isSelectedMode
            )

            if (selectedType == 1) {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10)
                this.root.alpha = 1.0f
            } else {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10_normal)
                if (selectedType == -1) {
                    this.root.alpha = 1f
                } else {
                    this.root.alpha = 0.5f
                }
            }
        }

        binding.lytNoMovement.apply {
            binding.lytNoMovement.tvHeader.text = getString(R.string.text_no_movement)
            compareChart.updateInitData(0, requireContext().getColor(R.color.no_movement_color))
            binding.lytNoMovement.compareChart.setDrawData(
                combinedData ?: ArrayList(), isSelectedMode
            )

            if (selectedType == 0) {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10)
                this.root.alpha = 1.0f
            } else {
                this.lytMain.setBackgroundResource(R.drawable.back_modal_new_10_normal)
                if (selectedType == -1) {
                    this.root.alpha = 1f
                } else {
                    this.root.alpha = 0.5f
                }
            }
        }

    }

    private fun handleMovementViews(isOpen: Boolean = false) {
        if (isOpen) {
            binding.viewOpen.gone()
            binding.ivOpen.gone()
            binding.tvCompareHeader.visible()
            binding.lytHighMovement.root.visible()
            binding.lytMediumMovement.root.visible()
            binding.lytLowMovement.root.visible()
            binding.lytNoMovement.root.visible()
            binding.viewClose.visible()
            binding.ivClose.visible()
        } else {
            binding.viewOpen.visible()
            binding.ivOpen.visible()
            binding.tvCompareHeader.gone()
            binding.lytHighMovement.root.gone()
            binding.lytMediumMovement.root.gone()
            binding.lytLowMovement.root.gone()
            binding.lytNoMovement.root.gone()
            binding.viewClose.gone()
            binding.ivClose.gone()
        }
    }

    private fun initCombineChart(dayData: ServerUserHealthData) {
        binding.lytStressMidGraph.graphStress.enableInteractiveMode(true)
        binding.lytStressMidGraph.graphStress.updateData(
            viewModel.oreoStressDataConvertor.getStressCombinedData(
                dayData
            )
        )
    }

}



