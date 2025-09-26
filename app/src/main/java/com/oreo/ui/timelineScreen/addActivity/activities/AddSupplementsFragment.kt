package com.oreo.ui.timelineScreen.addActivity.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.noisefit.data.model.timeline.SupplementOption
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddSupplementsBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.ui.common.bottomSheet.TIME_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.dpToPixel
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

    private fun setSpinnerUi(supplementsList: ArrayList<SupplementOption>) {
        val spinner = binding.spinnerSupplements

        // adapter, hint selection, offsets, etc.
        val adapter = CustomSpinnerAdapter(requireContext(), supplementsList)
        spinner.adapter = adapter

        spinner.setSelection(0)

        // 16dp gap below spinner
        (spinner as? androidx.appcompat.widget.AppCompatSpinner)?.apply {
            setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT)
            setDropDownVerticalOffset(16f.dpToPixel().toInt()) // or resources.getDimensionPixelSize(R.dimen.offset_16dp)
            setPopupBackgroundResource(R.drawable.bg_dropdown_add_log_bs_circadian)
        }

        // Cap dropdown height to 70% of fragment height
        view?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                val maxHeight = (view?.height?.times(0.7f))?.toInt()?: 400

                try {
                    val spinnerClass = androidx.appcompat.widget.AppCompatSpinner::class.java
                    val popupField = spinnerClass.getDeclaredField("mPopup").apply { isAccessible = true }
                    val popup = popupField.get(spinner)
                    // Import androidx.appcompat.widget.ListPopupWindow
                    if (popup is androidx.appcompat.widget.ListPopupWindow) {
                        popup.height = maxHeight   // <- THIS sets the popup’s max height (scrolls automatically)
                    }
                } catch (t: Throwable) {
                    // Reflection may break on some OEMs; swallow or log
                }
            }
        })
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
            val spinner = binding.spinnerSupplements
            val selected: SupplementOption? = spinner.selectedItem as? SupplementOption
            selected?.let {
                viewModel.logSupplements(it)
            }
        }
    }

    override fun subscribeObservers() {
        viewModel.supplementsList.observe(this){ supplements->
            setSpinnerUi(supplements)
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

    class CustomSpinnerAdapter(
        private val context: Context,
        private val data: List<SupplementOption>
    ) : BaseAdapter() {

        // Hint at index 0, followed by real items
        private val items: List<SupplementOption> = listOf(
            SupplementOption(id = -1, options = "Select Supplement", type = "", status = "", createdAt = "", updatedAt = "")
        ) + data

        override fun getCount(): Int = items.size
        override fun getItem(position: Int): Any = items[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun isEnabled(position: Int): Boolean = position != 0
        override fun areAllItemsEnabled(): Boolean = false

        // Closed view: shows hint at position 0 with arrow
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(android.R.layout.simple_spinner_item, parent, false)
            val tv = view.findViewById<TextView>(android.R.id.text1)
            val item = items[position]
            val isHint = position == 0 || item.id == -1

            tv.text = item.options
            tv.setTextColor(if (isHint) "#99FFFFFF".toColorInt() else "#FFFFFF".toColorInt())

            val arrow = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_keyboard_arrow_down_24)
            tv.setCompoundDrawablesWithIntrinsicBounds(null, null, arrow, null)
            tv.compoundDrawablePadding = (8 * view.resources.displayMetrics.density).toInt()
            return view
        }

        // Dropdown rows: hide hint by returning a zero-height view; avoid recycling that for real rows
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            if (position == 0) {
                // Always return/ensure a "hint spacer" view with height 0
                val v = (convertView?.takeIf { it.tag == "HINT_SPACER" } ?: View(context)).apply {
                    tag = "HINT_SPACER"
                    layoutParams = (layoutParams ?: ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 0
                    )).also { it.height = 0 }
                }
                return v
            }

            // If convertView is the hint spacer, ignore it and inflate a real row
            val safeConvert = convertView?.takeUnless { it.tag == "HINT_SPACER" }
            val view = safeConvert ?: LayoutInflater.from(context)
                .inflate(R.layout.spinner_item_addd_log_circadian, parent, false)

            val tv = view.findViewById<TextView>(R.id.tvTitle)
            val divider = view.findViewById<View>(R.id.divider)

            val item = items[position]
            tv.text = item.options ?: "-"
            tv.setTextColor("#FFFFFF".toColorInt())

            // divider visible except last visible item
            divider.setVisibilityByCondition(position != items.lastIndex)

            return view
        }

        /** Map spinner.selectedItemPosition -> data index (without hint). -1 if still on hint. */
        fun toDataIndex(spinnerPosition: Int): Int = if (spinnerPosition <= 0) -1 else spinnerPosition - 1
    }

}