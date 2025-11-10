package com.oreo.ui.lifeos.onboarding.quesChildFrags

import androidx.fragment.app.viewModels
import com.noisefit_commans.ui.BaseFragment
import com.oreo.data.model.lifeos.onboarding.Question
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ques = arguments?.getParcelable<Question>("question")
        ques?.let {
            setUi(ques)
        }
    }

    private fun setUi(ques: Question) {
        binding.tvQues.text = ques.text

        val ansAdapter = AnswersWithCheckboxAdapter(
            onSelectionChanged = {
                parentViewModel.saveSelectedItems(it)
            }
        )

        binding.rvAnswers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ansAdapter
        }

        ansAdapter.updateDataSet(ques.answer)
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}