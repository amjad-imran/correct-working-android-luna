package com.noisefit.ui.dashboard.feature.healthMonitor.heartRate

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.databinding.FragmentAlertHeartRateBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.HeartRateAlert
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlertHeartRateFragment :
    BaseFragment<FragmentAlertHeartRateBinding>(FragmentAlertHeartRateBinding::inflate) {

    private val viewModel: HeartRateSettingsViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.lytToolbar.apply {
            tvTitle.text = getString(R.string.heart_rate)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        viewModel.setLoading(true)
        binding.lytAutoHr.root.visible()
        binding.lytAutoHr.apply {
            tvTitle.text = getString(R.string.text_auto_hr)
            tvDesc.text = getString(R.string.text_auto_hr_monitor_about)
        }

        viewModel.sessionManager.sendQueryAction(QueryAction.GetHeartRateAlert)

    }


    private fun updateAlertValue() {
        binding.lytHeartRateAlertSettings.tvAlertValueData.text =
            viewModel.getHrValue()
    }

    override fun initListener() {

        binding.lytHeartRateAlertSettings.tvAlertValueData.setOnClickListener {

            if (!binding.lytAutoHr.switchCompat.isChecked) {
                return@setOnClickListener
            }
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                LOGS.i("$position | $selectedValue")
                viewModel.alertValue = try {
                    selectedValue!!.split(" ")[0].toInt()
                } catch (exp: Exception) {
                    exp.printStackTrace()
                    1
                }
                updateAlertValue()
                sendDataToWatch()
            }
            navigate(
                AlertHeartRateFragmentDirections.actionHeartRateSettingsFragmentToValueSelectorBottomSheet(
                    viewModel.getHrValue(),
                    viewModel.hrValueList!!,
                    getString(R.string.text_alert_value)
                )
            )

        }

        binding.lytAutoHr.switchCompat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            sendDataToWatch()
            if (isChecked){
                viewModel.sessionManager.logInsiderAppEvent(
                    InsiderAppEvents.HEART_RATE_AUTO_HR_CLICK,
                    HashMap<String, Any>().apply {
                    this["is_enabled"]=true
                })
            }
            else{
                viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.HEART_RATE_AUTO_HR_CLICK,HashMap<String, Any>().apply {
                    this["is_enabled"]=false
                })
            }
        }

    }

    private fun sendDataToWatch() {
        updateHeartRateFrequency()
        updateHeartRateInterval()
    }

    private fun updateHeartRateFrequency() {
        if (viewModel.alertValue == 0) {
            viewModel.alertValue = 140
        }
        val alert = HeartRateAlert(
            status = binding.lytAutoHr.switchCompat.isChecked,
            min_hr = 40,
            max_hr = viewModel.alertValue
        )
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetHeartRateAlert(
                alert
            )
        )
    }

    private fun updateHeartRateInterval() {
        val interval = HeartRateInterval(
            status = binding.lytAutoHr.switchCompat.isChecked,
            status2 = binding.lytAutoHr.switchCompat.isChecked,
            startTime = "00:00",
            endTime = "23:59",
            interval = 5
        )
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetHeartRateInterval(
                interval
            )
        )

    }

    override fun subscribeObservers() {
        viewModel.showOnlyAlertLayout.observe(this) {
            if (it == true) {
                binding.lytHeartRateAlertSettings.root.visible()
            } else {
                binding.lytHeartRateAlertSettings.root.gone()
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
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
                    else -> {}
                }
            }
        }

        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {

                is QueryCallback.HeartRateIntervalObtained -> {
                    binding.progressBar.root.gone()
                    binding.lytAutoHr.switchCompat.isChecked = it.interval.status
                    updateAlertValue()
                }
                is QueryCallback.HeartRateAlertDataObtained -> {
                    binding.progressBar.root.gone()
                    binding.lytAutoHr.switchCompat.isChecked = it.heartRateAlert.status
                    viewModel.alertValue = it.heartRateAlert.max_hr
                    updateAlertValue()
                }

                else -> {}
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