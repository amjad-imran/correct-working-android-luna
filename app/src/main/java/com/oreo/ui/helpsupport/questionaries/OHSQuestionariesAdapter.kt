package com.oreo.ui.helpsupport.questionaries

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.OreoHsQuestionariesItemBinding
import com.oreo.data.model.OHSQuestionariesResponseModel

class OHSQuestionariesAdapter : RecyclerView.Adapter<OHSQuestionariesAdapter.ViewHolder>() {
    private val mDataset = ArrayList<OHSQuestionariesResponseModel>()

    inner class ViewHolder(binding: OreoHsQuestionariesItemBinding) : RecyclerView.ViewHolder(binding.root) {

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
}