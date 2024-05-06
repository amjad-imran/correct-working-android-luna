package com.oreo.ui.femalehealth.cycletracker

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFMHSkinTemperatureBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FMHSkinTemperatureFragment :
    BaseFragment<FragmentFMHSkinTemperatureBinding>(FragmentFMHSkinTemperatureBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun initListener() {
        updateUI()
        binding.lytToolbar.tvTitle.text = getString(R.string.text_skin_temperature_variation)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.invisible()
        binding.lytToolbar.view1.loadImage(requireActivity(), R.drawable.ic_info_oreo)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    private fun updateUI() {
        binding.tvDate.text = "Wed, 23 April"
        binding.tvValue.text = "+0.5°F"

        binding.lytSkinTemp.lytLegendView.lytPeriod.textView1.text=getText(R.string.text_period)
        binding.lytSkinTemp.lytLegendView.lytPeriod.view1.backgroundTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.tvDate.context,
                    R.color.color_period
                )
            )
        binding.lytSkinTemp.lytLegendView.lytOvulation.textView1.text=getText(R.string.text_ovulation)
        binding.lytSkinTemp.lytLegendView.lytOvulation.view1.backgroundTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.tvDate.context,
                    R.color.color_ovulation
                )
            )
        binding.lytSkinTemp.lytLegendView.lytFollicular.textView1.text=getText(R.string.text_follicular)
        binding.lytSkinTemp.lytLegendView.lytFollicular.view1.backgroundTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.tvDate.context,
                    R.color.color_follicular
                )
            )
        binding.lytSkinTemp.lytLegendView.lytLuteal.textView1.text=getText(R.string.text_luteal)
        binding.lytSkinTemp.lytLegendView.lytLuteal.view1.backgroundTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.tvDate.context,
                    R.color.color_luteal
                )
            )

    }

    override fun subscribeObservers() {

    }


}