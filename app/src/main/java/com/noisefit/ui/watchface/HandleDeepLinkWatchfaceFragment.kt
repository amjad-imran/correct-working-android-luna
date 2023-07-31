package com.noisefit.ui.watchface

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.noisefit.R
import com.noisefit.databinding.LayoutWatchfaceDeeplinkBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HandleDeepLinkWatchfaceFragment :
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


        if (!isDeviceConnected()) {
            context.showShortToast(getString(R.string.text_device_not_connected))
            navigateUpSafe()
            return
        }

        if (watchesSDK.hasCategoryWatchFace()) {
            findNavController().popBackStack(R.id.handleDeepLinkWatchfaceFragment, true)
            navigate(R.id.watchface2CategoryFragment)
        } else {
            findNavController().popBackStack(R.id.handleDeepLinkWatchfaceFragment, true)
            navigate(R.id.watchFaceListingFragment)
        }
    }

    fun isDeviceConnected(): Boolean {
        connectedDevice = getDeviceConnected()
        if (connectedDevice == null) {
            return false
        }

        if (sessionManager.connectState.value is ConnectState.ConnectSuccess) {
            return true
        }
        return false
    }

    fun getDeviceConnected(): ColorFitDevice? {
        return localDataStore.getConnectedDevice()
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}