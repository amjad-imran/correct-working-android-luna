package com.oreo.ui.chatGpt.functions

import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class WorkoutPlanViewModel @Inject constructor():BaseViewModel() {
    private val _showEditScreen = MutableStateFlow(false)
    val showEditScreen: StateFlow<Boolean> = _showEditScreen

    fun showEditScreen(boolean: Boolean) {
        _showEditScreen.value = boolean
    }
}