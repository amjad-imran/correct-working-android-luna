package com.noisefit.ui.reward.tasks

import android.os.SystemClock
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.model.TaskList
import com.noisefit.luna.databinding.ItemAlltaskBinding
import com.noisefit_commans.models.TaskEnums
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS

class AllTasksAdapter(val listener: OnCollectClickListener) :
    RecyclerView.Adapter<AllTasksAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<TaskList>()
    var mLastClickTime: Long? = null


    inner class ViewHolder(val binding: ItemAlltaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(taskData: TaskList) {

            if (taskData.title.isNullOrEmpty()) {
                binding.tvTitle.text = "Complete Daily Step Goal"
                binding.ivCollectMoney.setImageResource(R.drawable.ic_task_c_steps)
                binding.tvEarnedCoin.invisible()
                binding.ivCoin.invisible()
                binding.ivNext.visible()

                binding.root.setOnClickListener {
                    listener.onItemClicked(null)
                }
            } else {

                if (taskData.status == null) {
                    /*if (shouldShowArrow(taskData.taskEnum ?: "")) {
                        binding.ivNext.visible()
                    } else {
                        binding.ivNext.gone()
                    }*/
                    binding.ivNext.visible()
                    binding.tvCollect.gone()
                } else if (taskData.status?.lowercase() == "earned") {
                    binding.ivNext.gone()
                    binding.tvCollect.visible()
                } else {
                    binding.ivNext.gone()
                    binding.tvCollect.gone()
                }

                binding.ivCollectMoney.loadImage(binding.ivCollectMoney.context, taskData.imageUrl)
                if (taskData.type.equals("npl")) {
                    binding.ivCoin.gone()
                    binding.tvEarnedCoin.visible()
                    binding.tvTitle.text = DateFormats.formatTimeNpl(taskData.date)
                    binding.tvEarnedCoin.text = AppConstants.teamNameMapping(null,taskData?.title?:"") +" won"
                } else {
                    binding.tvTitle.text = taskData.title
                    binding.tvEarnedCoin.visible()
                    binding.ivCoin.visible()
                    binding.tvEarnedCoin.text = taskData.points?.toString()
                }
                binding.tvCollect.setOnClickListener {

                    mLastClickTime?.let {
                        if (SystemClock.elapsedRealtime() - it < 2000) {
                            LOGS.d("Returning from watchface click")
                            return@setOnClickListener
                        }
                    }
                    mLastClickTime = SystemClock.elapsedRealtime()

                    listener.onCollectClick(
                        taskData, bindingAdapterPosition
                    )
                }
                binding.root.setOnClickListener {
                    if (taskData.status == null) {
                        listener.onItemClicked(taskData)
                    }
                }
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlltaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(allTaskResult: List<TaskList>) {
        mDataSet.clear()
        mDataSet.addAll(allTaskResult)
        notifyDataSetChanged()
    }

    fun updateItemStatus(statusValue: String, position: Int) {
        tryCatch {
            mDataSet[position].status = statusValue
            notifyItemChanged(position)
        }

    }

    fun shouldShowArrow(taskEnum: String): Boolean {
        return when (taskEnum) {
            TaskEnums.WATCH_PAIR.type, TaskEnums.PROFILE.type, TaskEnums.CUSTOM_WATCHFACE.type, TaskEnums.CHALLENGE_PARTICIPATION.type, TaskEnums.FRIEND_ADDED.type, TaskEnums.SHARE.type -> true
            else -> false
        }

    }

    fun removeItem(position: Int) {
        tryCatch {
            mDataSet.removeAt(position)
            notifyItemRemoved(position)
            notifyDataSetChanged()
        }
    }


}

interface OnCollectClickListener {
    fun onCollectClick(taskData: TaskList, position: Int)
    fun onItemClicked(taskData: TaskList?)
}