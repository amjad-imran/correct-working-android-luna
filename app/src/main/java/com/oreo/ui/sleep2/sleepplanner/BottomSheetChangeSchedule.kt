package com.oreo.ui.sleep2.sleepplanner

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.BottomSheetChangeScheduleBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent

const val CHANGE_SCHEDULE = "CHANGE_SCHEDULE"

class BottomSheetChangeSchedule : BaseBottomSheetWithTransparent<BottomSheetChangeScheduleBinding>(
    BottomSheetChangeScheduleBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    override fun initListener() {

        binding.btnCancel.setOnClickListener {
            setFragmentResult(
                CHANGE_SCHEDULE,
                bundleOf("change" to false)
            )
            navigateUpSafe()
        }

        binding.btnSelect.setOnClickListener {
            setFragmentResult(
                CHANGE_SCHEDULE,
                bundleOf("change" to true)
            )
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}