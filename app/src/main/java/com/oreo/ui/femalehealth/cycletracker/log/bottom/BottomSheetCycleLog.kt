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
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.FHFlowIconsModel
import com.oreo.data.model.FHSymptomsIconsModel
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

    private val args: BottomSheetCycleLogArgs by navArgs()
    private var selectedFlowType: String = ""
    private val flowAdapter: CycleLogAdapter by lazy {
        CycleLogAdapter(object : OnLogItemClick {
            override fun onItemClick(data: FHFlowIconsModel, position: Int) {
                selectedFlowType = data.symptomName ?: ""

            }
        })
    }
    private val symptomsAdapter: CycleSymptomsAdapter by lazy {
        CycleSymptomsAdapter(object : OnSymptomsItemClick {
            override fun onItemClick(data: FHSymptomsIconsModel, position: Int) {
                symptomsAdapter.updateItem(data, position)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
//            cycleLog = args.data
            viewModel.todayDate = LocalDate.parse(args.selectedDate)
        }
        setRecycler()
        setTitleDate()
        viewModel.getFemaleHealthIcons(viewModel.todayDate.toString())

    }

    private fun setRecycler() {
        with(binding.rvFlow) {
            adapter = flowAdapter
        }


        with(binding.rvSymptoms) {
            adapter = symptomsAdapter
        }

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

        binding.btnSave.setOnClickListener {
            val flowType = flowAdapter.getSelectedValue()
            val symptoms = symptomsAdapter.getData()
            val date = viewModel.todayDate.toString()
            viewModel.saveSymptom(date, symptoms, flowType)
        }
        binding.ivRight.setOnClickListener {

            viewModel.todayDate = LocalDate.parse(viewModel.todayDate.toString()).plusDays(1)
            setTitleDate()

            viewModel.getFemaleHealthIcons(viewModel.todayDate.toString())
            setFragmentResult(
                CALENDER_DAY_LOG_KEY,
                bundleOf("right" to true)
            )
        }

        binding.ivBack.setOnClickListener {

            viewModel.todayDate = LocalDate.parse(viewModel.todayDate.toString()).plusDays(-1)
            setTitleDate()
            viewModel.getFemaleHealthIcons(viewModel.todayDate.toString())
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
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
//                onApiErrorReceived(response)
            }
        }

        viewModel.femaleHealthIcons.observe(this) {
            it?.let {
                binding.groupHeader.visible()
                symptomsAdapter.setData(it.symptoms)
                flowAdapter.setData(it.flow)
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