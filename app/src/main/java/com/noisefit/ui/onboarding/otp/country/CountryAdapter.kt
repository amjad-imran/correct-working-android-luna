package com.noisefit.ui.onboarding.otp.country

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.response.Country
import com.noisefit.luna.databinding.RowCountryNameLayoutBinding
import java.util.*
import kotlin.collections.ArrayList


class CountryAdapter(
    private val listener: CountryInteractionListener
) : RecyclerView.Adapter<CountryAdapter.ViewHolder>(), Filterable {
    private var otherContactsResult = ArrayList<Country>()
    private var otherContactFilteredResults = ArrayList<Country>()


    inner class ViewHolder(val binding: RowCountryNameLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(country: Country) {

            //binding.imvFlag.loadImage(binding.imvFlag.context, country.flag)
            binding.tvName.text = country.name?.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(
                    Locale.getDefault()
                ) else char.toString()
            }
            binding.countryContainer.setOnClickListener {
                listener.onCountrySelected(country)
            }
        }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryAdapter.ViewHolder {
        val binding = RowCountryNameLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(otherContactFilteredResults[position])

    }

    override fun getItemCount(): Int {
        return otherContactFilteredResults.size
    }


    fun setDataSet(dataSet: List<Country>) {
        otherContactsResult.clear()
        otherContactFilteredResults.clear()
        otherContactsResult.addAll(dataSet)
        otherContactFilteredResults.addAll(dataSet)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = ArrayList<Country>()
                //  println("here we are  $charSearch")
                if (charSearch.isEmpty()) {
                    //Toast.makeText(context,"Char is Empty", Toast.LENGTH_SHORT).show()
                    resultList.addAll(otherContactsResult)
                } else {
                    //Toast.makeText(context,"Char NOT Empty", Toast.LENGTH_SHORT).show()
                    for (row in otherContactsResult) {
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
                otherContactFilteredResults.clear()
                otherContactFilteredResults.addAll(results?.values as ArrayList<Country>)
                notifyDataSetChanged()
            }
        }
    }
}


interface CountryInteractionListener {
    fun onCountrySelected(country: Country)
}