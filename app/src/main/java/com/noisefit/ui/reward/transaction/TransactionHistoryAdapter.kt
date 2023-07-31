package com.noisefit.ui.reward.transaction

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit_commans.data.model.TransactionHistory
import com.noisefit.luna.databinding.ItemTransactionHistoryBinding
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.DateFormats

class TransactionHistoryAdapter : RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder>() {
    private var transHistoryResult = ArrayList<TransactionHistory>()

    inner class ViewHolder(val binding: ItemTransactionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transData: TransactionHistory) {

            binding.tvTitle.text = AppConstants.teamNameMapping(null, transData.title ?: "",false)
            binding.tvTransDate.text = transData.transaction_at?.let {
                DateFormats.convertTimeStampToDate(
                    it
                )
            }

            Glide.with(binding.ivUserProfile.context)
                .load(transData.image_url)
                .into(binding.ivUserProfile)

            if (transData.status.toString().lowercase() == "credited") {
                binding.tvTransAmount.text = "+ ${transData.points}"
                binding.tvTransAmount.setTextColor(Color.parseColor("#29cc74"))
            } else {
                binding.tvTransAmount.text = "- ${transData.points}"
                binding.tvTransAmount.setTextColor(Color.parseColor("#fefefe"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemTransactionHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return transHistoryResult.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transHistoryResult[position])
    }

    fun setDataSet(transResult: List<TransactionHistory>) {
        transHistoryResult.clear()
        transHistoryResult.addAll(transResult)
        notifyDataSetChanged()
    }

}