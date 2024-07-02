package com.oreo.ui.sleep2.internal

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentSleepBarChartBinding
import com.noisefit_commans.ui.BaseFragment

class SleepBarChartFragment :
    BaseFragment<FragmentSleepBarChartBinding>(FragmentSleepBarChartBinding::inflate) {

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
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}