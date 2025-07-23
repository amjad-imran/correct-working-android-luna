package com.oreo.ui.circadianAlignment.quiz

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemQuestionQuizCircadianBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.QuizQuestionCircadianDataModel

class QuizQuestionAdapter(
    val quesClickListener: (ques: QuizQuestionCircadianDataModel) -> Unit
):
    RecyclerView.Adapter<QuizQuestionAdapter.QuizQuestionViewHolder>() {

    private val mList: ArrayList<QuizQuestionCircadianDataModel> = ArrayList()

    private var focusedIndex = 0

    fun setFocusedIndex(index: Int) {
        val old = focusedIndex
        focusedIndex = index
        notifyItemChanged(old)
        notifyItemChanged(index)
    }

    inner class QuizQuestionViewHolder(val binding: ItemQuestionQuizCircadianBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: QuizQuestionCircadianDataModel, isFocused: Boolean) {
            binding.tvQuestion.text = question.text

            binding.optionsContainer.removeAllViews()
            if (isFocused) {
                binding.blurOverlay.gone()
                question.options.forEach { option ->
                    val btn = Button(itemView.context).apply {
                        text = option
                        setOnClickListener {
                            quesClickListener(question)
                        }
                    }
                    binding.optionsContainer.addView(btn)
                }
            }else{
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

    fun updateDataSet(list: List<QuizQuestionCircadianDataModel>){
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

}