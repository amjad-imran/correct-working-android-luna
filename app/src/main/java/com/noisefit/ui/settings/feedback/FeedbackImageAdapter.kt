package com.noisefit.ui.settings.feedback

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.databinding.LayoutFeedbackImageListBinding
import com.noisefit_commans.ui.loadImage


class FeedbackImageAdapter(private val listener: FeedbackAction) :
    RecyclerView.Adapter<FeedbackImageAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<Uri>()

    inner class ViewHolder(private val binding: LayoutFeedbackImageListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) {
            binding.imageView.loadImage(itemView.context, uri)
            binding.deleteBtn.setOnClickListener {
                deleteImage(uri)
                listener.onFeedbackRemoved()
            }
        }

    }

    private fun deleteImage(uri: Uri) {
        mDataSet.remove(uri)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutFeedbackImageListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int = mDataSet.size

    fun setDataSet(dataSet: List<Uri>) {
        mDataSet.clear()
        mDataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    fun add(uri: Uri) {
        val itemCount = this.itemCount
        mDataSet.add(itemCount, uri)
        notifyItemInserted(itemCount)

    }
}

interface FeedbackAction {
    fun onFeedbackRemoved()
}

