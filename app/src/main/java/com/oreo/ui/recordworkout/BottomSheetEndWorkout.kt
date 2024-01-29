package com.oreo.ui.recordworkout

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetAlertTextBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

const val END_WORKOUT_KEY = "END_WORKOUT_KEY"

@AndroidEntryPoint
class BottomSheetEndWorkout :
    BaseBottomSheetWithTransparent<BottomSheetAlertTextBinding>(
        BottomSheetAlertTextBinding::inflate
    ) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = getString(R.string.text_end_workout)
        binding.tvDesc.text = getString(R.string.text_ready_to_end_your_workout)
        binding.btnAllow.text = getString(R.string.text_save)
        binding.btnCancel.text = getString(R.string.text_delete)

    }

    override fun initListener() {
        binding.btnAllow.setOnClickListener {
            setFragmentResult(
                END_WORKOUT_KEY,
                bundleOf("allow" to true)
            )
            navigateUpSafe()
        }
        binding.btnCancel.setOnClickListener {
            setFragmentResult(
                END_WORKOUT_KEY,
                bundleOf("delete" to true)
            )
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }

}
