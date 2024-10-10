package com.oreo.ui.recordworkout

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetAlertTextBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

const val BOTTOM_SHEET_WORKOUT_IN_PROGRESS = "BOTTOM_SHEET_WORKOUT_IN_PROGRESS"

@AndroidEntryPoint
class BottomSheetWorkoutInProgress :
    BaseBottomSheetWithTransparent<BottomSheetAlertTextBinding>(
        BottomSheetAlertTextBinding::inflate
    ) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = getString(R.string.text_ongoing_workout_detected)
        binding.tvDesc.text = getString(R.string.text_ongoing_message)
        binding.btnAllow.text = getString(R.string.text_end_start_new)
        binding.btnCancel.text = getString(R.string.text_end_workout)

    }

    override fun initListener() {
        binding.btnAllow.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                BOTTOM_SHEET_WORKOUT_IN_PROGRESS,
                bundleOf("allow" to true)
            )
            navigateUpSafe()
        }
        binding.btnCancel.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                BOTTOM_SHEET_WORKOUT_IN_PROGRESS,
                bundleOf("delete" to true)
            )
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }

}
