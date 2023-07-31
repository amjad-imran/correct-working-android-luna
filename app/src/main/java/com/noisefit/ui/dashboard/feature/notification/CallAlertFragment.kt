package com.noisefit.ui.dashboard.feature.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.FragmentCallAlertBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.ui.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class CallAlertFragment :
    BaseFragment<FragmentCallAlertBinding>(FragmentCallAlertBinding::inflate) {

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var watchedSDK: WatchesSDK


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (localDataStore.getDeviceFeatures()?.ble_calling_alert == 1) {
            binding.lytBleCallTile.root.visible()
            binding.progressBar.root.visible()
            sessionManager.sendQueryAction(QueryAction.GetQuickBleCallingSwitch)
        }

        binding.lytMediaStopInfo.root.gone()

//        if (watchedSDK.getWatchType() == SDKWatchType.SDK_QUBE) {
//            binding.lytMediaStopInfo.apply {
//                this.tvDescOther.text = getString(R.string.text_media_play_stop)
//                this.root.visible()
//                this.bGoToSettings.setOnClickListener {
//                    startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
//                }
//            }
//        } else {
//            binding.lytMediaStopInfo.root.gone()
//        }

    }

    override fun initListener() {
        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_call_accept_white)
            tvTitle.text = getString(R.string.text_call_alert)
            llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener
                if (isChecked) {
                    if (!checkCallPermissions()) {
                        callPermissionResultListener.launch(REQUIRED_CALL_PERMISSIONS)
                        return@setOnCheckedChangeListener
                    }
                    localDataStore.setCallAlertStatus(true)
                    sessionManager.logInsiderAppEvent(
                        InsiderAppEvents.CALL_ALERT_CLICK,
                        HashMap<String, Any>().apply {
                            this["is_enabled"] = true
                        })
                } else {
                    localDataStore.setCallAlertStatus(false)
                    sessionManager.logInsiderAppEvent(
                        InsiderAppEvents.CALL_ALERT_CLICK,
                        HashMap<String, Any>().apply {
                            this["is_enabled"] = false
                        })
                }

            }
        }

        binding.lytBleCallTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_call_accept_white)
            tvTitle.text = getString(R.string.text_bluetooth_calling)
            llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (!buttonView.isPressed) return@setOnCheckedChangeListener
                updateBleCallingState(isChecked)
                if (isChecked)
                    sessionManager.logInsiderAppEvent(
                        InsiderAppEvents.BLUETOOTH_CALLING_CLICK,
                        HashMap<String, Any>().apply {
                            this["is_enabled"] = true
                        })
                else
                    sessionManager.logInsiderAppEvent(
                        InsiderAppEvents.BLUETOOTH_CALLING_CLICK,
                        HashMap<String, Any>().apply {
                            this["is_enabled"] = true
                        })
            }
        }
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_call_alert)
            tvDesc.text = getString(R.string.text_allow_alerts_for_incoming_calls_on_your_device)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }
        setSwitchState(localDataStore.isCallAlertEnabled())

    }

    private fun setSwitchState(isChecked: Boolean) {
        binding.lytFeatureTile.llSwitch.isChecked = isChecked
    }

    private fun updateBleCallingState(state: Boolean) {
        binding.progressBar.root.visible()
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetBleCallingSwitch(
                state
            )
        )
    }


    private fun setBleCallingState(state: Boolean) {
        binding.lytBleCallTile.llSwitch.isChecked = state
    }

    override fun subscribeObservers() {
        sessionManager.deviceQueryCallback.observe(this) {
            when (it) {
                is QueryCallback.BleCallingSwitchObtained -> {
                    binding.progressBar.root.gone()
                    setBleCallingState(it.success)

                }

                else -> {}
            }
        }


        sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let { event ->
                if (event is UpdateDeviceDataCallback.BleCallingSwitchUpdated) {
                    binding.progressBar.root.gone()
                    context.showShortToast(getString(R.string.text_bluetooth_calling_updated))
                }
            }
        }
    }

    private fun checkCallPermissions() = REQUIRED_CALL_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private val callPermissionResultListener = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {

        if (checkCallPermissions()) {
            setSwitchState(true)
            localDataStore.setCallAlertStatus(true)
        } else {
            setSwitchState(false)
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.AreYouSureDialog(
                        getString(R.string.text_permission_required),
                        getString(R.string.text_permission_denial_call),
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

    companion object {
        private val REQUIRED_CALL_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ANSWER_PHONE_CALLS,
                    Manifest.permission.CALL_PHONE
                )
            } else {
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.CALL_PHONE
                )
            }

    }


}