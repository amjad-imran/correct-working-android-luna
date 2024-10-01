package com.oreo.ui.referral

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemReferralBinding
import com.oreo.data.model.referral.Referral

class ReferralsInternalAdapter : RecyclerView.Adapter<ReferralsInternalAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<Referral>()

    inner class ViewHolder(val binding: ItemReferralBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(referralsMain: Referral) {

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