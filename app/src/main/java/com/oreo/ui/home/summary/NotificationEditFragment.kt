package com.oreo.ui.home.summary

import android.os.Bundle
import android.view.View
import com.noisefit.luna.databinding.FragmentNotificationEditBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationEditFragment :
    BaseFragment<FragmentNotificationEditBinding>(FragmentNotificationEditBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }


}