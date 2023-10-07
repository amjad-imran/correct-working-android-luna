package com.noisefit.ui

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.LayoutAppUpdateBottomsheetBinding
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint


const val APP_EXIT = "APP_EXIT"
const val APP_UPDATE = "APP_UPDATE"
const val APP_CONTINUE = "APP_CONTINUE"

@AndroidEntryPoint
class AppUpdateBottomSheet : BaseBottomSheetWithTransparent<LayoutAppUpdateBottomsheetBinding>(
    LayoutAppUpdateBottomsheetBinding::inflate
) {
    private var versionCheckResponse: VersionCheckResponse? = null
    var isForceUpdate = false
    var isMaintenanceMode = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        versionCheckResponse = arguments?.let {
            AppUpdateBottomSheetArgs.fromBundle(it).versonResponse
        }

        handleAppVersion(versionCheckResponse!!)
    }

    private fun handleAppVersion(versionCheckResponse: VersionCheckResponse) {
        if (versionCheckResponse.maintenanceMode == true) {
            loadAppUnderMaintenanceView()
            return
        }

        binding.ivLogo.loadImage(requireContext(), R.mipmap.ic_launcher)
        binding.tvTitle.text = getString(R.string.text_noisefit_update)
        var description = versionCheckResponse.description
        if (description.isNullOrEmpty()) {
            description = getString(R.string.text_app_update_msg)
        }
        binding.btnAllow.visible()
        binding.btnAllow.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                APP_UPDATE,
                bundleOf("isSelected" to true)
            )
        }

        binding.tvDesc.text = description
        if (versionCheckResponse.upgradeType == "force_upgrade") {
            binding.btnAllow.visible()
            binding.btnRemindLater.gone()
            isForceUpdate = true
            //binding.btnAllow.setBackgroundResource(R.drawable.btn_primary_highlight_selector)
        } else {
            //binding.btnAllow.setBackgroundResource(R.drawable.btn_primary_bg_selector)
        }

        binding.btnRemindLater.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                APP_CONTINUE,
                bundleOf("isSelected" to true)
            )
            navigateUpSafe()

        }
    }

    private fun loadAppUnderMaintenanceView() {
        binding.ivLogo.loadImage(requireContext(), R.drawable.ic_maintenance)
        binding.tvTitle.text = getString(R.string.text_app_under_maintenance)
        val maintenanceMsg =
            getString(R.string.text_noisefit_is_currently_under_maintenance) + " " + getString(R.string.text_please_check_back_again_later)
        binding.tvDesc.text = maintenanceMsg
        binding.btnAllow.visible()
        binding.btnRemindLater.gone()
        binding.btnAllow.text = "Ok"
        isMaintenanceMode = true
        binding.btnAllow.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                APP_EXIT,
                bundleOf("isSelected" to true)
            )
        }
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
                isCancelable = !(isForceUpdate || isMaintenanceMode)
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }

}