package com.oreo.ui.femalehealth.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.data.model.DiagnoseDataItem
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentFMHOnboardSetHormonBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.utils.Event
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FMHOnboardSetHormonFragment :
    BaseFragment<FragmentFMHOnboardSetHormonBinding>(FragmentFMHOnboardSetHormonBinding::inflate) {


    private val mViewModel: FMHOnboardingViewModel by activityViewModels()

    private val mAdapter: FMHDiagnoseAdapter by lazy {
        FMHDiagnoseAdapter(object : FMHDiagnoseAdapter.OnItemClickListener {
            override fun onItemClick(data: DiagnoseDataItem, position: Int) {
                if (position == 0) {
                    mAdapter.onNoneSelected(data)
                } else {
                    mAdapter.updateItem(position, data)
                }
                mViewModel.selectedHormoneListData = mAdapter.getUpdatedSelectedListData()
                handleNext()

            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        binding.lytBottomControls.bNext.text = getString(R.string.text_done)
    }

    override fun onResume() {
        super.onResume()

        handleNext()
    }

    fun handleNext() {
        if (mViewModel.selectedHormoneListData.isEmpty()) {
            binding.lytBottomControls.bNext.disable()
        } else {
            binding.lytBottomControls.bNext.enable()
        }
    }

    private fun setRecycler() {
        with(binding.rvHormon) {
            adapter = mAdapter
        }
        mAdapter.setData(mViewModel.getHormonalData())
    }

    override fun initListener() {
        binding.lytBottomControls.bNext.setOnClickListener {
            mViewModel.onNextPress.value = Event(true)
        }

    }

    override fun subscribeObservers() {

    }
}