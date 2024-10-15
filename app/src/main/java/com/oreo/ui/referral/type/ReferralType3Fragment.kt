package com.oreo.ui.referral.type

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.noisefit.data.model.referral.CardStyle2
import com.noisefit.data.model.referral.CardStyle3
import com.noisefit.luna.databinding.FragmentReferralType2Binding
import com.noisefit.luna.databinding.FragmentReferralType3Binding
import com.noisefit_commans.ui.BaseFragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class ReferralType3Fragment :
    BaseFragment<FragmentReferralType3Binding>(FragmentReferralType3Binding::inflate) {

    companion object {
        fun getInstance(cardData: CardStyle3): Fragment {
            return ReferralType3Fragment().apply {
                this.arguments = bundleOf("cardData" to cardData)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardData = arguments?.getParcelable<CardStyle3>("cardData")

        cardData?.let {
            initUI(it)
        }

    }

    private fun initUI(cardData: CardStyle3) {
        binding.rootView.setBackgroundResource(cardData.backgroundRes)
        binding.tvName.text = cardData.name

        if (cardData.status.equals("purchased",true)) {
            binding.ivPurchased.setImageResource(cardData.selectedRingRes)
            binding.ivDelivered.setImageResource(cardData.defaultRing)
            binding.textPurchased.alpha = 1f
            binding.textDelivered.alpha = 0.5f
        } else if (cardData.status.equals("delivered",true)) {
            binding.ivPurchased.setImageResource(cardData.selectedRingRes)
            binding.ivDelivered.setImageResource(cardData.selectedRingRes)
            binding.textPurchased.alpha = 1f
            binding.textDelivered.alpha = 1f
        } else {
            binding.ivPurchased.setImageResource(cardData.defaultRing)
            binding.ivDelivered.setImageResource(cardData.defaultRing)
            binding.textPurchased.alpha = 0.5f
            binding.textDelivered.alpha = 0.5f
        }
        binding.ivConnector.setImageResource(cardData.ringConnectRes)

        binding.tvDate.setTextColor(cardData.textColor)
        binding.tvName.setTextColor(cardData.textColor)
        binding.textPurchased.setTextColor(cardData.textColor)
        binding.textDelivered.setTextColor(cardData.textColor)

        binding.tvDate.text =
            LocalDate.parse(cardData.date).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}