package com.noisefit.ui.friends.location.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.remote.StateData
import com.noisefit.luna.databinding.ItemSearchStateListBinding


class SearchStateAdapter(
    val action: OnSearchStateInteractionListener
) : RecyclerView.Adapter<SearchStateAdapter.MyViewHolder>(), Filterable {
    private var stateDataResult = ArrayList<StateData>()
    private var otherStateFilteredResults = ArrayList<StateData>()

    inner class MyViewHolder(private val binding: ItemSearchStateListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StateData) {
            binding.tvTitle.text = data.name
            binding.root.setOnClickListener {
                action.onRowClick(data)
            }

            binding.executePendingBindings()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            ItemSearchStateListBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentBuddy = otherStateFilteredResults[position]
        holder.bind(currentBuddy)
    }

    override fun getItemCount(): Int {
        return otherStateFilteredResults.size
    }

    fun setDataSet(currentContacts: List<StateData>) {
        //Toast.makeText(context,"ListSize is ${currentContacts.size}",Toast.LENGTH_SHORT).show()

        stateDataResult.clear()
        otherStateFilteredResults.clear()
        stateDataResult.addAll(currentContacts)
        otherStateFilteredResults.addAll(currentContacts)
        notifyDataSetChanged()
    }

    interface OnSearchStateInteractionListener {
        fun onRowClick(data: StateData)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = ArrayList<StateData>()
                //  println("here we are  $charSearch")
                if (charSearch.isEmpty()) {
                    //Toast.makeText(context,"Char is Empty", Toast.LENGTH_SHORT).show()
                    resultList.addAll(stateDataResult)
                } else {
                    //Toast.makeText(context,"Char NOT Empty", Toast.LENGTH_SHORT).show()
                    for (row in stateDataResult) {
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
                otherStateFilteredResults.clear()
                otherStateFilteredResults.addAll(results?.values as ArrayList<StateData>)
                notifyDataSetChanged()
            }
        }
    }
}