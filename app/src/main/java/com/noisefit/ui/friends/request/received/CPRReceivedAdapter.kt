package com.noisefit.ui.friends.request.received

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit.R
import com.noisefit_commans.data.response.Requests
import com.noisefit.databinding.RowCprReceivedBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class CPRReceivedAdapter(val listener: OnItemClickListener) :
    RecyclerView.Adapter<CPRReceivedAdapter.ViewHolder>() {

    val mDataSet = ArrayList<Requests>()

    inner class ViewHolder(val binding: RowCprReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(requests: Requests, position: Int) {

            binding.tvFriendName.text = requests.first_name
            binding.tvInterestName.text = requests.title

            if (requests.tempStatus.isNullOrEmpty()) {
                binding.tvAccept.text = binding.tvAccept.context.getString(R.string.text_accept)
                binding.tvDecline.text = binding.tvAccept.context.getString(R.string.text_decline)
                binding.tvDecline.visible()
                binding.tvAccept.visible()
                binding.tvAccept.isEnabled = true
                binding.tvDecline.isEnabled = true
            } else {
                binding.tvAccept.text = requests.tempStatus
                binding.tvDecline.gone()
                binding.tvAccept.visible()
                binding.tvAccept.isEnabled = false
                binding.tvDecline.isEnabled = false
            }

            Glide.with(binding.ivFriendProfile)
                .load(requests.image_url)
                .placeholder(R.drawable.ic_default_profile_image)
                .into(binding.ivFriendProfile)


            binding.tvAccept.setOnClickListener {
                listener.onAcceptClicked(requests, position)
            }
            binding.tvDecline.setOnClickListener {
                listener.onDeclineClicked(requests)
            }
            binding.root.setOnClickListener {
                listener.onItemClicked(requests)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowCprReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<Requests>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    fun removeItem(requests: Requests) {
        mDataSet.removeAll {
            it.user_id == requests.user_id
        }
        notifyDataSetChanged()
    }

    fun updateItemStatus(statusValue: String, position: Int) {
        mDataSet[position].tempStatus = statusValue
        notifyItemChanged(position)
    }
}

interface OnItemClickListener {
    fun onAcceptClicked(requests: Requests, position: Int)
    fun onDeclineClicked(requests: Requests)
    fun onItemClicked(requests: Requests)
}