package com.oreo.ui.device

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.bold
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.MainActivity
import com.noisefit.R
import com.noisefit.databinding.FragmentOreoMyDeviceBinding
import com.noisefit.oreo.OreoMainActivity
import com.noisefit.ui.SplashActivity
import com.noisefit.ui.myDevice.MyDeviceAction
import com.noisefit.ui.myDevice.MyDeviceFragmentDirections
import com.noisefit.ui.myDevice.UNPAIR_REQUEST_KEY
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.enums.Device
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.loadWatchImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.Event
import com.oreo.ui.profile.CHOOSE_DEVICE_KEY
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OreoMyDeviceFragment :
    BaseFragment<FragmentOreoMyDeviceBinding>(FragmentOreoMyDeviceBinding::inflate) {
    private val mViewModel: OMyDeviceViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
    }

    override fun initListener() {

        binding.btnReset.setOnClickListener {
            mViewModel.sessionManager.sendQueryAction(QueryAction.ResetTrigger)
        }


        binding.lytPairYourDeviceHeader.btnPairDevice.setOnClickListener {
            startActivity(PairDeviceActivity.getStartIntent(requireContext(), true))
            //activity?.finish()
        }

        binding.toolbar.setOnClickListener {
            showDeviceSelector()
        }
        binding.ivToolbarDeviceArrow.setOnClickListener {
            showDeviceSelector()
        }

        binding.rowAboutDevice.setOnClickListener {
            navigate(R.id.OAboutDeviceFragment)
        }
        binding.rowGoogleFit.setOnClickListener {
            navigate(R.id.googleFitFragmentOreo)
        }

        binding.rowWarrantyRegistration.setOnClickListener {
            navigate(R.id.warrantyFragmentOreo)
        }
        binding.btnUnpair.setOnClickListener {
            setFragmentResultListener(UNPAIR_REQUEST_KEY) { _, bundle ->
                val unpairDevice = bundle.getBoolean("unpair")
                val forceUnpair = bundle.getBoolean("force_unpair")
                if (unpairDevice) {
                    showUnPairDialog()
                }
                if (forceUnpair) {
                    showForceUnPairDialog()
                }
            }

            navigate(R.id.unpairBottomDialogFragment)

        }


//        val s =
//            SpannableStringBuilder().append(getString(R.string.text_don_t_have_a_noisefit_device_yet_check_out_our_latest_collection_by_clicking_on_the))
//                .append(" ").bold { append("'") }
//                .bold { append(getString(R.string.text_get_noise)) }.bold { append("'") }
//                .append(" ").append(getString(R.string.text_in_the_navbar))
//        updateToolbarTitle(getString(R.string.text_no_device_paired))
//        binding.lytPairYourDeviceHeader.tvMsg.text = s
        binding.ivToolbarDeviceArrow.gone()

    }

    override fun subscribeObservers() {


        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        mViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        mViewModel.startWatchFlow.observe(this) {
            it.getContent()?.let {
                mViewModel.localDataStore.savePairDeviceType(Device.SMARTWATCH)
                //startWatchService()
                activity?.let { act ->
                    startActivity(SplashActivity.getStartIntent(act))
                    act.finish()
                }
            }
        }

        mViewModel.deviceConnected.observe(this) { connected ->
            if (connected) {
                binding.apply {
                    lytDeviceConnected.root.visible()
                    btnUnpair.visible()
                    btnReset.visible()
                    lytPairYourDeviceHeader.root.gone()
                }

            } else {
                binding.apply {
                    lytDeviceConnected.root.gone()
                    btnUnpair.gone()
                    btnReset.gone()
                    updateToolbarTitle("")
                    lytPairYourDeviceHeader.root.visible()
                    binding.ivToolbarDeviceArrow.gone()
                }
            }
        }

        mViewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    setStateConnecting(connectedState.noiseFitDevice)
                }

                is ConnectState.Connecting -> {
                    setStateConnecting(connectedState.noiseFitDevice)
                }

                is ConnectState.ConnectSuccess -> {
                    setStateConnected(connectedState.noiseFitDevice)
                    mViewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
                }

                is ConnectState.UnPaired -> {
                    binding.lytFeatures.gone()
                    binding.progressBar.root.gone()
                    mViewModel.updateDeviceConnectedStatus()


                    val hasWatchDevice = mViewModel.localDataStore.getConnectedDevice()
                    if (hasWatchDevice != null) {
                        mViewModel.startWatchFlow.postValue(Event(true))
                    }
                }

                is ConnectState.Hibernate -> {
                    binding.progressBar.root.gone()


                    when (mViewModel.nextAction) {
                        MyDeviceAction.ADD_DEVICE -> {
                            activity?.let {
                                startActivity(PairDeviceActivity.getStartIntent(it))
                                it.finish()
                            }
                        }

                        MyDeviceAction.SWITCH_TO_WATCH -> {
                            mViewModel.localDataStore.getConnectedDevice()?.let {
                                mViewModel.updateUserDevice(it, true)
                            }
                        }

                        else -> {}
                    }
                }

                else -> {}
            }

        }
    }

    private fun startWatchService() {
        context?.let {
            ApplicationUtils.setRescueWorkManager(it)
        }
    }

    private fun showDeviceSelector() {
        setFragmentResultListener(CHOOSE_DEVICE_KEY) { _, bundle ->
            val addDevice = bundle.getBoolean("addDevice")
            if (addDevice) {
                mViewModel.setLoading(true)
                mViewModel.nextAction = MyDeviceAction.ADD_DEVICE
                mViewModel.sessionManager.hibernateCurrentDevice {
                    if (!it) {
                        context.showShortToast(getString(R.string.text_something_went_wrong))
                    }
                }

                /* activity?.let {
                     startActivity(PairDeviceActivity.getStartIntent(it))
                     it.finish()
                 }*/
                return@setFragmentResultListener
            }

            val selectedDevice = bundle.getSerializable("selectedDevice") as Device

            if (selectedDevice == Device.SMARTWATCH) {
                mViewModel.setLoading(true)
                mViewModel.nextAction = MyDeviceAction.SWITCH_TO_WATCH
                mViewModel.sessionManager.hibernateCurrentDevice {
                    if (!it) {
                        context.showShortToast(getString(R.string.text_something_went_wrong))
                    }
                }
            }
        }
        navigate(R.id.bottomSheetChooseDevice)
    }

    private fun showForceUnPairDialog() {
        val messageBuilder =
            StringBuilder("Manually reset the device to connect again. Press unpair to continue")

        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.AreYouSureDialog(getString(R.string.text_alert),
                    messageBuilder.toString(),
                    false,
                    getString(R.string.text_unpair),
                    object : BinaryActionCallback {
                        override fun yes() {

                            mViewModel.sessionManager.forceDisconnect.value = (Event(true))
                            mViewModel.sessionManager.setConnectStateRing(ConnectState.UnPaired())

                        }

                        override fun no() {

                        }
                    })
            )
        )
    }

    private fun showUnPairDialog() {

        if (mViewModel.ringDataStore.getRingDevice() != null) {
            mViewModel.ringDataStore.getRingDevice()?.let {
                mViewModel.connectionHandler.getConnectionActions(it)?.disconnect(it)
            }

            if (isAdded) {
                binding.progressBar.root.visible()
            }
        }
    }

    private fun updateToolbarTitle(title: String?) {
        binding.toolbar.text = title ?: ""
    }

    private fun setStateConnecting(noiseFitDevice: ColorFitDevice?) {
        binding.lytDeviceConnected.apply {
            this.layoutDevice.setBackgroundResource(R.drawable.back_modal_new_red)
            tvStatus.text = getString(R.string.text_trying_to_connect)
            tvStatus.setTextColor(
                resources.getColor(
                    R.color.color_error
                )
            )
            progressBarConnecting.visible()
            ivSettingsArrow.gone()
            imgWatch.loadImage(
                requireContext(), noiseFitDevice?.ringInfo?.image ?: ""
            )
            updateToolbarTitle(noiseFitDevice?.bluetoothName)
            binding.ivToolbarDeviceArrow.visible()
            tvLastSync.text = ""
            tvBatteryPercentage.text = ""

        }

        binding.lytFeatures.gone()
    }

    private fun setStateConnected(noiseFitDevice: ColorFitDevice) {
        val batteryPercent = "${mViewModel.watchDataStore.getBatteryPercentRing()}% Battery level"

        val lastSync =
            mViewModel.sessionManager.getLastSyncTime(Device.RING)
                ?.let { DateFormats.getRelativeTime(it) }
        val lastSyncText = "Synced : ${lastSync ?: getString(R.string.text_not_yet_syncyed)}"
        binding.lytDeviceConnected.apply {
            this.layoutDevice.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
            tvStatus.text = getString(R.string.text_connected)
            tvStatus.setTextColor(
                resources.getColor(
                    R.color.white
                )
            )
            progressBarConnecting.gone()
            ivSettingsArrow.visible()
            imgWatch.loadWatchImage(
                requireContext(), noiseFitDevice.ringInfo?.image ?: "",
                R.drawable.watch_default
            )
            tvBatteryPercentage.text = batteryPercent
            updateToolbarTitle(noiseFitDevice.bluetoothName)
            binding.ivToolbarDeviceArrow.visible()

            tvLastSync.text = lastSyncText

        }

        binding.apply {
            lytFeatures.visible()
        }
    }

}