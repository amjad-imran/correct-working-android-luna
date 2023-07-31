package com.noisefit.ui.npl.dashboard

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemPredictWinBinding
import com.noisefit.ui.common.*
import com.noisefit_commans.data.response.LiveMatch
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.numberFormatter
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.prettyCountDecimal

class NPLWinAdapter(val listener: PredictWinnerActions) :
    RecyclerView.Adapter<NPLWinAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<LiveMatch>()

    inner class ViewHolder(val binding: ItemPredictWinBinding) :
        RecyclerView.ViewHolder(binding.rootView) {
        var timer: CountDownTimer? = null
        fun bind(matchData: LiveMatch) {

            binding.layoutPredictWin.tvTeam1.text = matchData.teamA?.getTeamNames()
            binding.layoutPredictWin.tvTeam2.text = matchData.teamB?.getTeamNames()
            binding.layoutPredictWin.tvPredictValue.text =
                (matchData.predictionCount ?: 0L).numberFormatter()

            binding.layoutPredictWin.bPredictWinner.setOnClickListener {
                listener.onPredictClicked(matchData)
            }

            binding.layoutPredictWin.lytTeam1.ivTeam1.loadImage(
                binding.layoutPredictWin.lytTeam1.ivTeam1.context,
                matchData.teamA?.imageUrl
            )

            binding.layoutPredictWin.lytTeam2.ivTeam1.loadImage(
                binding.layoutPredictWin.lytTeam2.ivTeam1.context,
                matchData.teamB?.imageUrl
            )

            binding.tvDate.text = DateFormats.formatTimeNpl(matchData.startAt)

            if (matchData.userTeam != null) {
                //predicted state

                binding.layoutPredictWin.bPredictWinner.gone()

                binding.layoutPredictWin.pbSteps.visible()
                binding.layoutPredictWin.vBack.visible()


                binding.layoutPredictWin.tvTeamAPrediction.visible()
                binding.layoutPredictWin.tvTeamBPrediction.visible()

                binding.layoutPredictWin.tvTeamAPrediction.text =
                    "${(matchData.teamA?.meter ?: 0).prettyCountDecimal()}%"
                binding.layoutPredictWin.tvTeamBPrediction.text =
                    "${(matchData.teamB?.meter ?: 0).prettyCountDecimal()}%"

                if (matchData.teamA?.teamId == matchData.userTeam) {
                    binding.layoutPredictWin.lytTeam1.viewStroke.visible()
                    binding.layoutPredictWin.lytTeam2.viewStroke.gone()

                    binding.layoutPredictWin.pbSteps.apply {
                        progress = (matchData.teamA?.meter ?: 0f).toInt()
                        indicatorDirection =
                            LinearProgressIndicator.INDICATOR_DIRECTION_LEFT_TO_RIGHT
                    }
                    binding.layoutPredictWin.pbSteps.progress =
                        (matchData.teamA?.meter ?: 0f).toInt()
//
//                    binding.layoutPredictWin.lytTeam1.ivTop.setImageResource(R.drawable.ic_team_seleced)

                    binding.layoutPredictWin.lytTeam1.ivTop.setImageResource(R.drawable.ic_team_seleced)
                    binding.layoutPredictWin.lytTeam1.ivTop.visible()

                    binding.layoutPredictWin.lytTeam2.ivTop.gone()
                } else {
                    binding.layoutPredictWin.lytTeam1.viewStroke.gone()
                    binding.layoutPredictWin.lytTeam2.viewStroke.visible()
                    binding.layoutPredictWin.lytTeam1.ivTop.gone()

                    binding.layoutPredictWin.pbSteps.apply {
                        progress = (matchData.teamB?.meter ?: 0f).toInt()
                        indicatorDirection =
                            LinearProgressIndicator.INDICATOR_DIRECTION_RIGHT_TO_LEFT
                    }
//                    binding.layoutPredictWin.pbSteps.progress = (matchData.teamB?.meter ?: 0f).toInt()

                    binding.layoutPredictWin.lytTeam2.ivTop.visible()

                    binding.layoutPredictWin.lytTeam2.ivTop.setImageResource(R.drawable.ic_team_seleced)
                }

            } else {
                binding.layoutPredictWin.bPredictWinner.visible()

                binding.layoutPredictWin.pbSteps.gone()
                binding.layoutPredictWin.vBack.gone()
                binding.layoutPredictWin.tvTeamAPrediction.gone()
                binding.layoutPredictWin.tvTeamBPrediction.gone()

                if (matchData.userSelectedTeamId == null) {
                    binding.layoutPredictWin.lytTeam1.ivTop.setImageResource(R.drawable.ic_rb_unselected)
                    binding.layoutPredictWin.lytTeam2.ivTop.setImageResource(R.drawable.ic_rb_unselected)
                    binding.layoutPredictWin.lytTeam1.viewStroke.gone()
                    binding.layoutPredictWin.lytTeam2.viewStroke.gone()

                } else {
                    if (matchData.teamA?.teamId == matchData.userSelectedTeamId) {
                        binding.layoutPredictWin.lytTeam1.viewStroke.visible()
                        binding.layoutPredictWin.lytTeam2.viewStroke.gone()
                        binding.layoutPredictWin.lytTeam1.ivTop.setImageResource(R.drawable.ic_rb_selected)
                        binding.layoutPredictWin.lytTeam2.ivTop.setImageResource(R.drawable.ic_rb_unselected)
                    } else {
                        binding.layoutPredictWin.lytTeam1.viewStroke.gone()
                        binding.layoutPredictWin.lytTeam2.viewStroke.visible()
                        binding.layoutPredictWin.lytTeam2.ivTop.setImageResource(R.drawable.ic_rb_selected)
                        binding.layoutPredictWin.lytTeam1.ivTop.setImageResource(R.drawable.ic_rb_unselected)
                    }
                }
            }




            binding.layoutPredictWin.bPredictWinner.setOnClickListener {

                if (matchData.userSelectedTeamId == null) {
                    it.context.showShortToast("Please select a team")
                    return@setOnClickListener
                }

                listener.onPredictClicked(matchData)


            }

            binding.ivShare.setOnClickListener {
                listener.onShareClicked()
            }

            binding.layoutPredictWin.lytTeam1.root.setOnClickListener {
                if (matchData.userTeam != null) return@setOnClickListener
                matchData.userSelectedTeamId = matchData.teamA?.teamId
                listener.onTeamSelection(matchData)
                notifyItemChanged(bindingAdapterPosition)
            }
            binding.layoutPredictWin.lytTeam2.root.setOnClickListener {
                if (matchData.userTeam != null) return@setOnClickListener
                matchData.userSelectedTeamId = matchData.teamB?.teamId
                listener.onTeamSelection(matchData)
                notifyItemChanged(bindingAdapterPosition)
            }


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemPredictWinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val matchData = mDataSet[position]
        holder.bind(matchData)

        if (holder.timer != null) {
            holder.timer?.cancel();
        }
        holder.timer = object : CountDownTimer(((matchData.elapsed_time ?: 0L) * 1000), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                matchData.elapsed_time = millisUntilFinished / 1000
                val totalSecs = millisUntilFinished / 1000
                val hours = totalSecs / 3600
                val minutes = (totalSecs % 3600) / 60
                val seconds = totalSecs % 60
                val tempHour: String = if (hours < 10)
                    "0$hours"
                else
                    "$hours"
                val tempMin: String = if (minutes < 10)
                    "0$minutes"
                else
                    "$minutes"
                val tempSec: String = if (seconds < 10)
                    "0$seconds"
                else
                    "$seconds"

                holder.binding.layoutPredictWin.tvTime.text =
                    "Time Left : $tempHour : $tempMin : $tempSec"
            }

            override fun onFinish() {
                //hide view
                removeMatch(matchData.match_id ?: -1)
            }
        }
        holder.timer?.start()
    }

    fun setDataSet(dataSet: List<LiveMatch>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    fun removeMatch(matchId: Long) {

        val index = mDataSet.indexOfFirst {
            it.match_id == matchId
        }

        if (index != -1) {
            Handler(Looper.getMainLooper()).post {
                if (mDataSet.size > index) {
                    mDataSet.removeAt(index)
                    notifyItemRemoved(index)
                }
            }

        }

    }


}

interface PredictWinnerActions {
    fun onShareClicked()
    fun onPredictClicked(matchData: LiveMatch)
    fun onTeamSelection(matchData: LiveMatch)
}