package com.noisefit.ui.reward.about

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.FragmentAboutRewardsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutRewardsFragment :
    BaseFragment<FragmentAboutRewardsBinding>(FragmentAboutRewardsBinding::inflate) {

    private val viewModel: AboutRewardsViewModel by viewModels()

    private val rewardAdapter: AboutRewardsAdapter by lazy {
        AboutRewardsAdapter()
    }

    private val howToAdapter: AboutRewardsAdapter by lazy {
        AboutRewardsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        viewModel.getRewardListData()
    }

    private fun setRecycler() {
        with(binding.rvRewards) {
            layoutManager = LinearLayoutManager(context)
            adapter = rewardAdapter
        }
        with(binding.rvEarns) {
            layoutManager = LinearLayoutManager(context)
            adapter = howToAdapter
        }

    }

    override fun initListener() {
        binding.backBtn.apply {
            setOnClickListener {
                navigateUpSafe()
            }
        }
    }

    override fun subscribeObservers() {

        viewModel.rewardList.observe(this) {
            it?.let {
                rewardAdapter.setDataSet(it, false)
            }
        }

        viewModel.earnRewardList.observe(this) {
            it?.let {
                binding.tvSubtitle.text = viewModel.subCategoryTitle
                howToAdapter.setDataSet(it, true)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

}