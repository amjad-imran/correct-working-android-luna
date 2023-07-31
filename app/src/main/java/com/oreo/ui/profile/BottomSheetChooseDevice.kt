package com.oreo.ui.profile

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.text.color
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetChooseDeviceBinding
import com.noisefit.ui.common.CONFIRM_DIALOG_REQUEST_KEY
import com.noisefit_commans.data.enums.Device
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadWatchImage
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val CHOOSE_DEVICE_KEY = "CHOOSE_DEVICE_KEY"

@AndroidEntryPoint
class BottomSheetChooseDevice :
    BaseBottomSheetWithTransparent<BottomSheetChooseDeviceBinding>(BottomSheetChooseDeviceBinding::inflate) {


    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var ringDataStore: RingDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, com.noisefit_commans.R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()

    }


    private fun initUI() {
        val device1 = localDataStore.getConnectedDevice()
        val device2 = ringDataStore.getRingDevice()

        if (device1 != null) {
            binding.groupDevice1.visible()
            binding.ivDevice1.loadWatchImage(
                requireContext(), device1.url,
                R.drawable.watch_default
            )
            binding.tvDevice1Name.text = device1.bluetoothName
            binding.tvDevice1Mac.text = device1.address
        } else {
            binding.groupDevice1.gone()
        }

        if (device2 != null) {
            binding.groupDevice2.visible()
            binding.ivDevice2.loadWatchImage(
                requireContext(), device2.ringInfo?.image ?: "",
                R.drawable.watch_default//TODO add placeholder - pending from design
            )
            binding.tvDevice2Name.text = device2.bluetoothName
            binding.tvDevice2Mac.text = device2.address


            val colorInfo = if (device2.ringInfo != null) {
                " (${device2.ringInfo?.color}, Size ${device2.ringInfo?.size})"
            } else {
                null
            }
            binding.tvDevice2Detail.text = colorInfo

        } else {
            binding.groupDevice2.gone()
        }


        if (device1 != null && device2 != null) {
            binding.tvAddDevice.gone()
        } else {
            binding.tvAddDevice.visible()
        }


    }


    override fun initListener() {

        binding.vDevice1.setOnClickListener {
            navigateUpSafe()
            setFragmentResult(
                CHOOSE_DEVICE_KEY,
                bundleOf("selectedDevice" to Device.SMARTWATCH)
            )
        }
        binding.vDevice2.setOnClickListener {
            navigateUpSafe()
            setFragmentResult(
                CHOOSE_DEVICE_KEY,
                bundleOf("selectedDevice" to Device.RING)
            )
        }

        binding.tvAddDevice.setOnClickListener {
            navigateUpSafe()
            setFragmentResult(
                CHOOSE_DEVICE_KEY,
                bundleOf("addDevice" to true)
            )
        }

    }

    override fun subscribeObservers() {

    }


}