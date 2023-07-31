package com.noisefit.ui.npl.summary

import android.os.Bundle
import android.view.View
import com.noisefit.R
import com.noisefit.databinding.FragmentNplLostBinding
import com.noisefit.databinding.FragmentPredictionBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.InsiderAppEvents

class NplLostFragment :
    BaseFragment<FragmentNplLostBinding>(FragmentNplLostBinding::inflate) {


    companion object {
        @JvmStatic
        fun newInstance() =
            NplLostFragment()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun initListener() {
        binding.root.setOnClickListener {
            navigate(R.id.nplDashboardFragment)
        }
    }

    override fun subscribeObservers() {


    }


}