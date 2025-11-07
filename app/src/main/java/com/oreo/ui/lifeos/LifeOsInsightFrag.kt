package com.oreo.ui.lifeos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsInsightBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LifeOsInsightFrag :
    BaseFragment<FragmentLifeOsInsightBinding>(FragmentLifeOsInsightBinding::inflate) {

    private val viewModel: LifeOsInsightsViewModel by viewModels()

    private val insightAdapter by lazy {
        LifeOsInsightListAdapter { text ->
            navigate(
                R.id.lifeOsInsightDetailsFragment,
                android.os.Bundle().apply {
                    putString("insightId", "")
                }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
    }

    override fun initListener() {
        binding.ivBack.setOnClickListener {
            navigateUpSafe()
        }
        binding.ivNewChat.setOnClickListener {
            navigate(R.id.lifeOsChatFragment)
        }
    }

    private fun setupRecycler() {
        binding.rvInsights.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = insightAdapter
        }
    }

    override fun subscribeObservers() {
        viewModel.insights.observe(viewLifecycleOwner) { list ->
            insightAdapter.submit(list)
        }
    }
}
