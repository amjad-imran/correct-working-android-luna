package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moengage.core.internal.utils.showToast
import com.noisefit.data.model.timeline.SupplementOption
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddRecoveryBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.data.model.timeline.ItemTimelineResponseModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.getValue
import kotlin.math.roundToInt

@AndroidEntryPoint
class AddRecoveryFragment : BaseFragment<FragmentAddRecoveryBinding>(FragmentAddRecoveryBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
    private val viewModel: AddRecoveryViewModel by viewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    private val optAdapter by lazy {
        RecoveryOptionsAdapter(){
            handleOnOptionClicked(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSave.disable()
        viewModel.editData = arguments?.getParcelable("editData")
        viewModel.lunaOption = arguments?.getString("lunaOption")
        viewModel.getRecoveryOptionsList()
        setAdapter()
        setDefaultData(viewModel.editData)
        viewModel.editData?.canBeEditedOrDeleted?.let {
            setEditLayout(it)
        }
    }

    private fun setDefaultData(editData: ItemTimelineResponseModel ?= null) {
        setDate(editData)
        setDefaultStartAndEndTime()
    }

    private fun setDefaultStartAndEndTime() {
        if(viewModel.editData == null){
            viewModel.endTime = LocalTime.now()
            viewModel.startTime = viewModel.endTime?.minusMinutes(60)
        }else{
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            try {
                viewModel.endTime = LocalTime.parse(viewModel.editData?.endTime, formatter)
                viewModel.startTime = LocalTime.parse(viewModel.editData?.startTime, formatter)
            }catch (_: Exception){
                viewModel.endTime = LocalTime.now()
                viewModel.startTime = viewModel.endTime?.minusMinutes(60)
            }
        }

        viewModel.endTime?.let { binding.tvEndTime.text = getFormattedTimeString(it) }
        viewModel.startTime?.let { binding.tvStartTime.text = getFormattedTimeString(it) }
    }

    private fun getFormattedTimeString(time: LocalTime): String{
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
        return time.format(formatter).uppercase()
    }

    private fun setAdapter() {
        binding.rvOptions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = optAdapter
        }
    }

    private fun handleOnOptionClicked(option: SupplementOption) {
        viewModel.selectedOption = option
        binding.tvSelected.text = option.options
        binding.rvOptions.gone()
        binding.btnSave.text = getString(R.string.text_save)
        if(!binding.btnSave.isEnabled) binding.btnSave.enable()
    }

    override fun initListener() {

        binding.tvDate.setOnClickListener {
            onDateClicked()
        }

        binding.lytSelected.setOnClickListener {
            handleDropDown()
        }

        binding.lytStartTime.setOnClickListener {
            handleStartOrEndTimeClicked(true)
        }

        binding.lytEndTime.setOnClickListener {
            handleStartOrEndTimeClicked(false)
        }

        binding.btnSave.setOnClickListener {
            if(viewModel.selectedOption==null){
                showToast(requireActivity(), "Please select an option")
                return@setOnClickListener
            }

            if(getString(R.string.text_save).equals(binding.btnSave.text)) {
                viewModel.logRecovery(){
                    sharedViewModel.sessionManager.logMoEngageAppEvent(
                        MoEngageLunaAppEvents.insight_log_edited,
                    )
                }
            }else{
                if (sharedViewModel.ringDataStore.getRingDevice() == null) {
                    context.showShortToast(getString(R.string.text_luna_ai_message))
                }else {
                    if (sharedViewModel.localDataStore.isAiChatSplashShown()) {
                        navigate(
                            R.id.aiTopQuestionsFragment,
                            bundleOf("aiTopic" to AITopics.GENERAL)
                        )
                    } else {
                        navigate(R.id.aiChatOnboardFragment)
                    }
                }
            }

        }
    }

    private fun handleStartOrEndTimeClicked(isStartTimeClicked: Boolean) {
        parentFragment?.setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
            val hourOfDay = bundle.getInt("hour")
            val minute = bundle.getInt("minute")

            val time = LocalTime.of(hourOfDay, minute)
            if (time > LocalTime.now()) {
                val errorMsg = if(isStartTimeClicked){
                    getString(R.string.text_start_time_less_then_current_time)
                }else{
                    getString(R.string.text_end_time_greater_then_current_time)
                }
                context.showShortToast(errorMsg)
                return@setFragmentResultListener
            }

            if(isStartTimeClicked){
                if (time>=viewModel.endTime) {
                    showToast(requireActivity(), getString(R.string.text_start_time_less))
                    return@setFragmentResultListener
                }
                viewModel.startTime = time
                binding.tvStartTime.text = getFormattedTimeString(time)
                binding.btnSave.text = getString(R.string.text_save)
            }else{
                if (time<=viewModel.startTime) {
                    showToast(requireActivity(), getString(R.string.text_end_time_greater))
                    return@setFragmentResultListener
                }
                viewModel.endTime = time
                binding.tvEndTime.text = getFormattedTimeString(time)
                binding.btnSave.text = getString(R.string.text_save)
            }

        }

        val navController =
            NavHostFragment.Companion.findNavController(this@AddRecoveryFragment)

        val curTime = if(isStartTimeClicked) viewModel.startTime else viewModel.endTime

        navController.navigate(
            R.id.timeBottomSheet,
            bundleOf(
                "hour" to curTime?.hour,
                "minute" to curTime?.minute,
                "hourOther" to 0,
                "minuteOther" to 0,
                "isStart" to 1,
                "unitPosition" to 1,
                "title" to getString(R.string.text_time)
            )
        )
    }

    private fun handleDropDown() {
        if(viewModel.isDropdownOpen){
            binding.rvOptions.gone()
        }else{
            binding.rvOptions.visible()
        }
        viewModel.isDropdownOpen = !viewModel.isDropdownOpen
    }

    override fun subscribeObservers() {
        viewModel.recoveryListData.observe(this){
            optAdapter.updateDataSet(it)
            if(
                viewModel.isListLoadedFirstTime &&
                viewModel.editData?.metadata?.lunaTrackingOptionId != null
            ){
                it.first { it.id == (viewModel.editData?.metadata?.lunaTrackingOptionId ?: -1) }?.let { sOpt ->
                    binding.tvSelected.text = sOpt.options
                    viewModel.selectedOption = sOpt
                    binding.btnSave.apply {
                        text = getString(R.string.text_learn_more_with_luna_ai)
                        enable()
                        visible()
                    }
                }
                viewModel.isListLoadedFirstTime = false
            }
            else if(viewModel.lunaOption != null){
                it.find { it.options.equals(viewModel.lunaOption, true) }?.let { sOpt ->
                    binding.tvSelected.text = sOpt.options
                    viewModel.selectedOption = sOpt
                    binding.btnSave.apply {
                        text = getString(R.string.text_learn_more_with_luna_ai)
                        enable()
                        visible()
                    }
                }
                viewModel.isListLoadedFirstTime = false
            }

            binding.rvOptions.doOnNextLayout {
                capRvHeightToPercent(binding.rvOptions, binding.root, 0.70f)
            }
        }

        sharedViewModel.deleteBtnClickedEvent.observe(this){
            it.getContent()?.let {
                if(it){
                    viewModel.deleteRecoveryItem(){
                        sharedViewModel.sessionManager.logMoEngageAppEvent(
                            MoEngageLunaAppEvents.insight_log_deleted,
                        )
                    }
                }
            }
        }

        viewModel.onAddSuccess.observe(this){
            it.getContent()?.let {
                mainViewModel.sessionManager.reloadOnResume = true
                sharedViewModel.navigateUp()
            }
        }

        //
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
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

    private fun capRvHeightToPercent(rv: RecyclerView, root: View, percent: Float) {
        val rootH = root.height.takeIf { it > 0 } ?: root.measuredHeight
        if (rootH <= 0) return // nothing to do yet

        val maxH = (rootH * percent).roundToInt()
        val params = rv.layoutParams
        val rvMeasured = rv.measuredHeight

        params.height = if (rvMeasured > maxH) maxH else ViewGroup.LayoutParams.WRAP_CONTENT
        rv.layoutParams = params
    }

    private fun onDateClicked() {
        parentFragment?.setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
            val selectedValue = bundle.getString("selectedValue")
            selectedValue?.let { it1 ->
                val parsedDate =
                    LocalDate.parse(it1, DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString()
                viewModel.date = parsedDate
                setDate()
            }
        }
        val format = DateTimeFormatter.ofPattern("dd MMM yyyy")

        val navController =
            NavHostFragment.Companion.findNavController(this@AddRecoveryFragment)

        navController.navigate(
            R.id.valueSelectorBottomSheet,
            bundleOf(
                "selectedValue" to if (viewModel.date == null) null else LocalDate.parse(viewModel.date)
                    .format(format),
                "selectionList" to viewModel.getRecoveryDates(),
                "title" to  getString(R.string.text_date)
            )
        )

    }

    private fun setDate(editData: ItemTimelineResponseModel?= null) {
        var date = "Enter"
        val todayDate = LocalDate.now().toString()
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

        if(editData==null) {
            if (viewModel.date.isNullOrEmpty()) {
                viewModel.date = todayDate
            }
        }else{
            viewModel.date = viewModel.editData?.date ?: todayDate
        }
        date = LocalDate.parse(viewModel.date)
            .format(formatter)

        binding.tvDate.text = date
    }

    private fun setEditLayout(key: Int) {
        when(key){
            0 -> {
                binding.lytSelected.isClickable = false
                binding.tvDate.isClickable = false
                binding.lytStartTime.isClickable = false
                binding.lytEndTime.isClickable = false
            }

            else -> {
                binding.lytSelected.isClickable = true
                binding.tvDate.isClickable = false
                binding.lytStartTime.isClickable = true
                binding.lytEndTime.isClickable = true
            }
        }
    }

}