package com.oreo.ui.home.summary

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetAboutAutoWorkoutBinding
import com.noisefit.luna.databinding.BottomSheetAlertTextBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AboutAutoWorkoutBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetAboutAutoWorkoutBinding>(
        BottomSheetAboutAutoWorkoutBinding::inflate
    ) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.tvTitle.text = getString(R.string.text_automatic_activity_detection)
        binding.tvDesc.text = getString(R.string.text_automatic_activity_detection_desc)
        binding.btnCancel.gone()
        binding.btnAllow.text = "Got it"
    }


    override fun initListener() {
        binding.btnAllow.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }

}
