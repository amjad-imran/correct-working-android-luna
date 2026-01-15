package com.oreo.ui.lifeos

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemAiWhatsNewBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import io.noties.markwon.Markwon

class LifeOsWhatsNewAdapter : RecyclerView.Adapter<LifeOsWhatsNewAdapter.ViewHolder>() {

    private val items = ArrayList<String>()

    inner class ViewHolder(val binding: ItemAiWhatsNewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {

            val markwon = Markwon.create(this.binding.tvTitle.context)
            markwon.setMarkdown(binding.tvTitle, text)

            if (bindingAdapterPosition == items.size - 1) {
                binding.lytDivider.root.invisible()
            } else {
                binding.lytDivider.root.visible()
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
    }

    override fun getItemCount(): Int = items.size

    fun submit(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}

