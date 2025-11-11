package com.oreo.ui.lifeos.onboarding.quesChildFrags

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsOnboardTextFldOrNoneBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.hideKeyboard
import com.oreo.data.model.lifeos.onboarding.Question
import com.oreo.ui.lifeos.onboarding.LifeOsOnboardingQuesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class LifeOsOnboardTextFldOrNoneFragment : BaseFragment<FragmentLifeOsOnboardTextFldOrNoneBinding>(FragmentLifeOsOnboardTextFldOrNoneBinding::inflate) {

    private val parentViewModel: LifeOsOnboardingQuesViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { defaultViewModelProviderFactory }
    )

    private var tvNoIssuesSelected = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ques = arguments?.getParcelable<Question>("question")
        ques?.let {
            setUi(ques)
        }
    }

    private fun setUi(ques: Question) {
        binding.tvQues.text = ques.text
        binding.tvNoIssues.text = ques.answer?.find { it.text?.isNotEmpty() == true }?.text
    }

    override fun initListener() {

        val tvNoIssues = binding.tvNoIssues
        val etAnswer = binding.etAnswer
        binding.lytNoIssues.setOnClickListener {
            tvNoIssuesSelected = !tvNoIssuesSelected
            tvNoIssues.isSelected = tvNoIssuesSelected
            funSetNoneAnsBg()

            if (tvNoIssues.isSelected) {
                // Clear text and hide keyboard
                etAnswer.setText("")
                etAnswer.clearFocus()
                etAnswer.hideKeyboard()
            }

            lifecycleScope.launch {
                delay(100L)
                parentViewModel.switchToNextQuestion()
            }
        }

        // When user focuses/clicks the text field, unselect the second option
        etAnswer.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && tvNoIssuesSelected) {
                tvNoIssuesSelected = false
                tvNoIssues.isSelected = false
                funSetNoneAnsBg()
            }
        }

        etAnswer.setOnClickListener {
            if (tvNoIssuesSelected) {
                tvNoIssuesSelected = false
                tvNoIssues.isSelected = false
                funSetNoneAnsBg()
            }
        }
    }

    private fun funSetNoneAnsBg(){
        if(tvNoIssuesSelected){
            binding.lytNoIssues.setBackgroundResource(R.drawable.bg_mcq_selected_lifeos_inboard)
        }else{
            binding.lytNoIssues.setBackgroundResource(R.drawable.bg_mcq_lifeos_onboard)
        }
    }

    override fun subscribeObservers() {

    }

}