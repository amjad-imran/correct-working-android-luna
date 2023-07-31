package com.noisefit.ui.friends.addfriends

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
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_ADD
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_NONE
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_REJECTED
import com.noisefit.ui.friends.request.received.FRIEND_STATUS_REMOVE

class ContactListAdapter(val listener: OnAddItemClickListener) :
    RecyclerView.Adapter<ContactListAdapter.ViewHolder>(), Filterable {

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

            if (joinedInterests.isNullOrEmpty())
                binding.tvInterestName.gone()
            else
                binding.tvInterestName.visible()

            if (buddiesUser.status == FRIEND_STATUS_NONE.toInt()
                || buddiesUser.status == FRIEND_STATUS_REJECTED.toInt()
                || buddiesUser.status == FRIEND_STATUS_REMOVE.toInt()
            ) {
                binding.tvAdd.visible()
                binding.tvRemove.gone()
            } else {
                binding.tvAdd.gone()
                binding.tvRemove.visible()
            }
            binding.tvAdd.setOnClickListener {
                listener.onAddClicked(buddiesUser, bindingAdapterPosition)
            }
            binding.tvRemove.setOnClickListener {
                listener.onRemoveClicked(buddiesUser, bindingAdapterPosition)
            }
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
        //Toast.makeText(context,"ListSize is ${currentContacts.size}",Toast.LENGTH_SHORT).show()

        otherContactsResult.clear()
        otherContactFilteredResults.clear()
        otherContactsResult.addAll(currentContacts)
        otherContactFilteredResults.addAll(currentContacts)
        notifyDataSetChanged()
    }

    fun updateStatus(position: Int) {
        otherContactFilteredResults[position].status = FRIEND_STATUS_ADD.toInt()
        notifyItemChanged(position)
    }


    fun removeItem(position: Int) {
        otherContactFilteredResults.remove(otherContactFilteredResults[position])
        notifyDataSetChanged()
    }

    fun updateStatusToAdd(position: Int) {
        otherContactFilteredResults[position].status = FRIEND_STATUS_REMOVE.toInt()
        notifyItemChanged(position)
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
                if (otherContactsResult.isNotEmpty()) {
                    listener.onDataFiltered(otherContactFilteredResults.size != 0)
                }
            }
        }
    }
}

interface OnAddItemClickListener {
    //0-add 1-remove
    fun onAddClicked(friendUser: BuddiesUserNew, position: Int)
    fun onRemoveClicked(friendUser: BuddiesUserNew, position: Int)
    fun onItemClicked(friendUser: BuddiesUserNew)
    fun onDataFiltered(hasUser: Boolean)
}