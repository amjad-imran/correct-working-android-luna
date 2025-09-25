package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddAlcoholBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.DateFormats
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.getValue

@AndroidEntryPoint
class AddAlcoholFragment : BaseFragment<FragmentAddAlcoholBinding>(FragmentAddAlcoholBinding::inflate) {

    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
    private val viewModel: AddAlcoholViewmodel by viewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectedDate = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        setUi()
    }

    private fun setUi() {
        binding.lytDateTime.apply {
            lytDate.tvTime.text = getString(R.string.text_date)
            lytTime.tvTime.text = getString(R.string.text_time)

            // set Default Date
            lytDate.tvTimeValue.text =
                LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    .toString()

            // set Default(Current) Time
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val curFormattedTime = DateFormats.formatTimeWithAmPm(
                hour,
                minute
            )
            viewModel.alcoholTime = LocalTime.of(hour, minute)
            lytTime.tvTimeValue.text = curFormattedTime
        }
    }

    override fun initListener() {
        binding.lytDateTime.lytDate.tvTimeValue.setOnClickListener {
            onDateClicked()
        }

        binding.lytDateTime.lytTime.tvTimeValue.setOnClickListener {
            onTimeClicked()
        }

        binding.btnSave.setOnClickListener{
            viewModel.logAlcohol()
        }
    }

    override fun subscribeObservers() {
        viewModel.onAddSuccess.observe(this){
            it.getContent()?.let {
                mainViewModel.sessionManager.reloadOnResume = true
                sharedViewModel.navigateUp()
            }
        }

        //
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                uiController.displayProgressBar(true,"")
            } else {
                uiController.displayProgressBar(false,"")
            }
        }
    }

    private fun onTimeClicked() {
        parentFragment?.setFragmentResultListener(TIME_REQUEST_KEY) { _, bundle ->
            val hourOfDay = bundle.getInt("hour")
            val minute = bundle.getInt("minute")

            val time = LocalTime.of(hourOfDay, minute)
            if (time > LocalTime.now()) {
                context.showShortToast("Time cannot be in future") //TODO message change
                return@setFragmentResultListener
            }

            val curFormattedTime = DateFormats.formatTimeWithAmPm(
                hourOfDay,
                minute
            )
            viewModel.alcoholTime = time
            binding.lytDateTime.lytTime.tvTimeValue.text = curFormattedTime

        }

        val navController =
            NavHostFragment.Companion.findNavController(this@AddAlcoholFragment)

        navController.navigate(
            R.id.timeBottomSheet,
            bundleOf(
                "hour" to viewModel.alcoholTime.hour,
                "minute" to viewModel.alcoholTime.minute,
                "hourOther" to 0,
                "minuteOther" to 0,
                "isStart" to 1,
                "unitPosition" to 1,
                "title" to getString(R.string.text_time)
            )
        )
    }

    private fun onDateClicked() {
        parentFragment?.setFragmentResultListener(VALUE_REQUEST_KEY) { _, bundle ->
            val selectedValue = bundle.getString("selectedValue")
            selectedValue?.let { it1 ->

                val parsedDate =
                    LocalDate.parse(it1, DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString()
                viewModel.selectedDate = parsedDate

                binding.lytDateTime.lytDate.tvTimeValue.text = it1
            }
        }
        val format = DateTimeFormatter.ofPattern("dd MMM yyyy")

        val navController =
            NavHostFragment.Companion.findNavController(this@AddAlcoholFragment)

        navController.navigate(
            R.id.valueSelectorBottomSheet,
            bundleOf(
                "selectedValue" to if (viewModel.selectedDate == null) null else LocalDate.parse(viewModel.selectedDate)
                    .format(format),
                "selectionList" to viewModel.getAlcoholDates(),
                "title" to  getString(R.string.text_date)
            )
        )

    }

}