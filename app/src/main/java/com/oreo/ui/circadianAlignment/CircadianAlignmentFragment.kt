package com.oreo.ui.circadianAlignment

import android.animation.ArgbEvaluator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCircadianAlignmentBinding
import com.noisefit.util.CircadianMidPointGraphUtils
import com.noisefit_commans.data.model.circadian.CircadianGraphData
import com.noisefit_commans.data.model.circadian.CircadianMidPointData
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.CircadianGraphModel
import com.oreo.data.model.CircadianMidPointModel
import com.oreo.data.model.CircadianMidPointState
import com.oreo.data.model.CircadianMidPointStatus
import com.oreo.data.model.circadian.CircadianResponseModel
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.custom.ClockEvent
import com.oreo.ui.custom.ClockEventType
import com.oreo.ui.stress.help.StressInfoCardAction
import com.oreo.ui.stress.help.StressUnderstandingImageAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CircadianAlignmentFragment :
    BaseFragment<FragmentCircadianAlignmentBinding>(FragmentCircadianAlignmentBinding::inflate) {

    private val viewModel: CircadianAlignmentViewModel by viewModels()

    private val correctiveActivitiesAdapter by lazy {
        CorrectiveActivitiesAdapter() {
            navigate(
                R.id.addActivityTimelineFragment,
                bundleOf(
                    "key" to it.key,
                )
            )
        }
    }

    private val howItWorksAdapter: StressUnderstandingImageAdapter by lazy {
        StressUnderstandingImageAdapter(object : StressInfoCardAction {
            override fun onStressInfoCardClicked() {
//                navigate(R.id.stressUnderstandingFragment)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUi()
        setCircadianGraph()
        setRecycler()
        initListener()
        subscribeObservers()
        viewModel.initData()

        showCircularScheduler()
    }

    private fun showCircularScheduler() {
        binding.lytCircularView.lockedGroup.visible()
        binding.lytCircularView.circularView.gone()
        val clockEvents = listOf(
            ClockEvent(6f, 12f, ClockEventType.ARCH, Color.parseColor("#B4E6EC"), Color.parseColor("#FBE0BE"), "Natural Light"),
            ClockEvent(17f, 19f, ClockEventType.ARCH, Color.parseColor("#55313E"),
                Color.parseColor("#995CA0"), "Wind-Down"),
            ClockEvent(19f, 22f, ClockEventType.ARCH, Color.parseColor("#8F5EBA"),
                Color.parseColor("#443A7B"), "Dim-light"),
            ClockEvent(6f, 23f, ClockEventType.GRAPH, Color.parseColor("#B5845D"),Color.parseColor("#B5845D"), ""),
            ClockEvent(8f, 13f, ClockEventType.LINE, Color.parseColor("#B5845D"),Color.parseColor("#B5845D"), ""),
            //ClockEvent(23f, 24f, Color.parseColor("#7C3AED"), "Dim-Light"),
            //ClockEvent(0f, 6f, Color.parseColor("#5B21B6"), "Sleep"),
            //ClockEvent(8f, 18f, Color.parseColor("#FDE68A"), "Neutral Light"),
        )
        binding.lytCircularView.circularView.events = clockEvents
    }

    data class CircadianResponse(
        val startTime: String,
        val endTime: String,
        val circadianMidpoint: String,
        val avgBefore: String,
        /*var nudge: String?*/
    )


    private fun setCircadianGraph() {

        binding.graphView.isScrollLocked = false
        binding.graphView.graphStartTime = LocalTime.of(6, 0)
        binding.graphView.graphEndTime = LocalTime.of(23, 0)

        binding.graphView.timeWindows = ArrayList()//viewModel.dummyList()//
        binding.graphView.redraw()

        /*val circadianResponse = CircadianResponse(
            "2025-07-22 23:39:00",
            "2025-07-23 02:15:00",
            "2025-07-23 01:34:00",
            "2025-07-23 01:44:18",
            "You’re improving — staying active later and delaying sleep cues is helping."
        )
        setCircadianMidPointGraph(circadianResponse)*/

    }

    private fun setCircadianMidPointGraph(circadianResponse: CircadianResponse) {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

            val midStartDateTime = LocalDateTime.parse(circadianResponse.startTime, formatter)
            val midEndDateTime = LocalDateTime.parse(circadianResponse.endTime, formatter)
            val newStartDateTime = LocalDateTime
                .parse(circadianResponse.startTime, formatter)
                .minusHours(2)
                .minusMinutes(midStartDateTime.minute.toLong())
            val newEndDateTime = LocalDateTime
                .parse(circadianResponse.endTime, formatter)
                .plusHours(2)
                .plusMinutes((60 - midEndDateTime.minute.toLong()))
            val circadianMidPointDateTime =
                LocalDateTime.parse(circadianResponse.circadianMidpoint, formatter)
            val avgBeforeMidPointDateTime =
                LocalDateTime.parse(circadianResponse.avgBefore, formatter)

            val isSameDay = newStartDateTime.toLocalDate() == LocalDate.now()

            val totalHrs = if (!isSameDay) {
                val hoursFromStartToMidnight = 24 - newStartDateTime.hour
                val hoursFromMidnightToEnd = newEndDateTime.hour
                hoursFromStartToMidnight + hoursFromMidnightToEnd
            } else {
                newEndDateTime.hour - newStartDateTime.hour
            }

            binding.lytSleepMidPoint.circadianGraph.updateTotalHours(totalHrs)
            val totalBars = binding.lytSleepMidPoint.circadianGraph.totalBars()
            val graphView = binding.lytSleepMidPoint.circadianGraph
            val avgNowIndex = CircadianMidPointGraphUtils.getMidPointIndex(
                newStartDateTime,
                circadianMidPointDateTime
            )
            val avgBeforeIndex = CircadianMidPointGraphUtils.getMidPointIndex(
                newStartDateTime,
                avgBeforeMidPointDateTime
            )


            LOGS.d(
                "sadhsadhkjksdahksdaksdak " +
                        "avgNowIndex ${avgNowIndex} " +
                        " avgBeforeIndex ${avgBeforeIndex} "
            )

            val midStartIndex =
                CircadianMidPointGraphUtils.getMidPointIndex(newStartDateTime, midStartDateTime)
            val midEndIndex =
                CircadianMidPointGraphUtils.getMidPointIndex(newStartDateTime, midEndDateTime)


            val circadianGraphModelList = ArrayList<CircadianGraphModel>()
            for (index in 0..totalBars - 1) {
                var xAxis: String? = null


                when (index) {
                    0 -> {
                        xAxis = newStartDateTime.format(timeFormatter)
                    }

                    midStartIndex -> {
                        xAxis = midStartDateTime.format(timeFormatter)
                    }

                    midEndIndex -> {
                        xAxis = midEndDateTime.format(timeFormatter)
                    }

                    totalBars - 1 -> {
                        xAxis = newEndDateTime.format(timeFormatter)
                    }
                }

                circadianGraphModelList.add(
                    CircadianGraphModel(
                        xAxis = xAxis
                    )
                )
            }

            var avgNowMidPoint: CircadianMidPointModel? = null
            val avgBeforeMidPoint: CircadianMidPointModel? =
                CircadianMidPointGraphUtils.whiteMidPoint("Avg Before")

            val bgRange = IntArray(totalBars) { it }
            val phaseRange = (midStartIndex..midEndIndex).toList().toIntArray()

            val phaseState =
                CircadianMidPointGraphUtils.phaseState(
                    bgRange,
                    phaseRange,
                    avgBeforeIndex,
                    avgNowIndex
                )
            setMidPointGraphState(phaseState.first)
            when (phaseState.second) {
                CircadianMidPointStatus.Locked -> {
                    avgNowMidPoint = CircadianMidPointGraphUtils.whiteMidPoint("Avg Now")
                }

                CircadianMidPointStatus.Maintained -> {
                    setMidPointGraphData(
                        R.drawable.ic_maintained,
                        "#FFB963",
                        getString(R.string.text_maintained)
                    )
                    avgNowMidPoint = CircadianMidPointGraphUtils.orangeMidPoint("Avg Now")
                }

                CircadianMidPointStatus.Worsening -> {
                    setMidPointGraphData(
                        R.drawable.ic_worsening,
                        "#FF8A8A",
                        getString(R.string.text_worsening)
                    )


                    avgNowMidPoint = CircadianMidPointGraphUtils.redMidPoint("Avg Now")
                }

                CircadianMidPointStatus.Correcting -> {
                    setMidPointGraphData(
                        R.drawable.ic_correcting,
                        "#63FFB6",
                        getString(R.string.text_correcting)
                    )

                    avgNowMidPoint = CircadianMidPointGraphUtils.greenMidPoint("Avg Now")

                    binding.lytSleepMidPoint.textView173.text =
                        getString(R.string.text_circadian_mid_point_desc_correcting)
                }

                CircadianMidPointStatus.SleepMissing -> {
                    setMidPointGraphData(
                        R.drawable.ic_waiting_for_sleep,
                        "#A7ACFF",
                        getString(R.string.text_missing_sleep)
                    )

                    avgNowMidPoint = CircadianMidPointGraphUtils.whiteMidPoint("Avg Now")
                }

                CircadianMidPointStatus.AwaitingSync -> {
                    setMidPointGraphData(
                        R.drawable.ic_waiting_for_sleep,
                        "#A7ACFF",
                        getString(R.string.text_awaiting_sync)
                    )

                    avgNowMidPoint = CircadianMidPointGraphUtils.whiteMidPoint("Avg Now")
                }
            }

            //-4 -2

            if (avgBeforeIndex < 0 && avgNowIndex < 0) {
                // Use indices 0 and 1 for drawing since both are off-graph
                binding.lytSleepMidPoint.leftCdArrow.visible()
                graphView.drawOnSameIndex = (avgNowIndex == avgBeforeIndex)
                if (graphView.drawOnSameIndex) {
                    circadianGraphModelList.getOrNull(0)?.bothMidPoint =
                        Pair(avgBeforeMidPoint, avgNowMidPoint)
                } else {
                    val firstIndex = 0
                    val secondIndex = 1

                    if (avgNowIndex < avgBeforeIndex) {
                        assignMidpoints(circadianGraphModelList, firstIndex, avgNowMidPoint, null)
                        assignMidpoints(
                            circadianGraphModelList,
                            secondIndex,
                            null,
                            avgBeforeMidPoint
                        )
                    } else {
                        assignMidpoints(
                            circadianGraphModelList,
                            firstIndex,
                            avgBeforeMidPoint,
                            null
                        )
                        assignMidpoints(circadianGraphModelList, secondIndex, null, avgNowMidPoint)
                    }
                }


            } else if (avgBeforeIndex > bgRange.last() && avgNowIndex > bgRange.last()) {
                binding.lytSleepMidPoint.rightCdArrow.visible()
                // Use indices 0 and 1 for drawing since both are off-graph
                graphView.drawOnSameIndex = (avgNowIndex == avgBeforeIndex)
                val secondIndex = totalBars - 1
                if (graphView.drawOnSameIndex) {
                    circadianGraphModelList.getOrNull(secondIndex)?.bothMidPoint =
                        Pair(avgBeforeMidPoint, avgNowMidPoint)
                } else {
                    val firstIndex = totalBars - 2
                    val secondIndex = totalBars - 1

                    if (avgNowIndex < avgBeforeIndex) {
                        assignMidpoints(circadianGraphModelList, firstIndex, avgNowMidPoint, null)
                        assignMidpoints(
                            circadianGraphModelList,
                            secondIndex,
                            null,
                            avgBeforeMidPoint
                        )
                    } else {
                        assignMidpoints(
                            circadianGraphModelList,
                            firstIndex,
                            avgBeforeMidPoint,
                            null
                        )
                        assignMidpoints(circadianGraphModelList, secondIndex, null, avgNowMidPoint)
                    }
                }


            } else {
                graphView.drawOnSameIndex = (avgNowIndex == avgBeforeIndex)
                if (graphView.drawOnSameIndex) {
                    circadianGraphModelList.getOrNull(avgBeforeIndex)?.bothMidPoint =
                        Pair(avgBeforeMidPoint, avgNowMidPoint)
                } else {
                    val firstIndex = minOf(avgBeforeIndex, avgNowIndex)
                    val secondIndex = maxOf(avgBeforeIndex, avgNowIndex)

                    val firstMidPoint =
                        if (avgBeforeIndex < avgNowIndex) avgBeforeMidPoint else avgNowMidPoint
                    val secondMidPoint =
                        if (avgBeforeIndex < avgNowIndex) avgNowMidPoint else avgBeforeMidPoint

                    circadianGraphModelList.getOrNull(firstIndex)?.firstMidPoint = firstMidPoint
                    circadianGraphModelList.getOrNull(secondIndex)?.secondMidPoint = secondMidPoint
                }


            }


            LOGS.d("jasdlsajdljsadljsdaklsajdjsadlk ${phaseState.first} ${phaseState.second}")
            LOGS.d("jasdlsajdljsadljsdaklsajdjsadlk midStartIndex ${midStartIndex} midEndIndex ${midEndIndex}")

            val startColor = Color.parseColor("#795A54")
            val endColor = Color.parseColor("#67809F")

            val evaluator = ArgbEvaluator()
            val barColors = mutableListOf<Int>()

            val midBarCount = (midEndIndex - midStartIndex) + 1
            for (i in 0 until midBarCount) {
                val fraction = i.toFloat() / (midBarCount - 1)
                val color = evaluator.evaluate(fraction, startColor, endColor) as Int
                barColors.add(color)
            }


            var current = 0
            val colors = List(totalBars) {
                when (it) {
                    /*midStartIndex -> {
                        "#aa8866".toColorInt()
                    }

                    midEndIndex -> {
                        "#7799cc".toColorInt()
                    }*/
                    in midStartIndex..midEndIndex -> {
                        try {
                            val pos = current
                            current++
                            barColors[pos]
                        } catch (exp: Exception) {
                            "#67809F".toColorInt()
                        }
                    }

                    else -> "#1CFFFFFF".toColorInt()
                }
            }

            graphView.updateBars(circadianGraphModelList, colors)
        } catch (e: Exception) {
            LOGS.d("circadian mid point exception: $e")
        }
    }

    fun assignMidpoints(
        circadianGraphModelList: ArrayList<CircadianGraphModel>,
        targetIndex: Int,
        first: CircadianMidPointModel?,
        second: CircadianMidPointModel?
    ) {
        circadianGraphModelList.getOrNull(targetIndex)?.apply {
            this.firstMidPoint = first
            this.secondMidPoint = second
        }
    }

    private fun setMidPointGraphState(data: CircadianMidPointState) {
        when (data) {
            CircadianMidPointState.PhaseAligned -> {
                binding.lytSleepMidPoint.tvState.apply {
                    text = getString(R.string.text_phase_aligned)
                    setTextColor("#59E1A5".toColorInt())
                }
            }

            CircadianMidPointState.PhaseDelay -> {
                binding.lytSleepMidPoint.tvState.apply {
                    text = getString(R.string.text_phase_delay)
                    setTextColor("#FF8A8A".toColorInt())
                }
            }

            CircadianMidPointState.PhaseAdvance -> {
                binding.lytSleepMidPoint.tvState.apply {
                    text = getString(R.string.text_phase_advance)
                    setTextColor("#FF8A8A".toColorInt())
                }
            }

            CircadianMidPointState.None -> {
                binding.lytSleepMidPoint.tvState.text = ""
            }
        }
    }

    private fun setMidPointGraphData(icon: Int, color: String, text: String) {
        binding.lytSleepMidPoint.apply {
            imvStatus.setImageResource(icon)
            tvStatus.setTextColor(color.toColorInt())
            tvStatus.text = text
        }
    }

    private fun updateGraph(
        graphData: CircadianGraphData,
        circadianMidPoint: CircadianMidPointData?
    ) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        binding.graphView.graphStartTime = LocalTime.of(6, 0)
        binding.graphView.graphEndTime = LocalTime.of(23, 0)
        if (graphData.startTime != null && graphData.endTime != null) {

            val startDateTime = LocalDateTime.parse(graphData.startTime, formatter)
            val endDateTime = LocalDateTime.parse(graphData.endTime, formatter)

            val startTime = LocalTime.of(startDateTime.hour, startDateTime.minute)
            val endTime = LocalTime.of(endDateTime.hour, endDateTime.minute)

            binding.graphView.graphStartTime = startTime
            binding.graphView.graphEndTime = endTime

            binding.graphView.setDataSet(
                viewModel.getScrollGraphList(graphData),
                graphData.energyGraph
            )
            binding.graphView.redraw()
        }

        if (circadianMidPoint == null) {
            binding.lytSleepMidPoint.root.gone()
            binding.lytLockedSleepMidPoint.root.visible()
        } else {
            binding.lytLockedSleepMidPoint.root.gone()
            binding.lytSleepMidPoint.root.visible()
            val circadianMidPointResponse = CircadianResponse(
                circadianMidPoint.startTime ?: "",
                circadianMidPoint.endTime ?: "",
                circadianMidPoint.circadianMidpoint ?: "",
                circadianMidPoint.avgBefore ?: "",
                /*null,*/
            )
            setCircadianMidPointGraph(circadianMidPointResponse)
        }
    }

    private fun setUi() {
        binding.toolbar.tvTitle.text = getString(R.string.text_circadian_alignment)

        val tvReTakeQuiz = binding.lytYourChronotype.tvRetakeQuiz
        tvReTakeQuiz.paintFlags = tvReTakeQuiz.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setRecycler() {
        binding.lytCorrectiveActivities.recyclerV.layoutManager = LinearLayoutManager(context)
        binding.lytCorrectiveActivities.recyclerV.adapter = correctiveActivitiesAdapter

        viewModel.initHowItWorksData()

        binding.rvHowItWorks.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHowItWorks.adapter = howItWorksAdapter
    }

    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytCorrectiveActivities.viewAllLogs.setOnClickListener {
            navigate(R.id.timelineScreenFragment)
        }

        binding.lytYourChronotype.tvRetakeQuiz.setOnClickListener {
            navigate(R.id.quizCircadianFragment)
        }

        binding.lytFocusWindow.llLunaAi.setOnClickListener {
            if (viewModel.isChatSplashShown()) {
                navigate(
                    R.id.aiTopQuestionsFragment,
                    bundleOf("aiTopic" to AITopics.GENERAL)
                )
            } else {
                navigate(R.id.aiChatOnboardFragment)
            }
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

    override fun subscribeObservers() {
        viewModel.circadianResponseData.observe(viewLifecycleOwner) {
            LOGS.d("abcjacjcab Observing data: $it")
            setData(it)
            it.graphData?.let { it1 -> updateGraph(it1, it.circadianMidPoint) }
        }

        viewModel.correctiveActivitiesListData.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                correctiveActivitiesAdapter.updateDataSet(it)
            }
        }

        viewModel.lightExposureData.observe(viewLifecycleOwner) {
            correctiveActivitiesAdapter.updateSingleElement(it, 0)
        }

        viewModel.dailyStepsData.observe(viewLifecycleOwner) {
            correctiveActivitiesAdapter.updateSingleElement(it, 1)
        }

        viewModel.mealWindowData.observe(viewLifecycleOwner) {
            correctiveActivitiesAdapter.updateSingleElement(it, 2)
        }

        viewModel.workoutData.observe(viewLifecycleOwner) {
            correctiveActivitiesAdapter.updateSingleElement(it, 3)
        }

        viewModel.caffeineWindowData.observe(viewLifecycleOwner) {
            correctiveActivitiesAdapter.updateSingleElement(it, 4)
        }

        viewModel.getMessages().observe(viewLifecycleOwner) {
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

        viewModel.howItWorksDataList.observe(viewLifecycleOwner) {
            howItWorksAdapter.setDataSet(it)
            binding.dividerHowItWorks.root.visible()
            binding.lytHowItWorks.visible()
        }
    }

    fun setData(data: CircadianResponseModel) {
        LOGS.d("ansckaasc: $data")
        // focus window
        val isCircularViewContainsData = true
        binding.lytFocusWindow.apply {
            if (isCircularViewContainsData) {
                tvTitle.text = getString(R.string.text_focus_window)
                tvDesc.text =
                    getString(R.string.text_wear_your_luna_ring_when_you_go_to_bed_to_track_your_sleep_make_sure_to_charge_your_ring_to_avoid_missing_out_valuable_insights)
            } else {
                tvTitle.text = getString(R.string.text_take_it_easy_today)
                tvDesc.text =
                    getString(R.string.text_wear_your_luna_ring_when_you_go_to_bed_to_track_your_sleep_make_sure_to_charge_your_ring_to_avoid_missing_out_valuable_insights)
            }
        }

        // activity monitor
        val activityMonitorData = data.activityMonitor
        binding.lytActivityMonitor.apply {
            activityMonitorData?.forEach {
                when (it.type) {
                    CircadianAlignmentViewModel.daily_steps_key -> {
                        ivStateSteps.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    CircadianAlignmentViewModel.meal_window_key -> {
                        ivStateMeal.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    CircadianAlignmentViewModel.light_exposure_key -> {
                        ivStateLight.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    CircadianAlignmentViewModel.caffeine_window_key -> {
                        ivStateCaffeine.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    CircadianAlignmentViewModel.workout_key -> {
                        ivStateWorkout.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    else -> {}
                }
            }
        }

        // your chronotype
        val chronotypeData = data.chronotype
        binding.lytYourChronotype.apply {
            tvType.text = chronotypeData?.type ?: "-"

            when(chronotypeData?.type){
                getString(R.string.text_definite_morning_type) -> {
                    tvIntro.apply{
                        text = getString(R.string.text_you_re_an_early_riser_by_nature)
                        visible()
                    }
                }

                getString(R.string.text_moderate_morning_type) -> {
                    tvIntro.apply{
                        text =
                            getString(R.string.text_you_feel_best_in_the_first_half_of_the_day)
                        visible()
                    }
                }

                getString(R.string.text_intermediate_type) -> {
                    tvIntro.apply{
                        text =
                            getString(R.string.text_you_follow_a_balanced_day_night_rhythm)
                        visible()
                    }
                }

                getString(R.string.text_moderate_evening_type) -> {
                    tvIntro.apply{
                        text =
                            getString(R.string.text_you_re_naturally_inclined_to_be_a_night_owl)
                        visible()
                    }
                }

                getString(R.string.text_definite_evening_type) -> {
                    tvIntro.apply{
                        text = getString(R.string.text_you_re_a_true_night_owl)
                        visible()
                    }
                }

                else -> tvIntro.gone()
            }

            tvDescType.text = chronotypeData?.description ?: "-"
        }
    }

    private fun getActMoniStatusIcon(status: String?): Int {
        return when (status) {
            CircadianAlignmentViewModel.actMonStatusList[0] -> R.drawable.ic_partially_done_circadian
            CircadianAlignmentViewModel.actMonStatusList[1] -> R.drawable.ic_hm_check_mark
            CircadianAlignmentViewModel.actMonStatusList[2] -> R.drawable.ic_not_done_circadian
            else -> R.drawable.ic_hm_check_default_circadian
        }
    }

}