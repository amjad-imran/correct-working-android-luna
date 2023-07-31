package com.noisefit.ui.friends.profile.myfriend

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.data.model.FriendsFriendListData
import com.noisefit.luna.databinding.ItemMyFriendListBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible

class MyFriendListAdapter(val listener: OnFriendFilterListener) :
    RecyclerView.Adapter<MyFriendListAdapter.ViewHolder>(), Filterable {

    private var myFriendListResult = ArrayList<FriendsFriendListData>()
    private var myFriendListFilteredResults = ArrayList<FriendsFriendListData>()


    inner class ViewHolder(val binding: ItemMyFriendListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(resultData: FriendsFriendListData) {
            binding.tvFriendName.text = resultData.firstName

            binding.ivFriendProfile.loadImage(
                binding.ivFriendProfile.context,
                resultData.imageUrl,
                R.drawable.ic_default_profile_image
            )
            val joinedInterests = resultData.interests?.take(2)?.joinToString() ?: ""

            binding.tvInterestName.text =
                "${if (joinedInterests.isNotEmpty()) "$joinedInterests..." else ""}"


            if (resultData.status == 0) {
                binding.tvAdd.visible()
                binding.tvRemove.gone()
            } else if (resultData.status == 1) {
                binding.tvAdd.gone()
                 if (resultData.isRequestReceived != null) {
                    if (resultData.isRequestReceived) {
                        binding.tvRemove.gone()
                    } else {
                        showViews(binding)
                    }
                }
                else{
                    showViews(binding)
                 }
            } else {
                binding.tvRemove.gone()
                binding.tvAdd.gone()
            }

            binding.root.setOnClickListener {
                listener.onItemClick(resultData)
            }
            binding.tvAdd.setOnClickListener {
                listener.onAddClicked(resultData, bindingAdapterPosition)
            }
            binding.tvRemove.setOnClickListener {
                listener.onRemoveClicked(resultData, bindingAdapterPosition)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMyFriendListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(myFriendListFilteredResults[position])
    }

    override fun getItemCount(): Int {
        return myFriendListFilteredResults.size
    }

    fun setDataSet(resultData: List<FriendsFriendListData>) {
        myFriendListResult.clear()
        myFriendListResult.addAll(resultData)
        myFriendListFilteredResults.clear()
        myFriendListFilteredResults.addAll(resultData)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = ArrayList<FriendsFriendListData>()
                //  println("here we are  $charSearch")
                if (charSearch.isEmpty()) {
                    //Toast.makeText(context,"Char is Empty", Toast.LENGTH_SHORT).show()
                    resultList.addAll(myFriendListResult)
                } else {
                    //Toast.makeText(context,"Char NOT Empty", Toast.LENGTH_SHORT).show()
                    for (row in myFriendListResult) {
                        if (row.firstName?.lowercase()
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
                myFriendListFilteredResults.clear()
                if (results == null) {
                    myFriendListFilteredResults.addAll(ArrayList())
                } else {
                    myFriendListFilteredResults.addAll(results.values as ArrayList<FriendsFriendListData>)
                }
                notifyDataSetChanged()
                if (myFriendListResult.isNotEmpty()) {
                    listener.onDataFiltered(myFriendListFilteredResults.size != 0)
                }

            }
        }
    }

    fun updateStatus(position: Int, statusValue: Int) {
        myFriendListFilteredResults[position].status = statusValue
        notifyItemChanged(position)
    }

    private fun showViews(binding: ItemMyFriendListBinding) {
        binding.tvRemove.visible()
        binding.tvRemove.text = "Remove"
        binding.tvRemove.isClickable = true
    }


}


interface OnFriendFilterListener {
    fun onAddClicked(resultData: FriendsFriendListData, position: Int)
    fun onRemoveClicked(resultData: FriendsFriendListData, position: Int)

    fun onItemClick(resultData: FriendsFriendListData)
    fun onDataFiltered(hasUser: Boolean)
}



