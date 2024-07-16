//package com.noisefit_commans.utils
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.widget.TextView
//import com.github.mikephil.charting.components.MarkerView
//import com.github.mikephil.charting.data.Entry
//import com.github.mikephil.charting.highlight.Highlight
//import com.noisefit_commans.data.model.MarkerEntry
//import com.noisefit_commans.R
//
///**
// * Custom implementation of the MarkerView.
// */
//@SuppressLint("ViewConstructor")
//class XYMarkerView(context: Context?, xAxisValueFormatter: ArrayList<MarkerEntry>) :
//    MarkerView(context, R.layout.custom_marker_view) {
//    private val tvContent: TextView = findViewById(R.id.tvMarketData)
//    private val tvDate: TextView = findViewById(R.id.tvMarketDate)
//    private val tvType: TextView = findViewById(R.id.tvMarketType)
//    private val xAxisValueFormatter: ArrayList<MarkerEntry> = xAxisValueFormatter
//
//    // runs every time the MarkerView is redrawn, can be used to update the
//    // content (user-interface)
//    override fun refreshContent(e: Entry, highlight: Highlight) {
//        tvContent.text = xAxisValueFormatter[e.x.toInt()].value
//        tvDate.text = xAxisValueFormatter[e.x.toInt()].date
//        tvType.text = xAxisValueFormatter[e.x.toInt()].type
//        super.refreshContent(e, highlight)
//    }
////
////    override fun getOffset(): MPPointF {
////       // LOGS.d("OFFSET $width ${-height}")
////        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
////    }
//
////    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
////        val offset = getOffsetForDrawingAtPoint(posX, posY)
////        val saveId: Int = canvas.save()
////        // translate to the correct position and draw
////        canvas.translate(0f, 0f)
////        canvas1.draw(canvas)
////        canvas.restoreToCount(saveId)
////    }
//}
