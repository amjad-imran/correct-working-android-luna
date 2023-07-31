package com.noisefit.ui.npl.quiz

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.databinding.BottomSheetQuitQuizBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseBottomSheet
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val QUIT_KEY = "QUIT_KEY"

@AndroidEntryPoint
class NplQQuitBottomSheet : BaseBottomSheet<BottomSheetQuitQuizBinding>(
    BottomSheetQuitQuizBinding::inflate
) {
    @Inject
    lateinit var sessionManager: SessionManager
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()

    }

    private fun initListener() {
        binding.btnCancel.setOnClickListener {
            sessionManager.logInsiderAppEvent(InsiderAppEvents.NPL_QUIZ_CLOSER_CANCEL_CLICK)
            navigateUpSafe()
        }
        binding.btnQuit.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                QUIT_KEY,
                bundleOf("isSelected" to true)
            )
            navigateUpSafe()
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
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog

    }
}