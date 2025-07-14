package com.oreo.ui.circadianAlignment

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemCorrectiveActivitiesCircadianBinding

class CorrectiveActivitiesAdapter:
    RecyclerView.Adapter<CorrectiveActivitiesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCorrectiveActivitiesCircadianBinding):
        RecyclerView.ViewHolder(binding.root)
    {
        fun bind(data: CorrectiveActivitiesEnum){
            when(data){
                CorrectiveActivitiesEnum.LIGHT_EXPOSURE -> {
                    binding.shapeableImageView.setImageResource(R.drawable.image_back_health_monitor)
                }
                CorrectiveActivitiesEnum.DAILY_STEPS -> {

                }
                CorrectiveActivitiesEnum.MEAL_WINDOW -> {

                }
                CorrectiveActivitiesEnum.WORKOUT -> {

                }
                CorrectiveActivitiesEnum.CAFFEINE -> {

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

    }

    override fun getItemCount(): Int {

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

}