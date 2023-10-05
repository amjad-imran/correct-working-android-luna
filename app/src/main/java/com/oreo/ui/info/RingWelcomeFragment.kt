package com.oreo.ui.info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRingCareBinding
import com.noisefit.luna.databinding.FragmentRingWelcomeBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.loadImageWithCache
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RingWelcomeFragment :
    BaseFragment<FragmentRingWelcomeBinding>(FragmentRingWelcomeBinding::inflate) {

    val viewModel: RingCareViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getWelcomeRingData()


    }


    override fun initListener() {
        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }


        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

    }

    override fun subscribeObservers() {
        viewModel.ringWelcome.observe(this) {
            binding.toolbar.tvTitle.text = it.title

            val joinedData = it.content.joinToString(separator = "\n\n")

            binding.tvContent.text = joinedData
            binding.ivMain.loadImageWithCache(binding.ivMain.context, it.url)

        }

    }


}