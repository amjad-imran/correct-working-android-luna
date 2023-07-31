package com.oreo.ui.workout.add

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.data.local.AppStaticData
import com.noisefit.luna.databinding.FragmentOAddWorkoutBinding
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.common.upToNDecimal
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.OWorkoutListModal
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import kotlin.math.roundToInt

@AndroidEntryPoint
class OAddWorkoutFragment :
    BaseFragment<FragmentOAddWorkoutBinding>(FragmentOAddWorkoutBinding::inflate) {
    private val viewModel: OAddWorkoutViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                    R.color.accent_color_purple
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

            if(binding.lytStartEnd.lytStartTime.tvTimeValue.text == getString(R.string.text_enter)){
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
                }else if (DateFormats.compareTime(
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
                } else if (!DateFormats.checkDifferenceInBtwInterval(
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
                    getString(R.string.text_start_time)
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

    private fun setEndTimeBetween() {
        val endTime = DateFormats.formatTimeWithAmPm(
            viewModel.addWorkout.endHour,
            viewModel.addWorkout.endMinute
        )
//        if (viewModel.oAddWorkout.endHour != 0) {
        setTextWhite(binding.lytStartEnd.lytEndTime.tvTimeValue)
//        }
        viewModel.addWorkout.endTimeIn24H = DateFormats.formatTime(
            viewModel.addWorkout.endHour,
            viewModel.addWorkout.endMinute
        )
        setDuration()
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

    private fun enableSaveBtn() {
        if (viewModel.addWorkout.duration > 0 &&
            viewModel.addWorkout.intensity.isNotEmpty() &&
            viewModel.workoutListModal != null
        ) {
            binding.lytToolbar.apply {
                tvSave.enable()
                binding.lytToolbar.tvSave.alpha = 1f

            }
        }
    }

    override fun subscribeObservers() {
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
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