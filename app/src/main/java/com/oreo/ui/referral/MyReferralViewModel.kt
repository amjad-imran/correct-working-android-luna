package com.oreo.ui.referral

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.ReferralRepository
import com.noisefit.ui.profile.ReferralRunningState
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.referral.Referral
import com.oreo.data.model.referral.ReferralsMain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyReferralViewModel @Inject constructor(
    private val referralRepository: ReferralRepository
) : BaseViewModel() {

    val myReferrals = MutableLiveData<List<ReferralsMain>>()


    init {
       /* myReferrals.postValue(
            arrayListOf(
                ReferralsMain(
                    referralName = "Test referral",
                    pendingMessage = "2 friends left to receive their ring",
                    referrals = arrayListOf(
                        Referral(
                            name = "Test name",
                            status = "purchased"
                        ), Referral(
                            name = "Test name 2",
                            status = "delivered"
                        )
                    )
                )
            )
        )*/
    }

    fun getMyReferralHistory() {
        viewModelScope.launch {
            referralRepository.getReferralHistory().collect { resource ->
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
                                object :
                                    BinaryActionCallback {
                                    override fun yes() {
                                        getMyReferralHistory()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            myReferrals.postValue(it)
                        }
                    }
                }
            }
        }
    }

}