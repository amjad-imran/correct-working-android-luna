package com.noisefit.ui.workout.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowWorkoutImageBinding
import com.noisefit.ui.dashboard.summary.DashboardBannerAction

class ActivityShareImageAdapter() :
    RecyclerView.Adapter<ActivityShareImageAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<Pair<String?, Uri?>>()

    inner class ViewHolder(private val binding: RowWorkoutImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: Pair<String?, Uri?>, position: Int) {
            binding.ivBackground.destroyDrawingCache()
            binding.ivBackground.setImageURI(null)

            if (image.second != null) {
                binding.ivBackground.setImageURI(image.second)
            } else {
                Glide.with(binding.ivBackground.context)
                    .load(image.first)
                    .placeholder(R.drawable.placeholder_banner)
                    .error(R.drawable.placeholder_banner)
                    .into(binding.ivBackground)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            RowWorkoutImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setDataSet(dataSet: List<Pair<String?, Uri?>>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}