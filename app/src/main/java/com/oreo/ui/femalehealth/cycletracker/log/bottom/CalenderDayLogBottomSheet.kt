package com.oreo.ui.femalehealth.cycletracker.log.bottom

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.databinding.FragmentCalenderDayLogBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

const val CALENDER_DAY_LOG_KEY = "CALENDER_DAY_LOG_KEY"

@AndroidEntryPoint
class CalenderDayLogBottomSheet :
    BaseBottomSheetWithTransparent<FragmentCalenderDayLogBottomSheetBinding>(
        FragmentCalenderDayLogBottomSheetBinding::inflate
    ) {
    private val viewModel: CalenderDayLogViewModel by viewModels()
    private val args: CalenderDayLogBottomSheetArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.todayDate = LocalDate.parse(args.selectedDate)
        setTitleDate()
    }

    private fun setTitleDate() {
        binding.tvTitle.text = DateFormats.formatDate(
            viewModel.todayDate.toString(),
            DateFormats.dateFormat3,
            DateFormats.dateFormat7
        )
    }

    override fun initListener() {

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
    }

    override fun subscribeObservers() {

    }


}