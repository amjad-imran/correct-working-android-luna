package com.noisefit.ui.watchface.custom

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.data.local.AppStaticData
import com.noisefit_commans.data.model.WatchFaceWidgets
import com.noisefit.luna.databinding.BottomSheetWidgetSelectionBinding
import com.noisefit_commans.data.enums.GridType
import com.noisefit_commans.ui.BaseBottomSheet

class BottomSheetWidgetSelection :
    BaseBottomSheet<BottomSheetWidgetSelectionBinding>(BottomSheetWidgetSelectionBinding::inflate) {

    private var selectedWidget: WatchFaceWidgets? = null
    private var selectedColor: String? = null

    private var gridType: GridType? = null

    private val colorAdapter: GridColorAdapter by lazy {
        GridColorAdapter(object : GridColorAction {
            override fun onColorSelected(color: String) {
                selectedColor = "#$color"
            }
        })
    }
    private val widgetAdapter: WidgetsAdapter by lazy {
        WidgetsAdapter(object : WidgetsAdapterAction {
            override fun onWidgetSelected(widget: WatchFaceWidgets) {
                selectedWidget = if (widget.widgetName.equals("None", true)) {
                    null
                } else {
                    widget
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            gridType = BottomSheetWidgetSelectionArgs.fromBundle(it).gridType
            selectedWidget = BottomSheetWidgetSelectionArgs.fromBundle(it).selectedWidget
            selectedColor = BottomSheetWidgetSelectionArgs.fromBundle(it).selectedColor
        }

        setRecycler()

        binding.bDone.setOnClickListener {
            setFragmentResult(
                WIDGET_PICKER_RESULT,
                bundleOf("selectedWidget" to selectedWidget, "selectedColor" to selectedColor)
            )
            navigateUpSafe()
        }
        binding.bCancel.setOnClickListener {
            navigateUpSafe()
        }


    }

    private fun setRecycler() {
        binding.rvWidgets.layoutManager = LinearLayoutManager(context)
        binding.rvWidgets.adapter = widgetAdapter
        gridType?.let {
            widgetAdapter.setDataSet(getWidgetsList(it))
        }

        selectedWidget?.let {
            widgetAdapter.setSelected(it)
        }

        binding.rvColors.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvColors.adapter = colorAdapter
        colorAdapter.setDataSet(AppStaticData.getGridColors())

        selectedColor?.let {
            colorAdapter.setSelected(it)
        }
    }

    fun getWidgetsList(gridType: GridType): List<WatchFaceWidgets> {
        val widgets = AppStaticData.getWatchFaceWidgets()
        val list = ArrayList<WatchFaceWidgets>()
        widgets.forEach outer@{
            it.supportedGrid.forEach inner@{ gType ->
                if (gType == gridType) {
                    list.add(it)
                    return@inner
                }
            }
        }
        return list
    }

    companion object {
        const val WIDGET_PICKER_RESULT = "widget_picker_result"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { it ->
                val behaviour = BottomSheetBehavior.from(it)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }
}