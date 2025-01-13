package com.oreo.ui.googlefit

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.FragmentGoogleFitDataBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoogleFitDataFragment :
    BaseFragment<FragmentGoogleFitDataBinding>(FragmentGoogleFitDataBinding::inflate) {

    private val viewModel: GoogleFitDataViewModel by viewModels()
    private val mAdapter : GoogleFitDataAdapter by lazy {
        GoogleFitDataAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvMain.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMain.adapter = mAdapter

        viewModel.loadData()
    }
    override fun initListener() {

    }

    override fun subscribeObservers() {

        viewModel.unSyncedDataList.observe(this){
            mAdapter.setDataSet(it)
        }

    }

}