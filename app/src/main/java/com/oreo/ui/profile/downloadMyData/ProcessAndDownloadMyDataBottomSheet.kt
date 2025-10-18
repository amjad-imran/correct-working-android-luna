package com.oreo.ui.profile.downloadMyData

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentProcessAndDownloadMyDataBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.oreo.ui.device.OMyDeviceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProcessAndDownloadMyDataBottomSheet :
    BaseBottomSheetWithTransparent<FragmentProcessAndDownloadMyDataBottomSheetBinding>(
        FragmentProcessAndDownloadMyDataBottomSheetBinding::inflate
    ) {

    private val vm: OMyDeviceViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { defaultViewModelProviderFactory }
    )

    override fun initListener() {
        binding.btnOkay.setOnClickListener {
            vm.handleOkayBtnBsClicked()
            dismiss()
        }
    }

    override fun subscribeObservers() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.bsState.collect { state ->
                    when (state) {
                        OMyDeviceViewModel.DownloadMyDataBS.PROCESSING -> showProcessingUi()
                        OMyDeviceViewModel.DownloadMyDataBS.SUCCESS -> showSuccessUi()
                        OMyDeviceViewModel.DownloadMyDataBS.ERROR -> showErrorUi()
                        else -> Unit
                    }
                }
            }
        }
    }

    fun showProcessingUi(){
        binding.lytProcessing.visible()
        binding.lytSuccess.invisible()
    }

    fun showSuccessUi(){
        binding.lytSuccess.visible()
        binding.lytProcessing.invisible()
    }

    fun showErrorUi(){

    }

    override fun getTheme(): Int {
        return R.style.MyCustomDialogStyleWithBlurEffect
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