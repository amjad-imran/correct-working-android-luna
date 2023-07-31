package com.noisefit.ui.dashboard.feature.rapid

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRapidEyeMomentBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.ui.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RapidEyeMomentFragment :
    BaseFragment<FragmentRapidEyeMomentBinding>(FragmentRapidEyeMomentBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager.sendQueryAction(QueryAction.GetQuickEyeMovementSwitch)
        binding.progressBar.root.visible()
    }

    private fun updateReminder(status: Boolean) {
        binding.progressBar.root.visible()
        sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetQuickEyeMovementSwitch(status))
    }

    private fun setSwitchState(isChecked: Boolean) {
        binding.lytFeatureTile.llSwitch.isChecked = isChecked
    }


    override fun initListener() {
        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_rapid_eye_movment)
            tvTitle.text = getString(R.string.text_rapid_eye_movement)
            tvTitleDisc.text = getString(R.string.text_not_receiving_alerts)
            tvTitleDisc.setTextColor(requireActivity().resources.getColor(com.noisefit_commans.R.color.text_accent_color))
            llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener
                if (isChecked) {
                    updateReminder(true)
                    sessionManager.logInsiderAppEvent(InsiderAppEvents.RAPID_EYE_MOVEMENT_CLICK,HashMap<String, Any>().apply {
                        this["is_enabled"]=true
                    })
                } else {
                    updateReminder(false)
                    sessionManager.logInsiderAppEvent(InsiderAppEvents.RAPID_EYE_MOVEMENT_CLICK,HashMap<String, Any>().apply {
                        this["is_enabled"]=false
                    })
                }


            }
        }
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_rapid_eye_movement)
            tvDesc.gone()
//            tvDesc.text = getString(R.string.text_music_control_description)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }
    }

    override fun subscribeObservers() {
        sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.QuickEyeMovementSwitchObtained -> {
                    setSwitchState(it.switchSetting.status)
                    binding.progressBar.root.gone()
                }
                else -> {}
            }
        }

        sessionManager.updateDeviceCallback.observe(this) {
            val event = it.getContent() ?: return@observe
            when (event) {
                is UpdateDeviceDataCallback.QuickEyeMovementSwitchUpdated -> {
                    binding.progressBar.root.gone()
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_rapid_eye_updated))
                    }
                }
                else -> {}
            }
        }
    }

}