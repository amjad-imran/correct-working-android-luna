package com.oreo.ui.sleep.description

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ViewDescriptionSliderBinding
import com.oreo.data.model.Contributors

class DescriptionSliderAdaptor : RecyclerView.Adapter<DescriptionSliderAdaptor.ViewHolder>() {
    private var mDataSet = ArrayList<Contributors>()

    inner class ViewHolder(private val binding: ViewDescriptionSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Contributors) {
            binding.lytTopSubItem.tvTitle.text = data.title
            binding.lytTopSubItem.tvRemark.text = data.leftText
            binding.lytTopSubItem.pbSteps.progress = data.barPercent
            val progressColor = ContextCompat.getColor(
                binding.lytTopSubItem.pbSteps.context,
                data.barColor
            )
            val leftTextColor = ContextCompat.getColor(
                binding.lytTopSubItem.pbSteps.context,
                data.leftTextColor
            )
            binding.lytTopSubItem.pbSteps.setIndicatorColor(progressColor)
//            binding.lytBottomSubItem.tvBottomTitle.text = data.title
            binding.lytTopSubItem.tvRemark.setTextColor(leftTextColor)
            binding.lytBottomSubItem.tvDescription.text = Html.fromHtml(data.description)


        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            ViewDescriptionSliderBinding.inflate(
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

    fun setDataSet(dataSet: ArrayList<Contributors>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}