package com.oreo.ui.workout.add

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.LayoutSelectWorkoutItemBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.oreo.data.model.OWorkoutListModal

class OSelectWorkoutAdapter(val oSelectWorkoutInteraction: OSelectWorkoutInteraction) :
    RecyclerView.Adapter<OSelectWorkoutAdapter.ViewHolder>(), Filterable {
    private val mDataSet = ArrayList<OWorkoutListModal>()
    private var mDataSetFilteredResults = ArrayList<OWorkoutListModal>()

    inner class ViewHolder(private val binding: LayoutSelectWorkoutItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resultData: OWorkoutListModal) {
            binding.tvTitle.text = resultData.getFormattedActivityName()

            binding.root.setOnClickListener {
                oSelectWorkoutInteraction.onWorkoutSelected(resultData)
            }
            if (bindingAdapterPosition == mDataSet.size - 1) {
                binding.divider1.root.invisible()
            } else {
                binding.divider1.root.visible()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutSelectWorkoutItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(view)

    }

    override fun getItemCount() = mDataSetFilteredResults.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSetFilteredResults[position])
    }

    fun setData(dataSet: List<OWorkoutListModal>) {
        mDataSet.clear()
        mDataSetFilteredResults.clear()
        mDataSet.addAll(dataSet)
        mDataSetFilteredResults.addAll(dataSet)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = ArrayList<OWorkoutListModal>()
                //  println("here we are  $charSearch")
                if (charSearch.isEmpty()) {
                    //Toast.makeText(context,"Char is Empty", Toast.LENGTH_SHORT).show()
                    resultList.addAll(mDataSet)
                } else {
                    //Toast.makeText(context,"Char NOT Empty", Toast.LENGTH_SHORT).show()
                    for (row in mDataSet) {
                        if (row.getFormattedActivityName().lowercase()
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
                mDataSetFilteredResults.clear()
                if (results == null) {
                    mDataSetFilteredResults.addAll(ArrayList<OWorkoutListModal>())
                } else {
                    mDataSetFilteredResults.addAll(results.values as ArrayList<OWorkoutListModal>)
                }
                notifyDataSetChanged()

            }
        }
    }


    interface OSelectWorkoutInteraction {
        fun onWorkoutSelected(oWorkoutListModal: OWorkoutListModal)
    }
}