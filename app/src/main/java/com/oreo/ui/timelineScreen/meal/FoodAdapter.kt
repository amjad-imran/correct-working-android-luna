package com.oreo.ui.timelineScreen.meal

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.model.timeline.MealAiFoods
import com.noisefit.luna.R
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible

class FoodAdapter(
    val isViewMode: Boolean,
    private val onChanged: (List<MealAiFoods>) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodVH>() {

    private val items = mutableListOf<MealAiFoods>()

    fun setData(newItems: List<MealAiFoods>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
        onChanged.invoke(items)
    }

    fun addItem(item: MealAiFoods) {
        items.add(item)
        notifyItemInserted(items.size - 1)
        onChanged.invoke(items)
    }

    fun getItems(): List<MealAiFoods> = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_food, parent, false)
        return FoodVH(v)
    }

    override fun onBindViewHolder(holder: FoodVH, position: Int) {
        holder.bind(items[position])

        if(isViewMode){
            holder.ivRemove.gone()
            holder.etCalories.isEnabled = false
        }else{
            holder.ivRemove.visible()
            holder.etCalories.isEnabled = true
        }

        holder.ivRemove.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {

                if(items.size==1) return@setOnClickListener

                items.removeAt(pos)
                notifyItemRemoved(pos)
                onChanged.invoke(items)
            }
        }
        holder.attachWatcher { newValue ->
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                items[pos].calories = newValue ?: 0
                onChanged.invoke(items)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class FoodVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvFoodName)
        val etCalories: EditText = itemView.findViewById(R.id.etCalories)
        val ivRemove: ImageView = itemView.findViewById(R.id.ivRemove)

        private var watcher: TextWatcher? = null

        fun bind(item: MealAiFoods) {
            tvName.text = item.name
            val text = item.calories?.toString().orEmpty()
            if (etCalories.text?.toString() != text) {
                etCalories.setText(text)
            }
        }

        fun attachWatcher(onValueChanged: (Int?) -> Unit) {
            watcher?.let { etCalories.removeTextChangedListener(it) }
            watcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val value = s?.toString()?.takeIf { it.isNotBlank() }?.toIntOrNull()
                    onValueChanged.invoke(value)
                }
            }
            etCalories.addTextChangedListener(watcher)
        }
    }
}

