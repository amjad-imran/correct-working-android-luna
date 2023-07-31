package com.noisefit.ui.settings.helpAndSupport

import android.graphics.drawable.TransitionDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit_commans.data.response.HelpAndSupportResponse
import com.noisefit.databinding.ItemHelpSupportListBinding
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.tryCatch

class HelpAndSupportAdapter(val listener: HelpAndSupportInteractionListener) :
    RecyclerView.Adapter<HelpAndSupportAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<HelpAndSupportResponse>()
    private var highlightedItem = -1

    inner class ViewHolder(private val binding: ItemHelpSupportListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(value: HelpAndSupportResponse) {
            binding.tvTitle.text = value.title
            binding.tvSubtitle.text = value.subtitle
            binding.imv.loadImage(binding.imv.context, value.icon)

            if (highlightedItem == bindingAdapterPosition) {
                binding.container.setBackgroundResource(R.drawable.back_highlight_anim)
                val frameAnimation = binding.container.background as TransitionDrawable
                frameAnimation.startTransition(500)
                Handler(Looper.getMainLooper()).postDelayed({
                    tryCatch {
                        frameAnimation.reverseTransition(500)
                    }
                },500)
                highlightedItem = -1
            } else {
                binding.container.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
            }

            binding.container.setOnClickListener {
                listener.onHelpAndSupportClick(value)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HelpAndSupportAdapter.ViewHolder {
        val binding = ItemHelpSupportListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HelpAndSupportAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataList: List<HelpAndSupportResponse>) {
        mDataSet.clear()
        mDataSet.addAll(dataList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size

    fun highlightTopic(highlightTopic: HelpAndSupportType) {
        highlightedItem = 0
        notifyItemChanged(0)
    }


}

interface HelpAndSupportInteractionListener {
    fun onHelpAndSupportClick(helpAndSupportResponse: HelpAndSupportResponse)
}
