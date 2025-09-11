package com.oreo.ui.profile.ring

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOAboutRingBinding
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
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.ui.device.OMyDeviceViewModel
import com.oreo.ui.home.summary.update.UpdateLaunchMode
import com.oreo.ui.profile.AboutDeviceAdapter
import com.oreo.ui.profile.AboutDeviceData
import com.oreo.ui.profile.OAboutDeviceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class OAboutRingFragment : BaseFragment<FragmentOAboutRingBinding>(FragmentOAboutRingBinding::inflate) {

    private val viewModel: OAboutDeviceViewModel by viewModels()
    private val updateViewModel: CheckForUpdatesViewModel by activityViewModels()

    private val mViewModel: OMyDeviceViewModel by viewModels()

    val adapter: AboutDeviceAdapter by lazy {
        AboutDeviceAdapter()
    }

    val userInfoAdapter: AboutDeviceAdapter by lazy {
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
            requireContext(),
            if(connectedDevice?.ringInfo?.image3.isNullOrEmpty())
                "https://luna-cdn.gonoise.com/production/ring/set_2/Luna+Gen+2.538+(1)+1.png"
                else
                    connectedDevice?.ringInfo?.image3
        )
        binding.tvVersion.text = "MAC ${connectedDevice?.address ?: ""}"

        setChargeProgressUi()
        //
        binding.rvRingData.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRingData.adapter = adapter

        binding.rvUserData.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUserData.adapter = userInfoAdapter
        viewModel.getUserData()
        //

        connectedDevice?.let {
            adapter.setDataSet(generateData(it))
        }

        updateViewModel.sessionManager.sendQueryAction(QueryAction.QueryBatteryPower)
        updateViewModel.sessionManager.sendQueryAction(QueryAction.QueryFirmwareVersion)


    }

    private fun setChargeProgressUi() {
        val lytChargeProgress = binding.lytChargeProgress
        lytChargeProgress.ivImage.setImageResource(R.drawable.ic_ring_for_perc)

    }

    private fun generateData(connectedDevice: ColorFitDevice): List<AboutDeviceData> {
        val response = ArrayList<AboutDeviceData>()

        response.add(
            AboutDeviceData(
                getString(R.string.text_generation),
                "${getGeneration(connectedDevice.ringInfo?.serialNoRaw)}"
            )
        )

        val size = if (connectedDevice.ringInfo?.size != null) {
            "${connectedDevice.ringInfo?.size}"
        } else {
            "-"
        }
        response.add(
            AboutDeviceData(
                getString(R.string.text_colour),
                connectedDevice.ringInfo?.color ?: "-"
            )
        )
        response.add(AboutDeviceData(getString(R.string.text_size), size))
        response.add(
            AboutDeviceData(
                getString(R.string.text_serial_number),
                if (connectedDevice.ringInfo?.serialNoRaw.isNullOrEmpty()) {
                    val sNo = updateViewModel.watchDataStore.getSerialNo()
                    sNo ?: "-"
                } else {
                    connectedDevice.ringInfo?.serialNoRaw ?: "-"
                }
            )
        )

        response.add(
            AboutDeviceData(
                getString(R.string.text_mac_address),
                connectedDevice.address ?: ""
            )
        )
        response.add(
            AboutDeviceData(
                getString(R.string.text_version),
                "${if (WatchInfoGlobals.firmwareVersionRing != null) "${WatchInfoGlobals.firmwareVersionRing}" else ""}"
            )
        )

        return response


    }

    private fun getGeneration(serialNoRaw: String?): Int {
        if (serialNoRaw == null) return 1

        return try {
            serialNoRaw.substring(1, 2).toInt()
        } catch (exp: Exception) {
            exp.printStackTrace()
            1
        }
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
                context.showShortToast(getString(R.string.text_ring_not_connected))

            }
        }
        binding.btnCopyMac.setOnClickListener {
            updateViewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_aboutdevice_copy_mac_click)
            connectedDevice?.address?.copyToClipBoard(
                getString(R.string.text_copied)
            )
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

        viewModel.otaUpdateInfo.observe(this@OAboutRingFragment) {
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

        viewModel.userInfoListData.observe(this){
            userInfoAdapter.setDataSet(it)
        }

        mViewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                /*is ConnectState.ConnectFailed -> {

                }

                is ConnectState.Connecting -> {

                }*/

                is ConnectState.ConnectSuccess -> {
                    setStateConnected(true)
                }

                else -> {
                    setStateConnected(false)
                }

                /*is ConnectState.UnPaired -> {



                    *//* val hasWatchDevice = mViewModel.localDataStore.getConnectedDevice()
                     if (hasWatchDevice != null) {
                         mViewModel.startWatchFlow.postValue(Event(true))
                     }*//*
                }

                else -> {}*/
            }

        }

    }

    private fun setStateConnected(isConnected: Boolean) {
        val batteryPercent = mViewModel.watchDataStore.getBatteryPercentRing()
        binding.lytChargeProgress.apply {
            if(isConnected) {
                val screenWidthHalf = resources.displayMetrics.widthPixels / 2

                this.root.post {
                    val params = this.root.layoutParams
                    params.width = screenWidthHalf
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    this.root.layoutParams = params
                    //
                    ivImage.setImageResource(R.drawable.ic_ring_for_perc)
                    tvPercVal.text = "$batteryPercent%"
                    linearProgressIndicator.setIndicatorColor(
                        getIndicatorColor(
                            batteryPercent,
                            /*mViewModel.sessionManager.isRingCharging.value ?: */false
                        )
                    )
                    linearProgressIndicator.progress = batteryPercent

                    tvPercVal.visible()
                    lPbContainer.visible()
//                    ivLightening.setVisibilityByCondition(mViewModel.sessionManager.isRingCharging.value ?: false)
                }
            }else{
                tvPercVal.gone()
                lPbContainer.gone()
                this.root.post {
                    val params = this.root.layoutParams
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    this.root.layoutParams = params

                    this.ivImage.setImageResource(R.drawable.ic_ring_for_perc)
                }
            }
        }
    }

    private fun getIndicatorColor(value: Int, isCharging: Boolean = false): Int {
        return when (value) {
            in 0..20 -> "#CC2929".toColorInt()
            in 21..40 -> if (isCharging) "#29CC74".toColorInt() else "#CC8029".toColorInt()
            else -> if (isCharging) "#29CC74".toColorInt() else "#FFFFFF".toColorInt()
        }
    }

}