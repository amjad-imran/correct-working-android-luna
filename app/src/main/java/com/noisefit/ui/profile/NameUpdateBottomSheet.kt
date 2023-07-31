package com.noisefit.ui.profile

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetUpdateNameBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.showShortToast
import dagger.hilt.android.AndroidEntryPoint

const val NAME_REQUEST_KEY = "NAME_REQUEST_KEY"

@AndroidEntryPoint
class NameUpdateBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetUpdateNameBinding>(BottomSheetUpdateNameBinding::inflate) {

    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,com.noisefit_commans.R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            userName = NameUpdateBottomSheetArgs.fromBundle(it).name
        }

        if (!userName.isNullOrEmpty()) {
            binding.tvName.setText(userName)
        }
    }

    override fun initListener() {

        binding.btnSave.setOnClickListener {
            val etName = binding.tvName.text.toString().trim()
            if (etName.isEmpty()) {
                context.showShortToast(getString(R.string.text_please_enter_name_first))
                return@setOnClickListener
            }

            setFragmentResult(
                NAME_REQUEST_KEY,
                bundleOf("name" to etName)
            )

            navigateUpSafe()
        }
        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
    }


    override fun subscribeObservers() {

    }
}