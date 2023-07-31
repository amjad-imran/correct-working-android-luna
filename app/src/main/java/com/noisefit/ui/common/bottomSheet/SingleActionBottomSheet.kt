package com.noisefit.ui.common.bottomSheet

import android.os.Bundle
import android.view.View
import com.noisefit.R
import com.noisefit.databinding.BottomSheetSingleActionBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingleActionBottomSheet : BaseBottomSheetWithTransparent<BottomSheetSingleActionBinding>(
    BottomSheetSingleActionBinding::inflate
) {


    private var title: String = ""
    private var description: String = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            title = SingleActionBottomSheetArgs.fromBundle(it).title
            description = SingleActionBottomSheetArgs.fromBundle(it).description
        }

        binding.tvTitle.text = title
        binding.tvDesc.text = description
        binding.btnAllow.text = getText(R.string.text_done)
    }


    override fun initListener() {
        binding.btnAllow.setOnClickListener {
            navigateUpSafe()
        }


    }

    override fun subscribeObservers() {

    }

}
