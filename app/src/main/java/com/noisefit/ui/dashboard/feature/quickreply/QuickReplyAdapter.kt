package com.noisefit.ui.dashboard.feature.quickreply

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.databinding.RowCustomReplyBinding
import com.noisefit_commans.ui.gone
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit_commans.ui.visible
import com.noisefit_commans.models.CustomReplyData
import java.util.*

class QuickReplyAdapter(private val action: QuickReplyRowAction) :
    RecyclerView.Adapter<QuickReplyAdapter.ViewHolder>(), ItemMoveCallbackListener.Listener {

    private var mDataSet = ArrayList<CustomReplyData.CustomReply>()

    private var mEditMode = false
    private var mDisableDeleteMode = false

    fun setEditMode(status: Boolean) {
        mEditMode = status
        notifyDataSetChanged()
    }

    fun setDisableDeleteMode(status: Boolean) {
        mDisableDeleteMode = status

    }

    inner class ViewHolder(val binding: RowCustomReplyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(reply: CustomReplyData.CustomReply) {
            binding.tvReplyText.text = reply.content

            if (mEditMode) {
                if (!mDisableDeleteMode) {
                    binding.ivRemove.visible()
                }

                binding.ivDrag.visible()
            } else {
                binding.ivRemove.gone()
                binding.ivDrag.gone()
            }
            binding.root.setOnLongClickListener {
                if (!mDisableDeleteMode) {
                    action.onLongPressed(bindingAdapterPosition)
                }

                true
            }
            binding.ivDrag.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    action.onStartDrag(this)
                }
                return@setOnTouchListener true
            }

            binding.root.setOnClickListener {
                if (mEditMode) {
                    action.onItemClicked(bindingAdapterPosition, reply)
                }
            }

            binding.ivRemove.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    mDataSet.removeAt(bindingAdapterPosition)
                    action.onItemRemoved(bindingAdapterPosition)
                    notifyItemRemoved(bindingAdapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowCustomReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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


    fun setDataSet(alarms: List<CustomReplyData.CustomReply>) {
        mDataSet = alarms as ArrayList<CustomReplyData.CustomReply>
        notifyDataSetChanged()
    }

    fun getCurrentDataSet(): List<CustomReplyData.CustomReply> {
        return mDataSet
    }
}

interface QuickReplyRowAction {
    fun onLongPressed(position: Int)
    fun onItemRemoved(position: Int)
    fun onItemClicked(position: Int, reply: CustomReplyData.CustomReply)
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}