package com.oreo.ui.circadianAlignment

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCircadianHowItWorksDetailBinding
import com.noisefit_commans.ui.BaseFragment

class CircadianHowItWorksDetailFragment :
    BaseFragment<FragmentCircadianHowItWorksDetailBinding>(FragmentCircadianHowItWorksDetailBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getInt("position")?.let {
            setUi(it)
        }
    }

    private fun setUi(pos: Int) {
        when(pos){
            0 -> {
                binding.lytToolbar.tvTitle.text = getString(R.string.text_circadian_alignment)
                binding.tvTitle.text = getString(R.string.text_circadian_how_it_works_intro_1)
                binding.tvDesc.text = ""
            }

            1 -> {
                binding.lytToolbar.tvTitle.text = ""
                binding.tvTitle.text = ""
                binding.tvDesc.text = ""
            }

            2 -> {
                binding.lytToolbar.tvTitle.text = ""
                binding.tvTitle.text = ""
                binding.tvDesc.text = ""
            }

            3 -> {
                binding.lytToolbar.tvTitle.text = ""
                binding.tvTitle.text = ""
                binding.tvDesc.text = ""
            }
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}