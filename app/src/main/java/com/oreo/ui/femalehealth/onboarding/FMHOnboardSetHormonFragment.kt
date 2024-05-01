package com.oreo.ui.femalehealth.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.noisefit.data.model.DiagnoseDataItem
import com.noisefit.luna.databinding.FragmentFMHOnboardSetHormonBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FMHOnboardSetHormonFragment :
    BaseFragment<FragmentFMHOnboardSetHormonBinding>(FragmentFMHOnboardSetHormonBinding::inflate) {


    private val mViewModel: FMHOnboardingViewModel by activityViewModels()

    private val mAdapter: FMHDiagnoseAdapter by lazy {
        FMHDiagnoseAdapter(object : FMHDiagnoseAdapter.OnItemClickListener {
            override fun onItemClick(data: DiagnoseDataItem, position: Int) {

            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.rvHormon) {
            adapter = mAdapter
        }
        mAdapter.setData(mViewModel.getHormonalData())
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}