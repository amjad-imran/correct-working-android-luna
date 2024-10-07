package com.oreo.ui.referral

import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.model.referral.CardStyle1
import com.noisefit.data.model.referral.CardStyle2
import com.noisefit.data.model.referral.CardStyle3
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
import com.oreo.ui.referral.type.ReferralType3Fragment
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

        var referredCount = 0

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
                            backgroundRes = R.drawable.bg_ref_2,
                            textColor = Color.parseColor("#f6ed89")
                        )
                    )
                )
            } else if (it.type.equals("process", true)) {
                cards.add(
                    ReferralType2Fragment.getInstance(
                        CardStyle2(
                            title = it.title,
                            subTitle = it.subTitle,
                            backgroundRes = R.drawable.bg_ref_3,
                            textColor = Color.parseColor("#ffffff")
                        )
                    )
                )
            } else if (it.type.equals("prize_time", true)) {
                cards.add(
                    ReferralType2Fragment.getInstance(
                        CardStyle2(
                            title = it.title,
                            subTitle = it.subTitle,
                            backgroundRes = R.drawable.bg_ref_4,
                            textColor = Color.parseColor("#bcf04c")
                        )
                    )
                )
            } else if (it.type.equals("share", true)) {
                cards.add(
                    ReferralType2Fragment.getInstance(
                        CardStyle2(
                            title = it.title,
                            subTitle = it.subTitle,
                            backgroundRes = R.drawable.bg_ref_6,
                            textColor = Color.parseColor("#000000")
                        )
                    )
                )
            } else if (it.type.equals("referred", true)) {
                val drawables = getFriendsCard(referredCount)
                val rings = getRadioButtons(referredCount)
                referredCount += 1

                cards.add(
                    ReferralType3Fragment.getInstance(
                        CardStyle3(
                            name = it.name,
                            date = it.date,
                            status = it.status,
                            textColor = drawables.second,
                            selectedRingRes = rings.first,
                            defaultRing = rings.second,
                            backgroundRes = drawables.first,
                            ringConnectRes = rings.third
                        )
                    )
                )
            }
        }
        return cards
    }

    private fun getFriendsCard(referredCount: Int): Pair<Int, Int> {
        val modVal = referredCount % 4
        return when (modVal) {
            0 -> Pair(R.drawable.bg_ref_2, Color.parseColor("#f6ed89"))
            1 -> Pair(R.drawable.bg_ref_3, Color.parseColor("#dffc79"))
            2 -> Pair(R.drawable.bg_ref_4, Color.parseColor("#bcf04c"))
            3 -> Pair(R.drawable.bg_ref_5, Color.parseColor("#f2efe7"))
            else -> Pair(R.drawable.bg_ref_2, Color.parseColor("#f6ed89"))
        }
    }

    /**
     * Ring filled, default, join drawable
     */
    private fun getRadioButtons(referredCount: Int): Triple<Int, Int, Int> {
        val modVal = referredCount % 4
        return when (modVal) {
            0 -> Triple(
                R.drawable.ic_ref_status_filled_2,
                R.drawable.ic_ref_status_ring_2,
                R.drawable.ic_ring_join_2
            )

            1 -> Triple(
                R.drawable.ic_ref_status_filled_2,
                R.drawable.ic_ref_status_ring_2,
                R.drawable.ic_ring_join_2
            )

            2 -> Triple(
                R.drawable.ic_ref_status_filled_3,
                R.drawable.ic_ref_status_ring_3,
                R.drawable.ic_ring_join_3
            )

            3 -> Triple(
                R.drawable.ic_ref_status_filled,
                R.drawable.ic_ref_status_ring,
                R.drawable.ic_ring_join_white
            )

            else -> Triple(
                R.drawable.ic_ref_status_filled,
                R.drawable.ic_ref_status_ring,
                R.drawable.ic_ring_join_white
            )
        }
    }

    fun getUserName(): String {
        return localDataStore.getUser()?.firstName ?: ""
    }

}