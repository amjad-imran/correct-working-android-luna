package com.oreo.ui.activity.all

import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.OActivityListModal


class OActivityListAdapter(
    private val listener: OActivityListInteraction,
    private val dataUnitConverter: DataUnitConverter
) : RecyclerView.Adapter<OActivityListAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<OActivityListModal>()

    var unitsSystem = Units.METRIC
    var connectedDevice: String = ""

    fun removeItem(position: Int) {
        try {
            mDataSet.removeAt(position)
            notifyItemRemoved(position)
        } catch (exp: ArrayIndexOutOfBoundsException) {
            exp.printStackTrace()
            //CASE : when Swap is in progress
        }

    }
    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(activity: OActivityListModal, mode: ItemPos) {

            view.setOnClickListener {
                if (!activity.isHeader) {
                    listener.onActivitySelected(activity,bindingAdapterPosition)
                }
            }
            if (itemViewType == RecentActivityViewType.HEADER.type) {
                var date = activity.createdDate
                if(date == DateFormats.getCurrentDate(DateFormats.dateFormat6)){
                    date = "Today’s Workouts"
                }
                view.findViewById<TextView>(R.id.tvDate).text = date
            } else {
                when (mode) {
                    ItemPos.TOP -> {
                        view.findViewById<View>(R.id.parentContainer)
                            .setBackgroundResource(R.drawable.o_top_rounded_back)
                        view.findViewById<View>(R.id.view16).visible()
                    }
                    ItemPos.CENTRE -> {
                        view.findViewById<View>(R.id.parentContainer)
                            .setBackgroundResource(R.drawable.o_center_flat_back)
                        view.findViewById<View>(R.id.view16).visible()
                    }
                    ItemPos.BOTTOM -> {
                        view.findViewById<View>(R.id.parentContainer).apply {
                            setBackgroundResource(R.drawable.o_bottom_rounded_back)
                            bottom = 16
                        }

                        view.findViewById<View>(R.id.view16).gone()
                    }

                    ItemPos.DEFAULT -> {
                        view.findViewById<View>(R.id.parentContainer).apply {
                            setBackgroundResource(R.drawable.back_modal_dialog)
                            bottom = 16
                        }

                        view.findViewById<View>(R.id.view16).gone()
                    }
                }

                view.findViewById<TextView>(R.id.tvName).text = activity.getFormattedActivityName()
                val time =  DateFormats.convert24HourTo12(activity.startTime)
                if(time.isNotEmpty()){
                    val timeArray = time.split(" ")
                    if(timeArray.isNotEmpty() && timeArray.size == 2){
                        view.findViewById<TextView>(R.id.tvStart).text = buildSpannedString {
                            append(timeArray[0])
                            inSpans(
                                ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.white_48))
                            ) {
                                append(" ${timeArray[1].lowercase()}")
                            }
                        }
                    }else{
                        view.findViewById<TextView>(R.id.tvStart).text = time
                    }
                }


                view.findViewById<TextView>(R.id.tvMin).text = buildSpannedString {
                    append(activity.duration.toString())
                    inSpans(
                        ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.white_48))
                    ) {
                        append(" min")
                    }
                }

                view.findViewById<TextView>(R.id.tvCalories).text = buildSpannedString {
                    append(activity.calories.toString())
                    inSpans(
                        ForegroundColorSpan(ContextCompat.getColor(view.context, R.color.white_48))
                    ) {
                        append(" kcal")
                    }
                }

            }
        }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if (viewType == RecentActivityViewType.HEADER.type) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_o_activity_list_header, parent, false)
            return ViewHolder(view = view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_o_activity_list_detail, parent, false)
            return ViewHolder(view = view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position], getBackgroundMode(position))
    }

    /**
     *mode 0 -> Centre
    mode 1 -> bottom
    mode -1 -> top
    mode 2 -> default
     */

    private fun getBackgroundMode(position: Int): ItemPos {
        if (!mDataSet[position].isHeader) {

            val nextItem = try {
                mDataSet[position + 1]
            } catch (exp: Exception) {
                null
            }
            val previousItem = try {
                mDataSet[position - 1]
            } catch (exp: Exception) {
                null
            } ?: return ItemPos.TOP

            if (nextItem == null) {
                if(previousItem.isHeader){
                    return ItemPos.DEFAULT
                }else{
                    return ItemPos.BOTTOM
                }
            }

            if (nextItem.isHeader && previousItem.isHeader) {
                return ItemPos.DEFAULT
            }


            if (previousItem.isHeader) {
                return ItemPos.TOP
            }
            if (nextItem.isHeader) {
                return ItemPos.BOTTOM
            }
            return ItemPos.CENTRE
        }
        return ItemPos.DEFAULT
    }

    override fun getItemCount() = mDataSet.size

    override fun getItemViewType(position: Int): Int {
        return if (mDataSet[position].isHeader) {
            RecentActivityViewType.HEADER.type
        } else {
            RecentActivityViewType.DATA.type
        }
    }

    fun setDataSet(dataSet: List<OActivityListModal>) {
        mDataSet = dataSet as ArrayList<OActivityListModal>
        notifyDataSetChanged()
    }
}

enum class RecentActivityViewType(val type: Int) {
    HEADER(0), DATA(1)
}

interface OActivityListInteraction {
    fun onActivitySelected(activity: OActivityListModal,position: Int)
}

enum class ItemPos {
    TOP, CENTRE, BOTTOM, DEFAULT
}