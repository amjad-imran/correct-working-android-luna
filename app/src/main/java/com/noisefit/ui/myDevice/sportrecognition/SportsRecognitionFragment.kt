package com.noisefit.ui.myDevice.sportrecognition

import android.os.Bundle
import android.view.View
import com.noisefit.R
import com.noisefit.databinding.FragmentSportsRecognitionBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.SwitchSetting
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SportsRecognitionFragment :
    BaseFragment<FragmentSportsRecognitionBinding>(FragmentSportsRecognitionBinding::inflate) {


    @Inject
    lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.tvTitle.text = getString(R.string.text_sports_recognise)

        sessionManager.sendQueryAction(QueryAction.GetActivityRecogniseSettings)
        //binding.progressBar.root.visible()
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.sRunning.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            updateActivityRecogniseSettings()
        }
        binding.sWalking.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            updateActivityRecogniseSettings()
        }
    }

    fun updateActivityRecogniseSettings() {
        binding.progressBar.root.visible()
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetActivityRecogniseSwitch(
                SwitchSetting(
                    walk_status = binding.sWalking.isChecked,
                    run_status = binding.sRunning.isChecked
                )
            )
        )
    }

    override fun subscribeObservers() {
        sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.ActivityRecognise -> {
                    binding.progressBar.root.gone()
                    binding.sWalking.isChecked = it.switchSetting.walk_status
                    binding.sRunning.isChecked = it.switchSetting.run_status
                }
                else -> {}
            }
        }
        sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) {
            it.getContent()?.let {
                when (it) {
                    is UpdateDeviceDataCallback.ActivitySwitchUpdated -> {
                        binding.progressBar.root.gone()
                        if (it.success) {
                            context.showShortToast("Data updated successfully")
                        }
                    }
                    else -> {}
                }
            }

        }
    }


}