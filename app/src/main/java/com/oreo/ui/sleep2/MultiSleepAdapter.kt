package com.oreo.ui.sleep2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowMultiSleepBinding
import com.noisefit_commans.common.setTextGradient

class MultiSleepAdapter(val listener: MultiSleepAction) :
    RecyclerView.Adapter<MultiSleepAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<MultiSleepDisplay>()
    private var selectedPosition = 0

    inner class ViewHolder(val binding: RowMultiSleepBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MultiSleepDisplay) {
            binding.tvTime.text = "${data.sleepStart} | ${data.sleepTime}"

            if (bindingAdapterPosition == mDataSet.size - 1) {
                binding.tvScore.text = "Score ${data.score}"

                binding.tvScore.setTextGradient(
                    binding.tvScore.context.getColor(R.color.white),
                    binding.tvScore.context.getColor(R.color.white),
                    binding.tvScore.context.getColor(R.color.white),
                )

            } else {
                val string = StringBuilder()
                string.append("Score ")
                if (data.scoreImpact >= 0) {
                    string.append("+")
                    string.append(data.scoreImpact)
                } else {
                    string.append(data.scoreImpact)
                }

                binding.tvScore.text = string

                binding.tvScore.setTextGradient(
                    binding.tvScore.context.getColor(R.color.white),
                    Color.parseColor("#ddc5ff"),
                    Color.parseColor("#a665ff")
                )

            }

            if (selectedPosition == bindingAdapterPosition) {
                binding.ivBack.setBackgroundResource(R.drawable.back_sleep_selection_selected)
            } else {
                binding.ivBack.setBackgroundResource(R.drawable.back_sleep_selection_default)
            }

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