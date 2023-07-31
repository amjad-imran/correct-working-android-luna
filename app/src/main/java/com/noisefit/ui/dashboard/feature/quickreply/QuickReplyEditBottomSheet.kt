package com.noisefit.ui.dashboard.feature.quickreply

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentQuickReplyEditBinding
import com.noisefit_commans.ui.BaseBottomSheet
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.models.CustomReplyData


class QuickReplyEditBottomSheet :
    BaseBottomSheet<FragmentQuickReplyEditBinding>(FragmentQuickReplyEditBinding::inflate) {


    private var reply: CustomReplyData.CustomReply? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,com.noisefit_commans.R.style.DialogStyle)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListener()

        arguments?.let {
            reply = QuickReplyEditBottomSheetArgs.fromBundle(it).reply
            binding.etReply.setText(reply?.content)
        }

        if (reply == null) {
            binding.textView19.text = getString(R.string.text_add_quick_reply)
        } else {
            binding.textView19.text = getString(R.string.text_edit_quick_reply)
        }

    }

    fun initListener() {

        binding.btnContinue.setOnClickListener {

            val enteredText = binding.etReply.text.toString().trim()
            if (enteredText.isEmpty()) {
                context.showShortToast(getString(R.string.text_enter_some_value))
                return@setOnClickListener
            }

            if (reply == null) {
                reply = CustomReplyData.CustomReply()
            }
            reply!!.content = enteredText

            setFragmentResult(
                QUICK_REPLY_KEY,
                bundleOf("reply" to reply)
            )
            navigateUpSafe()

        }
        binding.btnCancel.setOnClickListener {
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