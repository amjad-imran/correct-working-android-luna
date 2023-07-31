package com.oreo.ui.sleep.banner

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentOreoSleepBannerBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.health.Nudges
import dagger.hilt.android.AndroidEntryPoint


const val SLEEP_SCORE_BANNER = "SLEEP_SCORE_BANNER"

@AndroidEntryPoint
class OreoSleepBannerFragment :
    BaseFragment<FragmentOreoSleepBannerBinding>(FragmentOreoSleepBannerBinding::inflate) {

    private var bannerData: Nudges? = null

    companion object {
        @JvmStatic
        fun newInstance(data: Nudges) =
            OreoSleepBannerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(SLEEP_SCORE_BANNER, data)

                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bannerData = it.getParcelable(SLEEP_SCORE_BANNER)
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