package com.noisefit.ui.diy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.data.model.DiyCustomWatchType
import com.noisefit.luna.databinding.ItemDiyWfTypeBinding
import com.noisefit_commans.ui.visible
import com.noisefit.watch.WatchForm
import com.noisefit_commans.ui.loadImage


class DiyWatchTypeAdapter(val diyWatchFaceBgListener: DiyWatchTypeListener) :
    RecyclerView.Adapter<DiyWatchTypeAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<DiyCustomWatchType>()


    inner class ViewHolder(private val binding: ItemDiyWfTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DiyCustomWatchType, position: Int) {

            binding.tvTitle.text = data.title
            binding.imv.loadImage(binding.imv.context, data.image)

            if (data.isSelected) {
                binding.container.setBackgroundResource(R.drawable.back_modal_solid_purple)
            } else {
                binding.container.setBackgroundResource(R.drawable.back_modal_10)
            }

            binding.root.setOnClickListener {
                if (data.isSelected) {
                    return@setOnClickListener
                }
                handleData(position)
                diyWatchFaceBgListener.onWatchFaceClicked(data, position)
            }
        }


    }

    private fun handleData(position: Int) {
        var lastSelectedPosition = -1
        mDataSet.forEachIndexed { index, watchFaceBg ->
            if (watchFaceBg.isSelected) {
                lastSelectedPosition = index
                watchFaceBg.isSelected = false
            }
        }

        mDataSet[position].isSelected = true


        if (lastSelectedPosition != -1) {
            notifyItemChanged(lastSelectedPosition)
        }
        notifyItemChanged(position)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DiyWatchTypeAdapter.ViewHolder {
        val binding = ItemDiyWfTypeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiyWatchTypeAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    fun setDataSet(dataList: List<DiyCustomWatchType>) {
        mDataSet.clear()
        mDataSet.addAll(dataList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size

    fun getSelectedPosition(): Int {
        var lastPos = -1
        mDataSet.forEachIndexed { index, watchFaceBg ->
            if (watchFaceBg.isSelected) {
                if (index in 0..1) {
                    return lastPos
                }
                lastPos = index
                return@forEachIndexed
            }
        }
        return lastPos
    }


    interface DiyWatchTypeListener {
        fun onWatchFaceClicked(diyCustomWatchType: DiyCustomWatchType, position: Int)
    }
}