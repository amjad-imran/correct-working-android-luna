package com.noisefit.ui.watchface2

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.noisefit.R
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.databinding.LayoutWatchfaceDeeplinkBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit.ui.watchface2.sub.WF2SubListFrom
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
            navigate(R.id.watchface2SubListFragment, Bundle().apply {
                putSerializable("type", WF2SubListFrom.Newly)
                putInt("categoryId", -1)
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