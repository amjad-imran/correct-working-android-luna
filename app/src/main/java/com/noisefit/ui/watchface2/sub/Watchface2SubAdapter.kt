package com.noisefit.ui.watchface2.sub

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.databinding.RowWatchFace2Binding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImageCacheWithProgress
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible


import com.varunest.sparkbutton.SparkEventListener


class Watchface2SubAdapter(private val listener: Watchface2InteractionListener) :
    RecyclerView.Adapter<Watchface2SubAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<Watchface2>()

    inner class ViewHolder(val binding: RowWatchFace2Binding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(watchFace: Watchface2, position: Int) {

            binding.ivFavourite.isChecked = watchFace.isFav()

            if (watchFace.rating.isNullOrEmpty()) {
                binding.imvRating.gone()
                binding.tvStarCount.gone()
            } else {
                binding.tvStarCount.text = watchFace.rating
                binding.imvRating.visible()
                binding.tvStarCount.visible()
            }
            binding.ivWatchFace.loadImageCacheWithProgress(
                binding.ivWatchFace.context,
                watchFace.imageUrl
            )
            binding.ivWatchFace.contentDescription = "${watchFace.wId}"

            binding.root.setOnClickListener {
                listener.onWatchFaceClicked(watchFace.wId, watchFace, position)
            }

            binding.ivFavourite.setEventListener(object : SparkEventListener {
                override fun onEvent(button: ImageView?, buttonState: Boolean) {
                    listener.onMarkFavouriteClicked(
                        buttonState,
                        watchFace,
                        position
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
        holder.bind(mDataSet[position], position)
    }

    override fun getItemCount() = mDataSet.size
    fun setDataSet(list: List<Watchface2>) {
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
            mDataSet[currentPosition].isFavourite = if (state) "1" else "0"
            notifyItemChanged(currentPosition)
        }
    }
}

interface Watchface2InteractionListener {
    fun onWatchFaceClicked(watchFaceId: Int, watchface: Watchface2, position: Int)
    fun onMarkFavouriteClicked(favourite: Boolean, watchface: Watchface2, position: Int)
}