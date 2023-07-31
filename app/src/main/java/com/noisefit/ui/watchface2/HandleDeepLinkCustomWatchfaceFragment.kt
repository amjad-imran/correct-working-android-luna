package com.noisefit.ui.watchface2

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.noisefit.luna.R
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.luna.databinding.LayoutWatchfaceDeeplinkBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HandleDeepLinkCustomWatchfaceFragment :
    BaseFragment<LayoutWatchfaceDeeplinkBinding>(LayoutWatchfaceDeeplinkBinding::inflate) {

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var watchesSDK: WatchesSDK

    var connectedDevice: ColorFitDevice? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val device = localDataStore.getConnectedDevice() ?: kotlin.run {
            context.showShortToast(getString(R.string.text_device_not_connected))
            navigateUpSafe()
            return
        }

        if (watchesSDK.hasCategoryWatchFace()) {
            findNavController().popBackStack(R.id.handleDeepLinkCustomWatchfaceFragment, true)
            val sdkWatchType = watchesSDK.getWatchType()
            when (device.deviceType) {
                DeviceType.COLORFIT_PRO_3.deviceType,
                DeviceType.NOISEFIT_AGILE.deviceType,
                DeviceType.NOISEFIT_AGILE_OTA.deviceType,
                DeviceType.NOISEFIT_AGILE_DFU.deviceType,
                DeviceType.NOISEFIT_ACTIVE.deviceType,
                DeviceType.NOISEFIT_ACTIVE_OTA.deviceType -> {
                    navigateUpSafe()
                    return
                }

                DeviceType.COLORFIT_VISION.deviceType -> {
                    navigate(R.id.visionCustomWatchFaceFragment)
                }

                DeviceType.COLORFIT_NAV.deviceType -> {
                    navigate(R.id.nfhCustomWatchFaceFragment)
                }

                else -> {
                    if (sdkWatchType == SDKWatchType.SDK_ZH ||
                        sdkWatchType == SDKWatchType.SDK_NAV_PLUS ||
                        sdkWatchType == SDKWatchType.SDK_RYEEX ||
                        sdkWatchType == SDKWatchType.SDK_EVOLVE
                    ) {
                        navigate(R.id.diyWatchFaceFragment)
                    }
                }
            }
        } else {
            findNavController().popBackStack(R.id.handleDeepLinkCustomWatchfaceFragment, true)
            if (device.deviceType.equals(DeviceType.COLORFIT_NAV.deviceType, true)) {
                navigate(R.id.nfhCustomWatchFaceFragment)
            } else if (device.deviceType.equals(DeviceType.COLORFIT_VISION.deviceType, true)) {
                navigate(R.id.visionCustomWatchFaceFragment)
            } else if (watchesSDK.getWatchType(device) == SDKWatchType.SDK_QUBE) {
                navigate(R.id.daFitCustomWatchFaceFragment)
            } else if (device.deviceType.equals(DeviceType.COLORFIT_NAV_PLUS.deviceType, true) ||
                device.deviceType.equals(DeviceType.XFIT.deviceType, true)
            ) {
                navigate(R.id.customiseWatchfaceFragment)
            } else {
                navigateUpSafe()
                return
            }

        }
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}