package com.noisefit.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.model.BuddiesUserNew
import com.noisefit.luna.databinding.RowContactNewBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadProfileEditImage
import com.noisefit_commans.ui.visible

class CommonFriendsListAdapter(val listener: OnItemClickListener) :
    RecyclerView.Adapter<CommonFriendsListAdapter.ViewHolder>(), Filterable {

    private var otherContactsResult = ArrayList<BuddiesUserNew>()
    private var otherContactFilteredResults = ArrayList<BuddiesUserNew>()


    inner class ViewHolder(val binding: RowContactNewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(buddiesUser: BuddiesUserNew) {
            binding.tvFriendName.text = buddiesUser.getFullUserName()
            binding.ivFriendProfile.loadProfileEditImage(
                binding.ivFriendProfile.context,
                buddiesUser.imageUrl,
                R.drawable.ic_default_profile_image
            )

            val joinedInterests = buddiesUser.interests?.take(2)?.joinToString() ?: ""

            binding.tvInterestName.text =
                "${if (joinedInterests.isNotEmpty()) "$joinedInterests..." else ""}"

            if (joinedInterests.isEmpty())
                binding.tvInterestName.gone()
            else
                binding.tvInterestName.visible()


            binding.tvAdd.gone()
            binding.tvRemove.gone()


            binding.root.setOnClickListener {
                listener.onItemClicked(buddiesUser)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowContactNewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(otherContactFilteredResults[position])
    }

    override fun getItemCount(): Int {
        return otherContactFilteredResults.size
    }

    fun setDataSet(currentContacts: List<BuddiesUserNew>) {
        otherContactsResult.clear()
        otherContactFilteredResults.clear()
        otherContactsResult.addAll(currentContacts)
        otherContactFilteredResults.addAll(currentContacts)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = ArrayList<BuddiesUserNew>()
                //  println("here we are  $charSearch")
                if (charSearch.isEmpty()) {
                    //Toast.makeText(context,"Char is Empty", Toast.LENGTH_SHORT).show()
                    resultList.addAll(otherContactsResult)
                } else {
                    //Toast.makeText(context,"Char NOT Empty", Toast.LENGTH_SHORT).show()
                    for (row in otherContactsResult) {
                        if (row.getFullUserName().lowercase()
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
                otherContactFilteredResults.clear()
                if (results == null) {
                    otherContactFilteredResults.addAll(ArrayList<BuddiesUserNew>())
                } else {
                    otherContactFilteredResults.addAll(results.values as ArrayList<BuddiesUserNew>)
                }
                notifyDataSetChanged()
                listener.onDataFiltered(otherContactFilteredResults.size != 0)
            }
        }
    }
}

interface OnItemClickListener {
    fun onItemClicked(friendUser: BuddiesUserNew)
    fun onDataFiltered(hasUser: Boolean)
}