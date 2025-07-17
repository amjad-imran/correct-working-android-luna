package com.noisefit.ui.dashboard.summary

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.noisefit.luna.R
import com.noisefit_commans.data.model.DashboardBanner
import com.noisefit.luna.databinding.ViewImageSliderBinding


class DashboardBannerSliderAdapter(val listener: DashboardBannerAction) :
    RecyclerView.Adapter<DashboardBannerSliderAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<DashboardBanner>()

    inner class ViewHolder(private val binding: ViewImageSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: DashboardBanner, position: Int) {

            Glide.with(binding.ivSliderImage.context)
                .load(image.image_url)
                .placeholder(R.drawable.placeholder_banner)
                .error(R.drawable.placeholder_banner)
                .dontTransform()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        listener.imageLoadedSuccessfully()
                        return false
                    }

                })
                .into(binding.ivSliderImage)

            binding.ivSliderImage.setOnClickListener {
                listener.onBannerClicked(image, position + 1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ViewImageSliderBinding.inflate(
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

    fun setDataSet(dataSet: List<DashboardBanner>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}

interface DashboardBannerAction {
    fun onBannerClicked(banner: DashboardBanner, position: Int)
    fun imageLoadedSuccessfully()
}