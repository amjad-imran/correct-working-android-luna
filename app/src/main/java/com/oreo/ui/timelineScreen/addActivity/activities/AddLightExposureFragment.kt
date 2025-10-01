package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.moengage.core.internal.utils.showToast
import com.noisefit.data.local.AppStaticData
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddLightExposureBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.circadianAlignment.CircadianAlignmentViewModel
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AddLightExposureFragment :
    BaseFragment<FragmentAddLightExposureBinding>(FragmentAddLightExposureBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
    private val viewModel: AddLightExposureViewModel by viewModels()

    private val mainViewModel: OreoMainViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.editData = arguments?.getParcelable("editData")
        viewModel.initData()
        setUi()
    }

    private fun setUi() {
        if(viewModel.editData!=null){
            when(viewModel.editData!!.canBeEditedOrDeleted){
                0 -> {
                    binding.lytCard.lytTimePicker.isClickable = false
                    binding.lytCard.lytDuration.isClickable = false
                    binding.btnSave.gone()
                }

                else -> {
                    binding.lytCard.lytTimePicker.isClickable = true
                    binding.lytCard.lytDuration.isClickable = true
                    binding.btnSave.apply {
                        disable()
                        visible()
                    }
                }
            }
        }
    }


    override fun initListener() {
        val navController =
            NavHostFragment.Companion.findNavController(this@AddLightExposureFragment)

        binding.lytSelected.setOnClickListener {
            sharedViewModel.showDropdownDialog(binding.lytSelected, AddActivityItemsEnum.LIGHT_EXPOSURE)
        }
        binding.btnSave.setOnClickListener {
            if (viewModel.lightTime.value != null && viewModel.lightDuration.value != null) {
                //
                sharedViewModel.sourceKey?.let { sourceKey ->
                    sharedViewModel.sessionManager.logMoEngageAppEvent(
                        MoEngageLunaAppEvents.insight_logged,
                        HashMap<String, Any>().apply {
                            this["source"] = sourceKey
                            if(sourceKey.equals("circadian")){
                                this["target"] = "light"
                            }else {
                                this["log_category"] = "light"
                            }
                        }
                    )
                }
                //
                viewModel.logLightExposure(
                    viewModel.lightTime.value!!,
                    viewModel.lightDuration.value!!
                )
            }
        }

        binding.lytCard.lytTimePicker.setOnClickListener {
            parentFragment?.setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
                val hourOfDay = bundle.getInt("hour")
                val minute = bundle.getInt("minute")

                val time = LocalTime.of(hourOfDay, minute)
                if (time > LocalTime.now()) {
                    context.showShortToast("Time cannot be in future") //TODO message change
                    return@setFragmentResultListener
                }

                viewModel.lightTime.postValue(time)

                if(viewModel.editData!=null){
                    val editDataTime = LocalTime.parse(
                        viewModel.editData?.startTime, DateTimeFormatter.ofPattern("HH:mm:ss")
                    )
                    if(
                        editDataTime.hour != viewModel.lightTime.value?.hour ||
                        editDataTime.minute != viewModel.lightTime.value?.minute
                    ){
                        binding.btnSave.enable()
                    }
                }

            }


            navController.navigate(
                R.id.timeBottomSheet,
                bundleOf(
                    "hour" to viewModel.lightTime.value!!.hour,
                    "minute" to viewModel.lightTime.value!!.minute,
                    "hourOther" to 0,
                    "minuteOther" to 0,
                    "isStart" to 1,
                    "unitPosition" to 1,
                    "title" to getString(R.string.text_time)
                )
            )
        }

        binding.lytCard.lytDuration.setOnClickListener {
            parentFragment?.setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { it1 ->
                    val minString = it1.split(" ").firstOrNull()
                    viewModel.lightDuration.postValue(
                        minString?.toLongOrNull() ?: viewModel.defaultMinutes
                    )

                    if(
                        viewModel.editDataDuration != null &&
                        viewModel.editDataDuration!=viewModel.lightDuration.value
                    ){
                        binding.btnSave.enable()
                    }
                }

            }
            navController?.navigate(
                R.id.valueSelectorBottomSheet,
                bundleOf(
                    "selectedValue" to "${viewModel.lightDuration.value} mins",
                    "selectionList" to AppStaticData.getLightExposureDurationValues(),
                    "title" to getString(R.string.text_duration)
                )
            )
        }
    }

    override fun subscribeObservers() {
        viewModel.lightTime.observe(this) {
            binding.lytCard.tvTime.text =
                it.format(DateTimeFormatter.ofPattern("h:mm a")).uppercase()
        }
        viewModel.lightDuration.observe(this) {
            binding.lytCard.tvDuration.text =
                "$it mins"//todo change to hour minute if greater than 60 min
        }
        viewModel.onAddSuccess.observe(this) {
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
                    viewModel.deleteLightExposureItem()
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
                uiController.displayProgressBar(true,"")
            } else {
                uiController.displayProgressBar(false,"")
            }
        }
    }

}