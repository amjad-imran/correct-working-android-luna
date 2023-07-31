package com.noisefit.ui.friends

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit.databinding.RowTabsBinding
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible

class FriendsTabAdapter(val dataSet: List<String>, val listener: TabActions) :
    RecyclerView.Adapter<FriendsTabAdapter.ViewHolder>() {
    var selectedPosition: Int = 0

    inner class ViewHolder(val binding: RowTabsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            if (selectedPosition == bindingAdapterPosition) {
                binding.tvTabTitle.setTextColor(binding.tvTabTitle.context.getColor(R.color.accent_color_purple))
                binding.vBottom.visible()
            } else {
                binding.tvTabTitle.setTextColor(Color.parseColor("#a3ffffff"))
                binding.vBottom.invisible()
            }
            binding.tvTabTitle.text = title

            binding.tvTabTitle.setOnClickListener {
                listener.onTabClicked(bindingAdapterPosition, binding.tvTabTitle.text.toString())
                selectedPosition = bindingAdapterPosition
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendsTabAdapter.ViewHolder {
        val binding =
            RowTabsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendsTabAdapter.ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

}

interface TabActions {
    fun onTabClicked(position: Int, text: String)
}