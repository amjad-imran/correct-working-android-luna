package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
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
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.OAddSleep
import com.oreo.ui.sleep2.add.OAddSleepViewModel
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
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

        // Start Time
        binding.lytCard.lytStartAndEndTime.lytDate.tvTimeValue.setOnClickListener {

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
        binding.lytCard.lytStartAndEndTime.lytTime.tvTimeValue.setOnClickListener {
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
                uiController.displayProgressBar(true,"")
            } else {
                uiController.displayProgressBar(false,"")
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
        binding.lytCard.tvSleepDuration.text = formatSecondsToHrMin(data.value?.toLong())

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

        binding.lytCard.lytStartAndEndTime.lytDate.tvTimeValue.isClickable = false
        binding.lytCard.lytStartAndEndTime.lytTime.tvTimeValue.isClickable = false

        when(data.canBeEditedOrDeleted){
            0 -> {
                binding.btnSave.gone()
            }

            else -> {
                binding.btnSave.disable()
            }
        }
    }

    fun formatSecondsToHrMin(totalSeconds: Long?): String {
        if(totalSeconds==null) return "--hr --min"
        return try {
            val h = TimeUnit.SECONDS.toHours(totalSeconds)
            val m = TimeUnit.SECONDS.toMinutes(totalSeconds) - TimeUnit.HOURS.toMinutes(h)
            "${h}hr ${m}min"
        }catch (_: Exception){
            "--hr --min"
        }
    }

    fun getDisplayFormatTime(date: String?, time: String?): String{
        return try {
            val day = if (LocalDate.now().toString().equals(date)) "Today"
            else "Yesterday"

            val inFmt  = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault())
            val outFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            val time = LocalTime.parse(time, inFmt)
                    .format(outFmt).uppercase()

            "$day, $time"
        }catch (_: Exception){
            "--"
        }
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
        val cg = binding.lytCard.chipGroupSleepEnv
        cg.removeAllViews()

        items.forEachIndexed { index, data ->
            val chip = Chip(requireContext(), null, com.google.android.material.R.attr.chipStyle).apply {
                setText(data.options)
                /*setChipBackgroundColorResource(R.color.chip_bg_color)
                setChipStrokeColorResource(R.color.chip_stroke)*/
                chipStrokeWidth = resources.getDimension(R.dimen.dimen_40dp)
                isCheckable = true
                isChecked = data.isChecked
                closeIcon = AppCompatResources.getDrawable(context, R.drawable.ic_hm_check_default)
                isCloseIconVisible = true
                isCloseIconEnabled = false
                closeIconTint = null
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                setEnsureMinTouchTargetSize(false)
                minHeight = resources.getDimensionPixelSize(R.dimen.dimen_40dp)
                setPadding(0, 0, 0, 0)
                setChipStartPadding(16f)
                setTextEndPadding(12f)
                setIconStartPadding(8f)
                setIconEndPadding(8f)

                //
                tag = index
                setOnCheckedChangeListener { btn, checked ->
                    val i = btn.tag as Int
                    items[i].isChecked = checked
                    binding.btnSave.enable()
                    viewModel.selectedOptMap[items[i].id as Int] = checked
                }
            }
            cg.addView(chip)
        }

        if(viewModel.editDataAddActivity?.canBeEditedOrDeleted == 0){
            for (i in 0 until cg.childCount){
                (cg.getChildAt(i) as? Chip)?.apply {
                    isCheckable = false
                    isClickable = false
                    isFocusable = false
                }
            }
        }

    }

}
