package com.oreo.ui.chatGpt.functions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowAiWorkoutBinding
import com.noisefit.luna.databinding.RowMealDataBinding

class MealsAdapter(val onMealSelected: (String) -> Unit) :
    RecyclerView.Adapter<MealsAdapter.ViewHolder>() {
    private val mDataSet = ArrayList<String>()

    inner class ViewHolder(val binding: RowMealDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {

            binding.tvTitle.text = "Breakfast"
            binding.rvMeals.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvMeals.adapter = SubMealAdapter(arrayListOf("","",""))

            binding.root.setOnClickListener {
                onMealSelected.invoke(data)
            }
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

    fun setDataSet(dataSet: List<String>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }


}