package com.noisefit.ui.dashboard.feature.sportSelection.zh

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.RowAddSportBinding
import com.noisefit.util.ImageUtil
import com.noisefit_commans.models.Widget
import java.util.*
import kotlin.collections.ArrayList


class ZhAddSportSelectionAdapter(
    val contactInteractionListener: SportInteractionListener
) : RecyclerView.Adapter<ZhAddSportSelectionAdapter.MyViewHolder>(), Filterable {

    private var sportsResult = ArrayList<Widget>()
    private var sportsFilteredResult = ArrayList<Widget>()

    inner class MyViewHolder(private val binding: RowAddSportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(widget: Widget, position: Int) {
            binding.tvName.text = widget.name.replace("_", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            binding.imageView8.setImageResource(ImageUtil().getImageFromActivity(widget.name))

            binding.checkbox.setOnClickListener { view ->
                val isChecked = (view as CompoundButton).isChecked
                contactInteractionListener.onSportClick(
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
        sportsFilteredResult[position].isEnable = isChecked
//        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            RowAddSportBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentBuddy = sportsFilteredResult[position]
        holder.bind(currentBuddy, position)
    }

    override fun getItemCount(): Int {
        return sportsFilteredResult.size
    }

    fun setDataSet(currentContacts: List<Widget>) {
        //Toast.makeText(context,"ListSize is ${currentContacts.size}",Toast.LENGTH_SHORT).show()

        sportsResult.clear()
        sportsFilteredResult.clear()
        sportsResult.addAll(currentContacts)
        sportsFilteredResult.addAll(currentContacts)
        notifyDataSetChanged()
    }

    interface SportInteractionListener {
        fun onSportClick(
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
                    resultList.addAll(sportsResult)
                } else {
                    //Toast.makeText(context,"Char NOT Empty", Toast.LENGTH_SHORT).show()
                    for (row in sportsResult) {
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
                sportsFilteredResult.clear()
                sportsFilteredResult.addAll(results?.values as ArrayList<Widget>)
                notifyDataSetChanged()
            }
        }
    }
}