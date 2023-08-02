package com.oreo.ui.workout.detect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentDetectWorkoutBinding
import com.noisefit.ui.common.bottomSheet.ALERT_REQUEST_KEY
import com.noisefit.ui.friends.FriendsPagerAdapter
import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.delay
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM1 = "ARG_PARAM1"

@AndroidEntryPoint
class DetectWorkoutFragment :
    BaseFragment<FragmentDetectWorkoutBinding>(FragmentDetectWorkoutBinding::inflate) {


    private val oreoAutoSportData = ArrayList<OreoAutoSportData>()
    private val detectWorkoutAdapter: DetectWorkoutAdapter by lazy {
        DetectWorkoutAdapter(object : DetectWorkoutListener {
            override fun onIdentifyWorkout() {

            }

            override fun onDismissWorkout() {

                setFragmentResultListener(ALERT_REQUEST_KEY) { _, bundle ->
                    val allow = bundle.getBoolean("allow")
                    if (allow) {

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
    }

    private fun setAdapter() {
        with(binding.rv) {
            adapter = detectWorkoutAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

    }



    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        detectWorkoutAdapter.setData(oreoAutoSportData)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            it.getParcelableArrayList<OreoAutoSportData>(ARG_PARAM1)
                ?.let { it1 -> oreoAutoSportData.addAll(it1) }
        }
    }


    companion object {

        @JvmStatic
        fun newInstance(dataList: ArrayList<OreoAutoSportData>) =
            DetectWorkoutFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, dataList as ArrayList)

                }
            }
    }
}