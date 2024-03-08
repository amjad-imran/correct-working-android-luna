package com.noisefit.ui.onboarding.setup

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.airbnb.lottie.LottieDrawable
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentSettingUpDeviceBinding
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.DeviceUnits
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImageWithCache
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LOW_VIBRATION
import com.noisefit_commans.utils.VibrationUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class SettingUpDeviceFragment :
    BaseFragment<FragmentSettingUpDeviceBinding>(FragmentSettingUpDeviceBinding::inflate) {

    val viewModel: DeviceSetupViewModel by viewModels()
    private val sharedViewModel: DeviceSetupSharedViewModel by activityViewModels()

    @Inject
    lateinit var vibrationUtils: VibrationUtils


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel.updateProgress3.postValue(50)
        viewModel.setUpUserDetails()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.animView.repeatCount = LottieDrawable.INFINITE
        binding.animView.setAnimation(R.raw.anim_pairing)
        binding.animView.playAnimation()

        binding.ivRingImage.loadImageWithCache(requireContext(), sharedViewModel.getRingImage2())

        Handler(Looper.getMainLooper()).postDelayed({
            if (viewModel.sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                if (viewModel.setupStarted.not()) {
                    context.showShortToast(getString(R.string.text_no_device_connected))
                    activity?.finish()
                }
            }
        }, 5000)
    }

    override fun initListener() {


    }

    override fun subscribeObservers() {

        viewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            LOGS.d("CONNECT_STATE", "PairingSuccessFragment > $connectedState")
            when (connectedState) {
                is ConnectState.ConnectSuccess -> {
                    if (!viewModel.setupStarted) {
                        viewModel.setupStarted = true
                        initDefaultValue()
                    }

                }

                else -> {}
            }
        }
    }

    private fun initDefaultValue() {
        Handler(Looper.getMainLooper()).postDelayed({
            updateDeviceDateTime()
        }, 1000)
        Handler(Looper.getMainLooper()).postDelayed({
            setUserInfo()
        }, 2000)
        Handler(Looper.getMainLooper()).postDelayed({
            setUnit()
        }, 3000)

        Handler(Looper.getMainLooper()).postDelayed({

            val isUpdateUserDeviceDone = viewModel.ringDataStore.isUpdateUserDeviceDone()
            if (isUpdateUserDeviceDone.not()) {
                viewModel.ringDataStore.getRingDevice()?.let {
                    sharedViewModel.updateUserDevice(it, true)
                }
            } else {
                setupSuccess()
            }
        }, 4000)

        sharedViewModel.navigateToDeviceSetupSuccess.observe(this) {
            it.getContent()?.let {
                setupSuccess()
            }
        }

    }

    private fun setupSuccess() {
        vibrationUtils.vibrate(LOW_VIBRATION)
        viewModel.localDataStore.setDeviceSetupStatus(2)
        tryCatch {
            navigate(SettingUpDeviceFragmentDirections.navigateToSetupSuccess())
        }
    }

    private fun setUnit() {
        viewModel.getUserGoal1().let {
            val unit = DeviceUnits(
                unitSystem = it.unitSystem
            )
            viewModel.sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.SetDeviceUnits(unit)
            )
        }
    }

    private fun setUserInfo() {
        val userGoals = viewModel.getUserGoal1()
        val userInfo = viewModel.getUserInfo()
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetUserInfo(userInfo, userGoals, viewModel.user?.firstName)
        )
    }

    private fun updateDeviceDateTime() {
        val deviceUnits = DateFormats.getDefaultUnitFormats(requireContext())
        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetDeviceDateTime(
                Calendar.getInstance(),
                deviceUnits
            )
        )
        deviceUnits.timeFormat?.let { viewModel.localDataStore.setTimeFormat(it) }
    }


}