package com.oreo.ui.workout.detect

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager

import com.noisefit.luna.databinding.FragmentDetectWorkoutBinding

import com.noisefit_commans.data.model.OreoAutoSportData
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM1 = "ARG_PARAM1"
private const val ARG_PARAM2 = "ARG_PARAM2"

@AndroidEntryPoint
class DetectWorkoutFragment :
    BaseFragment<FragmentDetectWorkoutBinding>(FragmentDetectWorkoutBinding::inflate) {


    private var detectWorkoutFragmentListener: DetectWorkoutFragmentListener? = null
    private val oreoAutoSportData = ArrayList<OreoAutoSportData>()
    private var key: String = ""


    private val detectWorkoutAdapter: DetectWorkoutAdapter by lazy {
        DetectWorkoutAdapter(object : DetectWorkoutListener {
            override fun onIdentifyWorkout(data: OreoAutoSportData, position: Int) {
                detectWorkoutFragmentListener?.onIdentifyWorkout(data, key)
            }

            override fun onDismissWorkout(data: OreoAutoSportData, position: Int) {
                detectWorkoutFragmentListener?.onDismissWorkout(data, key)

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

    fun setDetectWorkoutListener(detectWorkoutFragmentListener: DetectWorkoutFragmentListener) {
        this.detectWorkoutFragmentListener = detectWorkoutFragmentListener
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

            key = it.getString(ARG_PARAM2)!!
        }
    }


    companion object {

        @JvmStatic
        fun newInstance(dataList: ArrayList<OreoAutoSportData>, key: String) =
            DetectWorkoutFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, dataList as ArrayList)
                    putString(ARG_PARAM2, key)
                }
            }
    }
}

interface DetectWorkoutFragmentListener {
    fun onIdentifyWorkout(data: OreoAutoSportData, key: String)
    fun onDismissWorkout(data: OreoAutoSportData, key: String)
}