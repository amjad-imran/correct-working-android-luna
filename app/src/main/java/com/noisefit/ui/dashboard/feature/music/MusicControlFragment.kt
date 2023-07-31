package com.noisefit.ui.dashboard.feature.music

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentMusicControlBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.SwitchSetting
import com.noisefit_commans.ui.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MusicControlFragment :
    BaseFragment<FragmentMusicControlBinding>(FragmentMusicControlBinding::inflate) {

    private var notificationAccessGranted = false
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager.sendQueryAction(QueryAction.GetMusicControlSettings)
        binding.progressBar.root.visible()
    }

    private fun updateReminder(status: Boolean) {
        binding.progressBar.root.visible()
        sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetMusicSwitch(SwitchSetting(status = status)))
    }

    private fun setSwitchState(isChecked: Boolean) {
        binding.lytFeatureTile.llSwitch.isChecked = isChecked
    }


    private fun askMusicPermission(){
        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.AreYouSureDialog(
                    getString(R.string.text_permission_required),
                    getString(R.string.text_notification_disabled),
                    false,
                    getString(R.string.text_allow),
                    object : BinaryActionCallback {
                        override fun yes() {
                            ApplicationUtils.requestNotificationAccess(requireActivity())
                        }
                        override fun no() {

                        }

                    }
                )
            )
        )
    }
    override fun initListener() {
        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_music)
            tvTitle.text = getString(R.string.text_music_controls)
            tvTitleDisc.text = getString(R.string.text_not_receiving_alerts)
            tvTitleDisc.setTextColor(requireActivity().resources.getColor(com.noisefit_commans.R.color.text_accent_color))
            llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener

                if(isChecked && !notificationAccessGranted){
                    askMusicPermission()
                    llSwitch.isChecked = false
                    return@setOnCheckedChangeListener
                }
                if (isChecked) {
                    updateReminder(true)
                } else {
                    updateReminder(false)
                }


            }
        }
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_music_controls)
//            tvDesc.gone()
              tvDesc.text = getString(R.string.text_music_control_description)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }
    }

    override fun subscribeObservers() {
        sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.MusicControl -> {
                    setSwitchState(it.switchSetting.status)
                    binding.progressBar.root.gone()
                }
                else -> {}
            }
        }

        sessionManager.updateDeviceCallback.observe(this) {
            val event = it.getContent() ?: return@observe
            when (event) {
                is UpdateDeviceDataCallback.MusicSwitchUpdated -> {
                    binding.progressBar.root.gone()
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_music_alert_updated))
                    }
                }
                else -> {}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        notificationAccessGranted = ApplicationUtils.isNotificationServiceRunning(requireContext())


    }

}