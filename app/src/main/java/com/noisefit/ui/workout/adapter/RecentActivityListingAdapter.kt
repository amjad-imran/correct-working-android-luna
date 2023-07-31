package com.noisefit.ui.workout.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.data.dataConverter.DataUnitConverter
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.util.ImageUtil
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.DateFormats

class RecentWorkoutListingAdapter(
    private val listener: RecentActivityActions,
    private val dataUnitConverter: DataUnitConverter
) : RecyclerView.Adapter<RecentWorkoutListingAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<SportsModeResponse>()

    var unitsSystem = Units.METRIC
    var connectedDevice: String = ""

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(activity: SportsModeResponse, mode: ItemPos) {

            view.setOnClickListener {
                if (!activity.isHeader) {
                    listener.onActivitySelected(activity)
                }
            }
            if (itemViewType == RecentActivityViewType.HEADER.type) {
                view.findViewById<TextView>(R.id.tvDate).text = activity.date
            } else {
                when (mode) {
                    ItemPos.TOP -> {
                        view.findViewById<View>(R.id.parentContainer)
                            .setBackgroundResource(R.drawable.top_rounded_back)
                        view.findViewById<View>(R.id.view16).visible()
                    }
                    ItemPos.CENTRE -> {
                        view.findViewById<View>(R.id.parentContainer)
                            .setBackgroundResource(R.drawable.center_flat_back)
                        view.findViewById<View>(R.id.view16).visible()
                    }
                    ItemPos.BOTTOM -> {
                        view.findViewById<View>(R.id.parentContainer)
                            .setBackgroundResource(R.drawable.bottom_rounded_back)
                        view.findViewById<View>(R.id.view16).gone()
                    }
                    ItemPos.DEFAULT -> {
                        view.findViewById<View>(R.id.parentContainer)
                            .setBackgroundResource(R.drawable.back_recycler_workout_dark)
                        view.findViewById<View>(R.id.view16).gone()
                    }
                }

                view.findViewById<TextView>(R.id.tvName).text =
                    activity.getFormattedActivityName()

                val activityName = activity.type ?: activity.activityType
                view.findViewById<ImageView>(R.id.imageView8).setImageResource(ImageUtil().getImageFromActivity(activityName))

                if (activity.distance != null && activity.distance!! > 0) {

                    view.findViewById<TextView>(R.id.tvDistance).text =
                        dataUnitConverter.formatDistance(
                            activity.distance?.toInt() ?: 0,
                            unitsSystem
                        )
                    view.findViewById<TextView>(R.id.tvDistanceUnit).text = dataUnitConverter.distanceUnit(unitsSystem)
                } else {
                    view.findViewById<TextView>(R.id.tvDistance).text = activity.calories.toString()
                    view.findViewById<TextView>(R.id.tvDistanceUnit).text = "kcal"
                }


                view.findViewById<TextView>(R.id.tvDate).text = DateFormats.formatActivityTime(activity.time)

                /*val tempDateValue = getDateInMMMDDYYYY(activity.date)
                val date = tempDateValue?.substring(0, 2)
                val month = tempDateValue?.substring(3, 7)
                val year = tempDateValue?.substring(8, 12)
                val tempDate: String =
                    if (date == "01" || date == "02" || date == "03" || date == "04" || date == "05" || date == "06" || date == "07" || date == "08" || date == "09") {
                        date.replace("0", "")
                    } else
                        date.toString()

                    tempDate + getDayOfMonthSuffix(tempDate.roundToInt()) + " " + month + " " + year*/
            }
        }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if (viewType == RecentActivityViewType.HEADER.type) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_recent_activity_header, parent, false)
            return ViewHolder(view = view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_recent_activity, parent, false)
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

    fun setDataSet(dataSet: List<SportsModeResponse>) {
        mDataSet = dataSet as ArrayList<SportsModeResponse>
        notifyDataSetChanged()
    }
}

enum class RecentActivityViewType(val type: Int) {
    HEADER(0), DATA(1)
}

interface RecentActivityActions {
    fun onActivitySelected(activity: SportsModeResponse)
}

enum class ItemPos {
    TOP, CENTRE, BOTTOM, DEFAULT
}