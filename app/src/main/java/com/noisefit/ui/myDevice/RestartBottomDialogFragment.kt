package com.noisefit.ui.myDevice

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRestartBottomDialogBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
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



        connectedDevice = ringDataStore.getRingDevice()

        binding.tvTitle.text = "Restart the ring?"
        binding.tvPrivacy.text =
            "We advise placing the ring on the charger. The restart process may take a few seconds. " +
                    "Your ring will automatically pair with the app once the restart is successful."

        binding.btnAllow.setOnClickListener {

            if (!sessionManager.isDeviceConnected()) {
                navigateUpSafe()
                setFragmentResult(
                    REST_REQUEST_KEY,
                    bundleOf("ring_not_connected" to true)
                )
                return@setOnClickListener
            }

            restartDevice()

        }

        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
    }

    private fun restartDevice() {
        sessionManager.sendQueryAction(QueryAction.RestartDevice)
        Handler(Looper.getMainLooper()).postDelayed({
            connectedDevice?.let {
                sessionManager.setConnectStateRing(ConnectState.Connecting(it))
            }
        }, 1000)


        binding.viewMain.gone()
        binding.viewRestart.visible()

        Handler(Looper.getMainLooper()).postDelayed({
            navigateUpSafe()

            setFragmentResult(
                REST_REQUEST_KEY,
                bundleOf("restart" to true)
            )
        }, 10 * 1000L)

    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

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