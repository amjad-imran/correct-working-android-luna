package com.oreo.ui.heartrate

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOHRInfoBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OHRInfoFragment : BaseFragment<FragmentOHRInfoBinding>(FragmentOHRInfoBinding::inflate) {
    private val viewModel: OHRInfoViewModel by viewModels()
    private val mAdapter: OHRInfoAdapter by lazy {
        OHRInfoAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.vRecycler) {
            adapter = mAdapter
        }
        mAdapter.setData(viewModel.getHrInfoListData())
    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_heart_rate_graph)
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}