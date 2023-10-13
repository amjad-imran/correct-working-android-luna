package com.noisefit.ui.myDevice

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.FragmentRestartBottomDialogBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val REST_REQUEST_KEY = "REST_REQUEST_KEY"

@AndroidEntryPoint
class RestartBottomDialogFragment :
    BaseBottomSheetWithTransparent<FragmentRestartBottomDialogBinding>(
        FragmentRestartBottomDialogBinding::inflate
    ) {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var ringDataStore: RingDataStore

    private var connectedDevice: ColorFitDevice? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        connectedDevice =
            ringDataStore.getRingDevice()

        val title = "Soft reset the ring?"

        val messageBuilder = ""



        binding.tvTitle.text = title
        binding.tvPrivacy.text = messageBuilder.toString()
        binding.btnAllow.setOnClickListener {

            if (!sessionManager.isDeviceConnected()) {
                navigateUpSafe()
                setFragmentResult(
                    REST_REQUEST_KEY,
                    bundleOf("ring_not_connected" to true)
                )
                return@setOnClickListener
            }

            navigateUpSafe()

            setFragmentResult(
                REST_REQUEST_KEY,
                bundleOf("reset" to true)
            )
        }

        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}