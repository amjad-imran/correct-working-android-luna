package com.noisefit.ui.dashboard.feature.qrPayment.bottomsheets

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetQrInfoBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetQrInfo :
    BaseBottomSheetWithTransparent<BottomSheetQrInfoBinding>(BottomSheetQrInfoBinding::inflate) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL,com.noisefit_commans.R.style.DialogStyle)
    }

    override fun initListener() {

        binding.btnSave.setOnClickListener {
            navigateUpSafe()
        }
    }

    override fun subscribeObservers() {

    }
}