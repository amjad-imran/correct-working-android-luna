package com.noisefit.ui.content.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.SubCategoriesList
import com.noisefit.data.model.VideoCategoriesList
import com.noisefit.data.model.VideosList
import com.noisefit.databinding.ItemDashboardWorkoutBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible

class WorkoutAdapter(val listener: OnWorkoutClickListener) :
    RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<VideoCategoriesList>()

    inner class ViewHolder(val binding: ItemDashboardWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: VideoCategoriesList) {
            binding.tvTitle.text = resultData.title

            if (resultData.subCategories.isNullOrEmpty()) {
                binding.rvCategories.gone()
            } else {
                binding.rvCategories.visible()
                val wSubCategoryAdapter =
                    WSubCategoryAdapter(object : WSubCategoryAdapter.OnCategoryItemClickListener {
                        override fun onItemClick(result: SubCategoriesList, position: Int) {
                            resultData.defaultSelectedPosition = position
                            listener.onItemSubCategoriesClick(result, bindingAdapterPosition)
                        }
                    })
                binding.rvCategories.layoutManager = LinearLayoutManager(
                    binding.rvCategories.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                binding.rvCategories.adapter = wSubCategoryAdapter
                wSubCategoryAdapter.setDataSet(resultData.subCategories,resultData.defaultSelectedPosition)
            }
            if (resultData.videos.isNullOrEmpty()) {
                binding.rvCategoriesData.gone()
            } else {
                val wSubCategoryDataAdapter = WSubCategoryDataAdapter(object :
                    WSubCategoryDataAdapter.OnItemVideoClickListener {
                    override fun onVideoItemClick(resultData: VideosList) {
                        listener.onISCatVideoClick(resultData)
                    }

                })
                binding.rvCategoriesData.layoutManager = LinearLayoutManager(
                    binding.rvCategoriesData.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                binding.rvCategoriesData.adapter = wSubCategoryDataAdapter
                wSubCategoryDataAdapter.setDataSet(resultData.videos)
            }

            if (bindingAdapterPosition == mDataSet.size - 1) {
                binding.divider1.root.gone()
            } else {
                binding.divider1.root.visible()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemDashboardWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(result: List<VideoCategoriesList>) {
        mDataSet.clear()
        mDataSet.addAll(result)
        notifyDataSetChanged()
    }

    fun updateSubCategoryData(videos: List<VideosList>, position: Int) {
        tryCatch {
            (mDataSet[position].videos as? ArrayList)?.clear()
            (mDataSet[position].videos as? ArrayList)?.addAll(videos)
            notifyItemChanged(position)
        }
    }


    interface OnWorkoutClickListener {
        fun onItemSubCategoriesClick(resultData: SubCategoriesList, position: Int)
        fun onISCatVideoClick(resultData: VideosList)
    }
}