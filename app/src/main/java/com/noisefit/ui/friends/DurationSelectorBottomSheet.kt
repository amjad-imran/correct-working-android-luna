package com.noisefit.ui.friends

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.noisefit.luna.databinding.DurationBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit.ui.common.bottomSheet.ValueSelectorAdapter
import com.noisefit.ui.common.bottomSheet.ValueSelectorBottomSheetArgs
import com.noisefit_commans.utils.ScreenUtils
import com.noisefit_commans.utils.ValueLayoutManager
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val VALUE_REQUEST_KEY = "VALUE_REQUEST_KEY"

@AndroidEntryPoint
class DurationSelectorBottomSheet : BaseBottomSheetWithTransparent<DurationBottomSheetBinding>(
    DurationBottomSheetBinding::inflate
) {

    private var mSelectionList: Array<String>? = null
    private var mSelectedValue: String? = null
    private var mInitialSelectedValue: String? = null
    private var mTitle: String? = null
    private var mSelectedPosition: Int = 0


    @Inject
    lateinit var screenUtils: ScreenUtils

    private val listDefaultPadding by lazy {
        screenUtils.getPadding(requireContext())
    }

    private val valueSelectionAdapter by lazy {
        ValueSelectorAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()
        arguments?.let {
            val args = ValueSelectorBottomSheetArgs.fromBundle(it)
            mSelectionList = args.selectionList
            mTitle = args.title
            initUi(args.selectedValue, args.selectionList)
        }

    }


    private fun initUi(selectedValue: String?, selectionList: Array<String>) {
        if (selectionList.isEmpty()) {
            LOGS.e("Selection List empty")
            navigateUpSafe()
        }

        binding.tvTitle.text = mTitle
        valueSelectionAdapter.setDataSet(selectionList.toList())
        mSelectedValue = if (selectedValue.isNullOrEmpty()) selectionList[0] else selectedValue
        mInitialSelectedValue = selectedValue
        mSelectedPosition = if (selectedValue.isNullOrEmpty()) {
            0
        } else {
            getSelectedPosition(selectedValue, selectionList)
        }
        binding.rv.scrollToPosition(mSelectedPosition)


    }

    private fun setRecycler() {

        binding.rv.apply {
            setPadding(0, listDefaultPadding, 0, listDefaultPadding)
            adapter = valueSelectionAdapter
        }
        binding.rv.layoutManager = ValueLayoutManager(requireContext()).apply {
            callback = object : ValueLayoutManager.OnItemSelectedListener {
                override fun onItemSelected(layoutPosition: Int) {
                    LOGS.d("TAG", "onItemSelected: $layoutPosition ")
                    mSelectedValue = mSelectionList?.get(layoutPosition) ?: ""
                    mSelectedPosition = layoutPosition
                }
            }
        }


    }

    private fun getSelectedPosition(selectedValue: String, selectionList: Array<String>): Int {
        for ((index, item) in selectionList.withIndex()) {
            if (item.equals(selectedValue, true)) {
                return index
            }
        }
        return 0
    }


    override fun initListener() {

        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnAllow.setOnClickListener {

            val isValueChanged = if (mInitialSelectedValue == null) {
                true
            } else {
                !mInitialSelectedValue.equals(mSelectedValue, true)
            }

            requireActivity().supportFragmentManager.setFragmentResult(
                VALUE_REQUEST_KEY,
                bundleOf(
                    "selectedValue" to mSelectedValue,
                    "selectedPosition" to mSelectedPosition,
                    "isValueChanged" to isValueChanged
                )
            )
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }


}