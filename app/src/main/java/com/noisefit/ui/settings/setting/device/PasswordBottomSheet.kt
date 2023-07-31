package com.noisefit.ui.settings.setting.device

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentPasswordBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.showShortToast
import dagger.hilt.android.AndroidEntryPoint

const val PASSWORD_REQUEST_KEY = "PASSWORD_REQUEST_KEY"

@AndroidEntryPoint
class PasswordBottomSheet :
    BaseBottomSheetWithTransparent<FragmentPasswordBottomSheetBinding>(
        FragmentPasswordBottomSheetBinding::inflate
    ) {

    private var password: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        arguments?.let {
            password = PasswordBottomSheetArgs.fromBundle(it).password
        }

        if (password.isNotEmpty()) {
            binding.etPassword.setText(password)
            binding.etConfirmPassword.setText(password)
        }
    }

    override fun initListener() {
        binding.btnCancel.setOnClickListener {
            setFragmentResult(
                PASSWORD_REQUEST_KEY,
                bundleOf("cancel" to true)
            )
            navigateUpSafe()
        }
        binding.btnAllow.setOnClickListener {
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (password.isEmpty()) {
                context.showShortToast(getString(R.string.text_password_required))
                return@setOnClickListener
            }

            if (password.length < 4) {
                context.showShortToast(getString(R.string.text_password_length_required))
                return@setOnClickListener
            }
            if (confirmPassword.isEmpty()) {
                context.showShortToast(getString(R.string.text_confirm_password_required))
                return@setOnClickListener
            }

            if (!confirmPassword.equals(password, false)) {
                context.showShortToast(getString(R.string.text_password_mismatch))
                return@setOnClickListener
            }

            setFragmentResult(
                PASSWORD_REQUEST_KEY,
                bundleOf("password" to password)
            )
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }


}