package com.noisefit.ui.myDevice

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentUnpairBottomDialogBinding
import com.noisefit.luna.databinding.FragmentUnpairNoDeviceDialogBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.showShortToast
import com.noisefit.watch.CallingWatchUtils
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.models.ColorFitDevice
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class UnpairDeviceNotConnectedFragment :
    BaseBottomSheetWithTransparent<FragmentUnpairNoDeviceDialogBinding>(
        FragmentUnpairNoDeviceDialogBinding::inflate
    ) {

    val args: UnpairDeviceNotConnectedFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = args.title
        binding.tvPrivacy.text = args.message

        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}