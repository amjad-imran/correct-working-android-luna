package com.noisefit.ui.content.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.data.model.SubCategoriesList
import com.noisefit.luna.databinding.ItemWcCategoryBinding

class WSubCategoryAdapter(val listener: OnCategoryItemClickListener) :
    RecyclerView.Adapter<WSubCategoryAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<SubCategoriesList>()
    var selectedSubCategoryPosition: Int = 0

    inner class ViewHolder(val binding: ItemWcCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: SubCategoriesList) {
            binding.tvTitle.text = result.title
            binding.root.setOnClickListener {
                listener.onItemClick(result, bindingAdapterPosition)
            }
            if (selectedSubCategoryPosition == bindingAdapterPosition) {
                binding.tvTitle.setBackgroundResource(R.drawable.back_primary_button_selected)
                binding.tvTitle.setTextColor(binding.tvTitle.context.getColor(R.color.white))
            } else {
                binding.tvTitle.setBackgroundResource(R.drawable.back_modal_onb_progress)
                binding.tvTitle.setTextColor(binding.tvTitle.context.getColor(R.color.white_80))
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemWcCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])

    }

    fun setDataSet(result: List<SubCategoriesList>, defaultSelectedPosition: Int) {
        selectedSubCategoryPosition = defaultSelectedPosition
        mDataSet.clear()
        mDataSet.addAll(result)
        notifyDataSetChanged()
    }

    interface OnCategoryItemClickListener {
        fun onItemClick(result: SubCategoriesList, position: Int)
    }
}