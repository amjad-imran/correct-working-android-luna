package com.noisefit.ui.onboarding.onboardProfile.endgame

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit_commans.data.model.EndGame
import com.noisefit.databinding.ItemEndgameLayoutBinding
import com.noisefit_commans.ui.loadImage


class EndGameSelectionAdapter : RecyclerView.Adapter<EndGameSelectionAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<EndGame>()
    private var onEndGameInteractionListener: OnEndGameInteractionListener? = null

    private var selectedPosition = -1

    inner class ViewHolder(private val binding: ItemEndgameLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(endGame: EndGame, position: Int) {
            binding.tvTitle.text = endGame.title
            binding.tvSubHeading.text = endGame.subTitle

            if (selectedPosition == position) {
                binding.tvTitle.setTextColor(binding.tvTitle.context.resources.getColor(R.color.tab_text_selected_color))
                binding.tvSubHeading.setTextColor(binding.tvTitle.context.resources.getColor(R.color.tab_text_selected_color))
                binding.container.background = AppCompatResources.getDrawable(
                    binding.container.context,
                    R.drawable.selected_rounded_corner
                )
            } else {
                binding.tvTitle.setTextColor(binding.tvTitle.context.resources.getColor(R.color.tab_text_unselected_color))
                binding.tvSubHeading.setTextColor(binding.tvTitle.context.resources.getColor(R.color.tab_text_unselected_color))
                binding.container.background = AppCompatResources.getDrawable(
                    binding.container.context,
                    R.drawable.unselected_rounded_corner
                )
            }

            binding.imv.loadImage(binding.imv.context, endGame.image)

            binding.container.setOnClickListener {
                selectedPosition = position
                onEndGameInteractionListener?.onEndGameSelected(endGame, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEndgameLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    fun setOnEndGameInteractionListener(onEndGameInteractionListener: OnEndGameInteractionListener) {
        this.onEndGameInteractionListener = onEndGameInteractionListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], position)
    }

    fun setState(isSelected: Boolean, position: Int) {
        selectedPosition = position
        mDataSet[position].apply {
            this.selectd = isSelected
        }
        notifyItemChanged(position)
    }

    fun setSelectedPosition(selected:Int){
        selectedPosition = selected
    }
    fun setDataSet(data: ArrayList<EndGame>) {
        mDataSet.clear()
        mDataSet.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size

    fun selectEndGame(localEndGame: Int) {
        mDataSet.forEachIndexed { index, endGame ->
            if (endGame.id == localEndGame) {
                selectedPosition = index
                onEndGameInteractionListener?.onEndGameSelected(endGame, index)
            }
        }
    }

    interface OnEndGameInteractionListener {
        fun onEndGameSelected(endGame: EndGame, position: Int)
    }

}