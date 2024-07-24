package com.oreo.ui.sleep2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowMultiSleepBinding

class MultiSleepAdapter(val listener: MultiSleepAction) :
    RecyclerView.Adapter<MultiSleepAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<MultiSleepDisplay>()
    private var selectedPosition = 0

    inner class ViewHolder(val binding: RowMultiSleepBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MultiSleepDisplay) {
            binding.tvTime.text = "${data.sleepStart} | ${data.sleepTime}"
            binding.tvScore.text = "Score ${data.score}"

            binding.root.setOnClickListener {
                val oldPos = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(oldPos)
                notifyItemChanged(selectedPosition)
                listener.onSleepClicked(bindingAdapterPosition)
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

    fun setData(resultData: List<MultiSleepDisplay>) {
        selectedPosition = 0
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

}

interface MultiSleepAction {
    fun onSleepClicked(position: Int)
}

data class MultiSleepDisplay(
    val sleepStart: String,
    val sleepTime: String,
    val score: String,
    val scoreImpact: Int
)