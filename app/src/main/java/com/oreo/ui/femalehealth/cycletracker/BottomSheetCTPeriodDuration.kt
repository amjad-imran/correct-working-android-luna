package com.oreo.ui.femalehealth.cycletracker

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.BottomSheetCtPeriodDurationViewBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.utils.ScreenUtils
import com.noisefit_commans.utils.wheel.WheelAdapterPeriod
import com.noisefit_commans.utils.wheel.WheelItemPeriod
import com.oreo.ui.femalehealth.onboarding.MaxCycleDays
import com.oreo.ui.femalehealth.onboarding.MaxPeriodDays
import com.oreo.ui.femalehealth.onboarding.MinCycleDays
import com.oreo.ui.femalehealth.onboarding.MinPeriodDays
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val DURATION_LOG_SAVE = "DURATION_LOG_SAVE"

@AndroidEntryPoint
class BottomSheetCTPeriodDuration :
    BaseBottomSheetWithTransparent<BottomSheetCtPeriodDurationViewBinding>(
        BottomSheetCtPeriodDurationViewBinding::inflate
    ) {

    private var mSelectedValue: String? = null
    private var mInitialSelectedValue: String? = null
    private var mTitle: String? = null
    private var mSelectedPosition: Int = 0


    @Inject
    lateinit var screenUtils: ScreenUtils


    private val wheelAdapter: WheelAdapterPeriod<String> by lazy {
        WheelAdapterPeriod()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val args = BottomSheetCTPeriodDurationArgs.fromBundle(it)
            mTitle = args.title
            initUi(args.selectedValue)
        }

    }

    private fun initUi(selectedValue: String?) {

        prepareDaysData()
        binding.tvTitle.text = mTitle
        mSelectedValue = if (isFromPeriodDuration()) {
            if (selectedValue.isNullOrEmpty()) getPeriodDayData()[0].data else selectedValue
        } else {
            if (selectedValue.isNullOrEmpty()) getPeriodCycleDayData()[0].data else selectedValue

        }

        mInitialSelectedValue = selectedValue
        mSelectedPosition = if (selectedValue.isNullOrEmpty()) {
            0
        } else {
            if (isFromPeriodDuration())
                getSelectedPosition(selectedValue, getPeriodDayData())
            else
                getSelectedPosition(selectedValue, getPeriodCycleDayData())
        }
        setWheelPicker()
    }

    private fun setWheelPicker() {
        binding.wheelPicker.visibleItemCount = 3//it could not be less then 3
        val listData: ArrayList<WheelItemPeriod<String>> = if (isFromPeriodDuration())
            getPeriodDayData()
        else
            getPeriodCycleDayData()
        wheelAdapter.data = listData
        wheelAdapter.setOnItemSelectedListener { item ->
            //
            mSelectedValue = item
        }
        wheelAdapter.bind(binding.wheelPicker)
        val lastPosition: Int = if (isFromPeriodDuration())
            getSelectedPosition(mSelectedValue ?: "", getPeriodDayData())
        else
            getSelectedPosition(mSelectedValue ?: "", getPeriodCycleDayData())
        wheelAdapter.selectedItemPosition = lastPosition
    }

    private fun getSelectedPosition(
        selectedValue: String,
        selectionList: ArrayList<WheelItemPeriod<String>>
    ): Int {
        for ((index, item) in selectionList.withIndex()) {
            if (item.data.equals(selectedValue, true)) {
                return index
            }
        }
        return 0
    }

    private fun isFromPeriodDuration(): Boolean {
        return mTitle.equals("Period duration")
    }

    override fun initListener() {

        binding.btnDone.setOnClickListener {

            setFragmentResult(
                DURATION_LOG_SAVE,
                bundleOf("agree" to true, "selectedValue" to mSelectedValue)
            )
            navigateUpSafe()

        }
        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
            setFragmentResult(
                DURATION_LOG_SAVE,
                bundleOf("agree" to false, "selectedValue" to mSelectedValue)
            )
        }
    }

    override fun subscribeObservers() {

    }

    private var periodDays = ArrayList<Int>()
    private var periodCycleDays = ArrayList<Int>()

    private fun prepareDaysData() {
        for (i in MinPeriodDays..MaxPeriodDays) {
            periodDays.add(i)
        }
        for (i in MinCycleDays..MaxCycleDays) {
            periodCycleDays.add(i)
        }
    }


    private fun getPeriodDayData(): ArrayList<WheelItemPeriod<String>> {
        val dataSet = ArrayList<WheelItemPeriod<String>>()
        periodDays.forEach {
            if (it < 10) {
                dataSet.add(WheelItemPeriod("0$it"))
            } else {
                dataSet.add(WheelItemPeriod("$it"))
            }
        }
        return dataSet

    }

    private fun getPeriodCycleDayData(): ArrayList<WheelItemPeriod<String>> {
        val dataSet = ArrayList<WheelItemPeriod<String>>()
        periodCycleDays.forEach {
            if (it < 10) {
                dataSet.add(WheelItemPeriod("0$it"))
            } else {
                dataSet.add(WheelItemPeriod("$it"))
            }
        }
        return dataSet

    }


}