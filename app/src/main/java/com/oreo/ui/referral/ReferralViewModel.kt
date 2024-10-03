package com.oreo.ui.referral

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.referral.CardStyle1
import com.noisefit.data.model.referral.CardStyle2
import com.noisefit.data.model.referral.ReferralCodeResponse
import com.noisefit.data.model.referral.ReferralInfoResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.ReferralRepository
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.ui.referral.type.ReferralPrizeFragment
import com.oreo.ui.referral.type.ReferralType2Fragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReferralViewModel @Inject constructor(
    private val referralRepository: ReferralRepository,
    private val localDataStore: DataStoredInterface
) : BaseViewModel() {

    var referralInfo = MutableLiveData<ReferralInfoResponse>()
    val referralCode = MutableLiveData<ReferralCodeResponse>(null)

    fun getReferCode() {
        viewModelScope.launch {
            referralRepository.getReferCode().collect { resource ->
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
                                        getReferCode()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data.let {
                            referralCode.postValue(it)
                        }
                    }
                }
            }
        }

    }

    fun getCards(referralInfoResponse: ReferralInfoResponse): List<Fragment> {
        val cards = ArrayList<Fragment>()

        referralInfoResponse.banner?.forEach {
            if (it.type.equals("prize", true)) {
                cards.add(
                    ReferralPrizeFragment.getInstance(
                        CardStyle1(
                            title = it.title,
                            subtitle = it.subTitle,
                            prize = it.prize,
                            image = it.imageUrl
                        )
                    )
                )
            } else if (it.type.equals("friend", true)) {
                cards.add(
                    ReferralType2Fragment.getInstance(
                        CardStyle2(
                            title = it.title,
                            subTitle = it.subTitle,
                            backgroundRes = R.drawable.bg_ref_2
                        )
                    )
                )
            } else if (it.type.equals("process", true)) {
                cards.add(
                    ReferralType2Fragment.getInstance(
                        CardStyle2(
                            title = it.title,
                            subTitle = it.subTitle,
                            backgroundRes = R.drawable.bg_ref_3
                        )
                    )
                )
            } else if (it.type.equals("prize_time", true)) {
                cards.add(
                    ReferralType2Fragment.getInstance(
                        CardStyle2(
                            title = it.title,
                            subTitle = it.subTitle,
                            backgroundRes = R.drawable.bg_ref_4
                        )
                    )
                )
            }
        }
        return cards
    }

    fun getUserName(): String {
        return localDataStore.getUser()?.firstName ?: ""
    }

}