package com.oreo.ui.home.summary.update

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.oreo.data.model.AppUpdateModel
import com.oreo.data.model.OtaUpdateModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentUpdateDetailBinding
import com.noisefit.ui.onboarding.setup.firmware.BATTERY_CHARGE_FIRMWARE
import com.noisefit.ui.onboarding.setup.firmware.UpdateAvailableFragmentDirections
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImageWithCache
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateDetailFragment :
    BaseFragment<FragmentUpdateDetailBinding>(FragmentUpdateDetailBinding::inflate) {

    private val args: UpdateDetailFragmentArgs by navArgs()
    private val viewModel: UpdateDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initLaunchMode(args.launchMode)
    }

    private fun setUiAppUpdate(appUpdateModel: AppUpdateModel) {
        binding.toolbar.tvTitle.text = getString(R.string.text_update_my_app)
        binding.tvHeader.text = appUpdateModel.description?.header
        binding.tvMessage.text = appUpdateModel.description?.longDescription
        binding.ivBack.loadImageWithCache(
            binding.ivBack.context,
            appUpdateModel.description?.imageUrl
        )
    }

    private fun setUiOtaUpdate(otaUpdateModel: OtaUpdateModel) {
        binding.toolbar.tvTitle.text = getString(R.string.text_update_my_ring)
        binding.tvHeader.text = otaUpdateModel.description?.header
        binding.tvMessage.text = otaUpdateModel.description?.longDescription
        binding.ivBack.loadImageWithCache(
            binding.ivBack.context,
            otaUpdateModel.description?.imageUrl
        )

        viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)

    }

    override fun initListener() {


        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnUpdateNow.setOnClickListener {
            when (viewModel.launchMode) {
                UpdateLaunchMode.APP -> {
                    ShareUtil.openPlayStore(requireContext(), "com.noisefit.luna")
                }

                UpdateLaunchMode.OTA -> {


                    val battery = viewModel.sessionManager.batteryPercentRing.value ?: 0
                    val isCharging = viewModel.sessionManager.isRingCharging.value ?: false
                    if (isCharging.not() && battery != 100) {
                        showNotChargingDialog()
                        return@setOnClickListener
                    }

                    viewModel.otaUpdateInfo.value?.let {
                        navigate(UpdateDetailFragmentDirections.openRingUpdate(it))
                    } ?: navigateUpSafe()
                }

                UpdateLaunchMode.OTA_DEVICE -> {
                    val battery = viewModel.sessionManager.batteryPercentRing.value ?: 0
                    val isCharging = viewModel.sessionManager.isRingCharging.value ?: false
                    if (isCharging.not() && battery != 100) {
                        showNotChargingDialog()
                        return@setOnClickListener
                    }

                    viewModel.otaUpdateInfo.value?.let {
                        navigate(UpdateDetailFragmentDirections.openRingUpdateAboutDevice(it))
                    } ?: navigateUpSafe()
                }

                null -> {
                    navigateUpSafe()
                }
            }
        }
        binding.tvRemindLater.setOnClickListener {
            when (viewModel.launchMode) {
                UpdateLaunchMode.APP -> {
                    viewModel.appRemindLater()
                    navigateUpSafe()
                }

                UpdateLaunchMode.OTA, UpdateLaunchMode.OTA_DEVICE -> {
                    viewModel.otaRemindLater()
                    navigateUpSafe()
                }

                null -> {
                    navigateUpSafe()
                }
            }
        }


    }

    private fun showNotChargingDialog() {
        navigate(R.id.bottomSheetNotCharingFirmware)
    }

    override fun subscribeObservers() {
        setFragmentResultListener(BATTERY_CHARGE_FIRMWARE) { _, bundle ->
            val start = bundle.getBoolean("start")
            if (start) {
                viewModel.startUpdate.postValue(Event(true))
            }
        }
        viewModel.startUpdate.observe(this) {
            it.getContent()?.let {
                when (viewModel.launchMode) {
                    UpdateLaunchMode.APP -> {}
                    UpdateLaunchMode.OTA -> {
                        viewModel.otaUpdateInfo.value?.let {
                            navigate(UpdateDetailFragmentDirections.openRingUpdate(it))
                        } ?: navigateUpSafe()
                    }
                    UpdateLaunchMode.OTA_DEVICE -> {
                        viewModel.otaUpdateInfo.value?.let {
                            navigate(UpdateDetailFragmentDirections.openRingUpdateAboutDevice(it))
                        } ?: navigateUpSafe()
                    }
                    null -> {}
                }
            }
        }

        viewModel.appUpdateInfo.observe(viewLifecycleOwner) {
            if (it == null) {
                navigateUpSafe()
                return@observe
            }
            setUiAppUpdate(it)
        }
        viewModel.otaUpdateInfo.observe(viewLifecycleOwner) {
            if (it == null) {
                navigateUpSafe()
                return@observe
            }

            setUiOtaUpdate(it)
        }

    }


}

enum class UpdateLaunchMode {
    APP, OTA, OTA_DEVICE
}