package com.oreo.ui.appRating

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAppRatingDislikeFeedbackBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppRatingDislikeFeedbackBottomSheet :
    BaseBottomSheetWithTransparent<FragmentAppRatingDislikeFeedbackBottomSheetBinding>(
        FragmentAppRatingDislikeFeedbackBottomSheetBinding::inflate
    ) {

    private val viewModel: AppRatingDislikeFeedbackBSViewModel by viewModels()
    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

    companion object {
        const val APP_RATING_DISLIKE_FEEDBACK_KEY = "APP_RATING_DISLIKE_FEEDBACK_KEY"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.setViewCompositionStrategy(
            androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        setUi()
    }

    private fun setUi() {
        binding.composeView.setContent {
            val uiState by viewModel.uiState.collectAsState() // no extra dependency

            FeedbackSheetContent(
                uiState = uiState,
                onAction = viewModel::onAction,
                onSubmitBtnClicked = { handleSubmit() },
                modifier = Modifier,
                submitBtnText = getString(R.string.text_submit)
            )
        }
    }

    private fun handleSubmit() {
        val state = viewModel.uiState.value
        if (!state.isSubmitEnabled || state.isSubmitting) return

        val selected = state.selectedReasons.map { viewModel.getReasonString(it.labelRes) }
        val feedbackField = state.details.trim()

        setFragmentResult(
            APP_RATING_DISLIKE_FEEDBACK_KEY, // use your key (you had mismatched key in sample)
            bundleOf(
                "selectedReasons" to ArrayList(selected),
                "feedbackField" to feedbackField,
            )
        )
        navigateUpSafe()
    }

    override fun getTheme(): Int = R.style.MyCustomDialogStyleWithBlurEffect

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = true
                // NOTE: isCancelable is a DialogFragment property; but leaving your pattern intact
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }

}