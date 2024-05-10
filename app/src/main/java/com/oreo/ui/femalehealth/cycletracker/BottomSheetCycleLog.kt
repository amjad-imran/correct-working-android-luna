package com.oreo.ui.femalehealth.cycletracker

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetCycleLogBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.oreo.data.model.CycleLogDataModel
import com.oreo.data.model.FlowLog

const val CYCLE_LOG_SAVE = "CYCLE_LOG_SAVE"

class BottomSheetCycleLog : BaseBottomSheetWithTransparent<BottomSheetCycleLogBinding>(
    BottomSheetCycleLogBinding::inflate
) {
    private var cycleLog: CycleLogDataModel? = null
    private val args: BottomSheetCycleLogArgs by navArgs()
    private val flowAdapter: CycleLogAdapter by lazy {
        CycleLogAdapter(object : OnLogItemClick {
            override fun onItemClick(data: FlowLog, position: Int) {

            }
        })
    }
    private val symptomsAdapter: CycleLogAdapter by lazy {
        CycleLogAdapter(object : OnLogItemClick {
            override fun onItemClick(data: FlowLog, position: Int) {

            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            cycleLog = args.data
        }
        setRecycler()

    }

    private fun setRecycler() {
        with(binding.rvFlow) {
            adapter = flowAdapter
        }
        flowAdapter.setData(cycleLog?.flowData)

        with(binding.rvSymptoms) {
            adapter = symptomsAdapter
        }
        symptomsAdapter.setData(cycleLog?.symptomsData)
    }

    override fun initListener() {
        binding.tvTitle.text = getString(R.string.text_cycle_log)
        binding.btnSave.setOnClickListener {
            navigateUpSafe()
            setFragmentResult(
                CYCLE_LOG_SAVE,
                bundleOf("agree" to true)
            )

        }
        binding.ivLogAdd.setOnClickListener{
            navigateUpSafe()
            setFragmentResult(
                CYCLE_LOG_SAVE,
                bundleOf("agree" to false)
            )
        }
    }

    override fun subscribeObservers() {

    }
}