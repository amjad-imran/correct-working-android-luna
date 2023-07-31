package com.noisefit.ui.settings.helpAndSupport.details

import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R
import com.noisefit_commans.data.response.HelpAndSupportContent
import com.noisefit.databinding.ItemHSContentListBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.html
import com.noisefit_commans.ui.visible
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragment.Companion.FORMAT_BREAK_LINE
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragment.Companion.OPEN_APP_COMPATIBILITY_ARGS
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragment.Companion.OPEN_APP_SETTING_ARGS
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragment.Companion.OPEN_BATTERY_SAVER_ARGS
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragment.Companion.OPEN_CHECK_EXPIRATION_DATE_ARGS
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragment.Companion.OPEN_GET_THE_VOUCHER_DETAILS_ARGS
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragment.Companion.OPEN_IGNORE_BATTERY_OPTIMISATION_ARGS
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragment.Companion.OPEN_NOTIFICATION_SETTING_ARGS
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragment.Companion.OPEN_PHONE_SETTING_ARGS
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragment.Companion.OPEN_PLAYSTORE_ARGS
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragment.Companion.OPEN_TASK_LIST_ARGS
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragment.Companion.OPEN_VOUCHER_ARGS
import com.noisefit.ui.settings.helpAndSupport.details.HelpAndSupportDetailsFragment.Companion.OPEN_WATCH_FIRMWARE_ARGS


class HelpAndSupportContentAdapter(val listener: HelpAndSupportContentInteractionListener) :
    RecyclerView.Adapter<HelpAndSupportContentAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<HelpAndSupportContent>()


    inner class ViewHolder(private val binding: ItemHSContentListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(value: HelpAndSupportContent) {
            binding.tvTitle.text = value.title
           // LOGS.d("dsasdasaddsadsa ${Gson().toJson(value)}")

//            if(value.title?.equals("important information",true) == true){
//                binding.tvOtherDesc.text = value.text?.html()
//            }else{
//            binding.tvOtherDesc.text = value.text?.replace("<p>", "")?.replace("</p>", "")?.html()
//            binding.tvOtherDesc.text = Html.fromHtml("<a href=\"http://www.google.com\">Google</a>")
//            }


            if (value.actionId == FORMAT_BREAK_LINE) {
                val list = value.text?.split("<br>") ?: ArrayList()
                binding.rvOtherDesc.visible()
                binding.tvOtherDesc.gone()
                val adapter = OtherInfoAdapter()
                binding.rvOtherDesc.layoutManager = LinearLayoutManager(binding.rvOtherDesc.context)
                binding.rvOtherDesc.adapter = adapter
                adapter.setDataSet(list)
            } else {
                binding.tvOtherDesc.visible()
                binding.rvOtherDesc.gone()
                binding.tvOtherDesc.text = clearHtmlText(value.text)
                binding.tvOtherDesc.movementMethod = LinkMovementMethod.getInstance()
            }


            val layoutManager = LinearLayoutManager(
                binding.rv.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            //  layoutManager.initialPrefetchItemCount = value.mediaUrl?.size ?: 0

            val helpAndSupportImageAdapter = HelpAndSupportImageAdapter()
            if (value.mediaUrl.isNullOrEmpty()) {
                binding.rv.gone()
            } else {
                binding.rv.visible()
                binding.rv.apply {
                    setLayoutManager(layoutManager)
                    adapter = helpAndSupportImageAdapter
                    setRecycledViewPool(RecyclerView.RecycledViewPool())
                }
                helpAndSupportImageAdapter.setDataSet(value.mediaUrl!!)
            }




            when (value.actionId) {
                OPEN_WATCH_FIRMWARE_ARGS -> {
                    binding.btnAction.text =
                        binding.btnAction.context.getString(R.string.text_go_to_firmware_update)
                    showActionButton(binding)
                }
                OPEN_PLAYSTORE_ARGS -> {
                    binding.btnAction.text =
                        binding.btnAction.context.getString(R.string.text_go_to_play_store)
                    showActionButton(binding)
                }
                OPEN_APP_SETTING_ARGS -> {
                    showActionButton(binding)
                }
                OPEN_IGNORE_BATTERY_OPTIMISATION_ARGS, OPEN_BATTERY_SAVER_ARGS -> {
                    showActionButton(binding)
                }
                OPEN_PHONE_SETTING_ARGS -> {
                    showActionButton(binding)
                }
                OPEN_NOTIFICATION_SETTING_ARGS -> {
                    showActionButton(binding)
                }
                OPEN_CHECK_EXPIRATION_DATE_ARGS-> {
                    binding.btnAction.text =
                        binding.btnAction.context.getString(R.string.text_click_here)
                    showActionButton(binding)
                }
                OPEN_GET_THE_VOUCHER_DETAILS_ARGS-> {
                    binding.btnAction.text =
                        binding.btnAction.context.getString(R.string.text_get_the_voucher_details)
                    showActionButton(binding)
                }
                OPEN_TASK_LIST_ARGS -> {
                    binding.btnAction.text =
                        binding.btnAction.context.getString(R.string.text_check_task_list)
                    showActionButton(binding)
                }
                OPEN_VOUCHER_ARGS -> {
                    binding.btnAction.text =
                        binding.btnAction.context.getString(R.string.text_check_voucher_here)
                    showActionButton(binding)
                }
                OPEN_APP_COMPATIBILITY_ARGS -> {
                    binding.btnAction.text =
                        binding.btnAction.context.getString(R.string.text_check_app_compatibility)
                    showActionButton(binding)
                }
                else -> {
                    hideActionButton(binding)
                }
            }
            binding.btnAction.setOnClickListener {
                listener.onHelpAndSupportClick(value.actionId ?: 1)
            }
        }
    }

    private fun clearHtmlText(text: String?): Spanned? {
        return text?.replace("<p>", "")?.replace("</p>", "")?.replace("\\", "")?.html()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): HelpAndSupportContentAdapter.ViewHolder {
        val binding = ItemHSContentListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    private fun showActionButton(binding: ItemHSContentListBinding) {
        binding.btnAction.visible()
        binding.arrow.visible()
    }

    private fun hideActionButton(binding: ItemHSContentListBinding) {
        binding.btnAction.gone()
        binding.arrow.gone()
    }

    override fun onBindViewHolder(holder: HelpAndSupportContentAdapter.ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(dataList: List<HelpAndSupportContent>) {
        mDataSet.clear()
        mDataSet.addAll(dataList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mDataSet.size


}

interface HelpAndSupportContentInteractionListener {
    fun onHelpAndSupportClick(actionId: Int)
}
