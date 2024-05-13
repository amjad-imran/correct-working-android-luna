package com.oreo.ui.recordworkout

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.databinding.BottomSheetLocationPermBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

const val LOCATION_PERM_REQUEST = "LOCATION_PERM_REQUEST"

@AndroidEntryPoint
class BottomSheetLocationPermissionRequest :
    BaseBottomSheetWithTransparent<BottomSheetLocationPermBinding>(
        BottomSheetLocationPermBinding::inflate
    ) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initListener() {
        binding.btnYes.setOnClickListener {
            setFragmentResult(
                LOCATION_PERM_REQUEST, bundleOf("allow" to true)
            )
            navigateUpSafe()
        }
        binding.btnNo.setOnClickListener {
            setFragmentResult(
                LOCATION_PERM_REQUEST, bundleOf("allow" to false)
            )
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }

}
