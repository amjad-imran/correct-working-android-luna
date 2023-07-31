package com.noisefit.ui.diy

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit.data.model.DiyCustomWatchColor
import com.noisefit.databinding.LayoutDiySubColorListBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible


class DiyColorAdapter(val diyWatchFaceBgListener: DiyWatchFaceBgListener) :
    RecyclerView.Adapter<DiyColorAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<DiyCustomWatchColor>()

    private fun handleViewsDim(showDim: Boolean, binding: LayoutDiySubColorListBinding) {
        var alpha = 1f
        if (showDim) {
            alpha = .6f
        }
        binding.bgImv.alpha = alpha

    }

    inner class ViewHolder(private val binding: LayoutDiySubColorListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DiyCustomWatchColor, position: Int) {

            if (data.isSelected) {
                binding.vBackSelector.visible()
                handleViewsDim(false, binding)
            } else {
                binding.vBackSelector.gone()
                handleViewsDim(true, binding)
            }

            binding.bgImv.setBackgroundColor(data.color)
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
    ): DiyColorAdapter.ViewHolder {
        val binding = LayoutDiySubColorListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiyColorAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    fun setDataSet(dataList: List<DiyCustomWatchColor>) {
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

    fun dpToPx(px: Int, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
        )
    }

    interface DiyWatchFaceBgListener {
        fun onWatchFaceClicked(diyCustomWatchFaceBg: DiyCustomWatchColor, position: Int)
    }
}