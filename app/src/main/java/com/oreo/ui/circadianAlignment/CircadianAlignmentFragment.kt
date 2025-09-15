package com.oreo.ui.circadianAlignment

import android.animation.ArgbEvaluator
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCircadianAlignmentBinding
import com.noisefit.util.CircadianMidPointGraphUtils
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.model.circadian.CircadianGraphData
import com.noisefit_commans.data.model.circadian.CircadianMidPointData
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.CircadianGraphModel
import com.oreo.data.model.CircadianMidPointModel
import com.oreo.data.model.CircadianMidPointState
import com.oreo.data.model.CircadianMidPointStatus
import com.oreo.data.model.circadian.CircadianResponseModel
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.stress.help.StressInfoCardAction
import com.oreo.ui.stress.help.StressUnderstandingImageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@AndroidEntryPoint
class CircadianAlignmentFragment :
    BaseFragment<FragmentCircadianAlignmentBinding>(FragmentCircadianAlignmentBinding::inflate) {

    private val viewModel: CircadianAlignmentViewModel by viewModels()

    private val correctiveActivitiesAdapter by lazy {
        CorrectiveActivitiesAdapter() {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.insight_log,
                HashMap<String, Any>().apply {
                    this["source"] = "circadian"
                    this["target"] = when {
                        it.key.equals(CircadianAlignmentViewModel.light_exposure_key) -> "light"
                        it.key.equals(CircadianAlignmentViewModel.meal_window_key) -> "meal"
                        it.key.equals(CircadianAlignmentViewModel.caffeine_window_key) -> "caffeine"
                        else -> ""
                    }
                }
            )
            navigate(
                R.id.addActivityTimelineFragment,
                bundleOf(
                    "showTimeline" to false,
                    "key" to it.key,
                    "srcKey" to "circadian",
                )
            )
        }
    }

    private val howItWorksAdapter: StressUnderstandingImageAdapter by lazy {
        StressUnderstandingImageAdapter(object : StressInfoCardAction {
            override fun onStressInfoCardClicked() {}

            override fun onCircadianCardClicked(pos: Int) {
                navigate(
                    R.id.circadianHowItWorksDetailFragment,
                    bundleOf("position" to pos)
                )
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
    }

    private fun showCircularScheduler(graphData: CircadianGraphData?, isLocked: Boolean?) {
        /*binding.lytCircularView.lockedGroup.visible()
        binding.lytCircularView.circularView.gone()*/

        if (isLocked == true) {
            binding.lytCircularView.apply {
                lytUnlockedState.root.gone()
                lytLockedState.root.visible()
            }
        } else {
            binding.lytCircularView.lytLockedState.root.gone()


            viewModel.viewModelScope.launch(Dispatchers.IO) {
                val clockEvents = viewModel.generateClockEvents(graphData)
                val energyValues = viewModel.getEnergyValues(graphData, false)
                val sleepStart = graphData?.sleepData?.bedTime
                val sleepEnd = graphData?.sleepData?.wakeTime
                withContext(Dispatchers.Main) {
                    binding.lytCircularView.lytUnlockedState.circularView.setDataSet(
                        clockEvents,
                        energyValues,
                        sleepStart,
                        sleepEnd,
                        graphData?.sleepData == null
                    )
                }
            }


            binding.lytCircularView.lytUnlockedState.lytNoSleepData
                .setVisibilityByCondition(graphData?.sleepData == null)
            binding.lytCircularView.lytUnlockedState.ivEllipse.setVisibilityByCondition(graphData?.sleepData == null)

            binding.lytCircularView.lytUnlockedState.root.visible()
        }
    }

    data class CircadianResponse(
        val startTime: String,
        val endTime: String,
        val circadianMidpoint: String,
        val avgBefore: String,
        val avgNow: String,
        /*var nudge: String?*/
    )


    private fun setCircadianGraph() {

        binding.lytGraphView.graphView.isScrollLocked = false
        binding.lytGraphView.graphView.graphStartTime = LocalTime.of(6, 0)
        binding.lytGraphView.graphView.graphEndTime = LocalTime.of(23, 0)

        binding.lytGraphView.graphView.timeWindows = ArrayList()//viewModel.dummyList()//
        binding.lytGraphView.graphView.redraw()

    }

    private fun setCircadianMidPointGraph(circadianResponse: CircadianResponse, sidePaddingMinutes: Int) {
        if (viewModel.personChronotype == null) {
            binding.lytSleepMidPoint.tvChorotype.gone()
        } else {
            val fullText = (
                    getString(R.string.text_chronotype) +
                            viewModel.personChronotype
                    ).uppercase()

            val spannable = SpannableString(fullText)
            spannable.setSpan(
                ForegroundColorSpan("#CCFFFFFF".toColorInt()),
                0,
                getString(R.string.text_chronotype).length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            binding.lytSleepMidPoint.tvChorotype.apply {
                text = spannable
                visible()
            }
        }
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

            val midStartDateTime = LocalDateTime.parse(circadianResponse.startTime, formatter)
            val midEndDateTime = LocalDateTime.parse(circadianResponse.endTime, formatter)

            val interval = 10
            val padBars = kotlin.math.ceil(sidePaddingMinutes / 10.0).toLong()
            val padMinutesAligned = padBars * interval

            val startRemainder = midStartDateTime.minute % interval
            val startAlignedTo10 = midStartDateTime.minusMinutes(startRemainder.toLong())
            val endRemainderInt = (interval - (midEndDateTime.minute % interval)).let { if (it == interval) 0 else it }
            val endAlignedTo10 = midEndDateTime.plusMinutes(endRemainderInt.toLong())

            val newStartDateTime = startAlignedTo10.minusMinutes(padMinutesAligned)
            val newEndDateTime = endAlignedTo10.plusMinutes(padMinutesAligned)

            val circadianMidPointDateTime =
                LocalDateTime.parse(circadianResponse.avgNow, formatter)
            val avgBeforeMidPointDateTime =
                LocalDateTime.parse(circadianResponse.avgBefore, formatter)

            val isSameDay = newStartDateTime.toLocalDate() == newEndDateTime.toLocalDate()

            val totalMinutes = ChronoUnit.MINUTES.between(newStartDateTime, newEndDateTime).toInt()
            val totalBarsCalculated = (totalMinutes / 10).coerceAtLeast(1)

            LOGS.d("sdfjhsdkfj bars:$totalBarsCalculated - $newStartDateTime - $newEndDateTime - $isSameDay")

            binding.lytSleepMidPoint.circadianGraph.updateTotalBars(totalBarsCalculated)
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
            val avgNowOffsetMin = ChronoUnit.MINUTES.between(newStartDateTime, circadianMidPointDateTime).toInt()
            val avgBeforeOffsetMin = ChronoUnit.MINUTES.between(newStartDateTime, avgBeforeMidPointDateTime).toInt()


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
                    avgNowIndex,
                    binding.root.context
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
                    binding.lytSleepMidPoint.tvDesc.text =
                        getString(R.string.text_you_re_in_sync_keep_up_the_good_sleep_habits_to_stay_aligned)
                }

                CircadianMidPointStatus.Worsening -> {
                    setMidPointGraphData(
                        R.drawable.ic_worsening,
                        "#FF8A8A",
                        getString(R.string.text_worsening)
                    )


                    avgNowMidPoint = CircadianMidPointGraphUtils.redMidPoint("Avg Now")
                    binding.lytSleepMidPoint.tvDesc.text = phaseState.third
                        ?: getString(R.string.text_your_rhythm_shifted_later_try_dimming_lights_and_reducing_screen_time_before_bed_to_realign)
                }

                CircadianMidPointStatus.Correcting -> {
                    setMidPointGraphData(
                        R.drawable.ic_correcting,
                        "#63FFB6",
                        getString(R.string.text_correcting)
                    )

                    avgNowMidPoint = CircadianMidPointGraphUtils.greenMidPoint("Avg Now")

                    binding.lytSleepMidPoint.tvDesc.text = phaseState.third
                        ?: getString(R.string.text_circadian_mid_point_desc_correcting)
                }

                CircadianMidPointStatus.SleepMissing -> {
                    setMidPointGraphData(
                        R.drawable.ic_waiting_for_sleep,
                        "#A7ACFF",
                        getString(R.string.text_missing_sleep)
                    )

                    avgNowMidPoint = CircadianMidPointGraphUtils.whiteMidPoint("Avg Now")

                    binding.lytSleepMidPoint.tvDesc.text =
                        getString(R.string.text_circadian_mid_point_desc_missing_sleep)
                }

                CircadianMidPointStatus.AwaitingSync -> {
                    setMidPointGraphData(
                        R.drawable.ic_waiting_for_sleep,
                        "#A7ACFF",
                        getString(R.string.text_awaiting_sync)
                    )

                    avgNowMidPoint = CircadianMidPointGraphUtils.whiteMidPoint("Avg Now")

                    binding.lytSleepMidPoint.tvDesc.text =
                        getString(R.string.text_circadian_mid_point_desc_awaiting_sync)
                }
            }

            // Record bar indices so the view can order overlapping labels chronologically
            avgNowMidPoint?.index = avgNowIndex
            avgBeforeMidPoint?.index = avgBeforeIndex
            avgNowMidPoint?.minutesFromStart = avgNowOffsetMin
            avgBeforeMidPoint?.minutesFromStart = avgBeforeOffsetMin

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
        isLockedCircularView: Boolean?,
        graphData: CircadianGraphData?,
        circadianMidPoint: CircadianMidPointData?
    ) {
        if (
            isLockedCircularView != true && graphData?.startTime != null && graphData.endTime != null &&
            graphData.sleepData?.wakeTime != null && graphData.sleepData?.bedTime != null
        ) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

            binding.lytGraphView.graphView.graphStartTime = LocalTime.of(6, 0)
            binding.lytGraphView.graphView.graphEndTime = LocalTime.of(23, 0)

            val startDateTime = LocalDateTime.parse(graphData.startTime, formatter)
            val endDateTime = LocalDateTime.parse(graphData.endTime, formatter)

            val startTime = LocalTime.of(startDateTime.hour, startDateTime.minute)
            var endTime = LocalTime.of(endDateTime.hour, endDateTime.minute)

            val endDateTimeSleep = LocalDateTime.parse(graphData.sleepData?.wakeTime, formatter)

            if (endDateTimeSleep != null) {
                val sleepWakeTime = LocalTime.of(endDateTimeSleep.hour, endDateTimeSleep.minute)
                endTime = sleepWakeTime
            }

            binding.lytGraphView.graphView.graphStartTime = startTime
            binding.lytGraphView.graphView.graphEndTime = endTime

            viewModel.viewModelScope.launch(Dispatchers.IO) {
                val energyValues = viewModel.getEnergyValues(graphData, true)
                withContext(Dispatchers.Main) {
                    binding.lytGraphView.graphView.setDataSet(
                        viewModel.getScrollGraphList(graphData),
                        energyValues/*graphData.energyGraph*/
                    )
                }
            }



            binding.divider24HourGraph.root.visible()
            binding.tvDetailedOverview.visible()
            binding.lytGraphView.root.visible()
            binding.lytGraphView.graphView.redraw()
        } else {
            binding.divider24HourGraph.root.gone()
            binding.tvDetailedOverview.gone()
            binding.lytGraphView.root.gone()
        }

        // Circadian Mid - Point
        when {
            circadianMidPoint == null -> {
                binding.lytSleepMidPoint.root.gone()
                binding.lytLockedSleepMidPoint.apply {
                    textView178.text = getString(R.string.text_we_need_more_data_to_better_know_you)
                    textView173.text =
                        getString(R.string.text_we_haven_t_seen_enough_recent_sleep_data_to_show_your_circadian_rhythm_wearing_your_ring_consistently_will_help_unlock_personalized_insights)
                    root.visible()
                }
            }

            circadianMidPoint.avgBefore == null -> {
                binding.lytSleepMidPoint.root.gone()
                binding.lytLockedSleepMidPoint.apply {
                    textView178.text = getString(R.string.text_tuning_in_to_your_body_s_clock)
                    textView173.text =
                        getString(R.string.text_we_re_learning_your_unique_circadian_rhythm_keep_sleeping_with_your_luna_ring_to_unlock_your_body_s_natural_timing)
                    root.visible()
                }
            }

            else -> {
                binding.lytLockedSleepMidPoint.root.gone()
                binding.lytSleepMidPoint.root.visible()
                val circadianMidPointResponse = CircadianResponse(
                    circadianMidPoint.startTime ?: "",
                    circadianMidPoint.endTime ?: "",
                    circadianMidPoint.circadianMidpoint ?: "",
                    circadianMidPoint.avgBefore ?: "",
                    circadianMidPoint.avgNow ?: "",
                    /*null,*/
                )
                setCircadianMidPointGraph(circadianMidPointResponse, sidePaddingMinutes = 120)
            }
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

        binding.rvHowItWorks.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHowItWorks.adapter = howItWorksAdapter

        howItWorksAdapter.setDataSet(viewModel.getHowItWorksData())
        binding.dividerHowItWorks.root.visible()
        binding.lytHowItWorks.visible()
    }

    override fun initListener() {

        binding.lytCircularView.lytUnlockedState.lytNoSleepData.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.insight_log,
                HashMap<String, Any>().apply {
                    this["source"] = "circadian"
                    this["target"] = "sleep"
                }
            )
            navigate(
                R.id.addActivityTimelineFragment,
                bundleOf(
                    "showTimeline" to false,
                    "key" to "sleep",
                )
            )
        }

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytCorrectiveActivities.viewAllLogs.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.insight_clicked,
                HashMap<String, Any>().apply {
                    this["source"] = "circadian"
                    this["target"] = "timeline"
                }
            )
            navigate(
                R.id.timelineScreenFragment,
                bundleOf("srcKey" to "circadian")
            )
        }

        binding.lytYourChronotype.tvRetakeQuiz.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.quiz_restarted,
                HashMap<String, Any>().apply {
                    this["source"] = "circadian"
                }
            )
            navigate(R.id.quizCircadianFragment)
        }

        binding.lytFocusWindow.llLunaAi.setOnClickListener {
            /* if (viewModel.isChatSplashShown()) {*/
            viewModel.sessionManager.logMoEngageAppEvent(
                MoEngageLunaAppEvents.aichat_initiated_clicked,
                HashMap<String, Any>().apply {
                    this["source"] = "circadian"
                }
            )
            navigate(
                R.id.aiTopQuestionsFragment,
                bundleOf("aiTopic" to AITopics.CIRCADIAN)
            )
            /* } else {
                 navigate(R.id.aiChatOnboardFragment)
             }*/
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
        //showCircularScheduler(null)
        viewModel.circadianResponseData.observe(viewLifecycleOwner) {
            binding.mainScrollView.visible()
            LOGS.d("abcjacjcab Observing data: $it")
            setData(it)


            val string =
                "{\"start_time\": \"2025-09-02 04:00:00\", \"end_time\": \"2025-09-02 06:00:00\", \"circadian_midpoint\": \"2025-09-02 03:56:00\", \"avg_now\": \"2025-09-02 03:55:30\", \"avg_before\": \"2025-09-02 03:57:48\", \"chronotype\": \"Moderately evening type\" }"
            val json = Gson().fromJson<CircadianMidPointData>(string)

            updateGraph(it.isLockedCircularView, it.graphData,
                it.graphData?.circadianMidPointData)
            showCircularScheduler(it.graphData, it.isLockedCircularView)
        }

        viewModel.nudgeData.observe(viewLifecycleOwner) {
            val showShimmer = it.second
            if (showShimmer) {
                //binding.lytFocusWindow.progressBarFocusWindow.root.visible()
                binding.lytFocusWindow.shimmerLayout.startShimmer()
            } else {
                // binding.lytFocusWindow.progressBarFocusWindow.root.gone()
                binding.lytFocusWindow.shimmerLayout.stopShimmer()
                binding.lytFocusWindow.shimmerLayout.gone()
                binding.lytFocusWindow.apply {
                    tvTitle.text = it.first?.title ?: "-"
                    tvDesc.text = it.first?.description ?: "-"
                }
            }
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
    }

    fun setData(data: CircadianResponseModel) {
        LOGS.d("ansckaasc: $data")
        // lyt Circular State
        if (data.isLockedCircularView == true || data.graphData?.sleepData == null) {
            binding.lytCircularState.root.gone()
        } else {
            viewModel.getCaffeineState(data)?.let {
                binding.lytCircularState.apply {
                    tvCaffeineState.text = it
                    root.visible()
                }
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
            tvType.text = viewModel.personChronotype ?: "-"

            chronotypeData?.introduction?.let {
                tvIntro.apply {
                    text = it
                    visible()
                }
            }

            tvDescType.text = chronotypeData?.description ?: "-"

            /*tvRetakeQuiz.text = if (viewModel.localDataStore.isCircadianOnboardShown()) {
                getString(R.string.text_retake_chronotype_quiz)
            } else {
                getString(R.string.text_take_quiz)
            }*/
        }
    }

    private fun getActMoniStatusIcon(status: String?): Int {
        return when (status) {
            "partial" -> R.drawable.ic_partially_done_circadian
            "done" -> R.drawable.ic_hm_check_mark
            "not-done" -> R.drawable.ic_not_done_circadian
            else -> R.drawable.ic_hm_check_default_circadian
        }
    }

}
