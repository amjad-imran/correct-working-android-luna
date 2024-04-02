package com.oreo.ui.workout.details

import android.annotation.SuppressLint
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
import com.oreo.data.model.OWDActivityHRZoneData
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.model.WorkoutTypes
import com.oreo.ui.activity.all.DELETE_WORKOUT_REQUEST_KEY
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OWorkoutDetailsFragmentV2 :
    BaseFragment<FragmentOWorkoutDetailsV2Binding>(FragmentOWorkoutDetailsV2Binding::inflate) {

    private val viewModel: OWorkoutDetailsViewModelV2 by viewModels()
    private val args: OWorkoutDetailsFragmentV2Args by navArgs()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

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
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvEdit.setOnClickListener {
            val id = viewModel.workoutDetailsResponse.value?.id
            viewModel.deleteWorkout(id!!)
        }

    }

    private fun setDefaultUiValue() {
        binding.lytActivityItem.tvDurationTitle.text = getString(R.string.text_duration)
        binding.lytActivityItem.tvDistanceTitle.text = getString(R.string.total_distance)


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

        binding.rvActivityDetails.visible()
        binding.lytActivityItem.root.visible()
        binding.lytToolbar.tvTitle.text = DateFormats.formatActivityDate(it.date)
        binding.lytActivityItem.tvActivityName.text = it.getFormattedActivityName()
        binding.lytActivityItem.tvDurationValue.text =
            ApplicationUtils.getActivityDurationFormat2(it.duration)
        val topValue = viewModel.getDistance(it)
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

                hrZoneAdapter.setDataSet(arrayListOf<OWDActivityHRZoneData>().apply {
                    add(
                        OWDActivityHRZoneData(
                            title = "Zone1",
                            range = "<60%",
                            percentage = 30,
                            duration = "00:30",
                            color = "#3485ff",
                        )
                    )
                    add(
                        OWDActivityHRZoneData(
                            title = "Zone2",
                            range = "60-70%",
                            percentage = 20,
                            duration = "00:10",
                            color = "#34f3ff",
                        )
                    )
                    add(
                        OWDActivityHRZoneData(
                            title = "Zone3",
                            range = "60-70%",
                            percentage = 10,
                            duration = "00:10",
                            color = "#34f3ff",
                        )
                    )
                })
            }
        }


    }


}