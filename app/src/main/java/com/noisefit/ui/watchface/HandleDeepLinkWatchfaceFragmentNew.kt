package com.noisefit.ui.watchface

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.navigation.fragment.findNavController
import com.noisefit.luna.R
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.luna.databinding.LayoutWatchfaceDeeplinkBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.models.ColorFitDevice
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HandleDeepLinkWatchfaceFragmentNew :
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
            findNavController().popBackStack(R.id.handleDeepLinkWatchfaceFragmentNew, true)
            navigate(R.id.watchFaceCategoryFragment,Bundle().apply {
                putInt("categoryId",-2)
                putString("categoryName","Newly Added")
            })
        } else {
            navigateUpSafe()
            return
        }
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}