package com.oreo.ui.workout.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOWorkoutDetailsBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.OWDActivityData
import com.oreo.data.model.OWorkoutDetailsResponseModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OWorkoutDetailsFragment :
    BaseFragment<FragmentOWorkoutDetailsBinding>(FragmentOWorkoutDetailsBinding::inflate) {
    private val mViewModel: OWorkoutDetailsViewModel by viewModels()
    private val args: OWorkoutDetailsFragmentArgs by navArgs()
    private val mAdapter: OWorkoutDetailslAdapter by lazy {
        OWorkoutDetailslAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDefaultUiValue()
        setRecycler()
        mViewModel.getWorkoutDetails(args.workoutId)
    }

    private fun setRecycler() {
        with(binding.rvActivityDetails) {
            adapter = mAdapter
        }
    }


    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    private fun setDefaultUiValue() {
        binding.lytToolbar.tvTitle.text = args.workoutName
        binding.lytIntensity.tvTitle.text = getString(R.string.text_intensity)
        binding.lytHeartRate.tvTitle.text = getString(R.string.text_heart_rate)
        binding.lytHeartRate.tvSubtitle1.text = getString(R.string.text_lowest_hr)
        binding.lytHeartRate.tvSubtitle2.text = getString(R.string.text_average_hr)

        binding.lytPace.tvTitle.text = getString(R.string.text_pace)
        binding.lytPace.tvSubtitle1.text = getString(R.string.text_average)
        binding.lytPace.tvSubtitle2.text = getString(R.string.maximum)
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

    private fun updateUi(it: OWorkoutDetailsResponseModel) {
        binding.lytIntensity.tvIntensityType.text = it.intensity
        if ((it.hrLow == null || it.hrLow == 0) && (it.hrAvg == null || it.hrLow == 0)) {
            binding.lytHeartRate.root.gone()
            binding.divider2.root.gone()
        } else {
            binding.lytHeartRate.root.visible()
            binding.divider2.root.visible()
            if (it.hrLow != null) {
                binding.lytHeartRate.lytSubtitleValue1.tvValue.text = it.hrLow.toString()
                binding.lytHeartRate.lytSubtitleValue1.tvUnit.visible()
                binding.lytHeartRate.lytSubtitleValue1.tvUnit.text = "bpm"
                binding.lytHeartRate.lytSubtitleValue1.tvValue.visible()
            } else {
                binding.lytHeartRate.lytSubtitleValue1.tvValue.gone()
                binding.lytHeartRate.lytSubtitleValue1.tvUnit.gone()
            }
            if (it.hrAvg != null) {
                binding.lytHeartRate.lytSubtitleValue2.tvValue.text = it.hrAvg.toString()
                binding.lytHeartRate.lytSubtitleValue2.tvUnit.visible()
                binding.lytHeartRate.lytSubtitleValue2.tvUnit.visible()
                binding.lytHeartRate.lytSubtitleValue2.tvUnit.text = "bpm"
            }
            else
            {
                binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
                binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
            }
        }
        binding.lytActivityItem.tvActivityDate.text = DateFormats.formatActivityDate(it.date)
        binding.lytActivityItem.tvTime.text = "${
            DateFormats.formatActivityTime8(it.startTime).lowercase()
        } - ${DateFormats.formatActivityTime8(it.endTime).lowercase()}"
        prepareDataForActivity(it)
    }

    private fun prepareDataForActivity(it: OWorkoutDetailsResponseModel) {
        val activityList = ArrayList<OWDActivityData>()
        activityList.add(
            OWDActivityData(
                "Duration",
                ApplicationUtils.getActivityDurationFormat2(it.duration),
                ""
            )
        )
        if (it.calories != null && it.calories > 0) {
            activityList.add(
                OWDActivityData(
                    "Calories",
                    it.calories.toString(),
                    "Kcal",
                )
            )
        }

        mAdapter.setDataSet(activityList)
    }

}