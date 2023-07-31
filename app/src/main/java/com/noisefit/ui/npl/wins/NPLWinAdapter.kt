package com.noisefit.ui.npl.wins

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemBannerWinBinding
import com.noisefit_commans.data.response.LiveMatch
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadCircleImage
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats

class NPLWinAdapter(val listener: OnCollectRewardListener) :
    RecyclerView.Adapter<NPLWinAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<LiveMatch>()

    inner class ViewHolder(val binding: ItemBannerWinBinding) :
        RecyclerView.ViewHolder(binding.rootView) {
        fun bind(resultData: LiveMatch) {
            binding.layoutPredictWin.tvTeam1.text = resultData.teamA?.getTeamNames()
            binding.layoutPredictWin.lytTeam1.ivTeam1.loadCircleImage(
                binding.layoutPredictWin.lytTeam1.ivTeam1.context,
                resultData.teamA?.imageUrl
            )
            binding.layoutPredictWin.tvTeam2.text = resultData.teamB?.getTeamNames()
            binding.layoutPredictWin.lytTeam2.ivTeam1.loadCircleImage(
                binding.layoutPredictWin.lytTeam2.ivTeam1.context,
                resultData.teamB?.imageUrl
            )

            binding.tvDate.text = DateFormats.formatTimeNpl(resultData.startAt)


            if (resultData.winningTeam == resultData.teamA?.teamId) {

                binding.layoutPredictWin.tvWon.text = "Won"
                binding.layoutPredictWin.tvLost.text = "Lost"
                binding.layoutPredictWin.tvWon.setTextColor(
                    ContextCompat.getColor(
                        binding.layoutPredictWin.tvWon.context,
                        R.color.steps_arc
                    )
                )
                binding.layoutPredictWin.tvLost.setTextColor(
                    ContextCompat.getColor(
                        binding.layoutPredictWin.tvWon.context,
                        R.color.white_64
                    )
                )
                binding.layoutPredictWin.tvTeam1.setTextColor(ContextCompat.getColor(
                    binding.layoutPredictWin.tvTeam1.context,
                    R.color.white
                ))
                binding.layoutPredictWin.tvTeam2.setTextColor(ContextCompat.getColor(
                    binding.layoutPredictWin.tvTeam1.context,
                    R.color.white_48
                ))

                binding.layoutPredictWin.lytTeam2.viewStroke.gone()
                binding.layoutPredictWin.lytTeam2.ivTop.gone()

                binding.layoutPredictWin.lytTeam1.viewStroke.visible()
                binding.layoutPredictWin.lytTeam1.ivTop.visible()
                binding.layoutPredictWin.lytTeam1.ivTop.setImageResource(R.drawable.ic_accept_duotone)
            } else {

                binding.layoutPredictWin.tvLost.text = "Won"
                binding.layoutPredictWin.tvWon.text = "Lost"
                binding.layoutPredictWin.tvLost.setTextColor(
                    ContextCompat.getColor(
                        binding.layoutPredictWin.tvWon.context,
                        R.color.steps_arc
                    )
                )
                binding.layoutPredictWin.tvWon.setTextColor(
                    ContextCompat.getColor(
                        binding.layoutPredictWin.tvWon.context,
                        R.color.white_64
                    )
                )

                binding.layoutPredictWin.tvTeam1.setTextColor(ContextCompat.getColor(
                    binding.layoutPredictWin.tvTeam1.context,
                    R.color.white_48
                ))
                binding.layoutPredictWin.tvTeam2.setTextColor(ContextCompat.getColor(
                    binding.layoutPredictWin.tvTeam1.context,
                    R.color.white
                ))
                binding.layoutPredictWin.lytTeam1.viewStroke.gone()
                binding.layoutPredictWin.lytTeam1.ivTop.gone()

                binding.layoutPredictWin.lytTeam2.viewStroke.visible()
                binding.layoutPredictWin.lytTeam2.ivTop.visible()
                binding.layoutPredictWin.lytTeam2.ivTop.setImageResource(R.drawable.ic_accept_duotone)
            }

            binding.layoutPredictWin.bPredictWinner.setOnClickListener {
                resultData.prediction_id?.let { it1 ->
                    listener.onCollectReward(
                        it1,
                        "",
                        bindingAdapterPosition
                    )
                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemBannerWinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(resultData: ArrayList<LiveMatch>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        tryCatch {
            mDataSet.removeAt(position)
            notifyItemChanged(position)
            notifyDataSetChanged()
        }
    }

}

interface OnCollectRewardListener {
    fun onCollectReward(predictionId: Long, title: String, position: Int)
}