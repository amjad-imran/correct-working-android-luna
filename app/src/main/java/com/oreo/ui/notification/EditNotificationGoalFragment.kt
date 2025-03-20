package com.oreo.ui.notification

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentEditNotificationGoalBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.WheelAdapter
import com.noisefit_commans.utils.WheelItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditNotificationGoalFragment :
    BaseFragment<FragmentEditNotificationGoalBinding>(FragmentEditNotificationGoalBinding::inflate) {

    private val viewModel: EditNotificationGoalViewModel by viewModels()

    private val wheelAdapterHydrationImperial: WheelAdapter<String> by lazy {
        WheelAdapter()
    }

    private val wheelAdapterStepsPicker: WheelAdapter<String> by lazy {
        WheelAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initHydrationUi(viewModel.getHydrationGoalList(false), true)
        initStepsUi(viewModel.getStepsGoalsList())
        viewModel.getNotificationGoals()
    }

    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnSave.setOnClickListener {
            viewModel.updateNotificationGoal(
                10000,
                3000
            )
        }
    }

    override fun subscribeObservers() {
        viewModel.notificationGoalReceived.observe(this) {
            it.getContent()?.let {
                val selectedPosition = viewModel.getSelectedStepsPosition(it.steps_required ?: 3000)

                if (selectedPosition != -1) {
                    wheelAdapterStepsPicker.selectedItemPosition = selectedPosition
                }
            }
        }

        /*viewModel.notificatioGoal.observe(this){
            initHydrationUi(viewModel.getHydrationGoalList(false), true)
            initStepsUi(viewModel.getStepsGoalsList())
        }*/


        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }


    }


    private var selectedHydrationImperialValue: String? = null


    private fun initHydrationUi(
        selectionList: Pair<List<String>, List<String>>,
        isMetric: Boolean
    ) {
        val listData = ArrayList<WheelItem<String>>()
        selectionList.first.forEach {
            listData.add(WheelItem(it))
        }
        binding.lytHydrationPicker.wheelPicker.visibleItemCount = 5//it could not be less then 3
        wheelAdapterHydrationImperial.data = listData
        wheelAdapterHydrationImperial.setOnItemSelectedListener { item ->
            Log.d(
                "TAG",
                "onItemSelected: ${item.split(" ").get(0)}"
            )
            Log.d("TAG", "get current item ${wheelAdapterHydrationImperial.currentItemPosition}")
            //mSelectedValue = mSelectionList?.get(wheelAdapterHydrationImperial.currentItemPosition) ?: ""
            //mSelectedPosition = wheelAdapterHydrationImperial.currentItemPosition
        }
        wheelAdapterHydrationImperial.bind(binding.lytHydrationPicker.wheelPicker)
        /*        mSelectedValue = if (selectedValue.isNullOrEmpty()) selectionList[0] else selectedValue
                mInitialSelectedValue = selectedValue
                mSelectedPosition = if (selectedValue.isNullOrEmpty()) {
                    0
                } else {
                    getSelectedPosition(selectedValue, selectionList)
                }*/
        //wheelAdapterHydrationImperial.selectedItemPosition = mSelectedPosition
    }

    private fun initStepsUi(selectionList: List<String>) {
        val listData = ArrayList<WheelItem<String>>()
        selectionList.forEach {
            listData.add(WheelItem(it))
        }
        binding.lytStepsPicker.wheelPicker.visibleItemCount = 5//it could not be less then 3
        wheelAdapterStepsPicker.data = listData
        wheelAdapterStepsPicker.setOnItemSelectedListener { item ->
            Log.d(
                "TAG",
                "onItemSelected: ${item.split(" ").get(0)}"
            )
            Log.d("TAG", "get current item ${wheelAdapterStepsPicker.currentItemPosition}")
            //mSelectedValue = mSelectionList?.get(wheelAdapterHydrationImperial.currentItemPosition) ?: ""
            //mSelectedPosition = wheelAdapterHydrationImperial.currentItemPosition
        }
        wheelAdapterStepsPicker.bind(binding.lytStepsPicker.wheelPicker)
        /*        mSelectedValue = if (selectedValue.isNullOrEmpty()) selectionList[0] else selectedValue
                mInitialSelectedValue = selectedValue
                mSelectedPosition = if (selectedValue.isNullOrEmpty()) {
                    0
                } else {
                    getSelectedPosition(selectedValue, selectionList)
                }*/
        //wheelAdapterHydrationImperial.selectedItemPosition = mSelectedPosition
    }

    private fun getSelectedPosition(selectedValue: String, selectionList: Array<String>): Int {
        for ((index, item) in selectionList.withIndex()) {
            if (item.equals(selectedValue, true)) {
                return index
            }
        }
        return 0
    }

}