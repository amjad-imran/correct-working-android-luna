package com.noisefit.ui.myDevice.warrantyOld

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.databinding.BottomSheetMarketSelectorBinding
import com.noisefit_commans.ui.BaseBottomSheet
import com.noisefit.ui.myDevice.warranty.WarrantyMarketAction
import com.noisefit.ui.myDevice.warranty.WarrantyMarketAdapter

const val MARKET_REQUEST_KEY = "MARKET_REQUEST_KEY"

class BottomSheetMarketSelector :
    BaseBottomSheet<BottomSheetMarketSelectorBinding>(BottomSheetMarketSelectorBinding::inflate) {

    var selectedData: String? = null

    private val adapter: WarrantyMarketAdapter by lazy {
        WarrantyMarketAdapter(object : WarrantyMarketAction {
            override fun onPlaceSelected(selectedPlace: String) {
                selectedData = selectedPlace

                setFragmentResult(
                    MARKET_REQUEST_KEY,
                    bundleOf("selectedValue" to selectedPlace)
                )
                navigateUpSafe()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val marketList =
            arguments?.let { BottomSheetMarketSelectorArgs.fromBundle(it).marketPlaces }

        val selectedValue =
            arguments?.let { BottomSheetMarketSelectorArgs.fromBundle(it).selectedValue }

        setRecycler()

        marketList?.let {
            selectedData = it.firstOrNull()
            adapter.setDataSet(it)
            adapter.setSelected(selectedValue)
        }

    }

    private fun setRecycler() {
        binding.rvMarkets.layoutManager = LinearLayoutManager(context)
        binding.rvMarkets.adapter = adapter
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