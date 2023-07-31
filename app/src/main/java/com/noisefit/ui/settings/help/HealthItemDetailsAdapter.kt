package com.noisefit.ui.settings.help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.model.FitnessHealthModel
import com.noisefit.luna.databinding.RowFitnessHealthDetailsItemsBinding

class HealthItemDetailsAdapter(val activity: FragmentActivity, val listener: HyperlinkAction) :
    RecyclerView.Adapter<HealthItemDetailsAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<FitnessHealthModel>()

    inner class ViewHolder(private val binding: RowFitnessHealthDetailsItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(value: FitnessHealthModel) {
            binding.tvTitle.text = value.title
            binding.tvMsg.text = value.msg
            if (value.hyperLink.isNotEmpty()) {
                binding.tvHyperlink.visibility = View.VISIBLE
                binding.tvHyperlink.text = "Read more"
            } else {
                binding.tvHyperlink.visibility = View.GONE
            }
            binding.tvHyperlink.setTextColor(activity.resources.getColor(com.noisefit_commans.R.color.md_theme_dark_text_button))
            binding.tvHyperlink.setOnClickListener {
                listener.onLinkClicked(value)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HealthItemDetailsAdapter.ViewHolder {
        val binding = RowFitnessHealthDetailsItemsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HealthItemDetailsAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataList: ArrayList<FitnessHealthModel>) {
        mDataSet = dataList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size


}

interface HyperlinkAction {
    fun onLinkClicked(item: FitnessHealthModel)
}
