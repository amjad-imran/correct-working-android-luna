package com.oreo.ui.lifeos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.FragmentLifeOsInsightCardBinding
import com.oreo.data.model.lifeos.dashModels.InsightItemResponseModel

class LifeOsInsightListAdapter(
    private val onClick: (InsightItemResponseModel) -> Unit
) : RecyclerView.Adapter<LifeOsInsightListAdapter.ViewHolder>() {

    private val items = ArrayList<InsightItemResponseModel>()

    inner class ViewHolder(val binding: FragmentLifeOsInsightCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: InsightItemResponseModel) {
            binding.tvTitle.text = data.title
            binding.tvTime.text = "4 hrs ago"
            binding.root.setOnClickListener { onClick(data) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            FragmentLifeOsInsightCardBinding.inflate(inflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submit(list: List<InsightItemResponseModel>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }
}
