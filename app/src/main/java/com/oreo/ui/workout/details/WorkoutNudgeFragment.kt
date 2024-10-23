package com.oreo.ui.workout.details

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOreoRedinessBannerBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.health.Nudges
import com.oreo.ui.readiness.NudgeBannerListener
import dagger.hilt.android.AndroidEntryPoint

const val WORKOUT_NUDGE = "WORKOUT_NUDGE"
const val NUDGE_BG_KEY = "NUDGE_BG_KEY"

@AndroidEntryPoint
class WorkoutNudgeFragment() :
    BaseFragment<FragmentOreoRedinessBannerBinding>(
        FragmentOreoRedinessBannerBinding::inflate
    ) {

    private var bannerData: Nudges? = null
    private var nudgeBgColor: String? = null
    private var listener: NudgeBannerListener? = null

    companion object {
        @JvmStatic
        fun newInstance(data: Nudges, nudgeBgColor: NudgeBgColor) =
            WorkoutNudgeFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WORKOUT_NUDGE, data)
                    putString(NUDGE_BG_KEY, nudgeBgColor.name)
                }
            }
    }


    fun setClickListener(listener: NudgeBannerListener) {
        this.listener = listener
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bannerData = it.getParcelable(WORKOUT_NUDGE)
            nudgeBgColor = it.getString(NUDGE_BG_KEY)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUi(bannerData)
    }

    private fun setUi(bannerData: Nudges?) {
        when (nudgeBgColor) {
            NudgeBgColor.OVULATION_HIGH.name, NudgeBgColor.OVULATION_LOW.name -> {
                binding.bgImv.setBackgroundResource(R.drawable.bg_ovulation_main)
            }

            NudgeBgColor.PERIOD_HIGH.name, NudgeBgColor.PERIOD_LOW.name -> {
                binding.bgImv.setBackgroundResource(R.drawable.bg_period_main)
            }

            else -> {
                binding.tvLunaAi.gone()
                binding.bgImv.setBackgroundResource(R.drawable.ic_nudge_activity)
            }
        }

        binding.tvTitle.text = bannerData?.label
        if (bannerData?.label.isNullOrEmpty()) {
            binding.tvTitle.gone()
        } else {
            binding.tvTitle.visible()
        }
        binding.tvDescription.text = bannerData?.message
    }

    override fun initListener() {
        binding.tvLunaAi.setOnClickListener {
            listener?.onAiClicked()
        }
    }

    override fun subscribeObservers() {

    }


}

enum class NudgeBgColor {
    NONE,
    PERIOD_HIGH,
    PERIOD_LOW,
    OVULATION_HIGH,
    OVULATION_LOW
}