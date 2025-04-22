package com.oreo.ui.caffeineWindowScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemCaffeineFoodBinding
import androidx.core.graphics.toColorInt

sealed class CaffeineWindowScreenClickEnum {
    data class onFavIconClicked(var position: Int): CaffeineWindowScreenClickEnum()
    data class onNotFavIconClicked(var position: Int): CaffeineWindowScreenClickEnum()
}

class ItemAdapter(
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    var itemClickListener: ((type: CaffeineWindowScreenClickEnum) -> Unit) ?= null

    private val mDataSet = ArrayList<CaffeineFoodItem>()

    private var maxQuantity = 0

    inner class ViewHolder(val binding: ItemCaffeineFoodBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCaffeineFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mDataSet.get(position)

        holder.binding.tvTitle.text = item.name

        holder.binding.tvQuantity.apply {
            text = "${item.quantity} ${item.unit}"
            if (item.quantity >= maxQuantity){
                setTextColor("#7b8085".toColorInt())
            }
        }

        holder.binding.ivFav.setImageResource(
            if (item.is_favorite!!){
                if (item.quantity >= maxQuantity) {
                    R.drawable.ic_star_rating
                    R.drawable.ic_star_border
                }else{
                    R.drawable.ic_star_rating
                }
            }
            else R.drawable.icon_unfilled_star_fav_caffeine_food
        )

        holder.binding.ivFav.setOnClickListener {
            if (item.is_favorite == true){
                itemClickListener?.invoke(CaffeineWindowScreenClickEnum.onFavIconClicked(position))
            }else{
                itemClickListener?.invoke(CaffeineWindowScreenClickEnum.onNotFavIconClicked(position))
            }
        }
    }

    override fun getItemCount() = mDataSet.size

    fun updateItems(newItems: List<CaffeineFoodItem>) {
        mDataSet.clear()
        mDataSet.addAll(newItems)
        notifyDataSetChanged()
    }

    fun updateSingleItem(item: CaffeineFoodItem?, idx: Int){
        if (item == null){
            mDataSet.removeAt(idx)
            notifyItemRemoved(idx)
            notifyItemRangeChanged(idx, mDataSet.size - idx)
        }else{
            mDataSet.add(item)
            notifyItemInserted(mDataSet.size-1)
        }
    }

    fun getItemsList(): ArrayList<CaffeineFoodItem> = mDataSet
    fun getItemsListSize(): Int = mDataSet.size

    fun setMaxQuantity(num: Int){
        maxQuantity = num
    }
}