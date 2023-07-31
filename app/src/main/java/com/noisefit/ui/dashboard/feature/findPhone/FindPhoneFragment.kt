package com.noisefit.ui.dashboard.feature.findPhone

import android.os.Bundle
import android.view.View
import com.noisefit.R
import com.noisefit.databinding.FragmentFindPhoneBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.SwitchSetting
import com.noisefit_commans.ui.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FindPhoneFragment :
    BaseFragment<FragmentFindPhoneBinding>(FragmentFindPhoneBinding::inflate) {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager.sendQueryAction(QueryAction.GetFindPhoneSwitch)
        binding.progressBar.root.visible()
    }

    private fun updateReminder(status: Boolean) {
        binding.progressBar.root.visible()
        sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetFindMyPhone(SwitchSetting(status = status)))
    }

    private fun setSwitchState(isChecked: Boolean) {
        binding.lytFeatureTile.llSwitch.isChecked = isChecked
    }


    override fun initListener() {
        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_find_my_device)
            tvTitle.text = getString(R.string.text_find_my_phone)
            tvTitleDisc.text = getString(R.string.text_not_receiving_alerts)
            tvTitleDisc.setTextColor(requireActivity().resources.getColor(com.noisefit_commans.R.color.text_accent_color))
            llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener
                if (isChecked) {
                    updateReminder(true)
                } else {
                    updateReminder(false)
                }


            }
        }
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_find_my_phone)
            //  tvDesc.text = getString(R.string.text_allow_your_device_to_remind_you_to_get_up)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }
    }

    override fun subscribeObservers() {
        sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.FindMyPhoneSwitchObtained -> {
                    setSwitchState(it.switchSetting.status)
                    binding.progressBar.root.gone()
                }
                else -> {}
            }
        }

        sessionManager.updateDeviceCallback.observe(this) {
            val event = it.getContent() ?: return@observe
            when (event) {
                is UpdateDeviceDataCallback.FindPhoneUpdated -> {
                    binding.progressBar.root.gone()
                    if (event.success) {
                        context.showShortToast(getString(R.string.text_find_phone_updated))
                    }
                }
                else -> {}
            }
        }
    }

}