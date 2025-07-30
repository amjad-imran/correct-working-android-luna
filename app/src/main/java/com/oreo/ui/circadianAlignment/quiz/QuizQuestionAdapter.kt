package com.oreo.ui.circadianAlignment.quiz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemQuestionQuizCircadianBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.circadian.CircadianQuizResponseModel

class QuizQuestionAdapter(
    val onOptionSelected: (pair: Pair<Int, Int>) -> Unit
):
    RecyclerView.Adapter<QuizQuestionAdapter.QuizQuestionViewHolder>() {

    private val mList: ArrayList<CircadianQuizResponseModel> = ArrayList()

    private var focusedIndex = 0

    fun setFocusedIndex(index: Int) {
        val old = focusedIndex
        focusedIndex = index
        notifyItemChanged(old)
        notifyItemChanged(index)
    }

    inner class QuizQuestionViewHolder(val binding: ItemQuestionQuizCircadianBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: CircadianQuizResponseModel, isFocused: Boolean) {
            val context = binding.root.context
            binding.tvQuestion.text = question.text

            if (isFocused) {
                binding.blurOverlay.gone()
                question.answer?.let { options ->
                    val optionsAdapter = QuizOptionsAdapter(question.selectedOptionId){
                        if(it?.id != null && question?.id != null){
                            question.selectedOptionId = it.id
                            onOptionSelected(Pair(question.id, it.id))
                        }
                    }

                    binding.rvOptions.layoutManager = LinearLayoutManager(context)
                    binding.rvOptions.adapter = optionsAdapter

                    optionsAdapter.updateDataSet(options)
                    binding.rvOptions.visible()
                }
            }else{
                binding.rvOptions.gone()
                binding.blurOverlay.visible()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizQuestionViewHolder {
        val binding = ItemQuestionQuizCircadianBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuizQuestionViewHolder(binding)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: QuizQuestionViewHolder, position: Int) {
        holder.bind(mList[position], position == focusedIndex)
    }

    fun updateDataSet(list: List<CircadianQuizResponseModel>){
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

}