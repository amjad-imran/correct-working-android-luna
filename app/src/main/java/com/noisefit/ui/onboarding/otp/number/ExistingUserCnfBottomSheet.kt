package com.noisefit.ui.onboarding.otp.number

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.R
import com.noisefit.databinding.BottomSheetExistingUserBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.loadImage
import dagger.hilt.android.AndroidEntryPoint

const val EXISTING_USER_KEY = "EXISTING_USER_KEY"

@AndroidEntryPoint
class ExistingUserCnfBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetExistingUserBinding>(BottomSheetExistingUserBinding::inflate) {
    val args: ExistingUserCnfBottomSheetArgs by navArgs()
    private var imageUrl: String? = null
    private var email: String? = null
    private var mobile: String? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageUrl = args.imageUrl
        email = args.email
        mobile = args.mobileNumber
        initListener()
    }

    override fun initListener() {
        binding.tvMessage.text =
            "You already have a NoiseFit account with ${mobile}. Do you want to login to your existing account?"
        binding.ivProfile.loadImage(binding.ivProfile.context,imageUrl, R.drawable.ic_default_profile_image)
        binding.tvEmail.text=email

        binding.btnCancel.setOnClickListener {
            setFragmentResult(
                EXISTING_USER_KEY,
                bundleOf("isSelected" to false)
            )
            dismiss()
        }
        binding.btnYes.setOnClickListener {
            setFragmentResult(
                EXISTING_USER_KEY,
                bundleOf("isSelected" to true)
            )
            dismiss()
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
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog

    }

}