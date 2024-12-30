package com.oreo.ui.chatGpt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowSuggestionChipBinding

class SuggestionAdapter(val dataSet: List<String>, val onQuesClicked: (ques: String) -> Unit) :
    RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RowSuggestionChipBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {
            binding.tvQues.text = data
            binding.root.setOnClickListener {
                onQuesClicked(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            RowSuggestionChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

}