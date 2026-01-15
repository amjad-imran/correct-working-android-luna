package com.oreo.ui.lifeos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemRelatedSuggQuesLifeosBinding

class RelatedSuggestedQuesAdapter(
    private val itemList: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<RelatedSuggestedQuesAdapter.MyViewHolder>() {

    inner class MyViewHolder(
        private val binding: ItemRelatedSuggQuesLifeosBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String){
            binding.tvText.text = item

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemRelatedSuggQuesLifeosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount() = itemList.size
}