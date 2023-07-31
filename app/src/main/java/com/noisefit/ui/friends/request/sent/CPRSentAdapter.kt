package com.noisefit.ui.friends.request.sent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit.luna.R
import com.noisefit_commans.data.response.Requests
import com.noisefit.luna.databinding.RowCprReceivedBinding
import com.noisefit_commans.ui.gone

class CPRSentAdapter(val listener: OnItemClickListener) : RecyclerView.Adapter<CPRSentAdapter.ViewHolder>() {
    val mDataSet = ArrayList<Requests>()

    inner class ViewHolder(val binding: RowCprReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(requests: Requests) {
            binding.tvAccept.gone()
            binding.tvDecline.text = "Pending"
            binding.tvDecline.setTextColor(ContextCompat.getColor(binding.tvDecline.context,R.color.white_24))
            Glide.with(binding.ivFriendProfile)
                .load(requests.image_url)
                .placeholder(R.drawable.ic_default_profile_image)
                .into(binding.ivFriendProfile)
            binding.tvFriendName.text = requests.first_name
            binding.tvInterestName.text = requests.title

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
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(dataSet: List<Requests>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}