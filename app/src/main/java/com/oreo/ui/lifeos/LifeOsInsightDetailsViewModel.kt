package com.oreo.ui.lifeos

import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel
import com.oreo.ui.lifeos.charts.InsightCardUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class LifeOsInsightDetailsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    val sessionManager: SessionManager,
): BaseViewModel() {


    var insightData: InsightCardUiModel?= null

    /*fun submitDislikeBtmShtData(
        feedbackText: String,
        reasons: ArrayList<String>?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            onSuccess()
            return@launch

            val reqObj = JsonObject().apply {
                this.addProperty("feedbackText", feedbackText)
                if(reasons.isNullOrEmpty().not()){
                    val jsonArray = JsonArray()
                    reasons.forEach { jsonArray.add(it) }
                    this.add("reasons", jsonArray)
                }
            }
            userRepository.submitInsightDislikeFeedbackData(reqObj).collect{ resource ->
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
                                        submitDislikeBtmShtData(feedbackText, reasons, onSuccess)
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            onSuccess()
                        }
                    }
                }
            }
        }
    }*/

}