package com.oreo.ui.circadianAlignment

import android.os.Bundle
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCircadianAlignmentBinding
import com.noisefit.util.CircadianMidPointGraphUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.CircadianGraphModel
import com.oreo.data.model.CircadianMidPointModel
import com.oreo.data.model.CircadianMidPointState
import com.oreo.data.model.CircadianMidPointStatus
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.toColorInt
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.oreo.data.model.circadian.CircadianResponseModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CircadianAlignmentFrag :
    BaseFragment<FragmentCircadianAlignmentBinding>(FragmentCircadianAlignmentBinding::inflate) {

    private val viewModel: CircadianAlignmentViewModel by viewModels()

    private val correctiveActivitiesAdapter by lazy {
        CorrectiveActivitiesAdapter(){
            setFragmentResultListener(LOG_CIRCADIAN_BOTTOM_SHEET_KEY) { _, bundle ->
                val key = bundle.getString("key")
                val isLogged = bundle.getBoolean("isLogged")
                viewModel.postLogData(key, isLogged)
            }
            navigate(R.id.logCircadianBottomSheetFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUi()
        setRecycler()
        setCircadianGraph()
//        correctiveActivitiesAdapter.updateDataSet(viewModel.prepareCorrectiveActivitiesData(it.activities))
    }

    data class CircadianResponse(
        val startTime: String,
        val endTime: String,
        val circadianMidpoint: String,
        val avgBefore: String,
        val nudge: String
    )




    private fun setCircadianGraph() {

        val circadianResponse = CircadianResponse(
            "2025-07-22 23:39:00",
            "2025-07-23 02:15:00",
            "2025-07-23 01:34:00",
            "2025-07-23 01:44:18",
            "You’re improving — staying active later and delaying sleep cues is helping."
        )
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
        val avgBeforeMidPointDateTime = LocalDateTime.parse(circadianResponse.avgBefore, formatter)

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
        val avgNowIndex = CircadianMidPointGraphUtils.getMidPointIndex(newStartDateTime, circadianMidPointDateTime)
        val avgBeforeIndex = CircadianMidPointGraphUtils.getMidPointIndex(newStartDateTime, avgBeforeMidPointDateTime)


        LOGS.d(
            "sadhsadhkjksdahksdaksdak " +
                    "avgNowIndex ${avgNowIndex} " +
                    " avgBeforeIndex ${avgBeforeIndex} "
        )

        val midStartIndex = CircadianMidPointGraphUtils.getMidPointIndex(newStartDateTime, midStartDateTime)
        val midEndIndex = CircadianMidPointGraphUtils.getMidPointIndex(newStartDateTime, midEndDateTime)


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
        val avgBeforeMidPoint: CircadianMidPointModel? = CircadianMidPointGraphUtils.whiteMidPoint("Avg Before")

        val bgRange = IntArray(totalBars) { it }
        val phaseRange = (midStartIndex..midEndIndex).toList().toIntArray()

        val phaseState = CircadianMidPointGraphUtils.phaseState(bgRange, phaseRange, avgBeforeIndex, avgNowIndex)
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
            if(graphView.drawOnSameIndex){
                circadianGraphModelList.getOrNull(0)?.bothMidPoint = Pair(avgBeforeMidPoint,avgNowMidPoint)
            }else{
                val firstIndex = 0
                val secondIndex = 1

               if (avgNowIndex < avgBeforeIndex) {
                    assignMidpoints(circadianGraphModelList,firstIndex, avgNowMidPoint, null)
                    assignMidpoints(circadianGraphModelList,secondIndex, null, avgBeforeMidPoint)
                } else {
                    assignMidpoints(circadianGraphModelList,firstIndex, avgBeforeMidPoint, null)
                    assignMidpoints(circadianGraphModelList,secondIndex, null, avgNowMidPoint)
                }
            }


        } else if (avgBeforeIndex > bgRange.last() && avgNowIndex > bgRange.last()) {
            binding.lytSleepMidPoint.rightCdArrow.visible()
            // Use indices 0 and 1 for drawing since both are off-graph
            graphView.drawOnSameIndex = (avgNowIndex == avgBeforeIndex)
            val secondIndex = totalBars-1
            if(graphView.drawOnSameIndex){
                circadianGraphModelList.getOrNull(secondIndex)?.bothMidPoint = Pair(avgBeforeMidPoint,avgNowMidPoint)
            }else{
                val firstIndex = totalBars-2
                val secondIndex = totalBars-1

               if (avgNowIndex < avgBeforeIndex) {
                    assignMidpoints(circadianGraphModelList,firstIndex, avgNowMidPoint, null)
                    assignMidpoints(circadianGraphModelList,secondIndex, null, avgBeforeMidPoint)
                } else {
                    assignMidpoints(circadianGraphModelList,firstIndex, avgBeforeMidPoint, null)
                    assignMidpoints(circadianGraphModelList,secondIndex, null, avgNowMidPoint)
                }
            }


        } else {
            graphView.drawOnSameIndex = (avgNowIndex == avgBeforeIndex)
            if(graphView.drawOnSameIndex){
                circadianGraphModelList.getOrNull(avgBeforeIndex)?.bothMidPoint = Pair(avgBeforeMidPoint,avgNowMidPoint)
            }else{
                val firstIndex = minOf(avgBeforeIndex, avgNowIndex)
                val secondIndex = maxOf(avgBeforeIndex, avgNowIndex)

                val firstMidPoint = if (avgBeforeIndex < avgNowIndex) avgBeforeMidPoint else avgNowMidPoint
                val secondMidPoint = if (avgBeforeIndex < avgNowIndex) avgNowMidPoint else avgBeforeMidPoint

                circadianGraphModelList.getOrNull(firstIndex)?.firstMidPoint = firstMidPoint
                circadianGraphModelList.getOrNull(secondIndex)?.secondMidPoint = secondMidPoint
            }


        }


        LOGS.d("jasdlsajdljsadljsdaklsajdjsadlk ${phaseState.first} ${phaseState.second}")
        LOGS.d("jasdlsajdljsadljsdaklsajdjsadlk midStartIndex ${midStartIndex} midEndIndex ${midEndIndex}")


        val colors = List(totalBars) {
            when (it) {
                midStartIndex -> {
                    "#aa8866".toColorInt()
                }

                midEndIndex -> {
                    "#7799cc".toColorInt()
                }

                else -> "#1CFFFFFF".toColorInt()
            }
        }

        graphView.updateBars(circadianGraphModelList, colors)
    }

    fun assignMidpoints(
        circadianGraphModelList:ArrayList<CircadianGraphModel>,
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

    private fun setUi() {
        binding.toolbar.tvTitle.text = getString(R.string.text_circadian_alignment)

    }

    private fun setRecycler() {
        binding.lytCorrectiveActivities.recyclerV.layoutManager = LinearLayoutManager(context)
        binding.lytCorrectiveActivities.recyclerV.adapter = correctiveActivitiesAdapter
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytYourChronotype.tvRetakeQuiz.setOnClickListener {
            navigate(R.id.quizCircadianFragment)
        }
    }

    override fun subscribeObservers() {
        viewModel.circadianResponseData.observe(this){
            LOGS.d("abcjacjcab Observing data: $it")
            setData(it)
        }

        viewModel.correctiveActivitiesListData.observe(this){
            if(!it.isNullOrEmpty()){
                correctiveActivitiesAdapter.updateDataSet(it)
            }
        }
    }

    fun setData(data: CircadianResponseModel){
        LOGS.d("ansckaasc: $data")
        // activity monitor
        val activityMonitorData = data.activity_monitor
        binding.lytActivityMonitor.apply {
            activityMonitorData.forEach {
                when(it.type){
                    CircadianAlignmentViewModel.daily_steps_key -> {
                        ivStateSteps.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    CircadianAlignmentViewModel.meal_window_key -> {
                        ivStateLight.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    CircadianAlignmentViewModel.light_exposure_key -> {
                        ivStateLight.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    CircadianAlignmentViewModel.caffeine_window_key -> {
                        ivStateLight.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    CircadianAlignmentViewModel.workout_key -> {
                        ivStateLight.setImageResource(
                            getActMoniStatusIcon(it.status)
                        )
                    }

                    else -> {}
                }
            }
        }

        // corrective activities
        /*val correctiveActivitiesData = data.activities
        binding.lytCorrectiveActivities.apply {
            correctiveActivitiesData.
        }*/

        // your chronotype
        val chronotypeData = data.chronotype
        binding.lytYourChronotype.apply {
            tvTitle.text = chronotypeData.type
            tvDesc.text = chronotypeData.description
        }
    }

    private fun getActMoniStatusIcon(status: String?): Int {
        return when(status){
            CircadianAlignmentViewModel.actMonStatusList.get(0) -> R.drawable.ic_cancel
            CircadianAlignmentViewModel.actMonStatusList.get(1) -> R.drawable.ic_hm_check_mark
            else -> R.drawable.ic_hm_check_default_circadian
        }
    }

}