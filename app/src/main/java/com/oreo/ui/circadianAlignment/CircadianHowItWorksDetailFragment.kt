package com.oreo.ui.circadianAlignment

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCircadianHowItWorksDetailBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.html
import com.noisefit_commans.utils.LOGS

class CircadianHowItWorksDetailFragment :
    BaseFragment<FragmentCircadianHowItWorksDetailBinding>(FragmentCircadianHowItWorksDetailBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getInt("position")?.let {
            setUi(it)
        }
    }

    private fun setUi(pos: Int) {
        LOGS.d("abhcjABc : $pos")
        when(pos){
            0 -> {
                binding.lytToolbar.tvTitle.text = getString(R.string.text_circadian_alignment)
                binding.tvIntro.text = getString(R.string.text_circadian_how_it_works_intro_1)
                binding.tvDesc.text = getString(R.string.text_circadian_how_it_works_desc_1).html()
            }

            1 -> {
                binding.lytToolbar.tvTitle.text = getString(R.string.text_daily_rhythm_guide)
                binding.tvIntro.text = getString(R.string.text_circadian_how_it_works_intro_2)
                binding.tvDesc.text = getString(R.string.text_circadian_how_it_works_desc_2).html()
            }

            2 -> {
                binding.lytToolbar.tvTitle.text = getString(R.string.text_the_timeline)
                binding.tvIntro.text = getString(R.string.text_circadian_how_it_works_intro_3)
                binding.tvDesc.text = getString(R.string.text_circadian_how_it_works_desc_3).html()
            }

            3 -> {
                binding.lytToolbar.tvTitle.text = getString(R.string.text_circadian_mid_point)
                binding.tvIntro.text = getString(R.string.text_circadian_how_it_works_intro_4)
                binding.tvDesc.text = getString(R.string.text_circadian_how_it_works_desc_4).html()
            }
        }
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}