package com.noisefit.ui.myDevice.camera

import android.os.Bundle
import android.view.View
import com.noisefit.R
import com.noisefit.databinding.FragmentCameraSwitchBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CameraSwitchFragment :
    BaseFragment<FragmentCameraSwitchBinding>(FragmentCameraSwitchBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.tvTitle.text = getString(R.string.text_camera_switch)


        binding.tvDescription.text = getString(R.string.text_camera_switch_desc)

        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_camer_switch_yellow)
            tvTitle.text = getString(R.string.text_camera_switch)
            tvTitleDisc.text = getString(R.string.text_not_receiving_alerts)
            tvTitleDisc.setTextColor(requireActivity().resources.getColor(com.noisefit_commans.R.color.text_accent_color))
            llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener
                sessionManager.sendUpdateQueryAction(UpdateDeviceAction.StartCameraMode(isChecked))

            }
        }

        sessionManager.sendQueryAction(QueryAction.GetCameraSwitchSettings)
        binding.progressBar.root.visible()
    }

    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }


    }

    override fun subscribeObservers() {
        sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.CameraSwitchObtained -> {
                    binding.lytFeatureTile.llSwitch.isChecked = it.switchSetting.status
                    binding.progressBar.root.gone()
                }
                else -> {}
            }
        }
    }


}