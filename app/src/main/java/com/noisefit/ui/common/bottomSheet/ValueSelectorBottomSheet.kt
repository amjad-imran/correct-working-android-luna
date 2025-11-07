package com.noisefit.ui.common.bottomSheet

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentTempUnitBottomSheetBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.noisefit_commans.utils.ScreenUtils
import com.noisefit_commans.utils.WheelAdapter
import com.noisefit_commans.utils.WheelItem
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val VALUE_REQUEST_KEY = "VALUE_REQUEST_KEY"

@AndroidEntryPoint
class ValueSelectorBottomSheet : BaseBottomSheetWithTransparent<FragmentTempUnitBottomSheetBinding>(
    FragmentTempUnitBottomSheetBinding::inflate
) {

    private var mSelectionList: Array<String>? = null
    private var mSelectedValue: String? = null
    private var mInitialSelectedValue: String? = null
    private var mTitle: String? = null
    private var mSelectedPosition: Int = 0


    @Inject
    lateinit var screenUtils: ScreenUtils

    @Inject
    lateinit var sessionManager: SessionManager


    private val wheelAdapter: WheelAdapter<String> by lazy {
        WheelAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setRecycler()
        arguments?.let {
            val args = ValueSelectorBottomSheetArgs.fromBundle(it)
            mSelectionList = args.selectionList
            mTitle = args.title
            initUi(args.selectedValue, args.selectionList, args.isTopLineVisible)
        }

    }


    private fun initUi(selectedValue: String?, selectionList: Array<String>, isTopLineVisible: Boolean) {
        if (selectionList.isEmpty()) {
            LOGS.e("Selection List empty")
            navigateUpSafe()
        }

        binding.tvTitle.text = mTitle
        if(isTopLineVisible){
            binding.view1.visible()
            binding.list.setBackgroundResource(R.drawable.back_modal_dialog)
        }

        val listData = ArrayList<WheelItem<String>>()
        selectionList.forEach {
            listData.add(WheelItem(it))
        }
        binding.wheelPicker.visibleItemCount = 5//it could not be less then 3
        wheelAdapter.data = listData
        wheelAdapter.setOnItemSelectedListener { item ->
            Log.d(
                "TAG",
                "onItemSelected: ${item.split(" ").get(0)}"
            )
            Log.d("TAG", "get current item ${wheelAdapter.currentItemPosition}")
            mSelectedValue = mSelectionList?.get(wheelAdapter.currentItemPosition) ?: ""
            mSelectedPosition = wheelAdapter.currentItemPosition
        }
        wheelAdapter.bind(binding.wheelPicker)
//        valueSelectionAdapter.setDataSet(selectionList.toList())
        mSelectedValue = if (selectedValue.isNullOrEmpty()) selectionList[0] else selectedValue
        mInitialSelectedValue = selectedValue
        mSelectedPosition = if (selectedValue.isNullOrEmpty()) {
            0
        } else {
            getSelectedPosition(selectedValue, selectionList)
        }
        wheelAdapter.selectedItemPosition = mSelectedPosition
//        binding.rv.scrollToPosition(mSelectedPosition)


    }

    private fun setRecycler() {

//        binding.rv.apply {
//            setPadding(0, listDefaultPadding, 0, listDefaultPadding)
//            adapter = valueSelectionAdapter
//        }
//        binding.rv.layoutManager = ValueLayoutManager(requireContext()).apply {
//            callback = object : ValueLayoutManager.OnItemSelectedListener {
//                override fun onItemSelected(layoutPosition: Int) {
//                    LOGS.d("TAG", "onItemSelected: $layoutPosition ")
//                    mSelectedValue = mSelectionList?.get(layoutPosition) ?: ""
//                    mSelectedPosition = layoutPosition
//                }
//            }
//        }

//        wheelAdapter.setOnItemSelectedListener { item ->
//            Log.d(
//                "TAG",
//                "onItemSelected: ${item.split(" ").get(0)}"
//            )
//            Log.d("TAG", "get current item ${wheelAdapter.currentItemPosition}")
//
//        }
//        wheelAdapter.bind(binding.wheelPicker)
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

            if(getString(R.string.text_download_my_data).equals(mTitle)){
                sessionManager.logMoEngageAppEvent(
                    MoEngageLunaAppEvents.pdf_download
                )
            }

            setFragmentResult(
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