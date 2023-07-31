package com.noisefit.ui.settings.helpAndSupport.details

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.noisefit.luna.databinding.ItemHSImageListBinding


class HelpAndSupportImageAdapter() : RecyclerView.Adapter<HelpAndSupportImageAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<String>()

    inner class ViewHolder(private val binding: ItemHSImageListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(value: String) {

            val circularProgressDrawable = CircularProgressDrawable(binding.imageView.context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            circularProgressDrawable.start()

            //binding.ivWatchFace.loadImage(binding.ivWatchFace.context,watchFace.imageUrl)

            Glide.with(binding.imageView.context)
                .load(value)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(circularProgressDrawable)
                .into(binding.imageView)

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HelpAndSupportImageAdapter.ViewHolder {
        val binding = ItemHSImageListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HelpAndSupportImageAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataList: List<String>) {
        mDataSet.clear()
        mDataSet.addAll(dataList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size


}
