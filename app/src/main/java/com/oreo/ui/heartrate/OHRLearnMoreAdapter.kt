package com.oreo.ui.heartrate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.OreoHrLearnMoreItemBinding
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.LearnMoreDataModel

class OHRLearnMoreAdapter(val mListener: OnItemClickListener) :
    RecyclerView.Adapter<OHRLearnMoreAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<LearnMoreDataModel>()

    inner class ViewHolder(val binding: OreoHrLearnMoreItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: LearnMoreDataModel) {
            binding.tvTitle.text = resultData.title
            binding.textView2.text = resultData.msg
            binding.imageView1.loadImage(binding.imageView1.context, resultData.img)

            binding.root.setOnClickListener {
                mListener.onItemClick(resultData)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            OreoHrLearnMoreItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(resultData: List<LearnMoreDataModel>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

}

interface OnItemClickListener {
    fun onItemClick(item: LearnMoreDataModel)
}

