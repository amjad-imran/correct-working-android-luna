package com.oreo.ui.referral.type

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.noisefit.data.model.referral.CardStyle1
import com.noisefit.luna.databinding.FragmentPrizeBinding
import com.noisefit_commans.ui.BaseFragment


class ReferralPrizeFragment : BaseFragment<FragmentPrizeBinding>(FragmentPrizeBinding::inflate) {

    companion object {
        fun getInstance(cardData: CardStyle1): Fragment {
            return ReferralPrizeFragment().apply {
                this.arguments = bundleOf("cardData" to cardData)
            }
        }
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}