package com.noisefit.ui.dashboard.feature.worldclock

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowWorldClockBinding
import com.noisefit_commans.ui.gone
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit_commans.ui.visible
import com.noisefit_commans.models.WorldClockList
import com.noisefit_commans.utils.DateFormats
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt
import kotlin.math.truncate

class WorldClockAdapter(private val action: WorldClockRowAction) :
    RecyclerView.Adapter<WorldClockAdapter.ViewHolder>(), ItemMoveCallbackListener.Listener {

    private var mDataSet = ArrayList<WorldClockList.WClock>()

    private var mEditMode = false

    fun setEditMode(status: Boolean) {
        mEditMode = status
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: RowWorldClockBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(clock: WorldClockList.WClock) {
            binding.tvTimeZoneN.text = clock.content


            val offsetNew1 = (clock.timeZone * 15)
            val hours = truncate(offsetNew1 / 60f).roundToInt()
            var minutes = offsetNew1 % 60
            if (minutes < 0) {
                minutes *= -1
            }

            val offset =Date().timezoneOffset
                //cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET) / (60 * 1000) //TODO research

            val offsetNew = (clock.timeZone * 15) + offset
            val millis = Date().time + (offsetNew * 60 * 1000)

            val timeZoneString = StringBuilder()
            timeZoneString.append("GMT")
            if (hours >= 0) {
                timeZoneString.append("+")
            }
            timeZoneString.append(hours)

            if (minutes > 0) {
                timeZoneString.append(":")
                timeZoneString.append(minutes)
            }
            timeZoneString.append(" hr")

            binding.tvTimeZone.text = timeZoneString.toString()

            binding.tvTime.text = DateFormats.getDateFromMillis(millis, "HH:mm")
            binding.tvDate.text = DateFormats.getDateFromMillis(millis, "MM/dd")






            if (mEditMode) {
                binding.ivRemove.visible()
                binding.ivDrag.visible()
            } else {
                binding.ivRemove.gone()
                binding.ivDrag.gone()
            }
            binding.root.setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {
                    action.onLongPressed(bindingAdapterPosition)
                    return true
                }
            })
            binding.ivDrag.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    action.onStartDrag(this)
                }
                return@setOnTouchListener true
            }


            binding.ivRemove.setOnClickListener {
                action.onItemRemoved(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowWorldClockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])


    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(mDataSet, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(mDataSet, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }


    fun setDataSet(alarms: List<WorldClockList.WClock>) {
        mDataSet = alarms as ArrayList<WorldClockList.WClock>
        notifyDataSetChanged()
    }

    fun getCurrentDataSet(): List<WorldClockList.WClock> {
        return mDataSet
    }

    fun removeItem(position: Int) {
        try {
            mDataSet.removeAt(position)
            notifyItemRemoved(position)
        }catch (exp : ArrayIndexOutOfBoundsException){
            exp.printStackTrace()
            //CASE : when Swap is in progress
        }

    }
}

interface WorldClockRowAction {
    fun onLongPressed(position: Int)
    fun onItemRemoved(position: Int)
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}