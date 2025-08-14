package com.oreo.ui.circadianAlignment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.ItemCorrectiveActivitiesCircadianBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.setVisibilityByCondition
import com.noisefit_commans.ui.visible
import com.oreo.data.model.CorrectiveActivitiesModel

class CorrectiveActivitiesAdapter(
    val onLogClick: (CorrectiveActivitiesModel) -> Unit
):
    RecyclerView.Adapter<CorrectiveActivitiesAdapter.ViewHolder>() {

        private val mList: ArrayList<CorrectiveActivitiesModel> = ArrayList()

    inner class ViewHolder(val binding: ItemCorrectiveActivitiesCircadianBinding):
        RecyclerView.ViewHolder(binding.root)
    {

        val context: Context = binding.root.context

        fun bind(data: CorrectiveActivitiesModel) {
            val context = binding.root.context

            binding.shapeableImageView.setImageResource(data.bgMainImg)
            binding.tvTitile.text = data.title
            binding.tvDesc.text = data.desc

            when(data.key){
                CircadianAlignmentViewModel.light_exposure_key,
                CircadianAlignmentViewModel.meal_window_key,
                CircadianAlignmentViewModel.caffeine_window_key -> binding.llLytLog.visible()
                else -> binding.llLytLog.invisible()
            }

            data.onlyImgWithText?.let { lytData ->
                binding.ivOnlyImg.setImageResource(lytData.img)
                binding.tvOnlyText.text = lytData.txt
                binding.lytImageWithText.visible()

                binding.lytWithProgressBar.gone()
                binding.ivOnlyOnlyImg.gone()
            }

            data.progressBarLytData?.let { lytData ->
                binding.imageView88.setImageResource(lytData.img)
                binding.tvProgressTxt.text = lytData.txt

                binding.circularProgressBar.apply {
                    max = lytData.totalProgress
                    progress = lytData.currentProgress
                    when(data.key){
                        CircadianAlignmentViewModel.workout_key -> setIndicatorColor("#78C3F9".toColorInt())
                        CircadianAlignmentViewModel.daily_steps_key -> setIndicatorColor("#98D76B".toColorInt())
                        else -> {}
                    }
                }
                binding.lytWithProgressBar.visible()

                binding.lytImageWithText.gone()
                binding.ivOnlyOnlyImg.gone()
            }

            data.onlyOnlyImgLytData?.let {
                binding.ivOnlyOnlyImg.apply {
                    setBackgroundResource(it)
                    visible()
                }

                binding.lytWithProgressBar.gone()
                binding.lytImageWithText.gone()
            }

            if(data.showFooter==true){
                if (data.time==null){
                    binding.lytTimerTag.gone()
                    binding.tvOpenCloseTag.text = context.getString(R.string.text_opens_today)
                    binding.lytOpenCloseTag.setBackgroundResource(R.drawable.bg_opens_today_circadian)
                }
                else if(data.time == "0"){
                    binding.lytTimerTag.gone()
                    binding.tvOpenCloseTag.text = context.getString(R.string.text_opens_tomorrow)
                    binding.lytOpenCloseTag.setBackgroundResource(R.drawable.bg_opens_today_circadian)
                }
                else{
                    binding.imageView99.setVisibilityByCondition(data.logStatus==true)
                    binding.tvOpenCloseTag.text = context.getString(R.string.text_open)
                    binding.lytOpenCloseTag.setBackgroundResource(R.drawable.bg_open_tag_circadian)

                    binding.tvTimerTag.text = data.time
                    binding.lytTimerTag.visible()
                }

                binding.lytFooter.visible()
            }

            binding.llLytLog.setOnClickListener {
                onLogClick(data)
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