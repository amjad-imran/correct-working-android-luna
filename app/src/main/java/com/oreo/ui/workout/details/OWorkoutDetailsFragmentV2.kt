package com.oreo.ui.workout.details

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOWorkoutDetailsV2Binding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.ChartModel
import com.oreo.data.model.GraphDummyModel
import com.oreo.data.model.OWDActivityHRZoneData
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.model.SleepChartModel
import com.oreo.data.model.WorkoutTypes
import com.oreo.ui.activity.all.DELETE_WORKOUT_REQUEST_KEY
import com.oreo.ui.custom.LineChartType
import com.oreo.ui.custom.OnHeartRateChartClickAction
import com.oreo.ui.custom.OnLinearChartClickAction
import com.oreo.util.UtilClass
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class OWorkoutDetailsFragmentV2 :
    BaseFragment<FragmentOWorkoutDetailsV2Binding>(FragmentOWorkoutDetailsV2Binding::inflate) {

    private val viewModel: OWorkoutDetailsViewModelV2 by viewModels()
    private val args: OWorkoutDetailsFragmentV2Args by navArgs()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    private val workoutDetailsAdapter: OWorkoutDetailslAdapterV2 by lazy {
        OWorkoutDetailslAdapterV2()
    }
    private val hrZoneAdapter: OWorkoutHRZoneAdapter by lazy {
        OWorkoutHRZoneAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDefaultUiValue()
        setRecycler()
        viewModel.getWorkoutDetails(args.workoutId)
        viewModel.position = args.position

    }

    private fun setRecycler() {
        with(binding.rvActivityDetails) {
            adapter = workoutDetailsAdapter
        }
        with(binding.lytHeartRate.ryvHrZone) {
            adapter = hrZoneAdapter
        }
    }


    override fun initListener() {
        binding.lytActivityItem.root.setOnClickListener {

            binding.lytHeartRate.heartRateChart.updateHighlight(
                viewModel.getIndexList(1),
                Color.parseColor("#00FF00")
            )
            context.showShortToast("Clicked")
        }

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvEdit.setOnClickListener {
            val id = viewModel.workoutDetailsResponse.value?.id
            viewModel.deleteWorkout(id!!)
        }

        binding.svMain.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (Math.abs(scrollY - oldScrollY) > 0) {
                binding.lytHeartRate.heartRateChart.resetIfInteracting()
            }
        }

    }

    private fun setDefaultUiValue() {
        binding.lytActivityItem.tvDurationTitle.text = getString(R.string.text_duration)
        binding.lytArrow.ivArrowImage.setImageResource(R.drawable.ic_arrow_down)
    }

    override fun subscribeObservers() {

        viewModel.workoutDetailsResponse.observe(this) {
            if (it != null) {
                updateUi(it)
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.workoutDeletedResponse.observe(this) {
            it?.getContent()?.let { response ->
                mainViewModel.reloadTodaysData()

                setFragmentResult(
                    DELETE_WORKOUT_REQUEST_KEY,
                    bundleOf("allow" to true, "position" to viewModel.position)

                )
                navigateUpSafe()
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar1.root.visible()
            } else {
                binding.progressBar1.root.gone()
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(it: OWorkoutDetailsResponseModel) {
        LOGS.d("OWorkoutDetailsFragmentV2 detail=$it")

//        binding.lytIntensity.tvIntensityType.text = it.intensity

        if (it.date == DateFormats.getCurrentDate(DateFormats.dateFormat3) && !it.type.equals(
                "auto",
                true
            ) && !it.type.equals(
                "apple",
                true
            )
            && !it.type.equals(
                "google",
                true
            )
        ) {
            binding.tvEdit.visible()
        }


        binding.lytActivityItem.tvWorkoutTime.text =
            DateFormats.getActivityDisplayDates(it.startTime, it.endTime)


        binding.rvActivityDetails.visible()
        binding.lytActivityItem.root.visible()
        binding.lytToolbar.tvTitle.text = DateFormats.formatActivityDate(it.date)
        binding.lytActivityItem.tvActivityName.text = it.getFormattedActivityName()
        binding.lytActivityItem.tvDurationValue.text =
            ApplicationUtils.getActivityDurationFormat2(it.duration)
        val topValue = viewModel.getDistance(it)
        binding.lytActivityItem.tvDistanceTitle.text = topValue.third
        binding.lytActivityItem.tvDistanceValue.text = topValue.first
        binding.lytActivityItem.tvDistanceUnit.text = topValue.second

        if (it.nudge != null && !it.nudge.title.isNullOrEmpty()) {
            binding.lytCues.apply {
                root.visible()
                tvCuesTitle.text = it.nudge.title
                tvCuesDesc.text = it.nudge.description
            }
        }




        binding.lytActivityItem.ivWorkoutImage.loadImage(
            requireContext(), it.iconUrl
        )

        val workoutDetailList = viewModel.prepareDataForActivity(it)
        if (workoutDetailList.size > 4) {
            binding.lytArrow.root.visible()
            workoutDetailsAdapter.setDataSet(workoutDetailList.take(4))
            binding.lytArrow.ivArrow.setOnClickListener {
                if (!viewModel.workoutDetailsExpanded) {
                    viewModel.workoutDetailsExpanded = true
                    binding.lytArrow.ivArrowImage.rotation = 180f
                    workoutDetailsAdapter.setDataSet(workoutDetailList)
                } else {
                    viewModel.workoutDetailsExpanded = false
                    workoutDetailsAdapter.setDataSet(workoutDetailList.take(4))
                    binding.lytArrow.ivArrowImage.rotation = 0f

                }
            }
        } else {
            binding.lytArrow.root.gone()
            workoutDetailsAdapter.setDataSet(workoutDetailList)
        }


        if (it.type.equals(WorkoutTypes.USERWORKOUT.name, true)) {
            if (!it.hrArray.isNullOrEmpty()) {
                binding.llExpand.visible()
                binding.lytHeartRate.tvAverageValue.text =
                    if (it.hrAvg == null || it.hrAvg == 0 || it.hrAvg == 255) {
                        "-"
                    } else {
                        it.hrAvg.toString()
                    }
                val maxHr = if (it.hrMax == null || it.hrMax == 0 || it.hrMax == 255) {
                    "-"
                } else {
                    it.hrMax.toString()
                }
                binding.lytHeartRate.tvMaxHR.text =
                    "${getString(R.string.text_max_hr)} $maxHr ${getString(R.string.text_bpm_small)}"

                hrZoneAdapter.setDataSet(
                    viewModel.generateHrZones(it.hrArray)
                )
            } else {
                return
            }
        } else {
            return
        }


        val sleepChart = SleepChartModel()
        val chartList = ArrayList<ChartModel>()
        it.hrArray.forEachIndexed { index, data ->
            val chartModel = ChartModel()

            var value = data
            if (value == 255) {
                value = 0
            }

            chartModel.value = value
            chartModel.index = ""
            chartList.add(chartModel)
        }


        sleepChart.list = chartList

        binding.lytHeartRate.heartRateChart.apply {
            setVibrationUtil(vibrationUtils)
            updateGraphColor(
                Color.parseColor("#ff7f96"),
                Color.parseColor("#844B60"),
                Color.parseColor("#99ff718b"),
                Color.parseColor("#0Dff718b")
            )
            val lowValueIndex = updateDataWithMax(
                sleepChart, 5, false, false,
                GraphDummyModel(
                    false, 40, 100
                ),
                it.hrAvg,
                "${it.date} ${it.startTime}",
                "${it.date} ${it.endTime}"
            )

            setInteractiveMode(true)

            setClickListener(object : OnHeartRateChartClickAction {
                override fun onValueSelected(value: Int, isInteracting: Boolean, time: String?) {
                    if (isInteracting) {
                        /* binding.lytHeartRate.tvSubtitle1.text = time ?: ""
                         binding.lytHeartRate.lytSubtitleValue1.tvValue.text =
                             if (value > 0) "$value" else "-"*/

                    } else {
                        //setHrLowestHr()
                    }
                }

                override fun onTopClicked() {

                }

            })

        }


    }
}