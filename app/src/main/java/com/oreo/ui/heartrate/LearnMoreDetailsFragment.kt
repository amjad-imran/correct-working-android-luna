package com.oreo.ui.heartrate

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.databinding.FragmentLearnMoreDetailsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.LearnMoreDataModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LearnMoreDetailsFragment :
    BaseFragment<FragmentLearnMoreDetailsBinding>(FragmentLearnMoreDetailsBinding::inflate) {
    private val args: LearnMoreDetailsFragmentArgs by navArgs()
    private var data: LearnMoreDataModel? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            data = args.data
        }
        setUI()
    }

    private fun setUI() {
        binding.lytToolbar.tvTitle.text = data?.label
        binding.tvHeader.text = data?.title
        binding.imageView1.loadImage(requireContext(), data?.banner1)
        binding.tvDesc1.text=data?.desc1
        binding.imageView2.loadImage(requireContext(), data?.banner2)
        binding.tvDesc1.text=data?.desc2
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}