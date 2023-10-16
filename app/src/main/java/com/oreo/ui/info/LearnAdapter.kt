package com.oreo.ui.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ListRingCareBinding
import com.noisefit.luna.databinding.ListVideoInfoCardBinding
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.LearnModel

class LearnAdapter(val listener: LearnAdapterActions) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mDataSet = ArrayList<LearnModel>()

    inner class InfoVideoCardViewHolder(val binding: ListVideoInfoCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            data: LearnModel,
        ) {

            binding.tvTitle.text = data.title
            binding.tvMessage.text = data.content


            binding.ivBack.loadImage(
                binding.ivBack.context,
                data.banner
            )

            binding.root.setOnClickListener {
                listener.onClicked(data)
            }

        }

    }

    inner class InfoTextCardViewHolder(val binding: ListRingCareBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: LearnModel) {
            binding.tvTitle.text = data.title
            binding.tvMessage.text = data.content

            binding.imv.loadImage(
                binding.imv.context,
                data.banner
            )

            binding.root.setOnClickListener {
                listener.onClicked(data)
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> InfoVideoCardViewHolder(
                ListVideoInfoCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> InfoTextCardViewHolder(
                ListRingCareBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mDataSet[position].type.equals("video", true)) {
            1
        } else {
            0
        }
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == 1) {
            (holder as InfoVideoCardViewHolder).bind(mDataSet[position])
        } else {
            (holder as InfoTextCardViewHolder).bind(mDataSet[position])
        }
    }

    fun setDataSet(it: List<LearnModel>) {
        mDataSet.clear()
        mDataSet.addAll(it)
        notifyDataSetChanged()
    }
}

interface LearnAdapterActions {
    fun onClicked(data: LearnModel)
}