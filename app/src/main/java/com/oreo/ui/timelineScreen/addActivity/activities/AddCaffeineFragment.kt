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
import com.noisefit.luna.databinding.FragmentAddCaffeineBinding
import com.noisefit.luna.databinding.FragmentAddLightExposureBinding
import com.noisefit.luna.databinding.FragmentAddMealActivityTimelineBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.custom.CustomSlider
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import com.oreo.ui.workout.add.OAddWorkoutFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@AndroidEntryPoint
class AddCaffeineFragment :
    BaseFragment<FragmentAddCaffeineBinding>(FragmentAddCaffeineBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
    private val viewModel: AddCaffeineViewModel by viewModels()

    private val mainViewModel: OreoMainViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.editData = arguments?.getParcelable("editData")
        viewModel.initData()
        setSlider()
        setEditUi()
    }

    private fun setEditUi() {
        if(viewModel.editData != null){
            viewModel.editData?.value?.toFloat()?.let {
                binding.lytCard.lytSlider.setValue(it)
            }
            when(viewModel.editData!!.canBeEditedOrDeleted){
                0 -> {
                    // TODO: Block Slider touch
                    binding.lytCard.lytSlider.disable()
                    binding.lytCard.lytTimePicker.isClickable = false
                    binding.btnSave.gone()
                }

                else -> {
                    binding.lytCard.lytSlider.isClickable = true
                    binding.lytCard.lytTimePicker.isClickable = true
                    binding.btnSave.visible()
                    binding.btnSave.disable()
                }
            }
        }
    }

    private fun setSlider() {
        val customSlider = binding.lytCard.lytSlider

        customSlider.setRange(min = 25f, max = 400f, step = 25f)
        customSlider.setValue(75f)

        customSlider.setOnValueChangeListener(object : CustomSlider.OnValueChangeListener {
            override fun onValueChanged(value: Int) {
                viewModel.caffeineValue.value = value
                if(viewModel.editDataCaffeineVal!=null && viewModel.editDataCaffeineVal!=value){
                    binding.btnSave.enable()
                }
            }
        })
    }

    /*private fun setSlider() {
        val customSlider = binding.lytCard.lytSlider

        customSlider.setRange(min = 25f, max = 400f, step = 25f)
        customSlider.setValue(viewModel.editDataCaffeineVal?.toFloat() ?: 75f)

        if(viewModel.editData == null || viewModel.editData?.canBeEditedOrDeleted != 0){
            customSlider.setOnValueChangeListener(object : CustomSlider.OnValueChangeListener {
                override fun onValueChanged(value: Int) {
                    viewModel.caffeineValue.value = value
                    if (viewModel.editDataCaffeineVal != null && viewModel.editDataCaffeineVal != value) {
                        binding.btnSave.enable()
                    }
                }
            })
        }
    }*/


    override fun initListener() {
        val navController =
            NavHostFragment.Companion.findNavController(this@AddCaffeineFragment)

        binding.lytSelected.setOnClickListener {
            sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ACTIVITIES_LISTING)
        }
        binding.btnSave.setOnClickListener {
            if(viewModel.caffeineTime.value != null && viewModel.caffeineValue.value != null){
                sharedViewModel.sourceKey?.let { sourceKey ->
                    sharedViewModel.sessionManager.logMoEngageAppEvent(
                        MoEngageLunaAppEvents.insight_logged,
                        HashMap<String, Any>().apply {
                            this["source"] = sourceKey
                            if(sourceKey.equals("circadian")){
                                this["target"] = "caffeine"
                            }else {
                                this["log_category"] = "caffeine"
                                this["caffeine_quantity"] = "${viewModel.caffeineValue.value}"
                            }
                        }
                    )
                }
                viewModel.logCaffeineValue(
                    viewModel.caffeineTime.value!!,
                    viewModel.caffeineValue.value!!
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

                viewModel.caffeineTime.postValue(time)

                if(viewModel.editData!=null){
                    val editDataTime = LocalTime.parse(
                        viewModel.editData?.startTime, DateTimeFormatter.ofPattern("HH:mm:ss")
                    )
                    if(
                        editDataTime.hour != time.hour ||
                        editDataTime.minute != time.minute
                    ){
                        binding.btnSave.enable()
                    }
                }
            }


            navController.navigate(
                R.id.timeBottomSheet,
                bundleOf(
                    "hour" to viewModel.caffeineTime.value!!.hour,
                    "minute" to viewModel.caffeineTime.value!!.minute,
                    "hourOther" to 0,
                    "minuteOther" to 0,
                    "isStart" to 1,
                    "unitPosition" to 1,
                    "title" to getString(R.string.text_time)
                )
            )
        }

       /* binding.lytCard.lytDuration.setOnClickListener {
            parentFragment?.setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val selectedValue = bundle.getString("selectedValue")
                selectedValue?.let { it1 ->
                    val minString = it1.split(" ").firstOrNull()
                    viewModel.lightDuration.postValue(minString?.toLongOrNull()?:viewModel.defaultMinutes)
                }

            }
            navController?.navigate(
                R.id.valueSelectorBottomSheet,
                bundleOf(
                    "selectedValue" to "${viewModel.lightDuration.value} min",
                    "selectionList" to AppStaticData.getLightExposureDurationValues(),
                   "title" to getString(R.string.text_duration)
                )
            )
        }*/

        binding.lytSelected.setOnClickListener {
            sharedViewModel.showDropdownDialog(binding.lytSelected, AddActivityItemsEnum.CAFFEINE)
        }
    }

    override fun subscribeObservers() {
        viewModel.caffeineTime.observe(this) {
            binding.lytCard.tvTime.text =
                it.format(DateTimeFormatter.ofPattern("h:mm a")).uppercase()
        }
        viewModel.caffeineValue.observe(this) {
            binding.lytCard.tvCaffeineValue.text = "$it mg"
            binding.lytCard.tvMeasurement.text = "· ${viewModel.mgToCups(it)}"
        }
        viewModel.onAddSuccess.observe(this) {
            it.getContent()?.let {
                mainViewModel.sessionManager.reloadOnResume = true
                //mainViewModel.reloadTodaysData()
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
                    viewModel.deleteCaffeineItem(){
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
                uiController.displayProgressBar(true,"")
            } else {
                uiController.displayProgressBar(false,"")
            }
        }
    }

}