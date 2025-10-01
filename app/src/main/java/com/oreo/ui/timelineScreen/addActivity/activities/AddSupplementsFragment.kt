package com.oreo.ui.timelineScreen.addActivity.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moengage.core.internal.utils.showToast
import com.noisefit.data.model.timeline.SupplementOption
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddSupplementsBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.ui.timelineScreen.addActivity.AddActivityTimelineSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.getValue
import kotlin.math.roundToInt

@AndroidEntryPoint
class AddSupplementsFragment : BaseFragment<FragmentAddSupplementsBinding>(FragmentAddSupplementsBinding::inflate) {

    private val viewModel: AddSupplementsViewModel by viewModels()
    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    private val optAdapter by lazy {
        RecoveryOptionsAdapter(){
            handleOnOptionClicked(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.editData = arguments?.getParcelable("editData")
        viewModel.getSupplementsList()
        setUi()
        setAdapter()
    }

    private fun setAdapter() {
        binding.rvOptions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = optAdapter
        }
    }

    private fun setUi() {
        binding.btnSave.disable()
        binding.lytDateTime.apply {
            lytDate.tvTime.text = getString(R.string.text_date)
            lytTime.tvTime.text = getString(R.string.text_time)

            if(viewModel.editData == null) {
                // set Default Date
                viewModel.selectedDate = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
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
                viewModel.supplementTime = LocalTime.of(hour, minute)
                lytTime.tvTimeValue.text = curFormattedTime
            }else{
                viewModel.selectedDate = viewModel.editData?.startDate
                lytDate.tvTimeValue.text = LocalDate
                            .parse(viewModel.selectedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

                viewModel.supplementTime =
                    LocalTime.parse(viewModel.editData?.startTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
                lytTime.tvTimeValue.text = DateFormats.formatTimeWithAmPm(
                    viewModel.supplementTime.hour,
                    viewModel.supplementTime.minute
                )

                // Other Ui checks
                val editCondition = viewModel.editData?.canBeEditedOrDeleted!=0
                lytDate.tvTimeValue.isClickable = editCondition
                lytTime.tvTimeValue.isClickable = editCondition
                binding.lytSelected.isClickable = editCondition
                if(!editCondition){
                    binding.btnSave.gone()
                }
            }

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
            if(viewModel.selectedOption==null){
                // TODO: Update Msg Text
                showToast(requireContext(), "Please select an option!")
                return@setOnClickListener
            }
            viewModel.logSupplements()
        }

        binding.lytSelected.setOnClickListener {
            handleDropDown()
        }
    }

    override fun subscribeObservers() {
        viewModel.supplementsList.observe(this){ supplements->
            optAdapter.updateDataSet(supplements)
            if(
                viewModel.isListLoadedFirstTime &&
                viewModel.editData?.metadata?.lunaTrackingOptionId != null
            ){
                supplements.first { it.id == (viewModel.editData?.metadata?.lunaTrackingOptionId ?: -1) }?.let { sOpt ->
                    binding.tvSelected.text = sOpt.options
                    viewModel.selectedOption = sOpt
                }
                viewModel.isListLoadedFirstTime = false
            }
            binding.rvOptions.doOnNextLayout {
                capRvHeightToPercent(binding.rvOptions, binding.root, 0.70f)
            }
        }

        viewModel.onAddSuccess.observe(this){
            it.getContent()?.let {
                mainViewModel.sessionManager.reloadOnResume = true
                sharedViewModel.navigateUp()
            }
        }

        sharedViewModel.deleteBtnClickedEvent.observe(this){
            it.getContent()?.let {
                if(it) {
                    if(viewModel.editData?.id == null){
                        showToast(requireContext(),
                            getString(R.string.text_something_went_wrong_please_try_again))
                        return@observe
                    }
                    viewModel.deleteSupplementItem()
                }
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

    private fun capRvHeightToPercent(rv: RecyclerView, root: View, percent: Float) {
        val rootH = root.height.takeIf { it > 0 } ?: root.measuredHeight
        if (rootH <= 0) return // nothing to do yet

        val maxH = (rootH * percent).roundToInt()
        val params = rv.layoutParams
        val rvMeasured = rv.measuredHeight

        params.height = if (rvMeasured > maxH) maxH else ViewGroup.LayoutParams.WRAP_CONTENT
        rv.layoutParams = params
    }

    private fun handleDropDown() {
        if(viewModel.isDropdownOpen){
            binding.rvOptions.gone()
        }else{
            binding.rvOptions.visible()
        }
        viewModel.isDropdownOpen = !viewModel.isDropdownOpen
    }

    private fun handleOnOptionClicked(option: SupplementOption) {
        binding.btnSave.enable()
        if(viewModel.selectedOption == null){
            binding.tvSelected.setTextColor("#FFFFFF".toColorInt())
        }
        viewModel.selectedOption = option
        binding.tvSelected.text = option.options
        binding.rvOptions.gone()
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
            viewModel.supplementTime = time
            binding.lytDateTime.lytTime.tvTimeValue.text = curFormattedTime

            if(viewModel.editData?.startTime != null){
                val editDataTime = LocalTime.parse(viewModel.editData?.startTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
                if(
                    editDataTime.hour != viewModel.supplementTime.hour ||
                    editDataTime.minute != viewModel.supplementTime.minute
                ){
                    binding.btnSave.enable()
                }
            }
        }

        val navController =
            NavHostFragment.Companion.findNavController(this@AddSupplementsFragment)

        navController.navigate(
            R.id.timeBottomSheet,
            bundleOf(
                "hour" to viewModel.supplementTime.hour,
                "minute" to viewModel.supplementTime.minute,
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
                if(
                    viewModel.editData?.startDate != null &&
                    !parsedDate.equals(viewModel.editData?.startDate)
                ){
                    binding.btnSave.enable()
                }
            }
        }
        val format = DateTimeFormatter.ofPattern("dd MMM yyyy")

        val navController =
            NavHostFragment.Companion.findNavController(this@AddSupplementsFragment)

        navController.navigate(
            R.id.valueSelectorBottomSheet,
            bundleOf(
                "selectedValue" to if (viewModel.selectedDate == null) null else LocalDate.parse(viewModel.selectedDate)
                    .format(format),
                "selectionList" to viewModel.getSupplementsDates(),
                "title" to  getString(R.string.text_date)
            )
        )

    }

}