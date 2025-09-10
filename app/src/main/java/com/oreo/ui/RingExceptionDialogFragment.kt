package com.oreo.ui

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
import com.noisefit.luna.databinding.DialogRingExceptionBinding
import com.noisefit.luna.databinding.FragmentRestartBottomDialogBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RingExceptionDialogFragment :
    BaseBottomSheetWithTransparent<DialogRingExceptionBinding>(
        DialogRingExceptionBinding::inflate
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

        binding.btnShutDown.setOnClickListener {

            if (!sessionManager.isDeviceConnected()) {
                context.showShortToast(getString(R.string.text_ring_not_connected))
                navigateUpSafe()
                return@setOnClickListener
            }
            restartDevice()
        }
    }

    private fun restartDevice() {
        binding.btnShutDown.text = getString(R.string.text_shutting_down)
        sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetShutDownDevice())
        sessionManager.resetSleepException()

        Handler(Looper.getMainLooper()).postDelayed({
            navigateUpSafe()
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