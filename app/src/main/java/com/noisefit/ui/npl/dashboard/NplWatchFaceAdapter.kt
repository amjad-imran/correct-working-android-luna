package com.noisefit.ui.npl.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.Faces
import com.noisefit.luna.databinding.NplWatchfaceItemBinding
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.ui.loadImage

class NplWatchFaceAdapter(val listener: OnWatchFaceClickListener) :
    RecyclerView.Adapter<NplWatchFaceAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<Faces>()

    inner class ViewHolder(val binding: NplWatchfaceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(watchFace: Faces) {
            binding.ivWatchFace.loadImage(binding.ivWatchFace.context, watchFace.imageUrl)
            binding.ivWatchFace.setOnClickListener {
                listener.onWatchFaceItemClick(watchFace)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            NplWatchfaceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return if (mDataSet.size > 3)
            3
        else
            mDataSet.size
    }

    fun setDataSet(list: List<Faces>) {
        mDataSet.clear()
        mDataSet.addAll(list)
        notifyDataSetChanged()
    }

    interface OnWatchFaceClickListener {
        fun onWatchFaceItemClick(watchFace: Faces)
    }
}