package com.noisefit.ui.dashboard.feature.healthMonitor.heartRate

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.databinding.FragmentHeartRateSettingsBinding
import com.noisefit.ui.common.*
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.HeartRateAlert
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HeartRateSettingsFragment :
    BaseFragment<FragmentHeartRateSettingsBinding>(FragmentHeartRateSettingsBinding::inflate) {

    private val viewModel: HeartRateSettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val deviceFeatures = viewModel.localDataStore.getDeviceFeatures()
        binding.lytToolbar.apply {
            tvTitle.text = getString(R.string.heart_rate)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }




        if (deviceFeatures?.autoHeartMeasure == 1) {

            binding.lytAutoHr.root.visible()
            binding.lytAutoHr.apply {
                tvTitle.text = getString(R.string.text_auto_hr)
                tvDesc.text = getString(R.string.text_auto_hr_monitor_about)
            }


            if (!viewModel.fetchData) {
                viewModel.setLoading(true)
                viewModel.sessionManager.sendQueryAction(QueryAction.GetHeartRateInterval)
            } else {
                viewModel.setLoading(false)
                updateAllValues()
            }
            if (!viewModel.fetchData2) {
                viewModel.sessionManager.sendQueryAction(QueryAction.GetHeartRateAlert)
            } else {
                lowHr()
                highHr()
            }

        }


    }

    private fun setSwitchState(check: Boolean) {
        binding.lytAutoHr.switchCompat.isChecked = check
        editButtonState(check)
    }

    private fun updateAllValues() {

        setEndTime()
        setStartTime()
        setSwitchState(viewModel.hrSwitchEnabled)
        updateFreq()
    }

    private fun setStartTime() {
        binding.lytHeartRateSettings.tvStartTimeData.text =
            DateFormats.formatTimeWithAmPm(viewModel.startHour, viewModel.startMinute)

    }

    private fun setEndTime() {
        binding.lytHeartRateSettings.tvEndTimeData.text =
            DateFormats.formatTimeWithAmPm(viewModel.endHour, viewModel.endMinute)
    }

    private fun updateFreq() {
        binding.lytHeartRateSettings.tvFreqData.text =
            viewModel.getFreqValue()
    }

    private fun lowHr() {
        binding.lytHeartRateSettings.tvLowHrData.text =
            viewModel.getLowHrValue()
    }

    private fun highHr() {
        binding.lytHeartRateSettings.tvHighHrData.text =
            viewModel.getHighHrValue()
    }


    override fun initListener() {

        binding.lytHeartRateSettings.tvEdit.setOnClickListener {
            setFragmentResultListener(EDIT_HR_REQUEST_KEY) { _, bundle ->
                val heartRateInterval = bundle.getParcelable("hrInterval") as? HeartRateInterval
                val heartRateAlert = bundle.getParcelable("hrAlert") as? HeartRateAlert
                if (heartRateInterval != null && heartRateAlert != null) {
                    viewModel.updateHRInterval(heartRateInterval)
                    viewModel.updateHRAlert(heartRateAlert)
                    updateAllValues()
                    lowHr()
                    highHr()
                }


            }
            navigate(
                HeartRateSettingsFragmentDirections.actionHeartRateSettingsFragmentToEditHeartRateSettingsFragment(
                    viewModel.getHeartRateInterval(),
                    viewModel.getHeartRateAlert()
                )
            )
        }




        binding.lytAutoHr.switchCompat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            editButtonState(isChecked)
            updateHeartRateInterval()
        }

    }

    private fun editButtonState(isChecked: Boolean) {
        if (isChecked) {
            binding.lytHeartRateSettings.tvEdit.enable()
        } else {
            binding.lytHeartRateSettings.tvEdit.disable()
        }
    }

    private fun updateHeartRateInterval() {
        val hideRealHrLayout = viewModel.showRealTimeHrLayout.value == false
        var autoStatus = binding.lytAutoHr.switchCompat.isChecked
        var realHrStatus = binding.lytAutoHr.switchCompat.isChecked
        if (hideRealHrLayout) {
            autoStatus = binding.lytAutoHr.switchCompat.isChecked
            realHrStatus = binding.lytAutoHr.switchCompat.isChecked
        }

        viewModel.setLoading(true)
        val interval = HeartRateInterval(
            status = autoStatus,
            status2 = realHrStatus,
            startTime = "${viewModel.startHour}:${viewModel.startMinute}",
            endTime = "${viewModel.endHour}:${viewModel.endMinute}",
            interval = viewModel.frequency,
        )
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetHeartRateInterval(
                interval
            )
        )

        if (hideRealHrLayout) {
            viewModel.sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.SetHeartRateAlert(
                    HeartRateAlert(
                        status = autoStatus,
                        min_hr = viewModel.lowHrValue,
                        max_hr = viewModel.highHrValue
                    )
                )
            )
        }

    }

    override fun subscribeObservers() {

        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.HeartRateIntervalObtained -> {
//                    LOGS.d("HEART_RATE_ ${it.interval.status} ")
                    binding.progressBar.root.gone()
                    viewModel.fetchData = true
                    viewModel.hrSwitchEnabled = it.interval.status
                    viewModel.updateHRInterval(it.interval)
                    updateAllValues()
                }
                is QueryCallback.HeartRateAlertDataObtained -> {
                    viewModel.fetchData2 = true
//                    LOGS.d("HEART_RATE_1 ${it.heartRateAlert}")
                    binding.progressBar.root.gone()
                    viewModel.hrSwitchEnabled = it.heartRateAlert.status
                    viewModel.updateHRAlert(it.heartRateAlert)
                    lowHr()
                    highHr()

                }
                else -> {}
            }
        }



        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }


        viewModel.showHrSettingLayout.observe(this) {
            if (it == true) {
                binding.lytHeartRateSettings.root.visible()
            } else {
                binding.lytHeartRateSettings.root.gone()
            }
        }


        viewModel.showStartTimeLayout.observe(this) {
            if (it == true) {
                binding.lytHeartRateSettings.tvStartTimeData.visible()
                binding.lytHeartRateSettings.tvStartTimeText.visible()
            } else {
                binding.lytHeartRateSettings.tvStartTimeData.gone()
                binding.lytHeartRateSettings.tvStartTimeText.gone()
            }
        }

        viewModel.showEndTimeLayout.observe(this) {
            if (it == true) {
                binding.lytHeartRateSettings.tvEndTimeData.visible()
                binding.lytHeartRateSettings.tvEndTimeText.visible()
            } else {
                binding.lytHeartRateSettings.tvEndTimeData.gone()
                binding.lytHeartRateSettings.tvEndTimeText.gone()
            }
        }



        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { callBack ->
                when (callBack) {
                    is UpdateDeviceDataCallback.HeartRateMeasureIntervalSet -> {
                        binding.progressBar.root.gone()
                        if (callBack.success) {
                            context.showShortToast(getString(R.string.text_auto_hr_success))
                            showBatteryAlert()
                        } else {
                            context.showShortToast(getString(R.string.text_auto_hr_failed))
                        }
                    }
                    is UpdateDeviceDataCallback.HeartRateAlertUpdated -> {
                        binding.progressBar.root.gone()
                        if (callBack.success) {
                            context.showShortToast(getString(R.string.text_auto_hr_success))
                            showBatteryAlert()
                        } else {
                            context.showShortToast(getString(R.string.text_auto_hr_failed))
                        }
                    }

                    else -> {}
                }
            }
        }

    }

    fun showBatteryAlert(){
        if(!binding.lytAutoHr.switchCompat.isChecked) return

        val alertMessage = getString(R.string.text_auto_battery,"heart rate")
        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.InfoAlertDialog(
                    getString(
                        R.string.text_alert
                    ), alertMessage, getString(R.string.text_close)
                )
            )
        )
    }

}