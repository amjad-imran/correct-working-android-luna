package com.noisefit_commans.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController

import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.noisefit_commans.utils.LOGS

abstract class BaseBottomSheet<VB : ViewBinding>(
    private val inflate: Inflate<VB>
) : BottomSheetDialogFragment() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    
    fun navigate(destination: NavDirections) = with(NavHostFragment.findNavController(this)) {
        currentDestination?.getAction(destination.actionId)
            ?.let { navigate(destination) } ?: LOGS.w(
            "BaseBottomSheet",
            "Navigation is in danger. Google please save us -> BaseFragment -> navigate method"
        )
    }

    fun navigate(destination: Int,bundle: Bundle? = null) = with(NavHostFragment.findNavController(this)) {
        try {
            navigate(destination,bundle)

        } catch (e: Exception) {
            LOGS.w(
                "BaseBottomSheet",
                "Navigation with id is in danger. Google please save us -> BaseFragment -> navigate method"
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun navigateUpSafe(){
        try {
            findNavController().navigateUp()
        }catch (exp : Exception){
            LOGS.d("Safe navigate up")
        }
    }

    protected fun disableBottomSheetDraggableBehavior() {
        this.isCancelable = false
        this.dialog?.setCanceledOnTouchOutside(false)
    }



//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val bottomSheetDialog =
//            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
//        bottomSheetDialog.setOnShowListener { dia ->
//            val dialog = dia as BottomSheetDialog
//            val bottomSheet =
//                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
//            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
//                state = BottomSheetBehavior.STATE_HALF_EXPANDED
//                skipCollapsed = false
//                isHideable = true
//                isDraggable = false
//
//            }
//        }
//        return bottomSheetDialog
//    }


}