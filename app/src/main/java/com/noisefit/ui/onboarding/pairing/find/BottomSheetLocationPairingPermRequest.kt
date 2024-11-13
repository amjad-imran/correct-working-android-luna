package com.noisefit.ui.onboarding.pairing.find

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetLocationPermBinding
import com.noisefit.luna.databinding.BottomSheetLocationPermFindMyRingBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.oreo.ui.device.BottomSheetLocationPermissionFindMyRing
import dagger.hilt.android.AndroidEntryPoint

const val LOCATION_PERM_REQUEST_PAIRING = "LOCATION_PERM_REQUEST_PAIRING"

@AndroidEntryPoint
class BottomSheetLocationPermissionRequest :
    BaseBottomSheetWithTransparent<BottomSheetLocationPermFindMyRingBinding>(
        BottomSheetLocationPermFindMyRingBinding::inflate
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = getString(R.string.text_permission_to_use_your_location)
        binding.tvDescription.text = getString(R.string.text_when_using_bluetooth_to)
    }

    override fun initListener() {
        binding.btnYes.setOnClickListener {
            setFragmentResult(
                LOCATION_PERM_REQUEST_PAIRING, bundleOf("allow" to true)
            )
            navigateUpSafe()
        }

        binding.btnNo.setOnClickListener {
            setFragmentResult(
                LOCATION_PERM_REQUEST_PAIRING, bundleOf("allow" to false)
            )
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }

}
