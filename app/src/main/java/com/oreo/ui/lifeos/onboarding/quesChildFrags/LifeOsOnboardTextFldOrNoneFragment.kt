package com.oreo.ui.lifeos.onboarding.quesChildFrags

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.noisefit.luna.databinding.FragmentLifeOsOnboardTextFldOrNoneBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.hideKeyboard
import com.oreo.data.model.lifeos.onboarding.Question
import com.oreo.ui.lifeos.onboarding.LifeOsOnboardingQuesViewModel
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
        binding.tvNoIssues.text = ques.answer.firstOrNull()?.text
    }

    override fun initListener() {
        // When user taps the "No, I have nothing to report"
        val tvNoIssues = binding.tvNoIssues
        val etAnswer = binding.etAnswer
        tvNoIssues.setOnClickListener {
            tvNoIssuesSelected = !tvNoIssuesSelected
            tvNoIssues.isSelected = tvNoIssuesSelected

            if (tvNoIssues.isSelected) {
                // Clear text and hide keyboard
                etAnswer.setText("")
                etAnswer.clearFocus()
                etAnswer.hideKeyboard()
            }
        }

        // When user focuses/clicks the text field, unselect the second option
        etAnswer.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && tvNoIssuesSelected) {
                tvNoIssuesSelected = false
                tvNoIssues.isSelected = false
            }
        }

        etAnswer.setOnClickListener {
            if (tvNoIssuesSelected) {
                tvNoIssuesSelected = false
                tvNoIssues.isSelected = false
            }
        }
    }

    override fun subscribeObservers() {

    }

}