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
        fun onFoodAdded(name: String)
    }

    var callback: Callback? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSave.setOnClickListener {
            val name = binding.etFoodItem.text?.toString()?.trim().orEmpty()
            if (name.isNotEmpty()) {
                callback?.onFoodAdded(name)
                dismiss()
            }
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }
}

