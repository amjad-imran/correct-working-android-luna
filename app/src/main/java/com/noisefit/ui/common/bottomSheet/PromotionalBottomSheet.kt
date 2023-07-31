package com.noisefit.ui.common.bottomSheet

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import com.noisefit.databinding.FragmentPromotionalBottomSheetBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PromotionalBottomSheet :
    BaseBottomSheetWithTransparent<FragmentPromotionalBottomSheetBinding>(
        FragmentPromotionalBottomSheetBinding::inflate
    ) {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        //Code here


    }

    override fun initListener() {


    }

    override fun subscribeObservers() {

    }

}
