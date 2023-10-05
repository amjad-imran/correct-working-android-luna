package com.oreo.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOAboutDeviceBinding
import com.noisefit.ui.myDevice.manage.CheckForUpdatesViewModel
import com.noisefit_commans.common.copyToClipBoard
import com.noisefit_commans.common.decodeHex
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.FirebaseLunaAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.RingSerialNoParser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OAboutDeviceFragment :
    BaseFragment<FragmentOAboutDeviceBinding>(FragmentOAboutDeviceBinding::inflate) {
    val mViewModel: OAboutDeviceViewModel by viewModels()

    @Inject
    lateinit var ringDataStore: RingDataStore

    @Inject
    lateinit var watchDataStore: WatchDataStore


    private val updateViewModel: CheckForUpdatesViewModel by activityViewModels()

    val adapter: AboutDeviceAdapter by lazy {
        AboutDeviceAdapter()
    }
    var connectedDevice: ColorFitDevice? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi()
    }

    private fun setUi() {
        connectedDevice = ringDataStore.getRingDevice()
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
                    val sNo = watchDataStore.getSerialNo()
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


    override fun initListener() {
        binding.toolbar.tvTitle.text = getString(R.string.text_about_device)
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnCheckForUpdates.setOnClickListener {
            updateViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_ABOUTDEVICE_UPDATE_CLICK)
            updateViewModel.checkForUpdates(false)
        }
        binding.btnCopyMac.setOnClickListener {
            updateViewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_ABOUTDEVICE_COPY_MAC_CLICK)
            connectedDevice?.address?.copyToClipBoard()
        }
    }

    override fun subscribeObservers() {

        updateViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        mViewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        updateViewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        updateViewModel.updateInfo.observe(viewLifecycleOwner) {
            it.getContent()?.let { res ->
                updateViewModel.sessionManager.forceOtaResponseRing = res
                navigate(R.id.oreoUpdateRingFragment)
            }
        }

        updateViewModel.updateAvailable.observe(viewLifecycleOwner) {
            it.getContent()?.let { isAvailable ->
                if (!isAvailable) {
                    navigate(R.id.oreoBottomSheetNoUpdateAvailable)
                }
            }

        }
    }

}