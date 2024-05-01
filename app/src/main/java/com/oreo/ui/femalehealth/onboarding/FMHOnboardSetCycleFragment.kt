package com.oreo.ui.femalehealth.onboarding

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.luna.databinding.FragmentFMHOnboardSetCycleBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.WheelAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FMHOnboardSetCycleFragment :
    BaseFragment<FragmentFMHOnboardSetCycleBinding>(FragmentFMHOnboardSetCycleBinding::inflate) {
    private val mViewModel: FMHOnboardingViewModel by activityViewModels()
    private val wheelAdapter: WheelAdapter<String> by lazy {
        WheelAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWheelPicker()
    }

    private fun setWheelPicker() {
        binding.wheelPicker.visibleItemCount = 5//it could not be less then 3
        wheelAdapter.data = mViewModel.getPeriodCycleDayData()
        wheelAdapter.setOnItemSelectedListener { item ->
            Log.d(
                "TAG",
                "onItemSelected: ${item.split(" ").get(0)}"
            )
            Log.d("TAG", "get current item ${wheelAdapter.currentItemPosition}")
            mViewModel.updatePeriodCycleDayIndex(wheelAdapter.currentItemPosition)
        }
        wheelAdapter.bind(binding.wheelPicker)
        wheelAdapter.selectedItemPosition = mViewModel.getPeriodCycleDayIndex()
    }


    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}