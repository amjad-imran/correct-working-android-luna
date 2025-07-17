package com.oreo.ui.circadianAlignment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemCorrectiveActivitiesCircadianBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.oreo.data.model.CorrectiveActivitiesModel

class CorrectiveActivitiesAdapter:
    RecyclerView.Adapter<CorrectiveActivitiesAdapter.ViewHolder>() {

        private val mList: ArrayList<CorrectiveActivitiesModel> = ArrayList()

    inner class ViewHolder(val binding: ItemCorrectiveActivitiesCircadianBinding):
        RecyclerView.ViewHolder(binding.root)
    {

        val context: Context = binding.root.context

        fun bind(data: CorrectiveActivitiesModel) {
            binding.shapeableImageView.setImageResource(data.bgMainImg)
            binding.tvTitile.text = data.title
            binding.tvDesc.text = data.desc

            if(data.isOpen == true){
                binding.lytOpenCloseTag.setBackgroundResource(R.drawable.back_hm_optimal)
                binding.tvOpenCloseTag.text = context.getString(R.string.text_open)

                binding.tvTimerTag.text = data.time
                binding.lytTimerTag.visible()
            }else{
                binding.lytOpenCloseTag.setBackgroundResource(R.drawable.back_hm_warning)
                binding.tvOpenCloseTag.text = context.getString(R.string.text_open)

                binding.lytTimerTag.gone()
            }

            data.onlyImgWithText?.let { lytData ->
                binding.lytWithProgressBar.gone()

                binding.ivOnlyImg.setImageResource(lytData.img)
                if(lytData.txt.isNullOrEmpty()){
                    binding.tvOnlyText.gone()
                }else{
                    binding.tvOnlyText.text = lytData.txt
                }

                binding.lytImageWithText.visible()

                /*binding.lytImageWithText.apply {
                    //code
                    visible()
                }*/
            }

            data.progressBarLytData?.let { lytData ->
                binding.lytImageWithText.gone()



                binding.imageView88.setImageResource(lytData.img)
                binding.tvProgressTxt.text = lytData.txt

                binding.lytWithProgressBar.visible()

                /*binding.lytWithProgressBar.apply {
                    //code
                    visible()
                }*/
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCorrectiveActivitiesCircadianBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    fun updateDataSet(list: List<CorrectiveActivitiesModel>){
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    fun updateSingleElement(data: CorrectiveActivitiesModel?, pos: Int){
        if(data != null && pos < mList.size){
            mList[pos] = data
            notifyItemChanged(pos)
        }
    }

}