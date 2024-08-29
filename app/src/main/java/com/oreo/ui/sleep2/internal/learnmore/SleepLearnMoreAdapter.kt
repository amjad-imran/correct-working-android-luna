package com.oreo.ui.sleep2.internal.learnmore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.OreoSleepLearnMoreItemBinding
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.sleep.SleepLearnMoreDataModel

class SleepLearnMoreAdapter(val mListener: OnItemClickListener) :
    RecyclerView.Adapter<SleepLearnMoreAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<SleepLearnMoreDataModel>()

    inner class ViewHolder(val binding: OreoSleepLearnMoreItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: SleepLearnMoreDataModel) {
            binding.tvTitle.text = resultData.title

            binding.imageView1.loadImage(binding.imageView1.context, resultData.img)

            binding.root.setOnClickListener {
                mListener.onItemClick(resultData)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            OreoSleepLearnMoreItemBinding.inflate(
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

    fun setData(resultData: List<SleepLearnMoreDataModel>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

}

interface OnItemClickListener {
    fun onItemClick(item: SleepLearnMoreDataModel)
}