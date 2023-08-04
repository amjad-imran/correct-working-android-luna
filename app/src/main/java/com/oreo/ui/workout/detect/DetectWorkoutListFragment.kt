package com.oreo.ui.workout.detect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentDetectWorkoutListBinding
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.LOGS
import com.oreo.ui.activity.OreoDMAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DetectWorkoutListFragment :
    BaseFragment<FragmentDetectWorkoutListBinding>(FragmentDetectWorkoutListBinding::inflate) {
    private lateinit var pagerAdapter: DetectWorkoutPagerAdapter
    private val viewModel: DetectWorkoutViewModel by viewModels()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.oreoAutoSportData.value?.second.isNullOrEmpty()) {
            viewModel.getNotAcceptingData()
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

    private fun setViewPager(pairData: Pair<ArrayList<String>, LinkedHashMap<String, ArrayList<OreoAutoSportData>>>) {
        pagerAdapter =
            DetectWorkoutPagerAdapter(
                childFragmentManager,
                lifecycle,
                pairData.second,
                pairData.first,
                object : DetectWorkoutFragmentListener {
                    override fun onIdentifyWorkout(
                        data: OreoAutoSportData,
                        key: String,
                        movementList: List<Int>?
                    ) {
                        navigate(DetectWorkoutListFragmentDirections.actionDetectWorkoutListFragmentToAddWorkoutFragment().setMovementList(movementList?.toIntArray()).setAutoSport(data))

                    }

                    override fun onDismissWorkout(data: OreoAutoSportData, key: String) {

                        val remainingDataList = pairData.second[key]
                        val index = remainingDataList?.indexOfFirst { it.id == data.id }
                        val titleIndex = pairData.first.indexOfFirst { it == key }
                        if (index != null && index != -1) {
                            remainingDataList.removeAt(index)
                        }
                        pairData.second[key] = remainingDataList!!

//                        if (remainingDataList.isEmpty()) {
//                            pairData.first.removeAt(titleIndex)
//                            TabLayoutMediator(binding.tabLayout, binding.vpFriends) { tab, position ->
//                                tab.text = pairData.first[position]
//                            }.attach()
//                            binding.vpFriends.adapter = pagerAdapter
//                            binding.vpFriends.adapter?.notifyItemRemoved(titleIndex)
////                            binding.vpFriends.currentItem = pairData.first.size
////                            TabLayoutMediator(binding.tabLayout, binding.vpFriends) { tab, position ->
////                                tab.text = pairData.first[position]
////                            }.attach()
////                            binding.vpFriends.adapter?.notifyDataSetChanged()
//                        }

                    }

                })
        binding.vpFriends.isUserInputEnabled = true
        binding.vpFriends.offscreenPageLimit = 1
        binding.vpFriends.adapter = pagerAdapter
        binding.vpFriends.setCurrentItem(pairData.first.size,false)
        TabLayoutMediator(binding.tabLayout, binding.vpFriends) { tab, position ->
            tab.text = pairData.first[position]
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