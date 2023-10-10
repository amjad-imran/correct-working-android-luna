package com.noisefit.ui.myDevice

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentUnpairBottomDialogBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.showShortToast
import com.noisefit.watch.CallingWatchUtils
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.models.ColorFitDevice
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val UNPAIR_REQUEST_KEY = "UNPAIR_REQUEST_KEY"

@AndroidEntryPoint
class UnpairBottomDialogFragment :
    BaseBottomSheetWithTransparent<FragmentUnpairBottomDialogBinding>(
        FragmentUnpairBottomDialogBinding::inflate
    ) {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var callingWatchUtils: CallingWatchUtils

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var ringDataStore: RingDataStore

    private var connectedDevice: ColorFitDevice? = null

    var defValue = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        defValue = "Ring"

        connectedDevice =
            ringDataStore.getRingDevice()

        val bleName = connectedDevice?.bluetoothName ?: defValue
        val title = "Unpair your $bleName?"

        val messageBuilder =
            StringBuilder("Are you sure you want to unpair your $bleName?")

//        val callingWatchMessage = getBleCallingStatusMessage()
//        if (!callingWatchMessage.isNullOrEmpty()) {
//            messageBuilder.append("\n\n")
//            messageBuilder.append(callingWatchMessage)
//        }

        binding.tvTitle.text = title
        binding.tvPrivacy.text = messageBuilder.toString()
        binding.btnAllow.setOnClickListener {

            if (!sessionManager.isDeviceConnected()) {
                navigateUpSafe()
                setFragmentResult(
                    UNPAIR_REQUEST_KEY,
                    bundleOf("ring_not_connected" to true)
                )
                return@setOnClickListener
            }

            navigateUpSafe()

            setFragmentResult(
                UNPAIR_REQUEST_KEY,
                bundleOf("unpair" to true)
            )
        }

       /* binding.btnAllow.setOnLongClickListener {
            if (connectedDevice != null) {

                setFragmentResult(
                    UNPAIR_REQUEST_KEY,
                    bundleOf("force_unpair" to true)
                )
                navigateUpSafe()
            }


            true
        }*/
        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
    }

    private fun getBleCallingStatusMessage(): String? {
        val deviceFeatures = localDataStore.getDeviceFeatures() ?: return null
        val callingWatchName = callingWatchUtils.getCallingWatchBleName(deviceFeatures)
        if (callingWatchName.isNullOrEmpty()) {
            return null
        }
        return NoiseFitApplicationMain.context!!.getString(
            R.string.text_disconnect_calling_watch,
            callingWatchName
        )
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}