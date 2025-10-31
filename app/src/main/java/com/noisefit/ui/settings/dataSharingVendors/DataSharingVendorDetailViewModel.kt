package com.noisefit.ui.settings.dataSharingVendors

import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.dataSharingVendorModels.DataSharingVendorListResponseItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataSharingVendorDetailViewModel
@Inject constructor(
    val userRepository: UserRepository,
): BaseViewModel() {

    var mainData: DataSharingVendorListResponseItem ?= null

    fun submitDataSharingVendorToggleState(toggleState: Boolean,updateFeaturesListToggle: ()-> Unit){
        viewModelScope.launch {
            val reqObject = JsonObject().apply {
                this.addProperty("vendorId", mainData?.vendorId)
                this.addProperty("consent", toggleState)
            }
            userRepository.submitDataSharingVendorToggleState(reqObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        submitDataSharingVendorToggleState(toggleState, updateFeaturesListToggle)
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            updateFeaturesListToggle()
                        }
                    }
                }
            }
        }
    }

}