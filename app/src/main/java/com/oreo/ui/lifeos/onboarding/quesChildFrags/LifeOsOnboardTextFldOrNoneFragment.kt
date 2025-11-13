package com.oreo.ui.lifeos.onboarding.quesChildFrags

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLifeOsOnboardTextFldOrNoneBinding
import com.noisefit_commans.data.model.lifeos.onboarding.AnswerX
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.hideKeyboard
import com.noisefit_commans.data.model.lifeos.onboarding.Question
import com.noisefit_commans.utils.LOGS
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
    private var quesId: Int ?= null
    private var ansX: AnswerX ?= null
    private var textFieldAnsX: AnswerX ?= null

    private var allAns: List<AnswerX> ?= null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ques = arguments?.getParcelable<Question>("question")
        ques?.let {
            LOGS.d("sacaknscac : $ques")
            quesId = it.id
            allAns = it.answer
            setUi(ques)
        }
    }

    private fun setUi(ques: Question) {
        binding.tvQues.text = ques.text
        ques.answer.find { it.text.isNotEmpty() == true }?.let {
            ansX = it
            binding.tvNoIssues.text = it.text
            tvNoIssuesSelected = it.isSelected
        }

        ques.answer.find { it.text.isEmpty() }?.let {
            textFieldAnsX = it
            if(it.addOntext.isNotEmpty()){
                textFieldAnsX?.isSelected = true
                binding.etAnswer.setText(it.userInputText)
            }

            if(!it.userInputText.isNullOrEmpty()){
                textFieldAnsX?.isSelected = true
                binding.etAnswer.setText(it.userInputText)
            }
        }

        if(ansX?.isSelected == true || textFieldAnsX?.isSelected == true){
            parentViewModel.setNextBtnEnableState(true)
        }

        funSetNoneAnsBg()
    }

    override fun initListener() {

        val tvNoIssues = binding.tvNoIssues
        val etAnswer = binding.etAnswer
        binding.tvNoIssues.setOnClickListener {
            tvNoIssuesSelected = !tvNoIssuesSelected
            tvNoIssues.isSelected = tvNoIssuesSelected
            funSetNoneAnsBg()
            ansX?.isSelected = tvNoIssuesSelected

            if (tvNoIssues.isSelected) {
                // Clear text and hide keyboard
                etAnswer.setText("")
                etAnswer.clearFocus()
                etAnswer.hideKeyboard()
            }

            lifecycleScope.launch {
                delay(100L)
                parentViewModel.setNextBtnEnableState(true)
                val currentQ = parentViewModel.curQues.value
                currentQ?.let {
                    textFieldAnsX?.userInputText = null
                    textFieldAnsX?.isSelected = false
                    parentViewModel.saveCurrentQues(
                        it.id,
                        allAns
                    )
                }
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
            binding.tvNoIssues.setBackgroundResource(R.drawable.bg_mcq_selected_lifeos_inboard)
        }else{
            binding.tvNoIssues.setBackgroundResource(R.drawable.bg_mcq_lifeos_onboard)
        }
    }

    override fun subscribeObservers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString()
                if(input.isNullOrEmpty().not()){
                    textFieldAnsX?.userInputText = input
                    textFieldAnsX?.isSelected = true
                    parentViewModel.setNextBtnEnableState(true)

                    ansX?.isSelected = false
                    tvNoIssuesSelected = false
                    funSetNoneAnsBg()
                }else{
                    parentViewModel.setNextBtnEnableState(ansX?.isSelected ?: false)
                    textFieldAnsX?.isSelected = false
                }
            }
        }
        binding.etAnswer.addTextChangedListener(watcher)

        parentViewModel.nextBtnClicked.observe(this){
            if(it){
                parentViewModel.nextBtnClicked.value = false
                performSave(false)
            }
        }

        parentViewModel.saveAndExitBtnClickedBs.observe(this) {
            if (it) {
                parentViewModel.saveAndExitBtnClickedBs.value = false
                performSave(true)
            }
        }
    }

    private fun performSave(isExit: Boolean){
        if(ansX?.isSelected==true || textFieldAnsX?.isSelected==true /*input.isNotEmpty() && quesId != null && textFieldAnsX!=null*/){
            parentViewModel.saveCurrentQues(
                quesId = quesId!!,
                list = allAns,
                isSaveAndExit = isExit
            )
        }
    }

}