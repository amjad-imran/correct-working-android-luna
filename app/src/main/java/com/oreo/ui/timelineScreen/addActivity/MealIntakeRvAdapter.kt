package com.oreo.ui.timelineScreen.addActivity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemAddNextLogBottomSheetBinding
import com.noisefit.luna.databinding.ItemInputMealIntakeCardCircadianBsBinding
import com.oreo.data.model.addLogBottomSheetModels.AddLogBottomSheetDataModels

class MealIntakeRvAdapter: RecyclerView.Adapter<ViewHolder>() {

    private val mList: ArrayList<AddLogBottomSheetDataModels> = ArrayList()

    override fun getItemViewType(position: Int): Int {
        return when(mList[position]){
            is AddLogBottomSheetDataModels.MealIntakeAddLogModel -> R.layout.item_input_meal_intake_card_circadian_bs
            is AddLogBottomSheetDataModels.AddNextItemLogBsModel -> R.layout.item_add_next_log_bottom_sheet
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType){
            R.layout.item_input_meal_intake_card_circadian_bs -> InputMealIntakeViewHolder(
                ItemInputMealIntakeCardCircadianBsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            R.layout.item_add_next_log_bottom_sheet -> AddMealIntakeViewHolder(
                ItemAddNextLogBottomSheetBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        when(holder){
            is InputMealIntakeViewHolder -> holder.bind(item as AddLogBottomSheetDataModels.MealIntakeAddLogModel)
            is AddMealIntakeViewHolder -> holder.bind(item as AddLogBottomSheetDataModels.AddNextItemLogBsModel)
        }
    }

    fun updateDataSet(list: List<AddLogBottomSheetDataModels>){
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    var itemClickListener: ((type: AddLogItemsBottomSheetClickEnum) -> Unit)? = null

    inner class InputMealIntakeViewHolder(private val binding: ItemInputMealIntakeCardCircadianBsBinding): ViewHolder(binding.root){
        fun bind(data: AddLogBottomSheetDataModels.MealIntakeAddLogModel) {
            binding.lytTimePicker.setOnClickListener {
                itemClickListener?.invoke(
                    AddLogItemsBottomSheetClickEnum.OnInputMealIntakeTimerLytClick(
                        data
                    )
                )
            }
        }
    }

    inner class AddMealIntakeViewHolder(private val binding: ItemAddNextLogBottomSheetBinding): ViewHolder(binding.root){
        fun bind(data: AddLogBottomSheetDataModels.AddNextItemLogBsModel){
            binding.tvTitle.text = data.title
        }
    }

}

sealed class AddLogItemsBottomSheetClickEnum{
    data class OnInputMealIntakeTimerLytClick(val data: AddLogBottomSheetDataModels.MealIntakeAddLogModel): AddLogItemsBottomSheetClickEnum()
}