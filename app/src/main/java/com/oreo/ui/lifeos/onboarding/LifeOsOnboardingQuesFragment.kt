package com.oreo.ui.lifeos.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsOnboardingQuesBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.ui.lifeos.onboarding.quesChildFrags.LifeosCheckBoxAndOtherFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LifeOsOnboardingQuesFragment : BaseFragment<FragmentLifeOsOnboardingQuesBinding>(FragmentLifeOsOnboardingQuesBinding::inflate) {

    val viewModel: LifeOsOnboardingQuesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getOnboardQues()
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {
        viewModel.curQues.observe(this){
            val childFrag = LifeosCheckBoxAndOtherFragment().apply {
                this.arguments = Bundle().apply {
                    putParcelable("question", it)
                }
            }

            childFragmentManager.beginTransaction()
                .replace(R.id.childFragmentContainer, childFrag)
                .commit()
        }
    }

}