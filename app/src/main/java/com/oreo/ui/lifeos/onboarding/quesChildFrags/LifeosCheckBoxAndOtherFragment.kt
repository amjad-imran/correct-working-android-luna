package com.oreo.ui.lifeos.onboarding.quesChildFrags

import androidx.fragment.app.viewModels
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.data.model.lifeos.onboarding.Question
import com.oreo.ui.lifeos.onboarding.LifeOsOnboardingQuesViewModel
import kotlin.getValue
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.FragmentLifeosCheckBoxAndOtherBinding

class LifeosCheckBoxAndOtherFragment : BaseFragment<FragmentLifeosCheckBoxAndOtherBinding>(FragmentLifeosCheckBoxAndOtherBinding::inflate) {

    private val parentViewModel: LifeOsOnboardingQuesViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { defaultViewModelProviderFactory }
    )

    private val myAdapter: AnswersWithCheckboxAdapter by lazy {
        AnswersWithCheckboxAdapter(
            onSelectionChanged = {
                parentViewModel.setNextBtnEnableState(it.find { it.isSelected }!=null)
            }
        )
    }

    var quesId: Int ?= null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ques = arguments?.getParcelable<Question>("question")
        ques?.let {
            quesId = it.id
            setUi(ques)
        }
    }

    private fun setUi(ques: Question) {
        binding.tvQues.text = ques.text

        binding.rvAnswers.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
            this.adapter = myAdapter
        }

        myAdapter.updateDataSet(ques.answer)
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {
        parentViewModel.nextBtnClicked.observe(this) {
            if (it) {
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
        val getAllItems = myAdapter.getAllData()
        quesId?.let { qId ->
            parentViewModel.saveCurrentQues(
                quesId = qId,
                list = getAllItems,
                isSaveAndExit = isExit
            )
        }
    }

}