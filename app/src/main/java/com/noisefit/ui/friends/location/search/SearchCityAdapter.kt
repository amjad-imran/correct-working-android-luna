package com.noisefit.ui.friends.location.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.noisefit.data.remote.CityData
import com.noisefit.databinding.ItemSearchStateListBinding
import com.noisefit_commans.utils.LOGS


class SearchCityAdapter(
    val action: OnSearchCityInteractionListener
) : RecyclerView.Adapter<SearchCityAdapter.MyViewHolder>(), Filterable {
    private var cityDataResult = ArrayList<CityData>()
    private var otherCityFilteredResults = ArrayList<CityData>()

    inner class MyViewHolder(private val binding: ItemSearchStateListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CityData) {

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
        val currentBuddy = otherCityFilteredResults[position]
        holder.bind(currentBuddy)
    }

    override fun getItemCount(): Int {
        return otherCityFilteredResults.size
    }

    fun setDataSet(currentContacts: List<CityData>) {
        //Toast.makeText(context,"ListSize is ${currentContacts.size}",Toast.LENGTH_SHORT).show()

        cityDataResult.clear()
        otherCityFilteredResults.clear()
        cityDataResult.addAll(currentContacts)
        otherCityFilteredResults.addAll(currentContacts)
        notifyDataSetChanged()
    }

    interface OnSearchCityInteractionListener {
        fun onRowClick(data: CityData)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = ArrayList<CityData>()
                //  println("here we are  $charSearch")
                if (charSearch.isEmpty()) {
                    //Toast.makeText(context,"Char is Empty", Toast.LENGTH_SHORT).show()
                    resultList.addAll(cityDataResult)
                } else {
                    //Toast.makeText(context,"Char NOT Empty", Toast.LENGTH_SHORT).show()
                    for (row in cityDataResult) {
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
                otherCityFilteredResults.clear()
                otherCityFilteredResults.addAll(results?.values as ArrayList<CityData>)
                notifyDataSetChanged()
            }
        }
    }
}