package com.oreo.ui.device.surgeCase

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentItemAboutChargerBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.surgeCase.AboutSurgeCaseModel

class ItemAboutChargerFragment : BaseFragment<FragmentItemAboutChargerBinding>(FragmentItemAboutChargerBinding::inflate) {

    var cardItem: AboutSurgeCaseModel ?= null

    companion object {
        fun newInstance(item: AboutSurgeCaseModel): ItemAboutChargerFragment {
            val fragment = ItemAboutChargerFragment()
            fragment.cardItem = item
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardItem?.let {
            binding.tvTitle.text = it.title
            binding.tvDesc.text = it.description
            it.imageClosedCharger?.let {
                binding.ivImg.gone()
                binding.ivTopStickedImg.gone()
                binding.ivChargerClosedImg.setImageResource(it)
                binding.ivChargerClosedImg.visible()
            }
            it.imageCenter?.let {
                binding.ivChargerClosedImg.gone()
                binding.ivTopStickedImg.gone()
                binding.ivImg.setImageResource(it)
                binding.ivImg.visible()
            }
            it.imageTopSticked?.let {
                binding.ivChargerClosedImg.gone()
                binding.ivImg.gone()
                binding.ivTopStickedImg.setImageResource(it)
                binding.ivTopStickedImg.visible()
            }
        }
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}