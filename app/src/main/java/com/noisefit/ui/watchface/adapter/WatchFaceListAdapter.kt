package com.noisefit.ui.watchface.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.noisefit.luna.R
import com.noisefit.data.remote.response.Watchface2
import com.noisefit_commans.models.WatchFace
import com.noisefit.luna.databinding.RowWatchFaceBinding
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.varunest.sparkbutton.SparkEventListener

class WatchFaceListAdapter(private val listener: WatchFaceActions) :
    RecyclerView.Adapter<WatchFaceListAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<WatchFace>()
    private var mCategoryId: Int = 0

    inner class ViewHolder(val binding: RowWatchFaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(watchFace: WatchFace) {


            Glide.with(binding.ivWatchFace.context)
                .load(watchFace.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_placeholder_watchface)
                .error(R.drawable.ic_placeholder_watchface)
                .into(binding.ivWatchFace)

            binding.root.setOnClickListener {
                listener.onWatchFaceClicked(watchFace.id!!,watchFace)
            }
            binding.tvDownloadsCount.text = watchFace.getDownloadsToDisplay()

            if (mCategoryId==-1){//Favourites not visible for recent
                binding.ivFavouriteBack.invisible()
                binding.ivFavourite.invisible()
            }else{
                binding.ivFavouriteBack.visible()
                binding.ivFavourite.visible()
                binding.ivFavourite.isChecked = watchFace.isFav()
            }

            binding.ivFavourite.setEventListener(object : SparkEventListener {
                override fun onEvent(button: ImageView?, buttonState: Boolean) {
                    listener.onMarkFavouriteClicked(
                        buttonState,
                        watchFace,
                        bindingAdapterPosition
                    )
                    watchFace.is_favourite = if (buttonState) "1" else "0"
                }

                override fun onEventAnimationEnd(button: ImageView?, buttonState: Boolean) {}

                override fun onEventAnimationStart(button: ImageView?, buttonState: Boolean) {}
            })

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowWatchFaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size
    fun setDataSet(list: List<WatchFace>, id: Int) {
        mCategoryId = id
        mDataSet.clear()
        mDataSet.addAll(list)
        notifyDataSetChanged()
    }
}

interface WatchFaceActions {
    fun onWatchFaceClicked(watchFaceId: Int,watchface: WatchFace)
    fun onMarkFavouriteClicked(favourite: Boolean, watchface: WatchFace, position: Int)
}