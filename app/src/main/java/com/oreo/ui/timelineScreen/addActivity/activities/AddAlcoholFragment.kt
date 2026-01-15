package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.moengage.core.internal.utils.showToast
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddAlcoholBinding
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
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.lifeos.LifeOsChatFragment
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.getValue

@AndroidEntryPoint
class AddAlcoholFragment : BaseFragment<FragmentAddAlcoholBinding>(FragmentAddAlcoholBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
    private val viewModel: AddAlcoholViewmodel by viewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getAlcoholIdFromServer()
        viewModel.editData = arguments?.getParcelable("editData")

        binding.lytDateTime.textView197.gone()
        if(viewModel.editData == null) {
            viewModel.selectedDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            setUi()
        }else{
            setEditLayout(viewModel.editData!!)
        }
    }

    private fun setUi() {
        binding.lytDateTime.apply {
            lytDate.tvTime.text = getString(R.string.text_date)
            lytTime.tvTime.text = getString(R.string.text_time)

            // set Default Date
            lytDate.tvTimeValue.text =
                LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    .toString()

            // set Default(Current) Time
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val curFormattedTime = DateFormats.formatTimeWithAmPm(
                hour,
                minute
            )
            viewModel.alcoholTime = LocalTime.of(hour, minute)
            lytTime.tvTimeValue.text = curFormattedTime
        }
    }

    override fun initListener() {
        binding.lytDateTime.lytDate.tvTimeValue.setOnClickListener {
            onDateClicked()
        }

        binding.lytDateTime.lytTime.tvTimeValue.setOnClickListener {
            onTimeClicked()
        }

        binding.btnSave.setOnClickListener{
            if(viewModel.alcoholId == null){
                showToast(requireContext(), "Something went wrong!\nPlease try later.")
                return@setOnClickListener
            }

            if(getString(R.string.text_save).equals(binding.btnSave.text)) {
                viewModel.logAlcohol() {
                    if(viewModel.editData?.id != null) {
                        sharedViewModel.sessionManager.logMoEngageAppEvent(
                            MoEngageLunaAppEvents.insight_log_edited,
                        )
                    }else{
                        sharedViewModel.sessionManager.logMoEngageAppEvent(
                            MoEngageLunaAppEvents.insight_logged,
                            HashMap<String, Any>().apply {
                                this["source"] = "timeline"
                                this["log_category"] = "alcohol"
                            }
                        )
                    }
                }
            }else{
                if (sharedViewModel.ringDataStore.getRingDevice() == null) {
                    context.showShortToast(getString(R.string.text_luna_ai_message))
                }else {
                    val (frag, bundle) = LifeOsChatFragment.getStartData(
                        threadId = null,
                        userMessage = null,
                        title = null,
                        aiTopic = AITopics.GENERAL
                    )
                    navigate(
                        frag, bundle
                    )
                }
            }
        }
    }

    override fun subscribeObservers() {
        viewModel.onAddSuccess.observe(this){
            it.getContent()?.let {
                mainViewModel.sessionManager.reloadOnResume = true
                sharedViewModel.navigateUp()
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
                    viewModel.deleteAlcoholItem(){
                        sharedViewModel.sessionManager.logMoEngageAppEvent(
                            MoEngageLunaAppEvents.insight_log_deleted,
                        )
                    }
                }
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
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }

    private fun onTimeClicked() {
        parentFragment?.setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
            val hourOfDay = bundle.getInt("hour")
            val minute = bundle.getInt("minute")

            val time = LocalTime.of(hourOfDay, minute)
            if (LocalDate.now().toString().equals(viewModel.selectedDate) && time > LocalTime.now()) {
                context.showShortToast("Time cannot be in future") //TODO message change
                return@setFragmentResultListener
            }

            val curFormattedTime = DateFormats.formatTimeWithAmPm(
                hourOfDay,
                minute
            )
            viewModel.alcoholTime = time
            binding.lytDateTime.lytTime.tvTimeValue.text = curFormattedTime
            if(viewModel.editData?.startTime != null){
                val editDataTime = LocalTime.parse(viewModel.editData?.startTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
                if(editDataTime.hour != viewModel.alcoholTime.hour || editDataTime.minute != viewModel.alcoholTime.minute){
                    binding.btnSave.text = getString(R.string.text_save)
                }
            }
        }

        val navController =
            NavHostFragment.Companion.findNavController(this@AddAlcoholFragment)

        navController.navigate(
            R.id.timeBottomSheet,
            bundleOf(
                "hour" to viewModel.alcoholTime.hour,
                "minute" to viewModel.alcoholTime.minute,
                "hourOther" to 0,
                "minuteOther" to 0,
                "isStart" to 1,
                "unitPosition" to 1,
                "title" to getString(R.string.text_time)
            )
        )
    }

    private fun onDateClicked() {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        parentFragment?.setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
            val selectedValue = bundle.getString("selectedValue")
            selectedValue?.let { it1 ->

                val parsedDate =
                    LocalDate.parse(it1, formatter)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString()

                val curTime = LocalTime.now()

                if(LocalDate.now().toString().equals(parsedDate) && viewModel.alcoholTime > curTime){
                    viewModel.alcoholTime = curTime
                    binding.lytDateTime.lytTime.tvTimeValue.text = DateFormats.formatTimeWithAmPm(
                        curTime.hour,
                        curTime.minute
                    )
                }

                viewModel.selectedDate = parsedDate

                binding.lytDateTime.lytDate.tvTimeValue.text = it1

                if(viewModel.editData?.date != null && !parsedDate.equals(viewModel.editData?.date)){
                    binding.btnSave.text = getString(R.string.text_save)
                }
            }
        }

        val navController =
            NavHostFragment.Companion.findNavController(this@AddAlcoholFragment)

        navController.navigate(
            R.id.valueSelectorBottomSheet,
            bundleOf(
                "selectedValue" to if (viewModel.selectedDate == null) null else LocalDate.parse(viewModel.selectedDate)
                    .format(formatter),
                "selectionList" to viewModel.getAlcoholDates(),
                "title" to  getString(R.string.text_date)
            )
        )

    }

    private fun setEditLayout(editData: ItemTimelineResponseModel) {
        binding.lytDateTime.lytDate.tvTime.text = getString(R.string.text_date)
        binding.lytDateTime.lytTime.tvTime.text = getString(R.string.text_time)

        // Set Date
        viewModel.selectedDate = editData.date ?: LocalDate.now().toString()
        val parsedDate =
            LocalDate.parse(viewModel.selectedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy")).toString()
        binding.lytDateTime.lytDate.tvTimeValue.text = parsedDate

        // Set Time
        viewModel.alcoholTime = LocalTime.parse(editData.startTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
        binding.lytDateTime.lytTime.tvTimeValue.text = DateFormats.formatTimeWithAmPm(
            viewModel.alcoholTime.hour,
            viewModel.alcoholTime.minute
        )

        // Set Other UI States
        when(editData.canBeEditedOrDeleted){
            0 -> {
                binding.btnSave.gone()
                binding.lytDateTime.lytDate.tvTimeValue.isClickable = false
                binding.lytDateTime.lytTime.tvTimeValue.isClickable = false
            }

            else -> {
                binding.btnSave.apply {
                    text = getString(R.string.text_learn_more_with_luna_ai)
                    enable()
                    visible()
                }
                binding.lytDateTime.lytDate.tvTimeValue.isClickable = true
                binding.lytDateTime.lytTime.tvTimeValue.isClickable = true
            }
        }

//        binding.lytDateTime.lytDate.tvTimeValue.isClickable = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}