package com.noisefit.ui.watchface.custom.iconBuzz

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit.data.local.AppStaticData
import com.noisefit_commans.data.model.WatchFaceWidgets
import com.noisefit.luna.databinding.BottomWidgetSelectionBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit.ui.watchface.custom.WidgetsAdapter
import com.noisefit.ui.watchface.custom.WidgetsAdapterAction
import com.noisefit_commans.data.enums.GridType
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomWidgetSelection : BaseBottomSheetWithTransparent<BottomWidgetSelectionBinding>(
    BottomWidgetSelectionBinding::inflate
) {

    private val TAG = "BottomWidgetSelection"
    private var selectedWidget: WatchFaceWidgets? = null
    private var gridType: GridType? = null
    private var itemId = 0
    private var title: String = ""
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
            gridType = BottomWidgetSelectionArgs.fromBundle(it).gridType
            itemId = BottomWidgetSelectionArgs.fromBundle(it).itemId
            selectedWidget = BottomWidgetSelectionArgs.fromBundle(it).widget
            title = BottomWidgetSelectionArgs.fromBundle(it).title
        }

        binding.tvTitle.text = title
        binding.lytAllowCancel.btnAllow.text = getString(R.string.text_save)

        LOGS.d("$TAG ${gridType?.name} $itemId")
        setRecycler()

        binding.lytAllowCancel.btnAllow.setOnClickListener {
            setFragmentResult(
                WIDGET_PICKER_RESULT,
                bundleOf("selectedWidget" to selectedWidget, "itemId" to itemId)
            )
            navigateUpSafe()
        }
        binding.lytAllowCancel.btnCancel.setOnClickListener {
            navigateUpSafe()
        }


    }

    private fun setRecycler() {
        binding.rvWidgets.layoutManager = LinearLayoutManager(context)
        binding.rvWidgets.adapter = widgetAdapter
        gridType.let {
            widgetAdapter.setDataSet(getWidgetsList(it))
        }

        selectedWidget?.let {
            widgetAdapter.setSelectedWidgetName(it)
        }


    }

    private fun getWidgetsList(gridType: GridType?): List<WatchFaceWidgets> {
        val widgets = AppStaticData.getDatFitCustomItemsList()
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

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}