package com.oreo.ui.timelineScreen.addActivity.activities

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R

class DropdownAdapter(
    private val items: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<DropdownAdapter.ViewHolder>() {

    private val itemColors = mapOf(
        "Meal intake" to "#FFEB3B",
        "Light exposure" to "#FFA726",
        "Caffeine intake" to "#FF8A65",
        "Workout" to "#4FC3F7",
        "Water consumption" to "#4CAF50",
        "Period" to "#E91E63",
        "Nap" to "#9C27B0",
        "Sleep" to "#9575CD"
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tvDropdownItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dropdown, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item

        // Set color based on item
        val color = itemColors[item] ?: "#CCCCCC"
        holder.textView.setTextColor(Color.parseColor(color))

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size
}
