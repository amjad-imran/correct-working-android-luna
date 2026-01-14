package com.oreo.ui.chatGpt.audio.persona

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noisefit.luna.databinding.ItemPersonaVoiceBinding
import com.noisefit.luna.databinding.LayoutPersonaFeatureChipBinding
import com.noisefit_commans.data.model.chatGPT.voice.persona.ItemPersonaVoiceResponse

class ChoosePersonaVoiceVpAdapter  :
    RecyclerView.Adapter<ChoosePersonaVoiceVpAdapter.CardViewHolder>() {
    private val mDataSet = ArrayList<ItemPersonaVoiceResponse>()
    inner class CardViewHolder(private val binding: ItemPersonaVoiceBinding) : RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        fun bind(data: ItemPersonaVoiceResponse){
            binding.tvPersonaName.text = data.personaTitle
            Glide.with(binding.ivBgImg)
                .load(data.imgUrl)
                .into(binding.ivBgImg)
            data.personaFeatures?.let { setPersonaItemChip(it) }
        }

        private fun setPersonaItemChip(listData: List<String>) {
            val chipGrp = binding.chipsPrograms
            chipGrp.removeAllViews()

            val layoutInflater = LayoutInflater.from(context)
            for (item in listData) {
                val mChipBinding =
                    LayoutPersonaFeatureChipBinding
                        .inflate(layoutInflater, chipGrp, false)

                mChipBinding.tvTitle.text = item
                chipGrp.addView(mChipBinding.root)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemPersonaVoiceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount() = mDataSet.size

    fun updateDataSet(data: List<ItemPersonaVoiceResponse>){
        mDataSet.clear()
        mDataSet.addAll(data)
        notifyDataSetChanged()
    }
}