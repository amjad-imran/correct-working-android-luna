package com.oreo.ui.lifeos.onboarding

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsOnboardingQuesBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.oreo.ui.lifeos.onboarding.quesChildFrags.LifeOsOnboardTextFldOrNoneFragment
import com.oreo.ui.lifeos.onboarding.quesChildFrags.LifeosCheckBoxAndOtherFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LifeOsOnboardingQuesFragment : BaseFragment<FragmentLifeOsOnboardingQuesBinding>(FragmentLifeOsOnboardingQuesBinding::inflate) {

    val viewModel: LifeOsOnboardingQuesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {

            }
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

        binding.btnClose.setOnClickListener {

            setFragmentResultListener(LIFEOS_ONBOARD_BS_KEY) { _, bundle ->
                val isSaveAndExit = bundle.getBoolean("saveAndExit")
                if(isSaveAndExit){

                }
            }
            navigate(R.id.lifeOSOnboardSkipOrConBottomSheet)
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
            binding.tvSkip.invisible()
            binding.btnNext.text = getString(R.string.text_done)
        }else{
            binding.tvSkip.visible()
            binding.btnNext.text = getString(R.string.text_next)
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

                "text-none" -> {
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