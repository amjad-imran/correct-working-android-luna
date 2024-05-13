package com.oreo.ui.heartrate

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOHRBannerBinding
import com.noisefit_commans.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OHRBannerFragment : BaseFragment<FragmentOHRBannerBinding>(FragmentOHRBannerBinding::inflate) {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_o_h_r_banner, container, false)
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}