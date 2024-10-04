package com.oreo.ui.referral.type

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.noisefit.data.model.referral.CardStyle1
import com.noisefit.data.model.referral.CardStyle2
import com.noisefit.luna.databinding.FragmentPrizeBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImageWithCache


class ReferralPrizeFragment : BaseFragment<FragmentPrizeBinding>(FragmentPrizeBinding::inflate) {

    companion object {
        fun getInstance(cardData: CardStyle1): Fragment {
            return ReferralPrizeFragment().apply {
                this.arguments = bundleOf("cardData" to cardData)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val cardData = arguments?.getParcelable<CardStyle1>("cardData")

        cardData?.let {
            initUI(it)
        }

    }

    private fun initUI(cardData: CardStyle1) {
        binding.tvTitle.text = cardData.title
        binding.textView132.text = cardData.subtitle
        binding.tvPrizeWorth.text = cardData.prize
        binding.ivProductImage.loadImageWithCache(binding.ivProductImage.context, cardData.image)
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}