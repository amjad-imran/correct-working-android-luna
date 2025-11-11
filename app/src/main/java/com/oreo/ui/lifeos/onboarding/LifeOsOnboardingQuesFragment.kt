package com.oreo.ui.lifeos.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsOnboardingQuesBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.ui.lifeos.onboarding.quesChildFrags.LifeOsOnboardTextFldOrNoneFragment
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
        binding.tvSkip.setOnClickListener {
            viewModel.switchToNextQuestion()
        }

        binding.ivBack.setOnClickListener {
            viewModel.switchToPrevQuestion()
        }

        binding.btnNext.setOnClickListener {
            viewModel.switchToPrevQuestion()
        }
    }

    private fun setLinearProgressIndicatorUi() {
        val totalQues = viewModel.onBoardResponseData?.questions?.size
        val curProgress = viewModel.curQuesIndex?.plus(1)
        if(totalQues==null || curProgress==null){
            binding.tvSkip.gone()
            return
        }

        binding.indicatorProgress.apply {
            if(this.max != totalQues) max = totalQues
            progress = curProgress
        }
        if(totalQues==curProgress){
            binding.tvSkip.gone()
        }else{
            binding.tvSkip.visible()
        }

        if(curProgress==1){
            binding.ivBack.gone()
        }else{
            binding.ivBack.visible()
        }
    }

    override fun subscribeObservers() {
        viewModel.curQues.observe(this){

            setLinearProgressIndicatorUi()

            val childFrag = when(it.type){
                "mcq-single" -> {
                    LifeosCheckBoxAndOtherFragment().apply {
                        this.arguments = Bundle().apply {
                            putParcelable("question", it)
                        }
                    }
                }

                "mcq-multiple" -> {
                    LifeosCheckBoxAndOtherFragment().apply {
                        this.arguments = Bundle().apply {
                            putParcelable("question", it)
                        }
                    }
                }

                "picker" -> {
                    LifeOsOnboardTextFldOrNoneFragment().apply {
                        this.arguments = Bundle().apply {
                            putParcelable("question", it)
                        }
                    }
                }

                else -> null
            }

            childFrag?.let {
                childFragmentManager.beginTransaction()
                    .replace(R.id.childFragmentContainer, childFrag)
                    .commit()
            }
        }
    }

    private fun setSaveBtnState(){
        val curQues = viewModel.curQues
        if(curQues==null){
            return
        }

    }

}