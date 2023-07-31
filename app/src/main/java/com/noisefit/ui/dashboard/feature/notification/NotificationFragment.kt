package com.noisefit.ui.dashboard.feature.notification

import android.os.Bundle
import android.view.View
import com.noisefit.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.databinding.FragmentNotificationBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.visible
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.DeviceType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationFragment :
    BaseFragment<FragmentNotificationBinding>(FragmentNotificationBinding::inflate) {

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var sessionManager: SessionManager

    private var notificationAccessGranted = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.features = localDataStore.getDeviceFeatures()
        binding.lifecycleOwner = this
        handleOfflineSwitch()
    }

    override fun initListener() {

        val isAppNotificationEnabled = localDataStore.isNotificationAlertEnabled()
        val isSmsAlertEnabled = localDataStore.isSMSAlertEnabled()

        binding.llSmsAlert.setStatus(isSmsAlertEnabled)
        binding.llAppNotification.setStatus(isAppNotificationEnabled)

        binding.lytToolbar.apply {
            tvTitle.text = getString(R.string.text_notification)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        binding.llSmartNotifications.setOnClickListener {
            navigate(R.id.smartNotificationFragment)
        }
        binding.llSmsAlert.setOnClickListener {
            navigate(R.id.smsAlertFragment)
        }

        binding.llAppNotification.setOnClickListener {
            if (notificationAccessGranted) {
                navigate(R.id.appNotificationFragment)
            }
            else {
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

        }

        binding.llMyReminder.setOnClickListener {
            navigate(R.id.myReminderFragment)
        }


    }

    override fun onResume() {
        super.onResume()
        notificationAccessGranted = ApplicationUtils.isNotificationServiceRunning(requireContext())


    }

    private fun handleOfflineSwitch() {
        sessionManager.connectedDevice.value?.deviceType?.let {
            if (it.equals(DeviceType.COLORFIT_PRO_4_ALPHA.deviceType, true)) {
                binding.llSmartNotifications.visible()
            }
        }
    }


    override fun subscribeObservers() {

    }


}