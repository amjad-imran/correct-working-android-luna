package com.noisefit.ui.myDevice.manage

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentDeviceManagementBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit.ui.myDevice.MyDeviceViewModel
import com.noisefit.ui.myDevice.UNPAIR_REQUEST_KEY
import com.noisefit.ui.myDevice.warranty.WarrantyViewModel
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.watch.ConnectionHandler
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.common.copyToClipBoard
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class DeviceManagementFragment :
    BaseFragment<FragmentDeviceManagementBinding>(FragmentDeviceManagementBinding::inflate) {

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var connectHandler: ConnectionHandler

    @Inject
    lateinit var sessionManager: SessionManager

    private val viewModel: CheckForUpdatesViewModel by activityViewModels()

    private val warrantyViewModel: WarrantyViewModel by viewModels()

    private val myDeviceViewModel: MyDeviceViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytToolbar.tvTitle.text = getString(R.string.text_device_management)
        binding.features = localDataStore.getDeviceFeatures()

        sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)
        localDataStore.getConnectedDevice()?.let {
            setWatchUi(it)
        }

        if (viewModel.isUpdateAllowed()) {
            binding.tvCheckForUpdates.visible()
        } else {
            binding.tvCheckForUpdates.gone()
        }

    }

    override fun onResume() {
        super.onResume()
        //warrantyViewModel.checkWarranty()
    }

    @SuppressLint("Range")
    private fun setWatchUi(noiseFitDevice: ColorFitDevice) {
        binding.imgWatch.loadWatchImage(
            requireContext(),
            noiseFitDevice.url,
            R.drawable.watch_default
        )
        binding.tvWatchName.text = noiseFitDevice.bluetoothName
        binding.tvWatchMacAddress.text = "MAC ${noiseFitDevice.address}"

        binding.btnCopyMac.setOnClickListener {
            noiseFitDevice.address?.copyToClipBoard()
        }
        viewModel.watchDataStore.getFirmwareVersion().let {
            if (it.isNotEmpty()) {
                binding.tvFirmwareVersion.text = "Firmware version: $it"

                viewModel.sessionManager.addUserAttributeToInsider(false,HashMap<String,Any>().apply {
                    this["pair_device_firmware_number"]=it
                })
            }
        }


    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvCheckForUpdates.setOnClickListener {
            if (sessionManager.connectState.value is ConnectState.ConnectSuccess) {
                sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_DEVICE_CHECK_FOR_UPDATES_CLICK)
                navigate(R.id.checkForUpdatesFragment)
            } else {
                context.showShortToast(getString(R.string.text_device_not_connected))
                navigateUpSafe()
            }
        }

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvFactoryReset.setOnClickListener {
            setFragmentResultListener(CONFIRM_DIALOG_REQUEST_KEY) { key, bundle ->
                val isAllowClicked = bundle.getBoolean("allowClicked")

                if (isAllowClicked) {
                    unpairDevice()
                }
            }

            navigate(
                DeviceManagementFragmentDirections.actionDeviceManagementFragmentToConfirmBottomDialogFragment(
                    getString(R.string.text_factory_reset),
                    getString(R.string.text_factory_reset_message)
                )
            )
        }
        binding.tvRestartWatch.setOnClickListener {
            setFragmentResultListener(CONFIRM_DIALOG_REQUEST_KEY) { key, bundle ->
                val isAllowClicked = bundle.getBoolean("allowClicked")
                if (isAllowClicked) {
                    if (isAdded) {
                        sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetRestartDevice())
                        navigateUpSafe()
                    }
                }
            }

            navigate(
                DeviceManagementFragmentDirections.actionDeviceManagementFragmentToConfirmBottomDialogFragment(
                    getString(R.string.text_device_restart),
                    getString(R.string.text_device_restart_message)
                )
            )

        }

        binding.tvUnPairDevice.setOnClickListener {
            setFragmentResultListener(UNPAIR_REQUEST_KEY) { key, bundle ->
                val unpairDevice = bundle.getBoolean("unpair")

                if (unpairDevice) {
                    unpairDevice()
                }
            }
            sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_DEVICE_UNPAIR_CLICK)
            navigate(DeviceManagementFragmentDirections.actionDeviceManagementFragmentToUnpairBottomDialogFragment())
        }

        binding.tvWarrantyRegistration.setOnClickListener {

            /*if(warrantyViewModel.isWarrantyAlreadyRegistered.value==true){
                context.showShortToast("Warranty already registered")
            }*/

            sessionManager.logInsiderAppEvent(InsiderAppEvents.MY_DEVICE_WARRANTY_REGISTRATION_CLICK)
            navigate(R.id.warrantyFragmentOld)
        }
    }

    override fun subscribeObservers() {

        sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.FirmwareVersionObtained -> {
                    binding.tvFirmwareVersion.text =
                        "Firmware version: ${it.deviceFirmware.version}"
                }
                else -> {}
            }
        }

        sessionManager.connectState.observe(viewLifecycleOwner) { connectedState ->
            when (connectedState) {
                is ConnectState.UnPaired -> {
                    binding.progressBar.root.gone()
                    navigateUpSafe()
                }
                is ConnectState.DisconnectSuccess -> {
                    binding.progressBar.root.gone()
                    navigateUpSafe()
                }
                else -> {}
            }
        }

        warrantyViewModel.isWarrantyAlreadyRegistered.observe(this) {
            val color = if (it) {
                R.color.badge_color_connected
            } else {
                R.color.badge_color
            }
            binding.tvWarrantyRegistration.showBadge(true, color)

        }

    }

    private fun unpairDevice() {
        myDeviceViewModel.removeWatchTokenFromServer()
        localDataStore.getConnectedDevice()?.let {
            connectHandler.getConnectionActions(it)?.disconnect(it)
        }
        if (isAdded) {
            binding.progressBar.root.visible()
        }
    }
}