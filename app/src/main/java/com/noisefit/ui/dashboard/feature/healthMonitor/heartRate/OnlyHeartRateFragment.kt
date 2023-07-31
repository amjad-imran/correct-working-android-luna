package com.noisefit.ui.dashboard.feature.healthMonitor.heartRate

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.FragmentOnlyHeartRateBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.HeartRateInterval
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnlyHeartRateFragment :
    BaseFragment<FragmentOnlyHeartRateBinding>(FragmentOnlyHeartRateBinding::inflate) {

    private val viewModel: HeartRateSettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       // val deviceFeatures = viewModel.localDataStore.getDeviceFeatures()

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


        viewModel.sessionManager.sendQueryAction(QueryAction.GetHeartRateInterval)

    }

    override fun initListener() {


        binding.lytAutoHr.switchCompat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener


            updateHeartRateInterval()
        }

    }

    private fun updateHeartRateInterval() {
        binding.progressBar.root.visible()
        val interval = HeartRateInterval(
            status = binding.lytAutoHr.switchCompat.isChecked,
            status2 = false,
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