package com.oreo.ui.timelineScreen.addActivity.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddSupplementsBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.setVisibilityByCondition
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
class AddSupplementsFragment : BaseFragment<FragmentAddSupplementsBinding>(FragmentAddSupplementsBinding::inflate) {

    private val viewModel: AddSupplementsViewModel by viewModels()
    private val sharedViewModel: AddActivityTimelineSharedViewModel by activityViewModels()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getSupplementsList()
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
            viewModel.supplementTime = LocalTime.of(hour, minute)
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
            viewModel.logSupplements()
        }
    }

    override fun subscribeObservers() {
        viewModel.supplementsList.observe(this){ supplements->
            val spinner = binding.spinnerSupplements

            // Create an instance of the custom adapter
            val adapter = CustomSpinnerAdapter(requireContext(), supplements)

            // Set the adapter to the spinner
            spinner.adapter = adapter

        }

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

    class CustomSpinnerAdapter(
        private val context: Context,
        private val data: List<String>
    ) : BaseAdapter()
    {

        // This will return the number of items in the spinner
        override fun getCount(): Int {
            return data.size
        }

        // This will return the item at a particular position
        override fun getItem(position: Int): Any {
            return data[position]
        }

        // This will return the item ID at a particular position (optional)
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        // This is used to get the view for the spinner item
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.spinner_item_addd_log_circadian, parent, false)

            val textView: TextView = view.findViewById(R.id.tvTitle)
            textView.text = data[position]

            val divider: View = view.findViewById(R.id.divider)
            divider.setVisibilityByCondition(position!=data.size-1)
            // Set the color for the selected item
            if (position == 0) {
                textView.setTextColor(context.resources.getColor(android.R.color.darker_gray))  // Hint color (gray)
            } else {
                textView.setTextColor(context.resources.getColor(android.R.color.white))  // Regular item color
            }

            return view
        }

        // This is used to get the view for the dropdown items
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.spinner_item_addd_log_circadian, parent, false)

            val textView: TextView = view.findViewById(R.id.tvTitle)
            textView.text = data[position]

            // Set the color for the dropdown items
            if (position == 0) {
                textView.setTextColor(context.resources.getColor(android.R.color.darker_gray))  // Hint color (gray)
            } else {
                textView.setTextColor(context.resources.getColor(android.R.color.white))  // Regular item color
            }

            return view
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
            viewModel.supplementTime = time
            binding.lytDateTime.lytTime.tvTimeValue.text = curFormattedTime

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