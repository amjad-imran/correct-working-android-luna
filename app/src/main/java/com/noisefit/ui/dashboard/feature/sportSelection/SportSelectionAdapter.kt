package com.noisefit.ui.dashboard.feature.sportSelection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowSportSelectionBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.util.ImageUtil
import com.noisefit_commans.models.SportsModeList
import javax.inject.Inject


class SportSelectionAdapter(private val action: SportSelectionListener) :
    RecyclerView.Adapter<SportSelectionAdapter.ViewHolder>() {

    @Inject
    lateinit var imageUtil: ImageUtil

    private var mDataSet = ArrayList<SportsModeList.SportsMode>()

    private var mEditMode = false

    fun setEditMode(status: Boolean) {
        mEditMode = status
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: RowSportSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sport: SportsModeList.SportsMode) {
            binding.tvName.text = sport.name?.replace("_", " ")

            binding.imageView8.setImageResource(ImageUtil().getImageFromActivity(sport.name))
            if (mEditMode) {
                if (sport.remove) {
                    binding.ivRemove.visible()
                } else {
                    binding.ivRemove.gone()
                }
            } else {
                binding.ivRemove.gone()
            }
            binding.root.setOnLongClickListener {
                action.onLongPressed(bindingAdapterPosition)
                true
            }


            binding.ivRemove.setOnClickListener {
                action.onItemRemoved(bindingAdapterPosition, sport)
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


    fun setDataSet(dataList: List<SportsModeList.SportsMode>) {
        mDataSet = dataList as ArrayList<SportsModeList.SportsMode>
        notifyDataSetChanged()
    }

    fun getCurrentDataSet(): List<SportsModeList.SportsMode> {
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

interface SportSelectionListener {
    fun onLongPressed(position: Int)
    fun onItemRemoved(position: Int, sport: SportsModeList.SportsMode)
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}