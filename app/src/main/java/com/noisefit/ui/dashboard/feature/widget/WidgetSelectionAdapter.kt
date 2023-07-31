package com.noisefit.ui.dashboard.feature.widget

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowSportSelectionBinding
import com.noisefit_commans.ui.gone
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit_commans.ui.visible
import com.noisefit.util.ImageUtil
import com.noisefit_commans.models.Widget
import com.noisefit_commans.utils.LOGS
import java.util.*
import javax.inject.Inject


class WidgetSelectionAdapter(private val action: WidgetSelectionListener) :
    RecyclerView.Adapter<WidgetSelectionAdapter.ViewHolder>(), ItemMoveCallbackListener.Listener {

    @Inject
    lateinit var imageUtil: ImageUtil

    private var mDataSet = ArrayList<Widget>()

    private var mEditMode = false

    fun setEditMode(status: Boolean) {
        mEditMode = status
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: RowSportSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(widget: Widget) {
            binding.tvName.text = widget.name.replace("_", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

            binding.imageView8.setImageResource(ImageUtil().getImageForApp(widget.functionId))
            if (mEditMode) {
                binding.ivRemove.visible()
                if (widget.sortable) {
                    binding.ivDrag.visible()
                } else {
                    binding.ivDrag.gone()
                }

            } else {
                binding.ivDrag.gone()
                binding.ivRemove.gone()
            }


            binding.ivDrag.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    action.onStartDrag(this)
                }
                return@setOnTouchListener true
            }
            binding.root.setOnLongClickListener {
                action.onLongPressed(bindingAdapterPosition)
                true
            }


            binding.ivRemove.setOnClickListener {
                action.onItemRemoved(bindingAdapterPosition, widget)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowSportSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }


    fun setDataSet(dataList: List<Widget>) {
        mDataSet = dataList as ArrayList<Widget>
        notifyDataSetChanged()
    }

    fun getCurrentDataSet(): List<Widget> {
        return mDataSet
    }

    fun removeItem(position: Int) {
        try {
            mDataSet.removeAt(position)
            notifyItemRemoved(position)
        } catch (exp: ArrayIndexOutOfBoundsException) {
            exp.printStackTrace()
            //CASE : when Swap is in progress
        }

    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        LOGS.d("onRowMoved $fromPosition $toPosition")
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


}

interface WidgetSelectionListener {
    fun onLongPressed(position: Int)
    fun onItemRemoved(position: Int, widget: Widget)
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}