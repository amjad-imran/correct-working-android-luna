package com.oreo.ui.timelineScreen.meal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetAddFoodBinding

class AddFoodBottomSheet : BottomSheetDialogFragment() {

    interface Callback {
        fun onFoodAdded(name: String)
    }

    private var _binding: BottomSheetAddFoodBinding? = null
    private val binding get() = _binding!!

    var callback: Callback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveFood.setOnClickListener {
            val name = binding.etFoodName.text?.toString()?.trim().orEmpty()
            if (name.isNotEmpty()) {
                callback?.onFoodAdded(name)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

