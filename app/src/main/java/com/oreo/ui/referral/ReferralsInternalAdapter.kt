package com.oreo.ui.referral

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemReferralBinding
import com.oreo.data.model.referral.Referral
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReferralsInternalAdapter : RecyclerView.Adapter<ReferralsInternalAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<Referral>()

    inner class ViewHolder(val binding: ItemReferralBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(referralsMain: Referral) {
            binding.tvName.text = referralsMain.usedBy
            binding.tvDate.text = LocalDate.parse(referralsMain.createdDate)
                .format(DateTimeFormatter.ofPattern("dd MMM,yyyy"))

            if (referralsMain.status.equals("purchased")) {
                binding.ivPurchased.setImageResource(R.drawable.ic_ref_status_filled)
                binding.ivDelivered.setImageResource(R.drawable.ic_ref_status_ring)
                binding.tvStatus.text = binding.tvStatus.context.getString(R.string.text_pending)
                binding.tvStatus.setTextColor(Color.parseColor("#ffbb6b"))
            } else if (referralsMain.status.equals("delivered")) {
                binding.ivPurchased.setImageResource(R.drawable.ic_ref_status_filled)
                binding.ivDelivered.setImageResource(R.drawable.ic_ref_status_filled)
                binding.tvStatus.text = binding.tvStatus.context.getString(R.string.text_done)
                binding.tvStatus.setTextColor(Color.parseColor("#6bff9d"))
            } else if(referralsMain.status.equals("cancelled")) {
                binding.ivPurchased.setImageResource(R.drawable.ic_ref_status_ring)
                binding.ivDelivered.setImageResource(R.drawable.ic_ref_status_ring)
                binding.tvStatus.text = "Cancelled"
                binding.tvStatus.setTextColor(Color.parseColor("#ff4062"))
            }else {
                binding.ivPurchased.setImageResource(R.drawable.ic_ref_status_ring)
                binding.ivDelivered.setImageResource(R.drawable.ic_ref_status_ring)
                binding.tvStatus.text = "-"
                binding.tvStatus.setTextColor(Color.parseColor("#ccf4eded"))
            }


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemReferralBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(mDataSet[position])

    }

    fun setDataSet(referral: List<Referral>) {
        mDataSet.clear()
        mDataSet.addAll(referral)
        notifyDataSetChanged()
    }
}