package com.noisefit.ui.friends.request.received

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit.luna.R
import com.noisefit_commans.data.response.Requests
import com.noisefit.luna.databinding.RowCprReceivedBinding

class PFRReceivedAdapter(val listener: OnItemClickListener) :
    RecyclerView.Adapter<PFRReceivedAdapter.ViewHolder>() {

    val mDataSet = ArrayList<Requests>()

    inner class ViewHolder(val binding: RowCprReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(requests: Requests,position: Int) {

            binding.tvFriendName.text = requests.first_name

            val joinedInterests = requests.interests?.take(2)?.joinToString() ?: ""

            binding.tvInterestName.text = "${if (joinedInterests.isNotEmpty()) "$joinedInterests..." else ""}"


            Glide.with(binding.ivFriendProfile)
                .load(requests.image_url)
                .placeholder(R.drawable.ic_default_profile_image)
                .into(binding.ivFriendProfile)


            binding.tvAccept.setOnClickListener {
                listener.onAcceptClicked(requests, position )
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
        holder.bind(mDataSet[position],position)
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
}