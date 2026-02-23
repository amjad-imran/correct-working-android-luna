package com.oreo.ui.lifeos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit.luna.databinding.ItemAiWhatsNewBinding
import com.oreo.data.model.WhatsNewSection

class LifeOsWhatsNewAdapter(val onClick: (Int) -> Unit) : RecyclerView.Adapter<LifeOsWhatsNewAdapter.ViewHolder>() {
    private val items = ArrayList<WhatsNewSection>()

    inner class ViewHolder(val binding: ItemAiWhatsNewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WhatsNewSection) {
            binding.apply {
                tvCardTitle.text = item.title
                tvCardSubtitle.text = item.description
                Glide.with(root.context)
                    .load(item.imageUrl)
                    .into(ivCardBackground)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAiWhatsNewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            onClick.invoke(position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submit(list: List<WhatsNewSection>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}

