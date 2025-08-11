package com.oreo.ui.timelineScreen

import android.os.Bundle
import com.noisefit.luna.databinding.FragmentTimelineScreenDataBinding
import com.noisefit_commans.ui.BaseFragment

class TimelineScreenDataFragment : BaseFragment<FragmentTimelineScreenDataBinding>(FragmentTimelineScreenDataBinding::inflate) {
    private val ARGS_DATE = "ARGS_DATE"

    companion object {
        @JvmStatic
        fun newInstance(date: String) = TimelineScreenDataFragment().apply {
            arguments = Bundle().apply {
                putString(ARGS_DATE, date)
            }
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}