package com.noisefit.ui.feeds.bottomSheet

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
import com.noisefit.R
import com.noisefit.databinding.FragmentDeletePCBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

const val DELETE_PC_REQUEST_KEY = "DELETE_PC_REQUEST_KEY"

@AndroidEntryPoint
class DeletePCBottomSheet : BaseBottomSheetWithTransparent<FragmentDeletePCBottomSheetBinding>(
    FragmentDeletePCBottomSheetBinding::inflate
) {

    private val viewModel: FeedBottomSheetViewModel by viewModels()

    private val args: DeletePCBottomSheetArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.postId = args.postId
            viewModel.commentId = args.commentId
        }


        var title = getString(R.string.text_delete_post)
        var description = getString(R.string.text_post_delete_description)

        if (viewModel.commentId != -1L) {
            title = getString(R.string.text_delete_comment)
            viewModel.isOpenFromComment = true
            description = getString(R.string.text_post_comment_description)
        }

        val finalTitle = "$title ?"
        binding.tvTitle.text = finalTitle
        binding.tvDesc.text = description

    }


    override fun initListener() {
        binding.btnAllow.setOnClickListener {
            viewModel.deletePOrCFromServer()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }


    }

    override fun subscribeObservers() {
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.serverStatus.observe(this) {
            it?.let {
                dismiss()
                if (viewModel.isOpenFromComment) {
                    setFragmentResult(
                        DELETE_PC_REQUEST_KEY,
                        bundleOf(
                            "delete" to true,
                            "commentCount" to viewModel.commentCount
                        )
                    )
                } else {
                    requireActivity().supportFragmentManager.setFragmentResult(
                        DELETE_PC_REQUEST_KEY,
                        bundleOf(
                            "delete" to true,
                            "commentCount" to viewModel.commentCount
                        )
                    )
                }
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->

            }
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
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
