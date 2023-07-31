package com.noisefit.ui.watchface.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.R

class ColorsAdapter(val listener: ColorListAction) :
    RecyclerView.Adapter<ColorsAdapter.ViewHolder>() {
    private var mDataSet = ArrayList<Int>()

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(color: Int) {
            view.findViewById<View>(R.id.vColor).setBackgroundColor(color)
            view.findViewById<View>(R.id.vColor).setOnClickListener {
                listener.onColorSelected(color)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_color, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataSet[position])
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(watchFaceColorList: java.util.ArrayList<Int>) {
        mDataSet = watchFaceColorList
        notifyDataSetChanged()
    }
}

interface ColorListAction {
    fun onColorSelected(color: Int)
}