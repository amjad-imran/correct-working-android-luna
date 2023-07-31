package com.noisefit.ui.dashboard.feature.spo2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAutoSpo2Binding
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.Spo2Data
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AutoSpo2Fragment :
    BaseFragment<FragmentAutoSpo2Binding>(FragmentAutoSpo2Binding::inflate) {

    private val viewModel: AutoSpo2ViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // val deviceFeatures = viewModel.localDataStore.getDeviceFeatures()

        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_spo2)
            tvDesc.text = getString(R.string.text_auto_spo2_monitor_about)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        binding.lytFeatureTile.apply {
            tvTitle.text = getString(R.string.text_auto_spo2_monitor)
            imvIcon.loadImage(requireContext(), R.drawable.ic_act_spo2)
        }


        binding.progressBar.root.visible()
        binding.lytFeatureTile.root.visible()
        binding.lytHeartRateAlertSettings.tvAlertValueText.text = getString(R.string.text_interval)

        viewModel.sessionManager.sendQueryAction(QueryAction.GetSpo2Settings)

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
                AutoSpo2FragmentDirections.actionAutoSpo2FragmentToValueSelectorBottomSheet(
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
        val interval = Spo2Data(
            status = binding.lytFeatureTile.llSwitch.isChecked,
            interval = viewModel.interval
        )
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetSpo2Settings(
                interval
            )
        )

    }

    override fun subscribeObservers() {


        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { callBack ->
                when (callBack) {

                    is UpdateDeviceDataCallback.Spo2SettingsUpdated -> {
                        binding.progressBar.root.gone()
                        if (callBack.success) {
                            context.showShortToast(getString(R.string.text_auto_spo2_success))
                        } else {
                            context.showShortToast(getString(R.string.text_auto_spo2_failed))
                        }
                    }
                    else -> {}
                }
            }
        }

        viewModel.sessionManager.deviceQueryCallback.observe(this) {
            when (it) {

                is QueryCallback.Spo2SettingsObtained -> {
                    binding.progressBar.root.gone()
                    viewModel.interval = it.spo2Data.interval
                    binding.lytFeatureTile.llSwitch.isChecked = it.spo2Data.status
                    updateAlertValue()
                }
                else -> {}
            }
        }
    }
}