package com.noisefit.ui.watchface.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit_commans.models.CustomWatchFace
import com.noisefit.databinding.RowWatchFaceCustomBinding

class CustomWatchFaceAdapter(private val listener: CustomWatchFaceActions) :
    RecyclerView.Adapter<CustomWatchFaceAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<CustomWatchFace>()

    inner class ViewHolder(val binding: RowWatchFaceCustomBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(watchFace: CustomWatchFace) {

            Glide.with(binding.ivBackgroundLayer.context)
                .load(watchFace.backgroundLayer)
                .into(binding.ivBackgroundLayer)

            Glide.with(binding.ivTextLayer.context)
                .load(watchFace.textLayer)
                .into(binding.ivTextLayer)

            binding.root.setOnClickListener {
                listener.onWatchFaceClicked(watchFace)
            }


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowWatchFaceCustomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size

    fun setDataSet(list: List<CustomWatchFace>) {
        mDataSet = list as ArrayList<CustomWatchFace>
        notifyDataSetChanged()
    }
}

interface CustomWatchFaceActions {
    fun onWatchFaceClicked(face: CustomWatchFace)
}