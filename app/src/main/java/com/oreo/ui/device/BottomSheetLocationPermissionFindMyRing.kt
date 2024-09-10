package com.oreo.ui.device

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.databinding.BottomSheetLocationPermFindMyRingBinding
import com.noisefit.ui.common.bottomSheet.DELETE_REQ_REQUEST_KEY
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

const val FIND_RING_LOCATION_PERM_REQUEST = "FIND_RING_LOCATION_PERM_REQUEST"

@AndroidEntryPoint
class BottomSheetLocationPermissionFindMyRing :
    BaseBottomSheetWithTransparent<BottomSheetLocationPermFindMyRingBinding>(
        BottomSheetLocationPermFindMyRingBinding::inflate
    ) {
    val args: BottomSheetLocationPermissionFindMyRingArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initListener() {
        binding.btnYes.setOnClickListener {
            if (args.postOnActivity) {
                requireActivity().supportFragmentManager.setFragmentResult(
                    FIND_RING_LOCATION_PERM_REQUEST, bundleOf("allow" to true)
                )
            } else {
                setFragmentResult(
                    FIND_RING_LOCATION_PERM_REQUEST, bundleOf("allow" to true)
                )
            }
            navigateUpSafe()
        }
        binding.btnNo.setOnClickListener {
            if (args.postOnActivity) {
                requireActivity().supportFragmentManager.setFragmentResult(
                    FIND_RING_LOCATION_PERM_REQUEST, bundleOf("allow" to false)
                )
            } else {
                setFragmentResult(
                    FIND_RING_LOCATION_PERM_REQUEST, bundleOf("allow" to false)
                )
            }
            navigateUpSafe()
        }

    }

    override fun subscribeObservers() {

    }

}