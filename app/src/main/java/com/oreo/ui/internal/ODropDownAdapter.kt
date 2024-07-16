package com.oreo.ui.internal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemRvDropDownBinding
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.ODropDownDataModel

class ODropDownAdapter(val listener: ODDItemClickListener) :
    RecyclerView.Adapter<ODropDownAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<ODropDownDataModel>()
    private var selectedPost = -1


    inner class ViewHolder(val binding: ItemRvDropDownBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: ODropDownDataModel) {
            binding.ivIcon.loadImage(binding.ivIcon.context, resultData.icon)
            binding.tvTitle.text = resultData.title
            if (selectedPost == bindingAdapterPosition) {
                binding.lytMain.setBackgroundResource(R.drawable.back_selected_drop_down)
            } else {
                binding.lytMain.setBackgroundResource(R.drawable.back_non_selected_drop_down)
            }

            binding.root.setOnClickListener {
                listener.onItemClick(resultData, bindingAdapterPosition)
                selectedPost=bindingAdapterPosition
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemRvDropDownBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(resultData: List<ODropDownDataModel>) {

        mDataSet.clear()
        mDataSet.addAll(resultData)
        selectedPost=-1
        notifyDataSetChanged()
    }

    interface ODDItemClickListener {
        fun onItemClick(resultData: ODropDownDataModel, position: Int)
    }
}

