package com.noisefit.ui.onboarding.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.language.AppLanguage
import com.noisefit.luna.R
import com.noisefit.luna.databinding.RowLanguageSelectionBinding

class LanguagesAdapter(val listener: LanguageSelectionListener) :
    RecyclerView.Adapter<LanguagesAdapter.ViewHolder>() {
    val mDataSet = ArrayList<AppLanguage>()
    var selectedLanguage: String? = null

    inner class ViewHolder(val binding: RowLanguageSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(language: AppLanguage) {
            binding.tvLanguageName.text = language.languageName

            if (selectedLanguage.equals(language.languageCode, true)) {
                binding.ivSelection.setImageResource(R.drawable.ic_lg_radio_selected)
            } else {
                binding.ivSelection.setImageResource(R.drawable.ic_lg_radio_default)
            }

            binding.root.setOnClickListener {
                selectedLanguage = language.languageCode
                listener.onLanguageSelected(language)
                notifyDataSetChanged()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowLanguageSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataSet: List<AppLanguage>, selectedLanguage: String) {
        this.selectedLanguage = selectedLanguage
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}

interface LanguageSelectionListener {
    fun onLanguageSelected(language: AppLanguage)
}