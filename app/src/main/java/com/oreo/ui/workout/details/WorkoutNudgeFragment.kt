package com.oreo.ui.workout.details

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentOreoRedinessBannerBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.health.Nudges
import dagger.hilt.android.AndroidEntryPoint

const val WORKOUT_NUDGE = "WORKOUT_NUDGE"

@AndroidEntryPoint
class WorkoutNudgeFragment : BaseFragment<FragmentOreoRedinessBannerBinding>(
    FragmentOreoRedinessBannerBinding::inflate
) {

    private var bannerData: Nudges? = null

    companion object {
        @JvmStatic
        fun newInstance(data: Nudges) =
            WorkoutNudgeFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WORKOUT_NUDGE, data)

                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bannerData = it.getParcelable(WORKOUT_NUDGE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi(bannerData)
    }

    private fun setUi(bannerData: Nudges?) {
        binding.rootView.setBackgroundResource(0)
        binding.tvTitle.text = bannerData?.label
        binding.tvDescription.text = bannerData?.message
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}