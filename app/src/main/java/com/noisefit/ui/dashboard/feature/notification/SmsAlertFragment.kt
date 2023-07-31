package com.noisefit.ui.dashboard.feature.notification

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.FragmentSmsAlertBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImage
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SmsAlertFragment :
    BaseFragment<FragmentSmsAlertBinding>(FragmentSmsAlertBinding::inflate) {


    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var sessionManager:SessionManager


    override fun initListener() {
        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_message)
            tvTitle.text = getString(R.string.text_sms_alert)
            tvTitleDisc.text = getString(R.string.text_data_not_syncing)
            tvTitleDisc.setTextColor(requireActivity().resources.getColor(com.noisefit_commans.R.color.text_accent_color))
            llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener
                if (isChecked) {
                    if (!checkSmsPermissions()) {
                        llSwitch.isChecked = false
                        smsPermissionResultListener.launch(REQUIRED_SMS_PERMISSIONS)
                        return@setOnCheckedChangeListener
                    }
                    localDataStore.setSMSAlertStatus(true)
                    sessionManager.logInsiderAppEvent(InsiderAppEvents.SMS_ALERT_CLICK,HashMap<String,Any>().apply {
                        this["is_enabled"]=true
                    })
                } else {
                    localDataStore.setSMSAlertStatus(false)
                    sessionManager.logInsiderAppEvent(InsiderAppEvents.SMS_ALERT_CLICK,HashMap<String,Any>().apply {
                        this["is_enabled"]=false
                    })
                }

            }
        }
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_sms_alert)
            tvDesc.text =
                getString(R.string.text_allow_alert_for_incoming_sms_on_your_device_to_get_notified)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }
        setSwitchState(localDataStore.isSMSAlertEnabled())

    }

    private fun setSwitchState(isChecked: Boolean) {
        binding.lytFeatureTile.llSwitch.isChecked = isChecked
    }

    override fun subscribeObservers() {

    }

    private val smsPermissionResultListener = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {

        if (checkSmsPermissions()) {
            setSwitchState(true)
            localDataStore.setSMSAlertStatus(true)
        } else {
            localDataStore.setSMSAlertStatus(false)
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.AreYouSureDialog(
                        getString(R.string.text_permission_required),
                        getString(R.string.text_permission_denial_sms),
                        false,
                        getString(R.string.text_allow),
                        object : BinaryActionCallback {
                            override fun yes() {
                                activity?.let {
                                    ApplicationUtils.openAppSettings(it)
                                }
                            }

                            override fun no() {

                            }

                        }
                    )
                )
            )

        }
    }

    private fun checkSmsPermissions() = REQUIRED_SMS_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_SMS_PERMISSIONS =
            arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS)
    }


}