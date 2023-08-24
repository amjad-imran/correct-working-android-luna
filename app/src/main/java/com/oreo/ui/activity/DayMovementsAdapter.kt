package com.oreo.ui.activity

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.utils.Utils
import com.noisefit.luna.R
import com.noisefit.luna.databinding.LayoutDayMovementBinding
import com.noisefit.luna.databinding.OreoItemSleepContributorBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.ScreenUtils
import com.oreo.data.model.Contributors

class DayMovementsAdapter() :
    RecyclerView.Adapter<DayMovementsAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<Int>()

    inner class ViewHolder(val binding: LayoutDayMovementBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Int) {
            when (data) {
                0 -> {//inactive
                    binding.vBar.setBackgroundResource(R.drawable.back_movement_inactive)
                    val params = binding.vBar.layoutParams
                    params.height = dpToPx(11, binding.vBar.context).toInt()
                    binding.vBar.layoutParams = params
                }

                1 -> {//low
                    binding.vBar.setBackgroundResource(R.drawable.back_movement_low)
                    val params = binding.vBar.layoutParams
                    params.height = dpToPx(33, binding.vBar.context).toInt()
                    binding.vBar.layoutParams = params
                }

                2 -> {//medium
                    binding.vBar.setBackgroundResource(R.drawable.back_movement_medium)
                    val params = binding.vBar.layoutParams
                    params.height = dpToPx(67, binding.vBar.context).toInt()
                    binding.vBar.layoutParams = params
                }

                3 -> {//high
                    binding.vBar.setBackgroundResource(R.drawable.back_movement_high)
                    val params = binding.vBar.layoutParams
                    params.height = dpToPx(100, binding.vBar.context).toInt()
                    binding.vBar.layoutParams = params
                }

                else -> {// treat as inactive
                    binding.vBar.setBackgroundResource(R.drawable.back_movement_inactive)
                    val params = binding.vBar.layoutParams
                    params.height = dpToPx(11, binding.vBar.context).toInt()
                    binding.vBar.layoutParams = params
                }
            }

            setTime(bindingAdapterPosition)


        }

        private fun setTime(position: Int) {
            val value = when (position) {
                0 -> "12 am"
                23 -> "6 am"
                47 -> "12 pm"
                71 -> "6 pm"
                95 -> "12 am"
                else -> null
            }
            if (!value.isNullOrEmpty()) {
                binding.vTimeBar.visible()
                when (position) {
                    0 -> {
                        binding.tvTimeStart.visible()
                        binding.tvTime.invisible()
                        binding.tvTimeEnd.gone()
                        binding.tvTimeStart.text = value
                    }

                    (mDataSet.size - 1) -> {
                        binding.tvTimeStart.gone()
                        binding.tvTime.invisible()
                        binding.tvTimeEnd.visible()
                        binding.tvTimeEnd.text = value
                    }

                    else -> {
                        binding.tvTimeStart.gone()
                        binding.tvTime.visible()
                        binding.tvTimeEnd.gone()
                        binding.tvTime.text = value
                    }
                }
            } else {
                binding.tvTime.invisible()
                binding.tvTimeStart.gone()
                binding.tvTimeEnd.gone()
                binding.vTimeBar.invisible()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutDayMovementBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)
    }

    override fun getItemCount() = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setData(resultData: List<Int>) {
        mDataSet.clear()
        mDataSet.addAll(resultData)
        notifyDataSetChanged()
    }
}


fun dpToPx(px: Int, context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
    )
}
