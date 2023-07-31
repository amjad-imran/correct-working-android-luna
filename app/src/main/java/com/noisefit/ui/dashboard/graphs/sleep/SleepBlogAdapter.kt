package com.noisefit.ui.dashboard.graphs.sleep

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit_commans.data.response.SleepBlogCategories
import com.noisefit.luna.databinding.ItemAboutSleepLayoutBinding


class SleepBlogAdapter(
    private val listener: SleepBlogCardClickListener
) :
    RecyclerView.Adapter<SleepBlogAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<SleepBlogCategories>()

    inner class ViewHolder(private val binding: ItemAboutSleepLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(blog: SleepBlogCategories) {
            binding.tvTitle.text = blog.title
            Glide.with(binding.iv.context)
                .load(blog.image)
                .into(binding.iv)
            binding.root.setOnClickListener {
                listener.onSleepBlogCardClicked(blog)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemAboutSleepLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setDataSet(dataSet: List<SleepBlogCategories>) {
        mDataSet = dataSet as ArrayList<SleepBlogCategories>
        notifyDataSetChanged()
    }
}

interface SleepBlogCardClickListener {
    fun onSleepBlogCardClicked(blog: SleepBlogCategories)
}