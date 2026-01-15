package com.oreo.ui.lifeos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemLifeOsQuestionBinding

class LifeOsQuestionAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<LifeOsQuestionAdapter.ViewHolder>() {

    private val items = ArrayList<String>()

    inner class ViewHolder(val binding: ItemLifeOsQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.tvText.text = text
            binding.root.setOnClickListener { onClick(text) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLifeOsQuestionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submit(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}

