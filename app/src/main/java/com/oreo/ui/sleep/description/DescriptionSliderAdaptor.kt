package com.oreo.ui.sleep.description

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ViewDescriptionSliderBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.oreo.data.model.Contributors
import com.oreo.ui.sleep.scoredetails.ClickViewType

class DescriptionSliderAdaptor : RecyclerView.Adapter<DescriptionSliderAdaptor.ViewHolder>() {
    private var mDataSet = ArrayList<Contributors>()
    private var type: String? = null

    inner class ViewHolder(private val binding: ViewDescriptionSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Contributors) {
            binding.lytTopSubItem.tvTitle.text = data.title
            binding.lytTopSubItem.tvRemark.text = data.leftText
            if (data.hasData.not()) {
                binding.lytContributor.root.visible()
                binding.bgImv.visible()

                var drawable: Int? = null
                when (type) {
                    ClickViewType.READINESS.name -> {
                        drawable = R.drawable.bg_readiness_contributor_disabled
                    }

                    ClickViewType.SLEEP.name -> {
                        drawable = R.drawable.bg_sleep_contributor_disabled
                    }

                    ClickViewType.ACTIVITY.name -> {
                        drawable = R.drawable.bg_activity_contributor_disabled
                    }
                }

                drawable?.let {
                    binding.bgImv.loadImage(binding.bgImv.context, drawable)
                }

            } else {
                binding.lytContributor.root.gone()
                binding.bgImv.gone()
            }

            binding.lytTopSubItem.pbSteps.progress = if (data.hasData && data.barPercent == 0) {
                1
            } else {
                data.barPercent
            }

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

    fun setDataSet(dataSet: ArrayList<Contributors>, type: String?) {
        mDataSet.clear()
        this.type = type
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}