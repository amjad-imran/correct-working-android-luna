package com.noisefit.ui.dashboard.feature.sportSelection

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.databinding.RowAddSportBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.util.ImageUtil
import com.noisefit_commans.models.SportsModeList


class AddSportSelectionAdapter(
    val contactInteractionListener: SportInteractionListener
) : RecyclerView.Adapter<AddSportSelectionAdapter.MyViewHolder>(), Filterable {

    private var sportsResult = ArrayList<SportsModeList.SportsMode>()
    private var sportsFilteredResult = ArrayList<SportsModeList.SportsMode>()

    inner class MyViewHolder(private val binding: RowAddSportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sportsMode: SportsModeList.SportsMode, position: Int) {
            binding.tvName.text = sportsMode.name?.replace("_", " ")
            binding.imageView8.setImageResource(ImageUtil().getImageFromActivity(sportsMode.name))
            binding.checkbox.setOnClickListener { view ->
                val isChecked = (view as CompoundButton).isChecked
                contactInteractionListener.onSportClick(
                    sportsMode,
                    isChecked,
                    position,
                    binding.checkbox
                )
            }

            if (sportsMode.remove) {
                binding.checkbox.visible()
            } else {
                binding.checkbox.gone()
            }
            binding.checkbox.isChecked = sportsMode.value
            binding.executePendingBindings()
        }
    }

    fun updateContactList(position: Int, isChecked: Boolean) {
        sportsFilteredResult[position].value = isChecked
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

    fun setDataSet(currentContacts: List<SportsModeList.SportsMode>) {
        //Toast.makeText(context,"ListSize is ${currentContacts.size}",Toast.LENGTH_SHORT).show()

        sportsResult.clear()
        sportsFilteredResult.clear()
        sportsResult.addAll(currentContacts)
        sportsFilteredResult.addAll(currentContacts)
        notifyDataSetChanged()
    }

    interface SportInteractionListener {
        fun onSportClick(
            contact: SportsModeList.SportsMode,
            isChecked: Boolean,
            position: Int,
            checkBox: CheckBox
        )
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = ArrayList<SportsModeList.SportsMode>()
                //  println("here we are  $charSearch")
                if (charSearch.isEmpty()) {
                    //Toast.makeText(context,"Char is Empty", Toast.LENGTH_SHORT).show()
                    resultList.addAll(sportsResult)
                } else {
                    //Toast.makeText(context,"Char NOT Empty", Toast.LENGTH_SHORT).show()
                    for (row in sportsResult) {
                        if (row.name?.lowercase()
                                ?.contains(constraint.toString().lowercase()) == true
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
                sportsFilteredResult.addAll(results?.values as ArrayList<SportsModeList.SportsMode>)
                notifyDataSetChanged()
            }
        }
    }
}