package com.oreo.ui.timelineScreen.addActivity.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.timeline.SupplementOption
import com.noisefit.luna.databinding.SpinnerItemAdddLogCircadianBinding
import com.noisefit_commans.ui.setVisibilityByCondition

class RecoveryOptionsAdapter(
    val onItemClick: (SupplementOption) -> Unit
) : RecyclerView.Adapter<RecoveryOptionsAdapter.ViewHolder>() {

    private val mList = ArrayList<SupplementOption>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = SpinnerItemAdddLogCircadianBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(
            mList[position],
            position != mList.size - 1,
            onItemClick
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(private val binding: SpinnerItemAdddLogCircadianBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            data: SupplementOption,
            showDivider: Boolean,
            onItemClick: (SupplementOption) -> Unit
        ){
            binding.tvTitle.text = data.options

            binding.divider.root.setVisibilityByCondition(showDivider)

            binding.root.setOnClickListener {
                onItemClick(data)
            }
        }
    }

    fun updateDataSet(list: List<SupplementOption>){
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

}