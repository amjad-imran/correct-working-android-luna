package com.oreo.ui.sleep.nap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentONapBannerBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.health.Nudges
import com.oreo.ui.readiness.OreoReadinessBannerFragment
import com.oreo.ui.readiness.READINESS_BANNER

const val NAP_BANNER = "NAP_BANNER"

class ONapBannerFragment : BaseFragment<FragmentONapBannerBinding>(FragmentONapBannerBinding::inflate) {
    private var bannerData: Nudges? = null

    companion object {
        @JvmStatic
        fun newInstance(data: Nudges) =
            ONapBannerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(NAP_BANNER, data)

                }
            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bannerData = it.getParcelable(NAP_BANNER)
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