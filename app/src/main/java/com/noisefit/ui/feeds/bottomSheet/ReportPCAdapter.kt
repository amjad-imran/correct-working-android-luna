package com.noisefit.ui.feeds.bottomSheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.ReportAbuseData
import com.noisefit.luna.databinding.ItemReportAbuseListBinding


class ReportPCAdapter(val listener: OnRepostAbuseItemClickListener) :
    RecyclerView.Adapter<ReportPCAdapter.ViewHolder>() {


    val mDataSet = ArrayList<ReportAbuseData>()

    inner class ViewHolder(val binding: ItemReportAbuseListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ReportAbuseData) {
            binding.title.text = data.title
            binding.root.setOnClickListener {
                listener.onItemClick(data)
            }


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemReportAbuseListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<ReportAbuseData>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()

    }
}

interface OnRepostAbuseItemClickListener {
    fun onItemClick(data: ReportAbuseData)

}