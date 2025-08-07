package com.oreo.ui.circadianAlignment.quiz

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.FragmentQuesVpBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.circadian.CircadianQuizResponseModel

class QuesVpFragment(
    private val question: CircadianQuizResponseModel,
    private val onOptionSelected: (Pair<Int, Int>)->Unit,
) : BaseFragment<FragmentQuesVpBinding>(FragmentQuesVpBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lytQues.tvQuestion.text = question.text
    }

    override fun onResume() {
        super.onResume()
        val optionsAdapter = QuizOptionsAdapter(question.selectedOptionId){
            if(it.id != null && question.id != null){
                question.selectedOptionId = it.id
                onOptionSelected(Pair(question.id, it.id))
            }
        }

        binding.lytQues.rvOptions.layoutManager = LinearLayoutManager(context)
        binding.lytQues.rvOptions.adapter = optionsAdapter

        optionsAdapter.updateDataSet(question.answer?: ArrayList())
        binding.lytQues.rvOptions.visible()
    }

    override fun onPause() {
        super.onPause()
        binding.lytQues.rvOptions.gone()
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {

    }

}