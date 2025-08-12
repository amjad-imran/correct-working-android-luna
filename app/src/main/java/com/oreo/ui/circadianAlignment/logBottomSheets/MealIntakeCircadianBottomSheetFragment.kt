package com.oreo.ui.circadianAlignment.logBottomSheets

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.databinding.FragmentMealIntakeCircadianBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.oreo.ui.timelineScreen.addActivity.MealIntakeRvAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MealIntakeCircadianBottomSheetFragment : BaseBottomSheetWithTransparent<FragmentMealIntakeCircadianBottomSheetBinding>(FragmentMealIntakeCircadianBottomSheetBinding::inflate) {

    private val viewModel: MealIntakeBottomSheetViewmodel by viewModels()

    private val rvMealsAdapter by lazy {
        MealIntakeRvAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initMealsData()
        setRecycler()
        viewModel.updateDataSet(){
            rvMealsAdapter.updateDataSet(viewModel.dataList)
        }
    }

    private fun setRecycler() {
        binding.rvMeals.layoutManager = LinearLayoutManager(this.context)
        binding.rvMeals.adapter = rvMealsAdapter
    }

    override fun initListener() {
        rvMealsAdapter.itemClickListener = { type ->
            when(type){

                else -> {}
            }
        }
    }

    override fun subscribeObservers() {

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
                isCancelable = true
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }

}