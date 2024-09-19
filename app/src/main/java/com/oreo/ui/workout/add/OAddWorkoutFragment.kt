package com.oreo.ui.workout.add

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.data.local.AppStaticData
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOAddWorkoutBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

const val ADD_WORKOUT_REQUEST_KEY = "ADD_WORKOUT_REQUEST_KEY"

@AndroidEntryPoint
class OAddWorkoutFragment :
    BaseFragment<FragmentOAddWorkoutBinding>(FragmentOAddWorkoutBinding::inflate) {
    private val viewModel: OAddWorkoutViewModel by viewModels()
    private val args: OAddWorkoutFragmentArgs by navArgs()
    private val mainViewModel: OreoMainViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.movementList = args.movementList?.toList()
        if (args.autoSport != null) {
            viewModel.convertAutoSport(args.autoSport)
        } else {
            viewModel.userDayData =
                mainViewModel.userHealthData[DateFormats.getTodaysDateString(10)]
        }


        viewModel.getWorkoutList(false)

    }

    private fun setToolbar() {
        binding.lytToolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }

            tvSave.setOnClickListener {
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_add_workout_save_click)
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

    private fun setWorkout(workout: OWorkoutListModal) {
        viewModel.workoutListModal = workout

        context?.let { ctx ->
            binding.lytWorkout.ivWorkoutImage.loadImage(ctx, workout.iconUrl)
        }

        binding.lytWorkout.tvWorkout.text = workout.getFormattedActivityName()
        /*setCalories()
        enableSaveBtn()*/
        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_add_workout + "_${workout.activityType}_CLICK")

        updateCalculatedData()
    }

    override fun initListener() {

        setToolbar()
        setDefaultUIValue()


        setFragmentResultListener(SELECT_REQUEST_KEY) { _, bundle ->
            val workout = bundle.getParcelable<OWorkoutListModal>("workout")
            workout?.let {
                setWorkout(it)
            }
        }

        binding.lytWorkout.ivDropDown.setOnClickListener {
            if (viewModel.oWorkoutListModalResponse.value.isNullOrEmpty()) {
                viewModel.getWorkoutList(true)
            } else {
                goToSelectWorkout(viewModel.oWorkoutListModalResponse.value!!)
            }
        }


        binding.lytStartEnd.lytDate.root.setOnClickListener {
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { it1 ->
                    val parsedDate =
                        LocalDate.parse(it1, DateTimeFormatter.ofPattern("dd MMM yyyy"))
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString()
                    viewModel.addWorkout.date = parsedDate


                    val todayDate = LocalDate.now().toString()
                    if (parsedDate.equals(todayDate)) {
                        resetData()
                    }
                    setDate()
                }
            }
            val format = DateTimeFormatter.ofPattern("dd MMM yyyy")
            navigate(
                OAddWorkoutFragmentDirections.actionAddWorkoutFragmentToValueSelectorBottomSheet(
                    if (viewModel.addWorkout.date == null) null else LocalDate.parse(viewModel.addWorkout.date)
                        .format(format),
                    viewModel.getWorkoutDates(),
                    getString(R.string.text_date)
                )
            )

        }


        binding.lytStartEnd.lytStartTime.root.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_add_workout_start_time_click)

            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")


                val calendar = Calendar.getInstance()

                val todayDate = LocalDate.now().toString()
                if (viewModel.addWorkout.date?.equals(todayDate) == true || viewModel.isAutoWorkout()) {
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
                            viewModel.addWorkout.endHour!!,
                            viewModel.addWorkout.endMinute!!
                        ) >= 0
                    ) {
                        context.showShortToast(getString(R.string.text_start_time_less))
                    } else if (hourOfDay == viewModel.addWorkout.endHour && minute == viewModel.addWorkout.endMinute) {
                        context.showShortToast(getString(R.string.text_start_end_time_should_be_different))
                    } else if (!DateFormats.checkDifferenceInBtwInterval(
                            hourOfDay,
                            minute,
                            viewModel.addWorkout.endHour!!,
                            viewModel.addWorkout.endMinute!!,
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
                } else {
                    if (viewModel.addWorkout.endHour == null || viewModel.addWorkout.endMinute == null) {
                        viewModel.addWorkout.startHour = hourOfDay
                        viewModel.addWorkout.startMinute = minute

                        setStartTimeBetween()
                        setEndTimeBetween()
                    } else {
                        if (DateFormats.compareTime(
                                hourOfDay,
                                minute,
                                viewModel.addWorkout.endHour!!,
                                viewModel.addWorkout.endMinute!!
                            ) >= 0
                        ) {
                            viewModel.addWorkout.startHour = hourOfDay
                            viewModel.addWorkout.startMinute = minute
                            viewModel.addWorkout.endHour = null
                            viewModel.addWorkout.endMinute = null

                            setStartTimeBetween()
                            setEndTimeBetween()

                            //context.showShortToast(getString(R.string.text_start_time_less))
                        } else if (hourOfDay == viewModel.addWorkout.endHour && minute == viewModel.addWorkout.endMinute) {
                            context.showShortToast(getString(R.string.text_start_end_time_should_be_different))
                        } else if (!DateFormats.checkDifferenceInBtwInterval(
                                hourOfDay,
                                minute,
                                viewModel.addWorkout.endHour!!,
                                viewModel.addWorkout.endMinute!!,
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

                }
                updateCalculatedData()
            }

            navigate(
                OAddWorkoutFragmentDirections.actionAddWorkoutFragmentToTimeBottomSheet(
                    viewModel.addWorkout.startHour,
                    viewModel.addWorkout.startMinute,
                    viewModel.addWorkout.endHour ?: 0,
                    viewModel.addWorkout.endMinute ?: 0,
                    1,
                    1,
                    getString(R.string.text_start_time)
                )
            )
        }
        binding.lytStartEnd.lytEndTime.root.setOnClickListener {

            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_add_workout_end_time_click)
            if (binding.lytStartEnd.lytStartTime.tvTimeValue.text == getString(R.string.text_enter)) {
                context.showShortToast(getString(R.string.text_select_start_time_first))
                return@setOnClickListener
            }
            setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                val calendar = Calendar.getInstance()
                var isTodayWorkout = true
                if (viewModel.isAutoWorkout()) {
                    val todayData = DateFormats.getCurrentDate(DateFormats.dateFormat3())
                    isTodayWorkout = viewModel.preFilledOreoAutoSportData?.date.equals(todayData)
                } else {
                    val todayDate = LocalDate.now().toString()
                    isTodayWorkout = viewModel.addWorkout.date?.equals(todayDate) == true
                }

                if (isTodayWorkout && DateFormats.compareTime(
                        hourOfDay,
                        minute,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE)
                    ) > 0
                ) {
                    context.showShortToast(getString(R.string.text_end_time_greater_then_current_time))
                    return@setFragmentResultListener
                }

                if (DateFormats.compareTime(
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
                }/* else if (DateFormats.checkDifferenceInBtwInterval(//OS-408
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
                }*/ else {
                    viewModel.addWorkout.endHour = hourOfDay
                    viewModel.addWorkout.endMinute = minute
                    setEndTimeBetween()
                }

                updateCalculatedData()
            }

            navigate(
                OAddWorkoutFragmentDirections.actionAddWorkoutFragmentToTimeBottomSheet(
                    viewModel.addWorkout.endHour ?: 0,
                    viewModel.addWorkout.endMinute ?: 0,
                    viewModel.addWorkout.startHour,
                    viewModel.addWorkout.startMinute,
                    0,
                    1,
                    getString(R.string.text_end_time)
                )
            )
        }
        binding.lytIntensity.root.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_add_workout_intensity_click)
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { it1 ->
                    viewModel.addWorkout.intensity = it1
                    setIntensity()
                    //setCalories()
                    viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_workout + "_${it1}_LEVEL_CLICK")
                    updateCalculatedData()
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

    private fun updateCalculatedData() {

        val duration = viewModel.getWorkoutDuration()
        if (duration > 0) {
            viewModel.addWorkout.duration = duration
            binding.lytCaloriesBurn.tvDurationValue.text = duration.toString()
        } else {
            viewModel.addWorkout.duration = 0
            binding.lytCaloriesBurn.tvDurationValue.text = "-"
        }

        val calories = viewModel.getCaloriesBurnt()
        viewModel.addWorkout.calories = calories
        binding.lytCaloriesBurn.tvCalBurnValue.text = if (calories > 0) {
            "$calories"
        } else {
            "--"
        }


        if (viewModel.preFilledOreoAutoSportData != null) {
            val highlightedPoints = viewModel.getHighlightedPoints()
            binding.movementChart.setHighlightedPoints(highlightedPoints)

        }



        enableSaveBtn()

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

        /*if (viewModel.isStartTimeSelected && viewModel.isEndTimeSelected) {
            setDuration()
        }*/
        binding.lytStartEnd.lytStartTime.tvTimeValue.text = startTime
        viewModel.isStartTimeSelected = true
    }

    private fun setEndTimeBetween(ignoreDuration: Boolean = true) {
        if (viewModel.addWorkout.endHour == null || viewModel.addWorkout.endMinute == null) {
            viewModel.isEndTimeSelected = false
            binding.lytStartEnd.lytEndTime.tvTimeValue.text = "Enter"
        } else {
            val endTime = DateFormats.formatTimeWithAmPm(
                viewModel.addWorkout.endHour!!,
                viewModel.addWorkout.endMinute!!
            )

            setTextWhite(binding.lytStartEnd.lytEndTime.tvTimeValue)
            viewModel.addWorkout.endTimeIn24H = DateFormats.formatTime(
                viewModel.addWorkout.endHour!!,
                viewModel.addWorkout.endMinute!!
            )
            /*if (ignoreDuration) {
                setDuration()
            }*/
            viewModel.isEndTimeSelected = true
            binding.lytStartEnd.lytEndTime.tvTimeValue.text = endTime
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

    private fun setDate() {
        var date = "Enter"

        if (viewModel.addWorkout.date.isNullOrEmpty().not()) {
            setTextWhite(binding.lytIntensity.lytItem.tvTimeValue)
            date = LocalDate.parse(viewModel.addWorkout.date)
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        } else {
            date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")).toString()
            viewModel.addWorkout.date = LocalDate.now().toString()
        }

        binding.lytStartEnd.lytDate.tvTimeValue.text = date
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

    private fun enableSaveBtn() {

        val forceSave = viewModel.isAutoWorkout()

        if ((viewModel.addWorkout.duration > 0 &&
                    viewModel.addWorkout.intensity.isNotEmpty() &&
                    viewModel.workoutListModal != null) || forceSave
        ) {
            binding.lytToolbar.apply {
                tvSave.enable()
                binding.lytToolbar.tvSave.alpha = 1f

            }
        } else {
            binding.lytToolbar.apply {
                tvSave.disable()
                binding.lytToolbar.tvSave.alpha = 0.5f

            }
        }
    }

    private fun disableSelection() {
        binding.lytIntensity.root.disable()

        binding.lytStartEnd.lytStartTime.root.disable()
        binding.lytStartEnd.lytEndTime.root.disable()
    }

    private fun setPrefillData() {
        enableSaveBtn()
        setIntensity()

        binding.lytCaloriesBurn.tvCalBurnValue.text = viewModel.addWorkout.calories.toString()
        binding.lytCaloriesBurn.tvDurationValue.text = viewModel.addWorkout.duration.toString()
        setStartTimeBetween()
        setEndTimeBetween(false)
        binding.lytWorkout.tvWorkout.text = viewModel.activityType

        binding.lytToolbar.apply {
            tvTitle.text = getString(R.string.text_identify_workout)
        }

        viewModel.movementList?.let {
            handleMovementNewViews(it)
        }
    }

    private fun handleMovementNewViews(movementList: List<Int>) {
        binding.rvMovements.visible()

        val newList = viewModel.getCombinedMovementData(movementList)
        val highlightedPoints = viewModel.getHighlightedPoints()
        binding.movementChart.setData(newList, arrayListOf())
        binding.movementChart.setHighlightedPoints(highlightedPoints)

    }

    fun setDefaultData(oWorkoutListModal: OWorkoutListModal) {
        //set intensity
        viewModel.addWorkout.intensity = "Moderate"
        setDate()
        setIntensity()

        //set start time and end time
        val calStart = Calendar.getInstance()

        viewModel.addWorkout.endHour = calStart.get(Calendar.HOUR_OF_DAY)
        viewModel.addWorkout.endMinute = calStart.get(Calendar.MINUTE)

        if (calStart.get(Calendar.HOUR_OF_DAY) == 0) {
            calStart.add(Calendar.MINUTE, -calStart.get(Calendar.MINUTE))
        } else {
            calStart.add(Calendar.MINUTE, -60)
        }
        viewModel.addWorkout.startHour = calStart.get(Calendar.HOUR_OF_DAY)
        viewModel.addWorkout.startMinute = calStart.get(Calendar.MINUTE)
        setStartTimeBetween()

        setEndTimeBetween()


        //set workout
        setWorkout(oWorkoutListModal)

    }

    fun resetData() {
        //set start time and end time
        val calStart = Calendar.getInstance()
        viewModel.addWorkout.endHour = calStart.get(Calendar.HOUR_OF_DAY)
        viewModel.addWorkout.endMinute = calStart.get(Calendar.MINUTE)
        if (calStart.get(Calendar.HOUR_OF_DAY) == 0) {
            calStart.add(Calendar.MINUTE, -calStart.get(Calendar.MINUTE))
        } else {
            calStart.add(Calendar.MINUTE, -60)
        }
        viewModel.addWorkout.startHour = calStart.get(Calendar.HOUR_OF_DAY)
        viewModel.addWorkout.startMinute = calStart.get(Calendar.MINUTE)
        setStartTimeBetween()

        setEndTimeBetween()
        updateCalculatedData()

    }

    override fun subscribeObservers() {
        viewModel.updateCalculatedData.observe(this) {
            it.getContent()?.let {
                updateCalculatedData()
            }
        }

        viewModel.updateDefaultWorkout.observe(viewLifecycleOwner) {
            it.getContent()?.let {

                setDefaultData(it)
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.autoSport.observe(this) {
            it?.let {
                if (it) {
                    viewModel.userDayData = mainViewModel.userHealthData[viewModel.addWorkout.date]
                    setPrefillData()

                    binding.lytStartEnd.lytDate.root.gone()
                    binding.lytStartEnd.divider0.root.gone()
                    //disableSelection()
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

                viewModel.sessionManager.saveSportsActivities(listOf(it.first))

                mainViewModel.sessionManager.reloadTodayData.postValue(
                    Event(true)
                )
                mainViewModel.sessionManager.forceSyncData.postValue(Event(true))

                //mainViewModel.reloadTodaysData()

                setFragmentResult(
                    ADD_WORKOUT_REQUEST_KEY,
                    bundleOf(
                        "allow" to true,
                        "workId" to it.second,
                        "actName" to it.first.getFormattedActivityName()
                    )

                )
                context.showShortToast("Workout Added Successfully")
                navigateUpSafe()
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