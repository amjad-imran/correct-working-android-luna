package com.oreo.ui.femalehealth.cycletracker.log.bottom

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetCycleLogBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.CycleLogDataModel
import com.oreo.data.model.FlowLog
import com.oreo.ui.femalehealth.cycletracker.CycleSymptomsAdapter
import com.oreo.ui.femalehealth.cycletracker.OnSymptomsItemClick

import com.oreo.ui.femalehealth.cycletracker.log.CycleLogAdapter
import com.oreo.ui.femalehealth.cycletracker.log.OnLogItemClick
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

const val CYCLE_LOG_SAVE = "CYCLE_LOG_SAVE"

@AndroidEntryPoint
class BottomSheetCycleLog : BaseBottomSheetWithTransparent<BottomSheetCycleLogBinding>(
    BottomSheetCycleLogBinding::inflate
) {
    private val viewModel: CalenderDayLogViewModel by viewModels()
    private var cycleLog: CycleLogDataModel? = null
    private val args: BottomSheetCycleLogArgs by navArgs()
    private var selectedFlowType: String = ""
    private val flowAdapter: CycleLogAdapter by lazy {
        CycleLogAdapter(object : OnLogItemClick {
            override fun onItemClick(data: FlowLog, position: Int) {
                selectedFlowType = data.title ?: ""

            }
        })
    }
    private val symptomsAdapter: CycleSymptomsAdapter by lazy {
        CycleSymptomsAdapter(object : OnSymptomsItemClick {
            override fun onItemClick(data: FlowLog, position: Int) {
                symptomsAdapter.updateItem(data, position)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            cycleLog = args.data
            viewModel.todayDate = LocalDate.parse(args.selectedDate)
        }
        setRecycler()
        setTitleDate()

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

    private fun setTitleDate() {
        binding.tvDate.text = DateFormats.formatDate(
            viewModel.todayDate.toString(),
            DateFormats.dateFormat3,
            DateFormats.dateFormat7
        )
    }


    override fun initListener() {
        binding.tvTitle.text = getString(R.string.text_cycle_log)

        binding.ivRight.setOnClickListener {
            viewModel.todayDate =
                LocalDate.parse(viewModel.todayDate.toString()).plusDays(1)
            setTitleDate()
            setFragmentResult(
                CALENDER_DAY_LOG_KEY,
                bundleOf("right" to true)
            )
        }

        binding.ivBack.setOnClickListener {
            viewModel.todayDate =
                LocalDate.parse(viewModel.todayDate.toString()).plusDays(-1)
            setTitleDate()
            setFragmentResult(
                CALENDER_DAY_LOG_KEY,
                bundleOf("left" to true)
            )
        }
        binding.btnSave.setOnClickListener {

            val selectedSymptomList = symptomsAdapter.getUpdatedSelectedListData()
            setFragmentResult(
                CYCLE_LOG_SAVE,
                bundleOf("agree" to true, "data" to selectedSymptomList, "flow" to selectedFlowType)
            )
            navigateUpSafe()

        }
        binding.ivLogAdd.setOnClickListener {
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