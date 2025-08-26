package com.oreo.ui.stress.help

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemStressImageHowItWorksBinding
import com.noisefit.luna.databinding.ItemStressImageTitleOnlyHowItWorksBinding
import com.noisefit.luna.databinding.ItemStressUnderstandingSubListBinding
import com.noisefit_commans.ui.loadImage
import com.oreo.data.model.StressUnderstandingSubList


class StressUnderstandingImageAdapter(val listener: StressInfoCardAction) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var mDataSet = ArrayList<HowItWorksModel>()

    inner class StressViewHolder(val binding: ItemStressImageHowItWorksBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: HowItWorksModel.StressImageModel, position: Int) {

            binding.imv.loadImage(binding.imv.context, data.image)

            binding.tvTitle.text = data.title
            binding.tvMessage.text = data.content

            binding.root.setOnClickListener {
                listener.onStressInfoCardClicked()
            }
        }

    }

    inner class CircadianViewHolder(val binding: ItemStressImageTitleOnlyHowItWorksBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: HowItWorksModel.CircadianHowItWorksModel, position: Int) {

            binding.imv.loadImage(binding.imv.context, data.image)

            binding.tvTitle.text = data.title

            binding.root.setOnClickListener {
                listener.onCircadianCardClicked(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            R.layout.item_stress_image_how_it_works -> StressViewHolder(
                ItemStressImageHowItWorksBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            R.layout.item_stress_image_title_only_how_it_works -> CircadianViewHolder(
                ItemStressImageTitleOnlyHowItWorksBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> throw Exception()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(mDataSet[position]){
            is HowItWorksModel.StressImageModel -> (holder as StressViewHolder).bind(mDataSet[position] as HowItWorksModel.StressImageModel, position)
            is HowItWorksModel.CircadianHowItWorksModel -> (holder as CircadianViewHolder).bind(mDataSet[position] as HowItWorksModel.CircadianHowItWorksModel, position)
        }
    }

    override fun getItemCount() = mDataSet.size
    fun setDataSet(list: List<HowItWorksModel>) {
        mDataSet.clear()
        mDataSet.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when(mDataSet[position]){
            is HowItWorksModel.StressImageModel -> R.layout.item_stress_image_how_it_works
            is HowItWorksModel.CircadianHowItWorksModel -> R.layout.item_stress_image_title_only_how_it_works
        }
    }
}
interface StressInfoCardAction {
    fun onStressInfoCardClicked()
    fun onCircadianCardClicked(pos: Int)
}

sealed class HowItWorksModel(){
    data class StressImageModel(
        val title: String,
        val content: String,
        val image: Int
    ): HowItWorksModel()

    data class CircadianHowItWorksModel(
        val title: String,
        val image: Int,
    ): HowItWorksModel()
}