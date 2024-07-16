package com.oreo.ui.femalehealth.cycletracker.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCycleTrackerHistoryBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.FMHCycleHistoryDataModel
import com.oreo.ui.femalehealth.cycletracker.OnHistoryItemClickListener
import com.oreo.ui.femalehealth.cycletracker.streak.CycleDetailsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CycleTrackerHistoryFragment :
    BaseFragment<FragmentCycleTrackerHistoryBinding>(FragmentCycleTrackerHistoryBinding::inflate) {
    private val viewModel: CycleTrackerHistoryViewModel by viewModels()

    private val cycleTrackHistoryAdapter by lazy {
        FMHCycleTrackerHistoryAdapter(object : OnHistoryItemClickListener {
            override fun onHistoryItemClick(data: FMHCycleHistoryDataModel, position: Int) {
                val (frag, bundle) = CycleDetailsFragment.getStartData(
                    data
                )
                navigate(frag, bundle)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_cycle_cycle_history_page_visit)
        setRecycler()
        viewModel.getCycleHistoryData()
    }

    private fun setRecycler() {
        with(binding.rvCycleTrackHistory) {
            adapter = cycleTrackHistoryAdapter
        }
    }

    override fun initListener() {
        binding.lytToolbar.tvTitle.text = getText(R.string.text_cycle_history)

        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {
        viewModel.cycleHistoryData.observe(this) {
            cycleTrackHistoryAdapter.setData(it)
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(this) {
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