package com.oreo.ui.helpsupport.questionaries

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.OreoHsQuestionariesItemBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.OHSQuestionariesResponseModel

class OHSQAAdapter(val mListener: OnItemClickListener) :
    RecyclerView.Adapter<OHSQAAdapter.ViewHolder>() {
    private val mDataset = ArrayList<OHSQuestionariesResponseModel>()

    inner class ViewHolder(val binding: OreoHsQuestionariesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(result: OHSQuestionariesResponseModel) {
            binding.tvTitle.text=result.title
            binding.tvDesc.text=result.description

            if (result.isExpendable) {
                binding.tvDesc.visible()
                binding.ivExpand.setImageResource(R.drawable.ic_hs_collapse)
            } else {
                binding.ivExpand.setImageResource(R.drawable.ic_hs_expand)
                binding.tvDesc.gone()
            }
            binding.root.setOnClickListener {
                mListener.onItemClick(!result.isExpendable,bindingAdapterPosition)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = OreoHsQuestionariesItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(view)

    }

    override fun getItemCount() = mDataset.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataset[position])
    }

    fun setData(dataList: ArrayList<OHSQuestionariesResponseModel>) {
        mDataset.clear()
        mDataset.addAll(dataList)
        notifyDataSetChanged()
    }

    fun updateData(isExpanded: Boolean, position: Int) {
        mDataset[position].isExpendable = isExpanded
        mDataset[position] = mDataset[position]
        notifyDataSetChanged()

    }

    interface OnItemClickListener {
        fun onItemClick(isExpanded: Boolean,position: Int)
    }
}