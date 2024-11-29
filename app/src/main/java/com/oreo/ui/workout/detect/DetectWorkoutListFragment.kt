package com.oreo.ui.workout.detect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentDetectWorkoutListBinding
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.ui.BaseFragment
import com.oreo.ui.workout.add.ADD_WORKOUT_REQUEST_KEY
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


@AndroidEntryPoint
class DetectWorkoutListFragment :
    BaseFragment<FragmentDetectWorkoutListBinding>(FragmentDetectWorkoutListBinding::inflate) {
    private lateinit var pagerAdapter: DetectWorkoutPagerAdapter
    private val viewModel: DetectWorkoutViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onResume() {
        super.onResume()
        viewModel.getNotAcceptingData()
    }


    override fun initListener() {
        binding.lytToolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
            tvTitle.text = getString(R.string.text_detected_workouts)

        }

        setFragmentResultListener(ADD_WORKOUT_REQUEST_KEY) { _, bundle ->
            val allow = bundle.getBoolean("allow")
            val workId = bundle.getString("workId")
            val actName = bundle.getString("actName")
            if (allow) {
                workId?.let { navigateToDetailsWorkout(actName ?: "", it) }
            }
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
                        navigate(
                            DetectWorkoutListFragmentDirections.actionDetectWorkoutListFragmentToAddWorkoutFragment()
                                .setMovementList(movementList?.toIntArray()).setAutoSport(data)
                        )

                    }

                    override fun onDismissWorkout(id: Int, key: String) {


                        removeFromList(id, key, pairData)

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
        binding.vpFriends.setCurrentItem(pairData.first.size, false)
        TabLayoutMediator(binding.tabLayout, binding.vpFriends) { tab, position ->
            tab.text = LocalDate.parse(pairData.first[position]).format(
                DateTimeFormatter.ofPattern(
                    "MMM dd",
                    Locale(NoiseFitApplicationMain.appLanguage.languageCode)
                )
            )
        }.attach()

    }

    fun removeFromList(
        id: Int,
        key: String,
        pairData: Pair<ArrayList<String>, LinkedHashMap<String, ArrayList<OreoAutoSportData>>>
    ) {
        val remainingDataList = pairData.second[key]
        val index = remainingDataList?.indexOfFirst { it.id == id }
        val titleIndex = pairData.first.indexOfFirst { it == key }
        if (index != null && index != -1) {
            remainingDataList.removeAt(index)
        }
        pairData.second[key] = remainingDataList!!

        if (remainingDataList.isEmpty()) {
            viewModel.getNotAcceptingData()
        }
    }

    override fun subscribeObservers() {

        viewModel.oreoAutoSportData.observe(this) {

            it?.let {
                if (it.first.isEmpty()) {
                    navigateUpSafe()
                    return@observe
                }
                setViewPager(it)
            }
        }
    }

    private fun navigateToDetailsWorkout(activityName: String, workoutId: String) {
        navigate(R.id.oWorkoutDetailsFragment, Bundle().apply {
            putString("workoutName", activityName)
            putString("workoutId", workoutId)
            putInt("position", -1)
        })
    }


}