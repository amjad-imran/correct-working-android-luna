package com.oreo.ui.readiness

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentOreoRedinessBannerBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.health.Nudges
import dagger.hilt.android.AndroidEntryPoint

const val READINESS_BANNER = "READINESS_BANNER"

@AndroidEntryPoint
class OreoReadinessBannerFragment() :
    BaseFragment<FragmentOreoRedinessBannerBinding>(FragmentOreoRedinessBannerBinding::inflate) {

    private var bannerData: Nudges? = null
    private var listener: NudgeBannerListener? = null

    companion object {
        @JvmStatic
        fun newInstance(data: Nudges) =
            OreoReadinessBannerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(READINESS_BANNER, data)

                }
            }
    }
    fun setClickListener(listener: NudgeBannerListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bannerData = it.getParcelable(READINESS_BANNER)
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
        binding.lytChatCta.root.setOnClickListener {
            listener?.onAiClicked()
        }
    }

    override fun subscribeObservers() {

    }


}

interface NudgeBannerListener {
    fun onAiClicked()
}