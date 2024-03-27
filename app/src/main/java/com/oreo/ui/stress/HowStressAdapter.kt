package com.oreo.ui.stress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit.luna.databinding.ItemHowStressListBinding
import com.noisefit_commans.data.response.HowStressResponse


class HowStressAdapter :
    RecyclerView.Adapter<HowStressAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<HowStressResponse>()

    inner class ViewHolder(private val binding: ItemHowStressListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: HowStressResponse) {
            binding.tvTitle.text = data.title
            binding.tvDescription.text = data.title
            Glide.with(binding.iv.context)
                .load(data.image)
                .into(binding.iv)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemHowStressListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setDataSet(dataSet: List<HowStressResponse>) {
        mDataSet = dataSet as ArrayList<HowStressResponse>
        notifyDataSetChanged()
    }
}