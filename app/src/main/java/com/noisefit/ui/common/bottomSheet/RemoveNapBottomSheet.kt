package com.noisefit.ui.common.bottomSheet

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetAlertTextBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import dagger.hilt.android.AndroidEntryPoint

const val NAP_REQUEST_KEY = "NAP_REQUEST_KEY"

@AndroidEntryPoint
class RemoveNapBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetAlertTextBinding>(
        BottomSheetAlertTextBinding::inflate
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = getString(R.string.text_dismiss_detected_nap)
        binding.tvDesc.text =
            getString(R.string.text_nap_remove_message)

        binding.btnAllow.text = getString(R.string.text_keep)
        binding.btnCancel.text = getString(R.string.text_remove)
    }


    override fun initListener() {
        binding.btnAllow.setOnClickListener {

            navigateUpSafe()
        }
        binding.btnCancel.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                NAP_REQUEST_KEY,
                bundleOf("remove" to true)
            )
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }

}
