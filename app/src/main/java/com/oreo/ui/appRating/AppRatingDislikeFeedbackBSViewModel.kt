package com.oreo.ui.appRating

import com.noisefit.data.base.ResourcesProvider
import com.noisefit.luna.R
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AppRatingDislikeFeedbackBSViewModel @Inject constructor(
    val resourcesProvider: ResourcesProvider,
    val sessionManager: SessionManager,
): BaseViewModel() {

    private val _uiState = MutableStateFlow(FeedbackSheetUiState(
        reasons = buildReasonUiList()
    ))
    val uiState: StateFlow<FeedbackSheetUiState> = _uiState.asStateFlow()

    fun onAction(action: FeedbackSheetAction) {
        when (action) {
            is FeedbackSheetAction.ToggleReason -> toggle(action.reason)
            is FeedbackSheetAction.DetailsChanged ->
                _uiState.update { it.copy(details = action.value) }

            FeedbackSheetAction.Submit -> Unit
            FeedbackSheetAction.Dismiss -> Unit
        }
    }

    private fun toggle(reason: FeedbackReason) {
        _uiState.update { current ->
            val newSet = current.selectedReasons.toMutableSet().apply {
                if (contains(reason)) remove(reason) else add(reason)
            }.toSet()

            // ✅ If SOMETHING_ELSE is not selected anymore, clear details
            val shouldShowDetails = newSet.contains(FeedbackReason.SOMETHING_ELSE)
            val newDetails = if (shouldShowDetails) current.details else ""

            current.copy(
                selectedReasons = newSet,
                details = newDetails
            )
        }
    }

    private fun buildReasonUiList(): List<ReasonUi> {
        return FeedbackReason.entries.map { reason ->
            ReasonUi(
                reason = reason,
                label = resourcesProvider.getString(reason.labelRes)
            )
        }
    }

    fun getReasonString(stringId: Int) = resourcesProvider.getString(stringId)

    enum class FeedbackReason(val labelRes: Int) {
        ACCURACY(R.string.text_the_information_or_data_felt_inaccurate),
        SLOW_BUGGY(R.string.text_the_app_is_clunky_and_difficult_to_use),
        HARD_TO_FIND(R.string.text_there_was_a_bug),
        SOMETHING_ELSE(R.string.text_something_else)
    }

    data class ReasonUi(val reason: FeedbackReason, val label: String)
    data class FeedbackSheetUiState(
        val reasons: List<ReasonUi> = emptyList(),
        val selectedReasons: Set<FeedbackReason> = emptySet(),
        val details: String = "",
        val isSubmitting: Boolean = false,
    ) {
        val isSomethingElseSelected: Boolean
            get() = selectedReasons.contains(FeedbackReason.SOMETHING_ELSE)

        val isSubmitEnabled: Boolean
            get() = selectedReasons.isNotEmpty() &&
                    (!isSomethingElseSelected || details.isNotBlank())
    }

    sealed interface FeedbackSheetAction {
        data class ToggleReason(val reason: FeedbackReason) : FeedbackSheetAction
        data class DetailsChanged(val value: String) : FeedbackSheetAction
        data object Submit : FeedbackSheetAction
        data object Dismiss : FeedbackSheetAction
    }

}