package com.noisefit.ui.feeds.create

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.data.model.PostBackground
import com.noisefit.luna.databinding.RowPostImageSelectionBinding
import com.noisefit_commans.ui.loadImage


class ImagesAdapter(val onUserClicked: (postBackground: PostBackground, position: Int) -> Unit) :
    RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<PostBackground>()
    private var selectedPos = -1

    inner class ViewHolder(val binding: RowPostImageSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(postBackground: PostBackground) {
            binding.tvImageName.text = postBackground.title

            if (selectedPos == bindingAdapterPosition) {
                binding.vBackSelector.setBackgroundResource(R.drawable.back_modal_purple)
            } else {
                binding.vBackSelector.setBackgroundResource(com.noisefit_commans.R.drawable.back_modal_new)
            }

            setImage(binding, postBackground)



            binding.root.setOnClickListener {
                onUserClicked(postBackground, bindingAdapterPosition)
            }

        }

        private fun setImage(
            binding: RowPostImageSelectionBinding,
            postBackground: PostBackground
        ) {
            binding.ivMain.setImageURI(null)
            binding.ivMain.destroyDrawingCache()
            if (postBackground.imageUri != null) {
                binding.ivMain.setImageURI(postBackground.imageUri.toUri())
                return
            }
            if (postBackground.resourceId != null) {
                binding.ivMain.setImageResource(postBackground.resourceId)
                return
            }

            if (!postBackground.imageUrl.isNullOrEmpty()) {
                binding.ivMain.loadImage(binding.ivMain.context, postBackground.imageUrl)
                return
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RowPostImageSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    fun setDataSet(data: List<PostBackground>, selectedDefault: Int = 0) {
        mDataSet.clear()
        mDataSet.addAll(data)
        this.selectedPos = selectedDefault
        notifyDataSetChanged()
    }

    fun setSelected(position: Int) {
        selectedPos = position
        notifyDataSetChanged()
    }

}