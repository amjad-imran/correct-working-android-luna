package com.noisefit.ui.settings.helpAndSupport.sublist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.response.HelpAndSupportQuestion
import com.noisefit.luna.databinding.ItemHSListBinding

class HelpAndSupportListAdapter(val listener: HelpAndSupportInteractionListener) :
    RecyclerView.Adapter<HelpAndSupportListAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<HelpAndSupportQuestion>()

    inner class ViewHolder(private val binding: ItemHSListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(value: HelpAndSupportQuestion) {
            binding.tvQuestion.text = value.question

            binding.container.setOnClickListener {
                listener.onHelpAndSupportClick(value)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HelpAndSupportListAdapter.ViewHolder {
        val binding = ItemHSListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HelpAndSupportListAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataList: List<HelpAndSupportQuestion>) {
        mDataSet = dataList as ArrayList<HelpAndSupportQuestion>
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size


}

interface HelpAndSupportInteractionListener {
    fun onHelpAndSupportClick(helpAndSupportQuestion: HelpAndSupportQuestion)
}
