package com.oreo.ui.internal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOHMInternalBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.OHMDataModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OHMInternalFragment :
    BaseFragment<FragmentOHMInternalBinding>(FragmentOHMInternalBinding::inflate) {
    private val viewModel: OHMInternalViewModel by viewModels()
    private val mAdapter: OHMInternalAdapter by lazy {
        OHMInternalAdapter(object : OHMInternalAdapter.HMItemClickListener {
            override fun onItemClick(resultData: OHMDataModel, position: Int) {

            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_health_monitor)
        with(binding.rvHm) {
            adapter = mAdapter
        }
        mAdapter.setData(viewModel.getHealthMonitorData())
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }


}