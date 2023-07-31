package com.noisefit.ui.watchface.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.noisefit_commans.models.WatchFace
import com.noisefit.databinding.RowWatchFace2Binding
import com.noisefit_commans.ui.loadImageCacheWithProgress
import com.noisefit_commans.ui.tryCatch
import com.varunest.sparkbutton.SparkEventListener

class WatchFaceAdapter(private val listener: WatchFaceActions) :
    RecyclerView.Adapter<WatchFaceAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<WatchFace>()

    inner class ViewHolder(val binding: RowWatchFace2Binding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(watchFace: WatchFace) {

            binding.ivFavourite.isChecked = watchFace.isFav()

            binding.ivWatchFace.loadImageCacheWithProgress(
                binding.ivWatchFace.context,
                watchFace.imageUrl
            )

            binding.root.setOnClickListener {
                listener.onWatchFaceClicked(watchFace.id!!, watchFace)
            }

            binding.ivFavourite.setEventListener(object : SparkEventListener {
                override fun onEvent(button: ImageView?, buttonState: Boolean) {
                    listener.onMarkFavouriteClicked(
                        buttonState,
                        watchFace,
                        bindingAdapterPosition
                    )
                }

                override fun onEventAnimationEnd(button: ImageView?, buttonState: Boolean) {}

                override fun onEventAnimationStart(button: ImageView?, buttonState: Boolean) {}

            })

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowWatchFace2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size
    fun setDataSet(list: List<WatchFace>) {
        mDataSet.clear()
        mDataSet.addAll(list)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (mDataSet.size > position) {
            mDataSet.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun resetFavState(state: Boolean, currentPosition: Int) {
        tryCatch {
            mDataSet[currentPosition].is_favourite = if (state) "1" else "0"
            notifyItemChanged(currentPosition)
        }
    }
}