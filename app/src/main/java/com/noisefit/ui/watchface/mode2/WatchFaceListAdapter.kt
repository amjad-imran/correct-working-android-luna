package com.noisefit.ui.watchface.mode2

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.noisefit.luna.databinding.RowWatchFace3Binding
import com.noisefit_commans.models.WatchFace

class WatchFaceListAdapter(private val listener: WatchFaceActions) :
    RecyclerView.Adapter<WatchFaceListAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<WatchFace>()

    inner class ViewHolder(val binding: RowWatchFace3Binding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(watchFace: WatchFace) {


//            binding.tvDownloadsCount.text = watchFace.getDownloadsToDisplay()


            /*if(watchFace.displayDownloads.isEmpty()){
                binding.tvDownloadsCount.invisible()
            }else{
                binding.tvDownloadsCount.text = watchFace.downloads
            }*/

            binding.root.contentDescription = "$bindingAdapterPosition"
            binding.ivWatchFace.contentDescription = "$bindingAdapterPosition"

            val circularProgressDrawable = CircularProgressDrawable(binding.ivWatchFace.context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            circularProgressDrawable.start()

            //binding.ivWatchFace.loadImage(binding.ivWatchFace.context,watchFace.imageUrl)

            Glide.with(binding.ivWatchFace.context)
                .load(watchFace.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(circularProgressDrawable)
                .into(binding.ivWatchFace)

            binding.root.setOnClickListener {
                listener.onWatchFaceClicked(watchFace)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowWatchFace3Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size
    fun setDataSet(list: List<WatchFace>) {
        mDataSet = list as ArrayList<WatchFace>
        notifyDataSetChanged()
    }
}

interface WatchFaceActions {
    fun onWatchFaceClicked(watchFace: WatchFace)
}