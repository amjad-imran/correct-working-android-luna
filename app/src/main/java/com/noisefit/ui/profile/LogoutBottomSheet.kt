package com.noisefit.ui.profile

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.BottomSheetLogoutBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint


const val LOGOUT_KEY = "LOGOUT_KEY"

@AndroidEntryPoint
class LogoutBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetLogoutBinding>(BottomSheetLogoutBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    override fun initListener() {
        binding.btnCancel.setOnClickListener {
            setFragmentResult(
                LOGOUT_KEY,
                bundleOf("isSelected" to false)
            )
            dismiss()
        }
        binding.btnAllow.setOnClickListener {
            setFragmentResult(
                LOGOUT_KEY,
                bundleOf("isSelected" to true)
            )
            dismiss()
        }
    }

    override fun subscribeObservers() {

    }
}