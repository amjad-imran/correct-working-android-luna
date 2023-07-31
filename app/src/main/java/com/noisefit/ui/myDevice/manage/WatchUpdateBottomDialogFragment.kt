package com.noisefit.ui.myDevice.manage

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetWatchUpdateBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.getColor
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WatchUpdateBottomDialogFragment :
    BaseBottomSheetWithTransparent<BottomSheetWatchUpdateBinding>(
        BottomSheetWatchUpdateBinding::inflate
    ) {
    companion object {
        fun getInstance(
            title: String,
            message: String,
            typeText: String
        ): WatchUpdateBottomDialogFragment {
            val data = Bundle()
            data.putString("title", title)
            data.putString("message", message)
            data.putString("typeText", typeText)
            return WatchUpdateBottomDialogFragment().apply {
                arguments = data
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytProgress.pbSteps.setIndicatorColor(
            R.color.primary_btn_selected_color.getColor()
        )
        binding.tvTitle.text = arguments?.getString("title") ?: ""
        binding.tvMessage.text = arguments?.getString("message") ?: ""
        binding.tvTypeText.text = arguments?.getString("typeText") ?: ""
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

    fun setProgress(progress: Int) {
        try {
            binding.lytProgress.pbSteps.progress = progress
            binding.tvProgress.text = "$progress%"
        } catch (exp: Exception) {
        }
    }


}