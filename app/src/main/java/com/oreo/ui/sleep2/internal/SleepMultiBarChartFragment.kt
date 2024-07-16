package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentSleepBarChartBinding
import com.noisefit.luna.databinding.FragmentSleepMultiBarChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.custom.SleepGraphInteractionListener
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SleepMultiBarChartFragment :
    BaseFragment<FragmentSleepMultiBarChartBinding>(FragmentSleepMultiBarChartBinding::inflate) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    companion object {
        @JvmStatic
        fun newInstance() =
            SleepMultiBarChartFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.graphBar.setDataSet(
            arrayListOf(
                Pair(60, 40),
                Pair(80, 50),
                Pair(90, 60),
                Pair(100, 40),
                Pair(120, 20),
                Pair(150, 10),
                Pair(180, 0),
            ),
            4
        )


        binding.graphBar.setVibrationUtil(vibrationUtils)

        binding.graphBar.setClickListener(object : SleepSingleBarAction {
            override fun onValueSelected(position: Int) {

            }

            override fun isInteractionOnGoing(onGoing: Boolean) {

            }

        })
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}