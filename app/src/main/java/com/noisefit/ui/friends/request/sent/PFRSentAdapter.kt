package com.noisefit.ui.friends.request.sent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit.R
import com.noisefit_commans.data.response.Requests
import com.noisefit.databinding.RowCprReceivedBinding
import com.noisefit_commans.ui.gone

class PFRSentAdapter(val listener: OnItemClickListener) :
    RecyclerView.Adapter<PFRSentAdapter.ViewHolder>() {
    val mDataSet = ArrayList<Requests>()

    inner class ViewHolder(val binding: RowCprReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(requests: Requests) {
            binding.tvAccept.gone()

            binding.tvDecline.text = "Remove"
            binding.tvDecline.setTextColor(
                ContextCompat.getColor(
                    binding.tvDecline.context,
                    R.color.white_64
                )
            )
            Glide.with(binding.ivFriendProfile)
                .load(requests.image_url)
                .placeholder(R.drawable.ic_default_profile_image)
                .into(binding.ivFriendProfile)
            binding.tvFriendName.text = requests.first_name

            val joinedInterests = requests.interests?.take(2)?.joinToString() ?: ""

            binding.tvInterestName.text =
                "${if (joinedInterests.isNotEmpty()) "$joinedInterests..." else ""}"

            binding.tvDecline.setOnClickListener {
                listener.onRemoveClicked(requests)
            }
            binding.root.setOnClickListener {
                listener.onItemClicked(requests)
            }

            binding.tvDecline.alpha = 1f
            binding.tvDecline.setTextColor(
                binding.tvDecline.resources.getColor(R.color.white)
            )
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

    fun removeItem(requests: Requests) {
        mDataSet.removeAll {
            it.user_id == requests.user_id
        }
        notifyDataSetChanged()
    }
}

interface OnItemClickListener {
    fun onRemoveClicked(requests: Requests)
    fun onItemClicked(requests: Requests)
}