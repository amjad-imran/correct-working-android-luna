package com.oreo.ui.measurementsTestingScreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.moengage.core.internal.utils.showToast
import com.noisefit.luna.databinding.FragmentMeasurementsBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.models.ManualMeasureType
import com.noisefit_commans.models.ManualMeasureType.*
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.TapMeasureState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MeasurementsFragment :
    BaseFragment<FragmentMeasurementsBinding>(FragmentMeasurementsBinding::inflate)
{

    private val viewModel: MeasurementsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initData()
    }

    override fun initListener() {
        binding.btnHeartRate.setOnClickListener {
            viewModel.stateHeartRateCard.value?.let { performMeasureFunc(HEART_RATE) }
        }

        binding.btnStress.setOnClickListener {
            viewModel.stateStressCard.value?.let { performMeasureFunc(STRESS) }
        }

        binding.btnBodyTemp.setOnClickListener {
            viewModel.stateBodyTemp.value?.let { performMeasureFunc(BODY_TEMPERATURE) }
        }

        binding.btnBloodO2.setOnClickListener {
            viewModel.stateBloodOxygen.value?.let { performMeasureFunc(BLOOD_OXYGEN) }
        }

        binding.btnHrv.setOnClickListener {
            viewModel.stateHrv.value?.let { performMeasureFunc(HRV) }
        }

    }

    private fun performMeasureFunc(measurementType: ManualMeasureType){
        if(viewModel.isMeasuringAny.value == true){
            return
        }

        viewModel.viewModelScope.launch(Dispatchers.IO) {
            context?.let {
                val isWorkerRunning = ApplicationUtils.isOreoSyncDataWorkerRunning(it)
                if (isWorkerRunning) {
                    viewModel.stateStressCard.postValue(viewModel.stateStressCard.value?.apply {
                        withContext(Dispatchers.Main){
                            binding.tvResult.text = "Please Wait\nDevice Is In Sync"
                        }
                    })
                    return@launch
                }
                viewModel.measure(true, measurementType)
            }
        }
    }


    override fun subscribeObservers() {
        val context = binding.root.context
        viewModel.sessionManager.manualMeasurementValue.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                if (it) {
                    viewModel.updateManualValue(HEART_RATE)
                }
            }
        }

        viewModel.stateHeartRateCard.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.viewModelScope.launch {
                    when(it.first){
                        TapMeasureState.NO_DEVICE -> {
                            showToast(context, "Please Connect the Ring")
                        }
                        TapMeasureState.LAST_MEASURED -> {
                            binding.tvResult.text = "HeartRate Val: ${it.second}"
                            viewModel.isMeasuringAny.postValue(false)
                        }
                        TapMeasureState.MEASURING -> {
                            viewModel.isMeasuringAny.postValue(true)
                            binding.tvResult.text = "Measuring HeartRate..."
                        }
                        TapMeasureState.DEFAULT -> {

                        }
                        TapMeasureState.ERROR -> {
                            binding.tvResult.text = "Error - HeartRate"
                            viewModel.isMeasuringAny.postValue(false)
                        }
                        TapMeasureState.HIDE -> {

                        }
                    }

                }
            }
        }

        viewModel.sessionManager.manualMeasurementValueStress.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                if (it) {
                    viewModel.updateManualValue(STRESS)
                }
            }
        }

        viewModel.stateStressCard.observe(viewLifecycleOwner) {
            if (it != null) {
                when(it.first){
                    TapMeasureState.NO_DEVICE -> {
                        showToast(context, "Please Connect the Ring")
                    }
                    TapMeasureState.LAST_MEASURED -> {
                        binding.tvResult.text = "Stress Val: ${it.second}"
                        viewModel.isMeasuringAny.postValue(false)
                    }
                    TapMeasureState.MEASURING -> {
                        viewModel.isMeasuringAny.postValue(true)
                        binding.tvResult.text = "Measuring Stress..."
                    }
                    TapMeasureState.DEFAULT -> {

                    }
                    TapMeasureState.ERROR -> {
                        binding.tvResult.text = "Error - Stress"
                        viewModel.isMeasuringAny.postValue(false)
                    }
                    TapMeasureState.HIDE -> {

                    }
                }

            }
        }

        viewModel.sessionManager.manualMeasurementBodyTemp.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                if (it) {
                    viewModel.updateManualValue(BODY_TEMPERATURE)
                }
            }
        }

        viewModel.stateBodyTemp.observe(viewLifecycleOwner) {
            if (it != null) {
                when(it.first){
                    TapMeasureState.NO_DEVICE -> {
                        showToast(context, "Please Connect the Ring")
                    }
                    TapMeasureState.LAST_MEASURED -> {
                        it.second?.let {
                            binding.tvResult.text = "BodyTemp Val: ${it/100f}"
                        }
                        viewModel.isMeasuringAny.postValue(false)
                    }
                    TapMeasureState.MEASURING -> {
                        viewModel.isMeasuringAny.postValue(true)
                        binding.tvResult.text = "Measuring BodyTemp..."
                    }
                    TapMeasureState.DEFAULT -> {

                    }
                    TapMeasureState.ERROR -> {
                        binding.tvResult.text = "Error - BodyTemp"
                        viewModel.isMeasuringAny.postValue(false)
                    }
                    TapMeasureState.HIDE -> {

                    }
                }

            }
        }

        viewModel.sessionManager.manualMeasurementBloodOxygen.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                if (it) {
                    viewModel.updateManualValue(BLOOD_OXYGEN)
                }
            }
        }

        viewModel.stateBloodOxygen.observe(viewLifecycleOwner) {
            if (it != null) {
                when(it.first){
                    TapMeasureState.NO_DEVICE -> {
                        showToast(context, "Please Connect the Ring")
                    }
                    TapMeasureState.LAST_MEASURED -> {
                        binding.tvResult.text = "Blood Oxygen Val: ${it.second}"
                        viewModel.isMeasuringAny.postValue(false)
                    }
                    TapMeasureState.MEASURING -> {
                        viewModel.isMeasuringAny.postValue(true)
                        binding.tvResult.text = "Measuring Blood Oxygen..."
                    }
                    TapMeasureState.DEFAULT -> {

                    }
                    TapMeasureState.ERROR -> {
                        binding.tvResult.text = "Error - Blood Oxygen"
                        viewModel.isMeasuringAny.postValue(false)
                    }
                    TapMeasureState.HIDE -> {

                    }
                }

            }
        }

        viewModel.sessionManager.manualMeasurementHrv.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                if (it) {
                    viewModel.updateManualValue(HRV)
                }
            }
        }

        viewModel.stateHrv.observe(viewLifecycleOwner) {
            if (it != null) {
                when(it.first){
                    TapMeasureState.NO_DEVICE -> {
                        showToast(context, "Please Connect the Ring")
                    }
                    TapMeasureState.LAST_MEASURED -> {
                        binding.tvResult.text = "HRV Val: ${it.second}"
                        viewModel.isMeasuringAny.postValue(false)
                    }
                    TapMeasureState.MEASURING -> {
                        viewModel.isMeasuringAny.postValue(true)
                        binding.tvResult.text = "Measuring HRV..."
                    }
                    TapMeasureState.DEFAULT -> {

                    }
                    TapMeasureState.ERROR -> {
                        binding.tvResult.text = "Error - HRV"
                        viewModel.isMeasuringAny.postValue(false)
                    }
                    TapMeasureState.HIDE -> {

                    }
                }

            }
        }

        viewModel.isMeasuringAny.observe(this){
            isButtonsEnabled(it)
        }

    }

    private fun isButtonsEnabled(isMeasuring: Boolean){
        if(isMeasuring){
            binding.btnHeartRate.isEnabled = false
            binding.btnStress.isEnabled = false
            binding.btnBodyTemp.isEnabled = false
            binding.btnBloodO2.isEnabled = false
            binding.btnHrv.isEnabled = false
        }else{
            binding.btnHeartRate.isEnabled = true
            binding.btnStress.isEnabled = true
            binding.btnBodyTemp.isEnabled = true
            binding.btnBloodO2.isEnabled = true
            binding.btnHrv.isEnabled = true
        }
    }

}