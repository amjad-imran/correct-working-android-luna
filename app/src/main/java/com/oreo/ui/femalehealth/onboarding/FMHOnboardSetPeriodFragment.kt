package com.oreo.ui.femalehealth.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentFMHOnboardSetPeriodBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.wheel.WheelAdapterPeriod
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FMHOnboardSetPeriodFragment :
    BaseFragment<FragmentFMHOnboardSetPeriodBinding>(FragmentFMHOnboardSetPeriodBinding::inflate) {
    private val mViewModel: FMHOnboardingViewModel by activityViewModels()

    private val wheelAdapter: WheelAdapterPeriod<String> by lazy {
        WheelAdapterPeriod()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWheelPicker()
    }

    private fun setWheelPicker() {
        binding.wheelPicker.visibleItemCount = 3//it could not be less then 3
        wheelAdapter.data = mViewModel.getPeriodDayData()
        wheelAdapter.setOnItemSelectedListener { item ->
            mViewModel.updatePeriodDayIndex(wheelAdapter.currentItemPosition)
        }
        wheelAdapter.bind(binding.wheelPicker)
        wheelAdapter.selectedItemPosition = mViewModel.getPeriodDayIndex()
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}