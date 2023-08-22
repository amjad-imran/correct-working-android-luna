package com.oreo.ui.workout.details

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOWorkoutDetailsBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ChartModel
import com.oreo.data.model.GraphDummyModel
import com.oreo.data.model.OWDActivityData
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.model.SleepChartModel
import com.oreo.util.UtilClass
import dagger.hilt.android.AndroidEntryPoint

const val DELETE_WORKOUT_REQUEST_KEY = "DELETE_WORKOUT_REQUEST_KEY"

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
        mViewModel.position = args.position
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

        binding.tvEdit.setOnClickListener {
            val id = mViewModel.workoutDetailsResponse.value?.id
            mViewModel.deleteWorkout(id!!)
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

        mViewModel.workoutDeletedResponse.observe(this) {
            it?.getContent()?.let { response ->
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
            } else {
                binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
                binding.lytHeartRate.lytSubtitleValue2.tvUnit.gone()
            }


            if (!it.hrArray.isNullOrEmpty()) {
                binding.lytHeartRate.lineChart.visible()
                val baseDataList = UtilClass.graphTwoHourBaseInterval(
                    it.startTime.clearAmPm(),
                    it.endTime,
                    it.hrArray.size
                )


                val sleepChart = SleepChartModel()
                val chartList = ArrayList<ChartModel>()
                it.hrArray.forEachIndexed { index, data ->
                    val chartModel = ChartModel()

                    var value = data
                    if (value == 255) {
                        value = 0
                    }


                    chartModel.index = baseDataList[index]
                    chartModel.value = value
                    chartList.add(chartModel)
                }

                sleepChart.list = chartList

                binding.lytHeartRate.lineChart.updateGraphColor(
                    Color.parseColor("#ff3358"),
                    Color.parseColor("#4cff3358"),
                    Color.parseColor("#00ff3358")
                )

                binding.lytHeartRate.lineChart.updateDataWithMax(sleepChart, 5, false,
                    true,  GraphDummyModel(
                        false,40,100
                    )
                )
            }
        }

        if (it.date == DateFormats.getCurrentDate(DateFormats.dateFormat3)) {
            binding.tvEdit.visible()
        }

        binding.rvActivityDetails.visible()
        binding.lytActivityItem.root.visible()
        binding.lytActivityItem.tvActivityDate.text = DateFormats.formatActivityDate(it.date)
        binding.lytActivityItem.tvTime.text = DateFormats.getActivityDisplayDates(it.startTime,it.endTime)

        binding.lytActivityItem.ivWorkoutImage.loadImage(
            requireContext(),
            it.iconUrl
        )
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
        if (it.steps != null && it.steps > 0) {
            activityList.add(
                OWDActivityData(
                    "Steps",
                    it.steps.toString(),
                    "",
                )
            )
        }

        mAdapter.setDataSet(activityList)
    }

}