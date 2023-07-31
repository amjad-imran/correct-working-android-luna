package com.noisefit.ui.dashboard.feature.healthMonitor.heartRate

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.R
import com.noisefit.databinding.FragmentAutoSpo2Binding
import com.noisefit.databinding.FragmentHeartRateWithIntervalBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit.ui.dashboard.feature.spo2.AutoSpo2FragmentDirections
import com.noisefit.ui.dashboard.feature.spo2.AutoSpo2ViewModel
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.HeartRateInterval
import com.noisefit_commans.models.Spo2Data
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HeartRateWithIntervalFragment :
    BaseFragment<FragmentHeartRateWithIntervalBinding>(FragmentHeartRateWithIntervalBinding::inflate) {

    private val viewModel: AutoSpo2ViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // val deviceFeatures = viewModel.localDataStore.getDeviceFeatures()

        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.heart_rate)
            tvDesc.text = getString(R.string.text_auto_hr_monitor_about)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        binding.lytFeatureTile.apply {
            tvTitle.text = getString(R.string.text_auto_hr)
            imvIcon.loadImage(requireContext(), R.drawable.ic_heart_rate)
        }


        binding.progressBar.root.visible()
        binding.lytFeatureTile.root.visible()
        binding.lytHeartRateAlertSettings.tvAlertValueText.text = getString(R.string.text_interval)

        viewModel.sessionManager.sendQueryAction(QueryAction.GetHeartRateInterval)

    }

    override fun initListener() {


        binding.lytFeatureTile.llSwitch.setOnCheckedChangeListener { buttonView, _ ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener

            updateHeartRateInterval()
        }


        binding.lytHeartRateAlertSettings.tvAlertValueData.setOnClickListener {

            if (!binding.lytFeatureTile.llSwitch.isChecked) {
                return@setOnClickListener
            }
            setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
                val position = bundle.getInt("selectedPosition")
                val selectedValue = bundle.getString("selectedValue")
                LOGS.i("$position | $selectedValue")
                viewModel.interval = try {
                    selectedValue!!.split(" ")[0].toInt()
                } catch (exp: Exception) {
                    exp.printStackTrace()
                    1
                }
                updateAlertValue()
                updateHeartRateInterval()
            }
            navigate(
                HeartRateWithIntervalFragmentDirections.actionHeartRateWithIntervalFragmentToValueSelectorBottomSheet(
                    viewModel.getHrValue(),
                    viewModel.intervalValueList!!,
                    getString(R.string.text_interval)
                )
            )

        }


    }

    private fun updateAlertValue() {
        binding.lytHeartRateAlertSettings.tvAlertValueData.text =
            viewModel.getHrValue()
    }

    private fun updateHeartRateInterval() {
        binding.progressBar.root.visible()
        val interval = HeartRateInterval(
            status = binding.lytFeatureTile.llSwitch.isChecked,
            status2 = false,
            startTime = "00:00",
            endTime = "23:59",
            interval = viewModel.interval
        )
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetHeartRateInterval(
                interval
            )
        )
    }

    override fun subscribeObservers() {
        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { callBack ->
                when (callBack) {

                    is UpdateDeviceDataCallback.HeartRateMeasureIntervalSet -> {
                        binding.progressBar.root.gone()
                        if (callBack.success) {
                            context.showShortToast(getString(R.string.text_auto_hr_success))
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
                    binding.progressBar.root.gone()
                    viewModel.interval = it.interval.interval
                    binding.lytFeatureTile.llSwitch.isChecked = it.interval.status
                    updateAlertValue()

                }
                else -> {}
            }
        }
    }
}