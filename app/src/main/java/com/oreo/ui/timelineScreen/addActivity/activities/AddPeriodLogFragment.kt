package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.moengage.core.internal.utils.showToast
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddPeriodBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.FHFlowIconsModel
import com.oreo.data.model.FHSymptomsIconsModel
import com.oreo.ui.chatGpt.AITopics
import com.oreo.ui.femalehealth.cycletracker.CycleSymptomsAdapter
import com.oreo.ui.femalehealth.cycletracker.OnSymptomsItemClick
import com.oreo.ui.femalehealth.cycletracker.log.CycleLogAdapter
import com.oreo.ui.femalehealth.cycletracker.log.OnLogItemClick
import com.oreo.ui.femalehealth.cycletracker.log.bottom.CYCLE_LOG_SAVE
import com.oreo.ui.femalehealth.cycletracker.log.bottom.CalenderDayLogViewModel
import com.oreo.ui.lifeos.LifeOsChatFragment
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
                if(logViewModel.editDataAddActivity?.canBeEditedOrDeleted != 0) {
                    flowAdapter.updateItem(data, position)
                    if(symptomsAdapter.getData().isNotEmpty() || data.isChecked) {
                        binding.btnSave.enable()
                    }else{
                        binding.btnSave.disable()
                    }
                    binding.btnSave.text = getString(R.string.text_save)
                }
            }
        })
    }
    private val symptomsAdapter: CycleSymptomsAdapter by lazy {
        CycleSymptomsAdapter(object : OnSymptomsItemClick {
            override fun onItemClick(data: FHSymptomsIconsModel, position: Int) {
                if(logViewModel.editDataAddActivity?.canBeEditedOrDeleted != 0) {
                    symptomsAdapter.updateItem(data, position)
                    if(symptomsAdapter.getData().isNotEmpty() || flowAdapter.getSelectedValue() != null) {
                        binding.btnSave.enable()
                    }else{
                        binding.btnSave.disable()
                    }
                    binding.btnSave.text = getString(R.string.text_save)
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logViewModel.editDataAddActivity = arguments?.getParcelable("editData")
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
        if(logViewModel.editDataAddActivity != null){
            when(logViewModel.editDataAddActivity?.canBeEditedOrDeleted){
                0 -> {
                    binding.btnSave.gone()
                }

                else -> {
                    binding.btnSave.apply {
                        text = getString(R.string.text_learn_more_with_luna_ai)
                        enable()
                        visible()
                    }
                }
            }
        }else{
            binding.btnSave.disable()
        }
    }


    override fun initListener() {
        val navController =
            NavHostFragment.Companion.findNavController(this@AddPeriodLogFragment)

        binding.lytSelected.setOnClickListener {
            sharedViewModel.loadFragmentByType(AddActivityItemsEnum.ACTIVITIES_LISTING)
        }

        binding.btnSave.setOnClickListener {
            if(getString(R.string.text_save).equals(binding.btnSave.text)) {
                val flowType = flowAdapter.getSelectedValue()
                val symptoms = symptomsAdapter.getData()
                val date = logViewModel.selectedDate.value.toString()
                logViewModel.saveSymptom(date, symptoms, flowType)
                //
                //
                sharedViewModel.sourceKey?.let { sourceKey ->
                    sharedViewModel.sessionManager.logMoEngageAppEvent(
                        MoEngageLunaAppEvents.insight_logged,
                        HashMap<String, Any>().apply {
                            this["source"] = sourceKey
                            this["log_category"] = "period_symptom"
                            this["period_flow_type"] = "$flowType"
                            this["period_symptoms"] = "$symptoms"
                        }
                    )
                }
            }else{
                if (sharedViewModel.ringDataStore.getRingDevice() == null) {
                    context.showShortToast(getString(R.string.text_luna_ai_message))
                }else {
                    val (frag, bundle) = LifeOsChatFragment.getStartData(
                        threadId = null,
                        userMessage = null,
                        title = null,
                        aiTopic = AITopics.GENERAL
                    )
                    navigate(
                        frag, bundle
                    )
                }
            }
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
                mainViewModel.sessionManager.reloadOnResume = true
                sharedViewModel.navigateUp()
            }
        }

        sharedViewModel.deleteBtnClickedEvent.observe(this){
            it.getContent()?.let {
                if(it) {
                    if(logViewModel.editDataAddActivity?.id == null){
                        showToast(requireContext(),
                            getString(R.string.text_something_went_wrong_please_try_again))
                        return@observe
                    }
                    logViewModel.deletePeriodSymptomsItem(){
                        sharedViewModel.sessionManager.logMoEngageAppEvent(
                            MoEngageLunaAppEvents.insight_log_deleted,
                        )
                    }
                }
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
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

    }

}