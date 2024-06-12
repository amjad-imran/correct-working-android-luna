package com.oreo.ui.femalehealth.splash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ViewStressSplashDescriptionSliderBinding
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.StressSplashModel


class FMHSplashDescriptionAdapter :
    RecyclerView.Adapter<FMHSplashDescriptionAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<StressSplashModel>()


    inner class ViewHolder(private val binding: ViewStressSplashDescriptionSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StressSplashModel) {
            binding.imv.loadImage(binding.imv.context, data.image)
            binding.tvTitle.text = data.title
            binding.tvDescription.text = data.description
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            ViewStressSplashDescriptionSliderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataSet: ArrayList<StressSplashModel>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}