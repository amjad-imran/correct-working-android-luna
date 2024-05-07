package com.oreo.ui.femalehealth.cycletracker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCycleTrackerHistoryBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CycleTrackerHistoryFragment :
    BaseFragment<FragmentCycleTrackerHistoryBinding>(FragmentCycleTrackerHistoryBinding::inflate) {
    private val mViewModel: CycleTrackerViewModel by viewModels()
    private val cycleTrackHistoryAdapter by lazy {
        FMHCycleTrackorHistoryAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.rvCycleTrackHistory) {
            adapter = cycleTrackHistoryAdapter
        }
        cycleTrackHistoryAdapter.setData(mViewModel.getCycleHistoryData())
    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getText(R.string.text_cycle_history)

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {
        mViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        mViewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        mViewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar1.root.visible()
            } else {
                binding.progressBar1.root.gone()
            }
        }
    }


}