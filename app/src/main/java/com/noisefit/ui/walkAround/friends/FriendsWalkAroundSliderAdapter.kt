package com.noisefit.ui.walkAround.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.model.FriendsWalkAround
import com.noisefit.luna.databinding.ViewFriendWalkaroundSliderBinding
import com.noisefit_commans.ui.loadImage


class FriendsWalkAroundSliderAdapter() :
    RecyclerView.Adapter<FriendsWalkAroundSliderAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<FriendsWalkAround>()

    inner class ViewHolder(private val binding: ViewFriendWalkaroundSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: FriendsWalkAround, position: Int) {

            binding.imageView.loadImage(binding.imageView.context, data.image)
            binding.tvTitle.text = data.title
            binding.tvSubtitle.text = data.subtitle

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ViewFriendWalkaroundSliderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    override fun getItemCount(): Int = mDataSet.size


    fun setDataSet(dataSet: List<FriendsWalkAround>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}

