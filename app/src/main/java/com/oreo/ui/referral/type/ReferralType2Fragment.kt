package com.oreo.ui.referral.type

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.noisefit.data.model.referral.CardStyle2
import com.noisefit.luna.databinding.FragmentReferralType2Binding
import com.noisefit_commans.ui.BaseFragment


class ReferralType2Fragment :
    BaseFragment<FragmentReferralType2Binding>(FragmentReferralType2Binding::inflate) {

    companion object {
        fun getInstance(cardData: CardStyle2): Fragment {
            return ReferralType2Fragment().apply {
                this.arguments = bundleOf("cardData" to cardData)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardData = arguments?.getParcelable<CardStyle2>("cardData")

        cardData?.let {
            initUI(it)
        }

    }

    private fun initUI(cardData: CardStyle2) {
        binding.tvTitle.text = cardData.title
        binding.tvSubTitle.text = cardData.subTitle
        binding.rootView.setBackgroundResource(cardData.backgroundRes)

        binding.tvTitle.setTextColor(cardData.textColor)
        binding.tvSubTitle.setTextColor(cardData.textColor)
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}