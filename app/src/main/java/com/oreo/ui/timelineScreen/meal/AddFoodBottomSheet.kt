package com.oreo.ui.timelineScreen.meal

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.BottomSheetAddFoodBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent


class AddFoodBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetAddFoodBinding>(
        BottomSheetAddFoodBinding::inflate
    ) {

    interface Callback {
        fun onFoodAdded(name: String, calories: Int)
    }

    var callback: Callback? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSave.setOnClickListener {
            val name = binding.etFoodItem.text?.toString()?.trim().orEmpty()
            val calories = binding.etCalories.text?.toString()?.trim().orEmpty()
            if (name.isNotEmpty()) {
                val calories = if (calories.isEmpty()) {
                    0
                } else {
                    calories.toIntOrNull() ?: 0
                }
                callback?.onFoodAdded(name, calories)
                dismiss()
            }
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}

