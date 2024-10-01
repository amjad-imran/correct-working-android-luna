package com.oreo.ui.referral

import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.referral.Referral
import com.oreo.data.model.referral.ReferralsMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyReferralViewModel @Inject constructor() : BaseViewModel() {

    val myReferrals = MutableLiveData<List<ReferralsMain>>()


    init {
        myReferrals.postValue(
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
        )
    }

}