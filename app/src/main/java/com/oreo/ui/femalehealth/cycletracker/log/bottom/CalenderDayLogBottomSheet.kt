package com.oreo.ui.femalehealth.cycletracker.log.bottom

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentCalenderDayLogBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.femaleh.FemaleHealthUserInfoModel
import com.oreo.ui.femalehealth.cycletracker.CycleTrackerViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

const val CALENDER_DAY_LOG_KEY = "CALENDER_DAY_LOG_KEY"

@AndroidEntryPoint
class CalenderDayLogBottomSheet :
    BaseBottomSheetWithTransparent<FragmentCalenderDayLogBottomSheetBinding>(
        FragmentCalenderDayLogBottomSheetBinding::inflate
    ) {

    private val viewModel: CycleTrackerViewModel by viewModels()

    private val mAdapter: SymptomDayLogAdapter by lazy {
        SymptomDayLogAdapter()
    }
    private val args: CalenderDayLogBottomSheetArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        viewModel.firstPeriodDate = LocalDate.parse(args.firstPeriodDate)
        viewModel.lastPeriodDate =
            if (args.lastPeriodDate == null) null else LocalDate.parse(args.lastPeriodDate)
        viewModel.selectedDate.value = LocalDate.parse(args.selectedDate)
    }

    private fun setRecycler() {
        with(binding.rvFlow) {
            adapter = mAdapter
        }
    }

    private fun setTitleDate(localDate: LocalDate) {
        viewModel.getDataForDate(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        binding.tvTitle.text = localDate.format(DateTimeFormatter.ofPattern("dd MMM"))
    }

    override fun initListener() {

        binding.btnLog.setOnClickListener {
            setFragmentResult(
                CALENDER_DAY_LOG_KEY,
                bundleOf("open_log" to true)
            )
            navigateUpSafe()
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
            if (previousDate < viewModel.firstPeriodDate) {
                return@setOnClickListener
            }

            viewModel.selectedDate.value = previousDate

            setFragmentResult(
                CALENDER_DAY_LOG_KEY,
                bundleOf("left" to true)
            )
        }
    }

    private fun setTopData(data: FemaleHealthUserInfoModel) {
        binding.apply {
            val selectedDateLocal = viewModel.selectedDate.value ?: LocalDate.now()

            val selectedDate = selectedDateLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            if (selectedDateLocal > LocalDate.now()) {
                btnLog.isEnabled = false
            } else {
                btnLog.isEnabled = true
            }


            val datePeriod = "Day ${(data.currentDay ?: 0)} of ${(data.cycleLength ?: 0)}"
            tvDatePeriod.text = datePeriod

            tvMessage.text = viewModel.getPregnancyText(data.pregnancyChances)
            with(
                viewModel.getCurrentPhaseText(
                    data.ovulationDate, data.periodDate, selectedDate
                )
            ) {
                if (this == null) {
                    tvPhase.text = "-"
                } else {
                    tvPhase.text = this.first
                    tvPhase.setTextColor(tvPhase.context.getColor(this.second))
                }
            }

            val isPastDate = viewModel.isPastCycleLogic2(selectedDateLocal)


            if (data.isPeriod || data.isOvulation) {
                if (data.isPeriod) {
                    if (isPastDate) {
                        showPastCycleUI(data.currentDay ?: 0)
                    } else {
                        tvCurrentState.text = if (data.otaLog) "Period" else "Predicted period"
                        tvStateDay.text = "Day ${data.currentDay}"
                    }

                } else {
                    if (selectedDate.equals(data.ovulationDate)) {
                        if (isPastDate) {
                            showPastCycleUI(data.currentDay ?: 0)
                        } else {
                            tvCurrentState.text = "Predicted day of"
                            tvStateDay.text = "Ovulation"
                        }

                    }

                }
            } else {
                val daysUntilOvulation = if (data.ovulationDate != null) {
                    viewModel.calculateDaysLeft(data.ovulationDate, selectedDate)
                } else {
                    null
                }

                val daysUntilNextPeriod =
                    viewModel.calculateDaysLeft(data.nextPeriodDate!!, selectedDate)

                if (daysUntilOvulation != null && (daysUntilOvulation < daysUntilNextPeriod && daysUntilOvulation > 0)) {
                    if (isPastDate) {
                        showPastCycleUI(data.currentDay ?: 0)
                    } else {
                        tvCurrentState.text = "Ovulation in"
                        tvStateDay.text = "${daysUntilOvulation} Days"
                    }
                } else {
                    if (isPastDate) {
                        showPastCycleUI(data.currentDay ?: 0)
                    } else {
                        tvCurrentState.text = "Period in"
                        tvStateDay.text = "${daysUntilNextPeriod} Days"
                    }
                }
            }
        }
    }

    private fun showPastCycleUI(currentDay: Int) {
        binding.tvCurrentState.text = "Past cycle"
        binding.tvStateDay.text = "Day $currentDay"
    }

    override fun subscribeObservers() {
        viewModel.selectedDate.observe(this) {
            setTitleDate(it)
        }

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

        viewModel.femaleHealthData.observe(this) {
            if (it?.currentDay == null) { //should not come here
                navigateUpSafe()
                //context.showShortToast("Screen pending")
            } else {
                setTopData(it)
            }

        }


        viewModel.symptomList.observe(this) {
            it?.let {
                binding.btnLog.visible()
                if (it.isEmpty()) {
                    binding.btnLog.setText(getString(R.string.text_log))
                } else {
                    binding.btnLog.setText(getString(R.string.edit))
                }
                mAdapter.setData(it)
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