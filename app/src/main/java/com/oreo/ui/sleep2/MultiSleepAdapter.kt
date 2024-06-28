package com.oreo.ui.sleep2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowMultiSleepBinding

class MultiSleepAdapter(val listener: MultiSleepAction) :
    RecyclerView.Adapter<MultiSleepAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<String>()

    inner class ViewHolder(val binding: RowMultiSleepBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {
            binding.tvTime.text = "10:04 | 4hr 12min"
            binding.tvScore.text = "Score 66"

            binding.root.setOnClickListener {
                listener.onSleepClicked()
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            RowMultiSleepBinding.inflate(
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

    fun setData(resultData: List<String>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

}

interface MultiSleepAction {
    fun onSleepClicked()
}