package com.oreo.ui.femalehealth.cycletracker.log.bottom

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetCycleLogBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.MoEngageAppEventParams
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.FHFlowIconsModel
import com.oreo.data.model.FHSymptomsIconsModel
import com.oreo.ui.femalehealth.cycletracker.CycleSymptomsAdapter
import com.oreo.ui.femalehealth.cycletracker.OnSymptomsItemClick
import com.oreo.ui.femalehealth.cycletracker.log.CycleLogAdapter
import com.oreo.ui.femalehealth.cycletracker.log.OnLogItemClick
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

const val CYCLE_LOG_SAVE = "CYCLE_LOG_SAVE"

@AndroidEntryPoint
class BottomSheetCycleLog : BaseBottomSheetWithTransparent<BottomSheetCycleLogBinding>(
    BottomSheetCycleLogBinding::inflate
) {
    private val viewModel: CalenderDayLogViewModel by viewModels()

    private val args: BottomSheetCycleLogArgs by navArgs()
    private val flowAdapter: CycleLogAdapter by lazy {
        CycleLogAdapter(object : OnLogItemClick {
            override fun onItemClick(data: FHFlowIconsModel, position: Int) {
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_cycle_log_flow_click)
                flowAdapter.updateItem(data, position)

            }
        })
    }
    private val symptomsAdapter: CycleSymptomsAdapter by lazy {
        CycleSymptomsAdapter(object : OnSymptomsItemClick {
            override fun onItemClick(data: FHSymptomsIconsModel, position: Int) {
                viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_cycle_log_symtoms_click)
                symptomsAdapter.updateItem(data, position)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.calendarStartDate = LocalDate.parse(args.firstPeriodDate)
        viewModel.selectedDate.value = LocalDate.parse(args.selectedDate)


        if (args.periodEndDate != null) {
            viewModel.periodStartDate = LocalDate.parse(args.selectedDate)
            viewModel.periodEndDate = LocalDate.parse(args.periodEndDate)
        }

        setRecycler()
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
        binding.tvDate.text = viewModel.selectedDate.value?.format(
            DateTimeFormatter.ofPattern(
                "dd MMM",
                Locale(NoiseFitApplicationMain.appLanguage.languageCode)
            )
        ) ?: ""
    }


    override fun initListener() {
        binding.tvTitle.text = getString(R.string.text_cycle_log)

        binding.btnSave.setOnClickListener {
            val flowType = flowAdapter.getSelectedValue()
            val symptoms = symptomsAdapter.getData()
            val date = viewModel.selectedDate.value.toString()
            viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_cycle_log_period_click)
            viewModel.saveSymptom(date, symptoms, flowType)
        }

        binding.ivRight.setOnClickListener {
            val nextDate = viewModel.selectedDate.value!!.plusDays(1)
            if (nextDate > viewModel.todayDate) {
                return@setOnClickListener
            }
            viewModel.selectedDate.value = nextDate

            setFragmentResult(
                CALENDER_DAY_LOG_KEY,
                bundleOf("right" to true)
            )
        }

        binding.ivBack.setOnClickListener {

            val previousDate = viewModel.selectedDate.value!!.minusDays(1)
            if (previousDate < viewModel.calendarStartDate) {
                return@setOnClickListener
            }
            viewModel.selectedDate.value = previousDate

            setFragmentResult(
                CALENDER_DAY_LOG_KEY,
                bundleOf("left" to true)
            )
        }

        binding.ivLogAdd.setOnClickListener {

            setFragmentResult(
                CYCLE_LOG_SAVE,
                bundleOf("openActivity" to true)
            )
            navigateUpSafe()
        }
    }


    override fun subscribeObservers() {

        viewModel.selectedDate.observe(this) {
            setTitleDate()
            viewModel.getPeriodDates()
            viewModel.getFemaleHealthIcons(viewModel.selectedDate.value.toString())

        }

        viewModel.currentPeriodRange.observe(this) {
            if (it == null) {
                binding.tvSelectedDays.gone()
            } else {
                binding.tvSelectedDays.apply {
                    text = it
                    visible()
                }
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.serverSuccess.observe(this) {
            it?.getContent()?.let {

                setFragmentResult(
                    CYCLE_LOG_SAVE,
                    bundleOf("log_saved" to true)
                )

                navigateUpSafe()
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