package com.oreo.ui.chatGpt.functions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.AiMeal
import com.noisefit.data.model.AiMeals
import com.noisefit.luna.databinding.RowAiWorkoutBinding
import com.noisefit.luna.databinding.RowMealDataBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible

class MealsAdapter(val onMealSelected: (View, AiMeal, String) -> Unit) :
    RecyclerView.Adapter<MealsAdapter.ViewHolder>() {

    private val mDataSet = ArrayList<AiMeals>()

    inner class ViewHolder(val binding: RowMealDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: AiMeals) {

            binding.tvTitle.text = data.meal_type
            binding.rvMeals.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvMeals.adapter = SubMealAdapter(data.meal ?: ArrayList(), onMealSelected = {
                onMealSelected.invoke(binding.root,it,data.meal_type?:"")
            })

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowMealDataBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataSet: List<AiMeals>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }


}