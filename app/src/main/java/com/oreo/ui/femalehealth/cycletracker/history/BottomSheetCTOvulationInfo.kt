package com.oreo.ui.femalehealth.cycletracker.history

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetCtOvulationInfoBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone

const val INFO_LOG = "INFO_LOG"

class BottomSheetCTOvulationInfo :
    BaseBottomSheetWithTransparent<BottomSheetCtOvulationInfoBinding>(
        BottomSheetCtOvulationInfoBinding::inflate
    ) {
    val args: BottomSheetCTOvulationInfoArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()

    }

    private fun setUI() {
        if (args.launchMode == "Follicular") {
            binding.tvTitle.text = getString(R.string.text_follicular_phase)
            binding.tvDesc.text = getString(R.string.text_fillicular_phase_desc)
            binding.tvTitle.setTextColor(
                ContextCompat.getColor(
                    binding.tvTitle.context,
                    R.color.color_follicular_info
                )
            )
        }else if (args.launchMode == "Ovulation Graph") {
            binding.tvTitle.gone()
            binding.tvDesc.text = "The current period prediction relies on your most recent logged period and your average cycle length. Our algorithm learns the pattern of your menstrual cycle using continuous data of your skin temperature over three cycles."
            binding.tvTitle.setTextColor(
                ContextCompat.getColor(
                    binding.tvTitle.context,
                    R.color.color_follicular_info
                )
            )
        }else if (args.launchMode == "Ovulation Graph Details") {
            binding.tvTitle.gone()
            binding.tvDesc.text = "The current period prediction relies on your most recent logged period and your average cycle length. Our algorithm learns the pattern of your menstrual cycle using continuous data of your skin temperature over three cycles. "
            binding.tvTitle.setTextColor(
                ContextCompat.getColor(
                    binding.tvTitle.context,
                    R.color.color_follicular_info
                )
            )
        } else {
            binding.tvTitle.text = getString(R.string.text_luteal_phase)
            binding.tvDesc.text = getString(R.string.text_luteal_phase_desc)
            binding.tvTitle.setTextColor(
                ContextCompat.getColor(
                    binding.tvTitle.context,
                    R.color.color_luteal
                )
            )
        }
    }


    override fun initListener() {


    }

    override fun subscribeObservers() {

    }
}