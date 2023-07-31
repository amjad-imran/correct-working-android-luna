package com.noisefit.ui.dashboard.feature.qrPayment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.databinding.RowQrListBinding
import com.noisefit_commans.ui.gone
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit_commans.ui.visible
import com.noisefit_commans.models.UPIQRCode
import java.util.*

class QrCodeListingAdapter(private val action: QuickReplyRowAction) :
    RecyclerView.Adapter<QrCodeListingAdapter.ViewHolder>(), ItemMoveCallbackListener.Listener {

    private var mDataSet = ArrayList<UPIQRCode>()

    private var mEditMode = false

    fun setEditMode(status: Boolean) {
        mEditMode = status
        notifyDataSetChanged()
    }


    inner class ViewHolder(val binding: RowQrListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(qrCode: UPIQRCode) {
            binding.tvReplyText.text = qrCode.title

            if (mEditMode) {
                binding.ivRemove.visible()

                binding.ivDrag.visible()
            } else {
                binding.ivRemove.gone()
                binding.ivDrag.gone()
            }
            binding.root.setOnLongClickListener {
                action.onLongPressed(bindingAdapterPosition)
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
                    action.onItemClicked(bindingAdapterPosition, qrCode)
                }
            }

            binding.ivRemove.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    action.onItemRemoved(bindingAdapterPosition, qrCode)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowQrListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])


    }

    fun removeItem(position: Int) {
        if (position < itemCount) {
            mDataSet.removeAt(position)
            notifyItemRemoved(position)
        }

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


    fun setDataSet(dataSet: List<UPIQRCode>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    fun getCurrentDataSet(): List<UPIQRCode> {
        mDataSet.forEachIndexed { index, upiqrCode ->
            //if (upiqrCode.id == null) {
                upiqrCode.id = index
            //}
        }
        return mDataSet
    }
}

interface QuickReplyRowAction {
    fun onLongPressed(position: Int)
    fun onItemRemoved(position: Int, qrCode: UPIQRCode)
    fun onItemClicked(position: Int, qrCode: UPIQRCode)
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}