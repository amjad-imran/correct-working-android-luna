package com.noisefit.ui.settings.helpAndSupport.compatibility

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.model.AppCompatibility
import com.noisefit.databinding.ItemAppCompatibilityListBinding
import com.noisefit_commans.ui.loadImage


class AppCompatibilityAdapter(val listener: AppCompatibilityInteractionListener) :
    RecyclerView.Adapter<AppCompatibilityAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<com.noisefit_commans.data.model.AppCompatibility>()

    inner class ViewHolder(private val binding: ItemAppCompatibilityListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(value: com.noisefit_commans.data.model.AppCompatibility) {


            binding.tvTitle.text = value.watchName
            binding.tvAppName.text = value.appName
            binding.iconImv.loadImage(binding.iconImv.context, value.icon)

            binding.btnAction.setOnClickListener {
                listener.onAppCompatibilityClick(value.packageName)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AppCompatibilityAdapter.ViewHolder {
        val binding = ItemAppCompatibilityListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppCompatibilityAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataList: List<com.noisefit_commans.data.model.AppCompatibility>) {
        mDataSet.clear()
        mDataSet.addAll(dataList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size


}

interface AppCompatibilityInteractionListener {
    fun onAppCompatibilityClick(packageName: String)
}
