//package com.oreo.ui.sleep
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import androidx.viewbinding.ViewBinding
//import com.noisefit.luna.R
//import com.noisefit.luna.databinding.ListOSleepBottomCardItemBinding
//import com.noisefit.luna.databinding.ListOSleepHeaderCardItemBinding
//import com.noisefit_commans.utils.LOGS
//import com.oreo.data.model.OSleepDetails
//
//
//sealed class OSleepDetailClickEnum {
//    object MeasureHRClick : OSleepDetailClickEnum()
//    object PairDeviceClicked : OSleepDetailClickEnum()
//
//}
//
//class OSleepDetailAdapter :
//    RecyclerView.Adapter<SleepDetailsDRecyclerViewHolder>() {
//
//    var devicePaired = false
//    var lastPosition = -1
//    var refreshPosition: Int? = null
//
//    var items = listOf<OSleepDetails>()
//        set(value) {
//            field = value
////            if (refreshPosition != null) {
////                if (refreshPosition != -1) {
////                    notifyItemChanged(refreshPosition!!)
////                } else {
////                    notifyDataSetChanged()
////                }
////            }
//            notifyDataSetChanged()
//
//        }
//
//    var itemClickListener: ((type: OSleepDetailClickEnum) -> Unit)? = null
//
//
//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int
//    ): SleepDetailsDRecyclerViewHolder {
//        return when (viewType) {
//
//            R.layout.list_o_sleep_bottom_card_item -> SleepDetailsDRecyclerViewHolder.BottomViewHolder(
//                ListOSleepBottomCardItemBinding.inflate(
//                    LayoutInflater.from(parent.context),
//                    parent,
//                    false
//                )
//            )
//
//            R.layout.list_o_sleep_header_card_item -> SleepDetailsDRecyclerViewHolder.HeaderViewHolder(
//                ListOSleepHeaderCardItemBinding.inflate(
//                    LayoutInflater.from(parent.context),
//                    parent,
//                    false
//                )
//            )
//
//
//            else -> throw IllegalArgumentException("Invalid ViewType Provided")
//        }
//    }
//
//    override fun onBindViewHolder(holder: SleepDetailsDRecyclerViewHolder, position: Int) {
//        holder.itemClickListener = itemClickListener
//        when (holder) {
//            is SleepDetailsDRecyclerViewHolder.HeaderViewHolder -> holder.bind(
//                items[position] as OSleepDetails.Header,
//                position,
//                lastPosition,
//                devicePaired
//            )
//
//            is SleepDetailsDRecyclerViewHolder.BottomViewHolder -> holder.bind(
//                items[position] as OSleepDetails.Bottom,
//                position,
//                lastPosition,
//                devicePaired
//            )
//
//        }
//    }
//
//    override fun getItemCount() = items.size
//
//    override fun getItemViewType(position: Int): Int {
//        return when (items[position]) {
//            is OSleepDetails.Header -> R.layout.list_o_sleep_header_card_item
//            is OSleepDetails.Bottom -> R.layout.list_o_sleep_bottom_card_item
//
////            is OSleepDetails.Demo -> R.layout.list_o_w_demo_card_item
//        }
//    }
//}
//
//
//sealed class SleepDetailsDRecyclerViewHolder(binding: ViewBinding) :
//    RecyclerView.ViewHolder(binding.root) {
//
//    var itemClickListener: ((type: OSleepDetailClickEnum) -> Unit)? = null
//
//
//    class HeaderViewHolder(private val binding: ListOSleepHeaderCardItemBinding) :
//        SleepDetailsDRecyclerViewHolder(binding) {
//        fun bind(
//            data: OSleepDetails.Header,
//            position: Int,
//            lastPosition: Int,
//            devicePaired: Boolean
//        ) {
//
//            var scoreValue = 0
//            LOGS.d("sadsadsdasda ${data.list.size}")
//            binding.rvTopGraph.updateData(data.list, data.prefix, data.suffix)
//
//
//            binding.root.setOnClickListener {
//                //   itemClickListener?.invoke(it, data, position)
//            }
//        }
//    }
//
//
//    class BottomViewHolder(private val binding: ListOSleepBottomCardItemBinding) :
//        SleepDetailsDRecyclerViewHolder(binding) {
//        fun bind(
//            data: OSleepDetails.Bottom,
//            position: Int,
//            lastPosition: Int,
//            devicePaired: Boolean
//        ) {
//
//
//            binding.root.setOnClickListener {
//                //   itemClickListener?.invoke(it, data, position)
//            }
//        }
//    }
//
//
//}
//
//
//
