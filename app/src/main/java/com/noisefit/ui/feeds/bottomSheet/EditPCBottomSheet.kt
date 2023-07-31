package com.noisefit.ui.feeds.bottomSheet

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentEditPCBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import dagger.hilt.android.AndroidEntryPoint

const val EDIT_PC_REQUEST_KEY = "EDIT_PC_REQUEST_KEY"

@AndroidEntryPoint
class EditPCBottomSheet : BaseBottomSheetWithTransparent<FragmentEditPCBottomSheetBinding>(
    FragmentEditPCBottomSheetBinding::inflate
) {

    private val viewModel: FeedBottomSheetViewModel by viewModels()
    private val args: EditPCBottomSheetArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.postId = args.postId
            viewModel.commentId = args.commentId
        }

        if (viewModel.commentId != -1L) {
//            binding.tvEditPost.text = getString(R.string.text_edit_comment)
            binding.editContainer.gone()
            viewModel.isOpenFromComment = true
            binding.tvDeletePost.text = getString(R.string.text_delete_comment)
        }
    }

    override fun initListener() {
        binding.deleteContainer.setOnClickListener {
            dismiss()
            if (viewModel.isOpenFromComment) {
                setFragmentResult(
                    EDIT_PC_REQUEST_KEY,
                    bundleOf(
                        "delete" to true,
                        "postId" to viewModel.postId,
                        "commentId" to viewModel.commentId
                    )
                )
            } else {
                requireActivity().supportFragmentManager.setFragmentResult(
                    EDIT_PC_REQUEST_KEY,
                    bundleOf(
                        "delete" to true,
                        "postId" to viewModel.postId,
                        "commentId" to viewModel.commentId
                    )
                )
            }

        }

        binding.editContainer.setOnClickListener {
            dismiss()
            if (viewModel.isOpenFromComment) {
                setFragmentResult(
                    EDIT_PC_REQUEST_KEY,
                    bundleOf(
                        "edit" to true,
                        "postId" to viewModel.postId,
                        "commentId" to viewModel.commentId
                    )
                )
            } else {
                requireActivity().supportFragmentManager.setFragmentResult(
                    EDIT_PC_REQUEST_KEY,
                    bundleOf(
                        "edit" to true,
                        "postId" to viewModel.postId,
                        "commentId" to viewModel.commentId
                    )
                )
            }

        }
    }

    override fun subscribeObservers() {


    }

}