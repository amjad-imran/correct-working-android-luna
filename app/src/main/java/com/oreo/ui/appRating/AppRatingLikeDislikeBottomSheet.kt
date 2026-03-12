package com.oreo.ui.appRating

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAppRatingLikeDislikeBottomSheetBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppRatingLikeDislikeBottomSheet :
    BaseBottomSheetWithTransparent<FragmentAppRatingLikeDislikeBottomSheetBinding>(
        FragmentAppRatingLikeDislikeBottomSheetBinding::inflate
    ) {

    companion object {
        const val APP_RATING_LIKE_DISLIKE_KEY = "APP_RATING_LIKE_DISLIKE_KEY"
    }

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var localDataStore: DataStoredInterface

    override fun onStart() {
        super.onStart()
        localDataStore.setAndGetLastAppReviewRequestTime(System.currentTimeMillis())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi()
    }

    private fun setUi() {
        val isLikeFlow = arguments?.getBoolean("isLikeFlow") ?: true
        if (isLikeFlow) {
            binding.tvStatusText.text = getString(R.string.text_glad_you_like)
            binding.llSuccessContainer.invisible()
            binding.groupRating.visible()
        } else {
            binding.tvStatusText.text =
                getString(R.string.text_thanks_for_sharing_nwe_are_working_to_make_this_better)
            binding.groupRating.invisible()
            binding.llSuccessContainer.visible()
            lifecycleScope.launch {
                delay(1500L)
                navigateUpSafe()
            }
        }
    }

    override fun initListener() {
        binding.ivThumbsUp.setOnClickListener {
            sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.app_rating_modal_positive)
            binding.ivThumbsUp.setImageResource(R.drawable.ic_thumbs_up_pressed_app_rating)
            handleLikeDislikeClicked(true)
        }

        binding.ivThumbsDown.setOnClickListener {
            sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.app_rating_modal_negative)
            binding.ivThumbsDown.setImageResource(R.drawable.ic_thumbs_down_pressed_app_rating)
            handleLikeDislikeClicked(false)
        }
    }

    private fun handleLikeDislikeClicked(isLiked: Boolean) {
        binding.ivThumbsDown.isClickable = false
        binding.ivThumbsUp.isClickable = false
        lifecycleScope.launch {
            if (isLiked) {
                showSuccessState()
            } else {
                delay(500L)
            }
            navigateUpSafe()
            setFragmentResult(
                APP_RATING_LIKE_DISLIKE_KEY,
                Bundle().apply {
                    this.putBoolean("isLikedClicked", isLiked)
                }
            )
        }
    }

    suspend fun showSuccessState() {
        val ratingViews = listOf(binding.tvTitle, binding.ivThumbsUp, binding.ivThumbsDown)

        ratingViews.forEach { view ->
            view.animate()
                .alpha(0f)
                .setDuration(1000)
                .withEndAction {
                    view.invisible()
                }
                .start()
        }

        delay(1000L)

        binding.llSuccessContainer.apply {
            alpha = 0f
            visible()

            animate()
                .alpha(1f)
                .setDuration(1000)
                .start()
        }

        delay(1500L)
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