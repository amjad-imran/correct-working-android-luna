package com.oreo.ui.sleep2.sleepplanner.planner

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.data.model.SAGoalDataModel
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetGoalBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

const val SA_GOAL = "SA_GOAL"

@AndroidEntryPoint
class BottomSheetSetYourGoal : BaseBottomSheetWithTransparent<BottomSheetGoalBinding>(
    BottomSheetGoalBinding::inflate
) {
    private val viewModel: SleepGoalViewModel by viewModels()

    private val goalAdapter: SAGoalAdapter by lazy {
        SAGoalAdapter(object : OnGoalItemClick {
            override fun onItemClick(data: SAGoalDataModel, position: Int) {
                viewModel.newSelectedGoalKey = data.key
                binding.lytSetGoalView.btnSave.isEnabled = true
            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytSetGoalView.btnSave.isEnabled = false
        setRecycler()
        viewModel.getSelectedGoal()
    }

    private fun setRecycler() {
        with(binding.lytSetGoalView.rvFlow) {
            adapter = goalAdapter
        }
        goalAdapter.setData(prepareData())
    }


    override fun initListener() {
        binding.lytSetGoalView.btnSave.setOnClickListener {
            viewModel.updateGoal()
        }
        binding.lytGoalSetDone.btnDone.setOnClickListener {
            setFragmentResult(
                SA_GOAL,
                bundleOf("reload" to true)
            )
            navigateUpSafe()
        }
        binding.lytSetGoalView.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

        viewModel.userSelectedGoal.observe(this) {
            it.getContent()?.let { goal ->
                if (goal.isNotEmpty()) {
                    val returnVal = goalAdapter.selectByKey(goal)
                    if (returnVal) {
                        binding.lytSetGoalView.btnSave.isEnabled = true
                    }
                }
            }
        }

        viewModel.goalsUpdated.observe(this) {
            it.getContent()?.let {
                binding.lytSetGoalView.root.gone()
                binding.lytGoalSetDone.root.visible()
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }
    }

    private fun prepareData(): ArrayList<SAGoalDataModel> {
        val dataList = ArrayList<SAGoalDataModel>()
        dataList.add(
            SAGoalDataModel(
                title = getString(R.string.text_recover_sleep_debt),
                false,
                "sleep_debt"
            )
        )
        dataList.add(
            SAGoalDataModel(
                title = getString(R.string.text_establish_consistency),
                false,
                "weekly_consistency"
            )
        )
        return dataList
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
                isDraggable = false
                isCancelable = false
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }
}