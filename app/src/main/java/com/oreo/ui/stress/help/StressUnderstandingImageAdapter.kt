package com.oreo.ui.stress.help

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemStressImageHowItWorksBinding
import com.noisefit.luna.databinding.ItemStressUnderstandingSubListBinding
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.StressUnderstandingSubList


class StressUnderstandingImageAdapter(val listener: StressInfoCardAction) :
    RecyclerView.Adapter<StressUnderstandingImageAdapter.ViewHolder>() {


    private var mDataSet = ArrayList<StressImageModel>()

    inner class ViewHolder(val binding: ItemStressImageHowItWorksBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StressImageModel, position: Int) {

            binding.imv.loadImage(binding.imv.context, data.image)

            binding.tvTitle.text = data.title
            binding.tvMessage.text = data.content

            binding.root.setOnClickListener {
                listener.onStressInfoCardClicked()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemStressImageHowItWorksBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    override fun getItemCount() = mDataSet.size
    fun setDataSet(list: List<StressImageModel>) {
        mDataSet.clear()
        mDataSet.addAll(list)
        notifyDataSetChanged()
    }
}
interface StressInfoCardAction {
    fun onStressInfoCardClicked()
}

data class StressImageModel(
    val title: String,
    val content: String,
    val image: Int
)