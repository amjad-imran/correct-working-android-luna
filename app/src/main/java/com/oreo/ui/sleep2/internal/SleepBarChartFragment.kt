package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentSleepBarChartBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.custom.SleepGraphInteractionListener
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.ui.custom.sleep.internal.SleepSingleBarAction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SleepBarChartFragment :
    BaseFragment<FragmentSleepBarChartBinding>(FragmentSleepBarChartBinding::inflate) {

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    companion object {
        @JvmStatic
        fun newInstance() =
            SleepBarChartFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.graphBar.setDataSet(
            arrayListOf(
                20,
                30,
                null,
                50,
                100,
                70,
                null
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