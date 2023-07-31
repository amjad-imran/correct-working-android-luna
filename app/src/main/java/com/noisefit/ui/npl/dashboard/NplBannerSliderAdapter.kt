package com.noisefit.ui.npl.dashboard

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
import com.noisefit.luna.databinding.ViewImageSliderBinding


class NplBannerSliderAdapter(val listener: NplBannerAction) :
    RecyclerView.Adapter<NplBannerSliderAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<String>()

    inner class ViewHolder(private val binding: ViewImageSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: String, position: Int) {

            Glide.with(binding.ivSliderImage.context)
                .load(image)
                .placeholder(R.drawable.placeholder_banner)
                .error(R.drawable.placeholder_banner)
                .dontTransform()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        listener.imageLoadedSuccessfully()
                        return false
                    }

                })
                .into(binding.ivSliderImage)


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

    fun setDataSet(dataSet: List<String>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}

interface NplBannerAction {

    fun imageLoadedSuccessfully()
}