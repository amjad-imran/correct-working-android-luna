package com.oreo.ui.femalehealth.cycletracker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCycleTrackerStreaksBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CycleTrackerStreaksFragment :
    BaseFragment<FragmentCycleTrackerStreaksBinding>(FragmentCycleTrackerStreaksBinding::inflate) {
    private val viewModel: CycleTrackerStreakViewModel by viewModels()

    private val symptomsAdapter: CTStreakSymptomsAdapter by lazy {
        CTStreakSymptomsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()
        setRecycler()
    }

    private fun setUI() {
        binding.lytToolbar.tvTitle.text = getString(R.string.text_cycle_details)
        binding.lytToolbar.view1.visible()
        binding.lytToolbar.ivAddFriend.invisible()
        binding.lytToolbar.view1.loadImage(
            binding.lytToolbar.view1.context,
            R.drawable.ic_ct_streak_info
        )
    }

    private fun setRecycler() {
        with(binding.lytSymptomsRecord.rvSymptomsRecord) {
            adapter = symptomsAdapter
        }
        symptomsAdapter.setData(viewModel.getSymptomsData())
    }

    override fun initListener() {
        binding.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }


}