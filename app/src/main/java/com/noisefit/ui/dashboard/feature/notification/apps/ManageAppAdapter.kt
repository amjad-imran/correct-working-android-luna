package com.noisefit.ui.dashboard.feature.notification.apps

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.noisefit_commans.data.model.NotificationApp
import com.noisefit.databinding.LayoutManageAppItemsBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible


class ManageAppAdapter(private val action: ManageAppListener) :
    RecyclerView.Adapter<ManageAppAdapter.ViewHolder>(), Filterable {

    private var notificationResult = ArrayList<NotificationApp>()
    private var notificationFilteredResults = ArrayList<NotificationApp>()

    private var mEditMode = false

    fun setEditMode(status: Boolean) {
        mEditMode = status
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: LayoutManageAppItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notificationApp: NotificationApp, position: Int) {
            binding.tvTitle.text = notificationApp.appDisplayName

//            val imageDrawable = notificationApp.appPackageName?.let {
//                NoiseFitApplicationMain.context!!.packageManager
//                    .getApplicationIcon(it)
//            }

            binding.iconImv.loadImage(binding.iconImv.context,notificationApp.imageDrawable)
           // binding.iconImv.setImageDrawable(notificationApp.imageDrawable)

            if (mEditMode) {
                binding.checkbox.visible()
            } else {
                binding.checkbox.gone()
            }


            binding.checkbox.setOnClickListener { view ->
                val isChecked = (view as CompoundButton).isChecked
                action.onNotificationClick(notificationApp, isChecked, position, binding.checkbox)
            }
            binding.checkbox.isChecked = notificationApp.isEnabled

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutManageAppItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notificationFilteredResults[position], position)


    }

    override fun getItemCount(): Int {
        return notificationFilteredResults.size
    }


    fun setDataSet(dataList: ArrayList<NotificationApp>) {
        notificationFilteredResults.clear()
        notificationFilteredResults.addAll(dataList)
        notificationResult.clear()
        notificationResult.addAll(dataList)
        notifyDataSetChanged()
    }

    fun getCurrentDataSet(): List<NotificationApp> {
        return notificationFilteredResults
    }

    fun updateNotificationList(position: Int, isChecked: Boolean) {
        notificationFilteredResults[position].isEnabled = isChecked
//        notifyItemChanged(position)
    }
    fun removeItem(notificationApp: NotificationApp) {
        try {


            notificationFilteredResults.forEachIndexed { index, nApp ->
                if(nApp.appPackageName.equals(notificationApp.appPackageName,true)){
                    notificationFilteredResults.removeAt(index)
                    notifyItemRemoved(index)
                    return
                }
            }


        } catch (exp: ArrayIndexOutOfBoundsException) {
            exp.printStackTrace()
            //CASE : when Swap is in progress
        }

    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = ArrayList<NotificationApp>()
                //  println("here we are  $charSearch")
                if (charSearch.isEmpty()) {
                    //Toast.makeText(context,"Char is Empty", Toast.LENGTH_SHORT).show()
                    resultList.addAll(notificationResult)
                } else {
                    //Toast.makeText(context,"Char NOT Empty", Toast.LENGTH_SHORT).show()
                    for (row in notificationResult) {
                        if (row.appDisplayName.contains(constraint.toString(),true)
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
                notificationFilteredResults.clear()
                notificationFilteredResults.addAll(results?.values as ArrayList<NotificationApp>)
                notifyDataSetChanged()
            }
        }
    }


}

interface ManageAppListener {
    fun onNotificationClick(
        notificationApp: NotificationApp,
        isChecked: Boolean,
        position: Int,
        checkBox: CheckBox
    )
}