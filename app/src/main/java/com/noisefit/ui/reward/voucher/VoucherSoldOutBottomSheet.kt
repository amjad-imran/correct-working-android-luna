package com.noisefit.ui.reward.voucher

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetVoucherSoldBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

const val VOUCHER_SOLD_KEY = "VOUCHER_SOLD_KEY"

@AndroidEntryPoint
class VoucherSoldOutBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetVoucherSoldBinding>(BottomSheetVoucherSoldBinding::inflate) {

    private var title: String? = null
    private var message: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, com.noisefit_commans.R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            title = VoucherSoldOutBottomSheetArgs.fromBundle(it).title
            message = VoucherSoldOutBottomSheetArgs.fromBundle(it).message
        }

        if (!title.isNullOrEmpty()) {
            binding.tvTitle.text = title
        }
        if (!message.isNullOrEmpty()) {
            binding.tvMessage.text = message
        }
    }

    override fun initListener() {

        binding.btnSave.setOnClickListener {
            setFragmentResult(
                VOUCHER_SOLD_KEY,
                bundleOf("redirectToDeals" to true)
            )
            navigateUpSafe()
        }

    }


    override fun subscribeObservers() {

    }
}