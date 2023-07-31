package com.noisefit.ui.watchface2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.databinding.RowWatchFace2HorizontalBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImageCacheWithProgress
import com.noisefit_commans.ui.visible
import com.noisefit.ui.watchface2.sub.Watchface2InteractionListener
import com.noisefit.watch.WatchForm
import com.varunest.sparkbutton.SparkEventListener


class HorizontalWatchfaceAdapter(private val listener: Watchface2InteractionListener) :
    RecyclerView.Adapter<HorizontalWatchfaceAdapter.ViewHolder>() {

    private var screenType = WatchForm.SQUARE
    private var mDataSet = ArrayList<Watchface2>()

    inner class ViewHolder(val binding: RowWatchFace2HorizontalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(watchFace: Watchface2,position: Int) {

            binding.ivFavourite.isChecked = watchFace.isFav()

//            if(screenType == WatchForm.CIRCLE){
//                val layoutParams = LinearLayout.LayoutParams(120, 120)
//                binding.ivWatchFace.layoutParams = ViewGroup.LayoutParams(150, 150)
//            }

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

            binding.root.setOnClickListener {
                listener.onWatchFaceClicked(watchFace.wId, watchFace, position )
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
            RowWatchFace2HorizontalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position],position)
    }

    override fun getItemCount() = mDataSet.size
    fun setDataSet(list: List<Watchface2>,screenType:WatchForm) {
        mDataSet.clear()
        this.screenType = screenType
        mDataSet.addAll(list)
        notifyDataSetChanged()
    }
}