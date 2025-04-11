package com.oreo.ui.caffeineWindowScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemCaffeineFoodBinding
import androidx.core.graphics.toColorInt

class ItemAdapter(
    private val listener: ItemClickListener
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    private val mDataSet = ArrayList<CaffeineFoodItem>()

    private val maxQuantity = 40

    inner class ViewHolder(val binding: ItemCaffeineFoodBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCaffeineFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mDataSet[position]
        holder.binding.tvTitle.text = item.name

        holder.binding.tvQuantity.apply {
            text = "${item.quantity} ${item.unit}"
            if (item.quantity >= maxQuantity){
                setTextColor("#7b8085".toColorInt())
            }
        }

        holder.binding.ivFav.setImageResource(
            if (item.is_favourite) R.drawable.ic_star_rating
            else R.drawable.icon_unfilled_star_fav_caffeine_food
        )

        holder.binding.ivFav.setOnClickListener {
//            onFavoriteClick(item)
        }
    }

    override fun getItemCount() = mDataSet.size

    fun updateItems(newItems: List<CaffeineFoodItem>) {
        mDataSet.clear()
        mDataSet.addAll(newItems)
        notifyDataSetChanged()
    }
}

interface ItemClickListener{
    fun onItemStateChanged(position: Int)
}