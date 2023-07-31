package com.noisefit.ui.dashboard.feature.worldclock

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.R
import com.noisefit_commans.ui.BaseBottomSheet
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.data.response.WorldClockNetwork
import com.noisefit.databinding.BottomSheetAddStockBinding
import com.noisefit_commans.models.WorldClockList
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WorldClockBottomSheet : BaseBottomSheet<BottomSheetAddStockBinding>(BottomSheetAddStockBinding::inflate),
    TimeZoneListAction {

    private val viewModel: TimeZoneViewModel by viewModels()
    val adapter: TimeZoneAdapter by lazy {
        TimeZoneAdapter(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textView19.text = getString(R.string.text_select_city)
        binding.etSearch.hint = getString(R.string.text_search)

        setRecycler()
        subscribeObservers()
        initListener()
        viewModel.getCitiesList()
    }

    private fun setRecycler() {
        binding.rvTimeZones.layoutManager = LinearLayoutManager(context)
        binding.rvTimeZones.adapter = adapter
    }

    fun initListener() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterZoneList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    fun subscribeObservers() {

        viewModel.clockList.observe(viewLifecycleOwner) {
            adapter.setDataSet(it)
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(viewLifecycleOwner) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

    }

    override fun onZoneSelected(clock: WorldClockNetwork) {
        val wClock = WorldClockList.WClock(
            timeZone = clock.signedValue,
            content = clock.city
        )
        setFragmentResult(
            ADD_WORLD_CLOCK_KEY,
            bundleOf("clock" to wClock)
        )
        navigateUpSafe()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = true
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }
}