package com.noisefit.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit_commans.data.model.ShopCategory
import com.noisefit.databinding.RowProductCategoryBinding
import com.noisefit_commans.ui.getCircleProgressDrawable

class CategoriesAdapter(val listener: CategoryAction) :
    RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<ShopCategory>()

    inner class ViewHolder(val binding: RowProductCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(shopCategory: ShopCategory) {
            Glide.with(binding.ivMain.context)
                .load(shopCategory.img)
                .placeholder(getCircleProgressDrawable(binding.ivMain.context))
                .into(binding.ivMain)

            binding.tvCategoryName.text = shopCategory.title


            binding.root.setOnClickListener {
                listener.onCategoryClicked(shopCategory)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowProductCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(it: List<ShopCategory>) {
        mDataSet.clear()
        mDataSet.addAll(it)
        notifyDataSetChanged()
    }
}

interface CategoryAction {
    fun onCategoryClicked(shopCategory: ShopCategory)
}