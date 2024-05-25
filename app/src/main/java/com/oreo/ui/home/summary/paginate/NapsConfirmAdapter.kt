package com.oreo.ui.home.summary.paginate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowNapConfirmBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.model.OreoNapData
import com.noisefit_commans.utils.DateFormats

class NapsConfirmAdapter(val listener: NapConfirmAction) :
    RecyclerView.Adapter<NapsConfirmAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<OreoNapData>()
    private var mDate: String? = null

    inner class ViewHolder(private val binding: RowNapConfirmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(nap: OreoNapData) {
            val (hour, minute) = ApplicationUtils.getFormattedSleepDuration(nap.duration ?: 0)
            val time = if (hour == 0) {
                "$minute min"
            } else {
                "$hour hr $minute min"
            }

            val timeBuilder = StringBuilder()
            if (mDate != null && !nap.date.equals(mDate)) {
                timeBuilder.append("Yesterday ")
            }
            timeBuilder.append(
                DateFormats.parseDate(
                    nap.startTime ?: "",
                    DateFormats.dateTimeFormat5(),
                    DateFormats.timeFormat12()
                )?.lowercase()
            )

            binding.tvNapDuration.text = time

            binding.tvNapTime.text = timeBuilder.toString()



            binding.ivRemoveNap.setOnClickListener {
                listener.onNapRemoveClicked(nap)
            }
            binding.btnConfirm.setOnClickListener {
                listener.onNapConfirmClicked(nap)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            RowNapConfirmBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setDataSet(dataSet: List<OreoNapData>, date: String?) {
        mDate = date
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}

interface NapConfirmAction {
    fun onNapConfirmClicked(nap: OreoNapData)
    fun onNapRemoveClicked(nap: OreoNapData)
}