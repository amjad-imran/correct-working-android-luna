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

    private val adapter: LifeOsOnboardMcqSingleAdapter by lazy {
        LifeOsOnboardMcqSingleAdapter(object : OnMcqItemClicked {
            override fun onItemClick(data: AnswerX, position: Int) {
                adapter.updateItem(data, position)
                parentViewModel.setNextBtnEnableState(adapter.getSelectedValue()!=null)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ques = arguments?.getParcelable<Question>("question")
        ques?.let {
            quesId = it.id
            setUi(ques)
            setAdapter()
        }
    }

    private fun setAdapter() {
        binding.rvAnswers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapter
        }
    }

    private fun setUi(ques: Question) {
        binding.tvQues.text = ques.text
        adapter.setData(ques.answer)
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {
        parentViewModel.nextBtnClicked.observe(this) {
            if (it) {
                parentViewModel.nextBtnClicked.value = false

                val selected = adapter.getSelectedValue()
                val arrayList = ArrayList<AnswerX>()
                selected?.let { arrayList.add(selected) }
                quesId?.let { qId ->
                    parentViewModel.saveCurrentQues(
                        quesId = qId,
                        list = arrayList
                    )
                }
            }
        }
    }

}