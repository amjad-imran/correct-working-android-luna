package com.oreo.ui.timelineScreen.addActivity.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.moengage.core.internal.utils.showToast
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.local.AppStaticData
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddWorkoutBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.constants.SportActivityName
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.data.model.timeline.ItemTimelineResponseModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.StringUtils.capitalizeWords
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import com.oreo.ui.workout.add.OAddWorkoutViewModel
import com.oreo.ui.workout.add.SELECT_REQUEST_KEY
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class AddWorkoutFragment :
    BaseFragment<FragmentAddWorkoutBinding>(FragmentAddWorkoutBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()

    private val viewModel: OAddWorkoutViewModel by viewModels()

    private val mainViewModel: OreoMainViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //viewModel.movementList = args.movementList?.toList()
        //if (args.autoSport != null) {
        //   viewModel.convertAutoSport(args.autoSport)
        //} else {
        viewModel.editData = arguments?.getParcelable("editData")
        viewModel.userDayData =
            mainViewModel.userHealthData[DateFormats.getTodaysDateString(10)]
        //}


        viewModel.getWorkoutList(false)

    }

    private fun setToolbar() {
        binding.btnSave.disable()
        /*binding.lytToolbar.apply {
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
        }*/
    }

    private fun goToSelectWorkout(workoutList: List<OWorkoutListModal>) {
        val navController =
            NavHostFragment.Companion.findNavController(this@AddWorkoutFragment)

        navController.navigate(
            R.id.selectWorkoutBottomSheet, bundleOf("workoutList" to workoutList.toTypedArray())
        )
    }

    private fun setWorkout(workout: OWorkoutListModal) {
        viewModel.workoutListModal = workout

        context?.let { ctx ->
            binding.lytCard.ivWorkoutImage.loadImage(ctx, workout.iconUrl)
        }

        binding.lytCard.tvWorkout.text = workout.getTranslatedActivityName()
        /*setCalories()
        enableSaveBtn()*/
        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_add_workout + "_${workout.activityType}_CLICK")

        updateCalculatedData()
    }

    override fun initListener() {

        setToolbar()
        setDefaultUIValue()

        binding.lytSelected.setOnClickListener {
            sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ACTIVITIES_LISTING)
        }

        binding.btnSave.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_add_workout_save_click)
            viewModel.addWorkout(sharedViewModel.sourceKey)
        }

        parentFragment?.setFragmentResultListener(SELECT_REQUEST_KEY) { _, bundle ->
            val workout = bundle.getParcelable<OWorkoutListModal>("workout")
            workout?.let {
                setWorkout(it)
            }
        }

        binding.lytCard.ivDropDown.setOnClickListener {
            if (viewModel.oWorkoutListModalResponse.value.isNullOrEmpty()) {
                viewModel.getWorkoutList(true)
            } else {
                goToSelectWorkout(viewModel.oWorkoutListModalResponse.value!!)
            }
        }


        binding.lytCard.tvDate.setOnClickListener {
            onDateClicked()



        }

        binding.lytCard.icArrow.setOnClickListener {
            onDateClicked()
        }


        binding.lytCard.lytStartTime.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_add_workout_start_time_click)

            parentFragment?.setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
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

            val navController =
                NavHostFragment.Companion.findNavController(this@AddWorkoutFragment)


            navController.navigate(
                R.id.timeBottomSheet,
                bundleOf(
                    "hour" to viewModel.addWorkout.startHour,
                    "minute" to viewModel.addWorkout.startMinute,
                    "hourOther" to (viewModel.addWorkout.endHour ?: 0),
                    "minuteOther" to (viewModel.addWorkout.endMinute ?: 0),
                    "isStart" to 1,
                    "unitPosition" to 1,
                    "title" to getString(R.string.text_start_time)
                )
            )
        }
        binding.lytCard.lytEndTime.setOnClickListener {

            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_add_workout_end_time_click)
            if (binding.lytCard.tvStartTime.text == getString(R.string.text_enter)) {
                context.showShortToast(getString(R.string.text_select_start_time_first))
                return@setOnClickListener
            }
            parentFragment?.setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
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

            val navController =
                NavHostFragment.Companion.findNavController(this@AddWorkoutFragment)


            navController.navigate(
                R.id.timeBottomSheet,
                bundleOf(
                    "hour" to (viewModel.addWorkout.endHour ?: 0),
                    "minute" to (viewModel.addWorkout.endMinute ?: 0),
                    "hourOther" to (viewModel.addWorkout.startHour),
                    "minuteOther" to (viewModel.addWorkout.startMinute),
                    "isStart" to 0,
                    "unitPosition" to 1,
                    "title" to getString(R.string.text_end_time)
                )
            )
        }
        binding.lytCard.viewIntensity.setOnClickListener {
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_add_workout_intensity_click)
            parentFragment?.setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { it1 ->
                    viewModel.addWorkout.intensity = it1
                    setIntensity()
                    //setCalories()
                    viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_workout + "_${it1}_LEVEL_CLICK")
                    updateCalculatedData()
                }

            }

            val navController =
                NavHostFragment.Companion.findNavController(this@AddWorkoutFragment)

            navController.navigate(
                R.id.valueSelectorBottomSheet,
                bundleOf(
                    "selectedValue" to viewModel.addWorkout.intensity,
                    "selectionList" to AppStaticData.getIntensityValues(),
                    "title" to  getString(R.string.text_intensity)
                )
            )
        }

        binding.lytSelected.setOnClickListener {
            sharedViewModel.showDropdownDialog(binding.lytSelected, AddActivityItemsEnum.WORKOUT)
        }

    }

    private fun onDateClicked() {
        parentFragment?.setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
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

        val navController =
            NavHostFragment.Companion.findNavController(this@AddWorkoutFragment)

        navController.navigate(
            R.id.valueSelectorBottomSheet,
            bundleOf(
                "selectedValue" to if (viewModel.addWorkout.date == null) null else LocalDate.parse(viewModel.addWorkout.date)
                    .format(format),
                "selectionList" to viewModel.getWorkoutDates(),
                "title" to  getString(R.string.text_date)
            )
        )

    }

    private fun updateCalculatedData() {

        val duration = viewModel.getWorkoutDuration()
        if (duration > 0) {
            viewModel.addWorkout.duration = duration
            binding.lytCard.lytCaloriesBurn.tvDurationValue.text = duration.toString()
        } else {
            viewModel.addWorkout.duration = 0
            binding.lytCard.lytCaloriesBurn.tvDurationValue.text = "-"
        }

        val calories = viewModel.getCaloriesBurnt()
        viewModel.addWorkout.calories = calories
        binding.lytCard.lytCaloriesBurn.tvCalBurnValue.text = if (calories > 0) {
            "$calories"
        } else {
            "--"
        }


        /*if (viewModel.preFilledOreoAutoSportData != null) {
            val highlightedPoints = viewModel.getHighlightedPoints()
            binding.movementChart.setHighlightedPoints(highlightedPoints)

        }*/



        enableSaveBtn()

    }

    private fun setStartTimeBetween() {
        val startTime = DateFormats.formatTimeWithAmPm(
            viewModel.addWorkout.startHour,
            viewModel.addWorkout.startMinute
        )
//
//        if (viewModel.oAddWorkout.startHour != 0) {
        setTextWhite(binding.lytCard.tvStartTime)
//        }

        viewModel.addWorkout.startTimeIn24H = DateFormats.formatTime(
            viewModel.addWorkout.startHour,
            viewModel.addWorkout.startMinute
        )

        /*if (viewModel.isStartTimeSelected && viewModel.isEndTimeSelected) {
            setDuration()
        }*/
        binding.lytCard.tvStartTime.text = startTime
        viewModel.isStartTimeSelected = true
    }

    private fun setEndTimeBetween(ignoreDuration: Boolean = true) {
        if (viewModel.addWorkout.endHour == null || viewModel.addWorkout.endMinute == null) {
            viewModel.isEndTimeSelected = false
            binding.lytCard.tvEndTime.text = "Enter"
        } else {
            val endTime = DateFormats.formatTimeWithAmPm(
                viewModel.addWorkout.endHour!!,
                viewModel.addWorkout.endMinute!!
            )

            setTextWhite(binding.lytCard.tvEndTime)
            viewModel.addWorkout.endTimeIn24H = DateFormats.formatTime(
                viewModel.addWorkout.endHour!!,
                viewModel.addWorkout.endMinute!!
            )
            /*if (ignoreDuration) {
                setDuration()
            }*/
            viewModel.isEndTimeSelected = true
            binding.lytCard.tvEndTime.text = endTime
        }

    }

    private fun setIntensity() {
        var intensity = "Enter"

        if (viewModel.addWorkout.intensity.isNotEmpty()) {
            setTextWhite(binding.lytCard.lytItem.tvTimeValue)
            intensity = viewModel.addWorkout.intensity
            enableSaveBtn()
        }
        binding.lytCard.lytItem.tvTimeValue.text = viewModel.getTranslatedIntensity(intensity)
    }

    private fun setDate() {
        var date = "Enter"

        if (viewModel.addWorkout.date.isNullOrEmpty().not()) {
            setTextWhite(binding.lytCard.lytItem.tvTimeValue)
            date = LocalDate.parse(viewModel.addWorkout.date)
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        } else {
            date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")).toString()
            viewModel.addWorkout.date = LocalDate.now().toString()
        }

        binding.lytCard.tvDate.text = date
    }


    private fun setTextWhite(textView: TextView) {
        textView.setTextColor(Color.WHITE)
    }

    private fun setDefaultUIValue() {
        binding.lytCard.ivWorkoutImage.setImageResource(R.drawable.ic_o_workout_new)
        binding.lytCard.lytCaloriesBurn.tvDurationValue.text = "--"
        binding.lytCard.lytCaloriesBurn.tvDurationUnit.text = getString(R.string.text_min)
        binding.lytCard.lytCaloriesBurn.tvCalBurnValue.text = "--"
        binding.lytCard.lytCaloriesBurn.tvCalBurnUnit.text = getString(R.string.text_kcal)

        binding.lytCard.tvStartTime.text = getString(R.string.text_enter)

        binding.lytCard.tvEndTime.text = getString(R.string.text_enter)

        binding.lytCard.lytItem.tvTime.text = getString(R.string.text_intensity)
        binding.lytCard.lytItem.tvTimeValue.text = getString(R.string.text_enter)

    }

    private fun enableSaveBtn() {

        val forceSave = viewModel.isAutoWorkout()

        if ((viewModel.addWorkout.duration > 0 &&
                    viewModel.addWorkout.intensity.isNotEmpty() &&
                    viewModel.workoutListModal != null) || forceSave
        ) {
            binding.btnSave.enable()
        } else {
            binding.btnSave.disable()
        }
    }

    private fun getTranslatedName(
        workoutName: String?,
        resourcesProvider: ResourcesProvider
    ): String {
        if (workoutName.isNullOrEmpty()) {
            return ""
        }

        return when (workoutName.lowercase()) {
            SportActivityName.RUNNING -> {
                resourcesProvider.getString(R.string.text_running).capitalizeWords()
            }

            else -> {
                resourcesProvider.getString(R.string.text_walking).capitalizeWords()
            }
        }
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
                if(viewModel.editData == null) {
                    setDefaultData(it)
                }else{
                    setEditLayout(viewModel.editData!!, it)
                }
            }
        }

        sharedViewModel.deleteBtnClickedEvent.observe(this){
            it.getContent()?.let {
                if(it) {
                    if(viewModel.editData?.id == null){
                        showToast(requireContext(),
                            getString(R.string.text_something_went_wrong_please_try_again))
                        return@observe
                    }
                    viewModel.deleteWorkoutItem()
                }
            }
        }

        //
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

                viewModel.sessionManager.saveSportsActivities(listOf(it.first))

                mainViewModel.sessionManager.reloadTodayData.postValue(
                    Event(true)
                )
                mainViewModel.sessionManager.forceSyncData.postValue(Event(true))

                //mainViewModel.reloadTodaysData()

               /* setFragmentResult(
                    ADD_WORKOUT_REQUEST_KEY,
                    bundleOf(
                        "allow" to true,
                        "workId" to it.second,
                        "actName" to it.first.getFormattedActivityName()
                    )

                )*/
                context.showShortToast(getString(R.string.text_workout_added_successfully))

                uiController.logAppEvent(
                    MoEngageLunaAppEvents.past_workout_entry,
                    hashMapOf(
                        "source" to "activity",
                        "description" to "workout_start",
                        "workout_name" to "${it.first.activityType}"
                    )
                )
                sharedViewModel.navigateUp()
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                uiController.displayProgressBar(true,"")
            } else {
                uiController.displayProgressBar(false,"")
            }
        }
    }

    private fun setEditLayout(data: ItemTimelineResponseModel, workout: OWorkoutListModal){
        binding.btnSave.gone()

        // Set Workout
        binding.lytCard.ivWorkoutImage.loadImage(requireContext(), workout.iconUrl)
        binding.lytCard.tvWorkout.text = workout.getTranslatedActivityName()

        // Set Duration And Cals
        binding.lytCard.lytCaloriesBurn.tvDurationValue.text = data.value ?: "--"
        binding.lytCard.lytCaloriesBurn.tvDurationUnit.text = getString(R.string.text_min)

        binding.lytCard.lytCaloriesBurn.tvCalBurnValue.text = data.metadata?.calories.toString()
        binding.lytCard.lytCaloriesBurn.tvCalBurnUnit.text = getString(R.string.text_kcal)

        // Set Date, Start And End Time
        binding.lytCard.tvDate.text = LocalDate.parse(data.date)
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

        val inFmt  = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault())
        val outFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

        binding.lytCard.tvStartTime.text = LocalTime.parse(data.startTime, inFmt)
            .format(outFmt).uppercase()

        binding.lytCard.tvEndTime.text = LocalTime.parse(data.endTime, inFmt)
            .format(outFmt).uppercase()

        // Set Intensity
        binding.lytCard.lytItem.tvTimeValue.text = data.metadata?.intensity ?: "-"

        // Disable btns
        binding.lytCard.ivDropDown.isClickable = false

        binding.lytCard.tvDate.isClickable = false
        binding.lytCard.icArrow.isClickable = false

        binding.lytCard.lytStartTime.isClickable = false
        binding.lytCard.lytEndTime.isClickable = false

        binding.lytCard.viewIntensity.isClickable = false
    }

}