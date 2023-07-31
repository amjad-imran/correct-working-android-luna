package com.noisefit.ui.feeds.create

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit.data.model.MentionUser
import com.noisefit.databinding.RowUserSuggestionBinding
import com.noisefit_commans.ui.loadImage


class UserSuggestionAdapter(val onUserClicked: (user: MentionUser) -> Unit) :
    RecyclerView.Adapter<UserSuggestionAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<MentionUser>()

    inner class ViewHolder(val binding: RowUserSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: MentionUser) {

            binding.tvUserName.text = user.first_name
            binding.ivUserImage.loadImage(
                binding.ivUserImage.context,
                user.image_url,
                R.drawable.ic_default_profile_image
            )

            binding.root.setOnClickListener {
                onUserClicked(user)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowUserSuggestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(data: List<MentionUser>) {
        mDataSet.clear()
        mDataSet.addAll(data)
        notifyDataSetChanged()
    }

}