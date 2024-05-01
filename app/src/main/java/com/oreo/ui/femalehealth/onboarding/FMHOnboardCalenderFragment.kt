package com.oreo.ui.femalehealth.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.noisefit.luna.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FMHOnboardCalenderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_f_m_h_onboard_calender, container, false)
    }

}