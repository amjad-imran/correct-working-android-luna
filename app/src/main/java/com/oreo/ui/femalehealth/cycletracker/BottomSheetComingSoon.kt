package com.oreo.ui.femalehealth.cycletracker

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.BottomSheetComingSoonBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

const val NOTIFY_LOG = "NOTIFY_LOG"

class BottomSheetComingSoon : BaseBottomSheetWithTransparent<BottomSheetComingSoonBinding>(
    BottomSheetComingSoonBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }


    override fun initListener() {
        binding.btnNotifyMe.setOnClickListener {
            setFragmentResult(
                NOTIFY_LOG,
                bundleOf("agree" to true)
            )
        }

    }

    override fun subscribeObservers() {

    }
}