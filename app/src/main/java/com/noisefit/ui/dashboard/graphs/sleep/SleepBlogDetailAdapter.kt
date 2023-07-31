package com.noisefit.ui.dashboard.graphs.sleep

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit_commans.data.response.SleepBlogSubCategories
import com.noisefit.luna.databinding.RowSleepBlogDetailBinding


class SleepBlogDetailAdapter :
    RecyclerView.Adapter<SleepBlogDetailAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<SleepBlogSubCategories>()

    inner class ViewHolder(private val binding: RowSleepBlogDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(blog: SleepBlogSubCategories) {
            binding.tvTitle.text = blog.title
            binding.tvDescription.text = blog.description
            Glide.with(binding.iv.context)
                .load(blog.image)
                .into(binding.iv)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowSleepBlogDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setDataSet(dataSet: List<SleepBlogSubCategories>) {
        mDataSet = dataSet as ArrayList<SleepBlogSubCategories>
        notifyDataSetChanged()
    }
}