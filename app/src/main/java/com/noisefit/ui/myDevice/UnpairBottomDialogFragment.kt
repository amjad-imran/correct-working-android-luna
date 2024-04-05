package com.noisefit.ui.myDevice

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.databinding.FragmentUnpairBottomDialogBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.Event
import com.oreo.ui.device.OMyDeviceViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val UNPAIR_REQUEST_KEY = "UNPAIR_REQUEST_KEY"
const val NEW_PAIR_REQUEST_KEY = "NEW_PAIR_REQUEST_KEY"

@AndroidEntryPoint
class UnpairBottomDialogFragment :
    BaseBottomSheetWithTransparent<FragmentUnpairBottomDialogBinding>(
        FragmentUnpairBottomDialogBinding::inflate
    ) {

    @Inject
    lateinit var sessionManager: SessionManager

//    @Inject
//    lateinit var callingWatchUtils: CallingWatchUtils

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var ringDataStore: RingDataStore

    var defValue = ""

    private val mViewModel: OMyDeviceViewModel by viewModels()
    private val navArgs: UnpairBottomDialogFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        defValue = "Ring"

        binding.tvTitle.text =
            if (navArgs.forceUnpair) "Are you sure you want to pair a new ring?" else "Are you sure you want to reset?"

        binding.tvPrivacy.text =
            if (navArgs.forceUnpair) "Your ring will be unpaired & all the unsaved data will be lost" else
                "We advise placing the ring on the charger. Your ring will be unpaired & all the unsaved data will be lost"
        binding.btnAllow.text = "Reset"

        binding.btnAllow.setOnClickListener {

            if (!sessionManager.isDeviceConnected()) {
                if(navArgs.forceUnpair){

                    mViewModel.sessionManager.forceDisconnect.value = (Event(true))
                    mViewModel.sessionManager.setConnectStateRing(ConnectState.UnPaired())

                    navigateUpSafe()
                    setFragmentResult(
                        NEW_PAIR_REQUEST_KEY,
                        bundleOf("unpair" to true)
                    )

                    return@setOnClickListener
                }


                navigateUpSafe()
                setFragmentResult(
                    UNPAIR_REQUEST_KEY,
                    bundleOf("ring_not_connected" to true)
                )
                return@setOnClickListener
            }

            binding.viewUnpair.gone()
            binding.viewUnpairing.visible()

            unpairDevice()
        }

        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
    }

    private fun unpairDevice() {

        if (mViewModel.ringDataStore.getRingDevice() != null) {
            mViewModel.ringDataStore.getRingDevice()?.let {
                mViewModel.connectionHandler.getConnectionActions(it)?.disconnect(it)
            }


        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {
        mViewModel.sessionManager.connectStateRing.observe(viewLifecycleOwner) { connectedState ->
            when (connectedState) {

                is ConnectState.UnPaired -> {
                    navigateUpSafe()
                    if(navArgs.forceUnpair){
                        setFragmentResult(
                            NEW_PAIR_REQUEST_KEY,
                            bundleOf("unpair" to true)
                        )
                    }else{
                        setFragmentResult(
                            UNPAIR_REQUEST_KEY,
                            bundleOf("unpair" to true)
                        )
                    }

                }

                else -> {}
            }

        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = true
                isCancelable = false
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }


}