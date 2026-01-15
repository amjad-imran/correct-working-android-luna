package com.oreo.ui.chatGpt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowSuggestionChipBinding
import com.oreo.data.model.ai.TopQuestions

class SuggestionAdapter(val onQuesClicked: (ques: String) -> Unit) :
    RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<TopQuestions>()

    inner class ViewHolder(val binding: RowSuggestionChipBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: TopQuestions) {
            binding.tvQues.text = data.question
            binding.root.setOnClickListener {
                onQuesClicked(data.question?:"")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            RowSuggestionChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(it: List<TopQuestions>) {
        mDataSet.clear()
        mDataSet.addAll(it)
        notifyDataSetChanged()

    }

}