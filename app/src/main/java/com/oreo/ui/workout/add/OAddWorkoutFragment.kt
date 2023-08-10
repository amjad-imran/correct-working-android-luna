package com.oreo.ui.workout.add

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.noisefit.data.local.AppStaticData
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOAddWorkoutBinding
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.CandleChartModel
import com.oreo.data.model.OWorkoutListModal
import com.oreo.util.UtilClass
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.math.roundToInt

const val ADD_WORKOUT_REQUEST_KEY = "ADD_WORKOUT_REQUEST_KEY"

@AndroidEntryPoint
class OAddWorkoutFragment :
    BaseFragment<FragmentOAddWorkoutBinding>(FragmentOAddWorkoutBinding::inflate) {
    private val viewModel: OAddWorkoutViewModel by viewModels()
    private val args: OAddWorkoutFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.movementList = args.movementList?.toList()
        viewModel.convertAutoSport(args.autoSport)
    }

    private fun setToolbar() {
        binding.lytToolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }

            tvSave.setOnClickListener {
                viewModel.addWorkout()
            }

            tvTitle.text = getString(R.string.text_add_a_workout)
            tvSave.disable()
            binding.lytToolbar.tvSave.alpha = .5f
            tvSave.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.text_color_luna
                )
            )
        }
    }

    private fun goToSelectWorkout(workoutList: List<OWorkoutListModal>) {
        navigate(
            OAddWorkoutFragmentDirections.actionNavigationAddWorkoutFragToSelectWorkoutFragment(
                workoutList.toTypedArray()
            )
        )
    }

    override fun initListener() {

        setToolbar()
        setDefaultUIValue()


        setFragmentResultListener(SELECT_REQUEST_KEY) { _, bundle ->
            val workout = bundle.getParcelable<OWorkoutListModal>("workout")
            workout?.let {
                viewModel.workoutListModal = workout

                context?.let { ctx ->
                    binding.lytWorkout.ivWorkoutImage.loadImage(ctx, workout.iconUrl)
                }

                binding.lytWorkout.tvWorkout.text = workout.getFormattedActivityName()
                setCalories()
            }
        }

        binding.lytWorkout.ivDropDown.setOnClickListener {
            if (viewModel.oWorkoutListModalResponse.value.isNullOrEmpty()) {
                viewModel.getWorkoutList()
            } else {
                goToSelectWorkout(viewModel.oWorkoutListModalResponse.value!!)
            }
        }




        binding.lytStartEnd.lytStartTime.root.setOnClickListener {

            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")


                val calendar = Calendar.getInstance()

                if (DateFormats.compareTime(
                        hourOfDay,
                        minute,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE)
                    ) >= 0
                ) {
                    context.showShortToast(getString(R.string.text_start_time_less_then_current_time))
                } else if (DateFormats.compareTime(
                        hourOfDay,
                        minute,
                        viewModel.addWorkout.endHour,
                        viewModel.addWorkout.endMinute
                    ) >= 0
                ) {
                    context.showShortToast(getString(R.string.text_start_time_less))
                } else if (hourOfDay == viewModel.addWorkout.endHour && minute == viewModel.addWorkout.endMinute) {
                    context.showShortToast(getString(R.string.text_start_end_time_should_be_different))
                } else if (!DateFormats.checkDifferenceInBtwInterval(
                        hourOfDay,
                        minute,
                        viewModel.addWorkout.endHour,
                        viewModel.addWorkout.endMinute,
                        viewModel.minimumWorkoutTime

                    )
                ) {
                    context.showShortToast(
                        getString(
                            R.string.text_start_and_end_time_should_be_greater_than_frequency,
                            viewModel.minimumWorkoutTime.toString()
                        )
                    )
                } else {
                    viewModel.addWorkout.startHour = hourOfDay
                    viewModel.addWorkout.startMinute = minute
                    setStartTimeBetween()
                }


            }

            navigate(
                OAddWorkoutFragmentDirections.actionAddWorkoutFragmentToTimeBottomSheet(
                    viewModel.addWorkout.startHour,
                    viewModel.addWorkout.startMinute,
                    1,
                    getString(R.string.text_start_time)
                )
            )
        }
        binding.lytStartEnd.lytEndTime.root.setOnClickListener {

            if (binding.lytStartEnd.lytStartTime.tvTimeValue.text == getString(R.string.text_enter)) {
                context.showShortToast(getString(R.string.text_select_start_time_first))
                return@setOnClickListener
            }
            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                val calendar = Calendar.getInstance()

                if (DateFormats.compareTime(
                        hourOfDay,
                        minute,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE)
                    ) > 0
                ) {
                    context.showShortToast(getString(R.string.text_end_time_greater_then_current_time))
                } else if (DateFormats.compareTime(
                        hourOfDay,
                        minute,
                        viewModel.addWorkout.startHour,
                        viewModel.addWorkout.startMinute
                    ) < 0
                ) {
                    context.showShortToast(getString(R.string.text_end_time_greater))
                } else if (hourOfDay == viewModel.addWorkout.startHour && minute == viewModel.addWorkout.startMinute) {
                    context.showShortToast(getString(R.string.text_start_end_time_should_be_different))
                } else if (!DateFormats.checkDifferenceInBtwInterval(
                        viewModel.addWorkout.startHour,
                        viewModel.addWorkout.startMinute,
                        hourOfDay,
                        minute,
                        viewModel.minimumWorkoutTime
                    )
                ) {
                    context.showShortToast(
                        getString(
                            R.string.text_start_and_end_time_should_be_greater_than_frequency,
                            viewModel.minimumWorkoutTime.toString()
                        )
                    )
                } else if (DateFormats.checkDifferenceInBtwInterval(
                        viewModel.addWorkout.startHour,
                        viewModel.addWorkout.startMinute,
                        hourOfDay,
                        minute,
                        viewModel.maxWorkoutTime
                    )
                ) {
                    context.showShortToast(
                        getString(
                            R.string.text_start_and_end_time_should_be_less_than_frequency,
                            viewModel.maxWorkoutTime.toString()
                        )
                    )
                } else {
                    viewModel.addWorkout.endHour = hourOfDay
                    viewModel.addWorkout.endMinute = minute
                    setEndTimeBetween()
                }
            }

            navigate(
                OAddWorkoutFragmentDirections.actionAddWorkoutFragmentToTimeBottomSheet(
                    viewModel.addWorkout.endHour,
                    viewModel.addWorkout.endMinute,
                    1,
                    getString(R.string.text_end_time)
                )
            )
        }
        binding.lytIntensity.root.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { it1 ->
                    viewModel.addWorkout.intensity = it1
                    setIntensity()
                    setCalories()
                }

            }
            navigate(
                OAddWorkoutFragmentDirections.actionAddWorkoutFragmentToValueSelectorBottomSheet(
                    viewModel.addWorkout.intensity,
                    AppStaticData.getIntensityValues(),
                    getString(R.string.text_intensity)
                )
            )
        }

    }

    private fun setCalories() {
        val calories = viewModel.getCaloriesBurnt().roundToInt()
        viewModel.addWorkout.calories = calories
        binding.lytCaloriesBurn.tvCalBurnValue.text = if (calories > 0) {
            "$calories"
        } else {
            "--"
        }

    }

    private fun setStartTimeBetween() {
        val startTime = DateFormats.formatTimeWithAmPm(
            viewModel.addWorkout.startHour,
            viewModel.addWorkout.startMinute
        )
//
//        if (viewModel.oAddWorkout.startHour != 0) {
        setTextWhite(binding.lytStartEnd.lytStartTime.tvTimeValue)
//        }

        viewModel.addWorkout.startTimeIn24H = DateFormats.formatTime(
            viewModel.addWorkout.startHour,
            viewModel.addWorkout.startMinute
        )
        binding.lytStartEnd.lytStartTime.tvTimeValue.text = startTime
    }

    private fun setEndTimeBetween(ignoreDuration: Boolean = true) {
        val endTime = DateFormats.formatTimeWithAmPm(
            viewModel.addWorkout.endHour,
            viewModel.addWorkout.endMinute
        )

        setTextWhite(binding.lytStartEnd.lytEndTime.tvTimeValue)
        viewModel.addWorkout.endTimeIn24H = DateFormats.formatTime(
            viewModel.addWorkout.endHour,
            viewModel.addWorkout.endMinute
        )
        if (ignoreDuration) {
            setDuration()
        }

        binding.lytStartEnd.lytEndTime.tvTimeValue.text = endTime
    }

    private fun setDuration() {
        val duration = viewModel.getWorkoutDuration()
        setCalories()
        if (duration > 0) {
            viewModel.addWorkout.duration = duration
            binding.lytCaloriesBurn.tvDurationValue.text = duration.toString()
            enableSaveBtn()

        }
    }

    private fun setIntensity() {
        var intensity = "Enter"

        if (viewModel.addWorkout.intensity.isNotEmpty()) {
            setTextWhite(binding.lytIntensity.lytItem.tvTimeValue)
            intensity = viewModel.addWorkout.intensity
            enableSaveBtn()
        }
        binding.lytIntensity.lytItem.tvTimeValue.text = intensity
    }


    private fun setTextWhite(textView: TextView) {
        textView.setTextColor(Color.WHITE)
    }

    private fun setDefaultUIValue() {
        binding.lytWorkout.ivWorkoutImage.setImageResource(R.drawable.ic_o_workout_new)
        binding.lytCaloriesBurn.tvDurationValue.text = "--"
        binding.lytCaloriesBurn.tvDurationUnit.text = getString(R.string.text_min)
        binding.lytCaloriesBurn.tvCalBurnValue.text = "--"
        binding.lytCaloriesBurn.tvCalBurnUnit.text = getString(R.string.text_kcal)

        binding.lytStartEnd.lytStartTime.tvTime.text = getText(R.string.text_start_time)
        binding.lytStartEnd.lytStartTime.tvTimeValue.text = getString(R.string.text_enter)

        binding.lytStartEnd.lytEndTime.tvTime.text = getString(R.string.text_end_time)
        binding.lytStartEnd.lytEndTime.tvTimeValue.text = getString(R.string.text_enter)

        binding.lytIntensity.lytItem.tvTime.text = getString(R.string.text_intensity)
        binding.lytIntensity.lytItem.tvTimeValue.text = getString(R.string.text_enter)

    }

    private fun enableSaveBtn(forceSave: Boolean = false) {
        if ((viewModel.addWorkout.duration > 0 &&
                    viewModel.addWorkout.intensity.isNotEmpty() &&
                    viewModel.workoutListModal != null) || forceSave
        ) {
            binding.lytToolbar.apply {
                tvSave.enable()
                binding.lytToolbar.tvSave.alpha = 1f

            }
        }
    }

    private fun disableSelection() {
        binding.lytIntensity.root.disable()

        binding.lytStartEnd.lytStartTime.root.disable()
        binding.lytStartEnd.lytEndTime.root.disable()
    }

    private fun setPrefillData() {
        enableSaveBtn(true)
        setIntensity()

        binding.lytCaloriesBurn.tvCalBurnValue.text = viewModel.addWorkout.calories.toString()
        binding.lytCaloriesBurn.tvDurationValue.text = viewModel.addWorkout.duration.toString()
        setStartTimeBetween()
        setEndTimeBetween(false)
        binding.lytWorkout.tvWorkout.text = viewModel.activityType

        binding.lytToolbar.apply {
            tvTitle.text = getString(R.string.text_identify_workout)
        }

        setMovementGraph()

    }

    private fun setMovementGraph() {
        binding.candleChart.visible()
        val topIndexList =
            UtilClass.getDetectedWorkoutMovement(viewModel.preFilledOreoAutoSportData!!)
        val baseHrList = UtilClass.graphBaseInterval(null, null, viewModel.movementList?.size ?: 0)
        LOGS.d("setMovementGraph ${Gson().toJson(topIndexList)}")
        val candleChartModelList: MutableList<CandleChartModel> =
            java.util.ArrayList<CandleChartModel>()
        viewModel.movementList?.forEachIndexed { index, data ->

            val chartModel = CandleChartModel()

            chartModel.bottomLineText = baseHrList[index]
            chartModel.identifyText = topIndexList[index].toString()
            when (data) {
                1 -> {

                    chartModel.length =
                        (binding.candleChart.max * 0.4).toInt()

                    chartModel.type = CandleChartModel.Type.LOW
                    chartModel.color = if (chartModel.identifyText == "null") {
                        Color.parseColor("#3d3d3d")
                    } else if (chartModel.identifyText != "ignore") {
                        chartModel.length =
                            (binding.candleChart.max * 1.2).toInt()
                        Color.parseColor("#ffffff")

                    } else {
                        Color.parseColor("#4cffd230")
                    }
                }

                2 -> {

                    chartModel.length =
                        (binding.candleChart.max * 0.6).toInt()


                    chartModel.color = if (chartModel.identifyText == "null") {
                        Color.parseColor("#3d3d3d")
                    } else if (chartModel.identifyText != "ignore") {
                        chartModel.length =
                            (binding.candleChart.max * 1.2).toInt()
                        Color.parseColor("#ffffff")

                    } else {
                        Color.parseColor("#ffd230")
                    }
                    chartModel.type = CandleChartModel.Type.MEDIUM
                }

                3, 4 -> {

                    chartModel.length =
                        (binding.candleChart.max * 0.8).toInt()

                    chartModel.color = if (chartModel.identifyText == "null") {
                        Color.parseColor("#3d3d3d")
                    } else if (chartModel.identifyText != "ignore") {
                        chartModel.length =
                            (binding.candleChart.max * 1.2).toInt()
                        Color.parseColor("#ffffff")

                    } else {
                        Color.parseColor("#ffffff")
                    }


                    chartModel.type = CandleChartModel.Type.HIGH
                }

                else -> {
                    chartModel.length =
                        (binding.candleChart.max * 0.2).toInt()

                    chartModel.color = if (chartModel.identifyText == "null") {

                        Color.parseColor("#3d3d3d")

                    } else if (chartModel.identifyText != "ignore") {
                        chartModel.length =
                            (binding.candleChart.max * 1.2).toInt()
                        Color.parseColor("#ffffff")

                    } else {
                        Color.parseColor("#4c4c4c")
                    }

                    chartModel.type = CandleChartModel.Type.INACTIVE

                }
            }
            chartModel.value = data
            candleChartModelList.add(chartModel)
        }
        binding.candleChart.updateData(candleChartModelList)

    }


    override fun subscribeObservers() {
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.autoSport.observe(this) {
            it?.let {
                if (it) {
                    setPrefillData()
                    disableSelection()
                }
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.oWorkoutListModalResponse.observe(this) {
            it?.let {
                goToSelectWorkout(it)
            }
        }

        viewModel.addWorkoutResponse.observe(this) {
            it?.let {
                if (it) {

                    setFragmentResult(
                        ADD_WORKOUT_REQUEST_KEY,
                        bundleOf("allow" to true)

                    )
                    navigateUpSafe()
                }
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }

}