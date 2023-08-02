package com.oreo.ui.workout.detect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentDetectWorkoutListBinding
import com.noisefit.ui.common.bottomSheet.ALERT_REQUEST_KEY
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

    private fun setViewPager(pairData: Pair<ArrayList<String>, LinkedHashMap<String, ArrayList<OreoAutoSportData>>>) {
        pagerAdapter =
            DetectWorkoutPagerAdapter(
                childFragmentManager,
                lifecycle,
                pairData.second,
                pairData.first,
                object : DetectWorkoutFragmentListener {
                    override fun onIdentifyWorkout(data: OreoAutoSportData, key: String) {
                        navigate(R.id.addWorkoutFragment, Bundle().apply {
                            putParcelable("autoSport", data)

                        })
                    }

                    override fun onDismissWorkout(data: OreoAutoSportData, key: String) {

                        setFragmentResultListener(ALERT_REQUEST_KEY) { _, bundle ->
                            val allow = bundle.getBoolean("allow")
                            if (allow) {
                                val remainingDataList = pairData.second[key]
                                val index = remainingDataList?.indexOfFirst { it.id == data.id }
                                val titleIndex = pairData.first.indexOfFirst { it == key }
                                if (index != null && index != -1) {
                                    remainingDataList.removeAt(index)
                                }
                                pairData.second[key] = remainingDataList!!
                                pagerAdapter.createFragment(titleIndex)
                                binding.vpFriends.adapter?.notifyItemChanged(titleIndex)
                                LOGS.d("SDAsdasdasd ${Gson().toJson(pairData.second[key])}")
                            }
                        }
                        navigate(
                            DetectWorkoutListFragmentDirections.actionDetectWorkoutListFragmentToAlertTextBottomSheet(
                                getString(R.string.text_dismiss_activity_title),
                                getString(R.string.text_dismiss_activity_desc), "", ""
                            )
                        )
                    }

                })
        binding.vpFriends.isUserInputEnabled = true

        binding.vpFriends.adapter = pagerAdapter
        binding.vpFriends.setCurrentItem(pairData.first.size)
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