package com.noisefit.ui.common

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.databinding.FragmentUnpairBottomDialogBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

const val CONFIRM_DIALOG_REQUEST_KEY = "CONFIRM_DIALOG_REQUEST_KEY"

@AndroidEntryPoint
class ConfirmBottomDialogFragment :
    BaseBottomSheetWithTransparent<FragmentUnpairBottomDialogBinding>(
        FragmentUnpairBottomDialogBinding::inflate
    ) {

    val args: ConfirmBottomDialogFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.tvTitle.text = args.title
        binding.tvPrivacy.text = args.message

        binding.btnAllow.setOnClickListener {
            navigateUpSafe()
            setFragmentResult(
                CONFIRM_DIALOG_REQUEST_KEY,
                bundleOf("allowClicked" to true)
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