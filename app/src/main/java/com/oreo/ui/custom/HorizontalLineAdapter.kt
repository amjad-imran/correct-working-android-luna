package com.oreo.ui.custom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowHorizontalLineChartBinding
import com.noisefit_commans.utils.LOGS


class HorizontalLineAdapter : RecyclerView.Adapter<HorizontalLineAdapter.ViewHolder>() {

    private var mDataSet = ArrayList<Int>()
    private var mSelectedPosition = -1


    inner class ViewHolder(private val binding: RowHorizontalLineChartBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {

            val params = binding.ivPoint.layoutParams as ConstraintLayout.LayoutParams
            val bias = 1 - (position.toFloat() / 100)
            LOGS.d("BIAS___ pos ${(position.toFloat() / 100)} $bias")
            params.verticalBias = bias
            binding.ivPoint.layoutParams = params


            val startPos = try {
                mDataSet[bindingAdapterPosition - 1]
            } catch (exp: Exception) {
                0
            }
            val currentPos = position
            val lastPos = try {
                mDataSet[bindingAdapterPosition + 1]
            } catch (exp: Exception) {
                0
            }

            binding.vLine.setData(startPos,currentPos,lastPos)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowHorizontalLineChartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataList: ArrayList<Int>) {
        mDataSet.clear()
        mDataSet.addAll(dataList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setSelectedPosition(layoutPosition: Int) {
        mSelectedPosition = layoutPosition
        notifyDataSetChanged()
    }


}