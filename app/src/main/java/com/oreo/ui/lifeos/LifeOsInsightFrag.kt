package com.oreo.ui.lifeos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsInsightBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.ui.chatGpt.AITopics
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LifeOsInsightFrag :
    BaseFragment<FragmentLifeOsInsightBinding>(FragmentLifeOsInsightBinding::inflate) {

    private val viewModel: LifeOsInsightsViewModel by viewModels()

    private val insightAdapter by lazy {
        LifeOsInsightListAdapter { insightItem ->
            navigate(
                R.id.lifeOsInsightDetailsFragment,
                Bundle().apply { putParcelable("insightData", insightItem) }
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
            val (frag, bundle) = LifeOsChatFragment.getStartData(
                threadId = null,
                userMessage = null,
                title = null,
                aiTopic = AITopics.GENERAL
            )
            navigate(frag,bundle)
        }
    }

    private fun setupRecycler() {
        binding.rvInsights.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = insightAdapter
        }
    }

    override fun subscribeObservers() {
        viewModel.cards.observe(viewLifecycleOwner) { list ->
            if(list.isEmpty()){
                binding.llMainScrollView.gone()
                binding.lytDashInsightsEmpty.visible()
            }else{
                binding.lytDashInsightsEmpty.gone()
                binding.llMainScrollView.visible()
                insightAdapter.submitList(list)
            }
        }

        //
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }
}
