package com.oreo.ui.chatGpt.functions

import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.BaseViewModelCompose
import com.noisefit_commans.utils.LOGS
import com.oreo.data.repository.abstraction.OreoDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutPlanViewModel @Inject constructor(
    val oreoDeviceRepository: OreoDeviceRepository
) : BaseViewModelCompose() {
    private val _showEditScreen = MutableStateFlow(false)
    val showEditScreen: StateFlow<Boolean> = _showEditScreen

    fun showEditScreen(boolean: Boolean) {
        _showEditScreen.value = boolean
    }

    fun getWorkoutPlans() {
        viewModelScope.launch {
            oreoDeviceRepository.getAiWorkoutPlans().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getWorkoutPlans()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                        }
                    }
                }
            }
        }
    }
}