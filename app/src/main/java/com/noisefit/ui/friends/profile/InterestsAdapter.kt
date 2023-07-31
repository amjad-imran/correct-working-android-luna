package com.noisefit.ui.friends.profile

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit_commans.data.model.FriendInterest
import com.noisefit.databinding.RowInterestBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class InterestsAdapter : RecyclerView.Adapter<InterestsAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<FriendInterest>()

    inner class ViewHolder(val binding: RowInterestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: FriendInterest) {
            binding.tvTitle.text = friend.name

            if (friend.isCommon == true) {
                binding.tvTitle.setBackgroundResource(R.drawable.back_modal_10)
                binding.tvTitle.setTextColor(Color.parseColor("#ffffff"))
            } else {
                binding.tvTitle.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
                binding.tvTitle.setTextColor(Color.parseColor("#99ffffff"))
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowInterestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])

    }

    override fun getItemCount(): Int = mDataSet.size

    fun setDataSet(interests: List<FriendInterest>) {
        mDataSet.clear()
        mDataSet.addAll(interests)
        notifyDataSetChanged()
    }
}