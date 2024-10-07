package com.oreo.ui.referral

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemReferralMainBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.referral.ReferralsMain

class MyReferralsAdapter : RecyclerView.Adapter<MyReferralsAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<ReferralsMain>()

    inner class ViewHolder(val binding: ItemReferralMainBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(referralsMain: ReferralsMain) {

            binding.tvReferralName.text = referralsMain.referralName

            if (referralsMain.status.equals("won", true)) {
                binding.tvMessage.setTextColor(android.graphics.Color.parseColor("#6bff9d"))
            } else {
                binding.tvMessage.setTextColor(android.graphics.Color.parseColor("#ffbb6b"))
            }

            if (referralsMain.message.isNullOrEmpty()) {
                binding.tvMessage.gone()
            } else {
                binding.tvMessage.visible()
                binding.tvMessage.text = referralsMain.message
            }

            binding.rvReferrals.layoutManager = LinearLayoutManager(binding.rvReferrals.context)
            binding.rvReferrals.adapter = ReferralsInternalAdapter().apply {
                this.setDataSet(referralsMain.referred ?: ArrayList())
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemReferralMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(mDataSet[position])

    }

    fun setDataSet(referralsMains: List<ReferralsMain>) {
        mDataSet.clear()
        mDataSet.addAll(referralsMains)
        notifyDataSetChanged()
    }
}