package com.noisefit.ui.watchface.adapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.luna.databinding.RowRandomWatchfaceBinding
import com.noisefit_commans.models.WatchFace

@Deprecated("product changed requirement")
class RandomWatchFaceAdapter(private val listener: WatchFaceRandomActions) :
    RecyclerView.Adapter<RandomWatchFaceAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<Watchface2>()

    inner class ViewHolder(val binding: RowRandomWatchfaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(watchFace: Watchface2) {

            val circularProgressDrawable = CircularProgressDrawable(binding.ivWatchFace.context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.setColorFilter(Color.WHITE,PorterDuff.Mode.SRC_IN)
            circularProgressDrawable.start()

            Glide.with(binding.ivWatchFace.context)
                .load(watchFace.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(circularProgressDrawable)
                .into(binding.ivWatchFace)

            binding.root.setOnClickListener {
                listener.onWatchFaceClicked(watchFace.wId,watchFace)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowRandomWatchfaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size
    fun setDataSet(list: List<Watchface2>) {
        mDataSet.clear()
        mDataSet.addAll(list)
        notifyDataSetChanged()
    }
}
interface WatchFaceRandomActions {
    fun onWatchFaceClicked(watchFaceId: Int,watchface: Watchface2)
}