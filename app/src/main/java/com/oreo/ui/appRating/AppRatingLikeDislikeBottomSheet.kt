package com.oreo.ui.appRating

import android.app.Dialog
import android.os.Bundle
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAppRatingLikeDislikeBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.utils.LOGS
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppRatingLikeDislikeBottomSheet :
    BaseBottomSheetWithTransparent<FragmentAppRatingLikeDislikeBottomSheetBinding>(
        FragmentAppRatingLikeDislikeBottomSheetBinding::inflate
    ) {

        companion object{
            const val APP_RATING_LIKE_DISLIKE_KEY = "APP_RATING_LIKE_DISLIKE_KEY"
        }

    override fun initListener() {
        binding.ivThumbsUp.setOnClickListener {
            binding.ivThumbsUp.setImageResource(R.drawable.ic_thumbs_up_pressed_app_rating)
            handleLikeDislikeClicked(true)
        }

        binding.ivThumbsDown.setOnClickListener {
            binding.ivThumbsDown.setImageResource(R.drawable.ic_thumbs_down_pressed_app_rating)
            handleLikeDislikeClicked(false)
        }
    }

    private fun handleLikeDislikeClicked(isLiked: Boolean){
        binding.ivThumbsDown.isClickable = false
        binding.ivThumbsUp.isClickable = false
        lifecycleScope.launch {
            delay(1000L)
            navigateUpSafe()
            setFragmentResult(
                APP_RATING_LIKE_DISLIKE_KEY,
                Bundle().apply {
                    this.putBoolean("isLikedClicked", isLiked)
                }
            )
            LOGS.d("asclkasca vioasklvn: $isLiked")
        }
    }

    override fun subscribeObservers() {

    }

    override fun getTheme(): Int {
        return R.style.MyCustomDialogStyleWithBlurEffect
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = true
                isCancelable = true
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }

}