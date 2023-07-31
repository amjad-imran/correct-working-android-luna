package com.noisefit.ui.dashboard.feature.widget.add

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.databinding.RowAddSportBinding
import com.noisefit.util.ImageUtil
import com.noisefit_commans.models.Widget
import java.util.*
import kotlin.collections.ArrayList


class AddWidgetSelectionAdapter(
    val widgetInteractionListener: WidgetInteractionListener
) : RecyclerView.Adapter<AddWidgetSelectionAdapter.MyViewHolder>(), Filterable {

    private var widgetResult = ArrayList<Widget>()
    private var widgetFilteredResult = ArrayList<Widget>()

    inner class MyViewHolder(private val binding: RowAddSportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(widget: Widget, position: Int) {
            binding.tvName.text = widget.name.replace("_", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            binding.imageView8.setImageResource(ImageUtil().getImageForApp(widget.functionId))
            binding.checkbox.setOnClickListener { view ->
                val isChecked = (view as CompoundButton).isChecked
                widgetInteractionListener.onWidgetClick(
                    widget,
                    isChecked,
                    position,
                    binding.checkbox
                )
            }
            binding.checkbox.isChecked = widget.isEnable
            binding.executePendingBindings()
        }
    }

    fun updateContactList(position: Int, isChecked: Boolean) {
        widgetFilteredResult[position].isEnable = isChecked
//        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            RowAddSportBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentBuddy = widgetFilteredResult[position]
        holder.bind(currentBuddy, position)
    }

    override fun getItemCount(): Int {
        return widgetFilteredResult.size
    }

    fun setDataSet(currentContacts: List<Widget>) {
        //Toast.makeText(context,"ListSize is ${currentContacts.size}",Toast.LENGTH_SHORT).show()

        widgetResult.clear()
        widgetFilteredResult.clear()
        widgetResult.addAll(currentContacts)
        widgetFilteredResult.addAll(currentContacts)
        notifyDataSetChanged()
    }

    interface WidgetInteractionListener {
        fun onWidgetClick(
            contact: Widget,
            isChecked: Boolean,
            position: Int,
            checkBox: CheckBox
        )
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = ArrayList<Widget>()
                //  println("here we are  $charSearch")
                if (charSearch.isEmpty()) {
                    //Toast.makeText(context,"Char is Empty", Toast.LENGTH_SHORT).show()
                    resultList.addAll(widgetResult)
                } else {
                    //Toast.makeText(context,"Char NOT Empty", Toast.LENGTH_SHORT).show()
                    for (row in widgetResult) {
                        if (row.name.lowercase()
                                .contains(constraint.toString().lowercase())
                        ) {
                            resultList.add(row)
                        }
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = resultList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                widgetFilteredResult.clear()
                widgetFilteredResult.addAll(results?.values as ArrayList<Widget>)
                notifyDataSetChanged()
            }
        }
    }
}