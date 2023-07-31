package com.noisefit.ui.dashboard.feature.healthMonitor.stress

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.FragmentStressSettingsBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.SedentaryData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StressSettingsFragment :
    BaseFragment<FragmentStressSettingsBinding>(FragmentStressSettingsBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytToolbar.apply {
            tvTitle.text = getString(R.string.text_stress)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }
        binding.lytAutoStress.apply {
            tvTitle.text = getString(R.string.text_auto_stress_monitor)
            tvDesc.text = getString(R.string.text_auto_stress_monitor_about)
        }
        binding.progressBar.root.visible()
        sessionManager.sendQueryAction(QueryAction.GetStressSettings)
    }

    override fun initListener() {
        binding.lytAutoStress.switchCompat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            binding.progressBar.root.visible()
            sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.SetStressData(
                    SedentaryData(
                        status = isChecked
                    )
                )
            )
        }
    }

    //
    override fun subscribeObservers() {
        sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.StressParamObtained -> {
                    binding.progressBar.root.gone()
                    binding.lytAutoStress.switchCompat.isChecked = it.sedentaryData.status
                }
                else -> {}
            }
        }
        sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) {
            val event = it.getContent() ?: return@observe
            when (event) {
                is UpdateDeviceDataCallback.StressDataUpdated -> {
                    binding.progressBar.root.gone()
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_auto_stress_updated))
                        showBatteryAlert()
                    }
                }
                else -> {}
            }
        }
    }

    fun showBatteryAlert(){
        if(!binding.lytAutoStress.switchCompat.isChecked) return

        val alertMessage = getString(R.string.text_auto_battery,"stress")
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