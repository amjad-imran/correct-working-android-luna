package com.noisefit.ui.challengeNew.detail

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.response.LeaderBoard
import com.noisefit.luna.databinding.RowChallengeLeaderDetailsBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadCircleImage
import com.noisefit_commans.ui.visible
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.models.Units


class ChallengeLeaderDetailsAdapter :
    RecyclerView.Adapter<ChallengeLeaderDetailsAdapter.MyViewHolder>() {

    private var mDataSet = ArrayList<com.noisefit_commans.data.response.LeaderBoard>()
    private var mUserId: Int = -1
    private var challengeType: String = ""
    private var unit: Units? = null


    inner class MyViewHolder(private val binding: RowChallengeLeaderDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(leaderBoard: com.noisefit_commans.data.response.LeaderBoard) {
            binding.tvRank.text = "${leaderBoard.user_rank}"
            binding.tvName.text = leaderBoard.first_name

            val unitsValue = unit?.let { returnUnitValue(challengeType, it) } ?: ""

            if (challengeType.equals("distance", true)) {
                binding.tvAchieved.text = "${leaderBoard.progress}" + unitsValue
            } else {
                binding.tvAchieved.text = "${leaderBoard.progress?.toInt()}" + unitsValue
            }

            binding.vProfile.loadCircleImage(
                binding.vProfile.context,
                leaderBoard.image_url ?: "",
                R.drawable.ic_default_profile_image
            )

            if ((bindingAdapterPosition + 1) == mDataSet.size) {
                binding.include27.root.gone()
            } else {
                binding.include27.root.visible()
            }

            if (leaderBoard.user_id == mUserId) {
                binding.clMain.setBackgroundColor(Color.parseColor("#66ffba00"))
            } else {
                binding.clMain.setBackgroundColor(Color.parseColor("#00000000"))
            }

            binding.executePendingBindings()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            RowChallengeLeaderDetailsBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size

    fun setDataSet(userId: Int, data: List<com.noisefit_commans.data.response.LeaderBoard>, challengeType: String, units: Units) {
        mUserId = userId
        mDataSet.clear()
        this.challengeType = challengeType
        unit = units
        mDataSet.addAll(data)
        notifyDataSetChanged()
    }

    interface ChallengeLeaderInteractionListener {
    }

    fun returnUnitValue(challengeType: String, units: Units): String {
        val unitsValue: String
        when (challengeType) {
            "distance" -> {
                unitsValue = " " + units.let {
                    ApplicationUtils.getChallengeTypeUnit(
                        challengeType,
                        it
                    )
                }.toString()
            }
            "step" -> {
                unitsValue = " " + units.let {
                    ApplicationUtils.getChallengeTypeUnit(
                        challengeType,
                        it
                    )
                }.toString()
            }
            else -> {
                unitsValue = " " + units.let {
                    ApplicationUtils.getChallengeTypeUnit(
                        challengeType,
                        it
                    )
                }.toString()
            }
        }
        return unitsValue
    }

}