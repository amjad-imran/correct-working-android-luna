package com.noisefit.ui.dashboard.feature.contact

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.LayoutContactItemBinding
import com.noisefit_commans.models.Contact


class AddContactListAdapter(
    val context: Context,
    val contactInteractionListener: ContactInteractionListener
) : RecyclerView.Adapter<AddContactListAdapter.MyViewHolder>(), Filterable {

    private var otherContactsResult = ArrayList<Contact>()
    private var otherContactFilteredResults = ArrayList<Contact>()

    inner class MyViewHolder(private val binding: LayoutContactItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact, position: Int) {
            binding.tvPersonName.text = contact.name
            if (contact.number.isNotEmpty()) {
                binding.tvContactNumber.text = contact.number[0]
            }

            binding.checkbox.setOnClickListener { view ->
                val isChecked = (view as CompoundButton).isChecked
                contactInteractionListener.onContactClick(
                    contact,
                    isChecked,
                    position,
                    binding.checkbox
                )
            }
            binding.checkbox.isChecked = contact.selected
            binding.executePendingBindings()
        }
    }

    fun updateContactList(position: Int, isChecked: Boolean) {
        otherContactFilteredResults[position].selected = isChecked
//        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            LayoutContactItemBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentBuddy = otherContactFilteredResults[position]
        holder.bind(currentBuddy, position)
    }

    override fun getItemCount(): Int {
        return otherContactFilteredResults.size
    }

    fun setDataSet(currentContacts: List<Contact>) {
        //Toast.makeText(context,"ListSize is ${currentContacts.size}",Toast.LENGTH_SHORT).show()

        otherContactsResult.clear()
        otherContactFilteredResults.clear()
        otherContactsResult.addAll(currentContacts)
        otherContactFilteredResults.addAll(currentContacts)
        notifyDataSetChanged()
    }

    interface ContactInteractionListener {
        fun onContactClick(contact: Contact, isChecked: Boolean, position: Int, checkBox: CheckBox)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = ArrayList<Contact>()
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
                otherContactFilteredResults.addAll(results?.values as ArrayList<Contact>)
                notifyDataSetChanged()
            }
        }
    }
}