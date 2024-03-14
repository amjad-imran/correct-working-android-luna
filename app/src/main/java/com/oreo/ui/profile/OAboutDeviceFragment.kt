package com.oreo.ui.profile

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOAboutDeviceBinding
import com.noisefit.ui.myDevice.manage.CheckForUpdatesViewModel
import com.noisefit.ui.onboarding.setup.firmware.LOW_BATTERY_FIRMWARE
import com.noisefit_commans.common.copyToClipBoard
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.home.summary.update.UpdateLaunchMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OAboutDeviceFragment :
    BaseFragment<FragmentOAboutDeviceBinding>(FragmentOAboutDeviceBinding::inflate) {

    private val viewModel: OAboutDeviceViewModel by viewModels()
    private val updateViewModel: CheckForUpdatesViewModel by activityViewModels()

    val adapter: AboutDeviceAdapter by lazy {
        AboutDeviceAdapter()
    }
    var connectedDevice: ColorFitDevice? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateViewModel.mShouldFetchInfo = false
        setUi()

    }

    private fun setUi() {
        connectedDevice = updateViewModel.ringDataSore.getRingDevice()
        binding.ivDevice.loadImage(
            requireContext(), connectedDevice?.ringInfo?.image2
        )
        binding.tvVersion.text = "MAC ${connectedDevice?.address ?: ""}"


        binding.rvRingData.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRingData.adapter = adapter

        connectedDevice?.let {
            adapter.setDataSet(generateData(it))
        }

        updateViewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
        updateViewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)


    }

    private fun generateData(connectedDevice: ColorFitDevice): List<AboutDeviceData> {
        val response = ArrayList<AboutDeviceData>()

        response.add(AboutDeviceData("Generation", "1"))


        val size = if (connectedDevice.ringInfo?.size != null) {
            "${connectedDevice.ringInfo?.size}"
        } else {
            "-"
        }
        response.add(AboutDeviceData("Colour", connectedDevice.ringInfo?.color ?: "-"))
        response.add(AboutDeviceData("Size", size))
        response.add(
            AboutDeviceData(
                "Serial number",
                if (connectedDevice.ringInfo?.serialNoRaw.isNullOrEmpty()) {
                    val sNo = updateViewModel.watchDataStore.getSerialNo()
                    sNo ?: "-"
                } else {
                    connectedDevice.ringInfo?.serialNoRaw ?: "-"
                }
            )
        )

        response.add(AboutDeviceData("MAC address", connectedDevice.address ?: ""))
        response.add(
            AboutDeviceData(
                "Version",
                "${if (WatchInfoGlobals.firmwareVersionRing != null) "${WatchInfoGlobals.firmwareVersionRing}" else ""}"
            )
        )

        return response


    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun initListener() {

        binding.btnCheckForUpdates.text = if (viewModel.ringDataSore.isNewOtaAvailable()) {
            getString(R.string.text_update_available)
        } else {
            getString(R.string.text_check_for_an_update)
        }
        binding.toolbar.tvTitle.text = getString(R.string.text_about_device)
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnCheckForUpdates.setOnClickListener {
            updateViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_aboutdevice_update_click)

            if (updateViewModel.sessionManager.isDeviceConnected()) {

                val battery = viewModel.sessionManager.batteryPercentRing.value ?: 0

                if (battery != 0) {
                    if (battery <= 20
                    ) {
                        showBatteryWarning()
                        return@setOnClickListener
                    }
                }

                if (viewModel.ringDataSore.isNewOtaAvailable()) {
                    updateViewModel.setUpdateAvailable(true)
                } else {
                    checkCurrentFirmwareVersion()
                }

            } else {
                context.showShortToast("Ring not connected")

            }
        }
        binding.btnCopyMac.setOnClickListener {
            updateViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_aboutdevice_copy_mac_click)
            connectedDevice?.address?.copyToClipBoard()
        }
    }

    private fun showBatteryWarning() {
        setFragmentResultListener(LOW_BATTERY_FIRMWARE) { _, bundle ->
            val tryAgain = bundle.getBoolean("tryAgain")
            if (tryAgain) {

            }
        }
        navigate(R.id.bottomSheetLowBatteryFirmware)
    }

    private fun checkCurrentFirmwareVersion() {
        viewModel.setLoading(true)
        viewModel.sessionManager.postFirmwareDetailsOnAboutDevice = true
        viewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)
    }

    override fun subscribeObservers() {
        viewModel.sessionManager.checkForVersionUpdateAbout.observe(this) {
            it.getContent()?.let {
                viewModel.setLoading(false)
                viewModel.checkOtaVersionServer(it)
            }
        }

        viewModel.otaUpdateInfo.observe(this@OAboutDeviceFragment) {
            it.getContent()?.let {
//                updateViewModel.isOtaUpdateAvailable = true
                updateViewModel.setUpdateAvailable(true)
            }

        }

        updateViewModel.sessionManager.connectStateRing.observe(this) {
            when (it) {
                is ConnectState.ConnectSuccess -> {
                    if (updateViewModel.mShouldFetchInfo) {
                        updateViewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
                        updateViewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)
                        updateViewModel.mShouldFetchInfo = false
                    }
                }

                else -> {}
            }
        }

        updateViewModel.sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.FirmwareVersionObtained -> {
                    connectedDevice?.let {
                        adapter.setDataSet(generateData(it))
                    }
                }

                else -> {}
            }
        }


        updateViewModel.getMessages().observe(viewLifecycleOwner) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }


        viewModel.noUpdateAvailable.observe(viewLifecycleOwner) {
            it?.getContent()?.let {
                uiController.onDisplayError(getString(R.string.text_no_update_available))
            }
        }
        updateViewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }


        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        updateViewModel.updateAvailable.observe(viewLifecycleOwner) {
            it.getContent()?.let { isAvailable ->
                if (isAvailable) {

                    navigate(
                        R.id.appUpdateDetailFragment,
                        bundleOf("launchMode" to UpdateLaunchMode.OTA_DEVICE)
                    )
                }
            }

        }
    }

}