package com.noisefit.ui.myDevice

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.databinding.FragmentUnpairNoDeviceDialogBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class UnpairDeviceNotConnectedFragment :
    BaseBottomSheetWithTransparent<FragmentUnpairNoDeviceDialogBinding>(
        FragmentUnpairNoDeviceDialogBinding::inflate
    ) {

    val args: UnpairDeviceNotConnectedFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = args.title
        binding.tvPrivacy.text = args.message

        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}