package com.noisefit.ui.reward.about

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.model.RewardAboutSubCategoryList
import com.noisefit.luna.databinding.ItemAboutStreakListBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible

class AboutStreaksAdapter : RecyclerView.Adapter<AboutStreaksAdapter.ViewHolder>() {
    private var rewardAboutSubCategoryList = ArrayList<RewardAboutSubCategoryList>()

    inner class ViewHolder(val binding: ItemAboutStreakListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: RewardAboutSubCategoryList) {
            binding.tvTitle.text = resultData.title

            if (resultData.url.isNullOrEmpty()) {
                binding.imageView.gone()
            } else {
                binding.imageView.visible()
                binding.imageView.loadImage(binding.imageView.context, resultData.url)
            }
            binding.tvSubtitle.text = resultData.subtitle

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemAboutStreakListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return rewardAboutSubCategoryList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rewardAboutSubCategoryList[position])
    }

    fun setDataSet(data: List<RewardAboutSubCategoryList>) {
        rewardAboutSubCategoryList.clear()
        rewardAboutSubCategoryList.addAll(data)
        notifyDataSetChanged()
    }

}