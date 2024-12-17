package com.oreo.ui.chatGpt

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowSuggestedAiQuesBinding
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.SuggestedAiQuestions

class SuggestedQuestionAdapter(val onQuestionClicked: (SuggestedAiQuestions) -> Unit) :
    RecyclerView.Adapter<SuggestedQuestionAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<SuggestedAiQuestions>()

    inner class ViewHolder(val binding: RowSuggestedAiQuesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(suggestedAiQuestions: SuggestedAiQuestions) {
            binding.tvQues.text = suggestedAiQuestions.ques
            binding.imageBg.loadImage(binding.imageBg.context, suggestedAiQuestions.bg_image)
            binding.ivQuesIcon.loadImage(binding.ivQuesIcon.context, suggestedAiQuestions.icon)
            binding.tvQues.setTextColor(Color.parseColor(suggestedAiQuestions.color))
            binding.root.setOnClickListener {
                onQuestionClicked(suggestedAiQuestions)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowSuggestedAiQuesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataSet: List<SuggestedAiQuestions>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }


}