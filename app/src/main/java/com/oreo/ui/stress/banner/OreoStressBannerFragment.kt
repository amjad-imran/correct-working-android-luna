package com.oreo.ui.stress.banner

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOreoSleepBannerBinding
import com.noisefit.luna.databinding.FragmentOreoStressBannerBinding
import com.noisefit_commans.data.enums.StressType
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.StressNudge
import dagger.hilt.android.AndroidEntryPoint


const val STRESS_BANNER = "STRESS_BANNER"

@AndroidEntryPoint
class OreoStressBannerFragment :
    BaseFragment<FragmentOreoStressBannerBinding>(FragmentOreoStressBannerBinding::inflate) {

    private var bannerData: StressNudge? = null

    companion object {
        @JvmStatic
        fun newInstance(data: StressNudge) =
            OreoStressBannerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(STRESS_BANNER, data)

                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bannerData = it.getParcelable(STRESS_BANNER)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi(bannerData)
    }

    private fun setUi(bannerData: StressNudge?) {
        val stressType = bannerData?.value?.lowercase()
        if (stressType.equals(StressType.CALM.name.lowercase())) {
            binding.rootView.setBackgroundResource(R.drawable.ic_st_calm_cue_bg)
        } else if (stressType.equals(StressType.FOCUSED.name.lowercase())) {
            binding.rootView.setBackgroundResource(R.drawable.ic_st_focus_cue_bg)
        } else if (stressType.equals(StressType.STRESSED.name.lowercase())) {
            binding.rootView.setBackgroundResource(R.drawable.ic_st_stress_cue_bg)
        } else {
            binding.rootView.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
        }
        binding.tvTitle.text = bannerData?.label
        binding.tvDescription.text = bannerData?.message
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}