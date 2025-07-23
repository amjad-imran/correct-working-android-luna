package com.oreo.ui.circadianAlignment

import android.os.Bundle
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCircadianAlignmentBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.CircadianGraphModel
import com.oreo.data.model.CircadianMidPointModel
import com.oreo.data.model.CircadianMidPointStatus
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CircadianAlignmentFrag :
    BaseFragment<FragmentCircadianAlignmentBinding>(FragmentCircadianAlignmentBinding::inflate) {

    private val viewModel: CircadianAlignmentViewModel by viewModels()

    private val correctiveActivitiesAdapter by lazy {
        CorrectiveActivitiesAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUi()
        setRecycler()
        setCircadianGraph()
        correctiveActivitiesAdapter.updateDataSet(viewModel.prepareCorrectiveActivitiesData())
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
            "2025-07-23 02:30:00",
            "2025-07-23 01:24:18",
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
        val avgNowIndex = viewModel.getMidPointIndex(newStartDateTime, circadianMidPointDateTime)
        val avgBeforeIndex = viewModel.getMidPointIndex(newStartDateTime, avgBeforeMidPointDateTime)


        LOGS.d(
            "sadhsadhkjksdahksdaksdak " +
                    "avgNowIndex ${avgNowIndex} " +
                    " avgBeforeIndex ${avgBeforeIndex} "
        )

        val midStartIndex = viewModel.getMidPointIndex(newStartDateTime, midStartDateTime)
        val midEndIndex = viewModel.getMidPointIndex(newStartDateTime, midEndDateTime)
        val gradMidIndex = (midEndIndex - midStartIndex) / 2

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
        val avgBeforeMidPoint: CircadianMidPointModel? = viewModel.whiteMidPoint("Avg Before")


        val bgRange = IntArray(totalBars) { it }
        val phaseRange = (midStartIndex..midEndIndex).toList().toIntArray()

        val phaseState = viewModel.phaseState(bgRange, phaseRange, avgBeforeIndex, avgNowIndex)

        when (phaseState.second) {
            CircadianMidPointStatus.Locked -> {
                avgNowMidPoint = viewModel.whiteMidPoint("Avg Now")
            }

            CircadianMidPointStatus.Maintained -> {
                avgNowMidPoint = viewModel.orangeMidPoint("Avg Now")
            }

            CircadianMidPointStatus.Worsening -> {
                avgNowMidPoint = viewModel.redMidPoint("Avg Now")
            }

            CircadianMidPointStatus.Correcting -> {
                avgNowMidPoint = viewModel.greenMidPoint("Avg Now")
            }

            CircadianMidPointStatus.SleepMissing -> {
                avgNowMidPoint = viewModel.whiteMidPoint("Avg Now")
            }

            CircadianMidPointStatus.AwaitingSync -> {
                avgNowMidPoint = viewModel.whiteMidPoint("Avg Now")
            }
        }


        if (avgBeforeIndex < avgNowIndex) {
            circadianGraphModelList.getOrNull(avgBeforeIndex)?.let {
                it.firstMidPoint = avgBeforeMidPoint
            }
            circadianGraphModelList.getOrNull(avgNowIndex)?.let {
                it.secondMidPoint = avgNowMidPoint
            }
        } else {
            circadianGraphModelList.getOrNull(avgBeforeIndex)?.let {
                it.secondMidPoint = avgBeforeMidPoint
            }
            circadianGraphModelList.getOrNull(avgNowIndex)?.let {
                it.firstMidPoint = avgNowMidPoint
            }
        }


        LOGS.d("jasdlsajdljsadljsdaklsajdjsadlk ${phaseState.first} ${phaseState.second}")
        LOGS.d("jasdlsajdljsadljsdaklsajdjsadlk midStartIndex ${midStartIndex} midEndIndex ${midEndIndex}")


        val colors = List(totalBars) {
            when (it) {
                midStartIndex ->{
                    "#aa8866".toColorInt()
                }
                midEndIndex ->{
                    "#7799cc".toColorInt()
                }
                else -> "#1CFFFFFF".toColorInt()
//                in 0..midStartIndex - 1 -> "#444444".toColorInt()
//                in midStartIndex..midEndIndex - gradMidIndex -> "#aa8866".toColorInt()
//                in midEndIndex - gradMidIndex..midEndIndex -> "#7799cc".toColorInt()
//                else -> "#333333".toColorInt()
            }
        }


        graphView.updateBars(circadianGraphModelList, colors)
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

    }

}