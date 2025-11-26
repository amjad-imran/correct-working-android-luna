package com.oreo.ui.lifeos.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.ItemPersonalizeLifeosCardBinding
import com.noisefit_commans.data.model.lifeos.onboarding.Question

class PersonalizeQnaAdapter(private val onItemClick: (Question) -> Unit) :
    RecyclerView.Adapter<PersonalizeQnaAdapter.ViewHolder>() {

    val mList = ArrayList<Question>()

    inner class ViewHolder(private val binding: ItemPersonalizeLifeosCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Question) {
            binding.tvTitle.text = item.personalizeTitle ?: "Health Goals"
            binding.tvDesc.text = item.personalizeDesc

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPersonalizeLifeosCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int = mList.size

    fun updateDataSet(data: List<Question>){
        mList.clear()
        mList.addAll(data)
        notifyDataSetChanged()
    }

}