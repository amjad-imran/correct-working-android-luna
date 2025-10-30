package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.chip.Chip
import com.noisefit.data.model.timeline.SupplementOption
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddSleepBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.SLEEP_TIME_REQUEST_KEY
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.model.timeline.ItemTimelineResponseModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.OAddSleep
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.sleep2.add.OAddSleepViewModel
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.getValue

@AndroidEntryPoint
class AddSleepFragment :
    BaseFragment<FragmentAddSleepBinding>(FragmentAddSleepBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
    private val viewModel: OAddSleepViewModel by viewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()


    override fun initListener() {
        binding.lytSelected.setOnClickListener {
            sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ACTIVITIES_LISTING)
        }
        binding.btnSave.setOnClickListener {
            if (mainViewModel.isDeviceConnected().not()) {
                context.showShortToast(getString(R.string.text_please_connect_your_ring_to_add_sleep))
                return@setOnClickListener
            }

            if(getString(R.string.text_save).equals(binding.btnSave.text)) {

                if (viewModel.editDataAddActivity?.id != null) {
                    viewModel.submitSleepEnvOptions() {
                        sharedViewModel.sessionManager.logMoEngageAppEvent(
                            MoEngageLunaAppEvents.insight_log_edited,
                        )
                        mainViewModel.sessionManager.reloadOnResume = true
                        sharedViewModel.navigateUp()
                        mainViewModel.sleepDashTodayReload.value = Event(true)
                    }
                } else {

                    if (viewModel.startTimeSleep.day.isEmpty()) {
                        uiController.onDisplayError(getString(R.string.text_please_select_start_time))
                        return@setOnClickListener
                    }
                    if (viewModel.endTimeSleep.day.isEmpty()) {
                        uiController.onDisplayError(getString(R.string.text_please_select_end_time))
                        return@setOnClickListener
                    }
                    /*if (viewModel.getSleepDuration() < (3 * 60 * 60)) {
                uiController.onDisplayError("Sleep duration should be minimum of 3 hours")
                return@setOnClickListener
            }*/


                    viewModel.callApiToAddSleep()
                }

            }
            else{

                if (sharedViewModel.localDataStore.isAiChatSplashShown()) {
                    navigate(
                        R.id.aiTopQuestionsFragment,
                        bundleOf("aiTopic" to AITopics.SLEEP)
                    )
                } else {
                    navigate(R.id.aiChatOnboardFragment)
                }

            }
        }

        // Start Time
        binding.lytCard.lytStartAndEndTime.lytDate.lytLlInput.setOnClickListener {

            parentFragment?.setFragmentResultListener(SLEEP_TIME_REQUEST_KEY) { _, bundle ->
                val addSleep = bundle.getParcelable<OAddSleep>("sleepTime")

                addSleep?.let {
                    val oldData = viewModel.startTimeSleep.copy()
                    viewModel.startTimeSleep = addSleep

                    if (addSleep.hour.toInt() == 0) {
                        viewModel.startTimeSleep = addSleep.apply {
                            this.day = "Today"
                        }
                    }

                    if (!viewModel.isEndTimeSelected) {
                        setStartTimeBetween()
                    } else if (viewModel.startTimeSleep.day.equals("Today", true) &&
                        viewModel.endTimeSleep.day.equals("Today", true)
                    ) {

                        val startTime = LocalTime.of(
                            viewModel.startTimeSleep.hour.toInt(),
                            viewModel.startTimeSleep.minute.toInt()
                        )
                        val endTime = LocalTime.of(
                            viewModel.endTimeSleep.hour.toInt(),
                            viewModel.endTimeSleep.minute.toInt()
                        )

                        if (startTime < endTime) {
                            setStartTimeBetween()
                        } else {
                            uiController.onDisplayError(getString(R.string.text_start_time_should_be_less_than_end_time))
                            viewModel.startTimeSleep = oldData
                        }

                    } else if (viewModel.startTimeSleep.day.equals("Yesterday", true) &&
                        viewModel.endTimeSleep.day.equals("Yesterday", true)
                    ) {
                        val startTime = LocalTime.of(
                            viewModel.startTimeSleep.hour.toInt(),
                            viewModel.startTimeSleep.minute.toInt()
                        )
                        val endTime = LocalTime.of(
                            viewModel.endTimeSleep.hour.toInt(),
                            viewModel.endTimeSleep.minute.toInt()
                        )

                        if (startTime < endTime) {
                            setStartTimeBetween()
                        } else {
                            uiController.onDisplayError(getString(R.string.text_start_time_should_be_less_than_end_time))
                            viewModel.startTimeSleep = oldData
                        }

                    } else if (viewModel.startTimeSleep.day.equals("Yesterday", true) &&
                        viewModel.endTimeSleep.day.equals("Today", true)
                    ) {


                        setStartTimeBetween()
                    } else if (viewModel.startTimeSleep.day.equals("Today", true) &&
                        viewModel.endTimeSleep.day.equals("Yesterday", true)
                    ) {

                        uiController.onDisplayError(getString(R.string.text_please_check_end_time))
                        viewModel.startTimeSleep = oldData
                    }

                    updateCalculatedData()
                }

            }
            viewModel.startTimeSleep.title = getString(R.string.text_start_time)


            val navController =
                NavHostFragment.Companion.findNavController(this@AddSleepFragment)

            navController.navigate(R.id.sleepTimeBottomSheet, bundleOf(
                "addSleep" to viewModel.startTimeSleep.copy(),
                "isStartDateToday" to false))
        }

        // End Time
        binding.lytCard.lytStartAndEndTime.lytTime.lytLlInput.setOnClickListener {
            if (binding.lytCard.lytStartAndEndTime.lytDate.tvTimeValue.text == getString(R.string.text_enter)) {
                context.showShortToast(getString(R.string.text_select_start_time_first))
                return@setOnClickListener
            }
            parentFragment?.setFragmentResultListener(SLEEP_TIME_REQUEST_KEY) { _, bundle ->
                val addSleep = bundle.getParcelable<OAddSleep>("sleepTime")

                addSleep?.let {
                    val oldData = viewModel.endTimeSleep.copy()
                    viewModel.endTimeSleep = addSleep

                    if (viewModel.startTimeSleep.day.equals("Today", true) &&
                        viewModel.endTimeSleep.day.equals("Today", true)
                    ) {
                        val startTime = LocalTime.of(
                            viewModel.startTimeSleep.hour.toInt(),
                            viewModel.startTimeSleep.minute.toInt()
                        )
                        val endTime = LocalTime.of(
                            viewModel.endTimeSleep.hour.toInt(),
                            viewModel.endTimeSleep.minute.toInt()
                        )
                        if (startTime < endTime) {
                            setEndTimeBetween()
                        } else {
                            viewModel.endTimeSleep = oldData
                            uiController.onDisplayError(getString(R.string.text_end_time_must_be_later_than_the_start_time))
                        }

                    } else if (viewModel.startTimeSleep.day.equals("Yesterday", true) &&
                        viewModel.endTimeSleep.day.equals("Yesterday", true)
                    ) {
                        val startTime = LocalTime.of(
                            viewModel.startTimeSleep.hour.toInt(),
                            viewModel.startTimeSleep.minute.toInt()
                        )
                        val endTime = LocalTime.of(
                            viewModel.endTimeSleep.hour.toInt(),
                            viewModel.endTimeSleep.minute.toInt()
                        )
                        if (startTime < endTime) {
                            setEndTimeBetween()
                        } else {
                            viewModel.endTimeSleep = oldData
                            uiController.onDisplayError(getString(R.string.text_end_time_must_be_later_than_the_start_time))
                        }

                    } else if (viewModel.startTimeSleep.day.equals("Yesterday", true) &&
                        viewModel.endTimeSleep.day.equals("Today", true)
                    ) {
                        setEndTimeBetween()
                    } else if (viewModel.startTimeSleep.day.equals("Today", true) &&
                        viewModel.endTimeSleep.day.equals("Yesterday", true)
                    ) {
                        viewModel.endTimeSleep = oldData
                        uiController.onDisplayError(getString(R.string.text_please_check_end_time))
                    }
                }


                updateCalculatedData()
            }

            viewModel.endTimeSleep.title = getString(R.string.text_end_time)


            val navController =
                NavHostFragment.Companion.findNavController(this@AddSleepFragment)

            navController.navigate(R.id.sleepTimeBottomSheet, bundleOf(
                "addSleep" to viewModel.endTimeSleep.copy(),
                "isStartDateToday" to viewModel.isStartDateToday()))

        }
//        binding.tvDeleteSleep.setOnClickListener {
//            //wrote code to delete sleep
//        }

        binding.lytSelected.setOnClickListener {
            sharedViewModel.showDropdownDialog(binding.lytSelected, AddActivityItemsEnum.SLEEP)
        }
    }

    private fun updateCalculatedData() {
        val duration = viewModel.getSleepDuration()
        LOGS.d("updateCalculatedData $duration")
        val (hour, minute) = ApplicationUtils.getFormattedSleepDurationFromSeconds(duration.toInt())

        val text = StringBuilder()
        text.append(if (hour > 0) {
            hour.toString()
        } else {
            "--"
        })
        text.append("hr")
        text.append(" ")

        text.append(if (minute >= 0) {
            minute.toString()
        } else {
            "--"
        })
        text.append("min")

        binding.lytCard.tvSleepDuration.text = text.toString()
    }

    private fun setEndTimeBetween() {
        val endTime = DateFormats.formatTimeWithAmPm(
            viewModel.endTimeSleep.hour.toInt(),
            viewModel.endTimeSleep.minute.toInt()
        )
        viewModel.isEndTimeSelected = true
        binding.lytCard.lytStartAndEndTime.lytTime.tvTimeValue.setTextColor(resources.getColor(R.color.white))
        binding.lytCard.lytStartAndEndTime.lytTime.tvTimeValue.text = "${viewModel.endTimeSleep.day}, $endTime"
        if (viewModel.isStartTimeSelected && viewModel.isEndTimeSelected) {
            binding.btnSave.enable()
        }
    }

    private fun setStartTimeBetween() {
        val startTime = DateFormats.formatTimeWithAmPm(
            viewModel.startTimeSleep.hour.toInt(),
            viewModel.startTimeSleep.minute.toInt()
        )
        binding.lytCard.lytStartAndEndTime.lytDate.tvTimeValue.setTextColor(resources.getColor(R.color.white))
        binding.lytCard.lytStartAndEndTime.lytDate.tvTimeValue.text =
            "${viewModel.startTimeSleep.day}, $startTime"
        viewModel.isStartTimeSelected = true
    }

    override fun subscribeObservers() {
        viewModel.addSleepResponse.observe(this) { it1 ->
            it1?.getContent().let {
                if (it == true) {
                    sharedViewModel.localDataStore.setNudgeReadinessData(null)
                    mainViewModel.sessionManager.reloadOnResume = true
                    sharedViewModel.navigateUp()
                    mainViewModel.sleepDashTodayReload.value = Event(true)
                }
            }
        }

        viewModel.sleepEnvOptListData.observe(this){
            setSleepEnvChipsData(it)
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

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.editDataAddActivity = arguments?.getParcelable("editData")

        if(viewModel.editDataAddActivity==null) {
            initUI()
        }else{
            viewModel.getSleepEnvOptionsList()
            setEditLayout(viewModel.editDataAddActivity!!)
        }
    }

    private fun setEditLayout(data: ItemTimelineResponseModel) {
        // Set Sleep Duration
        binding.lytCard.tvSleepDuration.text = getSleepDuration(
            data.startDate,
            data.startTime,
            data.endDate,
            data.endTime,
            )

        // Set Start Date & Time
        binding.lytCard.lytStartAndEndTime.lytDate.tvTime.text = getString(R.string.text_start_time)
        binding.lytCard.lytStartAndEndTime.lytTime.tvTime.text = getString(R.string.text_end_time)

        binding.lytCard.lytStartAndEndTime.lytDate.tvTimeValue.apply {
            text = getDisplayFormatTime(
                data.startDate, data.startTime
            )
        }

        // Set End Time
        binding.lytCard.lytStartAndEndTime.lytTime.tvTimeValue.apply {
            text = getDisplayFormatTime(
                data.endDate, data.endTime
            )
        }

        // Other things
        binding.lytCard.lytStartAndEndTime.lytDate.ivMore.gone()
        binding.lytCard.lytStartAndEndTime.lytTime.ivMore.gone()

        binding.lytCard.lytStartAndEndTime.lytDate.lytLlInput.isClickable = false
        binding.lytCard.lytStartAndEndTime.lytTime.lytLlInput.isClickable = false

        when(data.canBeEditedOrDeleted){
            0 -> {
                binding.btnSave.gone()
            }

            else -> {
                binding.btnSave.apply {
                    text = getString(R.string.text_learn_more_with_luna_ai)
                    enable()
                    visible()
                }
            }
        }
    }

    fun getSleepDuration(
        startDate: String?,
        startTime: String?,
        endDate: String?,
        endTime: String?
    ): String {
        return try {
            // Define the format for time
            val timeFormatter12Hour = DateTimeFormatter.ofPattern("h:mm a")

            // Parse the start and end dates into LocalDate objects
            val startDateObj = LocalDateTime.parse(
                "$startDate $startTime",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            )
            val endDateObj = LocalDateTime.parse(
                "$endDate $endTime",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            )

            // If the end time is before the start time, adjust the end time to the next day
            val adjustedEndDateObj = if (endDateObj.isBefore(startDateObj)) {
                endDateObj.plusDays(1)
            } else {
                endDateObj
            }

            // Calculate the duration between start and end times
            val duration = Duration.between(startDateObj, adjustedEndDateObj)
            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60

            "$hours hr $minutes m"
        } catch (e: Exception) {
            LOGS.e("TIMELINE_GET_SLEEP_DURATION_EXCEPTION : $e")
            "--hr --min"
        }
    }

    fun getDisplayFormatTime(date: String?, time: String?): String{
        return try {
            val day = if (getDate(0).equals(date)) "Today"
            else if (getDate(1).equals(date)) "Yesterday"
            else LocalDate
                .parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

            val inFmt  = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault())
            val outFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            val time = LocalTime.parse(time, inFmt)
                    .format(outFmt).uppercase()

            "$day, $time"
        }catch (_: Exception){
            "--"
        }
    }

    fun getDate(prevDayNum: Long): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDate.now().minusDays(prevDayNum).format(formatter)
    }

    private fun initUI() {

        binding.lytCard.lytSleepEnvHeader.gone()
        binding.lytCard.chipGroupSleepEnv.gone()

        binding.btnSave.disable()

        binding.lytCard.tvSleepDuration.text = "--hr --min"

        binding.lytCard.lytStartAndEndTime.lytDate.tvTime.text = getString(R.string.text_start_time)
        binding.lytCard.lytStartAndEndTime.lytTime.tvTime.text = getString(R.string.text_end_time)

        binding.lytCard.lytStartAndEndTime.lytDate.tvTimeValue.apply {
            text = getString(R.string.text_enter)
            setTextColor("#FFFFFF".toColorInt())
        }
        binding.lytCard.lytStartAndEndTime.lytTime.tvTimeValue.apply {
            text = getString(R.string.text_enter)
            setTextColor("#FFFFFF".toColorInt())
        }

//        if (args.launchMode == OAddSleepLaunchState.ADD) {
//            binding.tvDeleteSleep.gone()
//        } else {
//            binding.tvDeleteSleep.visible()
//        }
    }

    private fun setSleepEnvChipsData(items: ArrayList<SupplementOption>) {
        val canBeViewedOnly = viewModel.editDataAddActivity?.canBeEditedOrDeleted == 0
        val checkedImg = if(canBeViewedOnly) R.drawable.ic_sleep_env_chip_box_selected
                        else R.drawable.ic_sleep_env_chip_box_checked

        val cg = binding.lytCard.chipGroupSleepEnv
        cg.removeAllViews()

        items.forEachIndexed { index, data ->
            val chip = layoutInflater.inflate(R.layout.layout_sleep_env_chip, cg, false)
            val tv = chip.findViewById<TextView>(R.id.tvLabel)
            val iv = chip.findViewById<ImageView>(R.id.ivBox)

            tv.text = data.options
            iv.setImageResource(if (data.isChecked) checkedImg else R.drawable.ic_sleep_env_chip_box_unchecked)

            if(canBeViewedOnly){
                chip.isClickable = false
                chip.isFocusable = false
            }else {
                chip.setOnClickListener {
                    val newChecked = !data.isChecked
                    data.isChecked = newChecked
                    iv.setImageResource(if (newChecked) checkedImg else R.drawable.ic_sleep_env_chip_box_unchecked)
                    binding.btnSave.text = getString(R.string.text_save)
                    binding.btnSave.enable()
                    viewModel.selectedOptMap[data.id as Int] = newChecked
                }
            }
            cg.addView(chip)
        }

    }

}
