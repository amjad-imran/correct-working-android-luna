package com.oreo.ui.activity

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentOreoActivityBannerBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.health.Nudges
import dagger.hilt.android.AndroidEntryPoint


const val SLEEP_ACTIVITY_BANNER = "SLEEP_ACTIVITY_BANNER"

@AndroidEntryPoint
class OreoActivityBannerFragment :
    BaseFragment<FragmentOreoActivityBannerBinding>(FragmentOreoActivityBannerBinding::inflate) {

    private var bannerData: Nudges? = null

    companion object {
        @JvmStatic
        fun newInstance(data: Nudges) =
            OreoActivityBannerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(SLEEP_ACTIVITY_BANNER, data)

                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bannerData = it.getParcelable(SLEEP_ACTIVITY_BANNER)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi(bannerData)
    }

    private fun setUi(bannerData: Nudges?) {
        binding.tvTitle.text = bannerData?.label
        binding.tvDescription.text = bannerData?.message
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}