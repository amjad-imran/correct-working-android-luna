package com.oreo.ui.stress.help

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentStressUnderstandingBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StressUnderstandingFragment :
    BaseFragment<FragmentStressUnderstandingBinding>(FragmentStressUnderstandingBinding::inflate) {

    private val viewModel: StressUnderstandingViewModel by viewModels()

    private val stressAdapter: StressUnderstandingAdapter by lazy {
        StressUnderstandingAdapter()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setData()
    }

    private fun setData() {
        with(binding.rv) {
            layoutManager = LinearLayoutManager(context)
            adapter = stressAdapter
        }
        stressAdapter.items = viewModel.getData()
    }

    override fun initListener() {
        binding.lytToolbar.apply {
            tvTitle.text = getString(R.string.text_understanding_stress)
            backBtn.setOnClickListener {
                navigateUpSafe()

            }
        }

    }

    override fun subscribeObservers() {

    }


}