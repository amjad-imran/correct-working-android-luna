package com.oreo.ui.workout.detect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentDetectWorkoutListBinding
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.ui.BaseFragment
import com.oreo.ui.activity.OreoDMAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DetectWorkoutListFragment :
    BaseFragment<FragmentDetectWorkoutListBinding>(FragmentDetectWorkoutListBinding::inflate) {
    private lateinit var pagerAdapter: DetectWorkoutPagerAdapter
    private val viewModel: DetectWorkoutViewModel by viewModels()
    private val dmGraphAdapter: OreoDMAdapter by lazy {
        OreoDMAdapter()
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        if (viewModel.oreoAutoSportData.value?.second.isNullOrEmpty()) {
            viewModel.getNotAcceptingData()
        }

    }

    private fun setAdapter() {
        with(binding.rvDMGraph) {
            adapter = dmGraphAdapter
        }


    }


    override fun initListener() {
        binding.lytToolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
            tvTitle.text = getString(R.string.text_detected_workouts)

        }
    }

    private fun setViewPager(data: Pair<ArrayList<String>, HashMap<String, ArrayList<OreoAutoSportData>>>) {
        pagerAdapter =
            DetectWorkoutPagerAdapter(childFragmentManager, lifecycle, data.second, data.first)
        binding.vpFriends.isUserInputEnabled = true

        binding.vpFriends.adapter = pagerAdapter
        binding.vpFriends.setCurrentItem(data.first.size)
        TabLayoutMediator(binding.tabLayout, binding.vpFriends) { tab, position ->
            tab.text = data.first[position]
        }.attach()

    }

    override fun subscribeObservers() {

        viewModel.oreoAutoSportData.observe(this) {
            it?.let {
                setViewPager(it)
            }
        }
    }


}