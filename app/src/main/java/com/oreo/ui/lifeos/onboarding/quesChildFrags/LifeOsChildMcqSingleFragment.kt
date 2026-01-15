package com.oreo.ui.lifeos.onboarding.quesChildFrags

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.FragmentLifeOsChildMcqSingleBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.data.model.lifeos.onboarding.AnswerX
import com.noisefit_commans.data.model.lifeos.onboarding.Question
import com.oreo.ui.lifeos.onboarding.LifeOsOnboardingQuesViewModel
import kotlin.getValue

class LifeOsChildMcqSingleFragment : BaseFragment<FragmentLifeOsChildMcqSingleBinding>(FragmentLifeOsChildMcqSingleBinding::inflate) {

    private val parentViewModel: LifeOsOnboardingQuesViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { defaultViewModelProviderFactory }
    )

    var quesId: Int ?= null

    private val myAdapter: LifeOsOnboardMcqSingleAdapter by lazy {
        LifeOsOnboardMcqSingleAdapter(object : OnMcqItemClicked {
            override fun onItemClick(data: AnswerX, position: Int) {
                myAdapter.updateItem(data, position)
                parentViewModel.setNextBtnEnableState(myAdapter.getSelectedValue()!=null)
            }
        })
    }

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

        // set rv
        binding.rvAnswers.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
            this.adapter = myAdapter
        }
        myAdapter.setData(ques.answer)
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
        val allItems = myAdapter.getAllItems()
        quesId?.let { qId ->
            parentViewModel.saveCurrentQues(
                quesId = qId,
                list = allItems,
                isSaveAndExit = isExit
            )
        }
    }

}