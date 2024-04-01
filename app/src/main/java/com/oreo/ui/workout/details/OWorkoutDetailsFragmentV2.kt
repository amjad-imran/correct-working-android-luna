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
import com.noisefit_commans.ui.custom.WorkoutIntensityGraphOreo
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.oreo.data.model.ChartModel
import com.oreo.data.model.GraphDummyModel
import com.oreo.data.model.OWDActivityData
import com.oreo.data.model.OWDActivityHRZoneData
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.model.SleepChartModel
import com.oreo.data.model.health.CommonListDataModel
import com.oreo.ui.activity.all.DELETE_WORKOUT_REQUEST_KEY
import com.oreo.util.UtilClass
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat


@AndroidEntryPoint
class OWorkoutDetailsFragmentV2 :
    BaseFragment<FragmentOWorkoutDetailsV2Binding>(FragmentOWorkoutDetailsV2Binding::inflate) {

    private val mViewModel: OWorkoutDetailsViewModelV2 by viewModels()
    private val args: OWorkoutDetailsFragmentV2Args by navArgs()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    private val mAdapter: OWorkoutDetailslAdapterV2 by lazy {
        OWorkoutDetailslAdapterV2()
    }
    private val hrZoneAdapter: OWorkoutHRZoneAdapter by lazy {
        OWorkoutHRZoneAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDefaultUiValue()
        setRecycler()
        mViewModel.getWorkoutDetails(args.workoutId)
        mViewModel.position = args.position

    }

    private fun setRecycler() {
        with(binding.rvActivityDetails) {
            adapter = mAdapter
        }
        with(binding.lytHeartRate.ryvHrZone) {
            adapter = hrZoneAdapter
        }
    }


    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytArrow.ivArrow.setOnClickListener {
            if (binding.llExpand.visibility == View.VISIBLE) {
                binding.lytArrow.ivArrowImage.rotation = 0f
                binding.llExpand.gone()
            } else {
                binding.lytArrow.ivArrowImage.rotation = 180f
                binding.llExpand.visible()
            }
        }
    }

    private fun setDefaultUiValue() {
        binding.lytActivityItem.tvDurationTitle.text = getString(R.string.text_duration)
        binding.lytActivityItem.tvDistanceTitle.text = getString(R.string.total_distance)
        binding.lytActivityItem.tvDistanceUnit.text = getString(R.string.text_km)

        binding.lytArrow.ivArrowImage.setImageResource(R.drawable.ic_arrow_down)


    }

    override fun subscribeObservers() {

        mViewModel.workoutDetailsResponse.observe(this) {
            if (it != null) {
                updateUi(it)
            }
        }

        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        mViewModel.workoutDeletedResponse.observe(this) {
            it?.getContent()?.let { response ->
                mainViewModel.reloadTodaysData()

                setFragmentResult(
                    DELETE_WORKOUT_REQUEST_KEY,
                    bundleOf("allow" to true, "position" to mViewModel.position)

                )
                navigateUpSafe()
            }
        }
        mViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        mViewModel.getLoading().observe(this) {
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

        binding.rvActivityDetails.visible()
        binding.lytActivityItem.root.visible()
        binding.lytToolbar.tvTitle.text = DateFormats.formatActivityDate(it.date)
        binding.lytActivityItem.tvActivityName.text = it.getFormattedActivityName()
        binding.lytActivityItem.tvDurationValue.text =
            ApplicationUtils.getActivityDurationFormat2(it.duration)
        binding.lytActivityItem.tvDistanceValue.text = "8.9"//TODO replace


        binding.lytCues.root.visible()


        binding.lytActivityItem.ivWorkoutImage.loadImage(
            requireContext(), it.iconUrl
        )
        prepareDataForActivity(it)

        binding.lytArrow.root.visible()


        binding.lytHeartRate.tvAverageValue.text = it.hrAvg.toString()
        val maxHr = it.hrArray?.maxOrNull()
        binding.lytHeartRate.tvMaxHR.text =
            "${getString(R.string.text_max_hr)} ${maxHr} ${getString(R.string.text_bpm_small)}"

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


    private fun prepareDataForActivity(it: OWorkoutDetailsResponseModel) {
        val activityList = ArrayList<OWDActivityData>()
        if (it.calories != null && it.calories > 0) {
            activityList.add(
                OWDActivityData(
                    getString(R.string.text_total_calories),
                    it.calories.toString(),
                    "kcal",
                )
            )
        }

        if (it.calories != null && it.calories > 0) {
            activityList.add(
                OWDActivityData(
                    getString(R.string.text_cadence),
                    "10",
                    "spm",
                )
            )
        }

        val maxHr = it.hrArray?.maxOrNull()
        val minHr = it.hrArray?.minOrNull()
        activityList.add(
            OWDActivityData(
                getString(R.string.text_max_hr),
                maxHr.toString(),
                "bpm",
            )
        )
        activityList.add(
            OWDActivityData(
                getString(R.string.text_min_hr),
                minHr.toString(),
                "bpm",
            )
        )

        if (it.steps != null && it.steps > 0) {
            activityList.add(
                OWDActivityData(
                    "Steps",
                    it.steps.toString(),
                    "",
                )
            )
        }

        activityList.add(
            OWDActivityData(
                getString(R.string.text_recovery_time),
                "01:34",
                "",
            )
        )

        mAdapter.setDataSet(activityList)
    }

}