package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.noisefit.luna.databinding.FragmentAddPeriodBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.oreo.data.model.FHFlowIconsModel
import com.oreo.data.model.FHSymptomsIconsModel
import com.oreo.ui.femalehealth.cycletracker.CycleSymptomsAdapter
import com.oreo.ui.femalehealth.cycletracker.OnSymptomsItemClick
import com.oreo.ui.femalehealth.cycletracker.log.CycleLogAdapter
import com.oreo.ui.femalehealth.cycletracker.log.OnLogItemClick
import com.oreo.ui.femalehealth.cycletracker.log.bottom.CYCLE_LOG_SAVE
import com.oreo.ui.femalehealth.cycletracker.log.bottom.CalenderDayLogViewModel
import com.oreo.ui.timelineScreen.addActivity.AddActivityItemsEnum
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AddPeriodLogFragment :
    BaseFragment<FragmentAddPeriodBinding>(FragmentAddPeriodBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
//    private val viewModel: AddPeriodViewModel by viewModels()

    private val logViewModel: CalenderDayLogViewModel by viewModels()

    private val mainViewModel: OreoMainViewModel by activityViewModels()

    private val flowAdapter: CycleLogAdapter by lazy {
        CycleLogAdapter(object : OnLogItemClick {
            override fun onItemClick(data: FHFlowIconsModel, position: Int) {
                flowAdapter.updateItem(data, position)

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

        setUi()
        setRecycler()
    }

    private fun setRecycler() {
        with(binding.lytCard.rvFlow) {
            adapter = flowAdapter
        }


        with(binding.lytCard.rvSymptoms) {
            adapter = symptomsAdapter
        }

    }

    private fun setUi() {

    }


    override fun initListener() {
        val navController =
            NavHostFragment.Companion.findNavController(this@AddPeriodLogFragment)

        binding.lytSelected.setOnClickListener {
            sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ACTIVITIES_LISTING)
        }

        binding.btnSave.setOnClickListener {
            val flowType = flowAdapter.getSelectedValue()
            val symptoms = symptomsAdapter.getData()
            val date = logViewModel.selectedDate.value.toString()
            logViewModel.saveSymptom(date, symptoms, flowType)
        }

        binding.lytSelected.setOnClickListener {
            sharedViewModel.showDropdownDialog(binding.lytSelected, AddActivityItemsEnum.CYCLE_LOG)
        }
    }

    override fun subscribeObservers() {
        logViewModel.femaleHealthIcons.observe(this) {
            it?.let {
                symptomsAdapter.setData(it.symptoms)
                flowAdapter.setData(it.flow)
            }
        }

        logViewModel.selectedDate.observe(this) {
//            setTitleDate()
            logViewModel.getPeriodDates()
            logViewModel.getFemaleHealthIcons(logViewModel.selectedDate.value.toString())
        }

        logViewModel.serverSuccess.observe(this) {
            it?.getContent()?.let {
                mainViewModel.reloadTodaysData()
                sharedViewModel.navigateUp()
            }
        }

        //
        logViewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        logViewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        logViewModel.getLoading().observe(this) {
            if (it) {
                uiController.displayProgressBar(true,"")
            } else {
                uiController.displayProgressBar(false,"")
            }
        }

    }

}