package com.noisefit.ui.workout.session

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowActivityOptionBinding
import com.noisefit_commans.models.SportsModeList


class WorkoutListingAdapter(
    private val listener: CardClickListener?
) :
    RecyclerView.Adapter<WorkoutListingAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<SportsModeList.SportsMode>()

    inner class ViewHolder(private val binding: RowActivityOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(summary: SportsModeList.SportsMode) {
            binding.root.setOnClickListener {
                listener?.onCardClicked(summary)
            }
            binding.tvActivityName.text = summary.name?.replace("_", " ") ?: ""
            binding.tvLastSession.text = summary.text

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowActivityOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setDataSet(dataSet: List<SportsModeList.SportsMode>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    fun add(summary: SportsModeList.SportsMode) {
        val itemCount = this.itemCount
        mDataSet.add(itemCount, summary)
        notifyItemInserted(itemCount)

    }
}

interface CardClickListener {
    fun onCardClicked(summary: SportsModeList.SportsMode)
}

data class SportsModeOptions(
    var name: String? = null,
    var type: Int? = 0,
    var value: Boolean,
    var lastSessionText: String? = null
)