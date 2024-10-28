package com.noisefit.ui.onboarding.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.data.model.language.AppLanguage
import com.noisefit.luna.databinding.RowLanguageSelectionBinding

class LanguagesAdapter : RecyclerView.Adapter<LanguagesAdapter.ViewHolder>() {
    val mDataSet = ArrayList<AppLanguage>()

    inner class ViewHolder(val binding: RowLanguageSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(language: AppLanguage) {
            binding.tvLanguageName.text = language.languageName
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowLanguageSelectionBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = mDataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataSet: List<AppLanguage>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }
}