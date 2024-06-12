package com.oreo.ui.femalehealth.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.data.model.DiagnoseDataItem
import com.noisefit.luna.databinding.FragmentFMHOnboardSetDignosisBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.utils.Event
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FMHOnboardSetDignosisFragment :
    BaseFragment<FragmentFMHOnboardSetDignosisBinding>(FragmentFMHOnboardSetDignosisBinding::inflate) {
    private val mViewModel: FMHOnboardingViewModel by activityViewModels()

    private val mAdapter: FMHDiagnoseAdapter by lazy {
        FMHDiagnoseAdapter(object : FMHDiagnoseAdapter.OnItemClickListener {
            override fun onItemClick(data: DiagnoseDataItem, position: Int) {
                if (position == 0) {
                    mAdapter.onNoneSelected(data)
                } else {
                    mAdapter.updateItem(position, data)
                }
                mViewModel.selectedDiagnoseListData = mAdapter.getUpdatedSelectedListData()
                handleNext()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        handleNext()
    }

    fun handleNext(){
        if (mViewModel.selectedDiagnoseListData.isEmpty()) {
            binding.lytBottomControls.bNext.disable()
        } else {
            binding.lytBottomControls.bNext.enable()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.rvDiagnose) {
            adapter = mAdapter
        }
        mAdapter.setData(mViewModel.getDiagnoseData())
    }

    override fun initListener() {

        binding.lytBottomControls.bNext.setOnClickListener {
            mViewModel.onNextPress.value = Event(true)
        }
    }

    override fun subscribeObservers() {

    }
}