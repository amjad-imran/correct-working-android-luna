package com.noisefit.ui.dashboard.feature.contact

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowContactBinding
import com.noisefit_commans.ui.gone
import com.noisefit.ui.common.utils.ItemMoveCallbackListener
import com.noisefit_commans.ui.visible
import com.noisefit_commans.models.Contact
import com.noisefit_commans.utils.LOGS
import java.util.*


class ContactListAdapter(private val action: ContactListListener,private val hideDrag:Boolean) :
    RecyclerView.Adapter<ContactListAdapter.ViewHolder>(), ItemMoveCallbackListener.Listener {

    private var mDataSet = ArrayList<Contact>()

    private var mEditMode = false

    fun setEditMode(status: Boolean) {
        mEditMode = status
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: RowContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            binding.tvName.text = contact.name

            if (contact.number.isNotEmpty()) {
                binding.tvNumber.text = contact.number[0]
            }


            if (mEditMode) {
                binding.ivRemove.visible()
                if(!hideDrag){
                    binding.ivDrag.visible()
                }

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


            binding.ivRemove.setOnClickListener {
                action.onItemRemoved(bindingAdapterPosition, contact)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])


    }

    override fun getItemCount(): Int {
        return mDataSet.size
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


    fun setDataSet(stocks: List<Contact>) {
        mDataSet = stocks as ArrayList<Contact>
        notifyDataSetChanged()
    }

    fun getCurrentDataSet(): List<Contact> {
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


}

interface ContactListListener {
    fun onLongPressed(position: Int)
    fun onItemRemoved(position: Int, stock: Contact)
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}